package org.ihsan.android.nolineadmin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ihsan on 15/4/24.
 */
public class BarChartData {
    private String mName;
    private int mValue;

    public BarChartData(JSONObject jsonObject) throws JSONException {
        mName = jsonObject.getString("name");
        mValue = Integer.valueOf(jsonObject.getString("value"));
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }
}
