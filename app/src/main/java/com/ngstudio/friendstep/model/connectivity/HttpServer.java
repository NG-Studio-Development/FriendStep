package com.ngstudio.friendstep.model.connectivity;


import android.net.ConnectivityManager;
import android.util.Log;
import android.util.Pair;

import com.ngstudio.friendstep.model.connectivity.requests.BaseRequest;
import com.ngstudio.friendstep.model.connectivity.requests.RequestType;
import com.ngstudio.friendstep.utils.WhereAreYouAppLog;


import org.apache.http.HttpException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;




public class HttpServer {

    //private static final String TAG = HttpServer.class.getSimpleName();
    private static final String TAG = "HTTP_SERVER";

    private static final int DEFAULT_TIMEOUT = 10000;

    public static <Request extends BaseRequest> String sendRequestToServer(final Request request) throws HttpException {
        Log.d(TAG, "start task");
        HttpURLConnection httpConnection = null;
        try {
            URL myUrl = new URL(request.getUrl());
            httpConnection = (HttpURLConnection) myUrl.openConnection();
            Log.i(TAG, "url = " + request.getUrl());
            if (request.getRequestType() == RequestType.POST || request.getRequestType() == RequestType.PUT || request.getRequestType() == RequestType.DELETE) {
                httpConnection.setDoOutput(true);
            }
            httpConnection.setDoInput(true);
            httpConnection.setUseCaches(false);
            httpConnection.setConnectTimeout(DEFAULT_TIMEOUT);
            httpConnection.setReadTimeout(DEFAULT_TIMEOUT);
            request.setRequestProperties(httpConnection);
            httpConnection.connect();

            if (request.getRequestType() == RequestType.POST || request.getRequestType() == RequestType.PUT || request.getRequestType() == RequestType.DELETE) {
                OutputStream connectionOutputStream = httpConnection.getOutputStream();
                request.writeData(connectionOutputStream);
                connectionOutputStream.close();
            }

            int responseCode = httpConnection.getResponseCode();
            Log.d(TAG, "code = " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String result = convertInputStreamToString(httpConnection.getInputStream());
                Log.d(TAG, "result = " + result);
                return result;
            } else {
                String error = convertInputStreamToString(httpConnection.getErrorStream());
                Log.d(TAG, "error = " + error);
                throw new HttpException(error);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpConnection != null)
                httpConnection.disconnect();
        }
        return null;
    }

    public static <Request extends BaseRequest> void submitToServer(@NotNull final Request request, @Nullable final BaseResponseCallback<String> callback) {
        NetworkExecutor.Task<Pair<String,Exception>, Void> task = new NetworkExecutor.Task<Pair<String,Exception>, Void>(ConnectivityManager.TYPE_MOBILE) {
            @Override
            public Pair<String,Exception> doInBackground() {
                String result = null;
                Exception exception = null;
                try {
                    result = sendRequestToServer(request);
                } catch (HttpException e) {
                    WhereAreYouAppLog.e(e.getMessage());
                    exception = e;
                }
                return new Pair<>(result,exception);
            }

            @Override
            public void onFinish(Pair<String,Exception> result) {
                if(result == null)
                    return;

                if(callback == null)
                    return;

                if(result.second == null)
                    callback.onSuccess(result.first);
                else
                    callback.onError(result.second);
            }
        };

        NetworkExecutor.submit(task);
    }


    public static String convertInputStreamToString(InputStream inputStream) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = br.readLine()) != null) {

                sb.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }



}
