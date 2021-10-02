package com.lfin.electricitymeterocr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
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
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MeterInfoDetailActivity extends AppCompatActivity {
    // 이미지 슬라이드를 위한 ViewPager 변수 선언


    ElectricityMeterDTO electDTO;
    private ViewPager2 viewPager2;
    private Button homeBtn;
    private Button modemCameraBtn;
    private TextView serialCd;
    private TextView supplyType;
    private TextView typename;
    private TextView modemCd;

    // 다운로드받은 문자열을 저장할 변수를 선언
    private String json;
    // 상세검색 결과를 저정할 변수
    Boolean result;

    // 화면 갱신을 위한 Handler 객체생성
    Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg){
            if(result == true){

                // 이 곳에 화면 갱신 내용을 작성
                serialCd.setText(electDTO.getSerialCd());
                supplyType.setText(electDTO.getSupplyType());
                typename.setText(electDTO.getTypeName());
                modemCd.setText(electDTO.getModemDTO().getModemCd());
            }else {
                Toast.makeText(MeterInfoDetailActivity.this, "상세 정보를 가져올 수 없습니다", Toast.LENGTH_LONG).show();
            }
            super.handleMessage(msg);
        }

    };
    // 다운로드 받을 Thread 클래스
    class DetailThread extends Thread {
        String serialId;
        public void run(){
            try {
                Log.e("인터넷 연결", "연결시도");
                // 웹 Server url설정
                URL url = new URL("http://172.30.1.54:5000/detail/" + serialId);

                // URL객체를 HttpURLConnection 으로 형 변환
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

                // 전력량계량기 DTO 정보 설정
                electDTO = new ElectricityMeterDTO();
                electDTO.setSerialCd(data.getString("serial_cd"));
                Log.e("제조번호  :: : : : : :",data.getString("serial_cd"));
                electDTO.setTypeName(data.getString("typename"));
                electDTO.setSupplyType(data.getString("supply_type"));
                electDTO.setElectricityFilename(data.getString("electricity_filename"));
                electDTO.setElectricitySaveDate(data.getString("electricity_save_date"));

                // 모뎀정보 DTO 정보 설정
                ModemDTO modelDTO = new ModemDTO();

                modelDTO.setSerialCd(data.getString("serial_cd"));
                modelDTO.setModemCd(data.getString("modem_cd"));
                modelDTO.setModemFilename(data.getString("modem_filename"));
                modelDTO.setModemSaveDate(data.getString("modem_save_date"));
                electDTO.setModemDTO(modelDTO);

                // 전처리 과정 DTO 정보 설정
                JSONArray preFileNameList = data.getJSONArray("pre_filenames");

                // 배열을 순회
                int i = 0;
                Log.e("파일의 크기",String.valueOf(preFileNameList.length()));

                while(i < preFileNameList.length()){
                    ElectricityPreprocessingDTO electPreDTO = new ElectricityPreprocessingDTO();
                    electPreDTO.setPreFilename( preFileNameList.getString(i));
                    electDTO.setElectPreDTO(electPreDTO);
                    i = i + 1;
                }
                Log.e("파싱 결과  :: : : : : :",electDTO.toString());
                // Handler 에게 메시지 전송
                handler.sendEmptyMessage(0);

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
//        viewPager2 = findViewById(R.id.viewPager2);
        homeBtn = findViewById(R.id.homeBtn);
        modemCameraBtn = findViewById(R.id.modemCameraBtn);
        serialCd = findViewById(R.id.serialCd);
        supplyType = findViewById(R.id.supplyType);
        typename = findViewById(R.id.typename);
        modemCd = findViewById(R.id.modemCd);

        // homeBtn 클릭시 메인페이지로 이동
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInfoFrom();
            }
        });

        // MeterInfoActivity에서 전달한 serial_id 데이터 읽어오기
        String serialId = getIntent().getStringExtra("serial_id");

        DetailThread detailThread = new DetailThread();
        detailThread.serialId = serialId;
        detailThread.start();

    }
    private void getInfoFrom(){
        Intent intent = new Intent(MeterInfoDetailActivity.this , MainActivity.class);
        startActivity(intent);
    }

}