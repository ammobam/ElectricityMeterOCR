package com.lfin.electricitymeterocr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.lfin.electricitymeterocr.DTO.ElectricityMeterDTO;
import com.lfin.electricitymeterocr.DTO.ElectricityPreprocessingDTO;
import com.lfin.electricitymeterocr.DTO.ModemDTO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeterInfoDetailActivity extends AppCompatActivity {
    ElectricityMeterDTO electDTO;
    private ViewPager viewPager2;
    private Button homeBtn;
    private Button modemCameraBtn;
    private TextView serialCd;
    private TextView supplyType;
    private TextView typename;
    private TextView modemCd;

    // 이미지 파일명을 저장할 리스트
    private List<String> fileNameList;

    // 다운로드받은 문자열을 저장할 변수를 선언
    private String json;
    // 상세검색 결과를 저정할 변수
    Boolean result;

    // 이미지를 서버에서 가져오고 슬라이드 설정을 해주는 ViewPagerAdapter 변수 선언
    private ViewPagerAdapter viewPagerAdapter;

    // 화면 갱신을 위한 Handler 객체생성
    Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                // textVeiw 갱신
                if (result == true) {
                    Log.e("MeterInfoDetailActivity","TextThread 에 대한 Handler");
                    // 이 곳에 화면 갱신 내용을 작성
                    serialCd.setText(electDTO.getSerialCd());
                    supplyType.setText(electDTO.getSupplyType());
                    typename.setText(electDTO.getTypeName());

                    String modemCdStr = electDTO.getModemDTO().getModemCd();
                    // 모뎀정보가 등록 모뎀촬영버튼표시여부
                    int cameraBtnOnOff = View.INVISIBLE;

                    if (modemCdStr.equals(Common.NULL_STR) == true) {
                        cameraBtnOnOff = View.VISIBLE;
                        modemCdStr = Common.MODEM_INFO_NONE;
                    }
                    modemCd.setText(modemCdStr);
                    modemCameraBtn.setVisibility(cameraBtnOnOff);

                } else {
                    Toast.makeText(MeterInfoDetailActivity.this, Common.TOAST_MESSAGE_1,  Toast.LENGTH_LONG).show();
                }
            } else {
                ArrayList<Bitmap> imageList = (ArrayList<Bitmap>)msg.obj;
                viewPagerAdapter = new ViewPagerAdapter(MeterInfoDetailActivity.this, imageList);
                viewPager2.setAdapter(viewPagerAdapter);
                viewPagerAdapter.notifyDataSetChanged();

            }
        }
    };

    // 다운로드 받을 Thread 클래스
    class TextThread extends Thread {
        String serialId;
        public void run(){
            try {
                Log.e("DetailView의 TextThread", " 인터넷 연결시도");
                // 웹 Server url설정
                URL url = new URL(Common.SEVER_URL + "/detail/" + serialId);

                // URL 객체를 HttpURLConnection 으로 형 변환
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                // 옵션 설정
                // cache 를 사용하겠다고 설정하면 이전 다운로드 받은 데이터를 재활용
                con.setUseCaches(false);
                // 연결 제한 시간 설정
                con.setConnectTimeout(30000);
                // server 쪽의 입력을 획득하고, 버퍼에 저장
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                // 변할 수 있는 문자열을 저장하는 객체
                StringBuilder sb = new StringBuilder();
                while(true){
                    // 버퍼에 저장된 문자열 한줄 읽어오기
                    String line = br.readLine();
                    // 읽어올게 없으면 반복문 종료
                    if(line == null){
                        break;
                    }
                    // 읽은 내용을 StringBuilder에 저장
                    sb.append(line.trim());
                }

                // 읽어온 문자열을 저장
                json = sb.toString();

                Log.e("받아온 상세 데이터", json);

                // BufferedReader close
                br.close();
                // HttpURLConnection 해제
                con.disconnect();

                // 데이터 전체를 객체로 변환
                JSONObject obj = new JSONObject(json);
                // 객체 내에서 data라는 키의 값을 추출
                JSONObject data = obj.getJSONObject("data");
                // result의 값을 추출
                result = obj.getBoolean("result");

                // 이미지 다운로드를 위해 이미지 파일명을 저장할 변수
                fileNameList = new ArrayList<>();
                // 전력량계량기 DTO 정보 설정
                electDTO = new ElectricityMeterDTO();
                electDTO.setSerialCd(data.getString("serial_cd"));
                electDTO.setTypeName(data.getString("typename"));
                electDTO.setSupplyType(data.getString("supply_type"));
                electDTO.setElectricityFilename(data.getString("electricity_filename"));
                electDTO.setElectricitySaveDate(data.getString("electricity_save_date"));

                // 전력량계량기 이미지 파일명 저장
                fileNameList.add(electDTO.getElectricityFilename());
                // 모뎀정보 DTO 정보 설정
                ModemDTO ModemDTO = new ModemDTO();
                ModemDTO.setSerialCd(data.getString("serial_cd"));
                ModemDTO.setModemCd(data.getString("modem_cd"));
                ModemDTO.setModemFilename(data.getString("modem_filename"));
                ModemDTO.setModemSaveDate(data.getString("modem_save_date"));
                electDTO.setModemDTO(ModemDTO);

                // TODO 나중에 추가
                // 모뎀 바코드 이미지 설정
                // fileNameList.add(ModemDTO.getModemFilename());
                // 전처리 과정 DTO 정보 설정
                JSONArray preFileNameList = data.getJSONArray("pre_filenames");

                // 배열을 순회
                int i = 0;

                while(i < preFileNameList.length()){
                    ElectricityPreprocessingDTO electPreDTO = new ElectricityPreprocessingDTO();
                    electPreDTO.setPreFilename( preFileNameList.getString(i));
                    // 전처리 과정 이미지파일명 저장
                    fileNameList.add(electPreDTO.getPreFilename());
                    electDTO.setElectPreDTO(electPreDTO);
                    i = i + 1;
                }
                Log.e("파싱 결과  :: : : : : :",electDTO.toString());
                Log.e("handler에게 메시지 전송",String.valueOf(fileNameList));

                Log.e("파일명 리스트 : ", String.valueOf(fileNameList));

                // 이미지슬라이드 설정
                viewPager2 = findViewById(R.id.viewPager2);

                handler.sendEmptyMessage(0);
                // 이미지 다운로드 스레드 실행
                ImageThread th = new ImageThread(fileNameList);
                th.start();
                // Handler 에게 메시지 전송
            }catch (Exception e){
                // console창으로 메시지를 확인
                // 태그, getLocalizedMessage()
                Log.e("다운로드 또는 파싱 실패", e.getLocalizedMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_info_detail);
        // home 버튼
        homeBtn = findViewById(R.id.homeBtn);
        // 이미지 슬라이드 뷰
        viewPager2 = findViewById(R.id.viewPager2);
        // textView
        serialCd = findViewById(R.id.serialCd);
        supplyType = findViewById(R.id.supplyType);
        typename = findViewById(R.id.typename);
        modemCd = findViewById(R.id.modemCd);
        // 모뎀 촬영 버튼
        modemCameraBtn = findViewById(R.id.modemCameraBtn);

        // homeBtn 클릭시 메인페이지로 이동
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToHome();
            }
        });

        // MeterInfoActivity에서 전달한 serial_id 데이터 읽어오기
        String serialId = getIntent().getStringExtra("serial_id");

        // homeBtn 클릭시 메인페이지로 이동
        modemCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeterInfoDetailActivity.this, BarcodeDetectorActivity.class);
                intent.putExtra("serial_id", serialId);
                startActivity(intent);
            }
        });


        TextThread textThread = new TextThread();
        textThread.serialId = serialId;
        textThread.start();

    }
    private void backToHome(){
        Intent intent = new Intent(MeterInfoDetailActivity.this , MainActivity.class);
        startActivity(intent);
    }

    // 다운로드 받을 Thread 클래스
    class ImageThread extends Thread {
        // 다운로드 받을 이미지명
        List<String> fileNameList;

        public ImageThread(List<String> fileNameList){
            this.fileNameList = fileNameList;
        }

        public void run() {
            ArrayList<Bitmap> imageList = new ArrayList<>();
            try {
                for(String fileName : fileNameList) {
                    // 이미지 다운로드를 스트림 생성
                    InputStream inputStream = new URL(Common.SEVER_URL + "/detailimagedownload/" + fileName).openStream();
                    // 이미지 다운로드
                    Bitmap bit = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();

                    imageList.add(bit);
                }
                Message msg = new Message();
                msg.what = 1;
                msg.obj = imageList;
                handler.sendMessage(msg);
            }catch(Exception e){
                Log.e("이미지 다운로드", e.toString());
            }
        }
    }

}