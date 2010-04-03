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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

/**
 * Base class for plug-ins of application SimplyHTML.
 * 
 * <p>
 * Defines some common methods for reuse in plug-ins. All settings such as
 * dockLocation or activation state of a plug-in are stored persistently in a
 * preferences file with the help of class <code>Prefs</code>. The preferences
 * file is valid for the current user, so each user has own plug-in settings.
 * </p>
 * 
 * <p>
 * Menus are constructed with the help of class <code>DynamicResource</code>.
 * This class needs menu definitions accessible in a .properties file as
 * described in the API docs of <code>DynamicResource</code>. I.e., methods of
 * class <code>AbstractPlugin</code> only work as defined herein when
 * accompanied by such .properties file accordingly.
 * </p>
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
 * 
 * @see com.lightdev.app.shtm.DynamicResource
 */

public abstract class AbstractPlugin implements SHTMLPlugin {

	/**
	 * construct an AbstractPlugin
	 * 
	 * <p>
	 * Constructor may not have parameters so that java.lang.Class.newInstance
	 * can be used on it.
	 * </p>
	 */
	public AbstractPlugin() {
		// System.out.println("AbstractPlugin constructor");
		/*
		 * SecurityManager security = System.getSecurityManager(); if (security
		 * != null) { security. }
		 */
		prefs = Preferences.userNodeForPackage(getClass());
	}

	/**
	 * init the plug-in
	 * 
	 * this is called by the PluginManager directly after instantiating the
	 * plug-in
	 */
	public void initPlugin(SHTMLPanelImpl owner) {
		this.owner = owner;
	}

	/**
	 * create the plug-in menu
	 */
	protected void createPluginMenu() {
		if (pluginMenuId != null) {
			pMenu = owner.dynRes.createMenu(AbstractPlugin.textResources,
					pluginMenuId);
		}
	}

	/**
	 * create the help menu
	 */
	protected void createHelpMenu() {
		if (helpMenuId != null) {
			hMenu = owner.dynRes.createMenu(AbstractPlugin.textResources,
					helpMenuId);
			initHelpMenu();
		}
	}

	public void initHelpMenu() {
	}

	/**
	 * create a frame for the component of this plug-in, if it has a JComponent
	 * to display.
	 */
	protected void createFrame() {
		if (c != null) {
			frame = new JFrame(getGUIName());
			frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			frame.setSize(300, 400);
			frame.getContentPane().add(c);
		}
		frame.pack();
	}

	/**
	 * create, show or hide frame as needed, depending on a given dock location
	 * 
	 * @param location
	 *            the dock location of the plug-in, one of DOCK_LOCATION_TOP,
	 *            DOCK_LOCATION_BOTTOM, DOCK_LOCATION.LEFT, DOCK_LOCATION_RIGHT
	 *            or DOCK_LOCATION_NONE, if the component shall not dock.
	 */
	protected void createFrameAsNeeded(int location) {
		if (location == SHTMLPlugin.DOCK_LOCATION_NONE) {
			if ((frame == null) && (c != null)) {
				createFrame();
			}
			if (frame != null) {
				frame.setVisible(true);
			}
		} else {
			if (frame != null) {
				frame.setVisible(true);
			}
		}
	}

	/* ----------- SimplyHTML plugin interface implementation start --------- */

	/**
	 * initialize the plugin
	 * 
	 * @param owner
	 *            the owner of this plug-in
	 * @param internalName
	 *            the internal name this plug-in shall have
	 * @param pluginMenuId
	 *            the id of the plug-in menu in the TextResources, or null if no
	 *            plugin-in menu is to be created
	 * @param helpMenuId
	 *            the id of the help menu for this plug-in in the TextResources,
	 *            or null if no help menu is to be created
	 */
	public void initPlugin(SHTMLPanel owner, String internalName,
			String pluginMenuId, String helpMenuId) {
		this.owner = (SHTMLPanelImpl) owner;
		this.internalName = internalName;
		this.pluginMenuId = pluginMenuId;
		this.helpMenuId = helpMenuId;
		try {
			// System.out.println("AbstractPlugin this.getClass.getName=" +
			// this.getClass().getName());
			if (SHTMLPanelImpl.pluginManager != null) {
				ClassLoader plLoader = SHTMLPanelImpl.pluginManager
						.getPluginLoader();
				if (plLoader != null) {
					final ResourceBundle resourceBundle = ResourceBundle
							.getBundle(this.getClass().getName(), Locale
									.getDefault(), plLoader);
					textResources = new DefaultTextResources(resourceBundle);
					// System.out.println("AbstractPlugin plLoader != null, resources="
					// + resources);
				} else {
					DefaultTextResources instance = new DefaultTextResources(
							ResourceBundle.getBundle(this.getClass().getName(),
									Locale.getDefault()));
					textResources = instance;
					// System.out.println("AbstractPlugin plLoader == null, resources="
					// + resources);
				}
			} else {
				DefaultTextResources instance = new DefaultTextResources(
						ResourceBundle.getBundle(this.getClass().getName(),
								Locale.getDefault()));
				textResources = instance;
				// System.out.println("AbstractPlugin pluginManager = null, resources="
				// + resources);
			}
			this.active = prefs.getBoolean(
					internalName + PREFSID_PLUGIN_ACTIVE, true);
			dockLocation = prefs.getInt(internalName
					+ PREFSID_PLUGIN_DOCK_LOCATION,
					SHTMLPlugin.DOCK_LOCATION_LEFT);
			createFrameAsNeeded(dockLocation);
		} catch (MissingResourceException mre) {
			Util.errMsg(null, this.getClass().getName()
					+ ".properties not found", mre);
		}

	}

