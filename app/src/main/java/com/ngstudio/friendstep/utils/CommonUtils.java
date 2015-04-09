package com.ngstudio.friendstep.utils;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

public class CommonUtils {

    public static <T> boolean areEqual(T a, T b) {
        if (a == null)
            return b == null;
        return a.equals(b);
    }

    private static Location getLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(curLocation == null) {
            curLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return curLocation;
    }


    public static String getCountryName(Context context) {
        Location curLocal = getLocation(context);
        if(curLocal == null)
            return null;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(curLocal.getLatitude(), curLocal.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && !addresses.isEmpty()) {
            return addresses.get(0).getCountryCode();
        }
        return null;
    }

    public static boolean isConnected(@NotNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    public static void alarmManagerSetExact(Context context, int type, long when, PendingIntent operation) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try {
            String alarmSetMethod = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ? "set" : "setExact";
            AlarmManager.class.getMethod(alarmSetMethod, int.class, long.class, PendingIntent.class).invoke(alarm, type, when, operation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static String getText(@NotNull EditText et) {
        final Editable editable = et.getText();
        if (editable == null)
            return "";
        return editable.toString();
    }

    public static String getFullNumber(String number, String countryCode) throws IllegalArgumentException{
        if(TextUtils.isEmpty(number))
            throw new IllegalArgumentException("Wrong phone number.");

        String formattedNumber = "+" + number.replaceAll("\\D+","");

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber numberPhone;
        try {
            numberPhone = phoneNumberUtil.parse(formattedNumber,null);

        } catch (NumberParseException e) {
            try {
                formattedNumber = "+" + countryCode + number.replaceAll("\\D+","");
                numberPhone = phoneNumberUtil.parse(formattedNumber,null);

            } catch (NumberParseException e1) {
                e.printStackTrace();
                throw new IllegalArgumentException("Wrong phone number.");
            }
        }

        return String.valueOf(numberPhone.getCountryCode()) + numberPhone.getNationalNumber();
    }

    private static final String ENCODING = "UTF-8";

    public static String calcHash(@NotNull ByteRepresentable value, @NotNull String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);

        try {
            return toHexString(md.digest(value.getBytes()));
        } finally {
            md.reset();
        }
    }

    public static String calcHash(final @NotNull String value, @NotNull String algorithm) throws NoSuchAlgorithmException {
        return calcHash(new ByteRepresentable() {
            @NotNull
            @Override
            public byte[] getBytes() {
                try {
                    return value.getBytes(ENCODING);
                } catch (UnsupportedEncodingException e) {
                    throw new Error(e);
                }
            }
        }, algorithm);
    }

    private static final char[] HEXADECIMAL_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Nullable
    public static String toHexString(@Nullable byte[] bytes) {
        if (bytes == null)
            return null;

        if (bytes.length == 0)
            return "";

        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; ++i) {
            int b = bytes[i] & 0xff;

            int j = i << 1;
            hexChars[j + 1] = HEXADECIMAL_DIGITS[b & 0x0f];
            hexChars[j] = HEXADECIMAL_DIGITS[b >>> 4];
        }
        return new String(hexChars);
    }
}
