package com.starnet.lcdserver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Context;

    public class SetIPStatic {
        public SetIPStatic(Context context){
                    WifiConfiguration wifiConfig=null;
                    WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo connectionInfo=wifiManager.getConnectionInfo();
                    List<WifiConfiguration> configuredNetworks=wifiManager.getConfiguredNetworks();
                    //使用WIFI时继续操作
                    if(configuredNetworks!=null){
                        for (WifiConfiguration conf : configuredNetworks) {
                            if (conf.networkId==connectionInfo.getNetworkId()) {
                                wifiConfig=conf;
                                break;
                            }
                        }
                    }else{
                        return;
                    }

                    try {
                        setStaticIpConfiguration(wifiManager, wifiConfig, InetAddress.getByName("192.168.1.202"), 24, InetAddress.getByName("192.168.1.202"), InetAddress.getAllByName("192.168.1.202"));
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

        public static void setStaticIpConfiguration(WifiManager manager,
                                                    WifiConfiguration config, InetAddress ipAddress, int prefixLength,
                                                    InetAddress gateway, InetAddress[] dns)
                throws ClassNotFoundException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException,
                NoSuchMethodException, NoSuchFieldException, InstantiationException {
            // First set up IpAssignment to STATIC.
            Object ipAssignment = getEnumValue(
                    "android.net.IpConfiguration$IpAssignment", "STATIC");
            callMethod(config, "setIpAssignment",
                    new String[] { "android.net.IpConfiguration$IpAssignment" },
                    new Object[] { ipAssignment });

            // Then set properties in StaticIpConfiguration.
            Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");
            Object linkAddress = newInstance("android.net.LinkAddress",
                    new Class[] { InetAddress.class, int.class }, new Object[] {
                            ipAddress, prefixLength });

            setField(staticIpConfig, "ipAddress", linkAddress);
            setField(staticIpConfig, "gateway", gateway);

            ArrayList<Object> aa= (ArrayList<Object>) getField(staticIpConfig, "dnsServers");
            aa.clear();
            for (int i = 0; i < dns.length; i++)
                aa.add(dns[i]);
            callMethod(config, "setStaticIpConfiguration",
                    new String[] { "android.net.StaticIpConfiguration" },
                    new Object[] { staticIpConfig });
            manager.updateNetwork(config);
            manager.saveConfiguration();
        }

        private static Object newInstance(String className)
                throws ClassNotFoundException, InstantiationException,
                IllegalAccessException, NoSuchMethodException,
                IllegalArgumentException, InvocationTargetException {
            return newInstance(className, new Class[0], new Object[0]);
        }

        private static Object newInstance(String className,
                                          Class[] parameterClasses, Object[] parameterValues)
                throws NoSuchMethodException, InstantiationException,
                IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, ClassNotFoundException {
            Class clz = Class.forName(className);
            Constructor constructor = clz.getConstructor(parameterClasses);
            return constructor.newInstance(parameterValues);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static Object getEnumValue(String enumClassName, String enumValue)
                throws ClassNotFoundException {
            Class enumClz = (Class) Class.forName(enumClassName);
            return Enum.valueOf(enumClz, enumValue);
        }

        private static void setField(Object object, String fieldName, Object value)
                throws IllegalAccessException, IllegalArgumentException,
                NoSuchFieldException {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.set(object, value);
        }

        private static  Object getField(Object object, String fieldName)
                throws IllegalAccessException, IllegalArgumentException,
                NoSuchFieldException {
            Field field = object.getClass().getDeclaredField(fieldName);
            Object out = field.get(object);
            return out;
        }

        private static void callMethod(Object object, String methodName,
                                       String[] parameterTypes, Object[] parameterValues)
                throws ClassNotFoundException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException,
                NoSuchMethodException {
            Class[] parameterClasses = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++)
                parameterClasses[i] = Class.forName(parameterTypes[i]);

            Method method = object.getClass().getDeclaredMethod(methodName,
                    parameterClasses);
            method.invoke(object, parameterValues);
        }

        //直接使用set方法调用 可能遇到需要地址转换方法如下：
        public static String int2ip(int ip) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf((int) (ip & 0xff)));
            sb.append('.');
            sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
            sb.append('.');
            sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
            sb.append('.');
            sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
            return sb.toString();
        }
    }
