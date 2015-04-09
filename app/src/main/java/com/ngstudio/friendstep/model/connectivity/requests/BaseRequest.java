package com.ngstudio.friendstep.model.connectivity.requests;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseRequest implements Serializable {

    private static final String TAG = BaseRequest.class.getSimpleName();

    protected String url;
    private RequestType requestType;
    protected Map<String,String> requestProperties;

    private String method;

    public void setMethod(String method) {
        this.method = method;
    }


    public BaseRequest(@NotNull String url, RequestType type) {
        this.url = url;
        this.requestType = type;
    }

    public BaseRequest(@NotNull String url, @Nullable String method, RequestType type) {
        this.url = url;
        this.requestType = type;
        this.method = method;
    }

    public BaseRequest setUrl(String mUrl) {
        this.url = mUrl;
        return this;
    }

    public String toGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .disableHtmlEscaping().create().toJson(this);
    }

    private String dataType;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    private String acceptType;

    public String getAcceptType() {
        return acceptType;
    }

    public void setAcceptType(String acceptType) {
        this.acceptType = acceptType;
    }

    public void setRequestProperties(@NotNull HttpURLConnection httpURLConnection) throws ProtocolException {
        initProperties();
        if(requestProperties != null) {
            for (String key : requestProperties.keySet()) {
                httpURLConnection.setRequestProperty(key,requestProperties.get(key));
            }
        }
        httpURLConnection.setRequestMethod(getRequestType().name());
    }

    protected void initProperties() {
        requestProperties = new HashMap<>();
        if(!TextUtils.isEmpty(dataType))
            requestProperties.put("Content-Type", dataType);

        if(!TextUtils.isEmpty(acceptType))
            requestProperties.put("Accept", acceptType);
    }

    public void writeData(@NotNull OutputStream httpURLConnectionStream) throws IOException {
        if(TextUtils.isEmpty(dataType))
            return;

        if("application/json".equals(dataType))
            writeSelfAsJson(httpURLConnectionStream);
        else if("application/x-www-form-urlencoded".equals(dataType))
            writeSelfAsQuery(httpURLConnectionStream);
    }

    private void writeSelfAsJson(@NotNull OutputStream httpURLConnectionStream) throws IOException {
        Log.d(TAG,"send data = " + this.toGson());
        httpURLConnectionStream.write(URLEncoder.encode(this.toGson(), "UTF-8").getBytes());
    }

    private void writeSelfAsQuery(@NotNull OutputStream httpURLConnectionStream) throws IOException {
        Log.d(TAG,"send data = " + toQueryParams(requestType));
        httpURLConnectionStream.write(toQueryParams(requestType).getBytes());
    }


    protected String toQueryParams(RequestType type) {
        StringBuilder queryBuilder = new StringBuilder();
        List<Field> fields = getAnnotatedFields(type);
        boolean isFirst = true;

        for(Field field : fields) {
            if(!field.isAccessible())
                field.setAccessible(true);
            try {
                Object value = field.get(this);
                String valueString;
                if(value != null && !TextUtils.isEmpty(valueString = String.valueOf(value))) {
                    if(isFirst) {
                        isFirst = false;
                    } else {
                        queryBuilder.append("&");
                    }
                    queryBuilder.append(URLEncoder.encode(field.getName(), "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(valueString, "UTF-8"));
                }
            } catch (IllegalAccessException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return queryBuilder.toString();
    }

    private List<Field> getAnnotatedFields(RequestType type) {
        List<Field> allFields = new ArrayList<>();
        Class current = ((Object)this).getClass();
        while (!current.equals(Object.class)) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                if(field.isAnnotationPresent(RequestField.class) && type.equals(field.getAnnotation(RequestField.class).type())) {
                    allFields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return allFields;
    }


    public String getUrl() {
        return url + (method == null ? "" : method) + "?" + toQueryParams(RequestType.GET);
    }

    /**
     *
     * @return type of request
     */
    public RequestType getRequestType() {
        return requestType;
    }

}
