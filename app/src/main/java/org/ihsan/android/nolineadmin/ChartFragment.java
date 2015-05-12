package org.ihsan.android.nolineadmin;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Ihsan on 15/4/23.
 */
public class ChartFragment extends Fragment {

    private TextView mDateTextView;
    private BarChart mChart;
    private ValueFormatter mFormatter = new MyValueFormatter();

    private String mBeginDate, mEndDate;
    private ImageButton mDateLeftButton;
    private ImageButton mDateRightButton;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        mDateTextView = (TextView) view.findViewById(R.id.chart_date_textView);
        if (mEndDate == null) {
            getThisWeekStart(new Date());
        }
        mDateTextView.setText(mBeginDate + " ~ " + mEndDate);

        mDateLeftButton = (ImageButton) view.findViewById(R.id.chart_date_left_button);
        mDateRightButton = (ImageButton) view.findViewById(R.id.chart_date_right_button);
        mDateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(mDateFormat.parse(mBeginDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calendar.add(Calendar.DATE, -1);
                getThisWeekStart(calendar.getTime());
                mDateTextView.setText(mBeginDate + " ~ " + mEndDate);
                mDateRightButton.setClickable(true);
                new FetchBarChartData().execute();
            }
        });
        mDateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(mDateFormat.parse(mEndDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calendar.add(Calendar.DATE, 1);
                getThisWeekEnd(calendar.getTime());
                mDateTextView.setText(mBeginDate + " ~ " + mEndDate);
                new FetchBarChartData().execute();
            }
        });
        mDateRightButton.setClickable(false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mChart = (HorizontalBarChart) view.findViewById(R.id.chart);
        } else {
            mChart = (BarChart) view.findViewById(R.id.chart);
        }
        mChart.setDescription("");
        mChart.setPinchZoom(false);
        mChart.setNoDataText("数据不存在");

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter(mFormatter);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setValueFormatter(mFormatter);

        new FetchBarChartData().execute();
        return view;
    }

    public void getThisWeekStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        mEndDate = mDateFormat.format(date);
        int minus = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (minus < 0) {
            minus = 6;
        }
        calendar.add(Calendar.DATE, -minus);
        mBeginDate = mDateFormat.format(calendar.getTime());
    }

    public void getThisWeekEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        mBeginDate = mDateFormat.format(date);
        calendar.add(Calendar.DATE, 6);
        Date endDate = calendar.getTime();
        Date nowDate = new Date();
        if (endDate.before(nowDate)) {
            mEndDate = mDateFormat.format(endDate);
        } else {
            mEndDate = mDateFormat.format(nowDate);
            mDateRightButton.setClickable(false);
        }
    }


    private void setData(ArrayList<BarChartData> dataArray) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Collections.sort(dataArray, new Comparator<BarChartData>() {
                @Override
                public int compare(BarChartData lhs, BarChartData rhs) {
                    return Integer.valueOf(lhs.getName()) > Integer.valueOf(rhs.getName()) ? -1 : 1;
                }
            });
        }

        if (dataArray.isEmpty()) {
            mChart.clear();
        } else {
            ArrayList<String> xVals = new ArrayList<String>();

            ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();

            for (BarChartData data : dataArray) {
                xVals.add(data.getName());
                yVals.add(new BarEntry(data.getValue(), dataArray.indexOf(data)));
            }

            BarDataSet set1 = new BarDataSet(yVals, "人数");
            set1.setBarSpacePercent(35f);
            set1.setColor(getResources().getColor(R.color.primary));

            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(xVals, dataSets);
            data.setValueFormatter(mFormatter);
            data.setValueTextSize(10f);

            mChart.setData(data);
        }
        mChart.invalidate();
    }

    private class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###,##0");
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value);
        }

    }

    private class FetchBarChartData extends AsyncTask<Void, Void, ArrayList<BarChartData>> {
        @Override
        protected ArrayList<BarChartData> doInBackground(Void... params) {
            int queueId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(getString(R.string.logined_queue_id), -1);
            return new DataFetcher(getActivity()).fetchBarChartData(queueId,mBeginDate,mEndDate);
        }

        @Override
        protected void onPostExecute(ArrayList<BarChartData> dataArray) {
            setData(dataArray);
        }
    }


}
