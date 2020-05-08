package org.aerogear.unifiedpush.android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfiguration;

import java.net.URI;

public class RnUnifiedPushModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public RnUnifiedPushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RnUnifiedPush";
    }

    @ReactMethod
    public void initialize(
            ReadableMap config, final Promise promise) {
  
      Log.i("REG", "INIT CALLED")          ;

      ReadableMap androidConfig = config.getMap("android");

      RegistrarManager.config("register", AeroGearFCMPushConfiguration.class)
          .setPushServerURI(URI.create(config.getString("pushServerURL")))
          .setSenderId(androidConfig.getString("senderID"))
          .setVariantID(androidConfig.getString("variantID"))
          .setSecret(androidConfig.getString("variantSecret"))
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
                          promise.resolve(null);
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
                          promise.reject(exception);
                        }
                      });
            }
          });
    }

}
