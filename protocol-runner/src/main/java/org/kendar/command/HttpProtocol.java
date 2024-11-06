package org.kendar.command;

import org.apache.commons.cli.Options;
import org.kendar.filters.FilterDescriptor;
import org.kendar.server.TcpServer;
import org.kendar.storage.generic.StorageRepository;
import org.kendar.utils.ini.Ini;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class HttpProtocol extends CommonProtocol{
    @Override
    public void run(String[] args, boolean isExecute, Ini go, Options options) throws Exception {
        options.addOption("http", true, "Http port (def 4080)");
        options.addOption("https", true, "Https port (def 4443)");
        options.addOption("proxy", true, "Http/s proxy port (def 9999)");
        options.addOption("apis", true, "The base url for special TPM controllers (def specialApisRoot)");

        options.addOption("cname", true, "Root cname");
        options.addOption("der", true, "Root certificate");
        options.addOption("key", true, "Root certificate keys");


        options.addOption("record", false, "Set if recording");

        options.addOption("replay", false, "Set if replaying");
        options.addOption("blockExternal", false, "Set if should block external sites replaying");

        options.addOption("showError", true, "The error to show (404/500 etc)");
        options.addOption("errorPercent", true, "The error percent to generate (default 50)");
        if(!isExecute)return;
        setData(args,options,go);
    }


    private void setData(String[] args, Options options, Ini ini) throws Exception {

    }

    @Override
    public String getDefaultPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void start(ConcurrentHashMap<String, TcpServer> protocolServer, String key, Ini ini, String protocol, StorageRepository storage, ArrayList<FilterDescriptor> filters, Supplier<Boolean> stopWhenFalseAction) {
   /*     AtomicBoolean stopWhenFalse = new AtomicBoolean(true);
        var port = ini.getValue(sectionKey, "http.port", Integer.class, 8085);
        var httpsPort = ini.getValue(sectionKey, "https.port", Integer.class, port + 400);
        var proxyPort = ini.getValue(sectionKey, "port.proxy", Integer.class, 9999);
        log.info("LISTEN HTTP: " + port);
        log.info("LISTEN HTTPS: " + httpsPort);
        log.info("LISTEN PROXY: " + proxyPort);
        var backlog = 60;
        var useCachedExecutor = true;
        var address = new InetSocketAddress(port);
        var sslAddress = new InetSocketAddress(httpsPort);


        // initialise the HTTP server
        var proxyConfig = loadRewritersConfiguration(sectionKey,ini);
        var dnsHandler = new DnsMultiResolverImpl();
        var connectionBuilder = new ConnectionBuilderImpl(dnsHandler);
        var requestResponseBuilder = new RequestResponseBuilderImpl();

        var certificatesManager = new CertificatesManager(new FileResourcesUtils());
        var httpServer = HttpServer.create(address, backlog);

        var der = ini.getValue(sectionKey+"-ssl", "der", String.class, "resources://certificates/ca.der");
        var key = ini.getValue(sectionKey+"-ssl", "key", String.class, "resources://certificates/ca.key");
        var cname = ini.getValue(sectionKey+"-ssl", "cname", String.class,"C=US,O=Local Development,CN=local.org");

        var httpsServer = createHttpsServer(certificatesManager,sslAddress, backlog,cname, der, key);


        var proxy = new ProxyServer(proxyPort)
                .withHttpRedirect(port).withHttpsRedirect(httpsPort)
                .withDnsResolver(host -> {
                    try {
                        certificatesManager.setupSll(httpsServer, List.of(host),cname, der, key);
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
        new Thread(proxy).start();

        var globalFilter = new GlobalFilter();

        filters.add(globalFilter);
        filters.add(new RecordingFilter());
        filters.add(new ErrorFilter());
        filters.add(new MockFilter());
        for (var i = filters.size() - 1; i >= 0; i--) {
            var filter = filters.get(i);
            var section = ini.getSection(sectionKey+"-"+filter.getId());
            if (!filter.getId().equalsIgnoreCase("global") &&
                    !ini.getValue(sectionKey+"-"+filter.getId(), "active", Boolean.class, false)) {
                filters.remove(i);
                continue;
            }
            log.info("EXTENSION: " + filter.getId());
            filter.initialize(section);
        }
        globalFilter.setFilters(filters);
        globalFilter.setServer(httpServer, httpsServer);
        globalFilter.setShutdownVariable(stopWhenFalse);
        var handler = new MasterHandler(
                new FilteringClassesHandlerImpl(filters),
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
        new Thread(()->{
            while(stopWhenFalseFunction.get()){
                Sleeper.sleep(100);
            }
            stopWhenFalse.set(false);
        }).start();*/
    }

    @Override
    public String getId() {
        return "http";
    }
}