/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.shequren.scancode.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import cn.shequren.scancode.CameraConfig;
import cn.shequren.scancode.DensityUtil;
import cn.shequren.scancode.R;
import cn.shequren.scancode.ScanCodeMangerUtils;
import cn.shequren.scancode.ScanCodeMode;
import cn.shequren.scancode.ScanCodeUtils;


public final class QrCodeFinderView extends RelativeLayout {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;

    private static final int MIN_FOCUS_BOX_WIDTH = 50;
    private static final int MIN_FOCUS_BOX_HEIGHT = 50;
    private static final int MIN_FOCUS_BOX_TOP = 200;

    private static Point ScrRes;
    private int top;

    private Paint mPaint;
    private int mScannerAlpha;
    private int mMaskColor;
    private int mFrameColor;
    private int mLaserColor;
    private int mTextColor;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;

    private Rect mFrameRect; //绘制的Rect
    private Rect mRect; //返回的Rect

    public QrCodeFinderView(Context context) {
        this(context, null);
    }

    public QrCodeFinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QrCodeFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        Resources resources = getResources();
        mMaskColor = resources.getColor(R.color.scan_code_viewfinder_mask);
        mFrameColor = resources.getColor(R.color.scan_code_possible_result_points);
        mLaserColor = resources.getColor(R.color.scan_code_my_greed);
        mTextColor = resources.getColor(R.color.scan_code_white);

