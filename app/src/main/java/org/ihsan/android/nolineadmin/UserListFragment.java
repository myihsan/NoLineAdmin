package org.ihsan.android.nolineadmin;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ihsan on 15/2/4.
 */
public class UserListFragment extends Fragment {
    public static final String EXTRA_SUBQUEUE_NUMBER = "org.ihsan.android.onlineadmin" +
            ".subqueue_number";
    public static final String EXTRA_SUBQUEUE_NAME = "org.ihsan.android.onlineadmin.subqueue_name";
    public static final String EXTRA_SUBQUEUE_USERS = "org.ihsan.android.onlineadmin" +
            ".subqueue_users";

    private ProgressBar mProgressBar;
    private ListView mUserListView;

    private boolean mIsHistory;
    private ArrayList<User> mUsers = new ArrayList<User>();

    public static UserListFragment newInstance(int number, String name, ArrayList<User> users) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_SUBQUEUE_NUMBER, number);
        args.putString(EXTRA_SUBQUEUE_NAME, name);
        if (users != null) {
            args.putSerializable(EXTRA_SUBQUEUE_USERS, users);
        }

        UserListFragment fragment = new UserListFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getArguments().getString(EXTRA_SUBQUEUE_NAME);
        getActivity().setTitle(title);
        if (!getArguments().containsKey(EXTRA_SUBQUEUE_USERS)) {
            mIsHistory = true;
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        mProgressBar= (ProgressBar) view.findViewById(R.id.load_progress);
        mUserListView = (ListView) view.findViewById(R.id.user_list_listView);
        if (!mIsHistory) {
            View header = inflater.inflate(R.layout.header_user_list, null);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserListActivity.class);
                    intent.putExtra(EXTRA_SUBQUEUE_NUMBER, getArguments().getInt
                            (EXTRA_SUBQUEUE_NUMBER));
                    intent.putExtra(EXTRA_SUBQUEUE_NAME, getArguments().getString
                            (EXTRA_SUBQUEUE_NAME)+"历史记录");
                    startActivity(intent);
                }
            });
            mUserListView.addHeaderView(header);
        }
        mUserListView.setAdapter(new UserAdapter(mUsers));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgress(true);
        new FetchQueueTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mUserListView.setVisibility(show ? View.GONE : View.VISIBLE);
        mUserListView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mUserListView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private class UserAdapter extends ArrayAdapter<User> {

        public UserAdapter(ArrayList<User> users) {
            super(getActivity(), 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_user, null);
            }

            User user = getItem(position);

            TextView numberTextView =
                    (TextView) convertView.findViewById(R.id.user_list_item_numberTextView);
            numberTextView.setText(String.valueOf(user.getNumber()));
            TextView tokenTextView =
                    (TextView) convertView.findViewById(R.id.user_list_item_tokenTextView);
            tokenTextView.setText(user.getToken());
            if (!getArguments().containsKey(EXTRA_SUBQUEUE_USERS)) {
                TextView stateTextView =
                        (TextView) convertView.findViewById(R.id.user_list_item_stateTextView);
                stateTextView.setText(user.getState());
            }

            return convertView;
        }
    }

    private class FetchQueueTask extends AsyncTask<Void, Void, ArrayList<User>> {
        @Override
        protected ArrayList<User> doInBackground(Void... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logined_queue_id), -1);
            if (!mIsHistory) {
                return new DataFetcher(getActivity()).fetchUser(queueId, getArguments().getInt
                        (EXTRA_SUBQUEUE_NUMBER), 0);
            } else {
                return new DataFetcher(getActivity()).fetchUser(queueId, getArguments().getInt
                        (EXTRA_SUBQUEUE_NUMBER));
            }
        }

        @Override
        protected void onPostExecute(ArrayList<User> users) {
            showProgress(false);
            if (users == null && !mIsHistory) {
                users = (ArrayList<User>) getArguments().getSerializable(EXTRA_SUBQUEUE_USERS);
                Toast.makeText(getActivity(), "服务器连接故障，更新信息失败", Toast.LENGTH_LONG).show();
            }
            if (users != null) {
                mUsers.clear();
                mUsers.addAll(users);
                if (!mIsHistory) {
                    HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter)
                            mUserListView.getAdapter();
                    ((UserAdapter) headerViewListAdapter.getWrappedAdapter())
                            .notifyDataSetChanged();
                    mUserListView.setSelection(1);
                } else {
                    ((UserAdapter) mUserListView.getAdapter()).notifyDataSetChanged();
                }
            } else {
                Toast.makeText(getActivity(), "获取信息失败，请重试", Toast.LENGTH_LONG).show();
            }
        }
    }
}
