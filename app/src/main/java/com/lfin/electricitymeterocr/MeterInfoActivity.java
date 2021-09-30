package com.lfin.electricitymeterocr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MeterInfoActivity extends AppCompatActivity {
    /**
     * json파싱을 위한 선언부
     **/
    // 다운로드받은 문자열을 저장할 변수를 선언
    String json;
    // 파싱한 결과가 여러개 이므로 List선언
    List<Meter> itemList;

    /**
     * View를 위한 선언부
     **/
    // 목록을 출력할 ListView
    ListView listView;

    // 데이터와 뷰를 이어줄 Adapter 변수
    MeterAdapter meterAdapter;

    // 진행 상황을 출력하 프로그래스 바
    ProgressBar downloadView;

    // 다운로드 받을 Thread 클래스
    class ItemThread extends Thread {
        public void run(){
            try {
                // 웹 서버 url설정
                URL url = new URL("http://172.30.1.54:5000/list");
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
    }
}