/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.DisconnectCause;
import android.telephony.PreciseDisconnectCause;

/**
 * Contains precise call state and call fail causes generated by the
 * framework and the RIL.
 *
 * The following call information is included in returned PreciseCallState:
 *
 * <ul>
 *   <li>Ringing call state.
 *   <li>Foreground call state.
 *   <li>Background call state.
 *   <li>Disconnect cause; generated by the framework.
 *   <li>Precise disconnect cause; generated by the RIL.
 * </ul>
 *
 * @hide
 */
public class PreciseCallState implements Parcelable {

    /** Call state is not valid (Not received a call state). */
    public static final int PRECISE_CALL_STATE_NOT_VALID =      -1;
    /** Call state: No activity. */
    public static final int PRECISE_CALL_STATE_IDLE =           0;
    /** Call state: Active. */
    public static final int PRECISE_CALL_STATE_ACTIVE =         1;
    /** Call state: On hold. */
    public static final int PRECISE_CALL_STATE_HOLDING =        2;
    /** Call state: Dialing. */
    public static final int PRECISE_CALL_STATE_DIALING =        3;
    /** Call state: Alerting. */
    public static final int PRECISE_CALL_STATE_ALERTING =       4;
    /** Call state: Incoming. */
    public static final int PRECISE_CALL_STATE_INCOMING =       5;
    /** Call state: Waiting. */
    public static final int PRECISE_CALL_STATE_WAITING =        6;
    /** Call state: Disconnected. */
    public static final int PRECISE_CALL_STATE_DISCONNECTED =   7;
    /** Call state: Disconnecting. */
    public static final int PRECISE_CALL_STATE_DISCONNECTING =  8;

    private int mRingingCallState = PRECISE_CALL_STATE_NOT_VALID;
    private int mForegroundCallState = PRECISE_CALL_STATE_NOT_VALID;
    private int mBackgroundCallState = PRECISE_CALL_STATE_NOT_VALID;
    private int mDisconnectCause = DisconnectCause.NOT_VALID;
    private int mPreciseDisconnectCause = PreciseDisconnectCause.NOT_VALID;

    /**
     * Constructor
     *
     * @hide
     */
    public PreciseCallState(int ringingCall, int foregroundCall, int backgroundCall,
            int disconnectCause, int preciseDisconnectCause) {
        mRingingCallState = ringingCall;
        mForegroundCallState = foregroundCall;
        mBackgroundCallState = backgroundCall;
        mDisconnectCause = disconnectCause;
        mPreciseDisconnectCause = preciseDisconnectCause;
    }

    /**
     * Empty Constructor
     *
     * @hide
     */
    public PreciseCallState() {
    }

    /**
     * Construct a PreciseCallState object from the given parcel.
     */
    private PreciseCallState(Parcel in) {
        mRingingCallState = in.readInt();
        mForegroundCallState = in.readInt();
        mBackgroundCallState = in.readInt();
        mDisconnectCause = in.readInt();
        mPreciseDisconnectCause = in.readInt();
    }

    /**
     * Get precise ringing call state
     *
     * @see PreciseCallState#PRECISE_CALL_STATE_NOT_VALID
     * @see PreciseCallState#PRECISE_CALL_STATE_IDLE
     * @see PreciseCallState#PRECISE_CALL_STATE_ACTIVE
     * @see PreciseCallState#PRECISE_CALL_STATE_HOLDING
     * @see PreciseCallState#PRECISE_CALL_STATE_DIALING
     * @see PreciseCallState#PRECISE_CALL_STATE_ALERTING
     * @see PreciseCallState#PRECISE_CALL_STATE_INCOMING
     * @see PreciseCallState#PRECISE_CALL_STATE_WAITING
     * @see PreciseCallState#PRECISE_CALL_STATE_DISCONNECTED
     * @see PreciseCallState#PRECISE_CALL_STATE_DISCONNECTING
     */
    public int getRingingCallState() {
        return mRingingCallState;
    }

