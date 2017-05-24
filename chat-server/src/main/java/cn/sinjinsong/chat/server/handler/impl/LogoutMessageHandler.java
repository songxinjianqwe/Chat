package cn.sinjinsong.chat.server.handler.impl;

import cn.sinjinsong.chat.server.handler.MessageHandler;
import cn.sinjinsong.chat.server.property.PromptMsgProperty;
import cn.sinjinsong.chat.server.user.UserManager;
import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.ResponseCode;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ProtoStuffUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Created by SinjinSong on 2017/5/23.
 */
@Component("MessageHandler.logout")
public class LogoutMessageHandler extends MessageHandler {
    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<DownloadInfo> queue) {
        try {
            SocketChannel clientChannel = (SocketChannel) client.channel();
            userManager.logout(clientChannel);
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(ResponseHeader.builder()
                            .type(ResponseType.PROMPT)
                            .responseCode(ResponseCode.LOGOUT_SUCCESS.getCode())
                            .sender(message.getHeader().getSender())
                            .timestamp(message.getHeader().getTimestamp()).build(),
                            PromptMsgProperty.LOGOUT_SUCCESS.getBytes()));
            clientChannel.write(ByteBuffer.wrap(response));

            //下线广播
            byte[] logoutBroadcast = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.NORMAL)
                                    .sender(SYSTEM_SENDER)
                                    .timestamp(message.getHeader().getTimestamp()).build(),
                            String.format(PromptMsgProperty.LOGOUT_BROADCAST, message.getHeader().getSender()).getBytes()));
            super.broadcast(logoutBroadcast, server);
            System.out.println("客户端退出");
            //必须要cancel，否则无法从keys从去除该客户端
            client.cancel();
            clientChannel.close();
            clientChannel.socket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
