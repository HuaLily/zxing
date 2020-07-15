package com.huawenli.zxing.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * @author ansen
 * @create time 2018/10/24
 */
public class ScreenUtil {
    //像素从sp到px
    public static int spToPx(Context context, float spValue) {
        // 获取DisplayMetrics实例
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, metrics);
        //TypedValue.applyDimension :Android系统中的非标准度量尺寸转变为标准度量尺寸 Android系统中的标准尺寸是px, 即像素
        //非标准单位: dp, in, mm, pt, sp
    }

    // 屏幕宽的像素个数
    public static int getWidthPixels() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int dpToPx(Context context,float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static final int dip2pix(Context context,float dip) {
        final float scale = context.getResources()
                .getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }
}
