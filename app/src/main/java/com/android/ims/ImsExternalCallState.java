/*
 * Copyright (c) 2016 The Android Open Source Project
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

package com.android.ims;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.telecom.Log;
import android.telephony.Rlog;

/*
 * This file contains all the api's through which
 * information received in Dialog Event Package can be
 * queried
 */

/**
 * Parcelable object to handle MultiEndpoint Dialog Information
 * @hide
 */
public class ImsExternalCallState implements Parcelable {

    private static final String TAG = "ImsExternalCallState";

    // Dialog States
    public static final int CALL_STATE_CONFIRMED = 1;
    public static final int CALL_STATE_TERMINATED = 2;
    // Dialog Id
    private int mCallId;
    // Number
    private Uri mAddress;
    private boolean mIsPullable;
    // CALL_STATE_CONFIRMED / CALL_STATE_TERMINATED
    private int mCallState;
    // ImsCallProfile#CALL_TYPE_*
    private int mCallType;
    private boolean mIsHeld;

    public ImsExternalCallState() {
    }

    public ImsExternalCallState(int callId, Uri address, boolean isPullable, int callState,
            int callType, boolean isCallheld) {
        mCallId = callId;
        mAddress = address;
        mIsPullable = isPullable;
        mCallState = callState;
        mCallType = callType;
        mIsHeld = isCallheld;
        Rlog.d(TAG, "ImsExternalCallState = " + this);
    }

    public ImsExternalCallState(Parcel in) {
        mCallId = in.readInt();
        ClassLoader classLoader = ImsExternalCallState.class.getClassLoader();
        mAddress = in.readParcelable(classLoader);
        mIsPullable = (in.readInt() != 0);
        mCallState = in.readInt();
        mCallType = in.readInt();
        mIsHeld = (in.readInt() != 0);
        Rlog.d(TAG, "ImsExternalCallState const = " + this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mCallId);
        out.writeParcelable(mAddress, 0);
        out.writeInt(mIsPullable ? 1 : 0);
        out.writeInt(mCallState);
        out.writeInt(mCallType);
        out.writeInt(mIsHeld ? 1 : 0);
        Rlog.d(TAG, "ImsExternalCallState writeToParcel = " + out.toString());
    }

    public static final Creator<ImsExternalCallState> CREATOR =
            new Creator<ImsExternalCallState>() {
        @Override
        public ImsExternalCallState createFromParcel(Parcel in) {
            return new ImsExternalCallState(in);
        }

        @Override
        public ImsExternalCallState[] newArray(int size) {
            return new ImsExternalCallState[size];
        }
    };

    public int getCallId() {
        return mCallId;
    }

    public Uri getAddress() {
        return mAddress;
    }

    public boolean isCallPullable() {
        return mIsPullable;
    }

    public int getCallState() {
        return mCallState;
    }

    public int getCallType() {
        return mCallType;
    }

    public boolean isCallHeld() {
        return mIsHeld;
    }

    @Override
    public String toString() {
        return "ImsExternalCallState { mCallId = " + mCallId +
                ", mAddress = " + Log.pii(mAddress) +
                ", mIsPullable = " + mIsPullable +
                ", mCallState = " + mCallState +
                ", mCallType = " + mCallType +
                ", mIsHeld = " + mIsHeld + "}";
    }
}
