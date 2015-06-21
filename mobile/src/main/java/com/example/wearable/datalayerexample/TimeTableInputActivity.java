package com.example.wearable.datalayerexample;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;


public class TimeTableInputActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {

    private Button btn;
    private EditText edit;
    private TextView txt;
    private CustomAdapter m_Adapter;
    private ListView m_ListView;
    private Item[] item_data;
    private TextToSpeech myTTS;
    ArrayList<Item> alist;

    boolean _ttsActive = false;
    ArrayList<Integer> idx_list = new ArrayList<Integer>();
    ArrayList<Integer> time_list = new ArrayList<Integer>();
    ArrayList<String> subject_list = new ArrayList<String>();
    Item[] table_data;
    Subject mySubject = new Subject();

    TouchNumView touchview;
    SerialHash sHash = new SerialHash();
    LinearLayout yLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table_input);

        yLayout = (LinearLayout) findViewById(R.id.y_layout);
        alist = new ArrayList<Item>(); // 메인 리스트 아이템 객체 생성
        m_Adapter = new CustomAdapter(this, alist); // 메인 어댑터 생성
        m_ListView = (ListView) findViewById(R.id.y_main_listView); // 메인 리스트뷰 연결
        m_ListView.setAdapter(m_Adapter); // 메인 리스트뷰에 메인 어댑터 연결
        btn = (Button) findViewById(R.id.y_btn_show_table);
        edit = (EditText) findViewById(R.id.y_editText);


        touchview = new TouchNumView(getBaseContext(),edit,null,sHash,0,this);
        yLayout.addView(touchview);





        btn.setOnClickListener(new View.OnClickListener() {
            // 파라미터로 넘어오는 View는 현재 클릭된 View이다. 현재 클릭된 View는 button이다.
            public void onClick(View v) {
                displayCurrentTime();
                getTimeTable();

            }
        });
        loadItems();
    }

    public String displayCurrentTime(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        //Date tomorrow = new Date ( date.getTime ( ) + (long) ( 1000 * 60 * 60 * 24*1.7 ) );
        //SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
        String strNow = sdfNow.format(date);
        Toast.makeText(this, "현재 시간 : " + strNow, Toast.LENGTH_LONG).show();
        return strNow;

    }


    protected void getTimeTable() {
        int student_num = Integer.parseInt(edit.getText().toString());
        int i = 0;
        int idx = 0;

        table_data = item_data;

        String student_data = null;
        String time_data = null;
        StringTokenizer st;
        for (i = 0; i < item_data.length; i++) {

            if (student_num == Integer.parseInt(item_data[i].getStudent_number().toString())) {
                subject_list.add(item_data[i].getSubject_name());

                idx_list.add(i);
                st = new StringTokenizer(item_data[i].getLecture_time(), ",");

                while (st.hasMoreTokens()) {   //토근이 있는동안 while문이 실행됨
                    String temp = st.nextToken();
                    time_list.add(Integer.parseInt(temp));
                }
                System.out.println(time_list);
            }
        }
        Intent myIntent = new Intent(TimeTableInputActivity.this, TimeTableActivity.class);
        myIntent.putExtra("idx_data", idx_list);
        myIntent.putExtra("time_list", time_list);
        myIntent.putExtra("subject_list", subject_list);
        startActivity(myIntent);
        finish();
    }


    protected void loadItems() {
        ItemClient.get("main_data2.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONArray response) {

                Gson gson = new GsonBuilder().create();

                Item items[] = gson.fromJson(response.toString(), Item[].class);
                item_data = items;
                m_Adapter.addAll(items);
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onInit(int status) {
    }
}