        mFocusThick = 1;
        mAngleThick = 8;
        mAngleLength = 40;
        mScannerAlpha = 0;
        init(context);
        this.setOnTouchListener(getTouchListener());
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }
        // 需要调用下面的方法才会执行onDraw方法
        setWillNotDraw(false);

        Rect rect = getSaveRect();
        if (rect == null) {
            if (mFrameRect == null) {

                ScrRes = ScanCodeUtils.getScreenResolution(context);

                int width = ScrRes.x * 3 / 5;
                int height = width;

                width = width == 0
                        ? MIN_FOCUS_BOX_WIDTH
                        : width < MIN_FOCUS_BOX_WIDTH ? MIN_FOCUS_BOX_WIDTH : width;

                height = height == 0
                        ? MIN_FOCUS_BOX_HEIGHT
                        : height < MIN_FOCUS_BOX_HEIGHT ? MIN_FOCUS_BOX_HEIGHT : height;

                int left = (ScrRes.x - width) / 2;
                int top = (ScrRes.y - height) / 2;

                this.top = top; //记录初始距离上方距离
                mFrameRect = new Rect(left, top, left + width, top + height);
                mRect = mFrameRect;
            }
        } else {
            ScrRes = ScanCodeUtils.getScreenResolution(context);
            this.top = rect.top; //记录初始距离上方距离
            mFrameRect = new Rect(rect.left, rect.top, rect.left + rect.width(), rect.top + rect.height());
            mRect = mFrameRect;
        }

    }

    public Rect getRect() {
        return mRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        Rect frame = mFrameRect;
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 绘制焦点框外边的暗色背景
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        drawFocusRect(canvas, frame);
        drawAngle(canvas, frame);
        drawText(canvas, frame);
        drawLaser(canvas, frame);
    }

    /**
     * 画聚焦框，白色的
     *
     * @param canvas
     * @param rect
     */
    private void drawFocusRect(Canvas canvas, Rect rect) {
        // 绘制焦点框（黑色）
        mPaint.setColor(mFrameColor);
        // 上
        canvas.drawRect(rect.left + mAngleLength, rect.top, rect.right - mAngleLength, rect.top + mFocusThick, mPaint);
        // 左
        canvas.drawRect(rect.left, rect.top + mAngleLength, rect.left + mFocusThick, rect.bottom - mAngleLength,
                mPaint);
        // 右
        canvas.drawRect(rect.right - mFocusThick, rect.top + mAngleLength, rect.right, rect.bottom - mAngleLength,
                mPaint);
        // 下
        canvas.drawRect(rect.left + mAngleLength, rect.bottom - mFocusThick, rect.right - mAngleLength, rect.bottom,
                mPaint);
    }

    /**
     * 画四个角
     *
     * @param canvas
     * @param rect
     */
    private void drawAngle(Canvas canvas, Rect rect) {
        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(OPAQUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mAngleThick);
        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bottom = rect.bottom;
        // 左上角
        canvas.drawRect(left, top, left + mAngleLength, top + mAngleThick, mPaint);
        canvas.drawRect(left, top, left + mAngleThick, top + mAngleLength, mPaint);
        // 右上角
        canvas.drawRect(right - mAngleLength, top, right, top + mAngleThick, mPaint);
        canvas.drawRect(right - mAngleThick, top, right, top + mAngleLength, mPaint);
        // 左下角
        canvas.drawRect(left, bottom - mAngleLength, left + mAngleThick, bottom, mPaint);
        canvas.drawRect(left, bottom - mAngleThick, left + mAngleLength, bottom, mPaint);
        // 右下角
        canvas.drawRect(right - mAngleLength, bottom - mAngleThick, right, bottom, mPaint);
        canvas.drawRect(right - mAngleThick, bottom - mAngleLength, right, bottom, mPaint);
    }

    private void drawText(Canvas canvas, Rect rect) {
        int margin = 40;
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(getResources().getDimension(R.dimen.text_size_13sp));
        String text = getResources().getString(R.string.qr_code_auto_scan_notification);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
        float newY = rect.bottom + margin + offY;
        float left = (DensityUtil.getScreenWidth(ScanCodeMangerUtils.newInstance().mContext) - mPaint.getTextSize() * text.length()) / 2;
        canvas.drawText(text, left, newY, mPaint);
    }

    private void drawLaser(Canvas canvas, Rect rect) {
        // 绘制焦点框内固定的一条扫描线
        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(SCANNER_ALPHA[mScannerAlpha]);
        mScannerAlpha = (mScannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = rect.height() / 2 + rect.top;
        canvas.drawRect(rect.left + 2, middle - 1, rect.right - 1, middle + 2, mPaint);

        mHandler.sendEmptyMessageDelayed(1, ANIMATION_DELAY);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
        }
    };

    private OnTouchListener touchListener;

    private OnTouchListener getTouchListener() {

        if (touchListener == null)
            touchListener = new OnTouchListener() {

                int lastX = -1;
                int lastY = -1;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            lastX = -1;
                            lastY = -1;
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            int currentX = (int) event.getX();
                            int currentY = (int) event.getY();
                            try {
                                Rect rect = mFrameRect;
                                final int BUFFER = 50;
                                final int BIG_BUFFER = 60;
                                if (lastX >= 0) {

                                    if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER)
                                            || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                            && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER)
                                            || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) { //右上角
                                        updateBoxRect(2 * (lastX - currentX), (lastY - currentY), true);

                                    } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER)
                                            || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                            && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER)
                                            || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) { //左上角
                                        updateBoxRect(2 * (currentX - lastX), (lastY - currentY), true);

                                    } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER)
                                            || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                            && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER)
                                            || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {//右下角
                                        updateBoxRect(2 * (lastX - currentX), (currentY - lastY), false);

                                    } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER)
                                            || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                            && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER)
                                            || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {//左下角
                                        updateBoxRect(2 * (currentX - lastX), (currentY - lastY), false);

                                    } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER)
                                            || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
                                            && ((currentY <= rect.bottom && currentY >= rect.top)
                                            || (lastY <= rect.bottom && lastY >= rect.top))) { //左侧
                                        updateBoxRect(2 * (lastX - currentX), 0, false);

                                    } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER)
                                            || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
                                            && ((currentY <= rect.bottom && currentY >= rect.top)
                                            || (lastY <= rect.bottom && lastY >= rect.top))) { //右侧
                                        updateBoxRect(2 * (currentX - lastX), 0, false);

                                    } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER)
                                            || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
                                            && ((currentX <= rect.right && currentX >= rect.left)
                                            || (lastX <= rect.right && lastX >= rect.left))) { //上方
                                        updateBoxRect(0, (lastY - currentY), true);

                                    } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER)
                                            || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
                                            && ((currentX <= rect.right && currentX >= rect.left)
                                            || (lastX <= rect.right && lastX >= rect.left))) { //下方
                                        updateBoxRect(0, (currentY - lastY), false);

                                    }
                                }
                            } catch (NullPointerException e) {
                            }
                            v.invalidate();
                            lastX = currentX;
                            lastY = currentY;
                            return true;
                        case MotionEvent.ACTION_UP:
                            mHandler.removeMessages(1); //移除之前的刷新

                            mRect = mFrameRect; //松手时对外更新
                            lastX = -1;
                            lastY = -1;
                            saveUserChose(mRect);
                            return true;
                    }
                    return false;
                }
            };

        return touchListener;
    }

    private void updateBoxRect(int dW, int dH, boolean isUpward) {

        int newWidth = (mFrameRect.width() + dW > ScrRes.x - 4 || mFrameRect.width() + dW < MIN_FOCUS_BOX_WIDTH)
                ? 0 : mFrameRect.width() + dW;

        //限制扫描框最大高度不超过屏幕宽度
        int newHeight = (mFrameRect.height() + dH > ScrRes.x || mFrameRect.height() + dH < MIN_FOCUS_BOX_HEIGHT)
                ? 0 : mFrameRect.height() + dH;

        int leftOffset = (ScrRes.x - newWidth) / 2;

        if (isUpward) {
            this.top -= dH;
        }

        int topOffset = this.top;

        if (topOffset < MIN_FOCUS_BOX_TOP) {
            this.top = MIN_FOCUS_BOX_TOP;
            return;
        }

        if (topOffset + newHeight > MIN_FOCUS_BOX_TOP + ScrRes.x) {
            return;
        }

        if (newWidth < MIN_FOCUS_BOX_WIDTH || newHeight < MIN_FOCUS_BOX_HEIGHT)
            return;

        mFrameRect = new Rect(leftOffset, topOffset, leftOffset + newWidth, topOffset + newHeight);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeMessages(1);
    }


    private void saveUserChose(Rect rect) {
        boolean type = ScanCodeMangerUtils.newInstance().mType;
        if (type) {

            String Data =ScanCodeMangerUtils.newInstance().getGson().toJson(rect);
            ScanCodeMangerUtils.newInstance().putDdata(CameraConfig.SCAN_RECT_SIZE, Data);
        }
    }

    private Rect getSaveRect() {
        Rect returnData = null;
        boolean type = ScanCodeMangerUtils.newInstance().mType;

        if (type && ScanCodeMangerUtils.newInstance().mScanCodeMode != ScanCodeMode.QR_CODE) {

            String Data = (String) ScanCodeMangerUtils.newInstance().getData(CameraConfig.SCAN_RECT_SIZE, "");
            if (!TextUtils.isEmpty(Data)) {
                returnData = ScanCodeMangerUtils.newInstance().getGson().fromJson(Data, Rect.class);
            }
        }
        return returnData;
    }


}
