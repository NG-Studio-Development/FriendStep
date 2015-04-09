package com.alexutils.helpers;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Display;
import android.view.WindowManager;

import com.alexutils.utility.MarkableInputStream;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    public static final int DESIRED_SIZE = 120;
    protected static final int MARKER = 128 * 1024;

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight, DecodeType decodeType) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while (DecodeType.BOTH_SHOULD_BE_EQUAL_OR_LESS.equals(decodeType) ? ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) :
                        DecodeType.ONE_SHOULD_BE_EQUAL_OR_LESS.equals(decodeType) ? ((height / inSampleSize) > reqHeight && (width / inSampleSize) > reqWidth) :
                        ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth)) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public enum DecodeType {
        BOTH_SHOULD_BE_EQUAL_OR_LESS, BOTH_SHOULD_BE_EQUAL_STRETCH, BOTH_SHOULD_BE_EQUAL_CUT, ONE_SHOULD_BE_EQUAL_OR_LESS, BOTH_SHOULD_BE_LARGER, BOTH_SHOULD_BE_EQUAL_IF_LARGER_OR_LESS, JUST_DECODE
    }

    private static Bitmap decodeStream(@NotNull InputStream imageStream, String filePath, int reqWidth, int reqHeight, DecodeType decodeType) throws IOException {
        Matrix matrix = new Matrix();
        matrix.setRotate(getBitmapRotation(filePath));

        MarkableInputStream input = new MarkableInputStream(imageStream);
        BitmapFactory.Options o = new BitmapFactory.Options();

        if(!DecodeType.JUST_DECODE.equals(decodeType)) {
            long mark = input.savePosition(MARKER);
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, o);
            if ((o.outWidth == -1) || (o.outHeight == -1))
                return null;

            o.inSampleSize = calculateInSampleSize(o, reqWidth, reqHeight, decodeType);

            o.inJustDecodeBounds = false;
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;

            input.reset(mark);
        }

        Bitmap temp = BitmapFactory.decodeStream(input, null, o);
        int width = temp.getWidth();
        int height = temp.getHeight();

        temp = Bitmap.createBitmap(temp, 0, 0, width, height, matrix, false);

        try {
            switch (decodeType) {
                case BOTH_SHOULD_BE_EQUAL_STRETCH:
                    return Bitmap.createScaledBitmap(temp,reqWidth,reqHeight,true);

                case BOTH_SHOULD_BE_EQUAL_CUT: {
                    float coefficient = getBestCoefficient(width, reqWidth, height, reqHeight, false);
                    temp = Bitmap.createScaledBitmap(temp, (int) (width/ coefficient), (int) (height / coefficient), true);

                    width = temp.getWidth();
                    height = temp.getHeight();

                    int difference;
                    if (temp.getHeight() > temp.getWidth()) {
                        return Bitmap.createBitmap(temp, 0, difference = (height - width) / 2, width,height - 2 * difference);
                    } else {
                        return Bitmap.createBitmap(temp,  difference = (width - height) / 2,0, width - 2 * difference,height);
                    }
                }

                case BOTH_SHOULD_BE_EQUAL_IF_LARGER_OR_LESS: {
                    if (temp.getHeight() <= reqHeight && temp.getWidth() <= reqWidth) {
                        return temp;
                    } else {
                        float coefficient = getBestCoefficient(width, reqWidth, height, reqHeight, true);
                        return Bitmap.createScaledBitmap(temp, (int) (width / coefficient), (int) (height / coefficient), true);
                    }
                }

                default:
                    return temp;
            }
        } finally {
            input.close();
        }
    }

    public static Bitmap decodeUri(@NotNull Context context,@NotNull Uri uri, int reqWidth, int reqHeight, @NotNull DecodeType decodeType) throws IOException {
        return decodeStream(context.getContentResolver().openInputStream(uri), ContentHelper.getPath(context, uri), reqWidth, reqHeight, decodeType);
    }

    public static Bitmap decodeFile(@NotNull File f, int reqWidth, int reqHeight, @NotNull DecodeType decodeType) throws IOException {
        return decodeStream(new FileInputStream(f),f.getAbsolutePath(),reqWidth,reqHeight,decodeType);
    }

    public static Bitmap decodeBase64(@NotNull String base64, int reqWidth, int reqHeight, @NotNull DecodeType decodeType) throws IOException{
        return decodeStream(new ByteArrayInputStream(Base64.decode(base64, Base64.DEFAULT)),null,reqWidth,reqHeight,decodeType);
    }

    public static float getBestCoefficient(int width, int reqWidth, int height, int reqHeight, boolean isMax) {
        float widthCoefficient = (float)width / reqWidth;
        float heightCoefficient = (float) height / reqHeight;
        return isMax ? Math.max(widthCoefficient,heightCoefficient) : Math.min(widthCoefficient, heightCoefficient);
    }

    private static int getExifOrientation(String mImagePath) {
        ExifInterface exif;
        int orientation = 0;
        try {
            exif = new ExifInterface( mImagePath );
            orientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 1 );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return orientation;
    }

    public static int getBitmapRotation(String path) {
        if (TextUtils.isEmpty(path))
            return 0;

        int rotation = 0;
        switch ( getExifOrientation(path) ) {
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }

        return rotation;
    }


    public static int getDisplayHeight(Activity activity) {
        return getDisplaySize(activity).y;
    }

    public static int getDisplayWidth(Activity activity) {
        return getDisplaySize(activity).x;
    }

    public static Point getDisplaySize(Activity activity) {
        Point size = new Point();

        WindowManager w = activity.getWindowManager();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
        }else{
            Display d = w.getDefaultDisplay();
            size.x = d.getWidth();
            size.y = d.getHeight();
        }
        return size;
    }


    public static Bitmap getScaledBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        float hCoef = (float) bitmap.getHeight() / reqHeight;
        float wCoef = (float) bitmap.getWidth() / reqWidth;
        float coef = Math.min(hCoef,wCoef);

        return Bitmap.createScaledBitmap(bitmap,(int) (coef * reqWidth),(int) (coef * reqHeight),true);
    }

    public static int[] getPixelsFromBitmap(Bitmap bmp) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        return pixels;
    }

    public static String convertBitmapToBase64(Bitmap src, boolean shoudRecycle) {
        if (src == null || src.isRecycled())
            return null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        try {
            return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        } finally {
            if(shoudRecycle)
                src.recycle();
        }
    }


}
