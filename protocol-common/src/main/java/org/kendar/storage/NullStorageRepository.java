package org.kendar.storage;

import org.kendar.storage.generic.LineToWrite;
import org.kendar.storage.generic.ResponseItemQuery;
import org.kendar.storage.generic.StorageRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NullStorageRepository implements StorageRepository {

    private final AtomicLong counter = new AtomicLong(0);

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
    public StorageItem readById(String instanceId, long id) {
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

    @Override
    public long generateIndex() {
        return counter.incrementAndGet();
    }

    @Override
    public List<CompactLineComplete> getAllIndexes(int maxLen) {
        return List.of();
    }

    @Override
    public List<CompactLine> getIndexes(String instanceId) {
        return List.of();
    }

    @Override
    public void clean() {

    }

    @Override
    public void update(long itemId, String protocolInstanceId, CompactLine index, StorageItem item) {

    }

    @Override
    public void delete(String instanceId, long itemId) {

    }

    @Override
    public String getType() {
        return "storage";
    }
}
