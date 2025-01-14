package org.kendar.http.plugins;

import com.fasterxml.jackson.databind.node.TextNode;
import org.kendar.apis.base.Request;
import org.kendar.apis.base.Response;
import org.kendar.di.annotations.TpmService;
import org.kendar.plugins.base.ProtocolPhase;
import org.kendar.plugins.base.ProtocolPluginDescriptor;
import org.kendar.plugins.base.ProtocolPluginDescriptorBase;
import org.kendar.proxy.PluginContext;
import org.kendar.settings.GlobalSettings;
import org.kendar.settings.PluginSettings;
import org.kendar.settings.ProtocolSettings;
import org.kendar.utils.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@TpmService(tags = "http")
public class HttpErrorPlugin extends ProtocolPluginDescriptorBase<HttpErrorPluginSettings> {

    private static final Logger log = LoggerFactory.getLogger(HttpErrorPlugin.class);
    private int errorCode;
    private String errorMessage;
    private double percentage;

    public HttpErrorPlugin(JsonMapper mapper) {
        super(mapper);
    }

    @Override
    public Class<?> getSettingClass() {
        return HttpErrorPluginSettings.class;
    }

    @Override
    public List<ProtocolPhase> getPhases() {
        return List.of(ProtocolPhase.PRE_CALL);
    }

    @Override
    public String getId() {
        return "error-plugin";
    }

    @Override
    public String getProtocol() {
        return "http";
    }


    public boolean handle(PluginContext pluginContext, ProtocolPhase phase, Request request, Response response) {
        if (!isActive()) return false;
        if (Math.random() < percentage) {

            log.info("Faking ERROR {} {}", request.getMethod(), request.buildUrl());
            response.setStatusCode(errorCode);
            response.setResponseText(new TextNode(errorMessage));
            return true;
        }
        return false;
    }

    @Override
    public ProtocolPluginDescriptor initialize(GlobalSettings global, ProtocolSettings protocol, PluginSettings pluginSetting) {
        super.initialize(global, protocol, pluginSetting);
        var settings = (HttpErrorPluginSettings) pluginSetting;
        this.errorCode = settings.getShowError();
        this.errorMessage = settings.getErrorMessage();
        this.percentage = ((double) settings.getErrorPercent()) / 100.0;
        return this;
    }
}
