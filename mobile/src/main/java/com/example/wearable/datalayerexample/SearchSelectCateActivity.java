package com.example.wearable.datalayerexample;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class SearchSelectCateActivity extends ActionBarActivity
        implements GestureDetector.OnGestureListener,
        GestureValue
{


    private GestureDetector gestureDetector;    // 제스처 처리

    private long tap_time = 0;
    private int tap_count = 0;
    private int tap_on = 0;
    private int long_press_on = 0;

    private int current_menu = 1;
    TTSAdapter tts;


    Intent intent;
    int mode = 0;


    /////////////////////  제스처 디텍터 ///////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d("onTouch", "onTouch");
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }


    ViewFlipper flipper;


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        tts.destroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_select_cate);

        flipper = (ViewFlipper) findViewById(R.id.s_searchFlipper);
        tts = new TTSAdapter(this);
        gestureDetector = new GestureDetector(this);
        intent = getIntent();
        mode = intent.getIntExtra("SA",0);

        speakFirst();
        tts.speak_delay("검색 방법을 선택해 주세요", 1500);



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
            if (System.currentTimeMillis() <= tap_time + TAP_TERM) {
                tap_triple();
                tap_count = 0;
                tap_on = 0;
                return true;
            }
        }
        if (tap_count == 1) {
            Log.d("tap", "double");
            tap_count = 2;
            if (System.currentTimeMillis() <= tap_time + TAP_TERM) {
                Thread thread = new Thread() {
                    public void run() {
                        tap_on = 2;
                        try {
                            Thread.sleep(TAP_TERM);
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
            if (System.currentTimeMillis() > tap_time + TAP_TERM) {
                tap_time = System.currentTimeMillis();
                tap_count = 1;

                Thread thread = new Thread() {
                    public void run() {
                        tap_on = 1;
                        try {
                            Thread.sleep(TAP_TERM);
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
        speakHelp();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(this, "Left Swipe", Toast.LENGTH_SHORT).show();
                swipe_left();
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(this, "Right Swipe", Toast.LENGTH_SHORT).show();
                swipe_right();
            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(this, "Swipe up", Toast.LENGTH_SHORT).show();
                swipe_up();
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //Toast.makeText(this, "Swipe down", Toast.LENGTH_SHORT).show();
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
        if(current_menu==4)
        {
            tts.speak("처음 항목 입니다.");
            current_menu=1;
        }
        speakMenu(current_menu);
    }

    public void swipe_right()
    {
        flipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_from_left));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.out_from_left));
        flipper.showPrevious();
        current_menu--;
        if(current_menu==0) current_menu=3;
        speakMenu(current_menu);

        //speakFirst();

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
        selectMenu(current_menu);
    }

    public void tap_triple()
    {

    }

    public void selectMenu(int i)
    {
        Intent nintent = null;

        String name[];
        int no[];
        GNodeList list = new GNodeList();
        int count = 0;
        int temp_count = 0;

        switch(i)
        {
            case 1:

                if(mode == 1)
                {
                    nintent = new Intent(this, MapActivity.class);
                    nintent.putExtra("mode",2);
                    nintent.putExtra("filter",8);
                }
                else if(mode == 0)
                {
                    for(int j=0; j<list.GNodeArr.length; j++)
                    {
                        if(list.GNodeArr[j].category == 8) count ++;
                    }

                    name = new String[count];
                    no = new int[count];

                    for(int j=0; j<list.GNodeArr.length; j++)
                    {
                        if(list.GNodeArr[j].category == 8)
                        {
                            Log.d("search",list.GNodeArr[j].Name+"/"+list.GNodeArr[j].id);
                            name[temp_count] = list.GNodeArr[j].Name;
                            no[temp_count++] = list.GNodeArr[j].id;
                        }
                    }
                    nintent = new Intent(this, SearchSelectListActivity.class);
                    nintent.putExtra("name_ar",name);
                    nintent.putExtra("no_ar",no);

                }
                break;
            case 2:

                if(mode == 1)
                {
                    nintent = new Intent(this, MapActivity.class);
                    nintent.putExtra("mode",2);
                    nintent.putExtra("filter",2);
                }
                else if(mode == 0)
                {
                    for(int j=0; j<list.GNodeArr.length; j++)
                    {
                        if(list.GNodeArr[j].category == 2) count ++;
                    }

                    name = new String[count];
                    no = new int[count];

                    for(int j=0; j<list.GNodeArr.length; j++)
                    {
                        if(list.GNodeArr[j].category == 2)
                        {
                            Log.d("search",list.GNodeArr[j].Name+"/"+list.GNodeArr[j].id);
                            name[temp_count] = list.GNodeArr[j].Name;
                            no[temp_count++] = list.GNodeArr[j].id;
                        }
                    }
                    nintent = new Intent(this, SearchSelectListActivity.class);
                    nintent.putExtra("name_ar",name);
                    nintent.putExtra("no_ar",no);
                }
                break;
            case 3:

                if(mode == 1)
                {
                    nintent = new Intent(this, MapActivity.class);
                    nintent.putExtra("mode",2);
                    nintent.putExtra("filter",4);
                }
                else if(mode == 0)
                {
                    for(int j=0; j<list.GNodeArr.length; j++)
                    {
                        if(list.GNodeArr[j].category == 4) count ++;
                    }

                    name = new String[count];
                    no = new int[count];

                    for(int j=0; j<list.GNodeArr.length; j++)
                    {
                        if(list.GNodeArr[j].category == 4)
                        {
                            Log.d("search",list.GNodeArr[j].Name+"/"+list.GNodeArr[j].id);
                            name[temp_count] = list.GNodeArr[j].Name;
                            no[temp_count++] = list.GNodeArr[j].id;
                        }
                    }
                    nintent = new Intent(this, SearchSelectListActivity.class);
                    nintent.putExtra("name_ar",name);
                    nintent.putExtra("no_ar",no);
                }
                break;
        }

        finish();
        startActivity(nintent);

    }

    public void speakMenu(int i)
    {
        switch(i)
        {
            case 1:
                tts.speak("공학관");
                break;
            case 2:
                tts.speak("일반건물");
                break;
            case 3:
                tts.speak("편의시설");
                break;
        }
    }

    public void speakFirst()
    {
        if(mode == 1)
        {
            tts.speak_delay("공간지각확장 설정 메뉴 입니다 ",1000);

            Thread thread = new Thread()
            {
                public void run()
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    speakMenu(current_menu);
                }
            };
        }
        else
        {
            tts.speak_delay("카테고리 검색 메뉴 입니다", 1000);
            Thread thread = new Thread()
            {
                public void run()
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    speakMenu(current_menu);
                }
            };
        }
    }
    public void speakHelp()
    {
        if(mode == 1)
        {
            tts.speak("공간지각확장 설정 메뉴 입니다 설명할 건물을 카테고리 별로 선택 합니다");
        }
        else
        {
            tts.speak("카테고리 검색 메뉴 입니다 검색할 건물을 카테고리 별로 선택 합니다");
        }
    }
}
