package com.example.wearable.datalayerexample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SearchSelectListActivity extends ActionBarActivity
    implements GestureDetector.OnGestureListener,
        GestureValue,
        GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener,
        GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener,
        DataApi.DataListener
    {

        private GestureDetector gestureDetector;    // 제스처 처리
        private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체

        private long tap_time = 0;
        private int tap_count = 0;
        private int tap_on = 0;
        private int long_press_on = 0;

        //private int current_menu = 0;
        private int menu_lenth = 0;

        ResultNode results[];
        String name[];
        int no[];

        Intent intent;

        TTSAdapter tts;

        /////////////////////  제스처 디텍터 ///////////////////////////////
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            Log.d("onTouch", "onTouch");
            return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
        }


        LinearLayout mainLayout;
        int resultCount = 0;
        int currentResult = 0;


        @Override
        protected void onDestroy()
        {
            super.onDestroy();
            tts.destroy();
        }
        @Override // Activity
        protected void onStart() {
            super.onStart();

            // 구글 플레이 서비스에 접속돼 있지 않다면 접속한다.
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_search_select_list);

            mainLayout = (LinearLayout) findViewById(R.id.list_main_layout);
            tts = new TTSAdapter(this);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            gestureDetector = new GestureDetector(this);
            intent = getIntent();

            name = intent.getStringArrayExtra("name_ar");
            no = intent.getIntArrayExtra("no_ar");

            resultCount = no.length;

            results = new ResultNode[no.length];
            for(int i=0; i<no.length; i++)
            {
                ResultNode temp = new ResultNode(no[i],name[i]);
                results[i] = temp;

                LinearLayout tempView = (LinearLayout) View.inflate(getApplicationContext(),R.layout.result_list_layout,null);
                TextView noView = (TextView) tempView.getChildAt(0);
                TextView nameView = (TextView) tempView.getChildAt(1);



                Log.d("debug","list no:"+no[i]);
                Log.d("debug","list name:"+name[i]);
                noView.setText(no[i]+"");
                nameView.setText(name[i]);
                tempView.setBackgroundColor(Color.BLACK);

                mainLayout.addView(tempView);

            }



            currentResult= 1;
            tts.speak("검색결과가 "+no.length+"개 있습니다");


            TextView nameView = (TextView)((LinearLayout)mainLayout.getChildAt(0)).getChildAt(1);
            tts.speak("처음 항목 입니다");
            tts.speak_delay((String) nameView.getText(),1000);


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
        currentResult--;
        if(currentResult==0)
        {
            mainLayout.getChildAt(currentResult).setBackgroundColor(Color.BLACK);
            currentResult=resultCount;
            mainLayout.getChildAt(currentResult-1).setBackgroundColor(Color.GRAY);
            LinearLayout l_temp = (LinearLayout) mainLayout.getChildAt(currentResult-1);
            TextView t_temp = (TextView) l_temp.getChildAt(1);

            tts.speak((String) t_temp.getText());
        }

        mainLayout.getChildAt(currentResult).setBackgroundColor(Color.BLACK);
        mainLayout.getChildAt(currentResult-1).setBackgroundColor(Color.GRAY);

        LinearLayout l_temp = (LinearLayout) mainLayout.getChildAt(currentResult-1);
        TextView t_temp = (TextView) l_temp.getChildAt(1);

        tts.speak((String) t_temp.getText());
    }

    public void swipe_right()
    {
        currentResult++;
        if(currentResult>resultCount)
        {
            tts.speak("처음 항목 입니다");
            mainLayout.getChildAt(currentResult-2).setBackgroundColor(Color.BLACK);
            currentResult=1;
            mainLayout.getChildAt(currentResult-1).setBackgroundColor(Color.GRAY);
            LinearLayout l_temp = (LinearLayout) mainLayout.getChildAt(0);
            TextView t_temp = (TextView) l_temp.getChildAt(1);

            tts.speak((String) t_temp.getText());
        }

        mainLayout.getChildAt(currentResult-2).setBackgroundColor(Color.BLACK);
        mainLayout.getChildAt(currentResult-1).setBackgroundColor(Color.GRAY);


        LinearLayout l_temp = (LinearLayout) mainLayout.getChildAt(currentResult-1);
        TextView t_temp = (TextView) l_temp.getChildAt(1);

        tts.speak((String) t_temp.getText());
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
       selectMenu();
    }

    public void tap_triple()
    {

    }


    public void tap_long()
    {

    }

    public void selectMenu()
    {

        if( currentResult==0 ) return;

        mainLayout.getChildAt(currentResult-1).setBackgroundColor(Color.GRAY);

        LinearLayout l_temp = (LinearLayout) mainLayout.getChildAt(currentResult-1);

        TextView name = (TextView) l_temp.getChildAt(1);
        TextView no = (TextView) l_temp.getChildAt(0);

        tts.speak(name.getText()+"길안내로 연결합니다");

        Intent naviIntent = new Intent(this,MapActivity.class);
        naviIntent.putExtra("mode", 1);
        naviIntent.putExtra("dest", Integer.parseInt((String) no.getText()));


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
        startActivity(naviIntent);

    }


    // 구글 플레이 서비스에 접속 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        //tts.speak("스마트워치가 연결 되었습니다.");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    // 구글 플레이 서비스에 접속이 일시정지 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int i) {
        //Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    // 구글 플레이 서비스에 접속을 실패했을 때 실행
    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        //tts.speak("스마트워치가 연결 되지 않았습니다.");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        if (messageEvent.getPath().equals("/MESSAGE_PATH")) {
            // 텍스트뷰에 적용 될 문자열을 지정한다.
            final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);

            // UI 스레드를 실행하여 텍스트 뷰의 값을 수정한다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mTextView.setText(msg);
                    //Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT);
                    Log.d("ges", msg);
                    // mEditText.setText(msg);

                    switch (msg) {
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
