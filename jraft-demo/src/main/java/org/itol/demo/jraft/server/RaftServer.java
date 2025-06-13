package org.itol.demo.jraft.server;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.option.NodeOptions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RaftServer {
    private RaftGroupService raftGroupService;
    private final StateMachineAdapter kvStateMachine = new KVStateMachine();
    private Node node;
    public void startRaftNode(String dataPath, String groupId, String serverId, String peers) {
        PeerId server = new PeerId();
        if (!server.parse(serverId)) {
            throw new IllegalArgumentException("invalid serverId " + serverId);
        }
        Configuration configuration = new Configuration();
        if (!configuration.parse(peers)) {
            throw new IllegalArgumentException("invalid peers" + peers);
        }

        // 配置 raft 选项
        NodeOptions options = new NodeOptions();
        options.setElectionTimeoutMs(1000); // 选举超时时间1s
        options.setSnapshotIntervalSecs(30); // 30秒生成一次快照
        options.setLogUri(dataPath + "/log");
        options.setRaftMetaUri(dataPath + "/meta");
        options.setSnapshotUri(dataPath + "/snapshot");
        options.setInitialConf(configuration);
        options.setFsm(kvStateMachine);

        raftGroupService = new RaftGroupService(groupId, server, options);
        node = raftGroupService.start();
    }

    public void appendLog(String message) {
        if (!node.isLeader()) {
            System.out.println("当前节点不是 Leader，不能写入数据." + node.getNodeId());
            return;
        }

        Task task = new Task();
        task.setData(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
        task.setDone((Status status) -> {
            if (!status.isOk()) {
                System.out.println("日志提交失败: " + status);
            } else {
                System.out.println("日志提交成功: " + message);
            }
        });

        node.apply(task);
    }

    public void stopRaftNode() {
        if (node != null) {
            node.shutdown();
        }
        if (raftGroupService != null) {
            raftGroupService.shutdown();
        }
    }
}
