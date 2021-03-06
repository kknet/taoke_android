package com.github.caoyouxin.taoke;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.baichuan.android.trade.callback.AlibcTradeInitCallback;
import com.github.caoyouxin.taoke.api.RxHelper;
import com.github.caoyouxin.taoke.ui.widget.MyRefreshFooter;
import com.github.caoyouxin.taoke.util.ShareHelper;
import com.github.gnastnosaj.boilerplate.Boilerplate;
import com.github.gnastnosaj.boilerplate.mvchelper.RxDataSource;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.io.IOException;
import java.net.SocketException;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;

//import io.realm.Realm;
//import io.realm.RealmMigration;
//import io.realm.RealmSchema;


public class TaoKe extends Application {
//    private static RealmMigration realmMigration;
//
//    public static RealmMigration getRealmMigration() {
//        if (realmMigration == null) {
//            realmMigration = (realm, oldVersion, newVersion) -> {
//                RealmSchema schema = realm.getSchema();
//                //migration
//            };
//        }
//        return realmMigration;
//    }

    @Override
    public void onCreate() {
        super.onCreate();

        Boilerplate.initialize(this, new Boilerplate.Config.Builder().leakCanary(true).build());

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                return;
            }
            Timber.w(e, "Undeliverable exception received, not sure what to do");
        });

        RxDataSource.addHook(new RxHelper.HandleServerExp());

        ShareHelper.initialize(this);

        SmartRefreshLayout.setDefaultRefreshHeaderCreater((Context context, RefreshLayout layout) -> new ClassicsHeader(context));
        SmartRefreshLayout.setDefaultRefreshFooterCreater((context, layout) -> {
            MyRefreshFooter myRefreshFooter = new MyRefreshFooter(context);
            myRefreshFooter.getTitleText().setText(R.string.we_have_underline);
            myRefreshFooter.getArrowView().setImageURI(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.mipmap.smile));
            return myRefreshFooter;
        });

//        Realm.init(this);

        AlibcTradeSDK.asyncInit(this, new AlibcTradeInitCallback() {
            @Override
            public void onSuccess() {
                //初始化成功，设置相关的全局配置参数
                Toast.makeText(TaoKe.this, "初始化成功", Toast.LENGTH_LONG).show();

                // 是否使用支付宝
                AlibcTradeSDK.setShouldUseAlipay(false);

                // 设置是否使用同步淘客打点
                AlibcTradeSDK.setSyncForTaoke(true);

                // 是否走强制H5的逻辑，为true时全部页面均为H5打开
                AlibcTradeSDK.setForceH5(false);

                // 设置全局淘客参数，方便开发者用同一个淘客参数，不需要在show接口重复传入
//                AlibcTradeSDK.setTaokeParams(new AlibcTaokeParams());

                // 设置渠道信息(如果有渠道专享价，需要设置)
//                AlibcTradeSDK.setChannel(typeName, channelName);

                // ...
            }

            @Override
            public void onFailure(int code, String msg) {
                //初始化失败，可以根据code和msg判断失败原因，详情参见错误说明
                Toast.makeText(TaoKe.this, String.format("百川初始化失败, code=%d, msg=%s", code, msg), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AlibcTradeSDK.destory();
    }
}
