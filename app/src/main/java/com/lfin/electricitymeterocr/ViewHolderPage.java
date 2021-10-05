package com.lfin.electricitymeterocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.lfin.electricitymeterocr.DTO.ElectricityPreprocessingDTO;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
* ViewHolderPage
* ImageSlide 영역을 설정하는 class
* */
public class ViewHolderPage extends RecyclerView.ViewHolder {
    // 이미지를 감싸는 Layout
    RelativeLayout imgSlideLayout;
    // 이미지를 설정할 imageView영역
    ImageView imageView;

    // 설정한 화면 정보를 전달할 handler
    Handler handler;

    // 출력할 셀을 생성
    ViewHolderPage(View itemView, Handler handler) {
        super(itemView);
        // item_viewpager.xml 의 imgSlideLayout 가져오기
        this.imgSlideLayout = itemView.findViewById(R.id.img_slide_layout);
        // item_viewpager.xml 의 imageView 가져오기
        this.imageView = imgSlideLayout.findViewById(R.id.imageView);
        this.handler = handler;
    }

    // 셀 모양을 설정하는 메소드
    public void onBind(String fileName){
        ImageThread th = new ImageThread();
        th.fileName = fileName;
        th.start();
    }

    // 다운로드 받을 Thread 클래스
    class ImageThread extends Thread {
        // 다운로드 받을 이미지명
        String fileName;

        public void run() {
            try {
                // 이미지 다운로드를 스트림 생성
                InputStream inputStream = new URL(Common.SEVER_URL + "/detailimagedownload/" + fileName).openStream();
                // 이미지 다운로드
                Bitmap bit = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                imageView.setImageBitmap(bit);
                imgSlideLayout.addView(imageView);


                // 핸들러에 전송할 데이터 만들기
                Message msg = new Message();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("bit", bit);
                map.put("imgSlideLayout", imgSlideLayout);
                msg.obj = map;

                // Handler 에게 메시지 전송
                handler.sendMessage(msg);
            }catch(Exception e){
                Log.e("이미지 다운로드", e.toString());
            }
        }
    }
}
