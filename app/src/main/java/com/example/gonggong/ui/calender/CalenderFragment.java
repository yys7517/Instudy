package com.example.gonggong.ui.calender;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gonggong.R;
import com.example.gonggong.ui.calender.decorators.EventDecorator;
import com.example.gonggong.ui.calender.decorators.OneDayDecorator;
import com.example.gonggong.ui.calender.decorators.SaturdayDecorator;
import com.example.gonggong.ui.calender.decorators.SundayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CalenderFragment extends Fragment {

    private static String IP_ADDRESS = "211.211.158.42";
    private static String TAG = "phptest";
    private String mJsonString;

    private ArrayList<CalanderData> mCalanderDatas = new ArrayList<>();

    private MaterialCalendarView mMaterialCalendarView;
    private Button mButtonTest;

    private SundayDecorator mSundayDecorator = new SundayDecorator();
    private SaturdayDecorator mSaturdayDecorator = new SaturdayDecorator();
    private OneDayDecorator mOneDayDecorator = new OneDayDecorator();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_calender, container, false);

        mMaterialCalendarView = (MaterialCalendarView) rootView.findViewById(R.id.mMaterialCalendarView);

        mMaterialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2010, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2049, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        mMaterialCalendarView.addDecorators(
                mSundayDecorator,
                mSaturdayDecorator,
                mOneDayDecorator
        );


        mMaterialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

            }
        });

        mButtonTest = (Button) rootView.findViewById(R.id.mButtonTest);
        mButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Test", "테스트 메세지");

                CalenderFragment.GetData task = new CalenderFragment.GetData();
                task.execute("http://" + IP_ADDRESS + "/instudy/GetCalander.php", "");
            }
        });

        return rootView;
    }

    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        String[] Time_Result;

        ApiSimulator(String[] Time_Result) {
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            for (int i = 0; i < Time_Result.length; i++) {
                CalendarDay day = CalendarDay.from(calendar);
                String[] time = Time_Result[i].split(",");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int dayy = Integer.parseInt(time[2]);

                dates.add(day);
                calendar.set(year, month - 1, dayy);
            }

            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
        }
    }

    // 값 가져오는 클래스
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            Log.d(TAG, "response - " + result);

            if (result == null) {

            } else {

                mJsonString = result;
                DataProcess();   //  showBoard 메소드 실행
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void DataProcess() {

        String TAG_JSON = "PostCount Table";
        String TAG_PostWID = "PostWID";
        String TAG_PostDate = "PostDate";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = jsonArray.length() - 1; i >= 0; i--) {

                JSONObject item = jsonArray.getJSONObject(i);

                String PostWID = item.getString(TAG_PostWID);
                String PostDate = item.getString(TAG_PostDate).substring(0, 10);

                CalanderData mCalanderData = new CalanderData();

                mCalanderData.setPostWID(PostWID); // 게시글 코드
                mCalanderData.setPostDate(PostDate); // 게시글 사진 경로

                String[] Date = PostDate.split("-");

                int YY = Integer.parseInt(Date[0]);
                int MM = Integer.parseInt(Date[1]);
                int DD = Integer.parseInt(Date[2]);

                mMaterialCalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(CalendarDay.from(YY, MM - 1, DD))));

                mCalanderDatas.add(mCalanderData);
            }
        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }
}