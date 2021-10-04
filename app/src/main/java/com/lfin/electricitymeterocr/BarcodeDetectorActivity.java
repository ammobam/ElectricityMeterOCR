package com.lfin.electricitymeterocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
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

import java.io.IOException;

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

    private void insertBarcodeDataFrom() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String saveText = (String)barcodeText.getText();

        if(!saveText.equals("BarcodeText")){
            builder.setTitle("바코드 정보 확인");
            builder.setMessage("제품번호: "+serialId +"\r"+"바코드번호: "+saveText);



            builder.setNegativeButton("취소",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id){
                    Toast.makeText(getApplicationContext(), "Cancel Click", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setPositiveButton("저장",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id){
                    Toast.makeText(getApplicationContext(), "Save Click", Toast.LENGTH_SHORT).show();
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