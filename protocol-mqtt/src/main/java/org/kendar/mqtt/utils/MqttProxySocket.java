package org.kendar.mqtt.utils;

import org.kendar.buffers.BBuffer;
import org.kendar.mqtt.fsm.GenericFrame;
import org.kendar.mqtt.fsm.MqttPacketTranslator;
import org.kendar.mqtt.fsm.events.MqttPacket;
import org.kendar.protocol.context.NetworkProtoContext;
import org.kendar.protocol.events.BaseEvent;
import org.kendar.protocol.events.BytesEvent;
import org.kendar.protocol.states.ProtoState;
import org.kendar.proxy.NetworkProxySocket;
import org.kendar.proxy.NetworkProxySplitterState;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.ArrayList;
import java.util.List;

public class MqttProxySocket extends NetworkProxySocket {
    private final MqttPacketTranslator translator = new MqttPacketTranslator().asProxy();

    public MqttProxySocket(NetworkProtoContext context, InetSocketAddress inetSocketAddress, AsynchronousChannelGroup group) {
        super(context, inetSocketAddress, group);
    }

    @Override
    protected NetworkProxySplitterState getStateToRetrieveOneSingleMessage() {
        return new GenericFrame();
    }

    private final List<ProtoState> states = new ArrayList<>();
    //Arrays.asList(
    //            (ProtoState)new ConnectAck()
    //    )

    @Override
    protected List<ProtoState> availableStates() {
        return states;
    }

    @Override
    protected List<? extends BaseEvent> buildPossibleEvents(NetworkProtoContext context, BBuffer buffer) {
        var be = new BytesEvent(context, null, buffer);
        if (translator.canRunEvent(be)) {
            var it = translator.execute(be);
            if (it.hasNext()) {
                var item = it.next().run();
                return List.of((MqttPacket) item);
            }
        }
        return List.of();
    }
}