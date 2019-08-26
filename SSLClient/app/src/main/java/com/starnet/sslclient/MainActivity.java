package com.starnet.sslclient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

public class MainActivity extends Activity{
    private static final String KEYSTOREPASSWORD = "123456";    //密钥库密码
    private static final String KEYSTOREPATH_CLIENT = "kclient.bks";    //本地密钥库
    private static final String KEYSTOREPATH_TRUST = "tclient.bks";        //信任密钥库
    private static String HOST = "192.168.";                //服务器IP地址
    private static final int PORT = 7891;                                //开放端口
    private TextView mServerIp;
    private EditText msgEt;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgEt = (EditText) findViewById(R.id.msg_et);

        mServerIp = (TextView) findViewById(R.id.SERVER_IP);
        mButton = (Button)findViewById(R.id.send_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectAndSend();
                Log.i("result","clicked");
            }
        });

        final UDPToGetServerIP udpToGetServerIP = new UDPToGetServerIP();
        udpToGetServerIP.startUDP(new UDPToGetServerIP.UDPDataCallBack(){

            @Override
            public void mCallback(String str) {
                String[] string = str.split("-");
                mServerIp.setText(string[1]);
                udpToGetServerIP.stopUDP();
            }
        });

    }


    private void connectAndSend() {
        new Thread(new Runnable() {
            SSLContext sslContext = null;

            @Override
            public void run() {
                try {
                    //取得TLS协议的SSLContext实例
                    sslContext = SSLContext.getInstance("TLS");
                    //取得BKS类型的本地密钥库实例，这里特别注意：手机只支持BKS密钥库，不支持Java默认的JKS密钥库
                    KeyStore clientkeyStore = KeyStore.getInstance("BKS");
                    //初始化
                    clientkeyStore.load(
                            getResources().getAssets().open(KEYSTOREPATH_CLIENT),
                            KEYSTOREPASSWORD.toCharArray());
                    KeyStore trustkeyStore = KeyStore.getInstance("BKS");
                    trustkeyStore.load(getResources().getAssets()
                            .open(KEYSTOREPATH_TRUST), KEYSTOREPASSWORD.toCharArray());

                    //获得X509密钥库管理实例
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory
                            .getInstance("X509");
                    keyManagerFactory.init(clientkeyStore, KEYSTOREPASSWORD.toCharArray());
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory
                            .getInstance("X509");
                    trustManagerFactory.init(trustkeyStore);

                    //初始化SSLContext实例
                    sslContext.init(keyManagerFactory.getKeyManagers(),
                            trustManagerFactory.getTrustManagers(), null);

                    Log.i("System.out", "SSLContext初始化完毕...");
                    //以下两步获得SSLSocket实例
                    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                    SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(HOST,
                            PORT);
                    Log.i("System.out", "获得SSLSocket成功...");
                    ObjectInputStream objectInputStream = new ObjectInputStream(sslSocket
                            .getInputStream());
                    Log.i("System.out", objectInputStream.readObject().toString());

                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                            sslSocket.getOutputStream());
                    objectOutputStream.writeObject(msgEt.getText().toString());
                    objectOutputStream.flush();

                    objectInputStream.close();
                    objectOutputStream.close();
                    sslSocket.close();

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
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
