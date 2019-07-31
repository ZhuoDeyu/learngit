package com.starnet.lcdserver;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.friendlyarm.FriendlyThings.LCDSet;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class MainActivity extends AppCompatActivity {
    private managerOnlineClient mManagerOnlineClient = new managerOnlineClient() {
        @Override
        public int addOnlineClient(String ip,String userName) {
                if(mOnlineUser.containsKey(ip)||mOnlineUser.containsValue(userName))
                    return mOnlineUser.size();
                mOnlineUser.put(ip,userName);
                mMyOnlienAdapter.setOnlineUsers(mOnlineUser);
                UpdateUi(LOGIN_CODE);

            return mOnlineUser.size();
        }

        @Override
        public int deleteOnlineClient(String ip,String userName) {

                mOnlineUser.remove(ip);
                mMyOnlienAdapter.setOnlineUsers(mOnlineUser);
                UpdateUi(LOGOUT_CODE);

            return mOnlineUser.size();
        }

        @Override
        public String changeLCDText(String str) {
            mCurrentText = str;
            mWebPostAndGet.OkHttpPost(getNewMac());
            UpdateUi(TEXT_SET_CODE);
            return mCurrentText;
        }

        @Override
        public void changeLCDSpeed(int speed) {
            mSpeed = speed;
            UpdateUi(SET_SPEED_CODE);
        }

        @Override
        public void changePasswordInList(final String userName) {
            String password = mSavedUser.get(userName);
            View view = mLayoutInflater.inflate(R.layout.new_user_input_edit,null);
            final EditText userNameInput = view.findViewById(R.id.add_user_name);
            userNameInput.setText("用户名为：  " + userName);
            userNameInput.setEnabled(false);
            final EditText passwordInput = view.findViewById(R.id.add_user_password);
            passwordInput.setText(password);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("修改用户密码");
            builder.setView(view);
            builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newPassword = passwordInput.getText().toString();
                    if(newPassword.length() != 0){
                        mSavedUser.put(userName,newPassword);
                        UpdateUser(userName,newPassword);
                        Toast.makeText(getApplicationContext(),"修改成功",Toast.LENGTH_SHORT);
                    }else{
                        Toast.makeText(getApplicationContext(),"不可为空",Toast.LENGTH_SHORT);
                    }
                }});
            builder.setNegativeButton("取消",null);
            builder.show();
        }

        @Override
        public void changePasswordInThread(String userName, String password) {
            UpdateUser(userName,password);
            mSavedUser.put(userName,password);
        }

        @Override
        public void deleteSavedUser(String userName) {
            mSavedUser.remove(userName);
            mMyAdapter.setUserNames(mSavedUser);
            mMyAdapter.notifyDataSetChanged();
            DeleteUser(userName);
        }

        @Override
        public String getLCDText() {
            return mCurrentText;
        }

        public synchronized boolean check(String userName,String password){
            Log.i("result","user" + userName + "password" +password);
            if(userName == null || password == null){
                return false;
            }else{
                if(mSavedUser.size() > 0 ){
                    if( mSavedUser.get(userName).equals(password)) {
                        return true;
                    }
                } else{
                    return false;
                }
            }
            return false;
        }

    };
    private ListView mConnectList;
    private static final int LOGIN_CODE = 0;
    private static final int TEXT_SET_CODE = 1;
    private static final int SET_SPEED_CODE = 4;
    private static final int LOGOUT_CODE = 5;
    private static final int REQUEST_CODE = 10;
    private static final int PORT = 5748;   //端口
    private static final String TAG = "result";
    private static ServerThread mServerThread;
    private ListView listView;
    private DrawerLayout mMenuDrawerLayout;
    private List<View> mView=new ArrayList<>();
    private ViewPager mViewpager;
    public HashMap<String,String> mOnlineUser = new HashMap<>();
    public HashMap<String,String> mSavedUser = new HashMap<>();
    public String mCurrentText = "";
    public int mSpeed;
    private TextView mIpText;
    private TextView mContentText;
    private EditText mChangeText;
    private Button mChangeSetButton;
    private ListView mUserListView;
    private SQLiteDatabase mDatabase;
    private Button mAddUserButton;
    private LayoutInflater mLayoutInflater;
    private MyAdapter mMyAdapter;
    private LCDSet mLCDSet;
    private SeekBar mSpeedSeekBar;
    private RadioGroup mDirectionRadioGroup;
    private MyOnlienAdapter mMyOnlienAdapter;
    private static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private TextView mUserCount;
    private TextView mOnlineUserCount;
    private TextView mTimeText;
    private TextView mDateText;
    private TextView mDayOfWeek;
    private TextView mWeather;
    private TextView mTemperatureText;
    private TextView mAddressText;
    private WebPostAndGet mWebPostAndGet;
    private MyTimeTask mMyTimeTask;
    private UDPSocketBroadCast mUDPSocketBroadCast;
    private String[] DAYOFWEEK={
            "星期天","星期一","星期二","星期三","星期四","星期五","星期六"
    };
    private int mDirection = LCDSet.DIRECTION_RIGHT_TO_LEFT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CheckPermission();


        Log.i("result","开始广播");
        mUDPSocketBroadCast = UDPSocketBroadCast.getInstance();
        mUDPSocketBroadCast.startUDP(getIP(),PORT);
        HeConfig.init("HE1907151230001027", "230c6dba40894214831b5036adf88d64");
        HeConfig.switchToFreeServerNode();

        mDatabase = new UserBaseHelper(getApplicationContext())
                .getWritableDatabase();


        getUsers();
        startServer();
        initData();
        setMenu();

        new Thread() {
            @Override
            public void run() {
                getWeatherByIP(GetNetIp());
            }
        }.start();

        mWebPostAndGet = new WebPostAndGet(mManagerOnlineClient);
        mLCDSet = new LCDSet();

        setTimer();
    }

    /**
     * 开始轮询服务器端的请求,30秒一次
     * */
    private void setTimer(){
        mMyTimeTask =new MyTimeTask(30000, new TimerTask() {
            @Override
            public void run() {
                mWebPostAndGet.OkHttpGetTheAd(getNewMac());
            }
        });
        mMyTimeTask.start();
    }

    /**
     * 获取本机IP
     * */
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
    /**
     * 启动服务器监听线程，等待客户端连接
     */
    private void startServer() {
        mServerThread = new ServerThread(mManagerOnlineClient,getApplicationContext());
    }

    /**
     * 初始化各个控件
     * */
    private void initData() {
        mMenuDrawerLayout = findViewById(R.id.drawer_layout);
        mViewpager = findViewById(R.id.view_pager);
        listView=(ListView) findViewById(R.id.v4_listview);
        mLayoutInflater=LayoutInflater.from(this);

        View tabLCDContent =mLayoutInflater.inflate(R.layout.lcd_content,null);
        View tabLCDSet = mLayoutInflater.inflate(R.layout.lcd_set,null);
        View tabConnectManager = mLayoutInflater.inflate(R.layout.connect_manager,null);
        View tabUserName=mLayoutInflater.inflate(R.layout.user_manager,null);

        mView.add(tabLCDContent);
        mView.add(tabLCDSet);
        mView.add(tabConnectManager);
        mView.add(tabUserName);

        PagerAdapter myPagerAdapter = new PagerAdapter() {

            @Override
            public int getCount() {

                return mView.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {

                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {

                container.removeView(mView.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = mView.get(position);
                container.addView(view);
                return view;
            }
        };
        mViewpager.setAdapter(myPagerAdapter);

        mIpText = tabLCDContent.findViewById(R.id.text_to_show_ip);
        mIpText.setText(getIP());
        mWeather = tabLCDContent.findViewById(R.id.weather);
        mTemperatureText = tabLCDContent.findViewById(R.id.temperature);
        mAddressText = tabLCDContent.findViewById(R.id.address);
        mTimeText = tabLCDContent.findViewById(R.id.mytime);
        mDateText = tabLCDContent.findViewById(R.id.date);
        mDayOfWeek = tabLCDContent.findViewById(R.id.dayOfWeek);
        mContentText = tabLCDContent.findViewById(R.id.content_text);
        InitContentTab();

        mConnectList = tabConnectManager.findViewById(R.id.connect_list);
        mMyOnlienAdapter = new MyOnlienAdapter(mOnlineUser);
        mConnectList.setAdapter(mMyOnlienAdapter);
        mOnlineUserCount = tabConnectManager.findViewById(R.id.online_count);

        mChangeText = tabLCDSet.findViewById(R.id.text_for_change);
        mChangeSetButton = tabLCDSet.findViewById(R.id.submit_button);
        mSpeedSeekBar = tabLCDSet.findViewById(R.id.speed_seekbar);
        mDirectionRadioGroup = tabLCDSet.findViewById(R.id.direction_group);
        InitLCDSetTab();

        mUserListView = tabUserName.findViewById(R.id.text_for_user_list);
        mUserCount = tabUserName.findViewById(R.id.saved_user_count);
        mUserCount.setText(String.valueOf(mSavedUser.size()));
        mMyAdapter = new MyAdapter(mSavedUser,mManagerOnlineClient);
        mUserListView.setAdapter(mMyAdapter);
        mAddUserButton = tabUserName.findViewById(R.id.add_user_button);
        InitUserTab();
    }

    /**
     * 为内容显示界面控件设监听事件
     * */
    private void InitContentTab() {
        mWeather.setText("正在加载天气。。");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            Runnable updateTime = new Runnable() {
                @Override
                public void run() {
                    Calendar calendar = Calendar.getInstance();

                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH)+1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                    mTimeText.setText(hour+":"+(minute<10?"0":"")+minute);
                    mDateText.setText(year+"年"+month+"月"+day+"日");
                    mDayOfWeek.setText(DAYOFWEEK[dayOfWeek-1]);
                }
            };
            @Override
            public void run() {
                runOnUiThread(updateTime);
            }
        },0,1000);
    }

    /**
     * 为内容设置界面控件设监听事件
     * */
    private void InitLCDSetTab() {
        mSpeedSeekBar.setProgress(1000);
        mSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSpeed = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mDirectionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.left_to_right:
                        mDirection = LCDSet.DIRECTION_LEFT_TO_RIGHT;
                        break;
                    case R.id.right_to_left:
                        mDirection = LCDSet.DIRECTION_RIGHT_TO_LEFT;
                        break;
                    default:
                        break;
                }
            }
        });


        mChangeSetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCurrentText = mChangeText.getText().toString();
                mContentText.setText(mCurrentText);
                mLCDSet.setShow(mCurrentText,mSpeed,mDirection);
                mWebPostAndGet.OkHttpPost(getNewMac());
            }
        });
    }

    /**
     * 为用户列表界面控件设监听事件
     * */
    private void InitUserTab() {
        mAddUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterNewUser();
            }
        });
    }

    /**
     * 添加新用户用到的弹出框
     * */
    private void EnterNewUser() {
        View view = mLayoutInflater.inflate(R.layout.new_user_input_edit,null);
        final EditText userNameInput = view.findViewById(R.id.add_user_name);
        userNameInput.setHint("账号");
        final EditText passwordInput = view.findViewById(R.id.add_user_password);
        passwordInput.setHint("密码");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("输入新用户信息");
        builder.setView(view);
        builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = userNameInput.getText().toString();
                String password = passwordInput.getText().toString();
                if(!mSavedUser.containsKey(userName)){
                    User user = new User(userName,password);
                    mSavedUser.put(userName,password);
                    mUserCount.setText(String.valueOf(mSavedUser.size()));
                    AddUser(user);
                }else{
                    Toast.makeText(getApplicationContext(),"用户已存在",Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    /**
     * 设置侧滑菜单
     * */
    private void setMenu() {
        final List<String> list = new ArrayList<String>();
        list.add("LCD当前显示");
        list.add("LCD设置");
        list.add("连接管理");
        list.add("用户管理");

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) listView.getAdapter().getItem(position);
                if (str.equals("LCD当前显示")) {
                    mViewpager.setCurrentItem(0);
                } else if (str.equals("LCD设置")) {
                    mViewpager.setCurrentItem(1);
                } else if (str.equals("连接管理")) {
                    mViewpager.setCurrentItem(2);
                } else if (str.equals("用户管理")) {
                    mViewpager.setCurrentItem(3);
                }
                mMenuDrawerLayout.closeDrawer(listView);
            }
        });
    }

    /**
     * 在程序关闭时关闭监听线程
     * */
    @Override
    protected void onStop() {
        mServerThread.flag = false;
        mMyTimeTask.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mUDPSocketBroadCast.stopUDP();
        super.onDestroy();
    }

    /**
     * 在程序开启时开始监听线程
     * */
    @Override
    protected void onRestart() {
        super.onRestart();
        mServerThread.flag = true;
    }

    /**
     * 根据事件码跟新界面
     * */
    private void UpdateUi(final int Code) {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        //更新UI
                        switch (Code){
                            case LOGIN_CODE:
                                mOnlineUserCount.setText(String.valueOf(mOnlineUser.size()));
                                mMyOnlienAdapter.notifyDataSetChanged();
                                break;
                            case LOGOUT_CODE:
                                mOnlineUserCount.setText(String.valueOf(mOnlineUser.size()));
                                mMyOnlienAdapter.notifyDataSetChanged();
                                break;
                            case TEXT_SET_CODE:
                                mContentText.setText(mCurrentText);
                                mChangeText.setText(mCurrentText);
                                mLCDSet.setShow(mCurrentText,mSpeed,mDirection);
                                break;
                            case SET_SPEED_CODE:
                                mLCDSet.setShow(mCurrentText,mSpeed,mDirection);
                            default:
                                break;
                        }
                    }
                });
            }
        }.start();
    }

    /**
     * 向数据库添加用户
     * */
    private void AddUser(User user){
        ContentValues values = getContentValues(user);
        if(mDatabase.insert(UserDbSchema.UserTable.NAME, null, values)==-1){
            Log.i("result","失败 ");
        }
        mMyAdapter.setUserNames(mSavedUser);
        mMyAdapter.notifyDataSetChanged();
    }

    /**
     * 在数据库删除用户
     * */
    private synchronized void DeleteUser(String userName){
        mSavedUser.remove(userName);
        mMyAdapter.setUserNames(mSavedUser);
        mMyAdapter.notifyDataSetChanged();
        mUserCount.setText(String.valueOf(mSavedUser.size()));
        mDatabase.delete(UserDbSchema.UserTable.NAME,
                UserDbSchema.UserTable.Cols.USERNAME + "= ?",
                new String[]{userName});
    }

    /**
     * 在数据库修改用户
     * */
    private void UpdateUser(String userName,String password){
        ContentValues contentValues = new ContentValues();
        contentValues.put("password",password);
        synchronized (mDatabase.getClass()){
            mDatabase.update(UserDbSchema.UserTable.NAME,
                    contentValues,
                    UserDbSchema.UserTable.Cols.USERNAME + "=?",
                    new String[]{userName});
        }
    }

    /**
     * 辅助读取数据库
     * */
    private UserCursorWrapper queryUsers(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                UserDbSchema.UserTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new UserCursorWrapper(cursor);
    }

    /**
     * 辅助数据库查询
     * */
    private static ContentValues getContentValues(User user){
        ContentValues values = new ContentValues();
        values.put(UserDbSchema.UserTable.Cols.USERNAME, user.userName);
        values.put(UserDbSchema.UserTable.Cols.PASSWORD, user.password);
        return values;
    }

    /**
     * 从数据库读取当前保存的用户列表
     * */
    private void getUsers(){
        mSavedUser= new HashMap<>();
        UserCursorWrapper cursor;

        cursor = queryUsers(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                mSavedUser.put(cursor.getUser().userName,cursor.getUser().password);
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
    }

    /**
     * 判断安卓手机版本，动态请求权限
     * */
    private void CheckPermission() {
        PackageManager pm = getApplicationContext().getPackageManager();
        PackageInfo pi;
        try {
            // 参数2必须是PackageManager.GET_PERMISSIONS
            pi = pm.getPackageInfo("com.starnet.lcdserver", PackageManager.GET_PERMISSIONS);
            String[] permissions = pi.requestedPermissions;
            if(permissions != null){
                for(String str : permissions){
                    boolean permission = (PackageManager.PERMISSION_GRANTED ==
                            pm.checkPermission(str, getPackageName()));

                    Log.i(TAG, str + permission);
                }
            }
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
            }
        }else{
            return;
        }
    }

    /**
     * 查询天气
     * */
    public void getWeatherByIP(final String[] str){
        if(!str[2].equals("")){
            new Thread() {
                public void run() {
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            //更新UI
                            mAddressText.setText(str[2]);
                        }

                    });
                }
            }.start();
            return;
        }else{
            HeWeather.getWeatherNow(MainActivity.this,str[0],
                    Lang.CHINESE_SIMPLIFIED ,
                    Unit.METRIC ,
                    new HeWeather.OnResultWeatherNowBeanListener() {
                        @Override
                        public void onError(Throwable e) {
                            Log.i(TAG, "Weather Now onError: ", e);
                        }

                        @Override
                        public void onSuccess(Now dataObject) {
                            Log.i(TAG, " Weather Now onSuccess: ");
                            //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                            if ( Code.OK.getCode().equalsIgnoreCase(dataObject.getStatus()) ){
                                //此时返回数据
                                final NowBase now = dataObject.getNow();
                                new Thread() {
                                    public void run() {
                                        //这儿是耗时操作，完成之后更新UI；
                                        runOnUiThread(new Runnable(){

                                            @Override
                                            public void run() {
                                                //更新UI
                                                mAddressText.setText(str[1]);
                                                mTemperatureText.setText(now.getTmp()+"℃");
                                                mWeather.setText(now.getCond_txt());
                                                Log.i("result weather","获取成功");
                                            }

                                        });
                                    }
                                }.start();
                            } else {
                                //在此查看返回数据失败的原因
                                String status = dataObject.getStatus();
                                Code code = Code.toEnum(status);
                                Log.i(TAG, "failed code: "  + code);
                            }
                        }
                    });
        }
    }

    /**
     * 获取外网IP
     * */
    public static String[] GetNetIp(){
        String str[] = {"","",""};
        try {
            String address = "http://ip.taobao.com/service/getIpInfo2.php?ip=myip";
            URL url = new URL(address);

            //  URLConnection htpurl=url.openConnection();

            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.7 Safari/537.36"); //设置浏览器ua 保证不出现503

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();

                // 将流转化为字符串
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));

                String tmpString = "";
                StringBuilder retJSON = new StringBuilder();
                while ((tmpString = reader.readLine()) != null) {
                    retJSON.append(tmpString + "\n");
                }

                JSONObject jsonObject = new JSONObject(retJSON.toString());
                String code = jsonObject.getString("code");

                Log.e("提示：" ,retJSON.toString());
                if (code.equals("0")) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    str[0] = data.getString("ip") ;
                    str[1] = data.getString("region") + data.getString("city");

                    Log.e("提示", "您的IP地址是：" + str[0]);
                } else {
                    str[2] = "IP接口异常，无法获取IP地址！";
                    Log.e("提示", "IP接口异常，无法获取IP地址！");
                }
            } else {
                str[2] = "网络连接异常，无法获取IP地址！";
                Log.e("提示", "网络连接异常，无法获取IP地址！");
            }
        } catch (Exception e) {
            str[2] ="获取IP地址时出现异常";
            Log.e("提示", "获取IP地址时出现异常，异常信息是：" + e.toString());
        }
        Log.e("提示",str[0]+ ":" + str[1] + ":" + str[2]);
        return str;
    }

    /**
     * 精确定位 百度SDK，弃用。
     * */
