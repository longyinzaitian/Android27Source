/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony;

import static android.hardware.radio.V1_0.DeviceStateType.CHARGING_STATE;
import static android.hardware.radio.V1_0.DeviceStateType.LOW_DATA_EXPECTED;
import static android.hardware.radio.V1_0.DeviceStateType.POWER_SAVE_MODE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.radio.V1_0.IndicationFilter;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.Rlog;
import android.util.LocalLog;
import android.view.Display;

import com.android.internal.util.IndentingPrintWriter;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * The device state monitor monitors the device state such as charging state, power saving sate,
 * and then passes down the information to the radio modem for the modem to perform its own
 * proprietary power saving strategy. Device state monitor also turns off the unsolicited
 * response from the modem when the device does not need to receive it, for example, device's
 * screen is off and does not have activities like tethering, remote display, etc...This effectively
 * prevents the CPU from waking up by those unnecessary unsolicited responses such as signal
 * strength update.
 */
public class DeviceStateMonitor extends Handler {
    protected static final boolean DBG = false;      /* STOPSHIP if true */
    protected static final String TAG = DeviceStateMonitor.class.getSimpleName();

    private static final int EVENT_RIL_CONNECTED                = 0;
    private static final int EVENT_SCREEN_STATE_CHANGED         = 1;
    private static final int EVENT_POWER_SAVE_MODE_CHANGED      = 2;
    private static final int EVENT_CHARGING_STATE_CHANGED       = 3;
    private static final int EVENT_TETHERING_STATE_CHANGED      = 4;

    private final Phone mPhone;

    private final LocalLog mLocalLog = new LocalLog(100);

    /**
     * Flag for wifi/usb/bluetooth tethering turned on or not
     */
    private boolean mIsTetheringOn;

    /**
     * Screen state provided by Display Manager. True indicates one of the screen is on, otherwise
     * all off.
     */
    private boolean mIsScreenOn;

    /**
     * Indicating the device is plugged in and is supplying sufficient power that the battery level
     * is going up (or the battery is fully charged). See BatteryManager.isCharging() for the
     * details
     */
    private boolean mIsCharging;

    /**
     * Flag for device power save mode. See PowerManager.isPowerSaveMode() for the details.
     * Note that it is not possible both mIsCharging and mIsPowerSaveOn are true at the same time.
     * The system will automatically end power save mode when the device starts charging.
     */
    private boolean mIsPowerSaveOn;

    /**
     * Low data expected mode. True indicates low data traffic is expected, for example, when the
     * device is idle (e.g. screen is off and not doing tethering in the background). Note this
     * doesn't mean no data is expected.
     */
    private boolean mIsLowDataExpected;

    /**
     * The unsolicited response filter. See IndicationFilter defined in types.hal for the definition
     * of each bit.
     */
    private int mUnsolicitedResponseFilter = IndicationFilter.ALL;

