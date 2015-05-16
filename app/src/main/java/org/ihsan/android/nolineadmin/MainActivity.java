package org.ihsan.android.nolineadmin;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.OnCheckedChangeListener;

/**
 * Created by Ihsan on 15/3/23.
 */
public class MainActivity extends SingleFragmentActivity {

    private ToggleQueueStateTask mToggleQueueStateTask;

    @Override
    protected Fragment createFragment() {
        return new QueueFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activateToolbar();

        AccountHeader.Result headerResult = new AccountHeader()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_header)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .build();


        boolean isOpen = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.logined_queue_state), false);

        Drawer.Result result = new Drawer()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("队列管理").withIcon(R.drawable
                                .ic_format_list_numbered_grey600_24dp).withIconTinted(true),
                        new PrimaryDrawerItem().withName("统计图表").withIcon(R.drawable
                                .ic_insert_chart_grey600_24dp).withIconTinted(true),
                        new DividerDrawerItem(),
                        new SwitchDrawerItem().withName("可排队").withChecked(isOpen)
                                .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(IDrawerItem iDrawerItem,
                                                                 CompoundButton compoundButton,
                                                                 boolean b) {
                                        if (mToggleQueueStateTask == null) {
                                            mToggleQueueStateTask = new ToggleQueueStateTask
                                                    (compoundButton);
                                            mToggleQueueStateTask.execute(!b);
                                        }
                                    }
                                }),
                        new SecondaryDrawerItem().withName("注销")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long
                            id, IDrawerItem drawerItem) {
                        FragmentManager fragmentManager = getFragmentManager();
                        Fragment fragment;
                        switch (position) {
                            case 0:
                                fragment = new QueueFragment();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainer, fragment)
                                        .commit();
                                break;
                            case 1:
                                fragment = new ChartFragment();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainer, fragment)
                                        .commit();
                                break;
                            case 3:
                                break;
                            case 4:
                                PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                                        .edit()
                                        .remove(getString(R.string.logined_queue_id))
                                        .commit();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                        }
                    }
                })
                .build();
    }

    private class ToggleQueueStateTask extends AsyncTask<Boolean, Void, Boolean> {

        private CompoundButton mCompoundButton;

        public ToggleQueueStateTask(CompoundButton compoundButton) {
            mCompoundButton = compoundButton;
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                    .getInt(getString(R.string.logined_queue_id), -1);
            return new DataFetcher(MainActivity.this).fetchToggleQueueStateResult(queueId,
                    params[0]);
        }

        @Override
        protected void onPostExecute(Boolean aboolean) {
            {
                if (aboolean) {
                    SharedPreferences defaultSharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this);
                    boolean isOpen = defaultSharedPreferences
                            .getBoolean(getString(R.string.logined_queue_state), false);
                    defaultSharedPreferences
                            .edit()
                            .putBoolean(getString(R.string.logined_queue_state), !isOpen)
                            .apply();
                } else {
                    if (mCompoundButton.isChecked()) {
                        mCompoundButton.setChecked(false);
                    } else {
                        mCompoundButton.setChecked(true);
                    }
                    Toast.makeText(MainActivity.this, "操作失败，请重试", Toast.LENGTH_LONG).show();
                }
            }
            mToggleQueueStateTask = null;
        }
    }
}

