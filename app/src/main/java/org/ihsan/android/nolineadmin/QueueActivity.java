package org.ihsan.android.nolineadmin;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Ihsan on 15/3/23.
 */
public class QueueActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new QueueFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activateToolbar();
    }
}
