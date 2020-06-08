package org.aerogear.unifiedpush.android;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.ReactApplication;

import org.jboss.aerogear.android.unifiedpush.MessageHandler;

import java.util.List;

public class ReactNativeMessageHandler implements MessageHandler {
    @Override
    public void onMessage(final Context androidContext, final Bundle message) {
        if (!isAppOnForeground((androidContext))) {
            Intent serviceIntent = new Intent(androidContext, ReactNativeMessageService.class);
            serviceIntent.putExtra("message", message);
            androidContext.startService(serviceIntent);
            HeadlessJsTaskService.acquireWakeLockNow(androidContext);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(
                    new Runnable() {
                        public void run() {
                            final ReactInstanceManager reactInstanceManager =
                                    ((ReactApplication) androidContext.getApplicationContext())
                                            .getReactNativeHost()
                                            .getReactInstanceManager();
                            ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
                            if (reactContext == null) {
                                reactInstanceManager.addReactInstanceEventListener(
                                        new ReactInstanceManager.ReactInstanceEventListener() {
                                            @Override
                                            public void onReactContextInitialized(ReactContext reactContext) {
                                                sendToJavaScript(reactContext, message.getString("alert"));
                                                reactInstanceManager.removeReactInstanceEventListener(this);
                                            }
                                        });
                                if (!reactInstanceManager.hasStartedCreatingInitialContext()) {
                                    reactInstanceManager.createReactContextInBackground();
                                }
                            } else {
                                sendToJavaScript(reactContext, message.getString("alert"));
                            }
                        }
                    });
        }
    }

    private void sendToJavaScript(ReactContext reactContext, String alert) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onDefaultMessage", alert);
    }

    /**
     * From https://reactnative.dev/docs/headless-js-android.html
     *
     * @param context android context
     * @return true if the application has a viewable activity.
     */
    private boolean isAppOnForeground(Context context) {
        /**
         We need to check if app is in foreground otherwise the app will crash.
         http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
         **/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}