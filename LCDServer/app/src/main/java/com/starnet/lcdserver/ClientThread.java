package com.starnet.lcdserver;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

public class ClientThread {
    private static final int LOGIN_CODE = 0;
    private static final int TEXT_SET_CODE = 1;
    private static final int SEND_TEXT = 2;
    private static final int MODIFY_PASSWORD_CODE = 3;
    private static final int SET_SPEED_CODE = 4;
    private static final int LOGOUT_CODE = 5;
    private static final int CONNECT_TEST_CODE = 6;
    private int mTimeoutCount = 0;
    private boolean isOnline = true;
    private SSLSocket mSocket;
    private String mUserName = "";
    private ObjectInputStream mObjectInputStream;
    private ObjectOutputStream mObjectOutputStream;
    private managerOnlineClient mManagerOnlineClient;
    private static String WEBLogin = "http://47.97.193.151/system/Login";
    private static String WEBALogout = "http://47.97.193.151/system/Logout";
    ClientThread(SSLSocket socket,managerOnlineClient managerOnlineClient) throws IOException {
        mSocket = socket;
        mSocket.setSoTimeout(10000);
        Log.i("result",mSocket.toString());
        mManagerOnlineClient = managerOnlineClient;

        new Thread(new Runnable() {
            SSLContext sslContext = null;

            @Override
            public void run() {
                // 读操作
                while (isOnline) {
                    try {
                        int command = readCommand();
                        switch (command) {
                            //登录
                            case LOGIN_CODE:
                                synchronized (this.getClass()) {
                                    if(mObjectInputStream == null){
                                        mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
                                    }
                                    mUserName = mObjectInputStream.readUTF();
                                    String password = mObjectInputStream.readUTF();
                                    if(mObjectOutputStream == null){
                                        mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
                                    }
                                    boolean result = mManagerOnlineClient.check(mUserName, password);
                                    mObjectOutputStream.writeBoolean(result);
                                    mObjectOutputStream.flush();
                                    if (result) {
                                        int count = mManagerOnlineClient
                                                .addOnlineClient(mSocket.getInetAddress().toString(),
                                                        mUserName);
                                        Log.i("result", count
                                                + mSocket.getInetAddress().toString()
                                                + "上线"
                                                + "用户名" + mUserName);
                                    }else{
                                        mUserName = "";
                                    }
                                    Log.i("result",
                                            mSocket.getInetAddress().toString()
                                            + "上线");
                                }
                                break;

                            //传输修改的内容
                            case TEXT_SET_CODE: {
                                if(mObjectInputStream == null){
                                    mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
                                }
                                String msg = mObjectInputStream.readUTF();
                                msg = mManagerOnlineClient.changeLCDText(msg);
                                Log.i("result", "修改显示文本为" + msg);
                            }
                                break;

                            //发送当前显示内容
                            case SEND_TEXT:
                                 if(mObjectOutputStream == null){
                                     mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
                                 }
                                mObjectOutputStream.writeUTF(mManagerOnlineClient.getLCDText());
                                mObjectOutputStream.flush();
                                Log.i("result", "发送当前显示");

                                break;

                            //修改密码
                            case MODIFY_PASSWORD_CODE:
                                if(mObjectInputStream == null){
                                    mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
                                }
                                String newPassword = mObjectInputStream.readUTF();
                                mManagerOnlineClient.changePasswordInThread(mUserName, newPassword);
                                if(mObjectOutputStream == null){
                                    mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
                                }
                                mObjectOutputStream.writeBoolean(true);
                                mObjectOutputStream.flush();
                                Log.i("result", "修改密码为" + newPassword);

                                break;

                            //调整LCD滚动速度
                            case SET_SPEED_CODE:
                                if(mObjectInputStream == null){
                                    mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
                                }
                                int speed = mObjectInputStream.readInt();
                                mManagerOnlineClient.changeLCDSpeed(speed);
                                break;

                            //注销
                            case LOGOUT_CODE:
                                int count = mManagerOnlineClient.deleteOnlineClient(
                                        mSocket.getInetAddress().toString(), mUserName);
                                Log.i("result", count
                                        + mSocket.getInetAddress().toString()
                                        + "下线"
                                        + "用户名" + mUserName);
                                break;

                            case CONNECT_TEST_CODE:
                                if(mObjectOutputStream == null){
                                    mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
                                }
                                mObjectOutputStream.writeUTF("online");
                                mObjectOutputStream.flush();
                                break;
                            //无操作
                            default:
                                break;
                        }
                    } catch (IOException e) {
                        isOnline = false;
                        int count = mManagerOnlineClient.deleteOnlineClient(
                                mSocket.getInetAddress().toString(), mUserName);
                        Log.i("result", count
                                + mSocket.getInetAddress().toString()
                                + "下线"
                                + "用户名" + mUserName);
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 获取命令代码
     * 获取失败或无命令为-1
     * */
    private int readCommand(){
        int command = -1;
        try {
            if(mObjectInputStream == null){
                mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
            }
            command = mObjectInputStream.readInt();
            mTimeoutCount = 0;
            Log.i("result command:",String.valueOf(command));
        } catch (SocketTimeoutException e) {
            if(!mUserName.equals("") && mTimeoutCount > 2){
                try {
                    mManagerOnlineClient.deleteOnlineClient(
                            mSocket.getInetAddress().toString(), mUserName);
                    Log.i("result", mSocket.getInetAddress().toString()
                            + "下线"
                            + "用户名" + mUserName);
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }catch (IOException e){

        }
        return command;
    }
}
