package org.kendar.http;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import org.kendar.http.settings.HttpProtocolSettings;
import org.kendar.http.utils.ConnectionBuilderImpl;
import org.kendar.http.utils.callexternal.ExternalRequesterImpl;
import org.kendar.http.utils.converters.RequestResponseBuilderImpl;
import org.kendar.http.utils.dns.DnsMultiResolverImpl;
import org.kendar.http.utils.plugins.PluginClassesHandlerImpl;
import org.kendar.http.utils.rewriter.RemoteServerStatus;
import org.kendar.http.utils.rewriter.SimpleRewriterConfig;
import org.kendar.http.utils.rewriter.SimpleRewriterHandlerImpl;
import org.kendar.http.utils.ssl.CertificatesManager;
import org.kendar.http.utils.ssl.FileResourcesUtils;
import org.kendar.plugins.PluginDescriptor;
import org.kendar.protocol.context.ProtoContext;
import org.kendar.protocol.descriptor.NetworkProtoDescriptor;
import org.kendar.protocol.descriptor.ProtoDescriptor;
import org.kendar.proxy.ProxyServer;
import org.kendar.server.KendarHttpsServer;
import org.kendar.settings.GlobalSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

public class HttpProtocol extends NetworkProtoDescriptor {
    private static final Logger log = LoggerFactory.getLogger(HttpProtocol.class);
    private final GlobalSettings globalSettings;
    private final HttpProtocolSettings settings;
    private final List<PluginDescriptor> plugins;
    private ProxyServer proxy;
    private HttpsServer httpsServer;
    private HttpServer httpServer;
    private boolean httpRunning;
    private boolean httpsRunning;

    public HttpProtocol(GlobalSettings globalSettings, HttpProtocolSettings settings, List<PluginDescriptor> plugins) {

        this.globalSettings = globalSettings;
        this.settings = settings;
        this.plugins = plugins;
    }

    private static <T> T getOrDefault(Object value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    private static SimpleRewriterConfig loadRewritersConfiguration(HttpProtocolSettings settings) {
        var proxyConfig = new SimpleRewriterConfig();
        for (var i = 0; i < settings.getRewrites().size(); i++) {
            var rw = settings.getRewrites().get(i);
            if (rw.getWhen() == null || rw.getThen() == null) {
                continue;
            }
            var remoteServerStatus = new RemoteServerStatus(i + "",
                    rw.getWhen(),
                    rw.getThen(),
                    rw.getTest());
            if (rw.getTest() == null || rw.getTest().isEmpty()) {
                remoteServerStatus.setRunning(true);
                remoteServerStatus.setForce(true);
            } else {
                remoteServerStatus.setRunning(true);
                remoteServerStatus.setForce(rw.isForceActive());
            }
            proxyConfig.getProxies().add(remoteServerStatus);
        }
        return proxyConfig;
    }

    private static HttpsServer createHttpsServer(CertificatesManager certificatesManager,
                                                 InetSocketAddress sslAddress, int backlog, String cname, String der,
                                                 String key, List<String> hosts) throws Exception {
        var httpsServer = new KendarHttpsServer(sslAddress, backlog);

        certificatesManager.setupSll(httpsServer, hosts, cname, der, key);
        return httpsServer;
    }

    public List<PluginDescriptor> getPlugins() {
        return plugins;
    }

    @Override
    public boolean isWrapper() {
        return true;
    }

    @Override
    public boolean isBe() {
        return false;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    protected void initializeProtocol() {

    }

    @Override
    protected ProtoContext createContext(ProtoDescriptor protoDescriptor, int contextId) {
        return null;
    }

    @Override
    public void terminate() {
        for (var i = plugins.size() - 1; i >= 0; i--) {
            var plugin = plugins.get(i);
            plugin.terminate();
        }
        proxy.terminate();
        httpsServer.stop(0);
        httpServer.stop(0);
        httpRunning = false;
        httpsRunning = false;
    }

    @Override
    public boolean isWrapperRunning() {
        return proxy.isRunning() && httpRunning && httpsRunning;
    }

    @Override
    public void start() {
        try {
            int port = getOrDefault(settings.getHttp(), 4080);
            int httpsPort = getOrDefault(settings.getHttps(), 4443);
            var proxyPort = getOrDefault(settings.getProxy(), 9999);
//        log.info("LISTEN HTTP: " + port);
//        log.info("LISTEN HTTPS: " + httpsPort);
//        log.info("LISTEN PROXY: " + proxyPort);
            var backlog = 60;
            var useCachedExecutor = true;
            var address = new InetSocketAddress(port);
            var sslAddress = new InetSocketAddress(httpsPort);


            // initialise the HTTP server
            var proxyConfig = loadRewritersConfiguration(settings);
            var dnsHandler = new DnsMultiResolverImpl();
            var connectionBuilder = new ConnectionBuilderImpl(dnsHandler);
            var requestResponseBuilder = new RequestResponseBuilderImpl();


            httpServer = HttpServer.create(address, backlog);
            log.debug("Http created");

            var sslDer = getOrDefault(settings.getSSL().getDer(), "resource://certificates/ca.der");
            var sslKey = getOrDefault(settings.getSSL().getKey(), "resource://certificates/ca.key");
            var cname = getOrDefault(settings.getSSL().getCname(), "C=US,O=Local Development,CN=local.org");

            var certificatesManager = new CertificatesManager(new FileResourcesUtils());
            httpsServer = createHttpsServer(certificatesManager,
                    sslAddress, backlog, cname, sslDer, sslKey, settings.getSSL().getHosts());
            log.debug("Https created");


            proxy = new ProxyServer(proxyPort)
                    .withHttpRedirect(port).withHttpsRedirect(httpsPort)
                    .withDnsResolver(host -> {
                        try {
                            certificatesManager.setupSll(httpsServer, List.of(host), cname, sslDer, sslKey);
                        } catch (Exception e) {
                            return host;
                        }
                        return "127.0.0.1";
                    }).
                    ignoringHosts("static.chartbeat.com").
                    ignoringHosts("detectportal.firefox.com").
                    ignoringHosts("firefox.settings.services.mozilla.com").
                    ignoringHosts("incoming.telemetry.mozilla.org").
                    ignoringHosts("push.services.mozilla.com");

            log.debug("Proxy created");
            proxy.start();


            for (var i = plugins.size() - 1; i >= 0; i--) {
                var plugin = plugins.get(i);
                plugin.initialize(globalSettings, settings);
            }

            log.debug("Filters added");
            var handler = new MasterHandler(
                    new PluginClassesHandlerImpl(plugins),
                    new SimpleRewriterHandlerImpl(proxyConfig, dnsHandler),
                    new RequestResponseBuilderImpl(),
                    new ExternalRequesterImpl(requestResponseBuilder, dnsHandler, connectionBuilder),
                    connectionBuilder);

            httpServer.createContext("/", handler);
            httpsServer.createContext("/", handler);
            if (useCachedExecutor) {
                httpServer.setExecutor(Executors.newCachedThreadPool());
                httpsServer.setExecutor(Executors.newCachedThreadPool());
            } else {
                httpServer.setExecutor(null); // creates a default executor
                httpsServer.setExecutor(null);
            }
            httpsServer.start();
            httpServer.start();
            log.debug("Servers started");
            httpRunning = true;
            httpsRunning = true;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}