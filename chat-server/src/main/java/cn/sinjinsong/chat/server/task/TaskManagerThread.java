package cn.sinjinsong.chat.server.task;

import cn.sinjinsong.chat.server.exception.handler.ExecutionExceptionHandler;
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
public class TaskManagerThread implements Runnable {
    private ExecutorService pool;
    private BlockingQueue<DownloadInfo> queue;
    private HttpConnectionManager manager;
    private ExecutionExceptionHandler exceptionHandler;

    public TaskManagerThread(ExecutorService pool, BlockingQueue<DownloadInfo> queue) {
        this.pool = pool;
        this.queue = queue;
        this.manager = SpringContextUtil.getBean("httpConnectionManager");
        this.exceptionHandler = SpringContextUtil.getBean("executionExceptionHandler");
    }

    public void shutdown() {
        Thread.currentThread().interrupt();
    }

    /**
     * 如果当前线程被中断，那么Future会抛出InterruptedException，
     * 此时可以通过future.cancel(true)来中断当前线程
     * <p>
     * 由submit方法提交的任务中如果抛出了异常，那么会在ExecutionException中重新抛出
     */
    @Override
    public void run() {
        Future<byte[]> future = null;
        DownloadInfo info;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                info = queue.take();
                MessageHeader header = info.getMessage().getHeader();
                future = pool.submit(new DownloadHandler(info, manager));
                System.out.println(info.getReceiver().getRemoteAddress() + "已执行任务");
                byte[] buffer = future.get();
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(ResponseHeader.builder()
                                .type(ResponseType.FILE)
                                .sender(header.getSender())
                                .timestamp(header.getTimestamp())
                                .build(),
                                buffer));
                info.getReceiver().write(ByteBuffer.wrap(response));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                System.out.println("线程运行时异常");
                exceptionHandler.handle(e.getCause());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                future.cancel(true);
            }
        }
    }
}
