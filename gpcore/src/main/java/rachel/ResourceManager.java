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

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import rachel.util.FileUtils;

public class ResourceManager {
	static Logger LOGGER = Logger.getLogger(ResourceManager.class);

	private static ResourceManager _manager;

	private ResourceLoader _loader;

	private ResourceManager() {
	}

	public ResourceLoader setResourceLoader(ResourceLoader newLoader) {
		ResourceLoader oldLoader = _loader;
		_loader = newLoader;
		return oldLoader;
	}

	public static ResourceManager getResourceManager() {
		if (_manager == null)
			_manager = new ResourceManager();

		return _manager;
	}

	public ImageIcon getResourceAsIcon(String name) {
		URL url = _loader.getResourceAsUrl(name);
		if (url == null) {
			LOGGER.error("*** failed to find icon resource '" + name + "'");
			return null;
		}

		return new ImageIcon(url);
	}

	public BufferedImage getResourceAsImage(String name) throws IOException {
		// fix: add String _loader.getSearchPath()
		// for better diagnostics and debug messages

		// todo: should i use InputStream instead of an URL?

		URL url = _loader.getResourceAsUrl(name);
		if (url == null) {
			LOGGER.error("*** failed to find image resource '" + name + "'");
			return null;
		}

		return ImageIO.read(url);
	}

	public Properties getResourceAsProperties(String name) throws IOException {
		InputStream in = _loader.getResourceAsStream(name);
		if (in == null) {
			LOGGER.error("*** failed to find properties resource '" + name
					+ "'");
			return null;
		}

		Properties props = new Properties();
		props.load(in);

		return props;
	}

	public InputStream getResourceAsStream(String name) {
		InputStream in = _loader.getResourceAsStream(name);
		if (in == null)
			LOGGER.error("*** failed to find resource '" + name + "'");

		return in;
	}

	public String getResourceAsString(String name) throws IOException {
		InputStream in = _loader.getResourceAsStream(name);
		if (in == null) {
			LOGGER.error("*** failed to find text resource '" + name + "'");
			return null;
		}

		String text = FileUtils.getInputStreamAsString(in);
		return text;
	}

	public URL getResourceAsUrl(String name) {
		URL url = _loader.getResourceAsUrl(name);
		if (url == null) {
			LOGGER.error("*** failed to find resource '" + name + "'");
			return null;
		}
		return url;
	}

	public Document getResourceAsXmlDocument(String name) throws JDOMException {
		InputStream in = _loader.getResourceAsStream(name);
		if (in == null) {
			LOGGER.error("*** failed to find xml resource '" + name + "'");
			return null;
		}

		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(in);
		} catch (IOException e) {
			LOGGER.error("*** failed to find xml resource '" + name + "'", e);
			return null;
		}
		return doc;
	}
}
