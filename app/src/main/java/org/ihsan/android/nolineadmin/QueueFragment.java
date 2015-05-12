package org.ihsan.android.nolineadmin;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;

/**
 * Created by Ihsan on 15/2/3.
 */
public class QueueFragment extends Fragment {
    private static final String TAG = "QueueFragment";

    private ListView mSubqueueListView;

    private ArrayList<Subqueue> mSubqueues = new ArrayList<Subqueue>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
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
                mSubqueues.clear();
                mSubqueues.addAll(subqueues);
                updateAdapter();
            } else {
                Toast.makeText(getActivity(), "获取队列信息失败，请重试", Toast.LENGTH_LONG).show();
            }
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
                    Intent intent = new Intent(getActivity(), UserListActivity.class);
                    intent.putExtra(UserListFragment.EXTRA_SUBQUEUE_NUMBER, index);
                    intent.putExtra(UserListFragment.EXTRA_SUBQUEUE_NAME, name);
                    startActivity(intent);
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
