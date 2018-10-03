package com.yunisrajab.curator.Activities;

import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yunisrajab.curator.AdminReceiver;
import com.yunisrajab.curator.R;

public class CosuActivity extends AppCompatActivity {

    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;
    private static final String Battery_PLUGGED_ANY = Integer.toString(
            BatteryManager.BATTERY_PLUGGED_AC |
                    BatteryManager.BATTERY_PLUGGED_USB |
                    BatteryManager.BATTERY_PLUGGED_WIRELESS);

    private static final String DONT_STAY_ON = "0";
    String  TAG =   "Cosu";

    Button  lockButton, unlockButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockout);

        mAdminComponentName = AdminReceiver.getComponentName(this);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mPackageManager = getPackageManager();


        mPackageManager.setComponentEnabledSetting(
                new ComponentName(getApplicationContext(),
                        CosuActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        if(mDevicePolicyManager.isDeviceOwnerApp(getPackageName())){
            setDefaultCosuPolicies(true);
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "This app is not set as device owner and cannot start lock task mode",
                    Toast.LENGTH_SHORT).show();
        }


//        startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")));

        lockButton  =   findViewById(R.id.lockButton);
        unlockButton    =   findViewById(R.id.unlockButton);

        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        mAdminComponentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "You need to activate Device Administrator to perform phonelost tasks!");
                startActivityForResult(intent, 9);

                mDevicePolicyManager.addUserRestriction(mAdminComponentName,
                        UserManager.DISALLOW_ADJUST_VOLUME);
//                setDefaultCosuPolicies(true);
            }
        });

        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefaultCosuPolicies(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9)   {
            Toast.makeText(this, "Success"+data, Toast.LENGTH_SHORT).show();
        }

        else    {
            Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        // start lock task mode if its not already active
//        if(mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())){
//            ActivityManager am = (ActivityManager) getSystemService(
//                    Context.ACTIVITY_SERVICE);
//            if(am.getLockTaskModeState() ==
//                    ActivityManager.LOCK_TASK_MODE_NONE) {
//                startLockTask();
//            }
//        }
//    }

    private void setDefaultCosuPolicies(boolean active) {
        // set user restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        // disable keyguard and status bar
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);

        // enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

        // set System Update policy

        if (active){
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                    SystemUpdatePolicy.createWindowedInstallPolicy(60,120));
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null);
        }

        // set this Activity as a lock task package

        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,
                active ? new String[]{getPackageName()} : new String[]{});

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            mDevicePolicyManager.addPersistentPreferredActivity(
                    mAdminComponentName, intentFilter, new ComponentName(
                            getPackageName(), CosuActivity.class.getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                    mAdminComponentName, getPackageName());
        }
    }

    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName,
                    restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName,
                    restriction);
        }
    }

    private void enableStayOnWhilePluggedIn(boolean enabled) {
        if (enabled) {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Battery_PLUGGED_ANY);
        } else {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN, DONT_STAY_ON);
        }
    }

    // TODO: Implement the rest of the Activity
}