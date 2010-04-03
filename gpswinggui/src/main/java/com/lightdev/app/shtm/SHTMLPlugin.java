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

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * Defines an interface all plug-ins for application SimplyHTML have to
 * implement.
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

public interface SHTMLPlugin {

	/** indicates docking is not requested */
	public static final int DOCK_LOCATION_NONE = 0;

	/** indicates docking requested on top of a given container */
	public static final int DOCK_LOCATION_TOP = 1;

	/** indicates docking requested on the right of a given container */
	public static final int DOCK_LOCATION_RIGHT = 2;

	/** indicates docking requested on bottom of a given container */
	public static final int DOCK_LOCATION_BOTTOM = 3;

	/** indicates docking requested on the left of a given container */
	public static final int DOCK_LOCATION_LEFT = 4;

	/**
	 * get the name of the plug-in as it shall appear on a GUI.
	 * 
	 * @return the name of the plug-in
	 */
	public String getGUIName();

	/**
	 * get the name used internally for this plug-in
	 * 
	 * @return the internal name of this plug-in
	 */
	public String getInternalName();

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
	public JMenuItem getPluginMenu();

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
	public JMenuItem getHelpMenu();

	/**
	 * get the location the component returned by getDockComponent() shall be
	 * docked at.
	 * 
	 * @return the dock location, one of DOCK_LOCATION_TOP,
	 *         DOCK_LOCATION_BOTTOM, DOCK_LOCATION.LEFT, DOCK_LOCATION_RIGHT or
	 *         DOCK_LOCATION_NONE, if the component shall not dock.
	 */
	public int getDockLocation();

	/**
	 * set the location the component returned by getDockComponent() shall be
	 * docked at.
	 * 
	 * @param location
	 *            the dock location, one of DOCK_LOCATION_TOP,
	 *            DOCK_LOCATION_BOTTOM, DOCK_LOCATION.LEFT, DOCK_LOCATION_RIGHT
	 *            or DOCK_LOCATION_NONE, if the component shall not dock.
	 */
	public void setDockLocation(int location);

	/**
	 * get the component that this plug-in produces, if any
	 * 
	 * @return the component produced by this plug-in, or null if none is
	 *         produced
	 */
	public JComponent getComponent();

	/**
	 * get the status of the plug-in
	 * 
	 * @return true, if activated, false if not
	 */
	public boolean isActive();

	/**
	 * set status of plug-in
	 * 
	 * @param isActive
	 *            indicates whether or not the plug-in shall be activated
	 */
	public void setStatus(boolean isActive);

	/**
	 * set the owner of this plug-in
	 * 
	 * @param owner
	 *            the main frame of the instance of SimplyHTML creating the
	 *            plug-in
	 */
	public void setOwner(SHTMLPanelImpl owner);

	/**
	 * get the owner of this plug-in
	 * 
	 * @return the main frame of the instance of SimplyHTML that created the
	 *         plug-in
	 */
	public SHTMLPanelImpl getOwner();

	/**
	 * get a string from the resource bundle of the owner of this plug-in
	 * 
	 * @param nm
	 *            the name of the string resource to get
	 * 
	 * @return the string with the given name or null, if none is found
	 */
	public String getOwnerResString(String nm);

	/**
	 * get an action from the resource bundle of the owner of this plug-in
	 * 
	 * @param cmd
	 *            the name of the action to get
	 * 
	 * @return the action with the given name or null, if none is found
	 */
	public Action getOwnerAction(String cmd);

	/**
	 * init the plug-in
	 * 
	 * this is called by the PluginManager directly after instantiating the
	 * plug-in
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
	public void initPlugin(SHTMLPanelImpl owner, String internalName,
			String pluginMenuId, String helpMenuId);

	public void initHelpMenu();

	public void showInitialInfo();
}