    /**
     * Get precise foreground call state
     *
     * @see PreciseCallState#PRECISE_CALL_STATE_NOT_VALID
     * @see PreciseCallState#PRECISE_CALL_STATE_IDLE
     * @see PreciseCallState#PRECISE_CALL_STATE_ACTIVE
     * @see PreciseCallState#PRECISE_CALL_STATE_HOLDING
     * @see PreciseCallState#PRECISE_CALL_STATE_DIALING
     * @see PreciseCallState#PRECISE_CALL_STATE_ALERTING
     * @see PreciseCallState#PRECISE_CALL_STATE_INCOMING
     * @see PreciseCallState#PRECISE_CALL_STATE_WAITING
     * @see PreciseCallState#PRECISE_CALL_STATE_DISCONNECTED
     * @see PreciseCallState#PRECISE_CALL_STATE_DISCONNECTING
     */
    public int getForegroundCallState() {
        return mForegroundCallState;
    }

    /**
     * Get precise background call state
     *
     * @see PreciseCallState#PRECISE_CALL_STATE_NOT_VALID
     * @see PreciseCallState#PRECISE_CALL_STATE_IDLE
     * @see PreciseCallState#PRECISE_CALL_STATE_ACTIVE
     * @see PreciseCallState#PRECISE_CALL_STATE_HOLDING
     * @see PreciseCallState#PRECISE_CALL_STATE_DIALING
     * @see PreciseCallState#PRECISE_CALL_STATE_ALERTING
     * @see PreciseCallState#PRECISE_CALL_STATE_INCOMING
     * @see PreciseCallState#PRECISE_CALL_STATE_WAITING
     * @see PreciseCallState#PRECISE_CALL_STATE_DISCONNECTED
     * @see PreciseCallState#PRECISE_CALL_STATE_DISCONNECTING
     */
    public int getBackgroundCallState() {
        return mBackgroundCallState;
    }

    /**
     * Get disconnect cause generated by the framework
     *
     * @see DisconnectCause#NOT_VALID
     * @see DisconnectCause#NOT_DISCONNECTED
     * @see DisconnectCause#INCOMING_MISSED
     * @see DisconnectCause#NORMAL
     * @see DisconnectCause#LOCAL
     * @see DisconnectCause#BUSY
     * @see DisconnectCause#CONGESTION
     * @see DisconnectCause#MMI
     * @see DisconnectCause#INVALID_NUMBER
     * @see DisconnectCause#NUMBER_UNREACHABLE
     * @see DisconnectCause#SERVER_UNREACHABLE
     * @see DisconnectCause#INVALID_CREDENTIALS
     * @see DisconnectCause#OUT_OF_NETWORK
     * @see DisconnectCause#SERVER_ERROR
     * @see DisconnectCause#TIMED_OUT
     * @see DisconnectCause#LOST_SIGNAL
     * @see DisconnectCause#LIMIT_EXCEEDED
     * @see DisconnectCause#INCOMING_REJECTED
     * @see DisconnectCause#POWER_OFF
     * @see DisconnectCause#OUT_OF_SERVICE
     * @see DisconnectCause#ICC_ERROR
     * @see DisconnectCause#CALL_BARRED
     * @see DisconnectCause#FDN_BLOCKED
     * @see DisconnectCause#CS_RESTRICTED
     * @see DisconnectCause#CS_RESTRICTED_NORMAL
     * @see DisconnectCause#CS_RESTRICTED_EMERGENCY
     * @see DisconnectCause#UNOBTAINABLE_NUMBER
     * @see DisconnectCause#CDMA_LOCKED_UNTIL_POWER_CYCLE
     * @see DisconnectCause#CDMA_DROP
     * @see DisconnectCause#CDMA_INTERCEPT
     * @see DisconnectCause#CDMA_REORDER
     * @see DisconnectCause#CDMA_SO_REJECT
     * @see DisconnectCause#CDMA_RETRY_ORDER
     * @see DisconnectCause#CDMA_ACCESS_FAILURE
     * @see DisconnectCause#CDMA_PREEMPTED
     * @see DisconnectCause#CDMA_NOT_EMERGENCY
     * @see DisconnectCause#CDMA_ACCESS_BLOCKED
     * @see DisconnectCause#ERROR_UNSPECIFIED
     */
    public int getDisconnectCause() {
        return mDisconnectCause;
    }

