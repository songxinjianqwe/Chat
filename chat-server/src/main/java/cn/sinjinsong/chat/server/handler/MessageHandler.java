package cn.sinjinsong.chat.server.handler;

import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.Message;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public interface MessageHandler {
    void handle(Message message, Selector server, SelectionKey client, BlockingQueue<DownloadInfo> queue);
}
