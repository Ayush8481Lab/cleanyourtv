package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class HomeKeyService extends AccessibilityService {

    private long[] homeTapTimes = new long[3];
    private boolean isFailsafeActive = false;
    private long failsafeTriggerTime = 0;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence pkgName = event.getPackageName();
            CharSequence clsName = event.getClassName();

            if (pkgName != null && pkgName.toString().equals("com.amazon.tv.launcher")) {
                if (clsName != null && (clsName.toString().contains("Settings") || clsName.toString().contains("MainSettingsActivity"))) {
                    return; 
                }
                long currentTime = System.currentTimeMillis();
                if (isFailsafeActive && (currentTime - failsafeTriggerTime > 30000)) {
                    isFailsafeActive = false;
                }
                if (isFailsafeActive) return; 

                homeTapTimes[0] = homeTapTimes[1];
                homeTapTimes[1] = homeTapTimes[2];
                homeTapTimes[2] = currentTime;

                if (homeTapTimes[2] - homeTapTimes[0] < 1500) {
                    isFailsafeActive = true;
                    failsafeTriggerTime = currentTime;
                    return; 
                }

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
