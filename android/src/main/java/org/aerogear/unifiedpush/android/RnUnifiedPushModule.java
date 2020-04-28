package org.aerogear.unifiedpush.android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;

import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfiguration;

import java.net.URI;

public class RnUnifiedPushModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final ReactMessageHandler messageHandler = new ReactMessageHandler();

    public RnUnifiedPushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RnUnifiedPush";
    }

    @ReactMethod
    public void init(
            ReadableMap config, final Callback successCallback, final Callback cancelCallback) {
  
      Log.i("REG", "INIT CALLED")          ;

      RegistrarManager.config("register", AeroGearFCMPushConfiguration.class)
          .setPushServerURI(URI.create(config.getString("url")))
          .setSenderId(config.getString("senderId"))
          .setVariantID(config.getString("variantId"))
          .setSecret(config.getString("secret"))
          .setAlias(config.getString("alias"))
          .asRegistrar();
  
      PushRegistrar registrar = RegistrarManager.getRegistrar("register");
      registrar.register(
          getCurrentActivity().getApplicationContext(),
          new org.jboss.aerogear.android.core.Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
              new Handler(Looper.getMainLooper())
                  .post(
                      new Runnable() {
                        @Override
                        public void run() {
                          successCallback.invoke();
                        }
                      });
            }
  
            @Override
            public void onFailure(final Exception exception) {
              new Handler(Looper.getMainLooper())
                  .post(
                      new Runnable() {
                        @Override
                        public void run() {
                          Log.e("REGISTRATION", exception.getMessage(), exception);
                          cancelCallback.invoke(exception.getMessage());
                        }
                      });
            }
          });
    }
  
    @ReactMethod
    public void registerMessageHandler(Callback messageCallback) {
      messageHandler.toCall = messageCallback;
      RegistrarManager.registerMainThreadHandler(messageHandler);
    }
  
    @ReactMethod
    public void unregisterMessageHandler(Callback messageCallback) {
      messageHandler.toCall = null;
      RegistrarManager.unregisterMainThreadHandler(messageHandler);
    }
  
    private static class ReactMessageHandler implements MessageHandler {
  
      Callback toCall;
  
      @Override
      public synchronized void onMessage(Context context, Bundle message) {
        if (toCall != null) {
          toCall.invoke(message.getString("alert"));
          toCall = null;
        }
      }
    }
}
