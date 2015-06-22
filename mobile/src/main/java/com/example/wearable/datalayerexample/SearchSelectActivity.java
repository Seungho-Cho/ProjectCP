package com.example.wearable.datalayerexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SearchSelectActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener,
        GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener,
        DataApi.DataListener,
        GestureDetector.OnGestureListener,
        GestureValue

{

    private GestureDetector gestureDetector;    // 제스처 처리

    private long tap_time = 0;
    private int tap_count = 0;
    private int tap_on = 0;
    private int long_press_on = 0;

    private int current_menu = 1;
    TTSAdapter tts;

    /////////////////////  제스처 디텍터 ///////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d("onTouch", "onTouch");
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }


    ViewFlipper flipper;

    private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체
    private Handler mHandler;

    @Override // Activity
    protected void onStart() {
        super.onStart();

        // 구글 플레이 서비스에 접속돼 있지 않다면 접속한다.
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }


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
        setContentView(R.layout.activity_search_select);

        flipper = (ViewFlipper) findViewById(R.id.s_searchFlipper);
        tts = new TTSAdapter(this);
        gestureDetector = new GestureDetector(this);
        mHandler = new Handler();



        //tts.speak_delay("검색방법을 선택해 주세요",1000);

        Thread thred = new Thread()
        {
            public void run()
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tts.speak("검색방법을 선택해 주세요");
                speakMenu(current_menu);


            }
        };
        thred.start();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

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
        if(current_menu==3)
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
        if(current_menu==0) current_menu=2;
        speakMenu(current_menu);

    }

    public void swipe_up()
    {
        finish();
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

    public void tap_long()
    {

    }

    public void selectMenu(int i)
    {
        Intent intent = null;
        switch(i)
        {
            case 1:
                intent = new Intent(this,SearchSelectSttActivity.class);
                break;
            case 2:

                intent = new Intent(this,SearchSelectCateActivity.class);
                intent.putExtra("SA",0);
                break;
        }

        Thread thread = new Thread()
        {
            public void run()
            {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        };
        thread.start();
        startActivity(intent);


    }

    public void speakMenu(int i)
    {
        switch(i)
        {
            case 1:
                tts.speak("이름으로 검색");
                break;
            case 2:
                tts.speak("카테고리 검색");
                break;
        }
    }

    // 구글 플레이 서비스에 접속 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        //tts.speak("스마트워치가 연결 되었습니다.");
        Wearable.MessageApi.addListener(mGoogleApiClient,this);
    }

    // 구글 플레이 서비스에 접속이 일시정지 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int i) {
       // Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    // 구글 플레이 서비스에 접속을 실패했을 때 실행
    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        //tts.speak("스마트워치가 연결 되지 않았습니다.");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        if (messageEvent.getPath().equals("/MESSAGE_PATH")) {
            // 텍스트뷰에 적용 될 문자열을 지정한다.
            final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);

            // UI 스레드를 실행하여 텍스트 뷰의 값을 수정한다.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //mTextView.setText(msg);
                    //Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT);
                    Log.d("ges", msg);
                   // mEditText.setText(msg);

                    switch(msg)
                    {
                        case "LEFT_SWIPE":
                            swipe_left();
                            break;
                        case "RIGHT_SWIPE":
                            swipe_right();
                            break;
                        case "UP_SWIPE":
                            swipe_up();
                            break;
                        case "DOWN_SWIPE":
                            swipe_down();
                            break;
                        case "SINGLE_TAP":
                            tap_single();
                            break;
                        case "DOUBLE_TAP":
                            tap_double();
                            break;
                        case "TRIPLE_TAP":
                            tap_triple();
                            break;
                        case "LONG_PRESS":
                            tap_long();
                            break;

                    }

                }
            });
        }
    }
    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onPeerConnected(Node node) {

    }

    @Override
    public void onPeerDisconnected(Node node) {

    }

}
