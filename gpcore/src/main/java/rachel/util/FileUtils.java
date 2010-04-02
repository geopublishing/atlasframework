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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
	public static String getInputStreamAsString(InputStream inStream)
			throws IOException {
		// todo: should i wrap inputStream in BufferedInputStream?

		StringBuffer text = new StringBuffer();

		InputStreamReader in = new InputStreamReader(inStream);
		char buffer[] = new char[4096];
		int bytes_read;
		while ((bytes_read = in.read(buffer)) != -1)
			text.append(new String(buffer, 0, bytes_read));

		return text.toString();
	}

	public static void saveStreamToFile(InputStream in, File outFile)
			throws IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			byte[] buf = new byte[4096];
			int bytes_read;
			while ((bytes_read = in.read(buf)) != -1)
				out.write(buf, 0, bytes_read);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}

			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}
}
