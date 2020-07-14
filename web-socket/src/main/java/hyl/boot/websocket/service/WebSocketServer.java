package hyl.boot.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    private Session session;

    private String sid;

    private static AtomicInteger onlineNum = new AtomicInteger();

    private static ConcurrentHashMap<String, WebSocketServer> webSocketSet = new ConcurrentHashMap<>();

    public static synchronized AtomicInteger getOnlineNum(){
        return onlineNum;
    }

    public static synchronized void addOnlineNum(){
        WebSocketServer.onlineNum.incrementAndGet();
    }
    public static synchronized void subOnlineNum(){
        WebSocketServer.onlineNum.decrementAndGet();
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid){
        this.sid = sid;
        this.session = session;
        webSocketSet.put(sid, this);
        addOnlineNum();
        log.info(sid + "加入连接——： " + onlineNum);
    }

    @OnClose
    public void onClose(){
        if (!sid.equals("")){
            webSocketSet.remove(this.sid);
            subOnlineNum();
            log.info(sid + "断开连接——：" + onlineNum);
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        log.info("客户端：" + message);
        webSocketSet.get(sid).sendMessage("send:" + message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("发生错误!");
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        //同步发送
//        this.session.getBasicRemote().sendText(message);
        //异步发送
        this.session.getAsyncRemote().sendText(message);
    }
}
