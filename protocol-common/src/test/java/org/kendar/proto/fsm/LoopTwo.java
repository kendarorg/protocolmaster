package org.kendar.proto.fsm;

import org.kendar.proto.silly.SillyTest;
import org.kendar.protocol.events.BytesEvent;
import org.kendar.protocol.messages.ProtoStep;
import org.kendar.protocol.messages.ReturnMessage;
import org.kendar.protocol.states.ProtoState;

import java.util.Iterator;

public class LoopTwo extends ProtoState implements ReturnMessage {
    public static boolean run = true;

    public LoopTwo(Class<?> bytesEventClass) {
        super(bytesEventClass);
    }

    public boolean canRun(BytesEvent event) {
        return run;
    }

    public Iterator<ProtoStep> execute(BytesEvent event) {
        SillyTest.result += "LoopTwo";
        return iteratorOfList(this);
    }
}
