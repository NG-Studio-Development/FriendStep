package com.ngstudio.friendstep.components.cache;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;

import java.io.IOException;

public class RoundImageDecoder extends BaseImageDecoder {
	/**
	 * @param loggingEnabled Whether debug logs will be written to LogCat.
	 *                       Usually should match {@link com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder#writeDebugLogs() ImageLoaderConfiguration.writeDebugLogs()}
	 */
	public RoundImageDecoder(boolean loggingEnabled) {
		super(loggingEnabled);
	}

	@Override
	public Bitmap decode(ImageDecodingInfo decodingInfo) throws IOException {
		return roundBitmap(super.decode(decodingInfo));
	}


	private static final Paint paintMask   = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static final Paint paintBitmap = new Paint(/*Paint.ANTI_ALIAS_FLAG*/);

	static {
		paintMask.setColor(Color.BLACK);
		paintMask.setStyle(Paint.Style.FILL);
		paintMask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

		paintBitmap.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	}

	public static Bitmap roundBitmap(Bitmap bitmapIn) {
		if (bitmapIn == null || bitmapIn.isRecycled())
			return bitmapIn;

		final int w, h = w = Math.min(bitmapIn.getWidth(), bitmapIn.getHeight());
		Bitmap bitmapOut = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		if (bitmapOut == null)
			return bitmapIn;

		try {
			final float c = Math.min(bitmapOut.getWidth(), bitmapOut.getHeight()) / 2.f;

			Canvas canvas = new Canvas(bitmapOut);
			canvas.drawARGB(0, 0, 0, 0);
			canvas.drawCircle(c, c, c, paintMask);
			canvas.drawBitmap(bitmapIn, (w - bitmapIn.getWidth()) / 2.f, (h - bitmapIn.getHeight()) / 2.f, paintBitmap);

			return bitmapOut;
		} finally {
			bitmapIn.recycle();
		}
	}
}
