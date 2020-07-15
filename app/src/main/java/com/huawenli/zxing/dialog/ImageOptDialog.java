package com.huawenli.zxing.dialog;

import android.app.Dialog;
import android.content.Context;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.huawenli.zxing.R;


/**
 * 这是长按图片弹出的保存还是识别图片的对话框
 * @author apple
 * @create time 2018/4/27
 */

public class ImageOptDialog extends Dialog {

    private Context context;
    private ImageOptCallback callback;

    public ImageOptDialog(@NonNull Context context) {
        super(context,R.style.bottom_dialog);
        setContentView(R.layout.dialog_image_opt);
        this.context = context;
        Window window = getWindow(); //得到window对象
        window.setGravity(Gravity.BOTTOM); //底部位置
        WindowManager.LayoutParams params = window.getAttributes(); //获取window 的params 然后给params去设置x y 参数
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        setCancelable(true); //dialog弹出后会点击屏幕或物理返回键，dialog消失
        setCanceledOnTouchOutside(true); //dialog弹出后会点击屏幕，dialog不失；点击物理返回键dialog消失

        findViewById(R.id.tv_cancel).setOnClickListener(clickListener); //取消
        findViewById(R.id.tv_identify_qr).setOnClickListener(clickListener); //识别
        findViewById(R.id.tv_save_image).setOnClickListener(clickListener); //保存图片

    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
            switch (view.getId()) {
                case R.id.tv_identify_qr:
                    if (callback!=null){
                        callback.onIdentifyQrClick();
                    }
                    dismiss();
                    break;
                case R.id.tv_cancel:
                    dismiss();
                    break;
                case R.id.tv_save_image:
                    if (callback!=null){
                        callback.onSaveImageClick();
                    }
                    dismiss();
                    break;
            }
        }
    };

    public void setCallback(ImageOptCallback callback) {
        this.callback = callback;
    }

    public interface  ImageOptCallback{
        void onIdentifyQrClick();//识别二维码

        void onSaveImageClick();//保存图片
    }
}
