
package org.aerogear.unifiedpush.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

public class ReactNativeMessageService extends HeadlessJsTaskService {

    /**
     * Background operations in android have up to 20 seconds to complete, we'll
     * allow React native 5 seconds of that to do its stuff.
     */
    private static final long ANDROID_BACKGROUND_TIMEOUT_MS = 15_000;

    @Nullable
    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            return new HeadlessJsTaskConfig(
                    "onBackgroundMessage",
                    Arguments.fromBundle(extras),
                    ANDROID_BACKGROUND_TIMEOUT_MS,
                    false
            );
        }
        return null;
    }
}
