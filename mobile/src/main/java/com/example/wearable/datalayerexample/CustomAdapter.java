package com.example.wearable.datalayerexample;

/**
 * Created by june on 2015-04-13.
 */


        import java.util.ArrayList;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.CheckBox;
        import android.widget.TextView;



class CustomAdapter extends ArrayAdapter<Item>{
    // 레이아웃 XML을 읽어들이기 위한 객체
    private LayoutInflater mInflater;

    public CustomAdapter(Context context, ArrayList<Item> object) {
        // 상위 클래스의 초기화 과정
        // context, 0, 자료구조
        super(context, 0, object);
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // 자신이 만든 xml의 스타일로 보이기 위한 구문
    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = null;
        final int pos = position;
        final Context context = parent.getContext();

        // 현재 리스트의 하나의 항목에 보일 컨트롤 얻기
        if (v == null) {
            // XML 레이아웃을 직접 읽어서 리스트뷰에 넣음
            view = mInflater.inflate(R.layout.custom_item, null);
        } else {
            view = v;
        }

        // 자료를 받는다.
        final Item data = this.getItem(position);
        if (data != null) {
            // 화면 출력

            TextView tv_major = (TextView) view.findViewById(R.id.y_txt_major);
            TextView tv_student_name = (TextView) view.findViewById(R.id.y_txt_student_name);
            TextView tv_student_number = (TextView) view.findViewById(R.id.y_txt_student_number);
            TextView tv_subject_name = (TextView) view.findViewById(R.id.y_txt_subject_name);
            TextView tv_day = (TextView) view.findViewById(R.id.y_txt_day);
            TextView tv_lecture_time = (TextView) view.findViewById(R.id.y_txt_lecture_time);
            // 텍스트뷰1에 getLabel()을 출력 즉 첫번째 인수값

            tv_major.setText(data.getMajor());
            tv_student_name.setText(data.getStudent_name());
            tv_student_number.setText(data.getStudent_number());
            tv_subject_name.setText(data.getSubject_name());
            tv_day.setText(data.getDay());
            tv_lecture_time.setText(data.getLecture_time());




            // 이미지뷰에 뿌려질 해당 이미지값을 연결 즉 세번째 인수값
            //imageview.setImageResource(data.getImgData());



        }
        return view;
    }

}

