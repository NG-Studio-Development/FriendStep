package com.ngstudio.friendstep.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;


import com.ngstudio.friendstep.R;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidationUtils {
	
	public static boolean checkEmail(String email) {
		final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		if (email.length() < 1 && email.length() >  256) {
			return false;
		}
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public static boolean checkEmailWithToast(Context context, String email) {
		boolean res = checkEmail(email);
		
		if(!res) {
			Toast.makeText(context, context.getString(R.string.toast_wrong_email), Toast.LENGTH_SHORT).show();
		}
		
		return res;
	}

	
	public static boolean checkPassword(String password) {
		
		if (TextUtils.isEmpty(password) || password.length() < 1 || password.length() >  30) {
			return false;
		}
		
		return true;
	}
	
	public static boolean checkPasswordWithToast(Context context, String password) {
		boolean res = checkPassword(password);
		
		if(!res) {
			Toast.makeText(context, context.getString(R.string.toast_wrong_password), Toast.LENGTH_SHORT).show();
		}
		

		return res;
	}


	
	public static boolean checkNonEmptyField(String username) {

        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(username.trim());

    }
	
	
	public static boolean checkNonEmptyFieldWithToast(Context context, String checkable, String fieldName) {
		boolean res = checkNonEmptyField(checkable);


		if(!res) {
			Toast.makeText(context, context.getString(R.string.toast_wrong_field,fieldName), Toast.LENGTH_SHORT).show();
		}

		return res;
	}

    public static boolean checkPasswordsWithToast(Context context, String password, String secondPassword) {
        boolean res = password.equals(secondPassword);

        if(!res) {
            Toast.makeText(context, context.getString(R.string.toast_passwords_mismatch), Toast.LENGTH_SHORT).show();
        }

        return res;
    }

    public static boolean checkPhoneNumber(String phone) {
        final String PHONE_PATTERN = "^[+]?[0-9\\s]{10,13}$";

        if (phone.length() < 10 && phone.length() >  13) {
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);

        return matcher.matches();
    }

    public static boolean checkPhoneNumber(Phonenumber.PhoneNumber phone) {
        return PhoneNumberUtil.getInstance().isValidNumber(phone);
    }

    public static boolean checkPhoneNumberWithToast(Context context, String phone) {
        boolean res = checkPhoneNumber(phone);

        if(!res) {
            Toast.makeText(context, context.getString(R.string.toast_wrong_phone), Toast.LENGTH_SHORT).show();
        }

        return res;
    }

    public static boolean checkPhoneNumber(String phone, boolean replaceNonDigits){
        final String phoneNumberText = replaceNonDigits ? phone.replace("\\D+","") : phone;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber;
        try {
            phoneNumber = phoneNumberUtil.parse("+" + phoneNumberText,null);
            if(!phoneNumberUtil.isPossibleNumber(phoneNumber))
                throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER,"impossible number");
        } catch (NumberParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean checkPhoneNumberWithToast(Context context, String phone, boolean replaceNonDigits) {
        boolean res = checkPhoneNumber(phone, replaceNonDigits);

        if(!res) {
            Toast.makeText(context, context.getString(R.string.toast_wrong_phone), Toast.LENGTH_SHORT).show();
        }

        return res;
    }

    public static boolean checkPhoneNumberWithToast(Context context, Phonenumber.PhoneNumber phone) {
        boolean res = checkPhoneNumber(phone);

        if(!res) {
            Toast.makeText(context, context.getString(R.string.toast_wrong_phone), Toast.LENGTH_SHORT).show();
        }

        return res;
    }
}
