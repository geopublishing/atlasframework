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

package rachel.http.resource;

import rachel.http.Http;
import rachel.util.DateUtils;

public class ErrorResource implements WebResource {
	private byte _data[];
	private Http.Error _error;

	public ErrorResource(Http.Error error) {
		_error = error;

		StringBuffer html = new StringBuffer();

		html.append("<HTML>\r\n");
		html.append("<HEAD><TITLE>" + error.getMessage() + "</TITLE>\r\n");
		html.append("</HEAD>\r\n");
		html.append("<BODY>\r\n");
		html.append("<H1>HTTP Error " + error.getCode() + ": "
				+ error.getMessage() + "</H1>\r\n");
		html.append("</BODY></HTML>\r\n");

		_data = html.toString().getBytes();
	}

	public String getContentType() {
		return "text/html";
	}

	public byte[] getData() {
		return _data;
	}

	public String getHeader() {
		StringBuffer header = new StringBuffer();

		header.append("HTTP/1.0 " + _error.getCode() + " "
				+ _error.getMessage() + "\r\n");
		header.append(Http.Header.DATE + ": " + DateUtils.getHttpDate()
				+ "\r\n");
		header.append(Http.Header.SERVER + ": " + Http.SERVER_ID + "\r\n");
		header.append(Http.Header.CONTENT_LENGTH + ": " + getData().length
				+ "\r\n");
		header.append(Http.Header.CONTENT_TYPE + ": " + getContentType()
				+ "\r\n");
		header.append("\r\n");

		return header.toString();
	}

}
