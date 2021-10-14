package com.lfin.electricitymeterocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lfin.electricitymeterocr.DTO.ElectricityMeterDTO;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAdapter extends BaseAdapter {

    // View 출력할 때 필요한 Context 변수 - Activity 를 대입
    Context context;
    // ListView 에 출력할 데이터
    List<ElectricityMeterDTO> data;
    // 셀 모양의 아이디를 저장할 변수
    int layout;
    // xml 파일의 내용을 View 클래스로 변경하기 위한 변수
    LayoutInflater inflater;

    public ListAdapter(Context context, List<ElectricityMeterDTO> data, int layout) {
        this.context = context;
        this.data = data;
        this.layout = layout;
        this.inflater =  (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // 행의 개수를 설정하는 메소드 - 반복문을 수행할 횟수
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i).getSerialCd();
    }

    // 셀을 구별하기 위한 아이디를 설정하는 메소드
    @Override
    public long getItemId(int i) {
        return (long)i;
    }
    ImageView imageView;
    Bitmap bit;

    // private 는 인스턴스가 접근 못합니다.
    class ImageThread extends Thread {
        String imagename;
        ImageView imageView;
        public void run(){
            try{
                // 이미지 다운로드를 스트림 생성
                InputStream inputStream = new URL(Common.SEVER_URL + "/listimagedownload/" + imagename).openStream();
                // 이미지 다운로드
                Bitmap bit = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                // 핸들러에 전송할 데이터 만들기
                Message msg = new Message();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("bit", bit);
                map.put("imageview", imageView);
                msg.obj = map;

                // 핸들러를 Message와 함께 호출
                handler.sendMessage(msg);
            }catch(Exception e){
                Log.e("이미지 다운로드", "실패");
            }

        }
    }

    // 다운로드 받은 이미지를 출력해주는 핸들러
    Handler handler = new Handler(Looper.getMainLooper()){
        // msg 의 obj에 Map을 전달
        // Map의 imageview 키에 ImageView, bit 키에 Bitmap을 전송
        public void handleMessage(Message msg){
            // 전달받은 데이터 가져오기
            Map<String, Object> map = (Map<String, Object>)msg.obj;
            ImageView imageView = (ImageView)map.get("imageview");
            Bitmap bit = (Bitmap)map.get("bit");
            imageView.setImageBitmap(bit);
        }
    };
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
        // R.id.serial_cd item_cell.xml의 android:id="@+id/serial_cd"와 매칭됨
        TextView serialCd = (TextView)returnView.findViewById(R.id.serial_cd);
        serialCd.setText(data.get(i).getSerialCd());

        TextView modemId = (TextView)returnView.findViewById(R.id.modem_id);
        String modemCd = data.get(i).getModemDTO().getModemCd();
        modemId.setText((modemCd.equals(Common.NULL_STR)) ? Common.MODEM_INFO_NONE : modemCd);

        // 이미지 출력을 위한 부분
        ImageView imageView = (ImageView) returnView.findViewById(R.id.electricity_image);
        ImageThread th = new ImageThread();
        th.imagename = data.get(i).getElectricityFilename();
        th.imageView = imageView;
        th.start();
        return returnView;
    }

}
