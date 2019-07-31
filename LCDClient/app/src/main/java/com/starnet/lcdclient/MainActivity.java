package com.starnet.lcdclient;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity{
    private static int SET_TEXT_CODE = 1;
    private static final int GET_TEXT = 2;
    private static int CHANGE_PASSWORD_CODE = 3;
    private static int TEST_CONNNECT_CODE = 6;
    private static String HOST = "192.168.137.243";
    private static ClientThread mThread;
    private static TextView mConnectText;
    private static Button mConnectButton;
    private ListView listView;
    private ArrayAdapter<String> adapter1;
    private ArrayAdapter<String> adapter2;
    private List<View> mViews = new ArrayList<>();
    private DrawerLayout mMenuDrawerLayout;
    private MyViewPager mViewPager;
    private TextView mContentText;
    private EditText mUserName;
    private EditText mPassword;
    private EditText mPrePasswordEdit;
    private EditText mNewPasswordEdit_1;
    private EditText mNewPasswordEdit_2;
    private EditText mChangeText;
    private Button mLoginBt;
    private Button mChangeSetButton;
    private Button mGoToSeeCurrentText;
    private Button mGoToModifyText;
    private Button mNewPasswordCommitButton;
    Timer timer = new Timer();
    private MyHandler mHandler;
    private boolean mIsOnline = false;
    private boolean result = false;
    private String mLCDText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //连接服务器端
        initData();
        setMenu();
        mHandler = new MyHandler();

        final UDPToGetServerIP udpToGetServerIP = new UDPToGetServerIP();
        udpToGetServerIP.startUDP(new UDPToGetServerIP.UDPDataCallBack(){
            @Override
            public void mCallback(String str) {
                Log.i("result",str);
                String[] string = str.split("-");
                HOST = string[1];
                udpToGetServerIP.stopUDP();
                mThread = new ClientThread(HOST,
                        getApplicationContext(),
                        mIsOnline,
                        mUserName.getText().toString(),
                        mPassword.getText().toString());
                mConnectText.setText("得到服务器IP，正在建立连接");
                mConnectButton.setEnabled(false);
            }
        });
    }

    /**
     * 初始化各个界面的控件
     * */
    private void initData() {
        mMenuDrawerLayout = findViewById(R.id.drawer_layout);
        mViewPager = findViewById(R.id.view_pager);
        listView=(ListView) findViewById(R.id.list_view);
        LayoutInflater mLayoutInflater=LayoutInflater.from(this);

        View tabLCDContent =mLayoutInflater.inflate(R.layout.lcd_content,null);
        View tabLCDSet = mLayoutInflater.inflate(R.layout.lcd_set,null);
        View tabMain = mLayoutInflater.inflate(R.layout.main,null);
        View tabLogin=mLayoutInflater.inflate(R.layout.login,null);
        View tabConnect = mLayoutInflater.inflate(R.layout.connect,null);
        View tabPasswordModify = mLayoutInflater.inflate(R.layout.modify_password,null);

        mViews.add(tabLogin);
        mViews.add(tabLCDContent);
        mViews.add(tabLCDSet);
        mViews.add(tabMain);
        mViews.add(tabConnect);
        mViews.add(tabPasswordModify);


        initPager();

        mUserName = tabLogin.findViewById(R.id.text_for_user_name);
        mPassword = tabLogin.findViewById(R.id.text_for_password);
        mLoginBt = tabLogin.findViewById(R.id.login_button);
        initLoginBT();

        mConnectText = tabConnect.findViewById(R.id.text_for_connect);
        mConnectButton = tabConnect.findViewById(R.id.connect_button);
        initConnectTab();

        mContentText = tabLCDContent.findViewById(R.id.content_text);

        mChangeText = tabLCDSet.findViewById(R.id.text_for_change);
        mChangeSetButton = tabLCDSet.findViewById(R.id.submit_button);
        initLCDSetTab();

        mGoToSeeCurrentText = tabMain.findViewById(R.id.current_text_button);
        mGoToModifyText = tabMain.findViewById(R.id.change_text_button);
        initMainTab();

        mPrePasswordEdit = tabPasswordModify.findViewById(R.id.pre_password);
        mNewPasswordEdit_1 = tabPasswordModify.findViewById(R.id.new_password_1);
        mNewPasswordEdit_2 = tabPasswordModify.findViewById(R.id.new_password_2);
        mNewPasswordCommitButton = tabPasswordModify.findViewById(R.id.new_password_commit);
        intiPassswordModifyTab();
    }

    /**
     * 修改密码界面的初始化
     * */
    private void intiPassswordModifyTab() {
        mNewPasswordCommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        mThread.sendCommand(CHANGE_PASSWORD_CODE);
                        mThread.sendMsg(mNewPasswordEdit_1.getText().toString());
                        result = mThread.readBoolean();
                        Log.i("result",String.valueOf(result));
                        runOnUiThread(new Runnable(){
                                          @Override
                                          public void run() {
                                              //更新UI
                                              if(result){
                                                  Toast.makeText(getApplicationContext(),"修改成功,请重新登录",Toast.LENGTH_SHORT)
                                                          .show();
                                                  mNewPasswordEdit_1.setText("");
                                                  mNewPasswordEdit_2.setText("");
                                                  mPrePasswordEdit.setText("");
                                                  mViewPager.setCurrentItem(0);
                                              }else{
                                                  Toast.makeText(getApplicationContext(),"修改失败",Toast.LENGTH_SHORT)
                                                          .show();
                                              }
                                          }
                                      }
                        );
                    }
                }.start();
            }
        });

        mPrePasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mPrePasswordEdit.getText().toString().equals(mPassword.getText().toString())){
                    mNewPasswordCommitButton.setEnabled(true);
                }else{
                    mNewPasswordCommitButton.setEnabled(false);
                }
            }
        });
        mNewPasswordEdit_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(mNewPasswordEdit_1.getText().toString() != null
                        && mNewPasswordEdit_1.getText().toString()
                        .equals(mNewPasswordEdit_2.getText().toString())){
                    mNewPasswordCommitButton.setEnabled(true);
                }else{
                    mNewPasswordCommitButton.setEnabled(false);
                }
            }
        });
        mNewPasswordEdit_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(mNewPasswordEdit_1.getText().toString() != null
                        && mNewPasswordEdit_1.getText().toString()
                        .equals(mNewPasswordEdit_2.getText().toString())){
                    mNewPasswordCommitButton.setEnabled(true);
                }else{
                    mNewPasswordCommitButton.setEnabled(false);
                }
            }
        });
    }

    /**
     * 主要操作界面的初始化
     */
    private void initMainTab() {
        mGoToModifyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(2);
            }
        });

        mGoToSeeCurrentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectText.setText("连接中。。。");
                mConnectButton.setEnabled(false);
                new Thread(){
                    @Override
                    public void run() {
                        mThread.sendCommand(GET_TEXT);
                        mLCDText = mThread.readMsg();
                        Log.i("result current text ",mLCDText);
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                //更新UI
                                mChangeText.setText(mLCDText);
                                mContentText.setText(mLCDText);
                                mConnectText.setText("已连接");
                                mConnectButton.setEnabled(false);
                            }
                        });
                    }

                }.start();
                mViewPager.setCurrentItem(1);
            }
        });
    }

    /**
     * 修改LCD显示界面的初始化
     * */
    private void initLCDSetTab() {
        mChangeSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        mThread.sendCommand(SET_TEXT_CODE);
                        mThread.sendMsg(mChangeText.getText().toString());
                    }
                }.start();
            }
        });
    }

    /**
     * 处理界面更新
     * */
    private class MyHandler extends Handler{
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //测试连接状态结果的显示
                    if(mThread!=null &&
                            mThread.mSSLSocket != null ){
                        if(!mThread.mSSLSocket.isClosed()){
                            new Thread(){
                                @Override
                                public void run() {
                                    mThread.sendCommand(TEST_CONNNECT_CODE);
                                    String str = mThread.readConnectMsg();
                                    Log.i("result","connect test" + str);
                                }
                            }.start();
                            mConnectText.setText("已连接");
                            mConnectButton.setEnabled(false);
                        }
                    }else{
                        mConnectText.setText("连接已断开");
                        mConnectButton.setEnabled(true);
                    }
                    break;
                case 2:
                    //登录成功后界面跳转
                    listView.setAdapter(adapter2);
                    mViewPager.setCurrentItem(3);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 定时任务，测试与服务器的连接状况
     * */
    TimerTask task = new TimerTask(){
        public void run() {
            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    };

    /**
     * 查看及重新连接服务器界面初始化
     * */
    private void initConnectTab() {
        timer.schedule(task,1000,10000);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final UDPToGetServerIP udpToGetServerIP = new UDPToGetServerIP();
                udpToGetServerIP.startUDP(new UDPToGetServerIP.UDPDataCallBack(){

                    @Override
                    public void mCallback(String str) {
                        Log.i("result","得到服务器IP");
                        String[] string = str.split("-");
                        HOST = string[1];
                        udpToGetServerIP.stopUDP();
                        mThread = new ClientThread(HOST,
                                getApplicationContext(),
                                mIsOnline,
                                mUserName.getText().toString(),
                                mPassword.getText().toString());
                    }
                });
            }
        });
    }

    /**
     * PagerAdapter初始化，设置视图
     * */
    private void initPager() {
        PagerAdapter mMyPagerAdapter=new PagerAdapter() {

            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view==object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view=mViews.get(position);
                container.addView(view);
                return view;
            }
        };
        mViewPager.setAdapter(mMyPagerAdapter);
    }

    /**
     * 登录的处理
     * */
    private void initLoginBT() {
        mLoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThread !=null && mThread.mSSLSocket != null) {
                    new Thread() {
                        @Override
                        public void run() {
                            result = mThread.login(mUserName.getText().toString(), mPassword.getText().toString());
                            Log.i("------result", String.valueOf(result));
                            Log.i("result", String.valueOf(result));
                            new Runnable() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    if (result) {
                                        mIsOnline = true;
                                        Toast.makeText(getApplicationContext(), "登录成功 ", Toast.LENGTH_SHORT)
                                                .show();
                                        SendToWebServer.OkHttpPost(SendToWebServer.LOGIN_CODE,
                                                SendToWebServer.LOGIN_URL,
                                                getLocalMacAddress());

                                        Message message = new Message();
                                        message.what = 2;
                                        mHandler.sendMessage(message);
                                    } else {
                                        if (mThread.mSSLSocket == null) {
                                            Log.i("result", "连接不上服务器");
                                            Toast.makeText(getApplicationContext(), "连接出错", Toast.LENGTH_SHORT)
                                                    .show();

                                        } else {
                                            Toast.makeText(getApplicationContext(), "登录失败", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }
                                    Looper.loop();
                                }
                            }.run();
                        }
                    }.
                            start();
                } else {
                    Toast.makeText(getApplicationContext(), "连接不到服务器", Toast.LENGTH_SHORT)
                            .show();
                    mLoginBt.setEnabled(true);
                    mLoginBt.setText("登录");
                }
            }
        });
    }

    /**
     * 侧滑菜单设置及切换逻辑
     * */
    private void setMenu() {

        final List<String> list1 = new ArrayList<String>();
        list1.add("登录");
        list1.add("通信状态");
        final List<String> list2 = new ArrayList<String>();
        list2.add("注销");
        list2.add("通信状态");
        list2.add("修改密码");
        list2.add("LCD查看");
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list1);
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list2);
        listView.setAdapter(adapter1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) listView.getAdapter().getItem(position);
                mMenuDrawerLayout.closeDrawer(listView);
                if (str.equals("登录")) {
                    mViewPager.setCurrentItem(0);
                } else if (str.equals("注销")) {
                    mIsOnline = false;
                    SendToWebServer.OkHttpPost(SendToWebServer.LOGOUT_CODE,
                            SendToWebServer.LOGOUT_URL,
                            getLocalMacAddress());
                    listView.setAdapter(adapter1);
                    new Thread(){
                        @Override
                        public void run() {
                            mThread.sendCommand(5);
                        }
                    }.start();
                    mViewPager.setCurrentItem(0);
                    //SendToWebServer.send(SendToWebServer.LOGOUT_CODE,"");
                } else if (str.equals("修改密码")) {
                    mViewPager.setCurrentItem(5);
                } else if (str.equals("通信状态")) {
                    mViewPager.setCurrentItem(4);
                }else if(str.equals("LCD查看")){
                    mViewPager.setCurrentItem(3);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        new Thread(){
            @Override
            public void run() {
                if(mThread!=null)
                    mThread.sendCommand(5);
            }
        }.start();
        super.onStop();
    }

    /**
     * 获取MAC地址
     * */
    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
}
