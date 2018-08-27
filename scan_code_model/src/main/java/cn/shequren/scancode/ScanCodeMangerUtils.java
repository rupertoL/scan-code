package cn.shequren.scancode;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import cn.shequren.scancode.newscan.ScanCodeNewActivity;

/**
 * Created by jz on 2018/5/30.
 * 扫描管理类
 */

public class ScanCodeMangerUtils {

    public Context mContext;
    /**
     * 扫描模式
     */
    public ScanCodeMode mScanCodeMode;
    /**
     * 是否保存上次改变后的大小
     */
    public boolean mType = true;
    /**
     * 返回值
     */
    public int mCode;
    /**
     * 是否允许切换算法
     */
    public boolean isNeedCahangPager = true;
    private SharedPreferences mPref;

    private boolean isRunning = false;

    public String resultDdataType = "data";
    /**
     * 需要的权限
     */
    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    public void setRunning(boolean running) {
        isRunning = running;
    }

    private static class Holder {
        private static ScanCodeMangerUtils mWebViewUtils = new ScanCodeMangerUtils();
    }

    /**
     * 设置是否保存报错调整后的窗口
     *
     * @param isChangDistinguishSize
     */
    public void setIsChangDistinguishSize(boolean isChangDistinguishSize) {

        mType = isChangDistinguishSize;
    }

    private ScanCodeMangerUtils() {

    }

    public static ScanCodeMangerUtils newInstance() {
        return Holder.mWebViewUtils;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Application context) {
        mContext = context;
        mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public Context getContext() {
        return mContext;
    }

    public void putDdata(String key, Object object) {
        if (mPref == null) {
            mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        SharedPreferences.Editor editor = mPref.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }

        editor.commit();

    }

    public Object getData(String key, Object object) {
        if (mPref == null) {
            mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        if (object instanceof String) {
            return mPref.getString(key, ((String) object));
        } else if (object instanceof Integer) {
            return mPref.getInt(key, ((Integer) object));
        } else if (object instanceof Boolean) {
            return mPref.getBoolean(key, ((Boolean) object));
        } else if (object instanceof Float) {
            return mPref.getFloat(key, ((Float) object));
        } else if (object instanceof Long) {
            return mPref.getLong(key, ((Long) object));
        }
        return null;

    }

    public void startIntent(final Activity activity, final ScanCodeMode scanCodeMode, final int requestCode, final int resultCode, final boolean needCahangPager, String resultDataType) {
        this.resultDdataType = resultDataType;
        startIntent(activity, scanCodeMode, requestCode, resultCode, needCahangPager);
    }

    /**
     * @param activity        需要启动的页面Activity
     * @param scanCodeMode    扫描模式（条形码, 二维码）
     * @param requestCode     请求码
     * @param resultCode      响应码
     * @param needCahangPager 是否需要切换页面
     */
    public void startIntent(final Activity activity, final ScanCodeMode scanCodeMode, final int requestCode, final int resultCode, final boolean needCahangPager) {

        if (mContext == null) {
            Toast.makeText(activity, "扫描控件为初始化", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!checkMorePermission()) {
            Toast.makeText(mContext, "没有相机权限，请您申请权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isRunning) {
            Toast.makeText(mContext, "扫码页面正在使用", Toast.LENGTH_SHORT).show();
            return;
        }

        mCode = resultCode;
        mScanCodeMode = scanCodeMode;
        isNeedCahangPager = needCahangPager;
        Intent intent = new Intent(activity, ScanCodeNewActivity.class);
        intent.putExtra(CameraConfig.RESPONSE_CODE, mCode);
        activity.startActivityForResult(intent, requestCode);
        isRunning = true;

    }

    protected boolean checkMorePermission() {
        boolean isAllCheck = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (mContext.checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    isAllCheck = true;
                    break;
                }
            }
            if (isAllCheck) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


    /**
     * @param activity        需要启动的页面Activity
     * @param scanCodeMode    扫描模式（条形码, 二维码）
     * @param code            请求响应码（请求响应码相同）
     * @param needCahangPager 是否需要切换页面
     */
    public void startIntent(final Activity activity, final ScanCodeMode scanCodeMode, final int code, final boolean needCahangPager) {
        startIntent(activity, scanCodeMode, code, code, needCahangPager);
    }

    /**
     * @param activity     需要启动的页面Activity
     * @param scanCodeMode 扫描模式（条形码, 二维码）
     * @param Code         请求响应码（请求响应码相同）
     */
    public void startIntent(final Activity activity, final ScanCodeMode scanCodeMode, final int Code) {
        startIntent(activity, scanCodeMode, Code, false);
    }
}
