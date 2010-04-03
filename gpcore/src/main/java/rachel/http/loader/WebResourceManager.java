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

package rachel.http.loader;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import rachel.http.Http;
import rachel.http.resource.ErrorResource;
import rachel.http.resource.WebResource;

public class WebResourceManager implements WebResourceLoader {
	static Logger LOGGER = Logger.getLogger(WebResourceManager.class);

	private static WebResourceManager _instance;

	private static ArrayList<WebResourceLoader> _resourceLoaders = new ArrayList<WebResourceLoader>();

	/**
	 * WebResourceManager is a singleton, don't allow creation of any instances
	 * outside this class.
	 */
	private WebResourceManager() {
	}

	public static WebResourceManager getInstance() {
		if (_instance == null)
			_instance = new WebResourceManager();

		return _instance;
	}

	public WebResource getResource(String name) {
		for (int i = 0; i < _resourceLoaders.size(); i++) {
			WebResourceLoader resourceLoader = _resourceLoaders.get(i);
			WebResource res = resourceLoader.getResource(name);
			if (res != null)
				// bingo!
				return res;
		}

//		LOGGER.warn("*** resource " + name + " not found");

		// resource not found
		return new ErrorResource(Http.Error.NOT_FOUND_404);
	}

	public static void addResourceLoader(WebResourceLoader loader) {
		getInstance().addResourceLoaderImpl(loader);
	}

	private void addResourceLoaderImpl(WebResourceLoader loader) {
		_resourceLoaders.add(loader);
	}

}
