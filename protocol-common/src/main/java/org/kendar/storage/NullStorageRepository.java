package org.kendar.storage;

import org.kendar.storage.generic.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NullStorageRepository implements StorageRepository {

    @Override
    public void initialize() {

    }

    @Override
    public void write(LineToWrite lineToWrite) {

    }

    @Override
    public void finalizeWrite(String instanceId) {

    }

    @Override
    public LineToRead read(String instanceId, CallItemsQuery query) {
        return null;
    }

    @Override
    public List<StorageItem> readResponses(String instanceId, ResponseItemQuery query) {
        return List.of();
    }

    @Override
    public byte[] readAsZip() {
        return new byte[0];
    }

    @Override
    public void writeZip(byte[] byteArray) {

    }
    private final AtomicLong counter = new AtomicLong(0);
    @Override
    public long generateIndex() {
        return counter.incrementAndGet();
    }

    @Override
    public String getType() {
        return "storage";
    }
}
