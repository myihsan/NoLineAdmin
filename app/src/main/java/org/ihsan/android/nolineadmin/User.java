package org.ihsan.android.nolineadmin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ihsan on 15/2/3.
 */
public class User {
    private int mNumber;
    private String mUserId;

    public User(JSONObject jsonQueuer) throws JSONException {
        mNumber = Integer.valueOf(jsonQueuer.getString("number"));
        mUserId = jsonQueuer.getString("userId");
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }
}
