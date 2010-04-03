/*
 ** Rachel  - Resource Loading Toolkit for Web Start/JNLP
 ** Copyright (c) 2001, 2002 by Gerald Bauer
 **
 ** This program is free software.
 **
 ** You may redistribute it and/or modify it under the terms of the GNU
 ** General Public License as published by the Free Software Foundation.
 ** Version 2 of the license should be included with this distribution in
 ** the file LICENSE, as well as License.html. If the license is not
 ** included with this distribution, you may find a copy at the FSF web
 ** site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
 ** Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
 **
 ** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
 ** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
 ** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
 ** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
 ** REDISTRIBUTION OF THIS SOFTWARE.
 **
 */

package rachel.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
	/**
	 * RFC 1123 date format example: Mon, 06 May 1996 04:57:00 GMT - days: Mon,
	 * Tue, Wed, Thu, Fri, Sat, Sun - months: Jan, Feb, Mar, Apr, May, Jun, Jul,
	 * Aug, Sep, Oct, Nov, Dec
	 */
	private static SimpleDateFormat _df;

	/**
	 * convienence method returns current timestamp
	 */
	public static String getHttpDate() {
		return getHttpDate(new Date());
	}

	public static String getHttpDate(long timestamp) {
		return getHttpDate(new Date(timestamp));
	}

	public static String getHttpDate(Date date) {
		return _df.format(date);
	}

	static {
		_df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
		_df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
}
