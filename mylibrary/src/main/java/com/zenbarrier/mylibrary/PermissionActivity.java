package com.zenbarrier.mylibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

public class PermissionActivity extends Activity {

    public final static int REQUEST_CODE_MAIN_WEAR = 1;
    public final static int REQUEST_CODE_COMPLICATION = 2;
    public final static String KEY_EXTRA_REQUEST_CODE = "KEY_EXTRA_REQUEST_CODE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        int requestCode = intent.getIntExtra(KEY_EXTRA_REQUEST_CODE, 0);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE_MAIN_WEAR:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setResult(Activity.RESULT_OK, null);
                }else{
                    setResult(Activity.RESULT_CANCELED, null);
                }
                finish();
                break;
            case REQUEST_CODE_COMPLICATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setResult(Activity.RESULT_OK, null);
                }else{
                    setResult(Activity.RESULT_CANCELED, null);
                }
                finish();
                break;
        }
    }
}
