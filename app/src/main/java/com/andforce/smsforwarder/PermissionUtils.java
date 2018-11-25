package com.andforce.smsforwarder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    public static String[] sPermissions = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.INTERNET,
//            Manifest.permission.SYSTEM_ALERT_WINDOW,
//            Settings.ACTION_MANAGE_OVERLAY_PERMISSION
    };

    public static List<String> checkPermissions(Context context) {
        List<String> failedPermissions = new ArrayList<>();

        for (String permission : PermissionUtils.sPermissions) {
            int result = ContextCompat.checkSelfPermission(context.getApplicationContext(), permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                failedPermissions.add(permission);
            }
        }
        return failedPermissions;
    }

    public static List<String> checkPermissions(Context context, String[] permissions, int[] grantResults) {
        List<String> failed = new ArrayList<>();

        for (int i = 0; i < grantResults.length; i++) {
            int result = grantResults[i];
            if (result != PackageManager.PERMISSION_GRANTED) {
                failed.add(permissions[i]);
            }
        }
        return failed;
    }

}
