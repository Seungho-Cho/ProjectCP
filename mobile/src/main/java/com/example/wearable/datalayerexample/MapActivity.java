package com.example.wearable.datalayerexample;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;

import java.util.LinkedList;


public class MapActivity extends NMapActivity implements

        GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener,
        GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener,
        DataApi.DataListener,
        SensorEventListener,
        GestureDetector.OnGestureListener,
        GestureValue
{

    private static final String LOG_TAG = "MapActivity";
    private static final boolean DEBUG = false;

    // set your API key which is registered for NMapViewer library.
    private static final String API_KEY = "60ae01f85da0ac8f13157b103ad665eb";

    private NMapView mMapView;
    private NMapController mMapController;

    private NMapViewerResourceProvider mMapViewerResourceProvider;
    private NMapOverlayManager mOverlayManager;

    private NMapMyLocationOverlay mMyLocationOverlay;
    private NMapLocationManager mMapLocationManager;
    private NMapCompassManager mMapCompassManager;

    private TTSAdapter tts;
    private Intent mIntent;

    SensorManager sensorM;



    private GestureDetector gestureDetector;    // 제스처 처리
    private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체



    private long tap_time = 0;
    private int tap_count = 0;
    private int tap_on = 0;
    private int long_press_on = 0;

    LinearLayout main_Layout;

    GNodeList GNodes = new GNodeList();
    MapGraph GMap = new MapGraph(59);

    LinkedList<GNode> path = null, movePath = null;
    boolean isGuide = false, isComp = false;
    double[] location = new double[2]; // [0]:longitude, [1]:latitude
    int destination = 0;
    float comp = 0; //compass

    Handler mHandler;

    /////////////////////  제스처 디텍터 ///////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("onTouch", "onTouch");
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    ///////////////////  Intent //////////
    Intent intent;
    private int mode;
    private int filter;
    private static final int MODE_NAVI    = 1;
    private static final int MODE_SA      = 2;
    //////////////////////////////////////


    /////////////////test 용 텍스트뷰/////////////////////////

    TextView lonText, latText, radText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        tts = new TTSAdapter(this);
        gestureDetector = new GestureDetector(this);

        // get Intent
        intent = this.getIntent();

        mHandler = new Handler();

        mode = intent.getIntExtra("mode",0);

        if(mode == MODE_NAVI)
        {
            destination = intent.getIntExtra("dest", -1);
        }
        else if(mode == MODE_SA)
        {
            filter = intent.getIntExtra("filter",-1);
        }

        // create map view
        mMapView = (NMapView) findViewById(R.id.mapView);
        main_Layout = (LinearLayout) findViewById(R.id.mapLayout);

        main_Layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        //mMapView = new NMapView(this);

        // 구글 플레이 서비스 객체를 시계 설정으로 초기화
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();





        // set a registered API key for Open MapViewer Library
        mMapView.setApiKey(API_KEY);

        // set the activity content to the map view
        //setContentView(mMapView);

        // initialize map view
        mMapView.setClickable(false);

        // register listener for map state changes
        mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
        mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);

        // use map controller to zoom in/out, pan and set map center, zoom level etc.
        mMapController = mMapView.getMapController();

        // create resource provider
        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

        // create overlay manager
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);

        // location manager
        mMapLocationManager = new NMapLocationManager(this);
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

        // compass manager
        mMapCompassManager = new NMapCompassManager(this);

        // create my location overlay
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);

        // compass class
        /*
        sensorM = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorM.registerListener(this,//Activity가 직접 리스너를 구현
                sensorM.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        */

        for (int i = 0; i < 59; i++) {
            GMap.insertVertex(i, GNodes.GNodeArr[i]);
        }

        GMap.insertEdge(0, 1, 1);
        GMap.insertEdge(1, 2, 1);
        GMap.insertEdge(2, 3, 1);
        GMap.insertEdge(2, 4, 1);
        GMap.insertEdge(4, 5, 1);
        GMap.insertEdge(4, 6, 1);
        GMap.insertEdge(5, 18, 1);
        GMap.insertEdge(6, 7, 1);
        GMap.insertEdge(6, 8, 1);
        GMap.insertEdge(8, 9, 1);
        GMap.insertEdge(8, 10, 1);
        GMap.insertEdge(8, 11, 1);
        GMap.insertEdge(8, 16, 1);
        GMap.insertEdge(11, 12, 1);
        GMap.insertEdge(11, 13, 1);
        GMap.insertEdge(11, 15, 1);
        GMap.insertEdge(13, 14, 1);
        GMap.insertEdge(15, 22, 1);
        GMap.insertEdge(16, 17, 1);
        GMap.insertEdge(16, 19, 1);
        GMap.insertEdge(17, 18, 1);
        GMap.insertEdge(17, 24, 1);
        GMap.insertEdge(18, 26, 1);
        GMap.insertEdge(19, 20, 1);
        GMap.insertEdge(19, 21, 1);
        GMap.insertEdge(19, 22, 1);
        GMap.insertEdge(21, 25, 1);
        GMap.insertEdge(22, 23, 1);
        GMap.insertEdge(22, 31, 1);
        GMap.insertEdge(24, 25, 1);
        GMap.insertEdge(24, 29, 1);
        GMap.insertEdge(25, 30, 1);
        GMap.insertEdge(26, 27, 1);
        GMap.insertEdge(26, 28, 1);
        GMap.insertEdge(26, 38, 1);
        GMap.insertEdge(28, 29, 1);
        GMap.insertEdge(29, 34, 1);
        GMap.insertEdge(30, 31, 1);
        GMap.insertEdge(30, 36, 1);
        GMap.insertEdge(31, 32, 1);
        GMap.insertEdge(31, 33, 1);
        GMap.insertEdge(31, 45, 1);
        GMap.insertEdge(34, 35, 1);
        GMap.insertEdge(34, 41, 1);
        GMap.insertEdge(35, 36, 1);
        GMap.insertEdge(35, 37, 1);
        GMap.insertEdge(37, 42, 1);
        GMap.insertEdge(38, 39, 1);
        GMap.insertEdge(39, 40, 1);
        GMap.insertEdge(41, 42, 1);
        GMap.insertEdge(41, 46, 1);
        GMap.insertEdge(42, 43, 1);
        GMap.insertEdge(42, 49, 1);
        GMap.insertEdge(43, 44, 1);
        GMap.insertEdge(43, 45, 1);
        GMap.insertEdge(45, 51, 1);
        GMap.insertEdge(46, 47, 1);
        GMap.insertEdge(46, 48, 1);
        GMap.insertEdge(48, 49, 1);
        GMap.insertEdge(49, 50, 1);
        GMap.insertEdge(50, 51, 1);
        GMap.insertEdge(50, 53, 1);
        GMap.insertEdge(51, 52, 1);
        GMap.insertEdge(51, 55, 1);
        GMap.insertEdge(53, 54, 1);
        GMap.insertEdge(53, 56, 1);
        GMap.insertEdge(55, 58, 1);
        GMap.insertEdge(56, 57, 1);
        GMap.insertEdge(56, 58, 1);

        location[0] = location[1] = 0;
        comp = -1;

        ///////////////////////////////////////
        // test

        lonText = (TextView) findViewById(R.id.lonText);
        latText = (TextView) findViewById(R.id.latText);
        radText = (TextView) findViewById(R.id.radText);
        switchGPS();

        Thread thred = new Thread()
        {
            public void run()
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cp_TTS("잠시만 기다려주세요");
                while(location[0] == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cp_TTS("시계를 들고, 화면을 길게 눌러주세요");
                getWatchComp();
                while(comp == -1) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                switch(mode) {
                    case MODE_NAVI:
                        //isGuide = true;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                guideStart();
                            }
                        });
                        break;

                    case MODE_SA:
                        /*
                        switchComp();
                        while(comp == -1) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        */
                        spaceAware();
                        break;

                    default:

                        break;
                }
            }
        };
        thred.start();
    }


   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }*/

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
    protected void onDestroy() {
        if(isComp)
            switchComp();
        switchGPS();
        mOverlayManager.clearOverlays();
        tts.destroy();
        super.onDestroy();
    }

    private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

        @Override
        public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {

            if (errorInfo == null) { // success
                // restore map view state such as map center position and zoom level.
                //restoreInstanceState();
                mMapController.setMapCenter(new NGeoPoint(126.7335061, 37.3400342), 13);

            } else { // fail
                Log.e(LOG_TAG, "onFailedToInitializeWithError: " + errorInfo.toString());

                Toast.makeText(MapActivity.this, errorInfo.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onAnimationStateChange(NMapView mapView, int animType, int animState) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onAnimationStateChange: animType=" + animType + ", animState=" + animState);
            }
        }

        @Override
        public void onMapCenterChange(NMapView mapView, NGeoPoint center) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onMapCenterChange: center=" + center.toString());
            }
        }

        @Override
        public void onZoomLevelChange(NMapView mapView, int level) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onZoomLevelChange: level=" + level);
            }
        }

        @Override
        public void onMapCenterChangeFine(NMapView mapView) {

        }

    };

    private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

        @Override
        public void onLongPress(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLongPressCanceled(NMapView mapView) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSingleTapUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTouchDown(NMapView mapView, MotionEvent ev) {

        }

        @Override
        public void onScroll(NMapView mapView, MotionEvent e1, MotionEvent e2) {
        }

        @Override
        public void onTouchUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

    };

    /* MyLocation Listener */
    // GPS 위치 받는 리스너
    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

        @Override
        public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {

            if (mMapController != null) {
                //mMapController.animateTo(myLocation);
                location[0] = myLocation.getLongitude();
                location[1] = myLocation.getLatitude();

                // debugging용 좌표 출력
                lonText.setText(Double.toString(location[0]));
                latText.setText(Double.toString(location[1]));

                if (isGuide) {
                    /*
                    if(path == null) {
                        int closest = cp_getClosest(location, destination); // 인접 노드 찾기
                        GMap.executeDijk(closest); // 다익스트라 생성
                        path = GMap.getPath(destination); // 링크드리스트 생성
                        movePath = (LinkedList<GNode>) path.clone(); // 중간 경로용 클론 생성
                        path.addFirst(new GNode(99, 32, null, null, location[0], location[1])); // 현재 위치 삽입
                        cp_TTS("안내를 시작합니다"); // 안내 시작
                    }
                    */
                    mOverlayManager.clearOverlays();
                    cp_gNodeMovePath(movePath);
                    cp_checkGuide();
                    cp_checkPath();
                }
            }

            return true;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

            // stop location updating
            //			Runnable runnable = new Runnable() {
            //				public void run() {
            //					stopMyLocation();
            //				}
            //			};
            //			runnable.run();

            Toast.makeText(MapActivity.this, "Your current location is temporarily unavailable.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {

            Toast.makeText(MapActivity.this, "Your current location is unavailable area.", Toast.LENGTH_LONG).show();

            stopMyLocation();
        }

    };

    /*
    private final NMapCompassManager.OnCompassChangeListener onMyCompassChangeListener = new NMapCompassManager.OnCompassChangeListener() {
        @Override
        public boolean onSensorChanged(NMapCompassManager nMapCompassManager, float v) {
            comp = nMapCompassManager.getHeading();
            return true;
        }
    };
    */
    private void stopMyLocation() {
        if (mMyLocationOverlay != null) {
            mMapLocationManager.disableMyLocation();
            /*
            if (mMapView.isAutoRotateEnabled()) {
                //mMyLocationOverlay.setCompassHeadingVisible(false);

                mMapCompassManager.disableCompass();

                mMapView.setAutoRotateEnabled(false, false);

                //mMapContainerView.requestLayout();
            }
            */
            //mMapCompassManager.disableCompass();
            mOverlayManager.removeMyLocationOverlay();
        }
    }

    // compass sensor changed
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION:
                comp = event.values[0];
                Float Comp;
                Comp = comp;
                radText.setText("스마트폰 : " + Comp.toString());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // custom
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void switchGPS() {
        //item.setFloatingMode(NMapPOIitem.FLOATING_FIXED);
        if (mMyLocationOverlay != null) {
            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
                mOverlayManager.addOverlay(mMyLocationOverlay);
            }

            if (mMapLocationManager.isMyLocationEnabled()) {
                /*
                if (!mMapView.isAutoRotateEnabled()) {
                    mMyLocationOverlay.setCompassHeadingVisible(true);

                    mMapCompassManager.enableCompass();

                    mMapView.setAutoRotateEnabled(true, false);

                    //mMapContainerView.requestLayout();
                } else {
                    stopMyLocation();
                }
                */
                stopMyLocation();

                mMapView.postInvalidate();
            } else {
                boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
                if (!isMyLocationEnabled) {
                    Toast.makeText(MapActivity.this, "GPS를 설정해주세요",
                            Toast.LENGTH_LONG).show();

                    Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(goToSettings);

                    return;
                }
            }
        }
    }

    void switchComp() {
        if (isComp == false) {
            sensorM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensorM.registerListener(this,//Activity가 직접 리스너를 구현
                    sensorM.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
            isComp = true;
        } else {
            sensorM = null;
            isComp = false;
        }
    }

    // 배열 사용 경로 출력 함수
    /*
    private void cp_gNodePathdataOverlay( GNode[] arr ) {

        // set POI data
        NMapPathData pathData = new NMapPathData(arr.length);

        pathData.initPathData();
        for(int i=0; i<arr.length; i++) {
            if( arr[i] == null )
                continue;
            pathData.addPathPoint(arr[i].lon, arr[i].lat, 0);
        }
        pathData.endPathData();

        NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
        pathDataOverlay.showAllPathData(0);
    }
    */
    // 링크드리스트 사용 경로 출력 함수
    private void cp_gNodePathdataOverlay(LinkedList<GNode> path) {

        // set POI data
        NMapPathData pathData = new NMapPathData(path.size());
        /*
        Toast.makeText(MapActivity.this, new Integer(path.size()).toString(),
                Toast.LENGTH_SHORT).show();
        */
        pathData.initPathData();
        for (GNode node : path) {
            pathData.addPathPoint(node.lon, node.lat, 0);
        }
        pathData.endPathData();

        NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
        pathDataOverlay.showAllPathData(0);
    }

    // POI 출력 테스트 함수
    private void cp_gNodePOIdataOverlay(GNode[] arr) {

        // set POI data
        NMapPOIdata poiData = new NMapPOIdata(arr.length, mMapViewerResourceProvider, true);
        poiData.beginPOIdata(arr.length);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null)
                continue;
            poiData.addPOIitem(arr[i].lon, arr[i].lat, null, NMapPOIflagType.NUMBER_BASE + i, null);
        }
        poiData.endPOIdata();

        // create POI data overlay
        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
    }


    // 경로 POI 출력 함수
    private void cp_gNodeMovePath(LinkedList<GNode> path) {

        // set POI data
        NMapPathData pathData = new NMapPathData(path.size() + 1);

        //Toast.makeText(MapActivity.this, new Integer(path.size() + 1).toString(),
        //        Toast.LENGTH_LONG).show();

        pathData.initPathData();
        pathData.addPathPoint(location[0]/*lon*/, location[1]/*lang*/, 0);
        for (GNode node : path) {
            pathData.addPathPoint(node.lon, node.lat, 0);
        }
        pathData.endPathData();

        NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
        pathDataOverlay.showAllPathData(0);
    }

    // 가장 가까운 노드 찾는 함수
    private int cp_getClosest(double[] location, int to) {
        int closest;
        double cloDis, tempDis;
        NGeoPoint locNP = new NGeoPoint(location[0], location[1]);

        closest = 0;
        cloDis = NGeoPoint.getDistance(locNP, new NGeoPoint(GNodes.GNodeArr[closest].lon, GNodes.GNodeArr[closest].lat));
        for (int i = 1; i < GMap.max; i++) {
            tempDis = NGeoPoint.getDistance(locNP, new NGeoPoint(GNodes.GNodeArr[i].lon, GNodes.GNodeArr[i].lat));
            if (cloDis > tempDis) {
                closest = i;
                cloDis = tempDis;
            }
        }

        return closest;
    }

    // 길안내 이동 경로 확인 함수
    private boolean cp_checkPath() {

        double dis, dir, timeDir;
        int disInt;
        GNode prev = null, next = movePath.peekFirst();


        NGeoPoint locNP = new NGeoPoint(location[0], location[1]);
        NGeoPoint toNP = new NGeoPoint(next.lon, next.lat);

        dis = NGeoPoint.getDistance(locNP, toNP);

        if (dis < 5) {
            if (next == movePath.peekLast()) {
                path = null;
                movePath = null;
                isGuide = false;
                cp_TTS("목적지에 도착했습니다");
                //finish();
            } else {
                for (GNode node : path) {
                    if (node == next) {
                        break;
                    }
                    prev = node;
                }
                dir = getMovComp(prev, movePath.pollFirst(), next = movePath.peekFirst());
                timeDir = (dir + 15) / 30;
                if (timeDir == 0)
                    timeDir = 12;
                disInt = (int)NGeoPoint.getDistance(toNP, new NGeoPoint(next.lon, next.lat));
                tts.speak("다음 지점까지" + timeDir + "시 방향으로" + disInt + "미터");
                //Toast.makeText(MapActivity.this, "Please enable a My Location source in system settings",
                //        Toast.LENGTH_SHORT).show();
                //movePath.pollFirst();
            }

            return true;
        }
        return false;
    }

    // 길안내 경로 이탈 확인 함수
    private boolean cp_checkGuide() {
        if (path == null)
            return false;

        double prevDis, nextDis, linDis;
        GNode prev = null, next;
        NGeoPoint locNP = new NGeoPoint(location[0], location[1]);
        next = movePath.peekFirst();

        for (GNode node : path) {
            if (node == next) {
                break;
            }
            prev = node;
        }
        prevDis = NGeoPoint.getDistance(locNP, new NGeoPoint(prev.lon, prev.lat));
        nextDis = NGeoPoint.getDistance(locNP, new NGeoPoint(next.lon, next.lat));
        linDis = NGeoPoint.getDistance(new NGeoPoint(prev.lon, prev.lat), new NGeoPoint(next.lon, next.lat));
        if ((linDis * 1.5) < (prevDis + nextDis)) {
            cp_TTS("경로를 이탈하였습니다");
            return false;
        }

        return true;
    }

    // 회전 각도 구하는 함수
    private double getMovComp(GNode p1, GNode p2, GNode p3) {
        double deg;

        double x1, y1, x2, y2;

        x1 = p2.lon - p1.lon;
        y1 = p2.lat - p1.lat;
        x2 = p3.lon - p2.lon;
        y2 = p3.lat - p2.lat;

        /*
        x1 = NGeoPoint.getDistance(new NGeoPoint(p1.lon, p1.lat), new NGeoPoint(p2.lon, p1.lat));
        y1 = NGeoPoint.getDistance(new NGeoPoint(p1.lon, p1.lat), new NGeoPoint(p1.lon, p2.lat));
        x2 = NGeoPoint.getDistance(new NGeoPoint(p2.lon, p2.lat), new NGeoPoint(p3.lon, p2.lat));
        y2 = NGeoPoint.getDistance(new NGeoPoint(p2.lon, p2.lat), new NGeoPoint(p2.lon, p3.lat));
        */
        deg = Math.toDegrees(Math.asin(((x1 * y2) - (x2 * y1)) / (Math.sqrt((x1 * x1) + (y1 * y1)) * Math.sqrt((x2 * x2) + (y2 * y2)))));
        //deg = Math.toDegrees(Math.atan(x1/y1)-Math.atan(x2/y2));

        //radText.setText(new Double(deg).toString());

        if(deg < 0)
            deg = 0 - deg;
        else
            deg = 360 - deg;

        return deg;
    }

    //두 좌표 북위 각도 차 구하는 함수
    private double getPtoPComp(double slon, double slat, double dlon, double dlat) {
        double comDir, rad;

        rad = Math.atan2((dlon - slon), (dlat - slat));
        comDir = rad * (180 / Math.PI);

        if(slon > dlon)
            comDir = 360 + comDir;

        return comDir;
    }

    private void cp_TTS(String msg) {
        tts.speak(msg);
    }

    //////////////////////////////////////////////////////////////////////////////////
    // 메인 기능 동작 함수
    /////////////////////////////////////////////////////////////////////////////////

    // 길 안내 함수
    void guideStart() {
        if (isGuide) {
            isGuide = false;
            mOverlayManager.clearOverlays();
            path = null;
        }
        else {
            if (!mMapLocationManager.isMyLocationEnabled()) {
                cp_TTS("GPS가 설정되어 있지 않습니다");
                finish();
            }

            isGuide = true;

            int closest = cp_getClosest(location, destination); // 인접 노드 찾기
            GMap.executeDijk(closest); // 다익스트라 생성
            path = GMap.getPath(destination); // 링크드리스트 생성
            movePath = (LinkedList<GNode>) path.clone(); // 중간 경로용 클론 생성
            path.addFirst(new GNode(99, 32, null, null, location[0], location[1])); // 현재 위치 삽입
            cp_TTS("안내를 시작합니다"); // 안내 시작
            cp_gNodeMovePath(movePath);

            GNode destN = movePath.peekFirst();
            double comDir, comDif;
            int comInt, timeDir, dis;

            comDir = getPtoPComp(location[0], location[1], destN.lon, destN.lat);
            comDif = comDir - comp;
            if(comDir < comp)
                comDif = comDif + 360;
            comInt = (int)comDif;
            timeDir = (comInt + 15) / 30;
            if(timeDir == 0)
                timeDir = 12;

            dis = (int)NGeoPoint.getDistance(new NGeoPoint(location[0], location[1]), new NGeoPoint(destN.lon, destN.lat));

            tts.speak(timeDir + "시 방향으로" + dis + "미터");
        }
    }

    // 공간 지각 확장 함수 space awareness functions
    void spaceAware() {

        if (!mMapLocationManager.isMyLocationEnabled()) {
            cp_TTS("GPS가 설정되어 있지 않습니다");
            finish();
        }

        if (isGuide)
            return;

        LinkedList<GNode> nodes = new LinkedList<GNode>();

        LinkedList<GNode>[] saNodes = new LinkedList[12];
        for(int i=0; i<12; i++) {
            saNodes[i] = new LinkedList<GNode>();
        }

        double lon, lat;
        double compass;

        NGeoPoint locNP;

        lon = location[0];
        lat = location[1];
        compass = (double)comp;
        locNP = new NGeoPoint(lon, lat);

        for(int i=0; i<GMap.max; i++) {
            if(GNodes.GNodeArr[i].category == filter)
                nodes.add(GNodes.GNodeArr[i]);
        }

        cp_TTS("공간지각확장을 시작합니다");

        for (GNode node : nodes) {
            double comDir, comDif;
            int comInt, timeDir;

            comDir = getPtoPComp(lon, lat, node.lon, node.lat);

            comDif = comDir - compass;
            if(comDir < compass)
                comDif = comDif + 360;

            comInt = (int)comDif;
            timeDir = (comInt + 15) / 30;
            if(timeDir == 12)
                timeDir = 0;

            saNodes[timeDir].add(node);
        }

        for(int i=1; i<12; i++) {
            for(GNode node : saNodes[i]) {
                int dis = (int)NGeoPoint.getDistance(locNP, new NGeoPoint(node.lon, node.lat));
                dis = dis - (dis%10);
                cp_TTS((i) + "시 방향" + dis + "미터" + node.Name);
            }
        }
        for(GNode node : saNodes[0]) {
            int dis = (int)NGeoPoint.getDistance(locNP, new NGeoPoint(node.lon, node.lat));
            dis = dis - (dis%10);
            cp_TTS((12) + "시 방향" + dis + "미터" + node.Name);
        }

    }

    //각도 test
    void spaceAware2() {

        if (!mMapLocationManager.isMyLocationEnabled()) {
            cp_TTS("GPS가 설정되어 있지 않습니다");
            finish();
        }

        if (isGuide)
            return;

        LinkedList<GNode> nodes = new LinkedList<GNode>();

        LinkedList<String>[] saNodes = new LinkedList[12];
        for(int i=0; i<12; i++) {
            saNodes[i] = new LinkedList<String>();
        }

        double lon, lat;
        double compass, comDir, comSA;
        int comInt, timeDir;

        double rad;

        lon = location[0];
        lat = location[1];
        compass = (double)comp;

        for(int i=0; i<GMap.max; i++) {
            if(GNodes.GNodeArr[i].category == filter)
                nodes.add(GNodes.GNodeArr[i]);
        }

        cp_TTS("테스트 시작");

        GNode node = GNodes.GNodeArr[4];

        rad = Math.atan2((node.lon - lon), (node.lat - lat));
        comDir = rad * (180 / Math.PI);

        if(lon > node.lon)
            comDir = 360 + comDir;


        comInt = (int)comDir;
        cp_TTS(comInt + "도");

        comSA = comDir - compass;
        if(comDir < compass)
            comSA = comSA + 360;

        comInt = (int)comSA;
        timeDir = (comInt + 15) / 30;

        saNodes[timeDir].add(node.Name);

    }


    ///////////////////////////////////////////////////////////
    // 테스트용 버튼 onClick 함수
    ///////////////////////////////////////////////////////////

    // POI, Path 모두 지우기
    public void onClickCancel(View arg0) {
        mOverlayManager.clearOverlays();
        if (isComp)
            switchComp();
        if (mMapLocationManager.isMyLocationEnabled())
            switchGPS();
    }

    public void onClickPathPOI(View arg0) {
        //item.setFloatingMode(NMapPOIitem.FLOATING_DISPATCH);
        /*
        // use array
        GNode[] arr = {
                GNodes.GNodeArr[1],
                GNodes.GNodeArr[2],
                GNodes.GNodeArr[4],
                GNodes.GNodeArr[5],
                GNodes.GNodeArr[18],
                GNodes.GNodeArr[26],
                GNodes.GNodeArr[38],
                GNodes.GNodeArr[39],
                GNodes.GNodeArr[40]
        };
        cp_gNodePathdataOverlay(arr);
        */

        // use LinkedList
        cp_gNodePathdataOverlay(path);
    }

    public void onClickPOI(View arg0) {
        //item.setFloatingMode(NMapPOIitem.FLOATING_FIXED);
        cp_gNodePOIdataOverlay(GNodes.GNodeArr);
    }

    public void onClickGuide(View arg0) {
        //item.setFloatingMode(NMapPOIitem.FLOATING_FIXED);
        if (isGuide) {
            isGuide = false;
            mOverlayManager.clearOverlays();
            path = null;
        }
        else {
            if (!mMapLocationManager.isMyLocationEnabled()) {
                switchGPS();
                return;
            }

            isGuide = true;
        }
    }


    //////////////////////////////////////////////////////////////////////
    // 좃승호
    /////////////////////////////////////////////////////////////////////

    public void getWatchComp() {
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
                            String message = "comp_mode_on";
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

           // Toast.makeText(getApplication(), resultString, Toast.LENGTH_SHORT).show();
        }
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
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //cp_TTS("테스트");
        Log.d("tap", "Long");
        if(!isGuide)
            switchComp();

        else if(isComp) {

            GNode destN = movePath.peekFirst();
            double comDir, comDif;
            int comInt, timeDir, dis;

            comDir = getPtoPComp(location[0], location[1], destN.lon, destN.lat);
            comDif = comDir - comp;
            if (comDir < comp)
                comDif = comDif + 360;
            comInt = (int) comDif;
            timeDir = (comInt + 15) / 30;
            if (timeDir == 0)
                timeDir = 12;

            dis = (int) NGeoPoint.getDistance(new NGeoPoint(location[0], location[1]), new NGeoPoint(destN.lon, destN.lat));

            tts.speak(timeDir + "시 방향으로" + dis + "미터");
        }
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


    public void swipe_left() {

    }

    public void swipe_right() {

    }

    public void swipe_up() {
        finish();
    }

    public void swipe_down() {

    }

    public void tap_single() {

    }

    public void tap_double() {

    }

    public void tap_triple() {

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


    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.d("send", "send");

        if (!messageEvent.getPath().equals("/MESSAGE_PATH")) {

            final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);

            // UI 스레드를 실행
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.d("degree",msg);

                    float degree = Float.parseFloat(msg);
                    comp = degree;

                    radText.setText("스마트워치 : " + (new Float(comp).toString()));

                    if(isGuide) {

                        GNode destN = movePath.peekFirst();
                        double comDir, comDif;
                        int comInt, timeDir, dis;

                        comDir = getPtoPComp(location[0], location[1], destN.lon, destN.lat);
                        comDif = comDir - comp;
                        if (comDir < comp)
                            comDif = comDif + 360;
                        comInt = (int) comDif;
                        timeDir = (comInt + 15) / 30;
                        if (timeDir == 0)
                            timeDir = 12;

                        dis = (int) NGeoPoint.getDistance(new NGeoPoint(location[0], location[1]), new NGeoPoint(destN.lon, destN.lat));

                        tts.speak(timeDir + "시 방향으로" + dis + "미터");
                    }
                }
            });
        }
    }

    @Override
    public void onPeerConnected(Node node) {

    }

    @Override
    public void onPeerDisconnected(Node node) {

    }


}