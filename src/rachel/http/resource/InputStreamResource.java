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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import rachel.http.Http;
import rachel.util.DateUtils;
import rachel.util.MimeUtils;

public class InputStreamResource implements WebResource {
	static Logger Status = Logger.getLogger(InputStreamResource.class);
	private String _contentType;

	private byte _data[];

	public InputStreamResource(String name, InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte buffer[] = new byte[2048];
		int bytes_read = 0;
		while ((bytes_read = in.read(buffer)) > 0)
			out.write(buffer, 0, bytes_read);

		out.flush();
		out.close();
		in.close();

		_data = out.toByteArray();

		// Status.debug( "data.length=" + _data.length );

		_contentType = MimeUtils.guessContentTypeFromName(name);
	}

	public String getContentType() {
		return _contentType;
	}

	public byte[] getData() {
		return _data;
	}

	public String getHeader() {
		StringBuffer header = new StringBuffer();

		header.append("HTTP/1.0 200 OK\r\n");
		header.append(Http.Header.DATE + ": " + DateUtils.getHttpDate()
				+ "\r\n");
		header.append(Http.Header.SERVER + ": " + Http.SERVER_ID + "\r\n");
		header.append(Http.Header.CONTENT_LENGTH + ": " + getData().length
				+ "\r\n");
		header.append(Http.Header.CONTENT_TYPE + ": " + getContentType()
				+ "\r\n");
		header.append("\r\n");

		// Status.debug( "Content-Length: " + getData().length );
		// Status.debug( "Content-Type: " + getContentType() );

		return header.toString();
	}
}
