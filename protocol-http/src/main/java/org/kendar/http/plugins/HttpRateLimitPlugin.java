package org.kendar.http.plugins;

import org.kendar.http.utils.Request;
import org.kendar.http.utils.Response;
import org.kendar.plugins.PluginDescriptor;
import org.kendar.plugins.ProtocolPhase;
import org.kendar.plugins.ProtocolPluginDescriptor;
import org.kendar.proxy.PluginContext;
import org.kendar.settings.PluginSettings;
import org.kendar.utils.FileResourcesUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class HttpRateLimitPlugin extends ProtocolPluginDescriptor<Request, Response> {
    private final Object sync = new Object();
    private HttpRateLimitPluginSettings settings;
    private List<Pattern> recordSites = new ArrayList<>();
    private Calendar resetTime;
    private int resourcesRemaining = -1;
    private Response customResponse;

    @Override
    protected void handleActivation(boolean active) {
        synchronized (sync) {
            resetTime = null;
            resourcesRemaining = -1;
        }
    }

    @Override
    public boolean handle(PluginContext pluginContext, ProtocolPhase phase, Request in, Response out) {
        if (isActive()) {
            var request = (Request) in;
            if (!recordSites.isEmpty()) {
                var matchFound = false;
                for (var pat : recordSites) {
                    if (pat.matcher(request.getHost()).matches()) {// || pat.toString().equalsIgnoreCase(request.getHost())) {
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    return false;
                }
            }
            return handleRateLimit(pluginContext, phase, in, out);
        }
        return false;
    }

    @Override
    public PluginDescriptor setSettings(PluginSettings plugin) {
        setActive(plugin.isActive());
        settings = (HttpRateLimitPluginSettings) plugin;
        setupSitesToRecord(settings.getRecordSites());
        if (settings.getCustomResponseFile() != null && Files.exists(Path.of(settings.getCustomResponseFile()))) {
            var frr = new FileResourcesUtils();
            customResponse = mapper.deserialize(frr.getFileFromResourceAsString(settings.getCustomResponseFile()), Response.class);
        }
        return this;
    }

    private void setupSitesToRecord(List<String> recordSites) {
        this.recordSites = recordSites.stream()
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(regex -> regex.startsWith("@") ?
                        Pattern.compile(regex.substring(1)) :
                        Pattern.compile(Pattern.quote(regex))).collect(Collectors.toList());
    }

    @Override
    public List<ProtocolPhase> getPhases() {
        return List.of(ProtocolPhase.PRE_CALL);
    }

    @Override
    public String getId() {
        return "rate-limit-plugin";
    }

    @Override
    public String getProtocol() {
        return "http";
    }

    @Override
    public void terminate() {

    }

    @Override
    public Class<?> getSettingClass() {
        return HttpRateLimitPluginSettings.class;
    }

    private boolean handleRateLimit(PluginContext pluginContext, ProtocolPhase phase, Request in, Response out) {

        synchronized (sync) {
            // set the initial values for the first request
            if (resetTime == null) {
                resetTime = Calendar.getInstance();
                resetTime.setTimeInMillis(Calendar.getInstance().getTimeInMillis() + (settings.getResetTimeWindowSeconds() * 1000L));
            }
            if (resourcesRemaining == -1) {
                resourcesRemaining = settings.getRateLimit();
            }

            // see if we passed the reset time window
            if (Calendar.getInstance().after(resetTime)) {
                resourcesRemaining = settings.getRateLimit();
                resetTime = Calendar.getInstance();
                resetTime.setTimeInMillis(Calendar.getInstance().getTimeInMillis() + (settings.getResetTimeWindowSeconds() * 1000L));
            }

            // subtract the cost of the request
            resourcesRemaining -= settings.getCostPerRequest();
            if (resourcesRemaining < 0) {
                resourcesRemaining = 0;

                var isnt = Calendar.getInstance();
                isnt.setTimeInMillis(
                        resetTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());


                var reset = settings.getResetFormat().equalsIgnoreCase("SecondsLeft") ?
                        isnt.getTimeInMillis() / 1000 :  // drop decimals
                        resetTime.toInstant().getEpochSecond();

                //Logger.LogRequest($"Exceeded resource limit when calling {request.Url}. Request will be throttled", MessageType.Failed, new LoggingContext(e.Session));
                if (settings.getCustomResponseFile() != null && Files.exists(Path.of(settings.getCustomResponseFile()))) {
                    out.getHeaders().clear();
                    out.getHeaders().putAll(customResponse.getHeaders());
                    out.removeHeader(settings.getHeaderRetryAfter());
                    out.addHeader(settings.getHeaderRetryAfter(), "" + (isnt.getTimeInMillis() / 1000));
                    out.setResponseText(customResponse.getResponseText());
                    out.setStatusCode(customResponse.getStatusCode());
                    return true;
                } else {
                    out.addHeader(settings.getHeaderLimit(), settings.getRateLimit() + "");
                    out.addHeader(settings.getHeaderReset(), reset + "");
                    out.addHeader(settings.getHeaderRetryAfter(), "" + (isnt.getTimeInMillis() / 1000));
                    out.setStatusCode(429);
                    return true;
                }
            } else if (resourcesRemaining < (settings.getRateLimit() -
                    (settings.getRateLimit() * settings.getWarningThresholdPercent() / 100))) {
                out.addHeader(settings.getHeaderLimit(), settings.getRateLimit() + "");
                out.addHeader(settings.getHeaderRemaining(), resourcesRemaining + "");
            }

        }
        return false;
    }
}