package com.lfin.electricitymeterocr;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MeterAdapter extends BaseAdapter {

    Context context;
    List<Meter> data;
    int layout;
    LayoutInflater inflater;

    public MeterAdapter(Context context, List<Meter> data, int layout){
        this.context = context;
        this.data = data;
        this.layout = layout;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i).getSerialCd();
    }

    @Override
    public long getItemId(int i) {
        return (long)i;
    }


    // private 는 인스턴스가 접근 못합니다.
    class ImageThread extends Thread {
        public void run(){
            try{
            }catch(Exception e){
                Log.e("이미지 다운로드", "실패");
            }
        }
    }

    // 셀 모양을 설정하는 메소드
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // 출력할 셀을 생성
        // 먼저 출력할 셀을 가지고 생성
        View returnView = view;
        // 출력한 적이 없다면 직접 생성
        if(returnView == null){
            returnView = inflater.inflate(layout, viewGroup, false);
        }
        // R.id.itemname는 item_cell.xml의 android:id="@+id/itemname"와 매칭됨
        TextView serialCd = (TextView)returnView.findViewById(R.id.itemname);
        serialCd.setText(data.get(i).getSerialCd());

        // 이미지 출력을 위한 부분
//        ImageView imageView = (ImageView) returnView.findViewById(R.id.itemimage);
//        ImageThread th = new ImageThread();
//        th.imagename = data.get(i).getPictureurl();
//        th.imageView = imageView;
//        th.start();
        return returnView;
    }


}
