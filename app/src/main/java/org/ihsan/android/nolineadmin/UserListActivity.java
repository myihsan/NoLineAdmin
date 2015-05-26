package org.ihsan.android.nolineadmin;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ihsan on 15/2/4.
 */
public class UserListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        int number = getIntent().getIntExtra(UserListFragment.EXTRA_SUBQUEUE_NUMBER, -1);
        String name = getIntent().getStringExtra(UserListFragment.EXTRA_SUBQUEUE_NAME);
        ArrayList<User> users = (ArrayList<User>) getIntent().getSerializableExtra
                (UserListFragment.EXTRA_SUBQUEUE_USERS);
        return UserListFragment.newInstance(number, name, users);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activateToolbarWithHomeEnabled();
    }
}
