package com.adai.gkdnavi.utils;

/**
 * Created by admin on 2016/7/20.
 */
public final class APNMatchTools {
    public static class APNNet{

        public static String CMWAP = "cmwap";

        public static String CMNET = "cmnet";
//中国联通3GWAP设置        中国联通3G因特网设置        中国联通WAP设置        中国联通因特网设置
//3gwap                 3gnet                uniwap            uninet

        public static String GWAP_3 = "3gwap";

        public static String GNET_3="3gnet";

        public static String UNIWAP="uniwap";

        public static String UNINET="uninet";
    }
    public static String matchAPN(String currentName) {
        if("".equals(currentName) || null==currentName){
            return "";
        }
        currentName = currentName.toLowerCase();
        if(currentName.startsWith(APNNet.CMNET))
            return APNNet.CMNET;
        else if(currentName.startsWith(APNNet.CMWAP))
            return APNNet.CMWAP;
        else if(currentName.startsWith(APNNet.GNET_3))
            return APNNet.GNET_3;
        else if(currentName.startsWith(APNNet.GWAP_3))
            return APNNet.GWAP_3;
        else if(currentName.startsWith(APNNet.UNINET))
            return APNNet.UNINET;
        else if(currentName.startsWith(APNNet.UNIWAP))
            return APNNet.UNIWAP;
        else if(currentName.startsWith("default"))
            return "default";
        else return "";
// return currentName.substring(0, currentName.length() - SUFFIX.length());
    }
}
