package com.ngstudio.friendstep.components.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;


import com.alexutils.helpers.BitmapUtils;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.BaseAvatarRequest;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.apache.http.HttpException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class AvatarBase64ImageDownloader extends BaseImageDownloader {

    public AvatarBase64ImageDownloader(Context context) {
        super(context);
    }

    public AvatarBase64ImageDownloader(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
    }

    public File getAppropriateCacheDir() {
        File cacheDir = context.getExternalCacheDir();
        return cacheDir != null
                ? cacheDir : context.getCacheDir();
    }

    private static final int PATH_BUCKET_IDX = 0;
    private static final int PATH_ITEM_IDX = 1;


    private static String getTempFilePrefix(String imageUri) {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static final String GEOMOBILE_SCHEME_PREFIX = "geomobile";
    private static final String GEOMOBLE_SCHEME = GEOMOBILE_SCHEME_PREFIX + "://";
    private static final String GEOMOBILE_SCHEME_FORMAT = GEOMOBLE_SCHEME + "%s";

    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        if (imageUri.startsWith(GEOMOBLE_SCHEME)) {
            try {
                String mobileNumber = imageUri.substring(GEOMOBLE_SCHEME.length());

                final File file = File.createTempFile(getTempFilePrefix(imageUri), "tmp", getAppropriateCacheDir());

                BaseAvatarRequest request = BaseAvatarRequest.getAvatarRequest(WhereAreYouApplication.getInstance().getUuid(), mobileNumber);
                String result = HttpServer.sendRequestToServer(request);

                if(TextUtils.isEmpty(result) || result.contains("No Avatar"))
                    return null;

                final String avatarBase64 = new JSONObject(result).getString("Avatar");
                Bitmap bitmap = BitmapUtils.decodeBase64(avatarBase64, 0, 0, BitmapUtils.DecodeType.JUST_DECODE);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file.getAbsolutePath()));
                bitmap.recycle();


                return new FileInputStream(file);
            } catch (IllegalArgumentException | HttpException | JSONException | NullPointerException e) {
                throw new IOException(e);
            }
        } // else if (imageUri.startsWith("scheme://")) {
        return super.getStreamFromOtherSource(imageUri, extra);
    }

    public static String  getImageUriFor(@NotNull String mobileContact) {
        return String.format(GEOMOBILE_SCHEME_FORMAT, mobileContact);
    }

}
