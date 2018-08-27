package cn.shequren.scancode;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * @author weilu
 * 屏幕信息获取数值的转换工具类
 * */
public class DensityUtil {

	/**
	 * 获取屏幕宽
	 *
	 * @param context 上下文
	 * @return 宽
	 */
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	/**
	 * 获取屏幕高
	 *
	 * @param context 上下文
	 * @return 高
	 */
	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.heightPixels;
	}

	/**
	 * 获取状态栏高度
	 */
	public static int getStatusHeight(Activity activity) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		activity.getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass
						.getField("status_bar_height").get(localObject)
						.toString());
				statusHeight = activity.getResources()
						.getDimensionPixelSize(i5);
			} catch (ClassNotFoundException | IllegalAccessException | NumberFormatException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

	/**
	 * 获取导航栏高度
	 */
	public static int getNavigationBarHeight(Activity mActivity) {
		int height = 0;
		boolean hasNavigationBar = false;
		Resources rs = mActivity.getResources();
		int id = rs
				.getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) {
			hasNavigationBar = rs.getBoolean(id);
		}
		try {
			Class systemPropertiesClass = Class
					.forName("android.os.SystemProperties");
			Method m = systemPropertiesClass.getMethod("get", String.class);
			String navBarOverride = (String) m.invoke(systemPropertiesClass,
					"qemu.hw.mainkeys");
			if ("1".equals(navBarOverride)) {
				hasNavigationBar = false;
			} else if ("0".equals(navBarOverride)) {
				hasNavigationBar = true;
			}
			if (hasNavigationBar) {
				int resourceId = rs.getIdentifier("navigation_bar_height",
						"dimen", "android");
				height = rs.getDimensionPixelSize(resourceId);
			} else {
				height = 0;
			}
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
		return height;
	}

	/**
	 *  将px值转换为dip或dp值，保证尺寸大小不变
	 *  @param pxValue
	 *  @param context
	 * （DisplayMetrics类中属性density）
	 *  @return 转换结果
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 *  将dip或dp值转换为px值，保证尺寸大小不变
	 *  @param dipValue
	 *  @param context
	 * （DisplayMetrics类中属性density）
	 *  @return 转换结果
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 *  将px值转换为sp值，保证文字大小不变
	 *  @param pxValue
	 *  @param context *
	 * （DisplayMetrics类中属性scaledDensity）
	 *  @return 转换结果
	 */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 *  将sp值转换为px值，保证文字大小不变
	 *  @param spValue
	 *  @param context
	 * （DisplayMetrics类中属性scaledDensity）
	 *  @return 转换结果
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}