/*
 * Copyright (C) 2010 ZXing authors
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

package cn.shequren.scancode.decode;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import cn.shequren.scancode.CameraConfig;
import cn.shequren.scancode.R;
import cn.shequren.scancode.ScanCodeMangerUtils;
import cn.shequren.scancode.newscan.ScanCodeNewActivity;
import cn.shequren.scancode.camera.NewScanPlanarYUVLuminanceSource;
import cn.shequren.scancode.camera.PlanarYUVLuminanceSource;


final class DecodeHandler extends Handler {

    private final ScanCodeNewActivity mActivity;
    private final MultiFormatReader mMultiFormatReader;
    private final Map<DecodeHintType, Object> mHints;
    private byte[] mRotatedData;

    DecodeHandler(ScanCodeNewActivity activity) {
        this.mActivity = activity;
        mMultiFormatReader = new MultiFormatReader();
        mHints = new Hashtable<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        Collection<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.EAN_13);
        barcodeFormats.add(BarcodeFormat.CODE_39);
        barcodeFormats.add(BarcodeFormat.CODE_128); //快递单常用格式39,128
        barcodeFormats.add(BarcodeFormat.QR_CODE); //二维码
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, barcodeFormats);
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.decode) {
            decode((byte[]) message.obj, message.arg1, message.arg2);

        } else if (message.what == R.id.quit) {
            Looper looper = Looper.myLooper();
            if (null != looper) {
                looper.quit();
            }

        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency, reuse the same reader
     * objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        if (null == mRotatedData) {
            mRotatedData = new byte[width * height];
        } else {
            if (mRotatedData.length < width * height) {
                mRotatedData = new byte[width * height];
            }
        }
        Arrays.fill(mRotatedData, (byte) 0);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x + y * width >= data.length) {
                    break;
                }
                mRotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        Result rawResult = null;
        try {
            Rect rect = mActivity.getCropRect();
            if (rect == null) {
                return;
            }

            LuminanceSource source = null;
            boolean type = (boolean) ScanCodeMangerUtils.newInstance().getData(CameraConfig.SCAN_PAGER_TYPE, false);
            if (!type) {
                source = new NewScanPlanarYUVLuminanceSource(mRotatedData, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
            } else {
                source = new PlanarYUVLuminanceSource(mRotatedData, width, height, rect.left, rect.top, rect.width(), rect.height());
            }

            BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
            rawResult = mMultiFormatReader.decode(bitmap1, mHints);

/*
            //适配文字识别代码
            if (mActivity.isQRCode()) {
                BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
                rawResult = mMultiFormatReader.decode(bitmap1, mHints);
            } else {
                Toast.makeText(MyApplication.mContext, "开始解析", Toast.LENGTH_SHORT).show();
                TessEngine tessEngine = TessEngine.Generate(MyApplication.mContext);
                Bitmap bitmap = source.renderCroppedGreyscaleBitmap();


                String result = tessEngine.detectText(bitmap);
                if (!TextUtils.isEmpty(result)) {
                    rawResult = new Result(result, null, null, null);
                    // rawResult.setBitmap(bitmap);
                }
                Toast.makeText(MyApplication.mContext, "解析完成:" + result, Toast.LENGTH_SHORT).show();
            }
*/

        } catch (Exception ignored) {
        } finally {
            mMultiFormatReader.reset();
        }

        if (rawResult != null) {
            Message message = Message.obtain(mActivity.getCaptureActivityHandler(), R.id.decode_succeeded, rawResult);
            message.sendToTarget();
        } else {
            Message message = Message.obtain(mActivity.getCaptureActivityHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }
}
