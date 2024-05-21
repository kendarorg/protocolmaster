package org.kendar.redis.fsm;

import org.kendar.buffers.BBuffer;
import org.kendar.protocol.messages.NetworkReturnMessage;
import org.kendar.protocol.messages.ProtoStep;
import org.kendar.protocol.states.ProtoState;
import org.kendar.proxy.ProxyConnection;
import org.kendar.redis.Resp3Context;
import org.kendar.redis.Resp3Proxy;
import org.kendar.redis.fsm.events.Resp3Message;
import org.kendar.utils.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class Resp3PullState extends ProtoState implements NetworkReturnMessage {
    private static final Logger log = LoggerFactory.getLogger(Resp3PullState.class);
    private Resp3Message event;
    private boolean proxy;

    public Resp3PullState() {
        super();
    }

    public Resp3PullState(Class<?>... events) {
        super(events);
    }

    public Resp3Message getEvent() {
        return event;
    }

    public Resp3PullState asProxy() {
        this.proxy = true;
        return this;
    }

    public boolean isProxyed() {
        return proxy;
    }

    @Override
    public void write(BBuffer resultBuffer) {
        try {
            var bytes = event.getMessage().getBytes(StandardCharsets.US_ASCII);
            resultBuffer.write(bytes);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public boolean canRun(Resp3Message event) {
        if(isProxyed() && event.getData() instanceof List) {
            if (((List<?>) event.getData()).get(0) != null && ((List<?>) event.getData()).get(0).toString().equalsIgnoreCase("message")) {
                return true;
            }

        }
        if(isProxyed()) {
            return false;
        }
        return true;
    }

    protected static final JsonMapper mapper = new JsonMapper();
    public Iterator<ProtoStep> execute(Resp3Message event) {
        var context = (Resp3Context) event.getContext();
        var proxy = (Resp3Proxy) context.getProxy();
        var connection = ((ProxyConnection) event.getContext().getValue("CONNECTION"));

        if (isProxyed()) {
            if(event.getData() instanceof List){
                if(((List<?>) event.getData()).get(0)!=null && ((List<?>) event.getData()).get(0).toString().equalsIgnoreCase("message")){
                    var storage = proxy.getStorage();
                    var res = "{\"type\":\"RESPONSE\",\"data\":" +
                            mapper.serialize(event.getData()) + "}";


                    storage.write(
                            context.getContextId(),
                            null
                            , mapper.toJsonNode(res)
                            , 0, "RESPONSE", "RESP3");
                    return iteratorOfList(event);
                }

            }
            return iteratorOfEmpty();

        }
        return iteratorOfRunnable(() -> proxy.execute(context,
                connection,
                event,
                new Resp3Response()
        ));
//        if (!this.proxy) {
//            return iteratorOfRunnable(() -> proxy.execute(context,
//                    connection,
//                    event,
//                    new Resp3PullState().asProxy()
//            ));
//        } else {
//            this.event = event;
//            return iteratorOfList(event);
//        }
    }
}
