package com.lfin.electricitymeterocr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "[IC]MainActivity";

//    private ImageView imageView;
//    private TextView textView;
    private Button galleryBtn;
    private Button cameraBtn;
    private Button infoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        galleryBtn = findViewById(R.id.galleryBtn);
        galleryBtn.setOnClickListener(view -> getImageFromGallery());

        cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(view -> getImageFromCamera());

        infoBtn = findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(view -> getInfoFrom());

        //imageView = findViewById(R.id.imageView);
        //textView = findViewById(R.id.textView);
    }


    private void getImageFromGallery(){
        Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
        startActivity(intent);
    }

    private void getImageFromCamera(){
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }


    private void getInfoFrom(){
        Intent intent = new Intent(MainActivity.this, MeterInfoActivity.class);
        startActivity(intent);
    }

}