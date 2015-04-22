package org.ihsan.android.nolineadmin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ihsan on 15/2/3.
 */
public class QueueFragment extends Fragment {
    private static final String TAG = "QueueFragment";

    private LinearLayout mSubqueueLinearLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        mSubqueueLinearLayout = (LinearLayout) view.findViewById(R.id.subqueue_linearLayout);
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
                        .remove(getString(R.string.logged_queue_id))
                        .commit();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    private class FetchQueueTask extends AsyncTask<Void, Void, Boolean> {
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            int adminId = PreferenceManager.getDefaultSharedPreferences(getActivity())
//                    .getInt(getString(R.string.logged_admin_id), -1);
//            if (adminId != -1) {
//                return new DataFetcher(getActivity()).fetchQueueByAdminId(adminId);
//            }
//            return false;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            if (aBoolean) {
//                updateView();
//                mQueueLinearLayout.setVisibility(View.VISIBLE);
//            }
//        }
//    }

    private class GetDetailTask extends AsyncTask<Void, Void, ArrayList<Subqueue>> {
        @Override
        protected ArrayList<Subqueue> doInBackground(Void... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logged_queue_id), -1);
            if (queueId != -1) {
                return new DataFetcher(getActivity()).fetchQueueDetail(queueId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Subqueue> subqueues) {
            if (subqueues != null) {
                mSubqueueLinearLayout.removeAllViews();
                for (Subqueue subqueue : subqueues) {
                    final int index = subqueues.indexOf(subqueue);
                    View subqueueView = getActivity().getLayoutInflater().inflate(R.layout.subqueue_item, mSubqueueLinearLayout, false);
                    TextView subqueueNameTextView = (TextView) subqueueView.findViewById(R.id.subqueue_name_textView);
                    LinearLayout descriptionView= (LinearLayout) subqueueView.findViewById(R.id.description_view);
                    TextView subqueueTotalTextView = (TextView) subqueueView.findViewById(R.id.subqueue_total_textView);
                    TextView subqueueFirstNumberTextView = (TextView) subqueueView.findViewById(R.id.subqueue_first_number_textView);
                    TextView subqueueLeftButton = (TextView) subqueueView.findViewById(R.id.subquque_left_button);
                    TextView subqueueRightButton = (TextView) subqueueView.findViewById(R.id.subquque_right_button);
                    subqueueNameTextView.setText(subqueue.getName());
                    subqueueTotalTextView.setText(String.valueOf(subqueue.getTotal()));
                    descriptionView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

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
                    mSubqueueLinearLayout.addView(subqueueView, index);
                }
            } else {
                Toast.makeText(getActivity(), "获取队列信息失败，请重试", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class NextQueuerTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logged_queue_id), -1);
            return new DataFetcher(getActivity()).fetchNextQueuerResult(queueId, params[0], params[1]);
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
}
