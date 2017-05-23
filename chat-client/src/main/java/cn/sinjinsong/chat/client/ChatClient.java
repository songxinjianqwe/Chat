package cn.sinjinsong.chat.client;

import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.domain.MessageHeader;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.MessageType;
import cn.sinjinsong.common.enumeration.ResponseCode;
import cn.sinjinsong.common.util.DateTimeUtil;
import cn.sinjinsong.common.util.FileUtil;
import cn.sinjinsong.common.util.ProtoStuffUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ChatClient extends Frame {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private Selector selector;
    private SocketChannel clientChannel;
    private ByteBuffer buf;
    private TextField tfText;
    private TextArea taContent;
    private ReceiverHandler listener;
    private String username;
    private boolean isLogin = false;

    public ChatClient(String name, int x, int y, int w, int h) {
        super(name);
        initFrame(x, y, w, h);
        initNetWork();
    }

    /**
     * 初始化窗体
     *
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void initFrame(int x, int y, int w, int h) {
        this.tfText = new TextField();
        this.taContent = new TextArea();
        this.setBounds(x, y, w, h);
        this.setLayout(new BorderLayout());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                disConnect();
                System.exit(0);
            }
        });
        this.taContent.setEditable(false);
        this.add(tfText, BorderLayout.SOUTH);
        this.add(taContent, BorderLayout.NORTH);
        this.tfText.addActionListener((actionEvent) -> {
            String str = tfText.getText().trim();
            tfText.setText("");
            send(str);
        });
        this.pack();
        this.setVisible(true);
    }

    /**
     * 初始化网络模块
     */
    private void initNetWork() {
        try {
            selector = Selector.open();
            clientChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9000));
            //设置客户端为非阻塞模式
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            login();
            listener = new ReceiverHandler();
            new Thread(listener).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        String username = JOptionPane.showInputDialog("请输入用户名");
        String password = JOptionPane.showInputDialog("请输入密码");
        Message message = new Message(
                MessageHeader.builder()
                        .type(MessageType.LOGIN)
                        .sender(username)
                        .timestamp(System.currentTimeMillis())
                        .build(), password);
        try {
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username = username;
    }

    private void disConnect() {
        try {
            logout();
            listener.shutdown();
            clientChannel.socket().close();
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        if (!isLogin) {
            return;
        }
        Message message = new Message(
                MessageHeader.builder()
                        .type(MessageType.LOGOUT)
                        .sender(username)
                        .timestamp(System.currentTimeMillis())
                        .build(), null);
        try {
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送信息，监听在回车键上
     *
     * @param content
     */
    public void send(String content) {
        if (!isLogin) {
            JOptionPane.showMessageDialog(null, "尚未登录");
            return;
        }
        try {
            Message message;
            //普通模式
            if (content.startsWith("@")) {
                String[] slices = content.split(":");
                String receiver = slices[0].substring(1);
                message = new Message(
                        MessageHeader.builder()
                                .type(MessageType.NORMAL)
                                .sender(username)
                                .receiver(receiver)
                                .timestamp(System.currentTimeMillis())
                                .build(), slices[1]);
            } else {
                //广播模式
                message = new Message(
                        MessageHeader.builder()
                                .type(MessageType.BROADCAST)
                                .sender(username)
                                .timestamp(System.currentTimeMillis())
                                .build(), content);
            }
            System.out.println(message);
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于接收信息的线程
     */
    private class ReceiverHandler implements Runnable {
        private boolean connected = true;

        public void shutdown() {
            connected = false;
        }


        public void run() {
            try {
                while (connected) {
                    int size = 0;
                    selector.select();
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                        SelectionKey selectionKey = it.next();
                        it.remove();
                        if (selectionKey.isReadable()) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            while ((size = clientChannel.read(buf)) > 0) {
                                buf.flip();
                                baos.write(buf.array(), 0, size);
                                buf.clear();
                            }
                            byte[] bytes = baos.toByteArray();
                            baos.close();
                            Response response = ProtoStuffUtil.deserialize(bytes, Response.class);
                            handleResponse(response);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleResponse(Response response) {
            System.out.println(response);
            ResponseHeader header = response.getHeader();
            switch (header.getType()) {
                case PROMPT:
                    String info = new String(response.getBody().array());
                    JOptionPane.showMessageDialog(null, info);
                    if (header.getResponseCode() != null) {
                        ResponseCode code = ResponseCode.fromCode(header.getResponseCode());
                        if (code == ResponseCode.LOGIN_SUCCESS) {
                            isLogin = true;
                            System.out.println("登录成功");
                        }
                    }
                    break;
                case NORMAL:
                    String content = formatMessage(taContent.getText(),response);
                    taContent.setText(content);
                    taContent.setCaretPosition(content.length());
                    break;
                case FILE:
                    try {
                        String path = JOptionPane.showInputDialog("请输入保存的文件路径");
                        ByteBuffer buf = response.getBody();
                        FileUtil.save(path, buf);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                default:
                    break;
            }
        }

        private String formatMessage(String originalText, Response response) {
            ResponseHeader header = response.getHeader();
            StringBuilder sb = new StringBuilder();
            sb.append(originalText)
                    .append(header.getSender())
                    .append(": ")
                    .append(new String(response.getBody().array()))
                    .append("    ")
                    .append(DateTimeUtil.formatLocalDateTime(header.getTimestamp()))
                    .append("\n");
            return sb.toString();
        }
    }


    public static void main(String[] args) {
        System.out.println("Initialing...");
        new ChatClient("Client", 200, 200, 300, 200);
    }
}

