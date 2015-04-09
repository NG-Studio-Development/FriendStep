//Copyright (C) 2011 by Tapjoy Inc.
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

package com.ngstudio.friendstep.utils;

import android.util.Log;

/**
 * 
 * This class is used to control debug messages to the console for Persona SDK related classes.
 *
 */
public class WhereAreYouAppLog
{
    private static String TAG = "WhereAreYouApp";

	private static boolean showLog = false;
	
	/**
	 * Enables or disables logging.
	 * @param enable		Set to true if logging should be enabled, false to disable logging.
	 */
	public static void enableLogging(boolean enable)
	{
		Log.i(TAG, "enableLogging: " + enable);
		showLog = enable;
	}
	
	/**
	 * Set an info log message.
	 * @param msg			Log message to output to the console.
	 */
	public static void i(String msg)
	{
		if (showLog)
			Log.i(TAG, msg);
	}

	/**
	 * Set an error log message.
	 * @param msg			Log message to output to the console.
	 */
	public static void e(String msg)
	{
		if (showLog)
			Log.e(TAG , msg);
	}

	/**
	 * Set a warning log message.
	 * @param msg			Log message to output to the console.
	 */
	public static void w(String msg)
	{
		if (showLog)
			Log.w(TAG, msg);
	}

	/**
	 * Set a debug log message.
	 * @param msg			Log message to output to the console.
	 */
	public static void d(String msg)
	{
		if (showLog)
			Log.d(TAG, msg);
	}

	/**
	 * Set a verbose log message.
	 * @param msg			Log message to output to the console.
	 */
	public static void v(String msg)
	{
		if (showLog)
			Log.v(TAG, msg);
	}
}
