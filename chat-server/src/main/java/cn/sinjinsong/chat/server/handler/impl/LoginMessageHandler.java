package cn.sinjinsong.chat.server.handler.impl;

import cn.sinjinsong.chat.server.user.UserManager;
import cn.sinjinsong.chat.server.handler.MessageHandler;
import cn.sinjinsong.common.domain.*;
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
@Component("MessageHandler.login")
public class LoginMessageHandler implements MessageHandler {
    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<DownloadInfo> queue) {
        SocketChannel clientChannel = (SocketChannel) client.channel();
        MessageHeader header = message.getHeader();
        String username = header.getSender();
        String password = message.getBody();
        try {
            if (userManager.login(clientChannel, username, password)) {

                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp())
                                        .responseCode(ResponseCode.LOGIN_SUCCESS.getCode()).build(),
                                ByteBuffer.wrap(UserManager.LOGIN_SUCCESS)));

                clientChannel.write(ByteBuffer.wrap(response));
            } else {
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .responseCode(ResponseCode.LOGIN_FAILURE.getCode())
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp()).build(),
                                ByteBuffer.wrap(UserManager.LOGIN_FAILURE)));
                clientChannel.write(ByteBuffer.wrap(response));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
