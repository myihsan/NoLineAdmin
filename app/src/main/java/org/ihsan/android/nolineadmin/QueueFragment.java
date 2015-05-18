package org.ihsan.android.nolineadmin;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import java.util.ArrayList;

/**
 * Created by Ihsan on 15/2/3.
 */
public class QueueFragment extends Fragment {
    private static final String TAG = "QueueFragment";

    private Activity mActivity;
    private Callbacks mCallbacks;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mSubqueueListView;

    private ArrayList<Subqueue> mSubqueues = new ArrayList<Subqueue>();

    public interface Callbacks {
        void onSubqueueTotalClick(int number, String name, ArrayList<User> users);

        void onDrawerCreate(ArrayList<Subqueue> subqueues);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity=activity;
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        getActivity().setTitle("队列管理");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.queue_swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetDetailTask().execute();
            }
        });
        mSubqueueListView = (ListView) view.findViewById(R.id.subqueue_list_view);
        mSubqueueListView.setAdapter(new SubqueueAdapter(mSubqueues));
        new GetDetailTask().execute();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_queue, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                String[] subqueueNames = new String[mSubqueues.size()];
                int i = 0;
                for (Subqueue subqueue : mSubqueues) {
                    subqueueNames[i] = (subqueue.getName());
                    i++;
                }
                new MaterialDialog.Builder(getActivity())
                        .title("选择子队列")
                        .items(subqueueNames)
                        .itemsCallbackSingleChoice(0, new MaterialDialog
                                .ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int
                                    which, CharSequence text) {
                                new QueueUpTask(text.toString()).execute(which);
                                return true;
                            }
                        })
                        .positiveText("添加")
                        .negativeText("取消")
                        .show();
                return true;
            case R.id.action_logout:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .remove(getString(R.string.logined_queue_id))
                        .commit();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetDetailTask extends AsyncTask<Void, Void, ArrayList<Subqueue>> {
        @Override
        protected ArrayList<Subqueue> doInBackground(Void... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logined_queue_id), -1);
            if (queueId != -1) {
                return new DataFetcher(getActivity()).fetchQueueDetail(queueId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Subqueue> subqueues) {
            if (subqueues != null) {
                mCallbacks.onDrawerCreate(subqueues);
                mSubqueues.clear();
                mSubqueues.addAll(subqueues);
                updateAdapter();
                if (!subqueues.get(0).isFresh()) {
                    Toast.makeText(getActivity(), "服务器连接故障，更新信息失败", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), "获取队列信息失败，请重试", Toast.LENGTH_LONG).show();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void updateAdapter() {
        ((SubqueueAdapter) mSubqueueListView.getAdapter()).notifyDataSetChanged();
    }

    private class NextQueuerTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logined_queue_id), -1);
            return new DataFetcher(getActivity()).fetchNextQueuerResult(queueId, params[0],
                    params[1]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                new GetDetailTask().execute();
            } else {
                Toast.makeText(getActivity(), "处理失败，请重试", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class QueueUpTask extends AsyncTask<Integer, Void, Integer> {

        private String mSubqueueName;

        public QueueUpTask(String subqueueName) {
            mSubqueueName = subqueueName;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logined_queue_id), -1);
            return new DataFetcher(getActivity()).fetchQueueUpResult(queueId, params[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {

            if (integer != -1) {
                Toast.makeText(getActivity(), "添加成功", Toast.LENGTH_LONG).show();
                new GetDetailTask().execute();
                new MaterialDialog.Builder(getActivity())
                        .title("添加结果")
                        .content(mSubqueueName + "：" + integer)
                        .positiveText("确定")
                        .show();
            } else {
                Toast.makeText(getActivity(), "加添失败，请重试", Toast.LENGTH_LONG).show();
            }
        }

    }

    private class SubqueueAdapter extends ArrayAdapter<Subqueue> {
        public SubqueueAdapter(ArrayList<Subqueue> subqueues) {
            super(getActivity(), 0, subqueues);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.subqueue_item, null);
            }
            convertView.setOnClickListener(null);

            Subqueue subqueue = getItem(position);
            final int index = position;
            final String name = subqueue.getName();
            final ArrayList<User> users = subqueue.getUsers();
            TextView subqueueNameTextView = (TextView) convertView.findViewById(R.id
                    .subqueue_name_textView);
            LinearLayout descriptionView = (LinearLayout) convertView.findViewById(R.id
                    .description_view);
            TextView subqueueTotalTextView = (TextView) convertView.findViewById(R.id
                    .subqueue_total_textView);
            TextView subqueueFirstNumberTextView = (TextView) convertView.findViewById(R
                    .id.subqueue_first_number_textView);
            TextView subqueueLeftButton = (TextView) convertView.findViewById(R.id
                    .subquque_left_button);
            TextView subqueueRightButton = (TextView) convertView.findViewById(R.id
                    .subquque_right_button);
            subqueueNameTextView.setText(name);
            subqueueTotalTextView.setText(String.valueOf(subqueue.getTotal()));
            descriptionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallbacks.onSubqueueTotalClick(index, name, users);
                }
            });
            subqueueFirstNumberTextView.setText(String.valueOf(subqueue.getFirstNumber()));
            subqueueLeftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new NextQueuerTask().execute(index, 2);
                }
            });
            subqueueRightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new NextQueuerTask().execute(index, 1);
                }
            });

            return convertView;
        }
    }
}
