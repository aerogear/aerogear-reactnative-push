package org.aerogear.unifiedpush.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushRegistrar;

import java.net.URI;
import java.util.ArrayList;

public class RnUnifiedPushModule extends ReactContextBaseJavaModule {

    public static final String MODULE_NAME = "RnUnifiedPush";
    private final ReactApplicationContext reactContext;
    private final ReactMessageHandler messageHandler = new ReactMessageHandler();

    private static final String REACT_NATIVE_PUSH_REGISTRAR_KEY = "org.jboss.aerogear.android.unifiedpush.REGISTRAR";
    private final SharedPreferences prefs;

    public RnUnifiedPushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        //In theory react native modules may not stay in memory between providing the init config
        //which won't change between app boots and register
        //We use prefs to save the init config if this happens and register is called
        //after init has been called before a GC.
        this.prefs = reactContext.getSharedPreferences(MODULE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void initialize(
            ReadableMap config, final Promise promise) {

        try {
            ReadableMap androidConfig = config.getMap("android");
            String uri = config.getString("pushServerURL");
            String senderID = androidConfig.getString("senderID");
            String variantID = androidConfig.getString("variantID");
            String secret = androidConfig.getString("variantSecret");

            //if the uri is invalid we'll catch the exception
            URI.create(uri);


            if (verifyInitialize(uri, senderID, variantID, secret, promise)) {
                prefs.edit()
                        .putString("pushServerURL", uri)
                        .putString("senderID", senderID)
                        .putString("variantID", variantID)
                        .putString("variantSecret", secret)
                        .commit();
                promise.resolve(true);
            }
        } catch (Exception exception) {
            promise.reject(MODULE_NAME, exception);
        }
    }

    private boolean verifyInitialize(String uri, String senderID, String variantID, String secret, Promise promise) {
        if (uri == null) {
            promise.reject(MODULE_NAME, "pushServerURL must not be null");
            return false;
        }
        if (senderID == null) {
            promise.reject(MODULE_NAME, "senderID must not be null");
            return false;
        }
        if (variantID == null) {
            promise.reject(MODULE_NAME, "variantID must not be null");
            return false;
        }
        if (secret == null) {
            promise.reject(MODULE_NAME, "variantSecret must not be null");
            return false;
        }
        return true;
    }

    @ReactMethod
    public void register(
            ReadableMap config, final Promise promise) {
        try {
            if (!verifyPrefs()) {
                promise.reject(MODULE_NAME, "Please call initialize before you register.");
            }

            URI uri = URI.create(prefs.getString("pushServerURL", ""));
            String secret = prefs.getString("variantSecret", "");
            String variantID = prefs.getString("variantID", "");
            String senderID = prefs.getString("senderID", "");


            AeroGearFCMPushConfiguration pushConfig = RegistrarManager.config(REACT_NATIVE_PUSH_REGISTRAR_KEY, AeroGearFCMPushConfiguration.class)
                    .setPushServerURI(uri)
                    .setSenderId(senderID)
                    .setVariantID(variantID)
                    .setSecret(secret);

            if (config.hasKey("alias")) {
                pushConfig.setAlias(config.getString("alias"));
            }

            if (config.hasKey("deviceType")) {
                pushConfig.setDeviceType(config.getString("deviceType"));
            }

            if (config.hasKey("operatingSystem")) {
                pushConfig.setOperatingSystem(config.getString("operatingSystem"));
            }

            if (config.hasKey("categories")) {
                ReadableArray reactCategoriesArray = config.getArray("categories");
                ArrayList<String> categoriesList = new ArrayList<>();
                for (int i = 0; i < reactCategoriesArray.size(); i++) {
                    categoriesList.add(reactCategoriesArray.getString(i));
                }
                pushConfig.setCategories(categoriesList.toArray(new String[]{}));
            }

            PushRegistrar registrar = pushConfig.asRegistrar();


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
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    private boolean verifyPrefs() {

        return prefs.contains("pushServerURL") && prefs.contains("variantSecret") && prefs.contains("variantID") && prefs.contains("senderID");
    }

}
