package org.kendar.http.plugins;

import org.kendar.http.settings.HttpProtocolSettings;
import org.kendar.plugins.*;
import org.kendar.proxy.PluginContext;
import org.kendar.settings.GlobalSettings;
import org.kendar.settings.ProtocolSettings;

import java.util.List;

public class SSLDummyPlugin extends ProtocolPluginDescriptor<String,String> implements AlwaysActivePlugin {
    private HttpProtocolSettings protocolSettings;

    @Override
    public boolean handle(PluginContext pluginContext, ProtocolPhase phase, String in, String out) {
        return false;
    }

    @Override
    public PluginDescriptor initialize(GlobalSettings global, ProtocolSettings protocol) {
        super.initialize(global, protocol);
        this.protocolSettings = (HttpProtocolSettings)protocol;
        return this;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    protected PluginApiHandler buildApiHandler() {
        return new SSLApiHandler(this,getId(),getInstanceId(),protocolSettings);
    }

    @Override
    public List<ProtocolPhase> getPhases() {
        return List.of();
    }

    @Override
    public String getId() {
        return "ssl-plugin";
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
        return null;
    }
}