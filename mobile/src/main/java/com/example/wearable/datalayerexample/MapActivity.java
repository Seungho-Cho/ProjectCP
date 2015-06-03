package com.example.wearable.datalayerexample;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
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

import org.w3c.dom.Text;

import java.util.LinkedList;


public class MapActivity extends NMapActivity
    implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        MessageApi.MessageListener,
        GestureDetector.OnGestureListener
{
    private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체

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
    private GestureDetector gestureDetector;    // 제스처 처리

    GNode[] GNodeArr = {
            new GNode(0, 0, null, null, 126.7320299, 37.3422762),
            new GNode(1, 1, null, null, 126.7315529, 37.3417747),
            new GNode(2, 1, null, null, 126.7321684, 37.3414226),
            new GNode(3, 1, null, null, 126.732261, 37.3415269),
            new GNode(4, 2, "TIP", new String[]{"TIP", "기숙사", "기술혁신파크"}, 126.7324044, 37.3413025),
            new GNode(5, 1, null, null, 126.7328204, 37.3416928),
            new GNode(6, 1, null, null, 126.7327854, 37.3410327),
            new GNode(7, 2, "농구장", new String[]{"농구장", "농구골대"}, 126.7328133, 37.3410663),
            new GNode(8, 1, null, null, 126.733123, 37.3408069),
            new GNode(9, 2, "체육관", new String[]{"체육관", "실내체육관"}, 126.7328133, 37.3410663),
            new GNode(10, 2, "A동 체육관 방향", new String[]{"A동", "공학관", "A"}, 126.733102, 37.340649),
            new GNode(11, 1, null, null, 126.7330052, 37.340734),
            new GNode(12, 2, "주차타워", new String[]{"주차타워", "주차", "주차장"}, 126.7329518, 37.3407607),
            new GNode(13, 1, null, null, 126.7325259, 37.3410148),
            new GNode(14, 2, "운동장", new String[]{"운동장", "야외운동장"}, 126.732269, 37.3407499),
            new GNode(15, 1, null, null, 126.7324946, 37.3402106),
            new GNode(16, 1, null, null, 126.7333928, 37.3406941),
            new GNode(17, 1, null, null, 126.7335508, 37.340697),
            new GNode(18, 1, null, null, 126.7338409, 37.3410369),
            new GNode(19, 1, null, null, 126.7331917, 37.3404828),
            new GNode(20, 2, "A동  옆문", new String[]{"A동", "공학관", "A"}, 126.733012, 37.3404005),
            new GNode(21, 2, "B동", new String[]{"B동", "공학관", "B"}, 126.7333303, 37.3403981),
            new GNode(22, 1, null, null, 126.7327469, 37.3400473),
            new GNode(23, 2, "비즈니스센터", new String[]{"비즈니스센터", "비지니스센터", "비센"}, 126.7326993, 37.3397297),
            new GNode(24, 1, null, null, 126.7337407, 37.3405675),
            new GNode(25, 1, null, null, 126.7334535, 37.3402799),
            new GNode(26, 1, null, null, 126.7341991, 37.340785),
            new GNode(27, 2, "후문", new String[]{"후문", "뒷문"}, 126.7342771, 37.3408865),
            new GNode(28, 2, "종합관", new String[]{"종합관", "종합교육관"}, 126.7340643, 37.3407165),
            new GNode(29, 1, null, null, 126.7339313, 37.3404741),
            new GNode(30, 1, null, null, 126.7336486, 37.3401883),
            new GNode(31, 1, null, null, 126.7331765, 37.3397643),
            new GNode(32, 2, "행정동", new String[]{"행정동", "행정"}, 126.7333881, 37.3397134),
            new GNode(33, 2, "정문", new String[]{"정문", "앞문"}, 126.7328697, 37.3394036),
            new GNode(34, 1, null, null, 126.7340958, 37.340394),
            new GNode(35, 1, null, null, 126.7339456, 37.3401767),
            new GNode(36, 1, null, null, 126.7337904, 37.3401126),
            new GNode(37, 2, "C동", new String[]{"C동", "공학관", "C"}, 126.7340423, 37.3401007),
            new GNode(38, 1, null, null, 126.7345681, 37.3405729),
            new GNode(39, 1, null, null, 126.7344959, 37.3404579),
            new GNode(40, 2, "G동 뒷문", new String[]{"G동", "공학관", "G"}, 126.7347369, 37.34029),
            new GNode(41, 1, null, null, 126.7342688, 37.3402554),
            new GNode(42, 1, null, null, 126.7342912, 37.3400528),
            new GNode(43, 1, null, null, 126.7341305, 37.3399751),
            new GNode(44, 2, "D동", new String[]{"D동", "공학관", "D"}, 126.7339943, 37.3398282),
            new GNode(45, 1, null, null, 126.7336674, 37.3394493),
            new GNode(46, 1, null, null, 126.7343518, 37.3401956),
            new GNode(47, 2, "G동 앞문", new String[]{"G동", "공학관", "G"}, 126.7346199, 37.3401387),
            new GNode(48, 1, null, null, 126.7347054, 37.3400582),
            new GNode(49, 1, null, null, 126.734515, 37.3399127),
            new GNode(50, 1, null, null, 126.7345649, 37.3397778),
            new GNode(51, 1, null, null, 126.7340227, 37.3392488),
            new GNode(52, 2, "산학융합본부 옆문", new String[]{"산융", "산학융합본부"}, 126.733845, 37.3389637),
            new GNode(53, 1, null, null, 126.7349993, 37.3394652),
            new GNode(54, 2, "E동 정문", new String[]{"E동", "공학관", "E"}, 126.7350439, 37.339524),
            new GNode(55, 1, null, null, 126.7342825, 37.3389035),
            new GNode(56, 1, null, null, 126.7352209, 37.3393179),
            new GNode(57, 2, "P동", new String[]{"P동", "공학관", "P"}, 126.7353305, 37.3394222),
            new GNode(58, 2, "산학융합본부 정문", new String[]{"산융", "산학융합본부"}, 126.7346572, 37.3386878)

    };

    MapGraph GMap = new MapGraph(59);

    LinkedList<GNode> path = null, movePath = null;
    boolean guiding = false;
    double[] location = new double[2];
    int destination = 0;

    ////////////////////////////////////////////////////
    // test

    TextView lonText, latText, radText;
    LinearLayout mapLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        tts = new TTSAdapter(this);

        // create map view
        mMapView = (NMapView) findViewById(R.id.mapView);
        //mMapView = new NMapView(this);

        // set a registered API key for Open MapViewer Library
        mMapView.setApiKey(API_KEY);

        // set the activity content to the map view
        //setContentView(mMapView);

        // initialize map view
        mMapView.setClickable(true);

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

        for (int i = 0; i < 59; i++) {
            GMap.insertVertex(i, GNodeArr[i]);
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

        ///////////////////////////////////////
        // test

        lonText = (TextView) findViewById(R.id.lonText);
        latText = (TextView) findViewById(R.id.latText);
        radText = (TextView) findViewById(R.id.radText);
        mapLayout = (LinearLayout) findViewById(R.id.mapLayout);
        mapLayout.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

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

    private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

        @Override
        public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {

            if (errorInfo == null) { // success
                // restore map view state such as map center position and zoom level.
                //restoreInstanceState();
                mMapController.setMapCenter(new NGeoPoint(126.7335061, 37.3400342), 14);

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
    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

        @Override
        public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {

            if (mMapController != null) {
                //mMapController.animateTo(myLocation);
                location[0] = myLocation.getLongitude();
                location[1] = myLocation.getLatitude();
                lonText.setText(Double.toString(location[0]));
                latText.setText(Double.toString(location[1]));
                if (guiding) {
                    if (path == null) {
                        destination = 28;  //
                        int closest = cp_getClosest(location, destination);
                        GMap.executeDijk(closest);
                        path = GMap.getPath(28/*목적지*/);
                        movePath = (LinkedList<GNode>) path.clone();
                        path.addFirst(new GNode(99, 32, null, null, location[0], location[1]));
                        cp_TTS("안내를 시작합니다");
                    }
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
            mOverlayManager.removeMyLocationOverlay();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // custom
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // 배열 사용 경로 출력 함수
    private void cp_gNodePathdataOverlay(GNode[] arr) {

        // set POI data
        NMapPathData pathData = new NMapPathData(arr.length);

        pathData.initPathData();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null)
                continue;
            pathData.addPathPoint(arr[i].lon, arr[i].lat, 0);
        }
        pathData.endPathData();

        NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
        pathDataOverlay.showAllPathData(0);
    }

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


    // POI, Path 모두 지우기
    public void onClickCancel(View arg0) {
        mOverlayManager.clearOverlays();
    }

    public void onClickPathPOI(View arg0) {
        //item.setFloatingMode(NMapPOIitem.FLOATING_DISPATCH);
        /*
        // use array
        GNode[] arr = {
                GNodeArr[1],
                GNodeArr[2],
                GNodeArr[4],
                GNodeArr[5],
                GNodeArr[18],
                GNodeArr[26],
                GNodeArr[38],
                GNodeArr[39],
                GNodeArr[40]
        };
        cp_gNodePathdataOverlay(arr);
        */

        // use LinkedList
        cp_gNodePathdataOverlay(path);
    }

    public void onClickPOI(View arg0) {
        //item.setFloatingMode(NMapPOIitem.FLOATING_FIXED);
        cp_gNodePOIdataOverlay(GNodeArr);
    }

    public void onClickGPS(View arg0) {
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
                    Toast.makeText(MapActivity.this, "Please enable a My Location source in system settings",
                            Toast.LENGTH_LONG).show();

                    Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(goToSettings);

                    return;
                }
            }
        }
    }

    public void onClickGuide(View arg0) {
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
                guiding = false;

                mMapView.postInvalidate();
            } else {
                boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
                if (!isMyLocationEnabled) {
                    cp_TTS("GPS를 설정해주세요");
                    Toast.makeText(MapActivity.this, "Please enable a My Location source in system settings",
                            Toast.LENGTH_LONG).show();

                    Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(goToSettings);

                    return;
                } else {
                    guiding = true;
                }
            }
        }
    }

    private void cp_gNodeMovePath(LinkedList<GNode> path) {

        // set POI data
        NMapPathData pathData = new NMapPathData(path.size() + 1);

        Toast.makeText(MapActivity.this, new Integer(path.size() + 1).toString(),
                Toast.LENGTH_LONG).show();

        pathData.initPathData();
        pathData.addPathPoint(location[0]/*lon*/, location[1]/*lang*/, 0);
        for (GNode node : path) {
            pathData.addPathPoint(node.lon, node.lat, 0);
        }
        pathData.endPathData();

        NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
        pathDataOverlay.showAllPathData(0);
    }

    //static double getDistance(NGeoPoint from, NGeoPoint to) 두 점 사이의 거리

    private int cp_getClosest(double[] location, int to) {
        int closest;
        double cloDis, tempDis;
        NGeoPoint locNP = new NGeoPoint(location[0], location[1]);

        closest = 0;
        cloDis = NGeoPoint.getDistance(locNP, new NGeoPoint(GNodeArr[closest].lon, GNodeArr[closest].lat));
        for (int i = 1; i < GMap.max; i++) {
            tempDis = NGeoPoint.getDistance(locNP, new NGeoPoint(GNodeArr[i].lon, GNodeArr[i].lat));
            if (cloDis > tempDis) {
                closest = i;
                cloDis = tempDis;
            }
        }

        return closest;
    }

    private boolean cp_checkPath() {

        double dis;
        GNode prev = null, next = movePath.peekFirst();


        NGeoPoint locNP = new NGeoPoint(location[0], location[1]);
        NGeoPoint toNP = new NGeoPoint(next.lon, next.lat);

        dis = NGeoPoint.getDistance(locNP, toNP);

        if (dis < 5) {
            if (next == movePath.peekLast()) {
                path = null;
                movePath = null;
                guiding = false;
                cp_TTS("목적지에 도착했습니다");
            } else {
                for (GNode node : path) {
                    if (node == next) {
                        break;
                    }
                    prev = node;
                }
                getRadian(prev, movePath.pollFirst(), next = movePath.peekFirst());
                dis = NGeoPoint.getDistance(toNP, new NGeoPoint(next.lon, next.lat));
                cp_TTS("다음 노드까지" + new Integer((int) dis).toString() + "미터입니다");
                Toast.makeText(MapActivity.this, "Please enable a My Location source in system settings",
                        Toast.LENGTH_SHORT).show();
                //movePath.pollFirst();
            }

            return true;
        }
        return false;
    }

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

    private double getRadian(GNode p1, GNode p2, GNode p3) {
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

        radText.setText(new Double(deg).toString());

        return deg;
    }

    private void cp_TTS(String msg) {
        tts.speak(msg);
    }

    @Override
    protected void onDestroy() {
        tts.destroy();
        super.onDestroy();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // 구글 플레이 서비스에 접속이 일시정지 됐을 때 실행
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // 구글 플레이 서비스에 접속을 실패했을 때 실행
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/MESSAGE_PATH")) {
            // 메세지로부터 스트링 추출
            final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);

            // UI 스레드를 실행하여 텍스트 뷰의 값을 수정한다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (msg) {

                    }
                }
            });
        }
    }

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

    }

    // 시계로 데이터 및 메시지를 전송 후 실행되는 메소드
    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(Result result) {
            String resultString = "Sending Result : " + result.getStatus().isSuccess();

            //Toast.makeText(getApplication(), resultString, Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
       // tap_long();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //SendGes("SINGLE_TAP");
        /*if(tap_count==2)
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
        }*/
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
       /* if(long_press_on ==0 )
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
        }*/
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
       /* try
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
        } catch (Exception e) {  }*/
        return true;
    }
}
