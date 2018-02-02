/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.tx.mcc.utils;

import com.genband.kandy.api.utils.IKandyLogger;
import com.genband.kandy.api.utils.KandyLog.Level;

/**
 * Sample logger implementation that prints logs to Andoroid logcat.
 *
 */
public class KandyLogger implements IKandyLogger {

	@Override
	public void log(int level, String tag, String message) {
		// print logs to Android logcat
		switch (level)
		{
		case Level.ERROR:
			android.util.Log.e(tag, message);
			break;
		case Level.INFO:
			android.util.Log.i(tag, message);
			break;
		case Level.VERBOSE:
			android.util.Log.v(tag, message);
			break;
		case Level.WARN:
			android.util.Log.w(tag, message);
			break;
		default:
			android.util.Log.d(tag, message);
			break;
		}
	}

	@Override
	public void log(int level, String tag, String message, Throwable throwable) {
		// print logs to Android logcat
		switch (level)
		{
		case Level.ERROR:
			android.util.Log.e(tag, message, throwable);
			break;
		case Level.INFO:
			android.util.Log.i(tag, message, throwable);
			break;
		case Level.VERBOSE:
			android.util.Log.v(tag, message, throwable);
			break;
		case Level.WARN:
			android.util.Log.w(tag, message, throwable);
			break;
		default:
			android.util.Log.d(tag, message, throwable);
			break;
		}
	}

}
