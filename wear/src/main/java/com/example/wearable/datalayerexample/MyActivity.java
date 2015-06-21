package com.example.wearable.datalayerexample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;

public class MyActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener,
        MessageApi.MessageListener,
        DataApi.DataListener,
        GestureDetector.OnGestureListener,
        SensorEventListener
{

    private TextView mTextView; // 텍스트를 출력할 뷰

    @Override
    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    private View mLayout; // 배경을 출력할 레이아웃

    private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체

    private static final int SWIPE_MIN_DISTANCE = 120;          // 제스처 최소 거리
    private static final int SWIPE_MAX_OFF_PATH = 2000;         // 제스처 최대 거리
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;    // 제스처 인식 속도

    private GestureDetector gestureDetector;    // 제스처 처리

    String send_msg="";

    private long tap_time = 0;
    private int tap_count = 0;
    private int tap_on = 0;




    private ImageView compassImage;         // 컴퍼스 이미지 뷰
    private float currentDegree = 0f;      // 현재 각도
    private SensorManager mSensorManager;   // 센서 매니저
    TextView viewDegree;                      // 각도 텍스트 표시

    private int send_comp_mode = 1;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d("onTouch","onTouch");
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
        // 터치 입력을 제스처 디텍터에 전달
    }

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);


        gestureDetector = new GestureDetector(this);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);




        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mLayout = stub.findViewById(R.id.layout);
                compassImage = (ImageView) stub.findViewById(R.id.img_compass);
                viewDegree = (TextView) stub.findViewById(R.id.degreeView);

                mLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        gestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
            }
        });

        // 구글 플레이 서비스 객체를 시계 설정으로 초기화
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    // 액티비티가 시작할 때 실행
    @Override // Activity
    protected void onStart()
    {
        super.onStart();

        // 구글 플레이 서비스에 접속돼 있지 않다면 접속한다.
        if (!mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.connect();
        }
    }

    // 액티비티가 종료될 때 실행
    @Override // Activity
    protected void onStop()
    {
        // 구글 플레이 서비스 접속 해제
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    // 구글 플레이 서비스에 접속 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle)
    {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        // 노드, 메시지, 데이터 이벤트를 활용할 수 있도록 이벤트 리스너 지정
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    // 구글 플레이 서비스에 접속이 일시정지 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    // 구글 플레이 서비스에 접속을 실패했을 때 실행
    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();

        // 노드, 메시지, 데이터 이벤트 리스너 해제
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }

    // 페어링이 되면 실행된다.
    @Override // NodeApi.NodeListener
    public void onPeerConnected(Node node)
    {
        Toast.makeText(this, "Peer Connected", Toast.LENGTH_SHORT).show();
    }

    // 페어링이 해제되면 실행된다.
    @Override // NodeApi.NodeListener
    public void onPeerDisconnected(Node node)
    {
        Toast.makeText(this, "Peer Disconnected", Toast.LENGTH_SHORT).show();
    }

    // 메시지가 수신되면 실행되는 메소드
    @Override // MessageApi.MessageListener
    public void onMessageReceived(MessageEvent messageEvent)
    {
        if (messageEvent.getPath().equals("/MESSAGE_PATH"))
        {
            // 텍스트뷰에 적용 될 문자열을 지정한다.
            final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);

            if(msg.compareTo("comp_mode_on") == 0) {
                // UI 스레드를 실행하여 텍스트 뷰의 값을 수정한다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        send_comp_mode = 1;
                        //mTextView.setTextColor(Color.YELLOW);
                    }
                });
            }
        }
    }

    // 구글 플레이 서비스의 데이터가 변경되면 실행된다.
    @Override // DataApi.DataListener
    public void onDataChanged(DataEventBuffer dataEvents)
    {

        // 데이터 이벤트 횟수별로 동작한다.
        for (DataEvent event : dataEvents)
        {

            // 데이터 변경 이벤트일 때 실행된다.
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                // 동작을 구분할 패스를 가져온다.
                String path = event.getDataItem().getUri().getPath();

                // 패스가 각도 데이터 일 때
                if (path.equals("/MESSAGE_PATH"))
                {
                    // 이벤트 객체로부터 데이터 맵을 가져온다.
                    //DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());


                    send_comp_mode = 1;
                    mTextView.setTextColor(Color.YELLOW);
                }

            }
        }
    }

    private void SendComp(float comp)
    {
        final String send_comp = Float.toString(comp);
        Toast.makeText(getApplicationContext(), send_comp, Toast.LENGTH_SHORT).show();

                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                                for (final Node node : getConnectedNodesResult.getNodes()) {
                                    byte[] bytes = send_comp.getBytes();

                                    Wearable.MessageApi.sendMessage(mGoogleApiClient,
                                            node.getId(), "/COMP_DATA", bytes)
                                            .setResultCallback(resultCallback);
                                }
                            }
                        });

    }


    // 비트맵을 에셋(Asset)으로부터 생성한다.
    private Bitmap loadBitmapFromAsset(GoogleApiClient apiClient, Asset asset)
    {
        if (asset == null)
        {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        // 에셋을 구글 플레이 서비스로부터 받는다.
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                apiClient, asset).await().getInputStream();

        if (assetInputStream == null)
        {
            return null;
        }

        // 에셋으로부터 비트맵을 생성한다.
        return BitmapFactory.decodeStream(assetInputStream);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) { }

    @Override
    public boolean onSingleTapUp(MotionEvent e)  // 싱글 탭
    {
        //SendGes("SINGLE_TAP");
        if(tap_count==2)
        {
            Log.d("tap","triple");
            if(System.currentTimeMillis() <= tap_time + 1000)
            {
                SendGes("TRIPLE_TAP");
                tap_count=0;
                tap_on = 0;
                return true;
            }
        }
        if(tap_count==1)
        {
            Log.d("tap","double");
            tap_count=2;
            if(System.currentTimeMillis() <= tap_time + 1000)
            {
                Thread thread = new Thread()
                {
                    public void run()
                    {
                        tap_on = 2;
                        try
                        {
                            Thread.sleep(1000);
                            if(tap_on==2)
                            {
                                SendGes("DOUBLE_TAP");
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
            if (System.currentTimeMillis() > tap_time + 1000)
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
                           Thread.sleep(1000);
                           if(tap_on==1)
                           {
                               SendGes("SINGLE_TAP");
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
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)  // 롱 프레스
    {
        if(send_comp_mode == 1)
        {
            SendComp(Float.parseFloat((String)viewDegree.getText()));
        }
        else
        {
            SendGes("LONG_PRESS");
        }
    }

    // 스와이프 처리
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try
        {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                //Toast.makeText(this, "Left Swipe", Toast.LENGTH_SHORT).show();
                SendGes("LEFT_SWIPE");
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                //Toast.makeText(this, "Right Swipe", Toast.LENGTH_SHORT).show();
                SendGes("RIGHT_SWIPE");
            }
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
            {
                //Toast.makeText(this, "Swipe up", Toast.LENGTH_SHORT).show();
                SendGes("UP_SWIPE");
            }
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
            {
                //Toast.makeText(this, "Swipe down", Toast.LENGTH_SHORT).show();
                SendGes("DOWN_SWIPE");
            }
        } catch (Exception e) {  }
        return true;
    }

    private ResultCallback resultCallback = new ResultCallback()
    {
        @Override
        public void onResult(Result result) {
            String resultString = "Sending Result : " + result.getStatus().isSuccess();
            //Toast.makeText(getApplication(), resultString, Toast.LENGTH_SHORT).show();
        }
    };

    private void SendGes(String msg)
    {
        send_msg = msg;
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>()
                {

                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult)
                    {
                        for (final Node node : getConnectedNodesResult.getNodes())
                        {
                            byte[] bytes = send_msg.getBytes();

                            Wearable.MessageApi.sendMessage(mGoogleApiClient,
                                    node.getId(),"/MESSAGE_PATH",bytes )
                                    .setResultCallback(resultCallback);
                        }
                    }
                });
        //send_msg = "";
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        viewDegree.setText(Float.toString(degree));

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        compassImage.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}

