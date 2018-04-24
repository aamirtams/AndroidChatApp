package com.applozic.mobicomkit.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.applozic.mobicomkit.uiwidgets.notification.NotificationIntentService;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sunil on 21/3/16.
 */
public class ApplozicSampleApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
       Fabric.with(this, new Crashlytics());
        MobileAds.initialize(this, "ca-app-pub-5246243065157193~1062035864");



    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
