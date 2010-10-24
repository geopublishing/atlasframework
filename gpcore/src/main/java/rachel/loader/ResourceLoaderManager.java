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

package rachel.loader;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import rachel.ResourceLoader;

public class ResourceLoaderManager implements ResourceLoader {
	HashSet<ResourceLoader> _loaders = new HashSet<ResourceLoader>();

	// ArrayList<ResourceLoader> _loaders = new ArrayList<ResourceLoader>();

	@Override
	public InputStream getResourceAsStream(String name) {
		Iterator<ResourceLoader> it = _loaders.iterator();
		while (it.hasNext()) {
			ResourceLoader loader = it.next();
			InputStream in = loader.getResourceAsStream(name);
			if (in != null)
				return in;
		}
		return null;
	}

	@Override
	public URL getResourceAsUrl(String name) {
//		if (name.startsWith("/"))
//			name = name.substring(1);
		Iterator<ResourceLoader> it = _loaders.iterator();
		while (it.hasNext()) {
			ResourceLoader loader = it.next();
			URL url = loader.getResourceAsUrl(name);
			if (url != null)
				return url;
		}
		return null;
	}

	public boolean removeResourceLoader(ResourceLoader loader) {
		return _loaders.remove(loader);
	}

	public void addResourceLoader(ResourceLoader loader) {
		_loaders.add(loader);
	}
}
