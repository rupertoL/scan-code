package cn.shequren.scancode.newscan;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


/**
 * @author weilu
 * Created by weilu on 2016/6/24.
 */
public abstract class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutResID());
        init(savedInstanceState);
    }


    protected abstract int setLayoutResID();

    /**
     * 初始化方法
     *
     * @param savedInstanceState 实例状态
     */
    protected abstract void init(Bundle savedInstanceState);


    public Context getContext() {
        return this;
    }


}
