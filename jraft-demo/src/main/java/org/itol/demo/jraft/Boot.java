package org.itol.demo.jraft;

import org.itol.demo.jraft.server.RaftLogReader;
import org.itol.demo.jraft.server.RaftServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Boot {
    private static final List<RaftServer> serverList = new ArrayList<>();

    private static final List<RaftLogReader> readerList = new ArrayList<>();

    public static void main(String[] args) {
        int count = 0;
        while (true) {
            BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
            try {
                String read = scanner.readLine();
                if ("1".equals(read)) {
                    startServers();
                } else if ("2".equals(read)) {
                    append("hello " + count++);
                } else if ("3".equals(read)) {
                    read();
                } else if ("quit".equals(read)) {
                    for (RaftServer s : serverList) {
                        s.stopRaftNode();
                    }
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static void startServers() {
        String dataPath = "/Users/panyinglong/workspace/spring-boot-demo/jraft-demo/src/main/resources/";
        String groupId = "test_raft_group";
        String peers = "127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083";
        String[] peer = peers.split(",");
        for (int i = 0; i < peer.length; i++) {
            String serverId = peer[i];
            RaftServer server = new RaftServer();
            server.startRaftNode(dataPath + "node" + i, groupId, serverId, peers);
            serverList.add(server);

            RaftLogReader reader = new RaftLogReader(dataPath + "node" + i);
            readerList.add(reader);
        }
    }

    private static void append(String msg) {
        for (RaftServer server : serverList) {
            server.appendLog(msg);
        }
    }

    private static void read() {
        readerList.get(0).readLogs();
    }
}
