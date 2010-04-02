/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.lightdev.app.shtm;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Finds and loads plug-ins of application SimplyHTML.
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the GNU General Public
 *         License, for details see file gpl.txt in the distribution package of
 *         this software
 * 
 * 
 */

public class PluginManager {

	/** name of sub-package where plug-ins are stored */
	private String PLUGIN_PACKAGE = "installed";

	/** the class loader pointing to all plug-in locations (JARs) */
	private URLClassLoader loader;

	/** the class names of all loaded plug-ins */
	private Vector pluginClassNames = new Vector();

	/** the plug-in objects loaded by this <code>PluginManager</code> */
	private Hashtable loadedPlugins = new Hashtable();

	private Hashtable nameMap = new Hashtable();

	/** the URLs pointing to the classes in pluginClassNames */
	private Vector urls = new Vector();

	private SHTMLPanelImpl owner;

	/**
	 * construct a new <code>PluginManager</code> and load all available
	 * plug-ins.
	 */
	public PluginManager(SHTMLPanelImpl owner) {
		this.owner = owner;
	}

	/**
	 * get all plug-ins loaded by this <code>PluginManager</code>
	 */
	public Enumeration plugins() {
		return loadedPlugins.elements();
	}

	public ClassLoader getPluginLoader() {
		return loader;
	}

	/**
	 * get a loaded plug-in by its GUI name.
	 * 
	 * @param guiName
	 *            the GUI name of this plaug-in
	 * 
	 * @return the plug-in having the given GUI name, or null of no plug-in with
	 *         that name is present
	 */
	public SHTMLPlugin pluginForName(String guiName) {
		String intName = (String) nameMap.get(guiName);
		return (SHTMLPlugin) loadedPlugins.get(intName);
	}

	public Object[] getPluginNames() {
		return nameMap.keySet().toArray();
	}

	/**
	 * load all plug-ins found in the plug-in path
	 */
	public void loadPlugins() {
		loadedPlugins.clear();
		String pluginPrefix = this.getClass().getPackage().getName()
				+ Util.CLASS_SEPARATOR + PLUGIN_PACKAGE + Util.CLASS_SEPARATOR;
		findPlugins(pluginPrefix.replace(Util.CLASS_SEPARATOR_CHAR,
				Util.URL_SEPARATOR_CHAR));
		Enumeration cNames = pluginClassNames.elements();
		Class cl;
		Object o;
		SHTMLPlugin p;
		String intName;
		while (cNames.hasMoreElements()) {
			try {
				String nextClass = (String) cNames.nextElement();
				// System.out.println("PluginManager loadPlugins loading " +
				// pluginPrefix + nextClass /* pluginPrefix + (String)
				// cNames.nextElement()*/);
				cl = loader.loadClass(/* pluginPrefix + */
				/* (String) cNames.nextElement() */pluginPrefix + nextClass);
				// System.out.println("PluginManager loadPlugins calling newInstance ");
				o = cl.newInstance();
				if (o instanceof SHTMLPlugin) {
					p = (SHTMLPlugin) o;
					p.initPlugin(owner, null, null, null);
					// p.setOwner(owner);
					// p.initPluginActions();
					intName = p.getInternalName();
					loadedPlugins.put(intName, o);
					nameMap.put(p.getGUIName(), intName);
				}
			} catch (Exception e) {
				Util.errMsg(null, this.getClass().getName() + ".loadPlugins: "
						+ e.getMessage(), e);
			}
		}
	}

	/**
	 * get a class loader for a given set of URLs specifying one or more class
	 * paths
	 * 
	 * @param urls
	 *            set of URLs specifying the class path(s)
	 * 
	 * @return the class loader
	 */
	private URLClassLoader createLoader(Vector urls) {
		URL[] urlArray = new URL[urls.size()];
		for (int i = 0; i < urls.size(); i++) {
			urlArray[i] = (URL) urls.elementAt(i);
			// System.out.println("urlArray[" + i + "]=" + urlArray[i]);
		}
		return new URLClassLoader(urlArray, this.getClass().getClassLoader());
	}