    /**
     * Get disconnect cause generated by the RIL
     *
     * @see PreciseDisconnectCause#NOT_VALID
     * @see PreciseDisconnectCause#NO_DISCONNECT_CAUSE_AVAILABLE
     * @see PreciseDisconnectCause#UNOBTAINABLE_NUMBER
     * @see PreciseDisconnectCause#NORMAL
     * @see PreciseDisconnectCause#BUSY
     * @see PreciseDisconnectCause#NUMBER_CHANGED
     * @see PreciseDisconnectCause#STATUS_ENQUIRY
     * @see PreciseDisconnectCause#NORMAL_UNSPECIFIED
     * @see PreciseDisconnectCause#NO_CIRCUIT_AVAIL
     * @see PreciseDisconnectCause#TEMPORARY_FAILURE
     * @see PreciseDisconnectCause#SWITCHING_CONGESTION
     * @see PreciseDisconnectCause#CHANNEL_NOT_AVAIL
     * @see PreciseDisconnectCause#QOS_NOT_AVAIL
     * @see PreciseDisconnectCause#BEARER_NOT_AVAIL
     * @see PreciseDisconnectCause#ACM_LIMIT_EXCEEDED
     * @see PreciseDisconnectCause#CALL_BARRED
     * @see PreciseDisconnectCause#FDN_BLOCKED
     * @see PreciseDisconnectCause#IMSI_UNKNOWN_IN_VLR
     * @see PreciseDisconnectCause#IMEI_NOT_ACCEPTED
     * @see PreciseDisconnectCause#CDMA_LOCKED_UNTIL_POWER_CYCLE
     * @see PreciseDisconnectCause#CDMA_DROP
     * @see PreciseDisconnectCause#CDMA_INTERCEPT
     * @see PreciseDisconnectCause#CDMA_REORDER
     * @see PreciseDisconnectCause#CDMA_SO_REJECT
     * @see PreciseDisconnectCause#CDMA_RETRY_ORDER
     * @see PreciseDisconnectCause#CDMA_ACCESS_FAILURE
     * @see PreciseDisconnectCause#CDMA_PREEMPTED
     * @see PreciseDisconnectCause#CDMA_NOT_EMERGENCY
     * @see PreciseDisconnectCause#CDMA_ACCESS_BLOCKED
     * @see PreciseDisconnectCause#ERROR_UNSPECIFIED
     */
    public int getPreciseDisconnectCause() {
        return mPreciseDisconnectCause;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mRingingCallState);
        out.writeInt(mForegroundCallState);
        out.writeInt(mBackgroundCallState);
        out.writeInt(mDisconnectCause);
        out.writeInt(mPreciseDisconnectCause);
    }

    public static final Creator<PreciseCallState> CREATOR
            = new Creator<PreciseCallState>() {

        public PreciseCallState createFromParcel(Parcel in) {
            return new PreciseCallState(in);
        }

        public PreciseCallState[] newArray(int size) {
            return new PreciseCallState[size];
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mRingingCallState;
        result = prime * result + mForegroundCallState;
        result = prime * result + mBackgroundCallState;
        result = prime * result + mDisconnectCause;
        result = prime * result + mPreciseDisconnectCause;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PreciseCallState other = (PreciseCallState) obj;
        return (mRingingCallState != other.mRingingCallState &&
            mForegroundCallState != other.mForegroundCallState &&
            mBackgroundCallState != other.mBackgroundCallState &&
            mDisconnectCause != other.mDisconnectCause &&
            mPreciseDisconnectCause != other.mPreciseDisconnectCause);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("Ringing call state: " + mRingingCallState);
        sb.append(", Foreground call state: " + mForegroundCallState);
        sb.append(", Background call state: " + mBackgroundCallState);
        sb.append(", Disconnect cause: " + mDisconnectCause);
        sb.append(", Precise disconnect cause: " + mPreciseDisconnectCause);

        return sb.toString();
    }
}
