package org.ihsan.android.nolineadmin;


import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ihsan on 15/2/4.
 */
public class UserListFragment extends Fragment {
    public static final String EXTRA_SUBQUEUE_NUMBER = "org.ihsan.android.onlineadmin.subqueue_number";
    public static final String EXTRA_SUBQUEUE_NAME = "org.ihsan.android.onlineadmin.subqueue_name";
    private ListView mUserListView;
    private ArrayList<User> mUsers = new ArrayList<User>();

    public static UserListFragment newInstance(int number, String name) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_SUBQUEUE_NUMBER, number);
        args.putString(EXTRA_SUBQUEUE_NAME, name);

        UserListFragment fragment = new UserListFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getArguments().getString(EXTRA_SUBQUEUE_NAME);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        mUserListView = (ListView) view.findViewById(R.id.user_list_listView);
        mUserListView.setAdapter(new UserAdapter(mUsers));
        new FetchQueueTask().execute();
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

            return convertView;
        }
    }

    private class FetchQueueTask extends AsyncTask<Void, Void, ArrayList<User>> {
        @Override
        protected ArrayList<User> doInBackground(Void... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logged_queue_id), -1);
            return new DataFetcher(getActivity()).fetchUser(queueId, getArguments().getInt(EXTRA_SUBQUEUE_NUMBER));
        }

        @Override
        protected void onPostExecute(ArrayList<User> users) {
            mUsers.clear();
            mUsers.addAll(users);
            ((UserAdapter) mUserListView.getAdapter()).notifyDataSetChanged();
        }
    }
}
