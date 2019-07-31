package com.startnet.android.musicplayer;
/**
 * 该类用以防止用户过快点击造成响应的延迟与混乱
 **/
public class Check {
    // 两次点击按钮之间的点击间隔不能少于1秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    //保留最近一次点击时间
    private static long lastClickTime;

    //在按钮点击前调用，控制点击频率
    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }
}