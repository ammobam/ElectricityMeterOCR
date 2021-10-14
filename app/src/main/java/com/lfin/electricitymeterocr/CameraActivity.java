package com.lfin.electricitymeterocr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class CameraActivity extends AppCompatActivity {

    public static final String TAG = "[IC]CameraActivity";
    //camera intent request code
    static final int CAMERA_REQUEST_CODE = 1;

    private ImageView imageView;
    private TextView textView;
    private Button cameraBtn;
    private Button insertBtn;


    // 이미지를 안드로이드 10.0미만 버전에서 사용하기 위한 변수
    private static final String KEY_SELECTED_URI = "KEY_SELECTED_URI";
    private Uri selectedImageUri;

    //바코드 테스트
    private Button barcodeBtn;

    // 진행 상황을 출력하 프로그래스 바
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //카메라 촬영 버튼
        cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(view -> getImageFromCamera());

        //이미지를 저장하는 버튼
        insertBtn = findViewById(R.id.insertBtn);
        insertBtn.setOnClickListener(view -> getInsertInfo());

        barcodeBtn = findViewById(R.id.barcodeBtn);
        barcodeBtn.setOnClickListener(view -> getBarcodeFrom());

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());

    }



    //결과를 출력할 핸들러
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            String insertResult;
            switch(msg.what) {
                case 0:
                    insertResult = (String)msg.obj;
                    break;
                case 1:
                    boolean result = (Boolean)msg.obj;
                    if (result == true) {
                        insertResult = "삽입 성공";
                    }else {
                        insertResult = "삽입 실패";
                    }
                    break;
                default:
                    insertResult = "결과 알 수 없음";
                    break;
            }
            Snackbar.make(getWindow().getDecorView().getRootView(), insertResult,
                    Snackbar.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    };


    // 이미지를 업로드 할 Thread 클래스
    class cameraThread extends Thread {
        String json;

        @Override
        public void run(){
            Message message = new Message();
            try{
                //다운로드 받을 주소 생성
                URL url = new URL(Common.SEVER_URL +"/insertElectricityMeter");
                //URL에 연결
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setUseCaches(false);
                con.setConnectTimeout(30000);
                //con.setReadTimeout(10000);

                //파일을 제외한 파라미터 만들기
                Date date = new Date();
                java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                String[] dataName = {"updatedate"};

                //파일을 제외한 파라미터 만들기
                String[] data = {sdf.format(date)};


                // boundary생성 실행할때마다 다른값을 할당 : 파일 업로드가 있을 때는 반드시 생성
                String lineEnd = "\r\n";
                String boundary = UUID.randomUUID().toString();

                // 연결 객체 옵션 설정
                con.setRequestMethod("POST"); //전송방식 설정
                con.setDoOutput(true);
                con.setDoInput(true);


                // 파일 업로드가 있는 경우 설정
                con.setRequestProperty("ENCTYPE", "multipart/form-data");
                con.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);

                //파라미터 생성
                String delimiter = "--" + boundary + lineEnd; // --androidupload\r\n
                StringBuffer postDataBuilder = new StringBuffer();
                for(int i=0;i<data.length;i++){
                    postDataBuilder.append(delimiter);
                    postDataBuilder.append("Content-Disposition: form-data; name=\"" + dataName[i] +"\""+lineEnd+lineEnd+data[i]+lineEnd);
                }

                //파일이름 설정
                //서버에서 변경할예정
                String fileName = "newMeterImage.jpg";

                // 파일이 존재할 때에만 생성
                if(fileName!=null){
                    postDataBuilder.append(delimiter);
                    postDataBuilder.append("Content-Disposition: form-data; name=\"" + "pictureurl" + "\";filename=\"" + fileName +"\"" + lineEnd);
                }

                //파라미터를 서버에 전송
                DataOutputStream ds = new DataOutputStream(con.getOutputStream());
                ds.write(postDataBuilder.toString().getBytes());

                //파일 전송과 body 종료
                //파일이 있는 경우에는 파일을 전송
                if(fileName!=null){
                    ds.writeBytes(lineEnd);

                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] buffer = stream.toByteArray();
                    ds.write(buffer, 0, buffer.length);

                    ds.writeBytes(lineEnd);
                    ds.writeBytes(lineEnd);
                    ds.writeBytes("--" + boundary + "--" + lineEnd); // requestbody end

                }
                //파일이 없는 경우에는 body의 종료만 생성
                else {
                    ds.writeBytes(lineEnd);
                    ds.writeBytes("--" + boundary + "--" + lineEnd); // requestbody end
                }

                ds.flush();
                ds.close();
                //문자열을 다운로드 받기 위한 스트림을 생성
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                //문자열을 읽어서 저장
                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    sb.append(line + "\n");
                }
                //사용한 스트림과 연결 해제
                br.close();
                con.disconnect();

                JSONObject json =  new JSONObject(sb.toString());
                String result = json.get("result").toString();
                Log.e("result", json.toString());

                if(result.equals("true") || result.equals("True")){
                    message.obj = true;
                }else{
                    message.obj = false;
                }

                message.what = 1;
                handler.sendMessage(message);

            }catch(Exception e){
                Log.e("삽입 예외", e.getMessage());
                message.obj = "삽입 에러로 파라미터 전송에 실패했거나 다운로드 실패\n서버를 확인하거나 파라미터 전송 부분을 확인하세요";
                message.what = 0;
                handler.sendMessage(message);

            }


        }
    }


    private void getImageFromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);

    }

    private void getInsertInfo(){
        final Message message = new Message();

        progressDialog = new ProgressDialog(CameraActivity.this);
        progressDialog.setMessage("문자인식 중입니다..");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

        progressDialog.show();


        //데이터 유효성 검사
        message.what = 0;

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();


        if(bitmap != null) {
            // Thread를 만들어서 실행
            new CameraActivity.cameraThread().start();

        }else{
            message.obj = "이미지를 선택하세요";
            handler.sendMessage(message);
        }

    }

    //바코드 테스트
    private void getBarcodeFrom(){
        Intent intent = new Intent(CameraActivity.this, BarcodeDetectorActivity.class);
        intent.putExtra("serial_id", "0000000");
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 카메라 촬영 후, 확인버튼을 눌렀을 경우에만 처리
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {

            Bitmap bitmap = null;
            // 메모리가 부족한 상황을 대비하여 try catch
            try {

                if (Build.VERSION.SDK_INT >= 29) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else {
//                    bitmap = MediaStore.Images.Media.getBitmap(
//                            getContentResolver(), selectedImageUri);

                    bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(selectedImageUri), null, null);
                }
            } catch (Exception e) {
                Log.e(TAG, "이미지 가져오기 실패");

            }

            if (bitmap != null) {
                //                textView.setText(resultStr);
                imageView.setImageBitmap(bitmap);
            }
        }
    }






}