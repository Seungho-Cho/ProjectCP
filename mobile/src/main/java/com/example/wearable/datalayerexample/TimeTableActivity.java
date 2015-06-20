package com.example.wearable.datalayerexample;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;


import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class TimeTableActivity extends ActionBarActivity
        implements GestureDetector.OnGestureListener
{

    private static final int SWIPE_MIN_DISTANCE = 120;          // 제스처 최소 거리
    private static final int SWIPE_MAX_OFF_PATH = 2000;         // 제스처 최대 거리
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;    // 제스처 인식 속도
    private GestureDetector gestureDetector;    // 제스처 처리

    private long tap_time = 0;
    private int tap_count = 0;
    private int tap_on = 0;
    private int long_press_on = 0;

    private int current_menu = 1;
    TTSAdapter tts;
    ViewFlipper flipper;

    /////////////////////  제스처 디텍터 ///////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("onTouch", "onTouch");
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }


    ///////////////////////////////////////////////////

    GridView grid;
    Button btn_alarm;
    String[] table = { "요일/교시", "월요일", "화요일", "수요일", "목요일", "금요일",
            "1교시 09:30~10:20", "","", "", "", "",
            "2교시 10:30~11:20", "", "", "", "", "",
            "3교시 11:30~12:20","", "", "", "", "",
            "4교시 12:30~13:20", "", "", "", "", "",
            "5교시 13:30~14:20", "", "", "", "", "",
            "6교시 14:30~15:20", "", "", "", "","",
            "7교시 15:30~16:20", "", "", "", "", "",
            "8교시 16:30~17:20", "", "", "","", "",
            "9교시 17:25~18:15", "", "", "", "", "",
            "10교시 18:15~19:05", "","", "", "", "",
            "11교시 19:05~19:55", "", "", "", "", "",
            "12교시 20:00~20:50", "", "", "", "", "",
            "13교시 20:50~21:40", "", "", "","", "",
            "14교시 21:40~22:30", "", "", "", "", ""
    };
    ArrayList<Integer> time_list;
    HashMap<Integer, TimeTable> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        Intent mIntent = new Intent(this.getIntent());
        Intent intent=new Intent(this.getIntent());
        ArrayList<Integer> test = new ArrayList<Integer>(){};
        btn_alarm = (Button)findViewById(R.id.y_btn_alarm);

        map = new HashMap<Integer, TimeTable>();
        ArrayList<Integer> idx_data = (ArrayList<Integer>)intent.getExtras().getSerializable("idx_data");
        time_list = (ArrayList<Integer>)intent.getExtras().getSerializable("time_list");
        ArrayList<String> subject_list = (ArrayList<String>)intent.getExtras().getSerializable("subject_list");
        flipper = (ViewFlipper) findViewById(R.id.y_menuFlipper);

        //////// tts 객체 선언
        tts = new TTSAdapter(this);
        //////// 제스쳐 디텍터 객체 선언
        gestureDetector = new GestureDetector(this);


        map.put(14, new TimeTable("화요일",10,30,"엔터프라이즈 컴퓨팅"));
        map.put(20, new TimeTable("화요일",11,30,"엔터프라이즈 컴퓨팅"));
        map.put(33, new TimeTable("수요일",13,30,"엔터프라이즈 컴퓨팅"));
        map.put(44, new TimeTable("화요일",15,30,"스포츠 클라이밍"));
        map.put(50, new TimeTable("화요일",16,30,"스포츠 클라이밍"));
        map.put(62, new TimeTable("화요일",18,15,"EH스마트앱설계"));
        map.put(68, new TimeTable("화요일",19,5,"EH스마트앱설계"));
        map.put(74, new TimeTable("화요일",20,00,"EH스마트앱설계"));
        map.put(64, new TimeTable("목요일",18,15,"네트워크 매니지먼트"));
        map.put(70, new TimeTable("목요일",19,5,"네트워크 매니지먼트"));
        map.put(76, new TimeTable("목요일",20,00,"네트워크 매니지먼트"));
        map.put(82, new TimeTable("목요일",20,50,"네트워크 매니지먼트"));
        map.put(57, new TimeTable("수요일",17,25, "종합설계"));
        map.put(63, new TimeTable("수요일",18,15, "종합설계"));
        map.put(69, new TimeTable("수요일",19,5, "종합설계"));
        map.put(75, new TimeTable("수요일",20,00, "종합설계"));
        map.put(45, new TimeTable("수요일",15,30, "기초 중국어"));
        map.put(51, new TimeTable("수요일",16,30, "기초 중국어"));


        for(int i=0; i< time_list.size(); i++)
        {

            table[time_list.get(i)] = map.get(time_list.get(i)).getClassName();
        }


        CustomGrid adapter = new CustomGrid(TimeTableActivity.this, table, time_list, subject_list, map);
        grid = (GridView) findViewById(R.id.y_grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(TimeTableActivity.this,
                        "You Clicked at " + table[+position],
                        Toast.LENGTH_SHORT).show();
            }
        });

        btn_alarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                //SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE");
                SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
                SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
                String currentDay = sdfDay.format(date);
                int currentHour = Integer.parseInt(sdfHour.format(date));
                int currentMinute = Integer.parseInt(sdfMinute.format(date));
                boolean check = false;

                for (int i = 0; i < time_list.size(); i++) {
                    int hour = map.get(time_list.get(i)).getHour();
                    int minute = map.get(time_list.get(i)).getMinute();
                    String className = map.get(time_list.get(i)).getClassName();
                    String day = map.get(time_list.get(i)).getDay();

                    //if (day.equals("수요일"))
                    if (map.get(time_list.get(i)).getDay().equals(currentDay)) {
                        if (currentHour <= hour) //현재 시간이 강의 시간보다 작으면
                        {
                            if (currentHour == hour) {
                                if (currentMinute <= minute) //현재 분이 강의시간 분보다 작으면
                                {
                                    Toast.makeText(TimeTableActivity.this, "다음 강의는" + className + "이고 강의 시작 시간은" + hour + "시" + minute + "분 입니다.", Toast.LENGTH_SHORT).show();
                                    check = true;
                                    break;
                                }
                            } else {
                                Toast.makeText(TimeTableActivity.this, "다음 강의는" + className + "이고 강의 시작 시간은" + hour + "시" + minute + "분 입니다.", Toast.LENGTH_SHORT).show();
                                check = true;
                                break;
                            }

                        }

                        //table[time_list.get(i)] = map.get(time_list.get(i));

                    }
                }
                if (!check)
                    Toast.makeText(TimeTableActivity.this, "오늘 강의는 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override

    public void onBackPressed() {

        // 여기에 코드 입력
        Intent myIntent = new Intent(TimeTableActivity.this, TimeTableInputActivity.class);
        startActivity(myIntent);
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.destroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_time_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (tap_count == 2) {
            Log.d("tap", "triple");
            if (System.currentTimeMillis() <= tap_time + 2000) {
                tap_triple();
                tap_count = 0;
                tap_on = 0;
                return true;
            }
        }
        if (tap_count == 1) {
            Log.d("tap", "double");
            tap_count = 2;
            if (System.currentTimeMillis() <= tap_time + 2000) {
                Thread thread = new Thread() {
                    public void run() {
                        tap_on = 2;
                        try {
                            Thread.sleep(2000);
                            if (tap_on == 2) {
                                tap_double();
                                tap_count = 0;
                            }

                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                };
                thread.start();
                return true;
            }
        }
        if (tap_count == 0) {
            Log.d("tap", "single");
            if (System.currentTimeMillis() > tap_time + 2000) {
                tap_time = System.currentTimeMillis();
                tap_count = 1;

                Thread thread = new Thread() {
                    public void run() {
                        tap_on = 1;
                        try {
                            Thread.sleep(2000);
                            if (tap_on == 1) {
                                tap_single();
                                tap_count = 0;
                            }

                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                };
                thread.start();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //cp_TTS("테스트");
        Log.d("tap", "Long");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(this, "Left Swipe", Toast.LENGTH_SHORT).show();
                swipe_left();
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(this, "Right Swipe", Toast.LENGTH_SHORT).show();
                swipe_right();
            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(this, "Swipe up", Toast.LENGTH_SHORT).show();
                swipe_up();
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(this, "Swipe down", Toast.LENGTH_SHORT).show();
                swipe_down();
            }
        } catch (Exception e) {
        }
        return true;
    }


    public void swipe_left()
    {
        flipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_from_right));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.out_from_right));
        flipper.showNext();
        current_menu++;
        if(current_menu==6)
        {
            tts.speak("처음 항목 입니다.");
            current_menu=1;
        }
        speak_day(current_menu);
    }

    public void swipe_right()
    {
        flipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_from_left));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.out_from_left));
        flipper.showPrevious();
        current_menu--;
        if(current_menu==0) current_menu=5;
        speak_day(current_menu);
    }

    public void swipe_up()
    {

    }

    public void swipe_down()
    {

    }

    public void tap_single()
    {

    }

    public void tap_double()
    {
        speak_day_timetable(current_menu);
    }

    public void tap_triple()
    {

    }

    public void speak_day(int i)
    {
        switch(i)
        {
            case 1:
                tts.speak("월요일");
                break;
            case 2:
                tts.speak("화요일");
                break;
            case 3:
                tts.speak("수요일");
                break;
            case 4:
                tts.speak("목요일");
                break;
            case 5:
                tts.speak("금요일");
                break;
        }
    }

    public void speak_day_timetable(int i)
    {
        switch(i)
        {
            case 1:
                tts.speak("월요일 입니다");
                break;
        }
    }
}