	/**
	 * set the owner of this plug-in
	 * 
	 * @param owner
	 *            the main frame of the instance of SimplyHTML creating the
	 *            plug-in
	 */
	public void setOwner(SHTMLPanelImpl owner) {
		this.owner = owner;
	}

	/**
	 * get the owner of this plug-in
	 * 
	 * @return the owner of this plug-in
	 */
	public SHTMLPanelImpl getOwner() {
		return owner;
	}

	/**
	 * set status of plug-in and persistently store setting in a preferences
	 * file
	 * 
	 * @param isActive
	 *            indicates whether or not the plug-in shall be activated
	 */
	public void setStatus(boolean isActive) {
		this.active = isActive;
		prefs.putBoolean(getInternalName() + PREFSID_PLUGIN_ACTIVE, isActive);
		try {
			prefs.flush();
		} catch (Exception e) {
			Util.errMsg(null, e.getMessage(), e);
		}
	}

	/**
	 * set the location the component returned by getDockComponent() shall be
	 * docked at. Persistently store setting in a preferences file.
	 * 
	 * @param location
	 *            the dock location, one of DOCK_LOCATION_TOP,
	 *            DOCK_LOCATION_BOTTOM, DOCK_LOCATION.LEFT, DOCK_LOCATION_RIGHT
	 *            or DOCK_LOCATION_NONE, if the component shall not dock.
	 */
	public void setDockLocation(int location) {
		dockLocation = location;
		prefs
				.putInt(getInternalName() + PREFSID_PLUGIN_DOCK_LOCATION,
						location);
		try {
			prefs.flush();
		} catch (Exception e) {
			Util.errMsg(null, e.getMessage(), e);
		}
		createFrameAsNeeded(location);
	}

	/**
	 * get a menu of actions this plug-in provides.
	 * 
	 * <p>
	 * <code>JMenu</code> is a decendant of <code>JMenuItem</code> so this
	 * method may return a single menu item up to a whole structure of submenus
	 * in its return value.
	 * </p>
	 * 
	 * @return the plug-in menu
	 */
	public JMenuItem getPluginMenu() {
		return pMenu;
	}

	/**
	 * get a menu item providing documentation about this plug-in.
	 * 
	 * <p>
	 * <code>JMenu</code> is a decendant of <code>JMenuItem</code> so this
	 * method may return a single menu item up to a whole structure of submenus
	 * in its return value.
	 * </p>
	 * 
	 * @return a menu item with help for this plug-in
	 */
	public JMenuItem getHelpMenu() {
		return hMenu;
	}

	/**
	 * get the name of the plug-in as it shall appear on a GUI.
	 * 
	 * @return the name of the plug-in
	 */
	public String getGUIName() {
		return "AbstractPlugin";
	}

	/**
	 * get the name used internally for this plug-in
	 * 
	 * @return the internal name of this plug-in
	 */
	public String getInternalName() {
		return internalName;
	}

	/**
	 * get the location the component returned by getDockComponent() shall be
	 * docked at.
	 * 
	 * @return the dock location, one of DOCK_LOCATION_TOP,
	 *         DOCK_LOCATION_BOTTOM, DOCK_LOCATION.LEFT, DOCK_LOCATION_RIGHT or
	 *         DOCK_LOCATION_NONE, if the component shall not dock.
	 */
	public int getDockLocation() {
		return dockLocation;
	}

	/**
	 * get the component that this plug-in produces, if any
	 * 
	 * @return the component produced by this plug-in, or null if none is
	 *         produced
	 */
	public JComponent getComponent() {
		return c;
	}

	/**
	 * get the status of the plug-in
	 * 
	 * @return true, if activated, false if not
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * get a string from the resource bundle of the owner of this plug-in
	 * 
	 * @param nm
	 *            the name of the string resource to get
	 * 
	 * @return the string with the given name or null, if none is found
	 */
	public String getOwnerResString(String nm) {
		return Util.getResourceString(SHTMLPanelImpl.getResources(), nm);
	}

	/**
	 * get an action from the resource bundle of the owner of this plug-in
	 * 
	 * @param cmd
	 *            the name of the action to get
	 * 
	 * @return the action with the given name or null, if none is found
	 */
	public Action getOwnerAction(String cmd) {
		return owner.getDynRes().getAction(cmd);
	}

	/* ----------- SimplyHTML plugin interface implementation end --------- */

	/* --------------- class fields start --------------------- */

	/** TextResources of plug-in */
	public static TextResources textResources;

	/** constant for active setting in preferences file */
	public static final String PREFSID_PLUGIN_ACTIVE = "Active";

	/** constant for dock location setting in preferences file */
	public static final String PREFSID_PLUGIN_DOCK_LOCATION = "DockLocation";

	/** the internal name of this plug-in */
	protected String internalName;

	/** the id in the ResourceFile for the menu of this plug-in */
	protected String pluginMenuId;

	/** the id in the ResourceFile for the help menu of this plug-in */
	protected String helpMenuId;

	/** the plug-in menu provided by this plug-in */
	protected JMenuItem pMenu = null;

	/** the help menu provided by this plug-in */
	protected JMenuItem hMenu = null;

	/** status of plug-in */
	protected boolean active = true;

	/** current dock location */
	protected int dockLocation = SHTMLPlugin.DOCK_LOCATION_LEFT;

	/** component of this plug-in */
	protected JComponent c = null;

	/** JFrame for dockLocation=none */
	protected JFrame frame = null;

	/** reference for user preferences for this class */
	protected Preferences prefs;

	/** the owner of this plug in */
	protected SHTMLPanelImpl owner;

	/* ------------- class fields end ------------------ */
}
