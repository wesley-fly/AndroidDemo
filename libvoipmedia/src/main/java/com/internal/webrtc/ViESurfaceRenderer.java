/*
 *  Copyright (c) 2011 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.internal.webrtc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.internal.voipmedia.util.Print;

import java.nio.ByteBuffer;


public class ViESurfaceRenderer implements Callback {

	private String TAG = getClass().getSimpleName();

	// the bitmap used for drawing.
	private Bitmap bitmap = null;
	private ByteBuffer byteBuffer;
	private SurfaceHolder surfaceHolder;
	// Rect of the source bitmap to draw
	private Rect srcRect = new Rect();
	// Rect of the destination canvas to draw to
	private Rect dstRect = new Rect();
	private int dstHeight = 0;
	private int dstWidth = 0;
	//ann:计算缩放比列及平移的位置
	private float scale=0;
	private float  widthTranslate=0;
	private float  heithtTrandlate = 0;

	private float dstTopScale = 0;
	private float dstBottomScale = 1;
	private float dstLeftScale = 0;
	private float dstRightScale = 1;

	private int mOldBitmapWidth;
	private int mOldBitmapHeight;
	
	 private PaintFlagsDrawFilter pfd;

	private static onTackPictureListener listener;
	private int mTop = 0;
	private int mLeft = 0;

	public ViESurfaceRenderer(SurfaceView view) {

		// ///////////////
		mTop = view.getTop();
		mLeft=view.getLeft();
		
		surfaceHolder = view.getHolder();
		if (surfaceHolder == null) {
			Print.w(TAG, "surfaceHolder is null");
			return;
		}

		Canvas canvas = surfaceHolder.lockCanvas();
		if (canvas != null) {
			Rect dst = surfaceHolder.getSurfaceFrame();
			if (dst != null) {
				dstRect = dst;
				dstHeight = dstRect.bottom - dstRect.top;
				dstWidth = dstRect.right - dstRect.left;
			}
			surfaceHolder.unlockCanvasAndPost(canvas);
		} else
			Print.w(TAG, "ViESurfaceRenderer ,canvas is null");

		surfaceHolder.addCallback(this);
		
		//2014/3/24 modiy zhanwei.zhao
		pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int in_width, int in_height) {

		Print.w(TAG, "surfaceChanged");

		dstHeight = in_height;
		dstWidth = in_width;
		dstRect.left = (int) (dstLeftScale * dstWidth);
		dstRect.top = (int) (dstTopScale * dstHeight);
		dstRect.bottom = (int) (dstBottomScale * dstHeight);
		dstRect.right = (int) (dstRightScale * dstWidth);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO(leozwang) Auto-generated method stub
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO(leozwang) Auto-generated method stub
	}

	public Bitmap CreateBitmap(int width, int height) {
		if (bitmap == null) {
			try {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
			} catch (Exception e) {
			}
		}
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		srcRect.left = 0;
		srcRect.top = 0;
		srcRect.bottom = height;
		srcRect.right = width;

		return bitmap;
	}

	public ByteBuffer CreateByteBuffer(int width, int height) {
		Print.w(TAG, "CreateByteBuffer width: " + String.valueOf(width) + " height:" + String.valueOf(height));
		if (bitmap == null) {
			try {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
			} catch (Exception e) {
			}
		}

		try {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			byteBuffer = ByteBuffer.allocateDirect(width * height * 2);
			srcRect.left = 0;
			srcRect.top = 0;
			srcRect.bottom = height;
			srcRect.right = width;
		} catch (Exception ex) {
			Print.w("*WEBRTC*", "Failed to CreateByteBuffer");
			bitmap = null;
			byteBuffer = null;
		}

		return byteBuffer;
	}

	public void SetCoordinates(float left, float top, float right, float bottom) {
		Print.w(TAG, "ViESurfaceRenderer SetCoordinates");
		dstLeftScale = left;
		dstTopScale = top;
		dstRightScale = right;
		dstBottomScale = bottom;

		dstRect.left = (int) (dstLeftScale * dstWidth);
		dstRect.top = (int) (dstTopScale * dstHeight);
		dstRect.bottom = (int) (dstBottomScale * dstHeight);
		dstRect.right = (int) (dstRightScale * dstWidth);

		Print.w(TAG, "SetCoordinates dstRect.width():" + dstRect.width() + " dstRect.height():" + dstRect.height());

	}

	public void DrawByteBuffer() {

		if (byteBuffer == null) {
			return;
		}

		byteBuffer.rewind();
		bitmap.copyPixelsFromBuffer(byteBuffer);
		DrawBitmap();
	}

	public void DrawBitmap() {
		if (null == bitmap){
			Print.d(TAG, "DrawBitmap bitmap is null");
			return;
		}
			

		Canvas canvas = surfaceHolder.lockCanvas();
		if (canvas != null) {

			int width = bitmap.getWidth();
			int heigth = bitmap.getHeight();

			if (width != mOldBitmapWidth || heigth != mOldBitmapHeight) {
				//computerRect(width, heigth);
				mOldBitmapHeight = heigth;
				mOldBitmapWidth = width;
			}
			canvas.setDrawFilter(pfd);



			computerScale(bitmap.getWidth(),bitmap.getHeight());
			//镜像
			Matrix matrix = new Matrix();
			matrix.setScale(-scale, scale);//等比列缩放
	       // matrix.postTranslate((float)scale*bitmap.getWidth(), 0);
			matrix.postTranslate(widthTranslate, heithtTrandlate);
			canvas.drawBitmap(bitmap, matrix, null);

			//canvas.scale(-mLeft, mTop, width + width / 2, heigth / 2);
			//canvas.drawBitmap(bitmap, srcRect, dstRect, null);


			if (null != listener) {
				listener.onCallback(bitmap);
			}
			// //////////////////
			surfaceHolder.unlockCanvasAndPost(canvas);

		}
	}

	// ////////take picture
	public static interface onTackPictureListener {
		void onCallback(Bitmap bitmap);
	}

	public static void setTackPictureListener(onTackPictureListener listener) {
		ViESurfaceRenderer.listener = listener;
	}

	//ann:计算缩放比列及平移的位置
	private void  computerScale(int width, int heigth) {
		float right  = (float)(dstRect.right-dstRect.left);
		float bottom = (float)(dstRect.bottom-dstRect.top);

		if (right > width && bottom > heigth) {
			// 显示区域 大于 bitmap图像 一般的情况,进行图像拉伸处理，原则上填充满显示区域
			float difW = right / width;
			float difH = bottom / heigth;

			if (difW > difH) {
				// 宽度的差异最大 宽度不变 高度进行拉伸
				scale = difW;
				double total = Math.ceil(heigth * difW);
				int difference = (int) Math.ceil((total - bottom) / 2);
				//图像左右镜像后，向x方向平移widthTranslate像素，向y方向平移heithtTrandlate像素
				heithtTrandlate = difference * -1;
				widthTranslate  = dstRect.right-dstRect.left;
			} else if (difW < difH) {
				// 高度方向的差异最大 高度不变宽度进行拉伸
				scale = difH;
				double total = Math.ceil(width * difH);
				int difference = (int) Math.ceil((total - right) / 2);
				heithtTrandlate = 0;
				widthTranslate  = (dstRect.right-dstRect.left)+Math.abs(difference);

			}else {
				scale = difH;
				heithtTrandlate = 0;
				widthTranslate  = dstRect.right-dstRect.left;
			}

		} else if (right < width && bottom < heigth) {
			// 显示区域小于bitmao图像 几乎不可能发生 如果发生 进行图像的缩放处理
			float difW =(float) right / width;   //  <1
			float difH =(float) bottom / heigth;  // <1

			//图像进行缩小处理
			if (difW > difH) {
				// 宽度的差异最大,为了填满显示区域，则以difH进行图像的缩小， 高度不变 宽度进行缩放
				scale = difH;
				double total = Math.ceil(width * difH);
				int difference = (int) Math.ceil((total - right) / 2);
				heithtTrandlate = 0;
				widthTranslate  = (dstRect.right-dstRect.left)+(Math.abs(difference) * -1);

			} else if (difW < difH) {
				// 高度方向的差异最大,为了填满显示区域，则以difw进行图像的缩小， 宽度不变高度进行缩放
				scale = difW;
				double total = Math.ceil(heigth * difW);
				int difference = (int) Math.ceil((total - bottom) / 2);
				heithtTrandlate = Math.abs(difference) * -1;
				widthTranslate  = dstRect.right-dstRect.left ;
			}else {
				scale = difW;
				//图像左右镜像后，向x方向平移widthTranslate像素，向y方向平移heithtTrandlate像素
				heithtTrandlate = 0;
				widthTranslate  = dstRect.right-dstRect.left ;
			}

		} else if (right == width && bottom < heigth) {
			// 宽度一样， 图像的高度大于 屏幕,上下裁图，图像需要向上移动
			scale = 1;
			int difH = (int) Math.ceil((heigth - bottom) / 2);
			heithtTrandlate = difH* -1;
			widthTranslate  = dstRect.right-dstRect.left ;

		} else if (right == width && bottom > heigth) {
			// 宽度一样， 图像的高度小于屏幕， 上下留黑边，图像需要向下平移difH
			scale = 1;
			int difH = (int) Math.ceil((bottom - heigth) / 2);
			heithtTrandlate = difH;
			widthTranslate  = dstRect.right-dstRect.left ;
		} else if (right > width && bottom == heigth) {
			// 高度一样，图像的宽度小于屏幕，左右留黑边
			scale = 1;
			int difW = (int) Math.ceil((right - width) / 2);
			heithtTrandlate = 0;
			widthTranslate  = dstRect.right-dstRect.left + difW ;

		} else if (right < width && bottom == heigth) {
			// 高度一样，图像的宽度大于屏幕,则左右裁图
			scale = 1;
			int difW = (int) Math.ceil((width - right) / 2);
			heithtTrandlate = 0;
			widthTranslate  =( dstRect.right-dstRect.left) + (difW*-1) ;
		}else {
			//图像的显示区域和bitmap图像一样大
			scale = 1;
			heithtTrandlate = 0;
			widthTranslate  = dstRect.right-dstRect.left;
		}
	}

	private void computerRect(int width, int heigth) {
		float right = dstRect.right;
		float bottom = dstRect.bottom;

		if (right > width && bottom > heigth) {
			// 显示区域 大于 bitmap图像 一般的情况
			float difW = right / width;
			float difH = bottom / heigth;

			if (difW > difH) {
				// 宽度的差异最大 宽度不变 高度进行缩放
				double total = Math.ceil(heigth * difW);
				int difference = (int) Math.ceil((total - bottom) / 2);
				dstRect.top = difference * -1;
				dstRect.bottom = difference + (int) bottom;
			} else if (difW < difH) {
				// 高度方向的差异最大 高度不变宽度进行缩放
				double total = Math.ceil(width * difH);
				int difference = (int) Math.ceil((total - right) / 2);
				dstRect.left = difference * -1;
				dstRect.right = difference + (int) right;
			}

		} else if (right < width && bottom < heigth) {
			// 显示区域小于bitmao图像 几乎不可能发生 如果发生 那么就显示中间区域就好了
			int difW = (int) Math.floor((width - right) / 2);
			int difH = (int) Math.floor((heigth - bottom) / 2);
			dstRect.left = difW * -1;
			dstRect.right = dstRect.right + difW;
			dstRect.top = difH * -1;
			dstRect.bottom = dstRect.bottom + difW;
		} else if (right == width && bottom < heigth) {
			// 宽度一样， 图像的高度大于 屏幕
			int difH = (int) Math.ceil((heigth - bottom) / 2);
			dstRect.top = difH * -1;
			dstRect.bottom = dstRect.bottom + difH;

		} else if (right == width && bottom > heigth) {
			// 宽度一样， 图像的高度小于 屏幕
			int difW = (int) Math.ceil((width * (bottom / heigth) - right) / 2);
			dstRect.left = difW * -1;
			dstRect.right = dstRect.right + difW;
		} else if (right > width && bottom == heigth) {
			// 高度一样，图像的宽度小于屏幕
			int difH = (int) Math.ceil((heigth * (right / width) - bottom) / 2);
			dstRect.top = difH * -1;
			dstRect.bottom = dstRect.bottom + difH;
		} else if (right < width && bottom == heigth) {
			// 高度一样，图像的宽度大于屏幕
			int difW = (int) Math.ceil((width - right) / 2);
			dstRect.left = difW * -1;
			dstRect.right = dstRect.right + difW;
		}
	}
}
