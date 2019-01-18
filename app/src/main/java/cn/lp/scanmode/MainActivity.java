package cn.lp.scanmode;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import cn.shequren.scancode.CameraConfig;
import cn.shequren.scancode.ScanCodeMangerUtils;
import cn.shequren.scancode.ScanCodeMode;
import cn.shequren.scancode.newscan.ScanCodeNewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }


    /**
     * 请求授权
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {
            //调用打电话的方法
            ScanCodeMangerUtils scanCodeMangerUtils = ScanCodeMangerUtils.newInstance();
            scanCodeMangerUtils.mCode = Activity.RESULT_FIRST_USER;
            scanCodeMangerUtils.mScanCodeMode = ScanCodeMode.ALL;
            scanCodeMangerUtils.isNeedCahangPager = false;
            Intent intent = new Intent(this, ScanCodeNewActivity.class);
            intent.putExtra(CameraConfig.RESPONSE_CODE, Activity.RESULT_FIRST_USER);
            startActivityForResult(intent, Activity.RESULT_FIRST_USER);
          /*  ScanCodeMangerUtils.newInstance().startIntent(getActivity(),
                    ScanCodeMode.ALL, Activity.RESULT_FIRST_USER, false);*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ScanCodeMangerUtils scanCodeMangerUtils = ScanCodeMangerUtils.newInstance();
        scanCodeMangerUtils.mCode = Activity.RESULT_FIRST_USER;
        scanCodeMangerUtils.mScanCodeMode = ScanCodeMode.ALL;
        scanCodeMangerUtils.isNeedCahangPager = false;
        Intent intent = new Intent(this, ScanCodeNewActivity.class);
        intent.putExtra(CameraConfig.RESPONSE_CODE, Activity.RESULT_FIRST_USER);
        startActivityForResult(intent, Activity.RESULT_FIRST_USER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.RESULT_FIRST_USER && resultCode == Activity.RESULT_FIRST_USER) {

            if (data != null) {
                String resultdata = data.getStringExtra(ScanCodeMangerUtils.newInstance().resultDdataType);
                if (!TextUtils.isEmpty(resultdata)) {
                    Toast.makeText(MainActivity.this, resultdata, Toast.LENGTH_LONG).show();
                }
            }
        }


    }

}