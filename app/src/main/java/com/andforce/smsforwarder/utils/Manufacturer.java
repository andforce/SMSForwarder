package com.andforce.smsforwarder.utils;

import android.os.Build;

public class Manufacturer {

    public static boolean isXiaomi(){
        return Build.MANUFACTURER.equalsIgnoreCase("xiaomi");
    }

    public static boolean isVivo(){
        return Build.MANUFACTURER.equalsIgnoreCase("vivo");
    }

    public static boolean isSmartisan(){
        return Build.MANUFACTURER.equalsIgnoreCase("smartisan");
    }
}
