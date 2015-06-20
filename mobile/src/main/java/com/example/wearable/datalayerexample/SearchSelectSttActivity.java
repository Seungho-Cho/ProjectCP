package com.example.wearable.datalayerexample;

import android.content.Intent;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.LineNumberReader;
import java.util.ArrayList;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

public class SearchSelectSttActivity extends ActionBarActivity
    implements GestureValue,
        GestureDetector.OnGestureListener

{

    GNodeList gNodeList;
    SpeechRecognizer mRecognizer;
    TextView stt_result;
    Handler mHandler;
    LinearLayout resultList;

    Intent srIntent;

    private GestureDetector gestureDetector;    // 제스처 처리
    private long tap_time = 0;
    private int tap_count = 0;
    private int tap_on = 0;
    private int long_press_on = 0;


    TTSAdapter tts;

    private int state;

    private static final int STATE_SEARCH_ON  = 1;
    private static final int STATE_SEARCH_OFF = 0;


   // ResultNode results[] = null;
    int resultCount = 0;
    int currentResult = 0;


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mRecognizer.destroy();
        tts.destroy();
    }

    /////////////////////  제스처 디텍터 ///////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d("onTouch", "onTouch");
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_select_stt);

        gNodeList = new GNodeList();
        gestureDetector = new GestureDetector(this);
        stt_result = (TextView) findViewById(R.id.stt_text);

        resultList = (LinearLayout) findViewById(R.id.result_list);


        mHandler = new Handler();
        tts = new TTSAdapter(this);


        srIntent= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);            //음성인식 intent생성
        srIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());    //데이터 설정
        srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                            //음성인식 언어 설정

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);                //음성인식 객체
        mRecognizer.setRecognitionListener(listener);                                        //음성인식 리스너 등록


        tts.speak("이름 검색 입니다.");
        state = STATE_SEARCH_OFF;


    }

    //음성인식 리스너
    private RecognitionListener listener = new RecognitionListener() {
        //입력 소리 변경 시
        @Override public void onRmsChanged(float rmsdB) {}

        //음성 인식 결과 받음
        @Override public void onResults(Bundle results)
        {
            Log.d("debug", "Record Result");
            ArrayList <String> al = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            stt_result.setText(al.get(0));
            tts.speak(al.get(0)+"으로 검색합니다.");
            state = STATE_SEARCH_ON;

            resultCount = 0;
            currentResult = 0;


            for(int i=0; i<gNodeList.GNodeArr.length; i++)
            {
                if(gNodeList.GNodeArr[i].category == GNodeList.CATE_NODE) continue;

                for(int j=0; j<gNodeList.GNodeArr[i].keywords.length; j++)
                {


                    if( gNodeList.GNodeArr[i].keywords[j].compareTo(al.get(0)) == 0)
                    {
                        //if(results==null) results[0] = new ResultNode(gNodeList.GNodeArr[i].id,gNodeList.GNodeArr[i].Name);
                        Log.d("=======result======",gNodeList.GNodeArr[i].Name);
                        LinearLayout temp = (LinearLayout) View.inflate(getApplicationContext(), R.layout.result_list_layout, null);
                        TextView noView = (TextView) temp.getChildAt(0);
                        TextView nameView = (TextView) temp.getChildAt(1);

                        noView.setText(gNodeList.GNodeArr[i].id+"");
                        nameView.setText(gNodeList.GNodeArr[i].Name);


                        resultList.addView(temp);
                        resultCount++;
                    }
                }
            }


            if(resultCount == 0) return;

            currentResult = 1;



        }

        //음성 인식 준비가 되었으면
        @Override public void onReadyForSpeech(Bundle params)
        {
            Log.d("debug", "Record Ready");
        }

        //음성 입력이 끝났으면
        @Override public void onEndOfSpeech()
        {
            Log.d("debug", "Record End");
        }

        //에러가 발생하면
        @Override public void onError(int error)
        {
            Log.d("debug", "Record Error : "+error);
        }

        @Override public void onBeginningOfSpeech()
        {
            Log.d("debug", "Record Start");

           // finish();
        }
        //입력이 시작되면


        @Override public void onPartialResults(Bundle partialResults)
        {
            Log.d("debug", "Record PResults");
            ArrayList <String> al = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        }       //인식 결과의 일부가 유효할 때

        //미래의 이벤트를 추가하기 위해 미리 예약되어진 함수
        @Override public void onEvent(int eventType, Bundle params) {}
        @Override public void onBufferReceived(byte[] buffer) {}                //더 많은 소리를 받을 때
    };


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
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        Log.d("debug", "LONG");
        mRecognizer.startListening(srIntent);
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

    }

    public void swipe_right()
    {


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
        if(state == STATE_SEARCH_ON)
        {
            if (resultCount > 0)
            {
                Intent naviIntent = new Intent(this,MapActivity.class);
                naviIntent.putExtra("mode",1);

                LinearLayout temp = (LinearLayout) resultList.getChildAt(currentResult - 1);
                TextView noTemp = (TextView) temp.getChildAt(0);
                naviIntent.putExtra("dest", Integer.parseInt((String)noTemp.getText()));

                finish();
                startActivity(naviIntent);

            }
        }
    }

    public void tap_triple()
    {

    }

    public void select_result()
    {

    }


}

class ResultNode
{
    int index;
    String name;

    ResultNode(int i, String str)
    {
        index = i;
        name = str;
    }
}

