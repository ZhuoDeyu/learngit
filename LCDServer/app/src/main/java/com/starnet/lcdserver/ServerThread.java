package com.starnet.lcdserver;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

class ServerThread {
    private static final String KEYSTOREPASSWORD = "123456";        //密钥库的密码
    private static final String KEYSTOREPATH_SERVER = "kserver.bks";   //密钥库存放路径
    private static final int PORT = 5748;   //端口
    private SSLServerSocket mSSLServerSocket;
    private managerOnlineClient mManagerOnlineClient;
    private Context mContext;
    public static boolean flag = true;
    public ServerThread(managerOnlineClient managerOnlineClient,Context context) {

        mManagerOnlineClient = managerOnlineClient;
        mContext = context;

        new Thread(new Runnable() {
            SSLContext sslContext = null;

            @Override
            public void run() {
                try {
                    KeyStore serverKeyStore = KeyStore.getInstance("BKS");
                    //利用提供的密钥库文件输入流和密码初始化密钥库实例
                    serverKeyStore.load(mContext.getResources().getAssets().open(KEYSTOREPATH_SERVER),
                            KEYSTOREPASSWORD.toCharArray());
                    //取得SunX509私钥管理器
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory
                            .getInstance("X509");
                    //用之前初始化后的密钥库实例初始化私钥管理器
                    keyManagerFactory.init(serverKeyStore, KEYSTOREPASSWORD.toCharArray());
                    //获得TLS协议的SSLContext实例
                    sslContext = SSLContext.getInstance("TLS");
                    //初始化SSLContext实例
                    sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
                    //以下两步获得SSLServerSocket实例
                    SSLServerSocketFactory sslServerSocketFactory = sslContext
                            .getServerSocketFactory();
                    mSSLServerSocket = (SSLServerSocket) sslServerSocketFactory
                            .createServerSocket(PORT);
                    Log.i("result",mSSLServerSocket.toString());
                    System.out.println("SSLServerSocket准备就绪..." + mSSLServerSocket.getInetAddress());
                    while (flag) {
                        SSLSocket socket = (SSLSocket) mSSLServerSocket.accept();
                        Log.i("result",mSSLServerSocket.toString());
                        ClientThread clientThread = new ClientThread(socket, mManagerOnlineClient);
                    }

                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
                System.out.println("服务器端退出了");
            }
        }).start();
    }
}
