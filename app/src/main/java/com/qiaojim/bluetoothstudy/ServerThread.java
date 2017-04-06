package com.qiaojim.bluetoothstudy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Administrator on 2017/4/4.
 */

public class ServerThread implements Runnable {

    final String TAG = "ServerThread";

    BluetoothAdapter bluetoothAdapter;
    BluetoothServerSocket serverSocket =null;
    BluetoothSocket socket = null;
    Handler uiHandler;
    Handler writeHandler;

    OutputStream out;
    InputStream in;
    BufferedReader reader;

    boolean acceptFlag = true;

    public ServerThread(BluetoothAdapter bluetoothAdapter, Handler handler) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.uiHandler = handler;
        BluetoothServerSocket tmp = null;
        try {
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Params.NAME, UUID.fromString(Params.UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = tmp;
        Log.e(TAG, "-------------- do new()");
    }

    @Override
    public void run() {
        Log.e(TAG, "-------------- do run()");
        try {
            while (acceptFlag) {
                socket = serverSocket.accept();
                // 阻塞，直到有客户端连接
                if (socket != null) {
                    Log.e(TAG, "-------------- socket not null, get a client");

                    out = socket.getOutputStream();
                    in = socket.getInputStream();
                    //reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));

                    BluetoothDevice remoteDevice = socket.getRemoteDevice();
                    Message message = new Message();
                    message.what = Params.MSG_REV_A_CLIENT;
                    message.obj = remoteDevice;
                    uiHandler.sendMessage(message);

                    // 读取服务器 socket 数据
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "-----------do server read run()");

                            byte[] buffer = new byte[1024];
                            int len;
                            String content;
                            try {
                                while ((len = in.read(buffer)) != -1) {
                                    content = new String(buffer, 0, len);
                                    Message message = new Message();
                                    message.what = Params.MSG_CLIENT_REV_NEW;
                                    message.obj = content;
                                    uiHandler.sendMessage(message);
                                    Log.e(TAG, "------------- server read data in while ,send msg ui" + content);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
//                    Looper.prepare();
//                    writeHandler = new Handler() {
//                        @Override
//                        public void handleMessage(Message msg) {
//                            switch (msg.what) {
//                                case Params.MSG_SERVER_WRITE_NEW:
//                                    String data = msg.obj.toString() + "\n";
//                                    try {
//                                        out.write(data.getBytes("utf-8"));
//                                        Log.e(TAG, "-------------server write data " + data);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    break;
//                            }
//                        }
//                    };
//                    Looper.loop();
                    break;
                }
            }// end while(true)
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void write(String data){
        data = data+"\r\n";
        try {
            out.write(data.getBytes("utf-8"));
            Log.e(TAG, "---------- write data ok "+data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            acceptFlag = false;
            serverSocket.close();
            Log.e(TAG, "-------------- do cancel ,flag is "+acceptFlag);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "----------------- cancel " + TAG + " error");
        }
    }
}
