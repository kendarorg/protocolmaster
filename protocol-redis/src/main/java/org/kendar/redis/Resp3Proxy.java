package org.kendar.redis;

import com.fasterxml.jackson.databind.JsonNode;
import org.kendar.protocol.context.NetworkProtoContext;
import org.kendar.protocol.messages.ReturnMessage;
import org.kendar.proxy.NetworkProxy;
import org.kendar.proxy.NetworkProxySocket;
import org.kendar.redis.fsm.events.Resp3Message;
import org.kendar.redis.utils.Resp3ProxySocket;
import org.kendar.redis.utils.Resp3Storage;
import org.kendar.storage.StorageItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.List;

public class Resp3Proxy extends NetworkProxy<Resp3Storage> {

    public Resp3Proxy(String connectionString, String userId, String password) {
        super(connectionString, userId, password);
    }

    public Resp3Proxy() {
        super();
    }

    @Override
    protected NetworkProxySocket buildProxyConnection(NetworkProtoContext context, InetSocketAddress inetSocketAddress, AsynchronousChannelGroup group) {
        try{
        return new Resp3ProxySocket(context,
                    new InetSocketAddress(InetAddress.getByName(host), port), group);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Resp3Proxy.class);
    @Override
    protected Object getData(Object of) {
        if(of instanceof Resp3Message) {
            return ((Resp3Message) of).getData();
        }
        return of;
    }

    @Override
    protected void sendBackResponses(List<StorageItem<JsonNode, JsonNode>> storageItems) {
        if (storageItems.isEmpty()) return;
        for (var item : storageItems) {
            var out = item.getOutput();
            var type = out.get("type").textValue();

            int connectionId = item.getConnectionId();
            if (type.equalsIgnoreCase("RESPONSE")) {
                log.debug("[SERVER][CB]: RESPONSE");
                var ctx = Resp3Protocol.consumeContext.get(connectionId);

                ReturnMessage fr = new Resp3Message(ctx, null, out.get("data"));
                ctx.write(fr);
            } else {
                throw new RuntimeException("MISSING RESPONSE_CLASS");
            }

        }
    }
}