    private final DisplayManager.DisplayListener mDisplayListener =
            new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) { }

                @Override
                public void onDisplayRemoved(int displayId) { }

                @Override
                public void onDisplayChanged(int displayId) {
                    boolean screenOn = isScreenOn();
                    Message msg = obtainMessage(EVENT_SCREEN_STATE_CHANGED);
                    msg.arg1 = screenOn ? 1 : 0;
                    sendMessage(msg);
                }
            };

    /**
     * Device state broadcast receiver
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("received: " + intent, true);

            Message msg;
            switch (intent.getAction()) {
                case PowerManager.ACTION_POWER_SAVE_MODE_CHANGED:
                    msg = obtainMessage(EVENT_POWER_SAVE_MODE_CHANGED);
                    msg.arg1 = isPowerSaveModeOn() ? 1 : 0;
                    log("Power Save mode " + ((msg.arg1 == 1) ? "on" : "off"), true);
                    break;
                case BatteryManager.ACTION_CHARGING:
                    msg = obtainMessage(EVENT_CHARGING_STATE_CHANGED);
                    msg.arg1 = 1;   // charging
                    break;
                case BatteryManager.ACTION_DISCHARGING:
                    msg = obtainMessage(EVENT_CHARGING_STATE_CHANGED);
                    msg.arg1 = 0;   // not charging
                    break;
                case ConnectivityManager.ACTION_TETHER_STATE_CHANGED:
                    ArrayList<String> activeTetherIfaces = intent.getStringArrayListExtra(
                            ConnectivityManager.EXTRA_ACTIVE_TETHER);

                    boolean isTetheringOn = activeTetherIfaces != null
                            && activeTetherIfaces.size() > 0;
                    log("Tethering " + (isTetheringOn ? "on" : "off"), true);
                    msg = obtainMessage(EVENT_TETHERING_STATE_CHANGED);
                    msg.arg1 = isTetheringOn ? 1 : 0;
                    break;
                default:
                    log("Unexpected broadcast intent: " + intent, false);
                    return;
            }
            sendMessage(msg);
        }
    };

    /**
     * Device state monitor constructor. Note that each phone object should have its own device
     * state monitor, meaning there will be two device monitors on the multi-sim device.
     *
     * @param phone Phone object
     */
    public DeviceStateMonitor(Phone phone) {
        mPhone = phone;
        DisplayManager dm = (DisplayManager) phone.getContext().getSystemService(
                Context.DISPLAY_SERVICE);
        dm.registerDisplayListener(mDisplayListener, null);

        mIsPowerSaveOn = isPowerSaveModeOn();
        mIsCharging = isDeviceCharging();
        mIsScreenOn = isScreenOn();
        // Assuming tethering is always off after boot up.
        mIsTetheringOn = false;
        mIsLowDataExpected = false;

        log("DeviceStateMonitor mIsPowerSaveOn=" + mIsPowerSaveOn + ",mIsScreenOn="
                + mIsScreenOn + ",mIsCharging=" + mIsCharging, false);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        filter.addAction(BatteryManager.ACTION_CHARGING);
        filter.addAction(BatteryManager.ACTION_DISCHARGING);
        filter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        mPhone.getContext().registerReceiver(mBroadcastReceiver, filter, null, mPhone);

        mPhone.mCi.registerForRilConnected(this, EVENT_RIL_CONNECTED, null);
    }

    /**
     * @return True if low data is expected
     */
    private boolean isLowDataExpected() {
        return mIsPowerSaveOn || (!mIsCharging && !mIsTetheringOn && !mIsScreenOn);
    }

    /**
     * @return True if signal strength update should be turned off.
     */
    private boolean shouldTurnOffSignalStrength() {
        return mIsPowerSaveOn || (!mIsCharging && !mIsScreenOn);
    }

    /**
     * @return True if full network update should be turned off. Only significant changes will
     * trigger the network update unsolicited response.
     */
    private boolean shouldTurnOffFullNetworkUpdate() {
        return mIsPowerSaveOn || (!mIsCharging && !mIsScreenOn && !mIsTetheringOn);
    }

    /**
     * @return True if data dormancy status update should be turned off.
     */
    private boolean shouldTurnOffDormancyUpdate() {
        return mIsPowerSaveOn || (!mIsCharging && !mIsTetheringOn && !mIsScreenOn);
    }

    /**
     * Message handler
     *
     * @param msg The message
     */
    @Override
    public void handleMessage(Message msg) {
        log("handleMessage msg=" + msg, false);
        switch (msg.what) {
            case EVENT_RIL_CONNECTED:
                onRilConnected();
                break;
            default:
                updateDeviceState(msg.what, msg.arg1 != 0);
        }
    }

    /**
     * Update the device and send the information to the modem.
     *
     * @param eventType Device state event type
     * @param state True if enabled/on, otherwise disabled/off.
     */
    private void updateDeviceState(int eventType, boolean state) {
        switch (eventType) {
            case EVENT_SCREEN_STATE_CHANGED:
                if (mIsScreenOn == state) return;
                mIsScreenOn = state;
                break;
            case EVENT_CHARGING_STATE_CHANGED:
                if (mIsCharging == state) return;
                mIsCharging = state;
                sendDeviceState(CHARGING_STATE, mIsCharging);
                break;
            case EVENT_TETHERING_STATE_CHANGED:
                if (mIsTetheringOn == state) return;
                mIsTetheringOn = state;
                break;
            case EVENT_POWER_SAVE_MODE_CHANGED:
                if (mIsPowerSaveOn == state) return;
                mIsPowerSaveOn = state;
                sendDeviceState(POWER_SAVE_MODE, mIsPowerSaveOn);
                break;
            default:
                return;
        }

        if (mIsLowDataExpected != isLowDataExpected()) {
            mIsLowDataExpected = !mIsLowDataExpected;
            sendDeviceState(LOW_DATA_EXPECTED, mIsLowDataExpected);
        }

        int newFilter = 0;
        if (!shouldTurnOffSignalStrength()) {
            newFilter |= IndicationFilter.SIGNAL_STRENGTH;
        }

        if (!shouldTurnOffFullNetworkUpdate()) {
            newFilter |= IndicationFilter.FULL_NETWORK_STATE;
        }

        if (!shouldTurnOffDormancyUpdate()) {
            newFilter |= IndicationFilter.DATA_CALL_DORMANCY_CHANGED;
        }

        setUnsolResponseFilter(newFilter, false);
    }

    /**
     * Called when RIL is connected during boot up or reconnected after modem restart.
     *
     * When modem crashes, if the user turns the screen off before RIL reconnects, device
     * state and filter cannot be sent to modem. Resend the state here so that modem
     * has the correct state (to stop signal strength reporting, etc).
     */
    private void onRilConnected() {
        log("RIL connected.", true);
        sendDeviceState(CHARGING_STATE, mIsCharging);
        sendDeviceState(LOW_DATA_EXPECTED, mIsLowDataExpected);
        sendDeviceState(POWER_SAVE_MODE, mIsPowerSaveOn);
        setUnsolResponseFilter(mUnsolicitedResponseFilter, true);
    }

    /**
     * Convert the device state type into string
     *
     * @param type Device state type
     * @return The converted string
     */
    private String deviceTypeToString(int type) {
        switch (type) {
            case CHARGING_STATE: return "CHARGING_STATE";
            case LOW_DATA_EXPECTED: return "LOW_DATA_EXPECTED";
            case POWER_SAVE_MODE: return "POWER_SAVE_MODE";
            default: return "UNKNOWN";
        }
    }

    /**
     * Send the device state to the modem.
     *
     * @param type Device state type. See DeviceStateType defined in types.hal.
     * @param state True if enabled/on, otherwise disabled/off
     */
    private void sendDeviceState(int type, boolean state) {
        log("send type: " + deviceTypeToString(type) + ", state=" + state, true);
        mPhone.mCi.sendDeviceState(type, state, null);
    }

    /**
     * Turn on/off the unsolicited response from the modem.
     *
     * @param newFilter See UnsolicitedResponseFilter in types.hal for the definition of each bit.
     * @param force Always set the filter when true.
     */
    private void setUnsolResponseFilter(int newFilter, boolean force) {
        if (force || newFilter != mUnsolicitedResponseFilter) {
            log("old filter: " + mUnsolicitedResponseFilter + ", new filter: " + newFilter, true);
            mPhone.mCi.setUnsolResponseFilter(newFilter, null);
            mUnsolicitedResponseFilter = newFilter;
        }
    }

    /**
     * @return True if the device is currently in power save mode.
     * See {@link BatteryManager#isPowerSaveMode BatteryManager.isPowerSaveMode()}.
     */
    private boolean isPowerSaveModeOn() {
        final PowerManager pm = (PowerManager) mPhone.getContext().getSystemService(
                Context.POWER_SERVICE);
        return pm.isPowerSaveMode();
    }

    /**
     * @return Return true if the battery is currently considered to be charging. This means that
     * the device is plugged in and is supplying sufficient power that the battery level is
     * going up (or the battery is fully charged).
     * See {@link BatteryManager#isCharging BatteryManager.isCharging()}.
     */
    private boolean isDeviceCharging() {
        final BatteryManager bm = (BatteryManager) mPhone.getContext().getSystemService(
                Context.BATTERY_SERVICE);
        return bm.isCharging();
    }

    /**
     * @return True if one the device's screen (e.g. main screen, wifi display, HDMI display, or
     *         Android auto, etc...) is on.
     */
    private boolean isScreenOn() {
        // Note that we don't listen to Intent.SCREEN_ON and Intent.SCREEN_OFF because they are no
        // longer adequate for monitoring the screen state since they are not sent in cases where
        // the screen is turned off transiently such as due to the proximity sensor.
        final DisplayManager dm = (DisplayManager) mPhone.getContext().getSystemService(
                Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();

        if (displays != null) {
            for (Display display : displays) {
                // Anything other than STATE_ON is treated as screen off, such as STATE_DOZE,
                // STATE_DOZE_SUSPEND, etc...
                if (display.getState() == Display.STATE_ON) {
                    log("Screen " + Display.typeToString(display.getType()) + " on", true);
                    return true;
                }
            }
            log("Screens all off", true);
            return false;
        }

        log("No displays found", true);
        return false;
    }

    /**
     * @param msg Debug message
     * @param logIntoLocalLog True if log into the local log
     */
    private void log(String msg, boolean logIntoLocalLog) {
        if (DBG) Rlog.d(TAG, msg);
        if (logIntoLocalLog) {
            mLocalLog.log(msg);
        }
    }

    /**
     * Print the DeviceStateMonitor into the given stream.
     *
     * @param fd The raw file descriptor that the dump is being sent to.
     * @param pw A PrintWriter to which the dump is to be set.
     * @param args Additional arguments to the dump request.
     */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        final IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.increaseIndent();
        ipw.println("mIsTetheringOn=" + mIsTetheringOn);
        ipw.println("mIsScreenOn=" + mIsScreenOn);
        ipw.println("mIsCharging=" + mIsCharging);
        ipw.println("mIsPowerSaveOn=" + mIsPowerSaveOn);
        ipw.println("mIsLowDataExpected=" + mIsLowDataExpected);
        ipw.println("mUnsolicitedResponseFilter=" + mUnsolicitedResponseFilter);
        ipw.println("Local logs:");
        ipw.increaseIndent();
        mLocalLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.decreaseIndent();
        ipw.flush();
    }
}
