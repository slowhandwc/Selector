package com.slow.selector.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.slow.selector.R;

/**
 * 底部dialog
 * @Author wuchao
 * @Date 2020/4/8-11:17 PM
 * @description
 * @email 329187218@qq.com
 * @see Dialog
 */
public class BottomDialog extends Dialog {

    private BottomDialog(@NonNull Context context) {
        super(context, R.style.bottom_dialog);
    }

    public static Builder newBuilder(Context context){
        return new Builder(context);
    }

    public static class Builder{
        private Context context;
        private int contentViewResourceId;
        private int width;
        private int height;
        private float dimAmount;
        private int offsetX;
        private int offsetY;
        private int flag;
        private boolean isCancelable;
        private boolean isCancelOnTouchOutside;

        public Builder setContentViewResourceId(int resourceId){
            this.contentViewResourceId = resourceId;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setOffsetX(int x){
            this.offsetX = x;
            return this;
        }

        public Builder setOffsetY(int y){
            this.offsetY = y;
            return this;
        }

        public Builder setFlag(int flag){
            this.flag = flag;
            return this;
        }

        public Builder setDimAmount(float dimAmount) {
            this.dimAmount = dimAmount;
            return this;
        }

        public Builder setCancelable(boolean isCancelable){
            this.isCancelable = isCancelable;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean isCancelable){
            this.isCancelable = isCancelable;
            return this;
        }

        public Builder(Context context){
            this.context = context;
            this.width = WindowManager.LayoutParams.MATCH_PARENT;
            this.height = WindowManager.LayoutParams.WRAP_CONTENT;
            this.dimAmount = 0.4f;
            this.isCancelable = true;
            this.isCancelOnTouchOutside = true;
            this.contentViewResourceId = -1;
        }

        public BottomDialog create(){
            BottomDialog bottomDialog = new BottomDialog(context);
            Window window = bottomDialog.getWindow();
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setGravity(Gravity.BOTTOM);
            window.setDimAmount(dimAmount);
            if(contentViewResourceId>0){
                bottomDialog.setContentView(contentViewResourceId);
            }
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = width;
            layoutParams.height = height;
            if(offsetX>0){
                layoutParams.x = offsetX;
            }
            if (offsetY>0) {
                layoutParams.y = offsetY;
            }
            window.setAttributes(layoutParams);
            if(flag!=0){
                window.addFlags(flag);
            }
            bottomDialog.setCancelable(isCancelable);
            bottomDialog.setCanceledOnTouchOutside(isCancelOnTouchOutside);
            return bottomDialog;
        }
    }
}
