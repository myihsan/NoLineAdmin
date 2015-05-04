package org.ihsan.android.nolineadmin;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

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

        AccountHeader.Result headerResult = new AccountHeader()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_header)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .build();

        Drawer.Result result = new Drawer()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("队列管理").withIcon(R.drawable.ic_format_list_numbered_grey600_24dp),
                        new PrimaryDrawerItem().withName("统计图表").withIcon(R.drawable.ic_insert_chart_grey600_24dp),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("设置"),
                        new PrimaryDrawerItem().withName("注销")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
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
                                PreferenceManager.getDefaultSharedPreferences(QueueActivity.this)
                                        .edit()
                                        .remove(getString(R.string.logged_queue_id))
                                        .commit();
                                Intent intent = new Intent(QueueActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                        }
                    }
                })
                .build();
    }
}
