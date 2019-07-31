package com.starnet.systemmonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 计算器不支持负数运算
 * */

public class ServiceReader extends AppCompatActivity implements View.OnClickListener{

    //用mem数组保存MemTotal,MemFree,Buffers,Cached,Active,Inactive
    int[] mem= {1,1,1,1,1,1};
    //用cpuLong数组保存计算时需要的4个部分的数值,后两个数值保存当前进程的相关时间
    long[] cpuLong = {0,0,0,0,0,0,0,0};

    private TextView mMenTotalText;
    private TextView mMemFree;
    private TextView mBuffersText;
    private TextView mCachedText;
    private TextView mActiveText;
    private TextView mInactiveText;
    private TextView mMemFreePect;
    private TextView mBuffersTextPect;
    private TextView mCachedTextPect;
    private TextView mActiveTextPect;
    private TextView mInactiveTextPect;
    private TextView mCpuUsageText;
    private TextView mPidText;
    private TextView mPidNameText;
    private static final int UPDATE_UI = 0;
    private Handler mHandler= new Handler(){
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
                case UPDATE_UI:
                    update();
                    break;
                default:
                    break;
            }
        }
    };

    private Button[] buttons = new Button[18];
    private int[] ids = new int[]{R.id.bt1,R.id.bt2,R.id.bt3,R.id.bt4,R.id.bt5,R.id.bt6,R.id.bt7,
            R.id.bt8,R.id.bt9,R.id.bt10,R.id.bt11,R.id.bt12,R.id.bt13,R.id.bt14,R.id.bt15,R.id.bt16,R.id.bt17,R.id.bt18
    };

    private TextView textView;
    private String expression = "";
    private boolean end = false;
    private int countOperate=2;
    int point = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_service_reader);

        mMenTotalText = (TextView) findViewById(R.id.mem_total);
        mMemFree = (TextView)findViewById(R.id.mem_free);
        mBuffersText = (TextView)findViewById(R.id.buffers);
        mCachedText = (TextView)findViewById(R.id.cached);
        mActiveText = (TextView)findViewById(R.id.active);
        mInactiveText = (TextView)findViewById(R.id.inactive);
        mMemFreePect = (TextView)findViewById(R.id.mem_free_percent);
        mBuffersTextPect = (TextView)findViewById(R.id.buffers_percent);
        mCachedTextPect = (TextView)findViewById(R.id.cached_percent);
        mActiveTextPect = (TextView)findViewById(R.id.active_percent);
        mInactiveTextPect = (TextView)findViewById(R.id.inactive_percent);
        mCpuUsageText = (TextView)findViewById(R.id.cpu_usage);
        mPidText = (TextView)findViewById(R.id.pid);
        mPidNameText = (TextView)findViewById(R.id.pid_name);

        new Thread(){
            public void run(){
                while(true){
                    mem = getMem(mem);
                    cpuLong = getCpuUsage(cpuLong);
                    mHandler.sendEmptyMessage(UPDATE_UI);
                    SystemClock.sleep(1000);
                }
            }
        }.start();
        for(int i=0;i<ids.length;i++){
            buttons[i] = (Button)findViewById(ids[i]);
            buttons[i].setOnClickListener(this);
        }
        textView = (TextView)findViewById(R.id.contentText);
        textView.setText("");

        mPidNameText.setText(getPidName());
    }

    public void update()
    {
        mMenTotalText.setText(mem[0] + "kB");
        mMemFree.setText(mem[1] + "kB");
        mBuffersText.setText(mem[2] + "kB");
        mCachedText.setText(mem[3] + "kB");
        mActiveText.setText(mem[4] + "kB");
        mInactiveText.setText(mem[5] + "kB");

        double usage;
        usage = 100.0*mem[1]/mem[0];
        mMemFreePect.setText(String.format("    %.2f %%",usage));
        usage = 100.0*mem[2]/mem[0];
        mBuffersTextPect.setText(String.format("    %.2f %%",usage));
        usage = 100.0*mem[3]/mem[0];
        mCachedTextPect.setText(String.format("    %.2f %%",usage));
        usage = 100.0*mem[4]/mem[0];
        mActiveTextPect.setText(String.format("    %.2f %%",usage));
        usage = 100.0*mem[5]/mem[0];
        mInactiveTextPect.setText(String.format("    %.2f %%",usage));

//totalCPUrate = ((totalCPUTime2-idle2)-(totalCPUTime1-idle1))／（totalCPUTime2-totalCPUTime1）x100%
        usage = 100*(1.0*(cpuLong[2]-cpuLong[3])-1.0*(cpuLong[0]-cpuLong[1]))/(1.0*(cpuLong[2]-cpuLong[0]));
        String usageText = String.format("    %.2f %%",usage);
        mCpuUsageText.setText(usageText);
        usage = 100*(1.0*(cpuLong[5]-cpuLong[4])/(cpuLong[2]-cpuLong[0]));
        mPidText.setText(String.format("PID  %d  %.2f %% ",getPid(),usage));
    }

    public native int[] getMemFromC(int [] arr);
    public native long[] getCpuUsageFromC(long [] cpuLong);
    public native int[] getMemJavaCallback();
    public native String getResult(String expression);
    public native int getPid();
    public native String getPidName();
    static
    {
        System.loadLibrary("systeminfo");
    }

    int[] getMem(int[] mem){
        //return getMemFromC(arr);
        return getMemJavaCallback();
    }

    int[] getMemFromJava()
    {
        //按教程不使用，我不知道怎么去设置mem数组的值在何处设置
        //我直接在回调函数中使用的函数内调用原先的函数
        return getMemFromC(mem);
    }

    long[]  getCpuUsage(long[] cpuLong){
        return getCpuUsageFromC(cpuLong);
    }


    //点击事件，强制排错
    public void onClick(View view) {

        int id = view.getId();
        Button button = (Button)view.findViewById(id);
        String current = button.getText().toString();
        if(end){ //如果上一次算式已经结束，则先清零
            expression = "";
            end = false;
            point = 0;
        }
        if(current.equals("CE")){ //如果为CE则清零
            expression = "";
            countOperate=2;
            point = 0;
        }else if(current.equals("Backspace")){ //如果点击退格
            if(expression.length()>1){
                //算式长度大于1
                char c = expression.charAt(expression.length()-1) ;
                if(c == '.'){
                    point = 0;
                }
                if(point == 2){
                    point = 1;
                }
                expression = expression.substring(0,expression.length()-1);//退一格
                int i = expression.length()-1;
                char tmp = expression.charAt(i); //获得最后一个字符
                char tmpFront = tmp;
                for(;i>=0;i--){ //向前搜索最近的 +-*/和.，并退出
                    tmpFront = expression.charAt(i);
                    if(tmpFront=='.'||tmpFront=='+'||tmpFront=='-'||tmpFront=='*'||tmpFront=='/'){
                        break;
                    }
                }
                if(tmp >= '0' && tmp <='9' ){ //最后一个字符为数字，则识别数赋值为0
                    countOperate=0;
                }else if(tmp==tmpFront&&tmpFront!='.') {
                    countOperate=2; //如果为+-*/，赋值为2
                }else if(tmpFront=='.') {
                    countOperate=1; //如果前面有小数点赋值为1
                }
            }else if(expression.length()==1){
                expression = "";
            }
        }else if(current.equals(".")){
            if(expression.equals("")||countOperate==2){
                expression+="0"+current;
                countOperate = 1;  //小数点按过之后赋值为1
                point = 1;
            }
            if(countOperate==0 && (point == 0 || point == 2)){
                expression+=".";
                countOperate = 1;
                point = 1;
            }
        }else if(current.equals("+")||current.equals("-")||current.equals("*")||current.equals("/")){
            if(expression.length() > 0){
                if(countOperate==0){
                    expression+=current;
                    countOperate = 2;  //  +-*/按过之后赋值为2
                    if(point == 1){
                        point = 2;
                    }else{
                        point = 0;
                    }
                }
            }
        }else if(current.equals("=")){ //按下=时，计算结果并显示
            if(expression.length() > 0){
                char tmp = expression.charAt(expression.length()-1);
                if(tmp == '+' || tmp == '-' || tmp == '*' || tmp == '/' ){
                    Toast.makeText(getApplicationContext(),"式子不能以运算符结束",Toast.LENGTH_SHORT)
                            .show();
                    return;
                }else if(tmp == '.'){
                    expression +="0";
                }

                String result = getResult(expression);
                if(result.equals("除数不能为零")){
                    expression += "  错误，" + result;
                }else{
                    char lastCharOfResult = result.charAt(result.length()-1);
                    while(lastCharOfResult == '0'){
                        result = result.substring(0,result.length()-1);
                        lastCharOfResult = result.charAt(result.length()-1);
                    }
                    if(lastCharOfResult == '.'){
                        result = result.substring(0,result.length()-1);
                    }
                    expression += "=" + result;
                }
                point = 0;
                end = true; //此次计算结束
            }
        } else {//此处是当退格出现2+0时，用current的值替代0
            if (expression.length() >= 1) {
                char tmp1 = expression.charAt(expression.length() - 1);
                if (tmp1 == '0' && expression.length() == 1) {
                    expression = expression.substring(0, expression.length() - 1);
                } else if (tmp1 == '0' && expression.length() > 1) {
                    char tmp2 = expression.charAt(expression.length() - 2);
                    if (tmp2 == '+' || tmp2 == '-' || tmp2 == '*' || tmp2 == '/') {
                        expression = expression.substring(0, expression.length() - 1);
                    }
                }
            }
            expression += current;
            if (countOperate == 2 || countOperate == 1) {
                countOperate = 0;
            }
        }
        textView.setText(expression); //显示出来
    }
}



