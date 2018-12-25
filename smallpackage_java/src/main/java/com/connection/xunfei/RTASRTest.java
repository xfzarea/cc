package com.connection.xunfei;

import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.connection.xunfei.util.EncryptUtil;

/**
 * ʵʱתд����demo
 * ��demoֻ��һ���򵥵ĵ���ʾ�������ʺ��õ�ʵ������������
 * 
 * @author white
 *
 */
public class RTASRTest {

    // appid
    private static final String APPID = "5bda9a52";

    // appid��Ӧ��secret_key
    private static final String SECRET_KEY = "d2b3b2eec6cc60760c783472ef1b30f8";

    // �����ַ
    private static final String HOST = "rtasr.xfyun.cn/v1/ws";

    private static final String BASE_URL = "ws://" + HOST;

    private static final String ORIGIN = "http://" + HOST;

    // ��Ƶ�ļ�·��
    private static final String AUDIO_PATH = "E:/8.mp3";

    // ÿ�η��͵����ݴ�С 1280 �ֽ�
    private static final int CHUNCKED_SIZE = 1280;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");

    public static void main(String[] args) throws Exception {
        while (true) {
            URI url = new URI(BASE_URL + getHandShakeParams(APPID, SECRET_KEY));
            DraftWithOrigin draft = new DraftWithOrigin(ORIGIN);
            CountDownLatch handshakeSuccess = new CountDownLatch(1);
            CountDownLatch connectClose = new CountDownLatch(1);
            MyWebSocketClient client = new MyWebSocketClient(url, draft, handshakeSuccess, connectClose);
            
            client.connect();
            
            while (!client.getReadyState().equals(READYSTATE.OPEN)) {
                System.out.println(getCurrentTimeStr() + "\t������");
                Thread.sleep(1000);
            }
            
            // �ȴ����ֳɹ�
            handshakeSuccess.await();
            System.out.println(sdf.format(new Date()) + " ��ʼ������Ƶ����");
            // ������Ƶ
            byte[] bytes = new byte[CHUNCKED_SIZE];
            try (RandomAccessFile raf = new RandomAccessFile(AUDIO_PATH, "r")) {
                int len = -1;
                long lastTs = 0;
                while ((len = raf.read(bytes)) != -1) {
                    if (len < CHUNCKED_SIZE) {
                        send(client, bytes = Arrays.copyOfRange(bytes, 0, len));
                        break;
                    }

                    long curTs = System.currentTimeMillis();
                    if (lastTs == 0) {
                        lastTs = System.currentTimeMillis();
                    } else {
                        long s = curTs - lastTs;
                        if (s < 40) {
                            System.out.println("error time interval: " + s + " ms");
                        }
                    }
                    send(client, bytes);
                    // ÿ��40���뷢��һ������
                    Thread.sleep(40);
                }
                
                // ���ͽ�����ʶ
                send(client,"{\"end\": true}".getBytes());
                System.out.println(getCurrentTimeStr() + "\t���ͽ�����ʶ���");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // �ȴ����ӹر�
            connectClose.await();
            break;
        }
    }

    // �������ֲ���
    public static String getHandShakeParams(String appId, String secretKey) {
        String ts = System.currentTimeMillis()/1000 + "";
        String signa = "";
        try {
            signa = EncryptUtil.HmacSHA1Encrypt(EncryptUtil.MD5(appId + ts), secretKey);
            return "?appid=" + appId + "&ts=" + ts + "&signa=" + URLEncoder.encode(signa, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void send(WebSocketClient client, byte[] bytes) {
        if (client.isClosed()) {
            throw new RuntimeException("client connect closed!");
        }

        client.send(bytes);
    }

    public static String getCurrentTimeStr() {
        return sdf.format(new Date());
    }

    public static class MyWebSocketClient extends WebSocketClient {

        private CountDownLatch handshakeSuccess;
        private CountDownLatch connectClose;

        public MyWebSocketClient(URI serverUri, Draft protocolDraft, CountDownLatch handshakeSuccess, CountDownLatch connectClose) {
            super(serverUri, protocolDraft);
            this.handshakeSuccess = handshakeSuccess;
            this.connectClose = connectClose;
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            System.out.println(getCurrentTimeStr() + "\t���ӽ����ɹ���");
        }

        @Override
        public void onMessage(String msg) {
            JSONObject msgObj = JSON.parseObject(msg);
            String action = msgObj.getString("action");
            if (Objects.equals("started", action)) {
                // ���ֳɹ�
                System.out.println(getCurrentTimeStr() + "\t���ֳɹ���sid: " + msgObj.getString("sid"));
                handshakeSuccess.countDown();
            } else if (Objects.equals("result", action)) {
                // תд���
                System.out.println(getCurrentTimeStr() + "\tresult: " + getContent(msgObj.getString("data")));
            } else if (Objects.equals("error", action)) {
                // ���ӷ�������
                System.out.println("Error: " + msg);
                System.exit(0);
            }
        }

        @Override
        public void onError(Exception e) {
            System.out.println(getCurrentTimeStr() + "\t���ӷ�������" + e.getMessage() + ", " + new Date());
            e.printStackTrace();
            System.exit(0);
        }

        @Override
        public void onClose(int arg0, String arg1, boolean arg2) {
            System.out.println(getCurrentTimeStr() + "\t���ӹر�");
            connectClose.countDown();
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            try {
                System.out.println(getCurrentTimeStr() + "\t����˷��أ�" + new String(bytes.array(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    // ��תд�������Ϊ����
    public static String getContent(String message) {
        StringBuffer resultBuilder = new StringBuffer();
        try {
            JSONObject messageObj = JSON.parseObject(message);
            JSONObject cn = messageObj.getJSONObject("cn");
            JSONObject st = cn.getJSONObject("st");
            JSONArray rtArr = st.getJSONArray("rt");
            for (int i = 0; i < rtArr.size(); i++) {
                JSONObject rtArrObj = rtArr.getJSONObject(i);
                JSONArray wsArr = rtArrObj.getJSONArray("ws");
                for (int j = 0; j < wsArr.size(); j++) {
                    JSONObject wsArrObj = wsArr.getJSONObject(j);
                    JSONArray cwArr = wsArrObj.getJSONArray("cw");
                    for (int k = 0; k < cwArr.size(); k++) {
                        JSONObject cwArrObj = cwArr.getJSONObject(k);
                        String wStr = cwArrObj.getString("w");
                        resultBuilder.append(wStr);
                    }
                }
            } 
        } catch (Exception e) {
            return message;
        }

        return resultBuilder.toString();
    }
}