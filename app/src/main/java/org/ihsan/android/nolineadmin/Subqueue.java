package org.ihsan.android.nolineadmin;

import java.util.ArrayList;

/**
 * Created by Ihsan on 15/4/17.
 */
public class Subqueue {
    private String mName;
    private int mSize;
    private int mTotal;
    private int mFirstNumber;
    private ArrayList<User> mUsers;
    private boolean mIsFresh;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int total) {
        mTotal = total;
    }

    public int getFirstNumber() {
        return mFirstNumber;
    }

    public void setFirstNumber(int firstNumber) {
        mFirstNumber = firstNumber;
    }

    public ArrayList<User> getUsers() {
        return mUsers;
    }

    public void setUsers(ArrayList<User> users) {
        mUsers = users;
    }

    public boolean isFresh() {
        return mIsFresh;
    }

    public void setIsFresh(boolean isFresh) {
        mIsFresh = isFresh;
    }
}
