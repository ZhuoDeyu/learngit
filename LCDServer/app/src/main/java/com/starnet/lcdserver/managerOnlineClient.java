package com.starnet.lcdserver;

public interface managerOnlineClient{
    public int addOnlineClient(String ip,String userName);
    public int deleteOnlineClient(String ip,String userName);
    public String changeLCDText(String str);
    public void changeLCDSpeed(int speed);
    public void changePasswordInList(String userName);
    public void changePasswordInThread(String userName,String password);
    public void deleteSavedUser(String userName);
    public String getLCDText();
    public boolean check(String userName,String password);
}