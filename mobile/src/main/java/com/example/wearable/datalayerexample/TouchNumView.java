package com.example.wearable.datalayerexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Created by PC on 2015-04-28.
 */

class Dot
{
    float x,y;
    Dot(float a,float b)
    {
        x=a;
        y=b;
    }
}

public class TouchNumView extends View
{
    Context context;
    Paint pDot;
    Paint pLine;
    TextView output;
    EditText targetText;

    Path path[] = {new Path(),new Path(),new Path(),new Path(),new Path(),new Path(),new Path(),new Path(),new Path(),new Path(),new Path(),new Path(),new Path(),new Path()};
    Dot dots[] = {new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0),new Dot(0,0)};
    SerialHash numHash = null;


    int index;
    int move_count;
    int dot_index;

    int DOT_COUNT_VALUE = 10;
    int DOT_SENS = 50;
    private static final int INVALIDATE_NOW = 1;

    static final int P_UP = 8;
    static final int P_DOWN = 2;
    static final int P_LEFT = 4;
    static final int P_RIGHT = 6;
    static final int P_LUP = 7;
    static final int P_LDOWN = 1;
    static final int P_RUP = 9;
    static final int P_RDOWN = 3;

    String char_code;
    String print_msg;

    int is_on = 1;
    int is_learn = 0;

    private SendMassgeHandler mMainHandler = null;

    public TouchNumView(Context context,EditText out,EditText edit,SerialHash hash,int learn)
    {
        super(context);
        this.context = context;
        this.targetText = edit;
        this.numHash = hash;
        this.is_learn = learn;
        index = 0;
        mMainHandler = new SendMassgeHandler(this);

        output = out;

        char_code = "";
        print_msg = "";

        setBackgroundColor(Color.LTGRAY);
        pDot = new Paint();
        pDot.setColor(Color.RED);
        pDot.setStrokeWidth(20f);
        pDot.setStyle(Paint.Style.STROKE);


        pLine = new Paint();
        pLine.setColor(Color.BLACK);
        pLine.setAntiAlias(true);
        pLine.setStrokeWidth(10f);
        pLine.setStyle(Paint.Style.STROKE);
        pLine.setStrokeJoin(Paint.Join.ROUND);

        put_hash();


        TouchTimer tc = new TouchTimer();
        tc.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float eventX = event.getX();
        float eventY = event.getY();

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                path[index] = new Path();
                path[index].moveTo(eventX,eventY);
                index++;
                move_count=0;
                dot_index=0;
                is_on=1;
                char_code= char_code + 0;
                return true;
            case MotionEvent.ACTION_MOVE:

                path[index-1].lineTo(eventX,eventY);
                if((move_count++)%DOT_COUNT_VALUE==0)
                {
                    dots[dot_index] = new Dot(eventX,eventY);
                    if(dot_index>0)
                    {
                        int code = get_code(dots[dot_index-1],dots[dot_index]);
                        if(dot_index==1) char_code=char_code + code;
                        else if(Character.getNumericValue(char_code.charAt(char_code.length()-1))!=code && code!=0)
                        {
                            char_code= char_code + code;
                           // Toast.makeText(context,char_code,Toast.LENGTH_SHORT).show();
                        }
                    }
                    dot_index++;

                }
                break;
            case MotionEvent.ACTION_UP:
                dots[dot_index] = new Dot(eventX,eventY);
                if(dot_index>0)
                {
                    int code = get_code(dots[dot_index-1],dots[dot_index]);
                    if(dot_index==1) char_code=char_code + code;
                    else if(Character.getNumericValue(char_code.charAt(char_code.length()-1))!=code && code!=0)
                    {
                        char_code= char_code + code;
                    }
                    char_code = char_code + 5;
                }
                dot_index++;
                is_on=0;
                //Toast.makeText(context,char_code,Toast.LENGTH_SHORT).show();
                if(char_code.length() > 4)
                {
                    Log.d("char_code",char_code);
                    if(char_code.equals("015035") || char_code.equals("035015")) {}
                    else { char_code = "000"; }
                }

                break;
        }
        invalidate();
        return true;

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        for(int i=0; i<index; i++)
        {
            canvas.drawPath(path[i],pLine);
        }
        for(int i=0; i<dot_index; i++)
        {
            canvas.drawPoint(dots[i].x,dots[i].y,pDot);
        }

        /*Paint pText = new Paint();
        pText.setColor(Color.MAGENTA);
        pText.setTextSize(50f);
        canvas.drawText(print_msg,0,60,pText);*/
        //canvas.drawText(""+testn,0,60,pText);
    }

    public int get_code(Dot a, Dot b)
    {
        float x;
        float y;
        double len;


        int ver=0;  // -1=up 0=same_y  1=down
        int hor=0;  // -1=left  0=same_x  1=right



        if(a.x < b.x)
        {
            if(Math.abs(a.x-b.x)>DOT_SENS) hor=1;
        }
        else
        {
            if(Math.abs(a.x-b.x)>DOT_SENS) hor=-1;
        }

        if(a.y < b.y)
        {
            if(Math.abs(a.y-b.y)>DOT_SENS) ver=1;
        }
        else
        {
            if(Math.abs(a.y-b.y)>DOT_SENS) ver=-1;
        }


        if(ver==0)
        {
            if(hor==-1) return P_LEFT;
            if(hor==1) return P_RIGHT;
        }
        if(hor==0)
        {
            if(ver==-1) return P_UP;
            if(ver==1) return P_DOWN;
        }

        if(ver==-1)
        {
            if(hor==-1) return P_LUP;
            if(hor==1) return P_RUP;
        }
        if(ver==1)
        {
            if(hor==-1) return P_LDOWN;
            if(hor==1) return P_RDOWN;
        }

        return 0;
    }



    void toNum(String msg)
    {
        Log.d("test", "" + msg);
        String temp = numHash.hash.get(msg);
        if(temp !=null) print_msg += temp;
        Message hMsg = mMainHandler.obtainMessage();
        hMsg.what = INVALIDATE_NOW;
        mMainHandler.sendMessage(hMsg);

    }

    void put_hash()
    {
        numHash.hash.put("035015","x");
        numHash.hash.put("015035","x");
        numHash.hash.put("075","1");
        numHash.hash.put("085","2");
        numHash.hash.put("095","3");
        numHash.hash.put("045","4");
        numHash.hash.put("005","5");
        numHash.hash.put("0","5");
        numHash.hash.put("065","6");
        numHash.hash.put("015","7");
        numHash.hash.put("025","8");
        numHash.hash.put("035","9");
        numHash.hash.put("000","0");
        numHash.hash.put("0135","b");

    }
    void put_hash_learn(String in,String target)
    {
        numHash.hash.put(in,target);
        Message hMsg = mMainHandler.obtainMessage();
        hMsg.what = 2;
        mMainHandler.sendMessage(hMsg);

    }
    class TouchTimer extends Thread
    {

        int ti=0;
        public void run()
        {
            super.run();
            while(true)
            {
                if(is_on==1) ti=0;
                ti++;
                if(ti==10)
                {
                    if(is_learn==0)toNum(char_code);
                    else put_hash_learn(char_code,targetText.getText().toString());
                    char_code = "";

                    for(int j=0; j<path.length-1; j++)
                    {
                        path[j] = new Path();
                    }
                    for(int j=0; j<dots.length-1; j++)
                    {
                        dots[j] = new Dot(0,0);
                    }
                    is_on=1;
                    move_count=0;
                    dot_index=0;
                    index=0;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class SendMassgeHandler extends Handler
    {


        TouchNumView tv;

        SendMassgeHandler(TouchNumView tv)
        {
            this.tv = tv;
        }
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case INVALIDATE_NOW:

                    String temp = output.getText().toString();
                    if(print_msg.equals("b"))
                    {
                        temp = temp.substring(0,temp.length()-1);
                    }
                    else
                    {
                        temp += print_msg;
                    }
                    output.setText(temp);
                    print_msg = "";



                    tv.invalidate();

                    break;
                case 2:
                    Toast.makeText(context, "학습 완료!", Toast.LENGTH_SHORT).show();
                    tv.invalidate();
                default:
                    break;
            }
        }

    };

}

class SerialHash implements Serializable
{
    Hashtable<String,String> hash = null;

    SerialHash()
    {
        hash = new Hashtable<String,String>();
    }
    SerialHash( Hashtable<String,String> hash )
    {
        this.hash = hash;
    }
}


