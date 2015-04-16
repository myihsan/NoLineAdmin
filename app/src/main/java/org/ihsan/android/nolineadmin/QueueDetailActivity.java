package org.ihsan.android.nolineadmin;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Ihsan on 15/2/4.
 */
public class QueueDetailActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new QueueDetailFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activateToolbarWithHomeEnabled();
    }
}
