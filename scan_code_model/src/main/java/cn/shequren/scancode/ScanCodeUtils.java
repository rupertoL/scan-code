package cn.shequren.scancode;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by jz on 2018/5/30.
 * 扫描工具类
 */

public class ScanCodeUtils {
    public static Point getScreenResolution(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();
        return new Point(width, height);
    }
}
