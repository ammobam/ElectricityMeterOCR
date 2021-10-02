package com.lfin.electricitymeterocr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraActivity extends AppCompatActivity {

    public static final String TAG = "[IC]CameraActivity";
    //camera intent request code
    static final int CAMERA_REQUEST_CODE = 1;



    private ImageView imageView;
    private TextView textView;
    private Button cameraBtn;
    private Button insertaBtn;


    // 이미지를 안드로이드 10.0미만 버전에서 사용하기 위한 변수
    private static final String KEY_SELECTED_URI = "KEY_SELECTED_URI";
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(view -> getImageFromCamera());

        insertaBtn = findViewById(R.id.insertaBtn);
        insertaBtn.setOnClickListener(view -> getInsertInfo());

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
    }

    private void getImageFromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }

    private void getInsertInfo(){
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, CAMERA_REQUEST_CODE);
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
                    bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), selectedImageUri);
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