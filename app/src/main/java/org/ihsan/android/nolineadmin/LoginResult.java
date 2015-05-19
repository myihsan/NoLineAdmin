package org.ihsan.android.nolineadmin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ihsan on 15/5/15.
 */
public class LoginResult {
    private int mQueueId;
    private boolean mIsOpen;

    public LoginResult(JSONObject jsonObject) throws JSONException {
        mQueueId = Integer.valueOf(jsonObject.getString("id"));
        if (mQueueId!=-1) {
            mIsOpen = jsonObject.getBoolean("isOpen");
        }
    }

    public int getQueueId() {
        return mQueueId;
    }

    public void setQueueId(int queueId) {
        mQueueId = queueId;
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void setIsOpen(boolean isOpen) {
        mIsOpen = isOpen;
    }
}
