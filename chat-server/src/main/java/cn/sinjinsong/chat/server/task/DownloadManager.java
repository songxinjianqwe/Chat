package cn.sinjinsong.chat.server.task;

import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.MessageHeader;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ProtostuffUtil;

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

    public DownloadManager(ExecutorService pool, BlockingQueue<DownloadInfo> queue) {
        this.pool = pool;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                DownloadInfo info = queue.take();
                MessageHeader header = info.getMessage().getHeader();
                Future<ByteBuffer> future = pool.submit(new DownloadHandler(info));
                ByteBuffer buffer = future.get();
                byte[] response = ProtostuffUtil.serialize(
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
