package com.example.kaich.nvmotiondetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;


import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    //UI
    private VideoView mVideoView;
    private MediaController mediaController;
    private BottomNavigationView mBottomNavigationView;
    private LinearLayout mLinearLayoutNavigation;
    private Button mChooseVideoBtn;
    private Button mAnalyzeVideoBtn;
    private Button mSaveVideoBtn;

    //permissions
    private static final String[] MY_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int MY_PERMISSIONS_CONSTANT = 1; //this is arbitrary

    //vision, graphics tools
    private DisplayMetrics metrics = new DisplayMetrics();
    private PhotoTools photoTools;
    private VisionProcess visionProcess;

    //file storage
    private Uri currentVideoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        photoTools = new PhotoTools(metrics);
        System.loadLibrary("opencv_java3"); //do NOT use Core.NATIVE_LIBRARY_NAME, OpenCV is already treated as local in jni
        visionProcess = new VisionProcess();

        //wait until permissions granted, then enable functionality
        requestPermissions();
    }


    private void requestPermissions(){
        ArrayList<String> neededPermissions = new ArrayList<String>();
        for(String permission: MY_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                neededPermissions.add(permission);
            }
        }

        if(neededPermissions.size() == 0){//initialize functionality, all permissions already granted
            initializeFunctionality();

        }else {
            String[] neededPermissionsArray = neededPermissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, neededPermissionsArray, MY_PERMISSIONS_CONSTANT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean cleared = true;
        for(int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PERMISSION_GRANTED) {
                cleared = false;
                break;
            }
        }
        if(cleared){//initialize functionality, all permissions have been granted
            initializeFunctionality();

        }else{
            Toast toast = Toast.makeText(this,"Permissions not granted, app needs all permissions in order to run", Toast.LENGTH_SHORT);
            toast.show();
            toast.cancel();
        }

    }

    private void initializeFunctionality(){
        mVideoView = findViewById(R.id.videoView);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);

        mBottomNavigationView = findViewById(R.id.bottomNavigationView);
        mLinearLayoutNavigation = findViewById(R.id.linearLayoutNavigation);
        mChooseVideoBtn = findViewById(R.id.chooseVideoBtn);
        mChooseVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, photoTools.RESULT_LOAD_VIDEO);
            }
        });
        mAnalyzeVideoBtn = findViewById(R.id.analyzeVideoBtn);
        mAnalyzeVideoBtn.setEnabled(false);
        mAnalyzeVideoBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });
        mSaveVideoBtn = findViewById(R.id.saveVideoBtn);
        mSaveVideoBtn.setEnabled(false);
        mSaveVideoBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });
        //TASK handle enable/disable of save and analyze
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == photoTools.RESULT_LOAD_VIDEO && resultCode == RESULT_OK  && !(data.equals(null))){
            currentVideoURI = data.getData();

            //TASK does this block do anything?
            String[] filePathColumn = {MediaStore.Video.Media.DATA};
            Cursor cursor = getContentResolver().query(currentVideoURI, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            cursor.close();
            //

            mVideoView.setVideoURI(currentVideoURI);
            mAnalyzeVideoBtn.setEnabled(true);
        }
    }
}