//    private void InitLocationOption() {
////定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
//        mlocationClient = new LocationClient(getApplicationContext());
////声明LocationClient类实例并配置定位参数
//        LocationClientOption locationOption = new LocationClientOption();
//        MyLocationListener myLocationListener = new MyLocationListener();
////注册监听函数
//        mlocationClient.registerLocationListener(myLocationListener);
////可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//        locationOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
////可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
//        locationOption.setCoorType("gcj02");
////可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
//        locationOption.setScanSpan(1000);
////可选，设置是否需要地址信息，默认不需要
//        locationOption.setIsNeedAddress(true);
////可选，设置是否需要地址描述
//        locationOption.setIsNeedLocationDescribe(false);
////可选，设置是否需要设备方向结果
//        locationOption.setNeedDeviceDirect(false);
////可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        locationOption.setLocationNotify(false);
////可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//        locationOption.setIgnoreKillProcess(true);
////可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        locationOption.setIsNeedLocationDescribe(true);
////可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//        locationOption.setIsNeedLocationPoiList(true);
////可选，默认false，设置是否收集CRASH信息，默认收集
//        locationOption.SetIgnoreCacheException(false);
////可选，默认false，设置是否开启Gps定位
//        locationOption.setOpenGps(false);
////可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
//        locationOption.setIsNeedAltitude(false);
////设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
//        locationOption.setOpenAutoNotifyMode();
////设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
//        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
////需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
//        mlocationClient.setLocOption(locationOption);
////开始定位
//        mlocationClient.start();
//    }
//    /**
//     * 实现定位回调，成功便查询天气，失败则提示
//     */
//    public class MyLocationListener extends BDAbstractLocationListener {
//        @Override
//        public void onReceiveLocation(BDLocation location){
//            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
//            //以下只列举部分获取经纬度相关（常用）的结果信息
//            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
//
//            //获取纬度信息
//            double latitude = location.getLatitude();
//            Log.i("result latitude",String.valueOf(latitude));
//
//            //获取经度信息
//            double longitude = location.getLongitude();
//            Log.i("result longitude",String.valueOf(longitude));
//
//            int errorCode = location.getLocType();
//            Log.i("result longitude",String.valueOf(errorCode));
//
//            if(errorCode != 161){
//                new Thread() {
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                mWeather.setText("");
//                                mTemperatureText.setText("获取失败，请保证开启位置权限及网络畅通");
//                            }
//
//                        });
//                    }
//                }.start();
//            }
//
//            mAddrStr = location.getAddrStr();
//            Log.i("result", mAddrStr);
//
//            StringBuilder sb = new StringBuilder();
//            sb.append(String.valueOf(longitude));
//            sb.append(",");
//            sb.append(String.valueOf(latitude));
//            String str = sb.toString();
//            Log.i("result",str);
//            getWeatherByLocation(str);
//        }
//    }

    /**
     * 通过网络接口取
     * @return
     */
    private static String getNewMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
