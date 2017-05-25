package cn.sinjinsong.chat.server.task;

import cn.sinjinsong.chat.server.exception.factory.ExceptionHandlingThreadFactory;
import cn.sinjinsong.chat.server.handler.task.BaseTaskHandler;
import cn.sinjinsong.chat.server.http.HttpConnectionManager;
import cn.sinjinsong.chat.server.util.SpringContextUtil;
import cn.sinjinsong.common.domain.Task;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by SinjinSong on 2017/5/23.
 * 消费者
 * 负责从阻塞队列中取出任务并提交给线程池
 */
public class TaskManagerThread extends Thread {
    private ExecutorService taskPool;
    private BlockingQueue<Task> taskBlockingQueue;
    private HttpConnectionManager httpConnectionManager;

    private ExecutorService crawlerPool;


    public TaskManagerThread(BlockingQueue<Task> taskBlockingQueue) {
        this.taskPool = new ThreadPoolExecutor(
                5, 10, 1000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10),
                new ExceptionHandlingThreadFactory(SpringContextUtil.getBean("taskExceptionHandler")),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.taskBlockingQueue = taskBlockingQueue;
        this.httpConnectionManager = SpringContextUtil.getBean("httpConnectionManager");
        this.crawlerPool = new ThreadPoolExecutor(
                5, 10, 1000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void shutdown() {
        taskPool.shutdown();
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
        Task task;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                task = taskBlockingQueue.take();
                System.out.println(task.getReceiver().getRemoteAddress()+"已从阻塞队列中取出");
                BaseTaskHandler taskHandler = SpringContextUtil.getBean("BaseTaskHandler", task.getType().toString().toLowerCase());
                taskHandler.init(task,httpConnectionManager,this);
                System.out.println(taskHandler);
                taskPool.execute(taskHandler);
            }
        } catch (InterruptedException e) {
            //这里也无法得知发来消息的是谁，所以只能直接退出了
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ExecutorService getCrawlerPool() {
        return crawlerPool;
    }
}
