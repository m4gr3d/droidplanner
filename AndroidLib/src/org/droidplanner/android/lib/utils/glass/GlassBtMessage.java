package org.droidplanner.android.lib.utils.glass;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wrapper for the glass related messages sent over the bluetooth link.
 */
public class GlassBtMessage implements Parcelable {

    private final String mMessageAction;
    private final int mArg;
    private final String mMessage;
    private final Parcelable mPayload;

    public GlassBtMessage(String action, int arg, String message, Parcelable payload){
        mMessageAction = action;
        mArg = arg;
        mMessage = message;
        mPayload = payload;
    }

    public String getMessageAction() {
        return mMessageAction;
    }

    public int getArg() {
        return mArg;
    }

    public String getMessage(){
        return mMessage;
    }

    public Parcelable getPayload() {
        return mPayload;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mMessageAction);
        dest.writeInt(this.mArg);
        dest.writeString(this.mMessage);
        dest.writeParcelable(this.mPayload, 0);
    }

    private GlassBtMessage(Parcel in) {
        this.mMessageAction = in.readString();
        this.mArg = in.readInt();
        this.mMessage = in.readString();
        this.mPayload = in.readParcelable(Parcelable.class.getClassLoader());
    }

    public static final Parcelable.Creator<GlassBtMessage> CREATOR = new Parcelable.Creator<GlassBtMessage>() {
        public GlassBtMessage createFromParcel(Parcel source) {
            return new GlassBtMessage(source);
        }

        public GlassBtMessage[] newArray(int size) {
            return new GlassBtMessage[size];
        }
    };
}
