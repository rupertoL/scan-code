package cn.shequren.scancode.decode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import cn.shequren.scancode.R;


/**
 * 二维码解析管理。
 */
public class DecodeManager {

    public void showCouldNotReadQrCodeFromScanner(Context context, final OnRefreshCameraListener listener) {
        new AlertDialog.Builder(context).setTitle(R.string.scan_code_qr_code_notification)
                .setMessage(R.string.qr_code_could_not_read_qr_code_from_scanner)
                .setPositiveButton(R.string.qc_code_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (listener != null) {
                            listener.refresh();
                        }
                    }
                })
                .show();
    }

    public interface OnRefreshCameraListener {
        void refresh();
    }
}
