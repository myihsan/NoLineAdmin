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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ihsan on 15/2/3.
 */
public class QueueFragment extends Fragment {
    private static final String TAG = "QueueFragment";

    private LinearLayout mQueueLinearLayout;
    private TextView mNextNumberTextView, mTotalTextView;
    private Button mNextOneButton, mQueueDetailButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        mQueueLinearLayout = (LinearLayout) view.findViewById(R.id.queue_linearLayout);
        mQueueLinearLayout.setVisibility(View.INVISIBLE);

        mNextNumberTextView = (TextView) view.findViewById(R.id.queue_now_textView);
        mTotalTextView = (TextView) view.findViewById(R.id.queue_total_textView);

        mNextOneButton = (Button) view.findViewById(R.id.queue_next_one);
        mNextOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NextQueuerTask().execute();
            }
        });

        mQueueDetailButton = (Button) view.findViewById(R.id.queue_detail);
        mQueueDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QueueDetailActivity.class);
                startActivity(intent);
            }
        });

        new FetchQueueTask().execute();
        setHasOptionsMenu(true);

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
                        .remove(getString(R.string.logged_admin_id))
                        .commit();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateView() {
        mNextNumberTextView.setText(String.valueOf(Queue.get(getActivity()).getNextNumber()));
        mTotalTextView.setText(String.valueOf(Queue.get(getActivity()).getTotal()));

    }

    private class FetchQueueTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            int adminId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logged_admin_id), -1);
            if (adminId != -1) {
                return new DataFetcher(getActivity()).fetchQueueByAdminId(adminId);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                updateView();
                mQueueLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private class NextQueuerTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            int adminId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logged_admin_id), -1);
            return new DataFetcher(getActivity()).fetchNextQueuerResult(adminId);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                new FetchQueueTask().execute();
            } else {
                Toast.makeText(getActivity(), "处理失败，请重试", Toast.LENGTH_LONG).show();
            }
        }
    }
}
