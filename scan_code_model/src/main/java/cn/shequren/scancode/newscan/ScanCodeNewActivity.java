package cn.shequren.scancode.newscan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.io.IOException;

import cn.shequren.scancode.CameraConfig;
import cn.shequren.scancode.R;
import cn.shequren.scancode.ScanCodeMangerUtils;
import cn.shequren.scancode.ScanCodeMode;
import cn.shequren.scancode.camera.CameraManager;
import cn.shequren.scancode.decode.CaptureActivityHandler;
import cn.shequren.scancode.decode.DecodeManager;
import cn.shequren.scancode.decode.InactivityTimer;
import cn.shequren.scancode.view.QrCodeFinderView;


/**
 * 二维码扫描类。
 */
public class ScanCodeNewActivity extends BaseActivity implements Callback, View.OnClickListener {

    private CaptureActivityHandler mCaptureActivityHandler;
    private boolean mHasSurface;
    private InactivityTimer mInactivityTimer;
    private QrCodeFinderView mQrCodeFinderView;
    private SurfaceView mSurfaceView;
    private ViewStub mSurfaceViewStub;
    private DecodeManager mDecodeManager = new DecodeManager();
    private SensorManager sensorManager;
    private long cahngTime;
    private CheckBox btn_light;
    private boolean mType;
    private int mCode;


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_scan_code;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mType = ScanCodeMangerUtils.newInstance().mType;
        mCode = getIntent().getIntExtra(CameraConfig.RESPONSE_CODE, 0);
        initView();
        initData();
        InitSensors();
    }

    /**
     * 初始化
     */
    private void initView() {
        mQrCodeFinderView = (QrCodeFinderView) findViewById(R.id.qr_code_view_finder);
        mSurfaceViewStub = (ViewStub) findViewById(R.id.qr_code_view_stub);
        mHasSurface = false;


        btn_light = (CheckBox) findViewById(R.id.btn_light);
        btn_light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CameraManager.get().setFlashLight(isChecked);
            }
        });
        setTitle();
        findViewById(R.id.tv_right).setOnClickListener(this);
        findViewById(R.id.topbar_left).setOnClickListener(this);
        if (!ScanCodeMangerUtils.newInstance().isNeedCahangPager) {
            findViewById(R.id.tv_right).setVisibility(View.GONE);
        }
    }

    private void setTitle() {
        String title;
        if (ScanCodeMangerUtils.newInstance().mScanCodeMode == ScanCodeMode.BAR_CODE) {
            title = "条形码扫描";
        } else if (ScanCodeMangerUtils.newInstance().mScanCodeMode == ScanCodeMode.QR_CODE) {
            title = "二维码扫描";
        } else {
            title = "二维码/条形码扫描";
        }
        boolean type = (boolean) ScanCodeMangerUtils.newInstance().getData(CameraConfig.SCAN_PAGER_TYPE, false);
        if (!type) {
            title = title + "*";
        }
        ((TextView) findViewById(R.id.topbar_title)).setText(title);
    }


    private void initData() {
        mInactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraManager.init();
        initCamera();
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        if (null == mSurfaceView) {
            mSurfaceViewStub.setLayoutResource(R.layout.scan_code_layout_surface_view);
            mSurfaceView = (SurfaceView) mSurfaceViewStub.inflate();
        }
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (mHasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.quitSynchronously();
            mCaptureActivityHandler = null;
        }
        CameraManager.get().setFlashLight(false);// 关闭闪光
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        if (null != mInactivityTimer) {
            mInactivityTimer.shutdown();
        }

        try {
            if (sensorEventListener != null && sensorManager != null) {
                sensorManager.unregisterListener(sensorEventListener);
            }
            CameraManager.get().stopPreview();
            CameraManager.get().closeDriver();
        } catch (Exception e) {
            // 关闭摄像头失败的情况下,最好退出该Activity,否则下次初始化的时候会显示摄像头已占用.
            finish();
        } finally {
            ScanCodeMangerUtils.newInstance().setRunning(false);
        }
        super.onDestroy();

    }

    /**
     * Handler scan result
     *
     * @param result
     */
    public void handleDecode(Result result) {
        mInactivityTimer.onActivity();
        if (null == result) {
            mDecodeManager.showCouldNotReadQrCodeFromScanner(this, new DecodeManager.OnRefreshCameraListener() {
                @Override
                public void refresh() {
                    restartPreview();
                }
            });
        } else {

            if (ScanCodeMangerUtils.newInstance().mScanCodeMode == ScanCodeMode.BAR_CODE) {
                int codeType = getCodeType(result);
                if (codeType != 1) {
                    Toast.makeText(ScanCodeMangerUtils.newInstance().mContext, "识别到的不是一维条形码", Toast.LENGTH_SHORT).show();
                    restartPreview();
                    return;
                }
            } else if (ScanCodeMangerUtils.newInstance().mScanCodeMode == ScanCodeMode.QR_CODE) {
                int codeType = getCodeType(result);
                if (codeType != 2) {
                    restartPreview();
                    Toast.makeText(ScanCodeMangerUtils.newInstance().mContext, "识别到的不是二维条形码", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            handleResult(result);
        }
    }

    private int getCodeType(Result result) {
        int CODE_TYPE = -1;      //标示 (1一维码、 2、二维码   3、其他码)
        String QR_CODE = "QR_CODE";           //二维码
        String DATA_MATRIX = "DATA_MATR";   //其他码

        if (null != result) {

            String mBarcodeFormat = result.getBarcodeFormat().toString();
            //扫描获取的 编码 不为空
            if (!TextUtils.isEmpty(mBarcodeFormat)) {

                if (mBarcodeFormat.equals(DATA_MATRIX)) {
                    CODE_TYPE = 3;
                } else if (mBarcodeFormat.equals(QR_CODE)) {
                    CODE_TYPE = 2;
                } else {
                    CODE_TYPE = 1;
                }
            }
        }
        return CODE_TYPE;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            if (!CameraManager.get().openDriver(surfaceHolder)) {
                return;
            }
        } catch (IOException e) {
            // 基本不会出现相机不存在的情况
            Toast.makeText(this, getString(R.string.qr_code_camera_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return;
        }
        mQrCodeFinderView.setVisibility(View.VISIBLE);
        findViewById(R.id.qr_code_view_background).setVisibility(View.GONE);
        if (mCaptureActivityHandler == null) {
            mCaptureActivityHandler = new CaptureActivityHandler(this);
        }
    }

    public void restartPreview() {
        if (null != mCaptureActivityHandler) {
            try {
                mCaptureActivityHandler.restartPreviewAndDecode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    public Handler getCaptureActivityHandler() {
        return mCaptureActivityHandler;
    }

    private void handleResult(Result result) {
        if (TextUtils.isEmpty(result.getText())) {
            mDecodeManager.showCouldNotReadQrCodeFromScanner(this, new DecodeManager.OnRefreshCameraListener() {
                @Override
                public void refresh() {
                    restartPreview();
                }
            });
        } else {
            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(200L);
            if (mType) {
                if (TextUtils.isEmpty(result.getText())) {
                    restartPreview();
                } else {// 扫描成功
                    Intent intent = new Intent();
                    intent.putExtra(ScanCodeMangerUtils.newInstance().resultDdataType, result.getText());
                    setResult(mCode, intent);
                    ScanCodeNewActivity.this.finish();

                }

            } else {
                mType = true;
                restartPreview();
            }
        }
    }


    /**
     * 函数功能：初始化传感器相关，包括传感器管理器和传感器类型
     */
    private void InitSensors() {
        //初始化传感器管理器
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);//给传感器初始化类型
        sensorManager.registerListener(sensorEventListener, sensor, 0);//注册传感器
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        /**
         * 函数功能：重写传感器参数改变时实现的方法
         * @param event
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!btn_light.isChecked()) {
                float value = event.values[0];
                long nowTime = System.currentTimeMillis();
                if (nowTime - cahngTime > 2 * 1000) {
                    if (value < 50) {
                        //  topbarRight.setVisibility(View.VISIBLE);
                        // 打开闪光灯
                        CameraManager.get().setFlashLight(true);
                        cahngTime = System.currentTimeMillis();
                    } else {
                        // 关闭闪光灯
                        // topbarRight.setVisibility(View.GONE);
                        CameraManager.get().setFlashLight(false);
                        cahngTime = System.currentTimeMillis();
                    }
                }
            }
        }

        /**
         * 当传感器参数精度改变时，可以通过本方法实现
         * @param sensor
         * @param accuracy
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    /**
     * 获取显示框大小
     *
     * @return
     */
    public Rect getCropRect() {
        return mQrCodeFinderView.getRect();
    }


    // (2)点击事件
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.topbar_left) {
            ScanCodeNewActivity.this.finish();
        } else if (i == R.id.tv_right) {
            boolean type = (boolean) ScanCodeMangerUtils.newInstance().getData(CameraConfig.SCAN_PAGER_TYPE, false);
            ScanCodeMangerUtils.newInstance().putDdata(CameraConfig.SCAN_PAGER_TYPE, !type);
            //setResult(CameraConfig.ZING_CODE_SWITCH);
            Toast.makeText(ScanCodeMangerUtils.newInstance().mContext, "扫码方案已切换成功", Toast.LENGTH_SHORT).show();
            setTitle();
        }
    }

}