package com.yunisrajab.curator;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), AdminReceiver.class);
    }

//    void showToast(Context context, CharSequence msg) {
//        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onEnabled(Context context, Intent intent) {
//        showToast(context, "Device Admin: enabled");
//    }
//
//    @Override
//    public CharSequence onDisableRequested(Context context, Intent intent) {
//        return "Deactivating this app as a device administrator removes the ability of the app to control the device.";
//    }
//
//    @Override
//    public void onDisabled(Context context, Intent intent) {
//        showToast(context, "Device Admin: disabled");
//    }
//
//    @Override
//    public void onPasswordChanged(Context context, Intent intent) {
//        showToast(context, "Device Admin: pw changed");
//    }
//
//    @Override
//    public void onPasswordFailed(Context context, Intent intent) {
//        showToast(context, "Device Admin: pw failed");
//    }
//
//    @Override
//    public void onPasswordSucceeded(Context context, Intent intent) {
//        showToast(context, "Device Admin: pw succeeded");
//    }
}