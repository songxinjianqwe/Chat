package cn.sinjinsong.chat.server.task;

import cn.sinjinsong.chat.server.http.HttpConnectionManager;
import cn.sinjinsong.chat.server.util.SpringContextUtil;
import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.MessageHeader;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ProtoStuffUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by SinjinSong on 2017/5/23.
 * 消费者
 */
public class DownloadManager implements Runnable {
    private ExecutorService pool;
    private BlockingQueue<DownloadInfo> queue;
    private HttpConnectionManager manager;
    private boolean isConnected;
    public DownloadManager(ExecutorService pool, BlockingQueue<DownloadInfo> queue) {
        this.pool = pool;
        this.queue = queue;
        this.manager = SpringContextUtil.getBean("httpConnectionManager");
        this.isConnected = true;
    }
    @Override
    public void run() {
        try {
            while (isConnected) {
                DownloadInfo info = queue.take();
                MessageHeader header = info.getMessage().getHeader();
                Future<byte[]> future = pool.submit(new DownloadHandler(info,manager));
                System.out.println(info.getReceiver().getRemoteAddress()+"已执行任务");
                byte[] buffer = future.get();
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(ResponseHeader.builder()
                                .type(ResponseType.FILE)
                                .sender(header.getSender())
                                .timestamp(header.getTimestamp())
                                .build(),
                                buffer));
                info.getReceiver().write(ByteBuffer.wrap(response));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
