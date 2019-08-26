package com.starnet.sslserver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
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

public class MainActivity extends Activity {
    private static final String KEYSTOREPASSWORD = "123456";        //密钥库的密码
    private static final String KEYSTOREPATH_SERVER = "kserver.bks";   //密钥库存放路径
    private static final int PORT = 7891;   //端口
    private UDPSocketBroadCast broadCast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ServerMain();
        broadCast = UDPSocketBroadCast.getInstance();
        broadCast.startUDP(getIP(),PORT);
        Log.i("result",getIP());
    }

    public static String getIP(){

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
                    {

                        Log.i("result", inetAddress.getHostAddress());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex){
            ex.printStackTrace();
        }
        return null;
    }
    private void ServerMain() {

        new Thread(new Runnable() {
            SSLContext sslContext = null;

            @Override
            public void run() {

                try {
                    KeyStore serverKeyStore = KeyStore.getInstance("BKS");
                    //利用提供的密钥库文件输入流和密码初始化密钥库实例
                    serverKeyStore.load(getResources().getAssets().open(KEYSTOREPATH_SERVER),
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
                    SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory
                            .createServerSocket(PORT);
                    System.out.println("SSLServerSocket准备就绪..." + sslServerSocket.toString());
                    while (true) {
                        SSLSocket socket = (SSLSocket) sslServerSocket.accept();

                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                                socket.getOutputStream());
                        objectOutputStream.flush();
                        objectOutputStream.writeObject("这里是服务器端...");
                        objectOutputStream.flush();

                        ObjectInputStream objectInputStream = new ObjectInputStream(
                                socket.getInputStream());
                        try {
                            System.out.println(objectInputStream.readObject().toString());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        objectOutputStream.close();
                        objectInputStream.close();
                        socket.close();
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

    @Override
    protected void onDestroy() {
        broadCast.stopUDP();
        super.onDestroy();
    }
}
