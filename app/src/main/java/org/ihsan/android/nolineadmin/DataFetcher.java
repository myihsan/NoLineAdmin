package org.ihsan.android.nolineadmin;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Ihsan on 15/1/23.
 */
public class DataFetcher {
    private static final String TAG = "DataFetcher";
    private Context mContext;

    public DataFetcher(Context context) {
        mContext = context;
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<Subqueue> fetchQueueDetail(int queueId) {
        String fetchUrl = mContext.getString(R.string.root_url) + "getqueue.php";
        String url = Uri.parse(fetchUrl).buildUpon()
                .appendQueryParameter("queueId", String.valueOf(queueId))
                .build().toString();
        String result = null;
        ArrayList<Subqueue> subqueues = new ArrayList<Subqueue>();
        try {
            try {
                result = getUrl(url);
                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .edit()
                        .putString(mContext.getString(R.string.queue_detail_cache), result)
                        .commit();
                parseQueueDetail(subqueues, result);
                subqueues.get(0).setIsFresh(true);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch URL: ", ioe);
                result = PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString(mContext.getString(R.string.queue_detail_cache), null);
                if (result != null) {
                    parseQueueDetail(subqueues, result);
                }
            }
        } catch (JSONException jsone) {
            Log.e(TAG, "Failed to parse result", jsone);
            return null;
        }
        return subqueues;
    }

    public void parseQueueDetail(ArrayList<Subqueue> subqueues, String jsonString) throws
            JSONException {
        JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("queueDetail");
        JSONArray subqueueNames = jsonObject.getJSONArray("subqueueNames");
        JSONArray subqueueSizes = jsonObject.getJSONArray("subqueueSizes");
        JSONArray subqueueTotals = jsonObject.getJSONArray("subqueueTotals");
        JSONArray subqueueFirstNumbers = jsonObject.getJSONArray("subqueueFirstNumbers");
        JSONArray subqueueUserArrays = jsonObject.getJSONArray("subqueueUserArrays");
        for (int i = 0; i < subqueueNames.length(); i++) {
            Subqueue subqueue = new Subqueue();
            subqueue.setName(subqueueNames.getString(i));
            subqueue.setSize(subqueueSizes.getInt(i));
            subqueue.setTotal(subqueueTotals.getInt(i));
            subqueue.setFirstNumber(subqueueFirstNumbers.getInt(i));
            JSONArray userArray = subqueueUserArrays.getJSONArray(i);
            ArrayList<User> users = new ArrayList<User>();
            for (int j = 0; j < userArray.length(); j++) {
                User user = new User(userArray.getJSONObject(j));
                users.add(user);
            }
            subqueue.setUsers(users);
            subqueues.add(subqueue);
        }
    }

    public ArrayList<User> fetchUser(int queueId, int subqueueNumber) {
        return fetchUser(queueId, subqueueNumber, -1);
    }

    public ArrayList<User> fetchUser(int queueId, int subqueueNumber, int state) {
        String fetchUrl = mContext.getString(R.string.root_url) + "getsubqueuedetail.php";
        String url = Uri.parse(fetchUrl).buildUpon()
                .appendQueryParameter("queueId", String.valueOf(queueId))
                .appendQueryParameter("subqueueNumber", String.valueOf(subqueueNumber))
                .appendQueryParameter("state", String.valueOf(state))
                .build().toString();
        ArrayList<User> users = new ArrayList<User>();
        try {
            String result = getUrl(url);
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                User user = new User(jsonArray.getJSONObject(i));
                users.add(user);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
            return null;
        } catch (JSONException jsone) {
            Log.e(TAG, "Failed to parse result", jsone);
            return null;
        }
        return users;
    }

    public ArrayList<BarChartData> fetchBarChartData(int queueId, String beginDate, String
            endDate) {
        String fetchUrl = mContext.getString(R.string.root_url) + "getbarchartdata.php";
        String url = Uri.parse(fetchUrl).buildUpon()
                .appendQueryParameter("queueId", String.valueOf(queueId))
                .appendQueryParameter("beginDate", beginDate)
                .appendQueryParameter("endDate", endDate)
                .build().toString();
        ArrayList<BarChartData> dataArray = new ArrayList<BarChartData>();
        try {
            String result = getUrl(url);
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                BarChartData data = new BarChartData(jsonArray.getJSONObject(i));
                dataArray.add(data);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
            return null;
        } catch (JSONException jsone) {
            Log.e(TAG, "Failed to parse result", jsone);
            return null;
        }
        return dataArray;
    }

    public LoginResult fetchLoginResult(String username, String password) {
        LoginResult loginResult=null;
        String loginUrl = mContext.getString(R.string.root_url) + "login.php";
        String url = Uri.parse(loginUrl).buildUpon()
                .appendQueryParameter("username", username)
                .appendQueryParameter("password", password)
                .appendQueryParameter("identity", "admin")
                .build().toString();
        Log.d(TAG, url);

        String jsonString;
        try {
            jsonString = getUrl(url);
            JSONObject jsonObject = new JSONObject(jsonString);
            loginResult=new LoginResult(jsonObject);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
        } catch (JSONException jsone) {
            Log.e(TAG, "Failed to parse result", jsone);
        }
        return loginResult;
    }

    public boolean fetchNextQueuerResult(int queueId, int subqueueNumber, int state) {
        boolean flag = false;
        if (queueId == -1) {
            return false;
        }
        String fetchUrl = mContext.getString(R.string.root_url) + "nextqueuer.php";
        String url = Uri.parse(fetchUrl).buildUpon()
                .appendQueryParameter("queueId", String.valueOf(queueId))
                .appendQueryParameter("subqueueNumber", String.valueOf(subqueueNumber))
                .appendQueryParameter("state", String.valueOf(state))
                .build().toString();
        try {
            String result = getUrl(url);
            Log.d(TAG, result);
            if (result.equals("succeed")) {
                flag = true;
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
        }
        return flag;
    }

    public boolean fetchToggleQueueStateResult(int queueId, boolean isOpen) {
        boolean flag = false;
        String fetchUrl = mContext.getString(R.string.root_url) + "togglequeuestate.php";
        String url = Uri.parse(fetchUrl).buildUpon()
                .appendQueryParameter("queueId", String.valueOf(queueId))
                .appendQueryParameter("isOpen", String.valueOf(isOpen))
                .build().toString();
        Log.d(TAG, url);
        try {
            String result = getUrl(url);
            Log.d(TAG, result);
            if (result.equals("succeed")) {
                flag = true;
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
        }
        return flag;
    }

    public int fetchQueueUpResult(int queueId, int subqueueNumber) {
        String fetchUrl = mContext.getString(R.string.root_url) + "queueup.php";
        String url = Uri.parse(fetchUrl).buildUpon()
                .appendQueryParameter("queueId", String.valueOf(queueId))
                .appendQueryParameter("subqueueNumber", String.valueOf(subqueueNumber))
                .appendQueryParameter("token", "管理员添加")
                .appendQueryParameter("userId", String.valueOf(-1))
                .build().toString();
        try {
            String result = getUrl(url);
            JSONObject jsonObject=new JSONObject(result);
            return jsonObject.getInt("queuedNumber");
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
        } catch (JSONException jsone) {
            Log.e(TAG, "Failed to parse result", jsone);
        }
        return -1;
    }
}
