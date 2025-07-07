package org.itol.demo.http.client;

import okhttp3.*;

public class HttpClient {
    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://127.0.0.1:11000")
                .build();

        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("连接已建立");
                webSocket.send("{\"RequestNo\":3,\"SendQryOrder\":{\"OrderID\":\"1000565760833093\"}}");
                webSocket.send("{\"RequestNo\":4,\"SendQryOrder\":{\"OrderID\":\"1000565760833093\"}}");
                webSocket.send("{\"RequestNo\":7,\"SendQryOrder\":{\"OrderID\":\"1000565760833093\"}}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                System.out.println("收到消息: " + text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("正在关闭: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("连接已关闭: " + code + " " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.err.println("发生错误: " + t.getMessage());
            }
        });

        // 保持程序运行
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.dispatcher().executorService().shutdown();
    }
}
