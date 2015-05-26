package org.ihsan.android.nolineadmin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Ihsan on 15/2/3.
 */
public class User implements Serializable {
    private int mNumber;
    private String mToken;
    private int mState;

    public User(){

    }

    public User(JSONObject jsonObject) throws JSONException {
        mNumber = jsonObject.getInt("number");
        mToken = jsonObject.getString("token");
        mState = jsonObject.getInt("state");
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public String getState() {
        switch (mState){
            case 0:
                return "排队中";
            case 1:
                return "已进店";
            case 2:
                return "已过号";
            case 3:
                return "已退出队列";
        }
        return null;
    }

    public void setState(int state) {
        mState = state;
    }
}
