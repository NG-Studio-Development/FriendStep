package com.ngstudio.friendstep.utils;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TextUtils {


	/**
	 * Email address validation expression.
	 *
	 * Useful capturing group indices:
	 * 1 - local part  (usually up to 64 symbols)
	 * 3 - domain part (usually up to 256 symbols)
	 */
	public static final Pattern PATTERN_EMAIL_ADDRESS = Pattern.compile(
			"^(\\w+?(\\.[\\w\\-\\+]+)*)@([\\w&&[^_]]+?(\\.[.\\w&&[^_]]+)*(\\.[.\\w&&[^_]]{2,})|\\[\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}\\])$"
	);


	/**
	 * E.164 ITU Phone number validation expression.
	 *
	 * Useful capturing group indices:
	 * 1 - Country code (CC ITU-T) 	(usually 1 to 3 digits)
	 * 2 - National number			(usually 7 to 14 digits)
	 */
	public static final Pattern PATTERN_E164_PHONE_NUMBER = Pattern.compile(
			"^\\+(9[976]\\d|8[987530]\\d|6[987]\\d|5[90]\\d|42\\d|3[875]\\d|2[98654321]\\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|4[987654310]|3[9643210]|2[70]|7|1)([1-9]\\d{6,13})$"
	);


	public static int safeLength(@Nullable String string) {
		return string != null ? string.length() : 0;
	}


	public enum Capitalize {
		WHOLE_TEXT(0),
		EVERY_SENTENCE(1),
		FIRST_LETTERS(2);


		private final int value;

		Capitalize(int value) {
			this.value = value;
		}

		@NotNull
		public static Capitalize fromValue(int value) {
			for (Capitalize c : Capitalize.values()) {
				if (c.value == value)
					return c;
			}
			throw new Error(String.format("%s  does not contains a corresponding value.",
					Capitalize.class.getSimpleName()));
		}
	}


	private static final Pattern CAPITALIZER_EVERY_SENTENCE = Pattern.compile("([^.!?\\s])[^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE);
	private static final Pattern CAPITALIZER_FIRST_LETTERS  = Pattern.compile("(^\\s*|\\s+)(\\w)\\w{2,}");


	private static String textCapitalizeImpl(Locale locale, String text, Pattern pattern, int group) {
		final Matcher m = pattern.matcher(text);
		final StringBuilder sb = new StringBuilder();
		int last = 0;
		while (m.find()) {
			sb.append(text.substring(last, m.start(group)));
			sb.append(m.group(group).toUpperCase(locale));
			last = m.end(group);
		}
		sb.append(text.substring(last));
		return sb.toString();
	}


	@Nullable
	public static String textCapitalize(Locale locale, String text, @MagicConstant(
			valuesFromClass = Capitalize.class) Capitalize capitalizerType) {
		if (text == null)
			return null;

		if (locale == null) {
			locale = Locale.getDefault();
		}

		switch (capitalizerType) {
			case WHOLE_TEXT:
				return text.toUpperCase(locale);

			case EVERY_SENTENCE: {
				return textCapitalizeImpl(locale, text, CAPITALIZER_EVERY_SENTENCE, 1);
			}

			case FIRST_LETTERS: {
				return textCapitalizeImpl(locale, text, CAPITALIZER_FIRST_LETTERS, 2);
			}

			default:
				throw new Error("Bad capitalizer type has been specified!");
		}
	}

	@Nullable
	public static String textCapitalize(String text, @MagicConstant(
			valuesFromClass = Capitalize.class) Capitalize capitalizerType) {
		return textCapitalize(null, text, capitalizerType);
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


	public static int compare(String lhs, String rhs, boolean lexicalOrder) {
		if (lhs == null)
			lhs = "";

		if (rhs == null)
			rhs = "";

		return lexicalOrder ? lhs.compareTo(rhs) : rhs.compareTo(lhs);
	}

	public static int compare(String lhs, String rhs) {
		return compare(lhs, rhs, true);
	}


	public static class StringComparator implements Comparator<String> {

		private static final Comparator<String> LEXICOGRAPHICAL_ORDER = new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				if (lhs == null)
					lhs = "";

				if (rhs == null)
					rhs = "";

				return lhs.compareTo(rhs);
			}
		};

		private static final Comparator<String> ANTI_LEXICOGRAPHICAL_ORDER = new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				if (lhs == null)
					lhs = "";

				if (rhs == null)
					rhs = "";

				return rhs.compareTo(lhs);
			}
		};


		public static final int ANTI_LEXICOGRAPHICAL = -1;
		public static final int LEXICOGRAPHICAL      = 1;


		private final Comparator<String> comparator;

		public StringComparator(@MagicConstant(valuesFromClass = StringComparator.class) int order) {

			switch (order) {
				case ANTI_LEXICOGRAPHICAL:
					comparator = ANTI_LEXICOGRAPHICAL_ORDER;
					break;

				case LEXICOGRAPHICAL:
					comparator = LEXICOGRAPHICAL_ORDER;
					break;

				default:
					throw new Error("Wrong order constant!");
			}

		}


		@Override
		public int compare(String lhs, String rhs) {
			return comparator.compare(lhs, rhs);
		}

	}

}
