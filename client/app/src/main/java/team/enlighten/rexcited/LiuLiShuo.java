package team.enlighten.rexcited;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by lzn on 2017-05-06.
 */

public class LiuLiShuo {
    private static URI serverUri;
    private final static int audioSource = MediaRecorder.AudioSource.DEFAULT;
    private final static int sampleRate = 16000;
    private final static int channel = AudioFormat.CHANNEL_IN_MONO;
    private final static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final static int audioBufferSize = 2048 * 16 / 8;
    private RecorderThread recorder_thread;
    WebSocket recog_ws, score_ws;
    String result;
    Context context;
    Handler handler;
    JSONObject score_result;

    static {
        try {
            serverUri = new URI("wss://rating.llsstaging.com/llcup/stream/upload");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public LiuLiShuo(Context context) {
        this.context = context;
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Toast.makeText(LiuLiShuo.this.context, msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public class RecorderThread extends Thread {
        AudioRecord recorder;
        ConcurrentLinkedQueue<WebSocket> wss = new ConcurrentLinkedQueue<>();

        @Override
        public void run() {
            byte[] buffer = new byte[audioBufferSize];
            recorder = new AudioRecord(audioSource, sampleRate, channel, audioFormat, audioBufferSize);
            try {
                while (recorder.getState() != AudioRecord.STATE_INITIALIZED)
                    Thread.sleep(100, 0);
            } catch (InterruptedException e) {
                recorder.release();
                return;
            }

            recorder.startRecording();
            for (; ; ) {
                int length = recorder.read(buffer, 0, buffer.length);
                if (length < 0)
                    Log.e("Record", "error: " + Integer.toString(length));
                else {
                    for (WebSocket ws : wss)
                        ws.sendBinary(buffer);
                }
                if (Thread.interrupted()) {
                    recorder.stop();
                    return;
                }
            }
        }
    }

    public void startRecongition() throws InterruptedException, IOException {
        synchronized (this) {
            if (recog_ws != null)
                return;
            result = null;
            WebSocketFactory wsf = new WebSocketFactory();
            recog_ws = wsf.createSocket(serverUri);
        }
        recog_ws.addListener(new WebSocketListener() {
            @Override
            public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
            }

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                JSONObject config = new JSONObject();
                config.put("type", "asr");
                config.put("quality", -1);
                String base64config = Base64.encodeToString(config.toString().getBytes(), Base64.DEFAULT);

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                DataOutputStream os = new DataOutputStream(buffer);
                os.writeInt(base64config.length());
                os.write(base64config.getBytes());
                os.flush();
                os.close();

                recog_ws.sendBinary(buffer.toByteArray());
                Log.d("ASR", "connected");
                synchronized (LiuLiShuo.this) {
                    if (recorder_thread == null) {
                        recorder_thread = new RecorderThread();
                        recorder_thread.start();
                    }
                    recorder_thread.wss.add(recog_ws);
                }
            }

            @Override
            public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {

            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {

            }

            @Override
            public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onTextMessage(WebSocket websocket, String text) throws Exception {

            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(binary));
                int length = is.readInt();
                byte[] strmeta = new byte[length];
                is.read(strmeta);
                JSONObject meta = new JSONObject(new String(strmeta));
                if (meta.getInt("flag") == 0) {
                } else {
                    Log.e("ASR", "got last result");
                    Log.e("ASR", meta.toString());
                    if (meta.getInt("status") == 0) {
                        JSONObject result = new JSONObject(new String(Base64.decode(meta.getString("result"), Base64.DEFAULT)));
                        synchronized (LiuLiShuo.this) {
                            LiuLiShuo.this.result = result.getString("decoded");
                            recog_ws = null;
                        }
                    } else if (meta.getInt("status") == -98) {
                        JSONObject result = new JSONObject(new String(Base64.decode(meta.getString("result"), Base64.DEFAULT)));
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("toast", result.getString("error"));
                        msg.setData(data);
                        handler.sendMessage(msg);
                        synchronized (LiuLiShuo.this) {
                            LiuLiShuo.this.result = "";
                            recog_ws = null;
                        }
                    } else {
                        synchronized (LiuLiShuo.this) {
                            recog_ws = null;
                        }
                    }
                }
            }

            @Override
            public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

            }

            @Override
            public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

            }

            @Override
            public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
            }

            @Override
            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                cause.printStackTrace();
                synchronized (LiuLiShuo.this) {
                    if (recorder_thread != null) {
                        recorder_thread.wss.remove(recog_ws);
                        if (recorder_thread.wss.isEmpty()) {
                            recorder_thread.interrupt();
                            recorder_thread = null;
                        }
                    }
                    recog_ws = null;
                }
            }

            @Override
            public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

            }

