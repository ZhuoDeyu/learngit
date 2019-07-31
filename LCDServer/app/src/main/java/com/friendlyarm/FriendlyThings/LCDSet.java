package com.friendlyarm.FriendlyThings;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/* for thread */

public class LCDSet{

    private Handler messageHandler;
    private Thread mThread;
    private MyTask mMyTask;
    private EditText mSpeedEdit;
    private Button mButton;
    private static int devFD = -1;
    final int MAX_LINE_SIZE = 16;
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;

    public LCDSet(){
        String i2cDevName = "/dev/i2c-0";
        int boardType = HardwareControler.getBoardType();
        if (boardType == BoardType.NanoPC_T4 || boardType == BoardType.NanoPi_M4 || boardType == BoardType.NanoPi_NEO4 || boardType == BoardType.SOM_RK3399) {
            i2cDevName = "/dev/i2c-2";
        }
        devFD = HardwareControler.open(i2cDevName, FileCtlEnum.O_RDWR);
        if (devFD < 0) {

        } else {
            // for LCD1602 (chip: pcf8574)
            if (HardwareControler.setI2CSlave(devFD, 0x27) < 0) {

            } else {

            }
        }
    }

    public void setShow(String str_to_show,int speed,int direction) {
        Log.i("result","call method to change ");
            if (mMyTask != null && mMyTask.getStatus() == AsyncTask.Status.RUNNING) {
                mMyTask.cancel(true);
                try {
                    Thread.sleep(500, 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mMyTask = new MyTask();
            mMyTask.execute(str_to_show, String.valueOf(speed), String.valueOf(direction), null, null);
    }

    private int LCD1602Init() throws InterruptedException {
        Thread.sleep(0, 1000 * 15);
        if (PCF8574.writeCmd4(devFD, (byte) (0x03 << 4)) == -1) {
            return -1;
        }
        Thread.sleep(0, 100 * 41);
        if (PCF8574.writeCmd4(devFD, (byte) (0x03 << 4)) == -1) {
            return -1;
        }
        Thread.sleep(0, 100);
        if (PCF8574.writeCmd4(devFD, (byte) (0x03 << 4)) == -1) {
            return -1;
        }
        if (PCF8574.writeCmd4(devFD, (byte) (0x02 << 4)) == -1) {
            return -1;
        }
        if (PCF8574.writeCmd8(devFD, (byte) (0x28)) == -1) {
            return -1;
        }
        if (PCF8574.writeCmd8(devFD, (byte) (0x0c)) == -1) {
            return -1;
        }
        Thread.sleep(0, 2000);
        if (PCF8574.writeCmd8(devFD, (byte) (0x06)) == -1) {
            return -1;
        }
        if (PCF8574.writeCmd8(devFD, (byte) (0x02)) == -1) {
            return -1;
        }
        Thread.sleep(0, 2000);
        return 0;
    }

    private int LCD1602DispStr(byte x, byte y, String str) throws InterruptedException {
        byte addr;
        addr = (byte) (((y + 2) * 0x40) + x);

        if (PCF8574.writeCmd8(devFD, addr) == -1) {
            return -1;
        }
        byte[] strBytes = str.getBytes();
        byte b;

        for (int i = 0; i < strBytes.length && i < MAX_LINE_SIZE; i++) {
            b = strBytes[i];
            if (PCF8574.writeData8(devFD, b) == -1) {
                return -1;
            }
        }
        return 0;
    }

    private int LCD1602DispLines(String line1, int line_1_start,String line2, int line_2_start) throws InterruptedException {

        int ret = LCD1602DispStr((byte) line_1_start, (byte) 0, line1);
        if (ret != -1 && line2.length() > 0) {
            ret = LCD1602DispStr((byte) line_2_start, (byte) 1, line2);
        }
        return ret;
    }

    private int LCD1602Clear() throws InterruptedException {
        if (PCF8574.writeCmd8(devFD, (byte) 0x01) == -1) {
            return -1;
        }
        return 0;
    }

    private boolean lcd1602Inited = false;

    private void sendMessage(String msg) {
        Message message = Message.obtain();
        message.obj = msg;
        messageHandler.sendMessage(message);
    }



    private class MyTask extends AsyncTask<String, Void, Void> {
        private volatile boolean running = true;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!lcd1602Inited) {
                try {
                    if (LCD1602Init() == 0) {
                        lcd1602Inited = true;
                    }
                } catch (Exception e) {
                    return null;
                }
            }

            if (!lcd1602Inited) {
                return null;
            }

            while (running) {
                String displayText = params[0];
                int speed = Integer.parseInt(params[1]);
                int direction = Integer.parseInt(params[2]);
                Log.i("result",displayText+speed + direction);
                try {
                    LCD1602Clear();
                    String strToShow = "";
                    String line1 = "";
                    String line2 = "";
                    if (DIRECTION_LEFT_TO_RIGHT == direction) {
                        for (int position = displayText.length() - 1; position >= 0; position--) {

                            strToShow = displayText.substring(position, displayText.length());

                            System.out.println("result show " + strToShow);

                            line1 = "";
                            line2 = "";
                            int i = 0;
                            for (i = 0; i < strToShow.length(); i++) {
                                if (isCancelled()) {
                                    return null;
                                }
                                if (line1.length() >= MAX_LINE_SIZE) {
                                    if (line2.length() >= MAX_LINE_SIZE) {
                                        break;
                                    } else {
                                        line2 = line2 + strToShow.charAt(i);
                                    }
                                } else {
                                    line1 = line1 + strToShow.charAt(i);
                                }
                            }
                            Log.i("result line1> ", line1);
                            Log.i("result line2>", line2);
                            LCD1602Clear();
                            if (isCancelled()) {
                                return null;
                            }
                            if (LCD1602DispLines(line1
                                    , 0
                                    , line2
                                    , 0) == -1) {
                                Log.i("result", "error");
                                return null;
                            }
                            Thread.sleep(speed);
                        }
                        int k = 0;
                        while (k < 64) {
                            if (isCancelled()) {
                                return null;
                            }
                            line1 = "";
                            line2 = "";
                            int i = 0;
                            if (k + displayText.length() > 32) {
                                if ((displayText.length() - k) > 0 && displayText.length() > 32) {
                                    strToShow = displayText.substring(displayText.length() - k) + " "
                                            + displayText.substring(0, displayText.length() - k);
                                    for (i = 0; i < strToShow.length(); i++) {
                                        if (isCancelled()) {
                                            return null;
                                        }
                                        if (line1.length() >= MAX_LINE_SIZE) {
                                            if (line2.length() >= MAX_LINE_SIZE) {
                                                break;
                                            } else {
                                                line2 = line2 + strToShow.charAt(i);
                                            }
                                        } else {
                                            line1 = line1 + strToShow.charAt(i);
                                        }
                                    }
                                } else {
                                    strToShow = displayText.substring(32 - k);
                                    for (int count = 0; count < 32 - displayText.length(); ++count) {
                                        strToShow = strToShow + " ";
                                    }
                                    strToShow = strToShow + displayText.substring(0, 32 - k);
                                    for (i = 0; i < strToShow.length(); i++) {
                                        if (isCancelled()) {
                                            return null;
                                        }
                                        if (line1.length() >= MAX_LINE_SIZE) {
                                            if (line2.length() >= MAX_LINE_SIZE) {
                                                break;
                                            } else {
                                                line2 = line2 + strToShow.charAt(i);
                                            }
                                        } else {
                                            line1 = line1 + strToShow.charAt(i);
                                        }
                                    }
                                }

                            } else if (k + displayText.length() > 16) {
                                if (16 - k > 0) {
                                    line1 = displayText.substring(0, 16 - k);
                                    line2 = displayText.substring(16 - k, displayText.length());
                                } else {
                                    line1 = "";
                                    line2 = displayText.substring(0, displayText.length());
                                }
                            } else {
                                line1 = displayText;
                                line2 = "";
                            }
                            k++;
                            k = k % 32;
                            LCD1602Clear();
                            if (isCancelled()) {
                                return null;
                            }
                            Log.i("result line1> ", line1);
                            Log.i("result line2>", line2);
                            if (LCD1602DispLines(line1
                                    , (k+displayText.length())>32?0:k%16
                                    , line2
                                    , (k<32)?(k<16?0:k-16):0) == -1) {
                                Log.i("result", "error");
                                return null;
                            }
                            Thread.sleep(speed);
                        }

                    } else if(DIRECTION_RIGHT_TO_LEFT== direction){
                        Log.i("result  < ",displayText+speed + direction);
                        int k = 0;
                        for (; k <= (32 + displayText.length()); k++) {
                            if (isCancelled()) {
                                return null;
                            }
                            if (k > 32) {
                                if (displayText.length() > k) {
                                    line1 = displayText.substring(k - 32, k - 16);
                                    line2 = displayText.substring(k - 16, k);
                                } else if (displayText.length() > k - 16) {
                                    line1 = displayText.substring(k - 32, k - 16);
                                    line2 = displayText.substring(k - 16);
                                } else {
                                    line1 = displayText.substring(k - 32);
                                    line2 = "";
                                }
                            } else if (k >= 16) {
                                if (k > displayText.length()+16) {
                                    line1 = displayText;
                                    line2 = "";
                                } else {
                                    line1 = displayText.substring(0, k - 16);
                                    line2 = displayText.substring(k - 16);
                                }
                            } else {
                                line1 = "";
                                if(line2.length()>k){
                                    line2 = displayText.substring(0, k);
                                }else{
                                    line2 = displayText;
                                }
                            }
                            LCD1602Clear();
                            if (isCancelled()) {
                                return null;
                            }
                            Log.i("result line1 <", line1);
                            Log.i("result line2 <", line2);

                            if (LCD1602DispLines(line1
                                    , (k <32 ) ? ((k<16)?0:32-k):0
                                    , line2
                                    , (k < 16) ? 16-k:0) == -1) {
                                Log.i("result", "error");
                                return null;
                            }
                        }
                        Thread.sleep( speed, 0);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            Log.i("result", "切换任务");
            return null;
        }
        @Override
        protected void onCancelled() {
            running= false;
        }
    }
}
//java -jar ./signapk.jar platform.x509.pem platform.pk8 ./app-debug.apk app-Signed.apk