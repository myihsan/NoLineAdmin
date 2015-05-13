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

    public User(){

    }

    public User(JSONObject jsonObject) throws JSONException {
        mNumber = Integer.valueOf(jsonObject.getString("number"));
        mToken = jsonObject.getString("token");
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
}
