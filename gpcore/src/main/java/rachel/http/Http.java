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

package rachel.http;

public class Http {
	public final static String SERVER_ID = "Rachel Ultra Light-Weight HTTP Server";

	public static class Error {

		public final static Error NOT_FOUND_404 = new Error(404,
				"Resource Not Found");
		public final static Error NOT_IMPLEMENTED_501 = new Error(501,
				"Method Not Implemented");
		private int _code;
		private String _message;

		public Error(int code, String message) {
			_code = code;
			_message = message;
		}

		public int getCode() {
			return _code;
		}

		public String getMessage() {
			return _message;
		}
	}

	public static class Header {
		public final static String CONTENT_LENGTH = "Content-Length";
		public final static String CONTENT_TYPE = "Content-Type";
		public final static String DATE = "Date";
		public final static String SERVER = "Server";
	}

	public static class Method {
		public final static String GET = "GET";
	}
}
