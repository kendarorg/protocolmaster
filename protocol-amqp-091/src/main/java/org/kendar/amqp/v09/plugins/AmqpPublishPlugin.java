package org.kendar.amqp.v09.plugins;

import org.kendar.amqp.v09.apis.AmqpPublishPluginApis;
import org.kendar.plugins.base.ProtocolPhase;
import org.kendar.plugins.base.ProtocolPluginApiHandler;
import org.kendar.plugins.base.ProtocolPluginDescriptorBase;
import org.kendar.proxy.PluginContext;
import org.kendar.settings.PluginSettings;

import java.util.List;

public class AmqpPublishPlugin extends ProtocolPluginDescriptorBase<PluginSettings> {
    @Override
    public List<ProtocolPhase> getPhases() {
        return List.of(ProtocolPhase.NONE);
    }

    public boolean handle(PluginContext pluginContext, ProtocolPhase phase, Object in, Object out) {
        return false;
    }

    @Override
    public String getProtocol() {
        return "amqp091";
    }

    @Override
    public String getId() {
        return "publish-plugin";
    }

    @Override
    protected ProtocolPluginApiHandler buildApiHandler() {
        return new AmqpPublishPluginApis(this, getId(), getInstanceId());
    }
}
