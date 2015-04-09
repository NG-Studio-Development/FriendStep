package com.ngstudio.friendstep.utils;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;

import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.model.Callback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ReverseGeoLocation {

    private static final String TAG = "ReverseGeoLocation";


    public static void getAddressesFromLocationBackground(final double latitude, final double longitude, final Callback<Collection<Address>, Exception> callback) {
        new AsyncTask<Void, Void, Collection<Address>>() {

            @Override
            protected Collection<Address> doInBackground(Void... voids) {
                if(!Geocoder.isPresent())
                    return getAddressesFromLocationGoogle(latitude,longitude);
                else
                    return getAddressesFromLocationGeocoder(latitude,longitude);
            }

            @Override
            protected void onPostExecute(Collection<Address> addresses) {
                if (callback != null) {
                    callback.onComplete(addresses);
                }
            }
        }.execute();
    }


    public static Collection<Address> getAddressesFromLocationGoogle(double latitude, double longitude)  {

        String address = String
                .format(Locale.ENGLISH,"http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=false&language=en", latitude, longitude);
        HttpGet httpGet = new HttpGet(address);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        List<Address> retList = null;
        JSONObject jsonObject;
        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();

            //Log.i("test!","address = " + stringBuilder.toString());
            jsonObject = new JSONObject(HttpServer.convertInputStreamToString(stream));

            retList = new ArrayList<>();

            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONArray results = jsonObject.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    String indiStr = result.getString("formatted_address");
                    Address addr = new Address(Locale.getDefault());
                    addr.setAddressLine(0, indiStr);
                    retList.add(addr);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return retList;
    }

    public static Collection<Address> getAddressesFromLocationGeocoder(double latitude, double longitude) {
        if(Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(WhereAreYouApplication.getInstance().getApplicationContext(), Locale.ENGLISH);
            try {
                return geocoder.getFromLocation(latitude,longitude,Integer.MAX_VALUE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void getLocationFromAddress(final String address, final GetGoogleAddressFromAddressCallback callback)  {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                return getLocationFromAddress(address);
            }

            @Override
            protected void onPostExecute(String gaddress) {
                if (callback != null) {
                    callback.locationGot(gaddress);
                }
            }
        }.execute();


    }

    public static String getLocationFromAddress(String address) {
        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?address=" + Uri.encode(address) + "&sensor=false");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }


    public interface GetGoogleAddressFromAddressCallback {
        void locationGot(String gaddress);
    }

    
}
