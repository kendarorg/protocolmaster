package org.kendar.redis;

import org.kendar.protocol.context.NetworkProtoContext;
import org.kendar.protocol.context.ProtoContext;
import org.kendar.protocol.descriptor.NetworkProtoDescriptor;
import org.kendar.protocol.descriptor.ProtoDescriptor;
import org.kendar.protocol.events.BytesEvent;
import org.kendar.protocol.states.special.ProtoStateWhile;
import org.kendar.redis.fsm.Resp3MessageTranslator;
import org.kendar.redis.fsm.Resp3PullState;
import org.kendar.redis.fsm.events.Resp3Message;

import java.util.concurrent.ConcurrentHashMap;

public class Resp3Protocol extends NetworkProtoDescriptor {
    private static final int PORT = 6379;
    private  int port = PORT;
    public static ConcurrentHashMap<Integer, NetworkProtoContext> consumeContext;

    private Resp3Protocol() {
        consumeContext = new ConcurrentHashMap<>();
    }
    public Resp3Protocol(int port) {
        this();
        this.port = port;
    }


    @Override
    public boolean isBe() {
        return true;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    protected void initializeProtocol() {
        addInterruptState(new Resp3MessageTranslator(BytesEvent.class));
        initialize(
            new ProtoStateWhile(
                    new Resp3PullState(Resp3Message.class)));

    }

    @Override
    protected ProtoContext createContext(ProtoDescriptor protoDescriptor) {
        return new Reps3Context(protoDescriptor);
    }
}
