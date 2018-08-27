package cn.shequren.scancode;

/**
 * Created by jz on 2018/5/30.
 * 扫描模式
 */

public enum ScanCodeMode {
    BAR_CODE(1), QR_CODE(2), ALL(3);
    private int mType;

    ScanCodeMode(int type) {
        this.mType = type;
    }

    public int getType() {
        return mType;
    }
}
