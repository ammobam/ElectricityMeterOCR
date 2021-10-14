package com.lfin.electricitymeterocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BarcodeDetectorActivity extends AppCompatActivity {

    public static final String TAG = "[IC]BarcodeDetectorActivity";

    private SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    //This class provides methods to play DTMF tones
    private ToneGenerator toneGen1;
    private TextView barcodeText;
    private String barcodeData;
    private Button barcodeDataBtn;

    String serialId;

    // 진행 상황을 출력하 프로그래스 바
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_detector);

        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC,100);
        surfaceView = findViewById(R.id.surface_view);
        barcodeText = findViewById(R.id.barcode_text);

        barcodeDataBtn = findViewById(R.id.barcodeDataBtn);
        barcodeDataBtn.setOnClickListener(view -> insertBarcodeDataFrom());

        // MeterInfoActivity에서 전달한 serial_id 데이터 읽어오기
        serialId = getIntent().getStringExtra("serial_id");
        //serialId = "serial_id";
        Log.e("onCreate]", "serialId :: "+ serialId);

        initialiseDetectorsAndSources();
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


    //TODO: 이미지를 캡쳐하는 기능구현시 파일 전송 추가
    //바코드 정보를 전송할 thread
    class barcodeThread extends Thread {
        String json;

        @Override
        public void run(){
            Message message = new Message();
            try{
                //다운로드 받을 주소 생성
                URL url = new URL(Common.SEVER_URL +"/insertbarcode");
                //URL에 연결
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setUseCaches(false);
                con.setConnectTimeout(30000);
                //con.setReadTimeout(10000);

                //파일을 제외한 파라미터 만들기
                Date date = new Date();
                java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


                //파일을 제외한 파라미터 만들기
                //보낼 데이터 키
                String[] dataName = {"modemId","serialId","modemFilename","updatedate"};
                //보낼 데이터 값
                String[] data = {barcodeText.getText().toString(),
                        serialId,
                        barcodeText.getText().toString(),
                        sdf.format(date)};


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
//                String fileName = "newMeterImage.jpg";
                String fileName = null;

                // 파일이 존재할 때에만 생성
//                if(fileName!=null){
//                    postDataBuilder.append(delimiter);
//                    postDataBuilder.append("Content-Disposition: form-data; name=\"" + "pictureurl" + "\";filename=\"" + fileName +"\"" + lineEnd);
//                }

                //파라미터를 서버에 전송
                DataOutputStream ds = new DataOutputStream(con.getOutputStream());
                ds.write(postDataBuilder.toString().getBytes());


                //파일 전송과 body 종료
                //파일이 있는 경우에는 파일을 전송
                if(fileName!=null){
                    ds.writeBytes(lineEnd);

//                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
//                    Bitmap bitmap = drawable.getBitmap();
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                    byte[] buffer = stream.toByteArray();
//                    ds.write(buffer, 0, buffer.length);

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


    private void insertBarcodeDataFrom() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //barcodeText.setText("12345678");
        String saveText = (String)barcodeText.getText();

        progressDialog = new ProgressDialog(BarcodeDetectorActivity.this);
        progressDialog.setMessage("문자인식 중입니다..");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

        progressDialog.show();


        if(!saveText.equals("BarcodeText")){
            builder.setTitle("바코드 정보 확인");
            builder.setMessage("제품번호: "+serialId +"\r"+"바코드번호: "+saveText);



            builder.setNegativeButton("취소",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id){
                    Snackbar.make(getWindow().getDecorView().getRootView(), "저장 취소",
                            Snackbar.LENGTH_SHORT).show();
                }
            });

            builder.setPositiveButton("저장",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id){
//                    Snackbar.make(getWindow().getDecorView().getRootView(), "Save Click",
//                            Snackbar.LENGTH_SHORT).show();
                    new BarcodeDetectorActivity.barcodeThread().start();

                    backToHome();
                }
            });

            AlertDialog alertDialog = builder.create();

            alertDialog.show();
        }else{
            Snackbar.make(getWindow().getDecorView().getRootView(), "바코드를 인식해 주세요!",
                    Snackbar.LENGTH_SHORT).show();
        }


    }

    private void initialiseDetectorsAndSources() {

        //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(BarcodeDetectorActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(BarcodeDetectorActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {


                    barcodeText.post(new Runnable() {

                        @Override
                        public void run() {

                            if(barcodes.valueAt(0).email != null) {
                                barcodeText.removeCallbacks(null);
                                barcodeData = barcodes.valueAt(0).email.address;
                                barcodeText.setText(barcodeData);
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                            } else {

                                barcodeData = barcodes.valueAt(0).displayValue;
                                barcodeText.setText(barcodeData);
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);

                            }
                        }
                    });

                }
            }
        });
    }

    private void backToHome(){
        Intent intent = new Intent(BarcodeDetectorActivity.this , MainActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();
        getSupportActionBar().hide();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
        initialiseDetectorsAndSources();
    }

}