            @Override
            public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

            }

            @Override
            public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

            }

            @Override
            public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

            }

            @Override
            public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

            }

            @Override
            public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    recog_ws.connect();
                } catch (WebSocketException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        for (; ; ) {
            synchronized (this) {
                if (recorder_thread != null && recorder_thread.wss.contains(recog_ws))
                    return;
            }
            Thread.sleep(100, 0);
        }
    }

    public String stopRecongition() throws InterruptedException {
        if (recog_ws == null)
            return null;
        synchronized (this) {
            recorder_thread.wss.remove(recog_ws);
            if (recorder_thread.wss.isEmpty()) {
                recorder_thread.interrupt();
                recorder_thread = null;
            }
        }
        recog_ws.sendBinary(new byte[]{0x45, 0x4f, 0x53});
        Log.d("ASR", "EOS sent");
        recog_ws.sendClose();
        for (; ; ) {
            synchronized (this) {
                if (recog_ws == null)
                    return result;
            }
            Thread.sleep(100, 0);
        }
    }

    public void startScore(final String reftext) throws IOException, InterruptedException {
        synchronized (this) {
            if (score_ws != null)
                return;
            score_result = null;
            WebSocketFactory wsf = new WebSocketFactory();
            score_ws = wsf.createSocket(serverUri);
        }
        score_ws.addListener(new WebSocketListener() {
            @Override
            public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
            }

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                JSONObject config = new JSONObject();
                config.put("type", "readaloud");
                config.put("quality", -1);
                config.put("reftext", reftext);
                String base64config = Base64.encodeToString(config.toString().getBytes(), Base64.DEFAULT);

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                DataOutputStream os = new DataOutputStream(buffer);
                os.writeInt(base64config.length());
                os.write(base64config.getBytes());
                os.flush();
                os.close();

                score_ws.sendBinary(buffer.toByteArray());
                synchronized (LiuLiShuo.this) {
                    if (recorder_thread == null) {
                        recorder_thread = new RecorderThread();
                        recorder_thread.start();
                    }
                    recorder_thread.wss.add(score_ws);
                }
                Log.d("Score", "connected");
            }

            @Override
            public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {

            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {

            }

            @Override
            public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onTextMessage(WebSocket websocket, String text) throws Exception {

            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(binary));
                int length = is.readInt();
                byte[] strmeta = new byte[length];
                is.read(strmeta);
                JSONObject meta = new JSONObject(new String(strmeta));
                if (meta.getInt("flag") == 0) {
                } else {
                    Log.d("Score", "got last result");
                    if (meta.getInt("status") == 0) {
                        synchronized (LiuLiShuo.this) {
                            score_result = new JSONObject(new String(Base64.decode(meta.getString("result"), Base64.DEFAULT)));
                            score_ws = null;
                        }
                    } else if (meta.getInt("status") == -98) {
                        JSONObject result = new JSONObject(new String(Base64.decode(meta.getString("result"), Base64.DEFAULT)));
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("toast", result.getString("error"));
                        msg.setData(data);
                        handler.sendMessage(msg);
                        synchronized (LiuLiShuo.this) {
                            LiuLiShuo.this.score_result = null;
                            score_ws = null;
                        }
                    } else {
                        synchronized (LiuLiShuo.this) {
                            recog_ws = null;
                        }
                    }
                }
            }

            @Override
            public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

            }

            @Override
            public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

            }

            @Override
            public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
            }

            @Override
            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                cause.printStackTrace();
                synchronized (LiuLiShuo.this) {
                    if (recorder_thread != null) {
                        recorder_thread.wss.remove(score_ws);
                        if (recorder_thread.wss.isEmpty()) {
                            recorder_thread.interrupt();
                            recorder_thread = null;
                        }
                    }
                    score_ws = null;
                }
            }

            @Override
            public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

            }

            @Override
            public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

            }

            @Override
            public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

            }

            @Override
            public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

            }

            @Override
            public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

            }

            @Override
            public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

            }

            @Override
            public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    score_ws.connect();
                } catch (WebSocketException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        for (; ; ) {
            synchronized (this) {
                if (recorder_thread != null && recorder_thread.wss.contains(score_ws))
                    return;
            }
            Thread.sleep(100, 0);
        }
    }

    public JSONObject stopScore() throws InterruptedException {
        if (score_ws == null)
            return null;
        synchronized (this) {
            recorder_thread.wss.remove(score_ws);
            if (recorder_thread.wss.isEmpty()) {
                recorder_thread.interrupt();
                recorder_thread = null;
            }
        }
        score_ws.sendBinary(new byte[]{0x45, 0x4f, 0x53});
        Log.d("Score", "EOS sent");
        score_ws.sendClose();
        for (; ; ) {
            synchronized (this) {
                if (score_ws == null)
                    return score_result;
            }
            Thread.sleep(100, 0);
        }
    }
}
