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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gonggong.R;
import com.example.gonggong.ui.calender.decorators.EventDecorator;
import com.example.gonggong.ui.calender.decorators.OneDayDecorator;
import com.example.gonggong.ui.calender.decorators.SaturdayDecorator;
import com.example.gonggong.ui.calender.decorators.SundayDecorator;
import com.example.gonggong.ui.home.HomeData;
import com.example.gonggong.ui.home.HomeFragment;
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
    private int Year, Month, Day;

    private String SignIn_ID = "1633866283";

    private String RequestType = "CalenderDataUpdate";

    private ArrayList<CalenderData> mCalenderDatas = new ArrayList<>();
    private ArrayList<HomeData> mHomeDatas = new ArrayList<>();

    private CalenderAdapter adapter;

    private MaterialCalendarView mMaterialCalendarView;
    private RecyclerView mRecyclerViewCalender;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mButtonTest;

    private SundayDecorator mSundayDecorator = new SundayDecorator();
    private SaturdayDecorator mSaturdayDecorator = new SaturdayDecorator();
    private OneDayDecorator mOneDayDecorator = new OneDayDecorator();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_calender, container, false);

        mRecyclerViewCalender = (RecyclerView) rootView.findViewById(R.id.mRecyclerViewCalender);
        mRecyclerViewCalender.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerViewCalender.setLayoutManager(mLayoutManager);
        mRecyclerViewCalender.scrollToPosition(0);
        adapter = new CalenderAdapter(mHomeDatas);

        mRecyclerViewCalender.setAdapter(adapter);
        mRecyclerViewCalender.setItemAnimator(new DefaultItemAnimator());


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

                Year = date.getYear();
                Month = date.getMonth() + 1;
                Day = date.getDay();

                mHomeDatas.clear();
                CalenderDataUpdate();

            }
        });

        HomeDataUpdate();

        return rootView;
    }

    public void CalenderDataUpdate() {      //달력 게시물 작성 확인 새로고침 메소드
        RequestType = "CalenderDataUpdate";
        mCalenderDatas.clear();             //CalenderData 목록 clear
        adapter.notifyDataSetChanged();     // 게시글 목록 갱신
        CalenderFragment.GetData task = new CalenderFragment.GetData();
        task.execute("http://" + IP_ADDRESS + "/instudy/GetImgExample.php", "");
    }

    public void HomeDataUpdate() {          //게시글 새로고침 메소드
        RequestType = "HomeDataUpdate";
        mHomeDatas.clear();                 //HomeData 목록 clear
        adapter.notifyDataSetChanged();     // 게시글 목록 갱신
        CalenderFragment.GetData task = new CalenderFragment.GetData();
        task.execute("http://" + IP_ADDRESS + "/instudy/GetImgExample.php", "");
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
                int YY = Integer.parseInt(time[0]);
                int MM = Integer.parseInt(time[1]);
                int DD = Integer.parseInt(time[2]);

                dates.add(day);
                calendar.set(YY, MM - 1, DD);
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

            if (result == null) {

            } else {

                mJsonString = result;

                if (RequestType == "HomeDataUpdate") {
                    CalenderDataProcess();   //  DataProcess 메소드 실행
                    Log.i("Request Type", "HomeDataUpdate");
                } else if (RequestType == "CalenderDataUpdate") {
                    Log.i("Request Type", "CalenderDataUpdate");
                    HomeDataProcess(Year, Month, Day);
                } else {
                    Log.e("Error", "Unknown Request");
                }


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

    private void HomeDataProcess(int YY, int MM, int DD) {
        Log.i("YYMMDD", "I Get -> " + YY + "-" + MM + "-" + DD);

        String TAG_JSON = "Post Table";
        String TAG_CODE = "PostCode";
        String TAG_POST_WID = "PostWID";
        String TAG_NICKNAME = "PostNickName";
        String TAG_DATE = "PostDate";
        String TAG_CONTENTS = "PostContent";
        String TAG_IMGPATH = "PostImgPath";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = jsonArray.length() - 1; i >= 0; i--) {

                JSONObject item = jsonArray.getJSONObject(i);

                String POST_CODE = item.getString(TAG_CODE);
                String POST_WID = item.getString(TAG_POST_WID);
                String POST_NICKNAME = item.getString(TAG_NICKNAME);
                String POST_CONTENTS = item.getString(TAG_CONTENTS);
                String POST_DATE = item.getString(TAG_DATE);
                String POST_IMGPATH = item.getString(TAG_IMGPATH);

                String Date = POST_DATE.substring(0, 10);

                String[] Dates = Date.split("-");

                int Y = Integer.parseInt(Dates[0]);
                int M = Integer.parseInt(Dates[1]);
                int D = Integer.parseInt(Dates[2]);

                if (YY == Y && MM == M && DD == D) {

                    if(POST_WID == "1633866283") {
                        HomeData mHomeData = new HomeData();

                        mHomeData.setCode(POST_CODE); // 게시글 코드
                        mHomeData.setImgPost(POST_IMGPATH); // 게시글 사진 경로

                        mHomeData.setNickname(POST_NICKNAME); // 게시글 작성자
                        mHomeData.setDate(POST_DATE); // 게시글 작성 날짜
                        mHomeData.setContents(POST_CONTENTS); // 게시글 내용

                        mHomeData.setUserid(POST_WID);   // 게시글 작성자 ID

                        mHomeDatas.add(mHomeData);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    private void CalenderDataProcess() {

        String TAG_JSON = "Post Table";
        String TAG_PostWID = "PostWID";
        String TAG_PostDate = "PostDate";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = jsonArray.length() - 1; i >= 0; i--) {

                JSONObject item = jsonArray.getJSONObject(i);

                String PostWID = item.getString(TAG_PostWID);
                String PostDate = item.getString(TAG_PostDate).substring(0, 10);
                CalenderData mCalenderData = new CalenderData();

                mCalenderData.setPostWID(PostWID); // 게시글 코드
                mCalenderData.setPostDate(PostDate); // 게시글 사진 경로

                String[] Date = PostDate.split("-");

                int YY = Integer.parseInt(Date[0]);
                int MM = Integer.parseInt(Date[1]);
                int DD = Integer.parseInt(Date[2]);

                mMaterialCalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(CalendarDay.from(YY, MM - 1, DD))));

                mCalenderDatas.add(mCalenderData);
            }
        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }
}