package com.starnet.lcdclient;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

class ClientThread {

    private static final String KEYSTOREPASSWORD = "123456";    //密钥库密码
    private static final String KEYSTOREPATH_CLIENT = "kclient.bks";    //本地密钥库
    private static final String KEYSTOREPATH_TRUST = "tclient.bks";        //信任密钥库
    private static String HOST = "192.168.1.202";                //服务器IP地址，还未实现动态获取
    private static final int PORT = 5748;                                //开放端口
    private static final int LOGIN_CODE = 0;
    private static int mOutOfTime = 0;
    private SSLContext mSSLContext;
    public SSLSocket mSSLSocket;
    private Context mContext;
    private boolean mIsOnline = false;
    private String mUserName;
    private String mPassword;
    private ObjectOutputStream mObjectOutputStream;
    private ObjectInputStream mObjectInputStream;

    public ClientThread(String host,Context context,boolean isOnline,String userName,String password){
        mIsOnline = isOnline;
        mUserName = userName;
        mPassword = password;
        this.HOST= host;
        mContext = context;
        startConnect();
    }


    public void startConnect() {
        new Thread(new Runnable() {
            SSLContext sslContext = null;

            @Override
            public void run() {
                try {
                    mSSLContext = SSLContext.getInstance("TLS");
                    //取得BKS类型的本地密钥库实例，这里特别注意：手机只支持BKS密钥库，不支持Java默认的JKS密钥库
                    KeyStore clientkeyStore = KeyStore.getInstance("BKS");
                    //初始化
                    clientkeyStore.load(
                            mContext.getResources().getAssets().open(KEYSTOREPATH_CLIENT),
                            KEYSTOREPASSWORD.toCharArray());
                    KeyStore trustkeyStore = KeyStore.getInstance("BKS");
                    trustkeyStore.load(mContext.getResources().getAssets()
                            .open(KEYSTOREPATH_TRUST), KEYSTOREPASSWORD.toCharArray());

                    //获得X509密钥库管理实例
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory
                            .getInstance("X509");
                    keyManagerFactory.init(clientkeyStore, KEYSTOREPASSWORD.toCharArray());
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory
                            .getInstance("X509");
                    trustManagerFactory.init(trustkeyStore);

                    //初始化SSLContext实例
                    mSSLContext.init(keyManagerFactory.getKeyManagers(),
                            trustManagerFactory.getTrustManagers(), null);

                    Log.i("System.out", "SSLContext初始化完毕...");
                    //以下两步获得SSLSocket实例
                    synchronized ((Object)mIsOnline){
                        SSLSocketFactory sslSocketFactory = mSSLContext.getSocketFactory();
                        mSSLSocket = (SSLSocket) sslSocketFactory.createSocket(HOST,
                                PORT);

                        Log.i("System.out", "获得SSLSocket成功...");

                        if (mIsOnline) {
                            login(mUserName, mPassword);
                            Log.i("result", "重新连接服务器" + mUserName + ":" + mPassword);
                        }
                    }

                    Log.i("System.out", "获得SSLSocket成功...");
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void sendMsg(String str) {
        try {

            if(mObjectInputStream == null){
                mObjectOutputStream = new ObjectOutputStream(mSSLSocket.getOutputStream());
            }
            mObjectOutputStream.writeUTF(str); // 写一个UTF-8的信息
            mObjectOutputStream.flush();
            System.out.println("发送消息" + str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(int command) {
        try {
            Log.i("result", "sendCommand");
            if(mObjectOutputStream == null){
                mObjectOutputStream = new ObjectOutputStream(mSSLSocket.getOutputStream());
            }
            Log.i("result",mObjectOutputStream.toString());
            mObjectOutputStream.writeInt(command);
            mObjectOutputStream.flush();
            Log.i("result","发送命令" + command);
        } catch (IOException e) {
            Log.i("result","发送命令 出错" + command);
            e.printStackTrace();
        }
    }

    public boolean login(String userName, String password) {
        Log.i("result", "login");
        sendCommand(LOGIN_CODE);
        sendMsg(userName);
        sendMsg(password);

        boolean result = false;
        try {
            if(mObjectInputStream == null){
                mObjectInputStream = new ObjectInputStream(mSSLSocket.getInputStream());
            }
            result = mObjectInputStream.readBoolean() ;
            mOutOfTime = 0;
        } catch (IOException e) {
            Log.i("result", "已关闭" +e);
            try {
                mSSLSocket.close();
                mSSLSocket = null;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return result;
    }

    public String readMsg() {
        String str = "";
        try {
            if(mObjectInputStream == null){
                mObjectInputStream = new ObjectInputStream(mSSLSocket.getInputStream());
            }
            str = mObjectInputStream.readUTF() ;
            mOutOfTime = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public String readConnectMsg() {
        String str = "";
        try {
            if(mObjectInputStream == null){
                mObjectInputStream = new ObjectInputStream(mSSLSocket.getInputStream());
            }
            str = mObjectInputStream.readUTF();
            mOutOfTime = 0;
        } catch (IOException e) {
            try {
                mOutOfTime++;
                if (mOutOfTime > 2) {
                    if (mSSLSocket != null){

                        mSSLSocket.close();
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return str;
    }

    public boolean readBoolean(){
        boolean result = false;
        try {
            if(mObjectInputStream == null){
                mObjectInputStream = new ObjectInputStream(mSSLSocket.getInputStream());
            }
            result = mObjectInputStream.readBoolean();
            mOutOfTime = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
