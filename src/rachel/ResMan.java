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

package rachel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.jdom.Document;
import org.jdom.JDOMException;

public class ResMan {
	public static ResourceLoader setResourceLoader(ResourceLoader loader) {
		return ResourceManager.getResourceManager().setResourceLoader(loader);
	}

	public static ImageIcon getIcon(String name) {
		return ResourceManager.getResourceManager().getResourceAsIcon(name);
	}

	public static BufferedImage getImage(String name) throws IOException {
		return ResourceManager.getResourceManager().getResourceAsImage(name);
	}

	public static InputStream getInputStream(String name) {
		return ResourceManager.getResourceManager().getResourceAsStream(name);
	}

	public static Properties getProperties(String name) throws IOException {
		return ResourceManager.getResourceManager().getResourceAsProperties(
				name);
	}

	public static String getText(String name) throws IOException {
		return ResourceManager.getResourceManager().getResourceAsString(name);
	}

	public static URL getUrl(String name) {
		return ResourceManager.getResourceManager().getResourceAsUrl(name);
	}

	public static Document getXmlDocument(String name) throws JDOMException {
		return ResourceManager.getResourceManager().getResourceAsXmlDocument(
				name);
	}
}
