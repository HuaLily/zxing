/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawenli.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;


import com.google.zxing.ResultPoint;
import com.huawenli.zxing.R;
import com.huawenli.zxing.utils.ScreenUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ScannerView extends View {
	private static final long LASER_ANIMATION_DELAY_MS = 100l;
	private static final int DOT_OPACITY = 0xa0;//160

	private static final int DOT_TTL_MS = 500;

	private final Paint maskPaint;
	private final Paint laserPaint;
	private final Paint dotPaint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	private final Map<ResultPoint, Long> dots = new HashMap<ResultPoint, Long>(
			16);
	private Rect frame, framePreview;
	private final Paint textPaint;

	public ScannerView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		final Resources res = getResources();
		maskColor = res.getColor(R.color.scan_mask);
		resultColor = res.getColor(R.color.scan_result_view);
		final int laserColor = res.getColor(R.color.scan_laser);
		final int dotColor = res.getColor(R.color.scan_dot);

		maskPaint = new Paint();
		maskPaint.setStyle(Style.FILL); //填充

		textPaint = new Paint();
		textPaint.setColor(Color.parseColor("#FFFFFF"));
		textPaint.setStyle(Style.FILL_AND_STROKE); //填充内部和描边
		textPaint.setAntiAlias(true);//抗锯齿
		textPaint.setTextAlign(Paint.Align.CENTER); //设置对齐方式
		textPaint.setTextSize(ScreenUtil.spToPx(context,16));

		int DOT_SIZE = ScreenUtil.dip2pix(context,3); //dot 尺寸

		laserPaint = new Paint();
		laserPaint.setColor(laserColor);
		laserPaint.setStrokeWidth(DOT_SIZE);//设置线条宽度 同时向两边进行扩展
		laserPaint.setStyle(Style.STROKE); // 将画笔设置为描边

		dotPaint = new Paint();
		dotPaint.setColor(dotColor);
		dotPaint.setAlpha(DOT_OPACITY);//设置透明度
		dotPaint.setStyle(Style.STROKE);
		dotPaint.setStrokeWidth(DOT_SIZE);
		dotPaint.setAntiAlias(true);////抗锯齿
	}

	public void setFraming(final Rect frame,
			final Rect framePreview) {
		this.frame = frame;
		this.framePreview = framePreview;
		invalidate();//请求重绘View树
	}

	public void drawResultBitmap(final Bitmap bitmap) {
		resultBitmap = bitmap;
		invalidate();
	}

	public void addDot(final ResultPoint dot) {
		dots.put(dot, System.currentTimeMillis());

		invalidate();
	}

	@Override
	public void onDraw(final Canvas canvas) {
		if (frame == null)
			return;

		final long now = System.currentTimeMillis();

		final int width = canvas.getWidth();
		final int height = canvas.getHeight();

		// draw mask darkened
		//画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
		//扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
		maskPaint.setColor(resultBitmap != null ? resultColor : maskColor);
		canvas.drawRect(0, 0, width, frame.top, maskPaint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, maskPaint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				maskPaint);
		canvas.drawRect(0, frame.bottom + 1, width, height, maskPaint);

		Rect rect = new Rect();
		//将TextView 的文本放入一个矩形中， 测量TextView的高度和宽度
		//这个方法将文字的区域传递到 Rect
		textPaint.getTextBounds(getResources().getString(R.string.scan_qr_code_warn), 0, getResources().getString(R.string.scan_qr_code_warn).length(), rect);
		canvas.drawText(getResources().getString(R.string.scan_qr_code_warn), ScreenUtil.getWidthPixels()/2,frame.bottom+rect.height()+ScreenUtil.dpToPx(getContext(),7),textPaint);

		if (resultBitmap != null) {
			canvas.drawBitmap(resultBitmap, null, frame, maskPaint);
		} else {

			// draw red "laser scanner" to show decoding is active
			final boolean laserPhase = (now / 600) % 2 == 0;
			laserPaint.setAlpha(laserPhase ? 160 : 255);
			canvas.drawRect(frame, laserPaint);

			// draw points
			final int frameLeft = frame.left;
			final int frameTop = frame.top;
			final float scaleX = frame.width() / (float) framePreview.width();
			final float scaleY = frame.height() / (float) framePreview.height();

			for (final Iterator<Map.Entry<ResultPoint, Long>> i = dots
					.entrySet().iterator(); i.hasNext();) {
				final Map.Entry<ResultPoint, Long> entry = i.next();
				final long age = now - entry.getValue();
				if (age < DOT_TTL_MS) {
					dotPaint.setAlpha((int) ((DOT_TTL_MS - age) * 256 / DOT_TTL_MS));

					final ResultPoint point = entry.getKey();
					canvas.drawPoint(frameLeft + (int) (point.getX() * scaleX),
							frameTop + (int) (point.getY() * scaleY), dotPaint);
				} else {
					i.remove();
				}
			}

			// schedule redraw
			//只刷新扫描框的内容，其他地方不刷新
			postInvalidateDelayed(LASER_ANIMATION_DELAY_MS);
		}
	}
}
