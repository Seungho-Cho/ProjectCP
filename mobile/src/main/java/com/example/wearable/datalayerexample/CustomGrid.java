package com.example.wearable.datalayerexample;



/**
 * Created by june on 2015-04-29.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class CustomGrid extends BaseAdapter {
    private Context mContext;
    private final String[] table;
    private ArrayList<Integer> time_list = new ArrayList<Integer>();
    private ArrayList<String> subject_list = new ArrayList<String>();
    private HashMap<Integer, TimeTable> timeTable = new HashMap<Integer, TimeTable>();

    public CustomGrid(Context c, String[] table, ArrayList<Integer> time_list, ArrayList<String> subject_list, HashMap<Integer, TimeTable> ClassTable) {
        mContext = c;

        this.table = table;
        this.time_list = time_list;
        this.subject_list = subject_list;
        this.timeTable = ClassTable;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return table.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_single, null);
            TextView textView = (TextView) grid.findViewById(R.id.y_grid_text);
            textView.setText(table[position]);


            Iterator itr = time_list.iterator();


            while (itr.hasNext()) {

                if ((Integer)itr.next() == position) {

                    //int colorType = Integer.parseInt(timeTable.get(position))%7;
                    int colorType = timeTable.get(position).getClassName().length()%7;
                    Color color = new Color();
                    switch(colorType)
                    {
                        case 0:
                            grid.setBackgroundColor(Color.parseColor("#FF0000"));
                            break;
                        case 1:
                            grid.setBackgroundColor(Color.parseColor("#FF5E00"));
                            break;
                        case 2:
                            grid.setBackgroundColor(Color.parseColor("#FFE400"));
                            break;
                        case 3:
                            grid.setBackgroundColor(Color.parseColor("#1DDB16"));
                            break;
                        case 4:
                            grid.setBackgroundColor(Color.parseColor("#0054FF"));
                            break;
                        case 5:
                            grid.setBackgroundColor(Color.parseColor("#0100FF"));
                            break;
                        case 6:
                            grid.setBackgroundColor(Color.parseColor("#5F00FF"));
                            break;
                    }


//                    int R = (int) (Math.random() * 256);
//                    int G = (int) (Math.random() * 256);
//                    int B = (int) (Math.random() * 256);
//                    grid.setBackgroundColor(Color.rgb(R, G, B));
                }
            }

        } else {
            grid = (View) convertView;
        }


        return grid;
    }


}
