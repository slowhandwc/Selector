package com.slow.selector.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.slow.selector.R;

/**
 * loading Dialog
 * @Author wuchao
 * @Date 2020/4/13-10:10 AM
 * @description
 * @email 329187218@qq.com
 * @see
 */
public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context,R.style.base_dialog);
        init(context);
    }

    private void init(Context context){
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.widget_loading_dialog);
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setDimAmount(0);
        window.setAttributes(layoutParams);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}
