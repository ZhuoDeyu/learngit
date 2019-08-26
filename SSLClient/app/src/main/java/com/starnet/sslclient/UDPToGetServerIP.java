package com.starnet.sslclient;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPToGetServerIP {
    private final String BROADCAST_IP = "224.224.224.225";
    private final int BROADCAST_PORT = 8681;
    private byte[] getData = new byte[1024];
    private boolean isStop = false;
    private MulticastSocket mSocket = null;
    private InetAddress address = null;
    private DatagramPacket dataPacket;
    private Thread mUDPThread = null;
    private UDPDataCallBack mCallBack = null;

    /**
     * 开始接收广播
     *
     * @param ip
     */
    public void startUDP(UDPDataCallBack mCallBack) {
        this.mCallBack = mCallBack;
        mUDPThread = new Thread(UDPRunning);
        mUDPThread.start();
    }
    /**
     * 停止广播
     */
    public void stopUDP() {
        try {
            mSocket.leaveGroup(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isStop = true;
        mUDPThread.interrupt();
    }

    /**
     * 创建udp数据
     */
    private void CreateUDP() {
        try {
            mSocket = new MulticastSocket(BROADCAST_PORT);
            mSocket.setTimeToLive(1);// 广播生存时间0-255
            address = InetAddress.getByName(BROADCAST_IP);
            mSocket.joinGroup(address);
            dataPacket = new DatagramPacket(getData, getData.length, address,
                    BROADCAST_PORT);
            Log.i("result", "udp is create");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable UDPRunning = new Runnable() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String result = (String) msg.obj;
                mCallBack.mCallback(result);
                Log.d("tag", "handler get data:" + result);
            }
        };

        @Override
        public void run() {
            CreateUDP();
            Message msg = handler.obtainMessage();
            while (!isStop) {
                if (mSocket != null) {
                    try {
                        mSocket.receive(dataPacket);
                        String mUDPData = new String(getData, 0,
                                dataPacket.getLength());
/**
 * 确定是否是这个客户端发过来的数据
 */
                        if (mUDPData != null
                                && "xxxx".equals(mUDPData
                                .split("-")[0])) {
                            msg.obj = mUDPData;
                            handler.sendMessage(msg);
                            isStop = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    msg.obj = "error";
                    handler.sendMessage(msg);
                }
            }
        }
    };
    public interface UDPDataCallBack {
        public void mCallback(String str);
    }

}
