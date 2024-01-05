package com.zzh.yummy.demo;

import android.app.Application;
import android.util.Log;

import com.kwai.video.ksmediaplayerkit.KSMediaPlayerConfig;
import com.kwai.video.ksmediaplayerkit.KSMediaPlayerException;
import com.kwai.video.ksmediaplayerkit.Logger.KSMediaPlayerLogListener;

/**
 * author: zhouzhihui
 * created on: 2024/1/6 02:53
 * description:
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KSMediaPlayerConfig.setLogListener(new KSMediaPlayerLogListener() {
            @Override
            public void v(String tag, String message, Throwable e) {
                Log.v(tag, "demoApp " + message, e);
            }

            @Override
            public void i(String tag, String message, Throwable e) {
                Log.i(tag, "demoApp " + message, e);
            }

            @Override
            public void d(String tag, String message, Throwable e) {
                Log.d(tag, "demoApp " + message, e);
            }

            @Override
            public void w(String tag, String message, Throwable e) {
                Log.w(tag, "demoApp " + message, e);
            }

            @Override
            public void e(String tag, String message, Throwable e) {
                Log.e(tag, "demoApp " + message, e);
            }
        });
        String did = "zzhYummyAarDemo";
        KSMediaPlayerConfig.init(this, "ks699183843584817611", "did",
                this.getFilesDir() + "/mdb", new KSMediaPlayerConfig.OnInitListener() {
                    @Override
                    public void onInitSuccess() {
                    }

                    @Override
                    public void onInitError(KSMediaPlayerException e) {
                    }
                });

    }
}
