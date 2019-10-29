package com.sungbin.reply.bot.api.PictureTransmission;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.graphics.Path;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityServiceManager extends AccessibilityService {

    public static AccessibilityServiceManager instance;

    public static AccessibilityServiceManager getInstance() {
        return instance;
    }

    @Override
    public void onServiceConnected() {
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @TargetApi(Build.VERSION_CODES.N)
    public void dispatch(int x, int y) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(x, y);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 10L, 10L));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }
}