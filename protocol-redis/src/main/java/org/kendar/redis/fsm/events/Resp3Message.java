package org.kendar.redis.fsm.events;

import com.fasterxml.jackson.databind.JsonNode;
import org.kendar.buffers.BBuffer;
import org.kendar.protocol.context.ProtoContext;
import org.kendar.protocol.events.BaseEvent;
import org.kendar.protocol.messages.NetworkReturnMessage;
import org.kendar.protocol.states.TaggedObject;
import org.kendar.redis.parser.Resp3Parser;

public class Resp3Message extends BaseEvent implements TaggedObject, NetworkReturnMessage {
    private static Resp3Parser parser = new Resp3Parser();
    private Object data;
    private String message;

    public Resp3Message(ProtoContext context, Class<?> prevState, JsonNode data) {
        super(context, prevState);
        try {
            this.message = parser.serialize(data);
            this.data = parser.parse(message);
        } catch (Exception ex) {
            throw new RuntimeException("UNABLE TO DESERIALIZE FROM JSON NODE");
        }
    }

    public Resp3Message(ProtoContext context, Class<?> prevState, Object data, String message) {
        super(context, prevState);
        this.data = data;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @Override
    public void write(BBuffer resultBuffer) {
        try {
            var data = message.getBytes("ASCII");
            resultBuffer.write(data);
        } catch (Exception ex) {
            System.err.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAARRRH");
        }
    }
}

