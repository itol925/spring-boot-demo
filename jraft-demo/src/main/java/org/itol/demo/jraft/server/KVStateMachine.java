package org.itol.demo.jraft.server;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.entity.LogEntry;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KVStateMachine extends StateMachineAdapter {
    private final ConcurrentMap<String, String> store = new ConcurrentHashMap<>();

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        // 持久化当前 store 数据为快照
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        // 从快照恢复数据
        return true;
    }

    @Override
    public void onApply(Iterator iterator) {
        while (iterator.hasNext()) {
            ByteBuffer data = iterator.next();
            String command = new String(data.array());

            // 假设 command 是 "PUT key value"
            String[] parts = command.split(" ");
            if (parts[0].equals("PUT")) {
                store.put(parts[1], parts[2]);
            }
        }
    }
}