	/**
	 * find plug-ins by looking for JARs inside a given path and create a class
	 * loader for them.
	 * 
	 * <p>
	 * JARs are searched in sub-package .plugin.installed of the package this
	 * class resides in.
	 * </p>
	 * 
	 * <p>
	 * On return of this method fields <code>loader</code> and
	 * <code>pluginClassNames</code> are initialized and filled accordingly.
	 * </p>
	 * 
	 * @param pluginPath
	 *            the path to look for plug-in JAR files, e.g.
	 *            com/lightdev/app/shtm/plugin/installed/
	 */
	private void findPlugins(String pluginPath) {
		String appPath = Util.getClassFilePath(this.getClass());
		String filePath;
		if (appPath.indexOf(":") < 0) {
			filePath = "/" + appPath;
		} else {
			filePath = appPath;
		}
		// System.out.println("PluginManager.findPlugins appPath=" + appPath +
		// ", filePath=" + filePath);
		pluginClassNames.clear();
		urls.clear();
		String fName;
		try {
			File plugindir = new File(filePath);// new URI(Util.FILE_PREFIX +
			// Util.URL_SEPARATOR +
			// appPath));
			if (plugindir != null) {
				File[] content = plugindir.listFiles();
				if (content != null) {
					for (int i = 0; i < content.length; i++) {
						if (content[i].isFile()) {
							fName = content[i].getName();
							// System.out.println("PluginManager.findPlugins fName="
							// + fName);
							if (fName.toLowerCase().endsWith("jhall.jar")) {
								/*
								 * System.out.println("PluginManager.findPlugins adding URL "
								 * + Util.FILE_PREFIX + Util.URL_SEPARATOR +
								 * appPath + fName);
								 */
								urls
										.addElement(new URL(Util.FILE_PREFIX
												+ Util.URL_SEPARATOR + appPath
												+ fName));
							}
							if (fName.toLowerCase().endsWith("simplyhtml.jar")) {
								/*
								 * System.out.println("PluginManager.findPlugins adding URL "
								 * + Util.FILE_PREFIX + Util.URL_SEPARATOR +
								 * appPath + fName);
								 */
								urls
										.addElement(new URL(Util.FILE_PREFIX
												+ Util.URL_SEPARATOR + appPath
												+ fName));
							} else if (fName.endsWith(Util.JAR_EXTENSION)) {
								readJar(appPath, pluginPath, content[i], fName);
							}
						}
					}
				}
			}
			loader = createLoader(urls);
		} catch (Exception e) {
			Util.errMsg(null, this.getClass().getName() + ".findPlugins: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * read a Java archive (JAR) file and look for classes within a given
	 * package path inside the JAR file. Store class names and their URLS for
	 * later use.
	 * 
	 * <p>
	 * Some of the parameters required below could be extracted from others
	 * passed to this method but as previous methods already determined
	 * respective paramaters, it is faster to pass the existing values as
	 * parameters than to re-build the values locally.
	 * </p>
	 * 
	 * @param filePath
	 *            the absolute path pointing to the JAR file, e.g.
	 *            file:/C:/Programs/SimplyHTML/
	 * @param pluginPath
	 *            the path inside filePath pointing to potential plug-ins
	 * @param jarFile
	 *            the file object referring to the JAR file to read
	 * @param fileName
	 *            the name of the JAR file
	 */
	private void readJar(String filePath, String pluginPath, File jarFile,
			String fileName) {
		try {
			Enumeration jarEntries = new JarFile(jarFile).entries();
			JarEntry je;
			String jeName;
			while (jarEntries.hasMoreElements()) {
				je = (JarEntry) jarEntries.nextElement();
				jeName = je.getName();
				if (jeName.startsWith(pluginPath) && !je.isDirectory()
						&& jeName.endsWith(Util.CLASS_EXT)) {
					/*
					 * System.out.println("PluginManager.readJar adding URL " +
					 * Util.FILE_PREFIX + Util.URL_SEPARATOR + filePath +
					 * fileName);
					 */
					urls.addElement(new URL(Util.FILE_PREFIX
							+ Util.URL_SEPARATOR + filePath + fileName));
					pluginClassNames.addElement(jeName.substring(pluginPath
							.length(), jeName.indexOf(Util.CLASS_SEPARATOR)));
				}
			}
		} catch (Exception e) {
			/*
			 * Util.errMsg(null, this.getClass().getName() + ".readJar: " +
			 * e.getMessage(), e);
			 */
		}
	}
}
