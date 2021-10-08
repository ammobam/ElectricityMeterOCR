package com.lfin.electricitymeterocr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class GalleryActivity extends AppCompatActivity {


    public static final String TAG = "[IC]GalleryActivity";
    //gallery intent request code
    public static final int GALLERY_IMAGE_REQUEST_CODE = 1;

    //이미지를 표시
    private ImageView imageView;
    private TextView textView;
    //불러온 이미지 파일 이름
    private TextView filenameView;
    //갤러리로 가기
    private Button galleryBtn;
    //불러온 이미지 저장
    private Button insertaBtn;
    // 인식된 제조번호
    private String serial_cd;
    //갤러리에서 불러온 이미지 정보
    Uri selectedImage;

    // 진행 상황을 출력하 프로그래스 바
    ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        galleryBtn = findViewById(R.id.galleryBtn);
        galleryBtn.setOnClickListener(view -> getImageFromGallery());

        insertaBtn = findViewById(R.id.insertaBtn);
        insertaBtn.setOnClickListener(view -> getInsertFrom());

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        filenameView = findViewById(R.id.filenameView);
    }

    private void getImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
    }


    private void getInsertFrom(){

        final Message message = new Message();

        //데이터 유효성 검사
        message.what = 0;
        Log.e("imageView", String.valueOf(imageView));

        if(imageView != null){
//            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
//            Bitmap bitmap = drawable.getBitmap();

//        if(bitmap != null) {
            progressDialog = new ProgressDialog(GalleryActivity.this);
            progressDialog.setMessage("문자인식 중입니다..");
            progressDialog.setCancelable(true);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

            progressDialog.show();

            // Thread를 만들어서 실행
            new GalleryActivity.galleryThread().start();

        }else{
            message.obj = "이미지를 선택하세요";
            message.what = -1;

//            Snackbar.make(getWindow().getDecorView().getRootView(), "이미지를 선택하세요",
//                    Snackbar.LENGTH_SHORT).show();
             handler.sendMessage(message);
        }

    }


    //결과를 출력할 핸들러
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            String insertResult;

            switch(msg.what) {
                case -1:
                    insertResult = (String)msg.obj;
                    break;
                case 0:
                    insertResult = (String)msg.obj;
                    break;
                case 1:
                    boolean result = (Boolean)msg.obj;
                    if (result == true) {
                        insertResult = "삽입 성공";
                        // 삽입 성공 시 상세화면으로 이동
                        getInfoFrom(serial_cd);
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

    // 삽입 성공시 삽입화면으로 이동
    private void getInfoFrom(String serial_id){
        Intent intent = new Intent(GalleryActivity.this , MeterInfoDetailActivity.class);
        intent.putExtra("serial_id", serial_id);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK &&
                requestCode == GALLERY_IMAGE_REQUEST_CODE) {

            if (data == null) {
                return;
            }

            selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                if(Build.VERSION.SDK_INT >= 29) {
                    Uri fileUri = data.getData();
                    ContentResolver resolver = getContentResolver();
                    InputStream inputStream = resolver.openInputStream(fileUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);

                }
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to read Image", ioe);
            }

            if(bitmap != null) {
//                textView.setText(resultStr);
                imageView.setImageBitmap(bitmap);

                String fileName = getFileName(selectedImage);
                filenameView.setText(fileName);

            }
        }

    }

    // 이미지를 업로드 할 Thread 클래스
    class galleryThread extends Thread {
        @Override
        public void run(){
            Message message = new Message();
            try{
                //다운로드 받을 주소 생성
                URL url = new URL(Common.SEVER_URL +"/insertElectricityMeter");
                //URL에 연결
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //파일을 제외한 파라미터 만들기

                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                String[] dataName = {"updatedate"};

                //파일을 제외한 파라미터 만들기
                String[] data = {sdf.format(date)};


                // boundary생성 실행할때마다 다른값을 할당 : 파일 업로드가 있을 때는 반드시 생성
                String lineEnd = "\r\n";
                String boundary = UUID.randomUUID().toString();

                // 연결 객체 옵션 설정
                con.setRequestMethod("POST");
                con.setReadTimeout(10000);
                con.setConnectTimeout(10000);
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setUseCaches(false);

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

                Date file_date = new Date();
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd_hhmmss");
                String fileName = sdf2.format(file_date).toString() + ".jpg";
                Log.e(TAG,"fileName::"+fileName);
                //String fileName = getFileName(selectedImage); //이미지 원본 이름 파일로 변수 저장


                // 파일이 존재할 때에만 생성
                if(fileName!=null){
                    postDataBuilder.append(delimiter);
                    postDataBuilder.append("Content-Disposition: form-data; name=\"" + "pictureurl" + "\";filename=\"" + fileName +"\"" + lineEnd);
                }

                //파라미터 전송
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
                serial_cd = json.get("serial_cd").toString();
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

    // 파일 경로 찾기
    private String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //갤러리 파일 경로를 가져오는 메소드
    private String getFileName(Uri uri){
        String[] proj =  { MediaStore.Images.ImageColumns.DISPLAY_NAME };
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        cursor.moveToFirst();

        return  cursor.getString(column_index);
    }


}