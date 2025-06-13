package org.itol.demo.jraft.server;

import com.alipay.sofa.jraft.entity.LogEntry;
import com.alipay.sofa.jraft.storage.impl.RocksDBLogStorage;

import java.nio.charset.StandardCharsets;

public class RaftLogReader {
    private final RocksDBLogStorage logStorage;
    private long index = -1;

    public RaftLogReader(String path) {
        this.logStorage = new RocksDBLogStorage(path, null);
        index = logStorage.getFirstLogIndex();
    }

    public void readLogs() {
        while (index < logStorage.getLastLogIndex()) {
            LogEntry entry = logStorage.getEntry(index);
            System.out.println("日志内容：" + new String(entry.getData().array(), StandardCharsets.UTF_8));
            index++;
        }
    }
}
