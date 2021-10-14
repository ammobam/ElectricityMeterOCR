package com.lfin.electricitymeterocr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lfin.electricitymeterocr.DTO.ElectricityMeterDTO;
import com.lfin.electricitymeterocr.DTO.ModemDTO;

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;

import java.util.List;

public class MeterInfoActivity extends AppCompatActivity {


    // 다운로드받은 문자열을 저장할 변수를 선언
    String json;
    // 파싱한 결과가 여러개 이므로 List선언
    List<ElectricityMeterDTO> itemList;

    // 목록을 출력할 ListView
    ListView listView;

    // 데이터와 뷰를 이어줄 Adapter 변수
//    ArrayAdapter<ElectricityMeterDTO> listAdapter;
    ListAdapter listAdapter;

    // 진행 상황을 출력하 프로그래스 바
    ProgressBar downloadView;
    private Button homeBtn;
    // 화면 갱신을 위한 Handler 객체생성
    Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg){
            // 이 곳에 화면 갱신 내용을 작성

            // 어댑터가 연결된 View에게 데이터가 갱신되었음을 알리고
            // 다시 출력하라고 하는 코드
            listAdapter.notifyDataSetChanged();
            downloadView.setVisibility(View.GONE);
        }
    };

    // 다운로드 받을 Thread 클래스
    class ItemThread extends Thread {
        public void run(){
            try {
                Log.e("인터넷 연결", "연결시도");
                // 웹 Server url설정
                URL url = new URL(Common.SEVER_URL + "/list");
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

                Log.e("받아온 데이터", json);

                // BufferedReader close
                br.close();
                // HttpURLConnection 해제
                con.disconnect();

                // 데이터 전체를 객체로 변환
                JSONObject obj = new JSONObject(json);

                // 객체 내에서 data라는 키의 배열을 추출
                JSONArray ar = obj.getJSONArray("data");

                // 파싱 결과를 저장할 인스턴스를 생성
//                List<ElectricityMeterDTO> itemList = new ArrayList<>();

                // 배열을 순회
                int i = 0;
                while(i < ar.length()){
                    // 배열의 요소 가져오기
                    // 객체는 key로 요소를 가져오지만, 배열은 인덱스로 요소를 가져옵니다.
                    JSONObject object = ar.getJSONObject(i);

                    // DTO 클래스의 객체를 생성
                    ElectricityMeterDTO electDTO = new ElectricityMeterDTO();
                    ModemDTO modelDTO = new ModemDTO();

                    electDTO.setSerialCd(object.getString("serial_cd"));
                    modelDTO.setModemCd(object.getString("modem_cd"));
                    electDTO.setElectricityFilename(object.getString("electricity_filename"));
                    electDTO.setElectricitySaveDate(object.getString("electricity_save_date"));
                    electDTO.setModemDTO(modelDTO);
                    // list에 추가
                    itemList.add(electDTO);
                    i = i + 1;
                }
                Log.e("파싱 결과", itemList.toString());

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
        setContentView(R.layout.activity_meter_info);
        // home 버튼
        homeBtn = findViewById(R.id.homeBtn);

        // 초기화 작업
        itemList = new ArrayList<>();

        // xml파일에 디자인한 뷰 가져오기
        listView = (ListView)findViewById(R.id.listView);
        downloadView = (ProgressBar)findViewById(R.id.downloadView);

        // this, 행의 모양, 데이터
//        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listAdapter = new ListAdapter(this , itemList, R.layout.item_cell);
        listView.setAdapter(listAdapter);

        // 색을 만들고 높이를 지정해야 합니다. 이 순서는 바뀌면 안됩니다.
        listView.setDivider(new ColorDrawable(Color.RED)); //가로줄 색을 빨간색으로 변경
        listView.setDividerHeight(3); // 가로줄 Height 크기를 3으로 지정

        // Thread를 만들어서 실행
        new ItemThread().start();

        // 리스트 중 아이템 하나를 클릭했을 때의 처리
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String serialCd = itemList.get(position).getSerialCd();
//                Toast.makeText(MeterInfoActivity.this ,serialCd,Toast.LENGTH_LONG).show();
                getInfoFrom(serialCd);
            }
        });

        // homeBtn 클릭시 메인페이지로 이동
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToHome();
            }
        });
    }

    private void getInfoFrom(String serial_id){
        Intent intent = new Intent(MeterInfoActivity.this , MeterInfoDetailActivity.class);
        intent.putExtra("serial_id", serial_id);
        startActivity(intent);
    }
    private void backToHome(){
        Intent intent = new Intent(MeterInfoActivity.this , MainActivity.class);
        startActivity(intent);
    }
}