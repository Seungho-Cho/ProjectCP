package com.example.wearable.datalayerexample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainMenuActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener,
        GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener,
        DataApi.DataListener,
        GestureDetector.OnGestureListener
{
    private EditText mEditText; // 시계로 전송 할 텍스트뷰

    private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체

    private ViewFlipper menuFlipper;
   // private Handler mHandler;

    private static final int SWIPE_MIN_DISTANCE = 120;          // 제스처 최소 거리
    private static final int SWIPE_MAX_OFF_PATH = 2000;         // 제스처 최대 거리
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;    // 제스처 인식 속도
    private GestureDetector gestureDetector;    // 제스처 처리

    private long tap_time = 0;
    private int tap_count = 0;
    private int tap_on = 0;
    private int long_press_on = 0;

    View main_layout;
    TTSAdapter tts;
    int current_menu = 1;
    String current_menu_name = "빠른 길찾기";
    int needHelp = 1;


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d("onTouch","onTouch");
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy()
    {
        tts.destroy();
        super.onDestroy();
    }

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        gestureDetector = new GestureDetector(this);

        tts = new TTSAdapter(this);

        Thread thred = new Thread()
        {
            public void run()
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tts.speak("처음 화면 입니다 도움말을 들으시려면 화면을 꾹 눌러주세요");
                int needHelp = 0;
                speak_menu(current_menu);
            }
        };
        thred.start();

        main_layout =  findViewById(R.id.main_layout);
        main_layout.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
        //mHandler = new Handler();

        // 시계로 전송 할 텍스트뷰
        mEditText = (EditText) findViewById(R.id.text);
        menuFlipper = (ViewFlipper) findViewById(R.id.menuFlipper);

        // 구글 플레이 서비스 객체를 시계 설정으로 초기화
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    // 액티비티가 시작할 때 실행
    @Override // Activity
    protected void onStart() {
        super.onStart();

        // 구글 플레이 서비스에 접속돼 있지 않다면 접속한다.
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    // 액티비티가 종료될 때 실행
    @Override // Activity
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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
        //Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    // 구글 플레이 서비스에 접속을 실패했을 때 실행
    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        //tts.speak("스마트워치가 연결 되지 않았습니다.");
    }

    // Send Message 버튼을 클릭했을 때 실행
    public void onSendMessage(View view) {

        // 페어링 기기들을 지칭하는 노드를 가져온다.
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
            .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

                // 노드를 가져온 후 실행된다.
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult
                                             getConnectedNodesResult) {

                    // 노드를 순회하며 메시지를 전송한다.
                    for (final Node node : getConnectedNodesResult.getNodes()) {

                        // 전송할 메시지 텍스트 생성
                        String message = "Message : " + mEditText.getText();
                        byte[] bytes = message.getBytes();

                        // 메시지 전송 및 전송 후 실행 될 콜백 함수 지정
                        Wearable.MessageApi.sendMessage(mGoogleApiClient,
                                node.getId(), "/MESSAGE_PATH", bytes)
                                .setResultCallback(resultCallback);
                    }
                }
            });
    }

    // 시계로 데이터 및 메시지를 전송 후 실행되는 메소드
    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(Result result) {
            String resultString = "Sending Result : " + result.getStatus().isSuccess();

            Toast.makeText(getApplication(), resultString, Toast.LENGTH_SHORT).show();
        }
    };

    // 데이터 전송 횟수이다.
    // 텍스트가 같더라도 데이터가 계속 변할 수 있도록 count 값을 같이 보낸다.
    private int sendCount = 0;

    // Send Data String 버튼을 클릭했을 때 실행
    public void onSendDataString(View view) {
        // 전송할 텍스트를 생성한다.
        EditText editText = (EditText) findViewById(R.id.text);
        String text = "String Data : " + editText.getText().toString();

        // 시계로 전송할 데이터 묶음인 데이터 맵을 생성한다.
        PutDataMapRequest dataMap = PutDataMapRequest.create("/STRING_DATA_PATH");

        // 전송할 텍스트를 지정한다.
        dataMap.getDataMap().putString("sendString", text);

        // 현재 보내는 텍스트와 지난번 보냈던 텍스트가 같으면
        // onDataChanged() 메소드가 실행되지 않는다.
        // 텍스트가 같더라도 데이터가 계속 변할 수 있도록 count 값을 같이 보낸다.
        dataMap.getDataMap().putInt("count", sendCount++);

        // 데이터 맵으로 전송할 요청 객체를 생성한다.
        PutDataRequest request = dataMap.asPutDataRequest();

        // 데이터 전송 및 전송 후 실행 될 콜백 함수 지정
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(resultCallback);


        Intent intent = new Intent(this,TimeTableInputActivity.class);
        startActivity(intent);
    }

    // Send Data Image 버튼을 클릭했을 때 실행
    public void onSendDataImage(View view) {
        // 전송할 비트맵을 생성한다.
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_1);

        // 비트맵으로 전송 가능한 에셋(Asset)을 생성한다.
        Asset asset = createAssetFromBitmap(bitmap);

        // 시계로 전송할 데이터 묶음인 데이터 맵을 생성한다.
        PutDataMapRequest dataMap = PutDataMapRequest.create("/IMAGE_DATA_PATH");

        // 전송할 에셋을 지정한다.
        dataMap.getDataMap().putAsset("assetImage", asset);

        // 현재 보내는 텍스트와 지난번 보냈던 텍스트가 같으면
        // onDataChanged() 메소드가 실행되지 않는다.
        // 이미지가 같더라도 데이터가 계속 변할 수 있도록 count 값을 같이 보낸다.
        dataMap.getDataMap().putInt("sendCount", sendCount++);
        PutDataRequest request = dataMap.asPutDataRequest();

        // 데이터 전송 및 전송 후 실행 될 콜백 함수 지정
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(resultCallback);
    }

    // 비트맵을 사용해 에셋을 생성한다.
    private Asset createAssetFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
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
                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT);
                    Log.d("ges",msg);
                    mEditText.setText(msg);

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

    public void swipe_left()
    {
        NextMenu();
    }

    public void swipe_right()
    {
        PrevMenu();
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
        SelectMenu();
    }

    public void tap_triple()
    {

    }
    public void tap_long()
    {
        tts.speak("처음 메뉴 입니다. 어플의 각 기능에 접근할 수 있으며, 화면을 좌 우로 움직여 기능을 고르고 두번 눌러 기능을 선택합니다");
    }

    public void NextMenu()
    {
        menuFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_from_right));
        menuFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.out_from_right));
        menuFlipper.showNext();
        current_menu++;
        if(current_menu==5)
        {
            tts.speak("처음 항목 입니다.");
            current_menu=1;
        }
        speak_menu(current_menu);


    }
    public void PrevMenu()
    {
        menuFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_from_left));
        menuFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.out_from_left));
        menuFlipper.showPrevious();
        current_menu--;
        if(current_menu==0) current_menu=4;
        speak_menu(current_menu);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(needHelp==1) return;
        Thread thred = new Thread()
        {
            public void run()
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tts.speak("처음 화면 입니다");
                speak_menu(current_menu);
            }
        };
        thred.start();
    }

    public void SelectMenu()
    {
        tts.speak(current_menu_name+"를 선택 하였습니다");
        Intent intent = null;
        switch(current_menu)
        {
            case 1:
                intent = new Intent(this,MapActivity.class);
                break;
            case 2:
                intent = new Intent(this,SearchSelectActivity.class);
                break;
            case 3:
                break;
            case 4:
                intent = new Intent(this,TimeTableInputActivity.class);
                break;
        }
        if(intent!=null) startActivity(intent);
        else return;
    }
    public void ReturnMenu()
    {

    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onPeerConnected(Node node) {

    }

    @Override
    public void onPeerDisconnected(Node node) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
            tap_long();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //SendGes("SINGLE_TAP");
        if(tap_count==2)
        {
            Log.d("tap","triple");
            if(System.currentTimeMillis() <= tap_time + 2000)
            {
                tap_triple();
                tap_count=0;
                tap_on = 0;
                return true;
            }
        }
        if(tap_count==1)
        {
            Log.d("tap","double");
            tap_count=2;
            if(System.currentTimeMillis() <= tap_time + 2000)
            {
                Thread thread = new Thread()
                {
                    public void run()
                    {
                        tap_on = 2;
                        try
                        {
                            Thread.sleep(2000);
                            if(tap_on==2)
                            {
                                tap_double();
                                tap_count=0;
                            }

                        } catch (InterruptedException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                };
                thread.start();
                return true;
            }
        }
        if(tap_count==0)
        {
            Log.d("tap","single");
            if (System.currentTimeMillis() > tap_time + 2000)
            {
                tap_time = System.currentTimeMillis();
                tap_count=1;

                Thread thread = new Thread()
                {
                    public void run()
                    {
                        tap_on = 1;
                        try
                        {
                            Thread.sleep(2000);
                            if(tap_on==1)
                            {
                                tap_single();
                                tap_count=0;
                            }

                        } catch (InterruptedException e1)
                        {
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
    public void onLongPress(MotionEvent e)
    {
        if(long_press_on ==0 )
        {
            Thread thread = new Thread()
            {
                public void run()
                {
                    long_press_on = 1;
                    tap_long();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    long_press_on = 0;
                }
            };
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        try
        {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                //Toast.makeText(this, "Left Swipe", Toast.LENGTH_SHORT).show();
                swipe_left();
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                //Toast.makeText(this, "Right Swipe", Toast.LENGTH_SHORT).show();
                swipe_right();
            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
            {
                //Toast.makeText(this, "Swipe up", Toast.LENGTH_SHORT).show();
                swipe_up();
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
            {
                //Toast.makeText(this, "Swipe down", Toast.LENGTH_SHORT).show();
                swipe_down();
            }
        } catch (Exception e) {  }
        return true;
    }

    public void speak_menu(int i)
    {
        switch(i)
        {
            case 1:
                tts.speak("빠른 길찾기");
                current_menu_name = "빠른 길찾기";
                break;
            case 2:
                tts.speak("일반 길찾기");
                current_menu_name = "일반 길찾기";
            break;
            case 3:
                tts.speak("공간지각 확장");
                current_menu_name = "공간지각 확장";
            break;
            case 4:
                tts.speak("시간표 확인");
                current_menu_name = "시간표 확인";
            break;
        }
    }
}
