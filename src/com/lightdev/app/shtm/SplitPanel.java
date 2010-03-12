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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
 * A panel to manage a pluggable panel layout, i.e. around a center panel, other
 * panels can be placed very similar to BorderLayout. The difference of this
 * class to BorderLayout is that it creates JSplitPanes for each panel.
 * 
 * (Dan: This class is a JPanel somehow trying to achieve what would better be
 * achieved with Layout. Ideally, it would be removed altogether.)
 * 
 * <p>
 * This uses JTabbedPanes for each of the panels surrounding the center panel.
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
 */

public class SplitPanel extends JPanel {

	/* --------------- class fields start --------------- */

	/** constant for the major axis being the horizontal one */
	public static final int MAJOR_AXIS_HORIZONTAL = 1; // NOT SUPPORTED

	/** constant for the major axis being the vertical one */
	public static final int MAJOR_AXIS_VERTICAL = 2;

	/** constant for the north plug-in container of this SplitPanel */
	public static final int NORTH = 0;

	/** constant for the east plug-in container of this SplitPanel */
	public static final int EAST = 1;

	/** constant for the south plug-in container of this SplitPanel */
	public static final int SOUTH = 2;

	/** constant for the west plug-in container of this SplitPanel */
	public static final int WEST = 3;

	/** constant for the center panel of this SplitPanel */
	public static final int CENTER = 4;

	/** the outer panels of this SplitPanel */
	private JSplitPane[] outerPanels;

	/** current setting for major axis of this SplitPanel */
	private int majorAxis = SplitPanel.MAJOR_AXIS_VERTICAL;

	/* ------ class fields end ------------------ */

	/**
	 * Constructor
	 */
	public SplitPanel() {
		super();
		setLayout(new BorderLayout());

		int[] directions = new int[] { NORTH, EAST, SOUTH, WEST };
		outerPanels = new JSplitPane[directions.length];
		for (int i = 0; i < directions.length; i++) {
			outerPanels[directions[i]] = new JSplitPane();
			outerPanels[directions[i]].setBorder(new EmptyBorder(0, 0, 0, 0));
			outerPanels[directions[i]].setContinuousLayout(true);
		}

		buildLayout();
		restorePrefs();
	}

	/**
	 * Sets up the outer panels.
	 */
	private void buildLayout() {
		removeAll();

		outerPanels[NORTH].setOrientation(JSplitPane.VERTICAL_SPLIT);
		outerPanels[SOUTH].setOrientation(JSplitPane.VERTICAL_SPLIT);
		outerPanels[WEST].setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		outerPanels[EAST].setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		if (majorAxis == SplitPanel.MAJOR_AXIS_VERTICAL) {
			// [ ALWAYS THE CASE ]
			// System.out.println("SplitPanel.buildLayout majorAxis = vertical");
			outerPanels[SOUTH].setTopComponent(outerPanels[NORTH]);
			outerPanels[WEST].setRightComponent(outerPanels[SOUTH]);
			outerPanels[EAST].setLeftComponent(outerPanels[WEST]);
			this.add(outerPanels[EAST], BorderLayout.CENTER);
		} else {
			// [ NOT SUPPORTED ]
			// System.out.println("SplitPanel.buildLayout majorAxis = horizontal");
			outerPanels[SOUTH].setTopComponent(outerPanels[NORTH]);
			outerPanels[WEST].setRightComponent(outerPanels[SOUTH]);
			outerPanels[EAST].setLeftComponent(outerPanels[WEST]);
			this.add(outerPanels[SOUTH], BorderLayout.CENTER);
		}
	}

	/**
	 * Removes panels surrounding the center panel.
	 */
	public void removeAllOuterPanels() {
		// Warning: it does not really remove the outer panels per se.
		JComponent p;
		if (majorAxis == MAJOR_AXIS_VERTICAL) {
			p = (JComponent) outerPanels[NORTH].getTopComponent();
			p.removeAll();
			p.setVisible(false);
			p = (JComponent) outerPanels[WEST].getLeftComponent();
			p.removeAll();
			p.setVisible(false);
			p = (JComponent) outerPanels[SOUTH].getBottomComponent();
			p.removeAll();
			p.setVisible(false);
			p = (JComponent) outerPanels[EAST].getRightComponent();
			p.removeAll();
			p.setVisible(false);
		} else {
			// pending...
		}
	}

	/**
	 * Sets the location of a given divider.
	 * 
	 * @param panel
	 *            the panel to set the location for
	 * @param loc
	 *            the relative location of the divider (0, 0.1, ..., 0.9, 1)
	 */
	public void setDivLoc(int panel, double loc) {
		// System.out.println("SplitPanel.setDivLoc panel=" + panel + ", loc=" +
		// loc);
		outerPanels[panel].setDividerLocation(loc);
	}

	/**
	 * Gets the divider location for a given panel.
	 * 
	 * @param panel
	 *            the panel to get the divider location for
	 * 
	 * @return the divider location
	 */
	public int getDivLoc(int panel) {
		// System.out.println("SplitPanel.getDivLoc panel=" + panel + ", loc=" +
		// outerPanels[panel].getDividerLocation());
		return outerPanels[panel].getDividerLocation();
	}

	/**
	 * Saves divider locations to preferences.
	 */
	public void savePrefs() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		for (int i = 0; i < 4; i++) {
			prefs.putInt("divLoc" + i, outerPanels[i].getDividerLocation());
			// System.out.println("SplitPanel.savePrefs divLoc" + i + "=" +
			// outerPanels[i].getDividerLocation());
		}
	}

	/**
	 * Restores divider locations from preferences.
	 */
	public void restorePrefs() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		for (int i = 0; i < 4; i++) {
			outerPanels[i].setDividerLocation(prefs.getInt("divLoc" + i, 300));
			// System.out.println("SplitPanel.restorePrefs divLoc" + i + "=" +
			// prefs.getInt("divLoc" + i, 300));
		}
	}

	/**
	 * Gets the plug-in container for a given panel.
	 * 
	 * @param location
	 *            the location of the desired container (SplitPanel.NORTH,
	 *            SOUTH, etc.)
	 * 
	 * @return the plug-in container
	 */
	public JTabbedPane getPanel(int location) {
		Component component;
		switch (location) {
		case NORTH:
			component = outerPanels[NORTH].getTopComponent();
			break;
		case EAST:
			component = outerPanels[EAST].getRightComponent();
			break;
		case SOUTH:
			component = outerPanels[SOUTH].getBottomComponent();
			break;
		case WEST:
			component = outerPanels[WEST].getLeftComponent();
			break;
		default:
			component = null;
		}
		return (JTabbedPane) component;
	}

	/**
	 * Shows/hides dividers according to visibility of their associated plug-in
	 * containers.
	 */
	public void adjustDividerSizes() {
		JSplitPane splitPane;

		splitPane = outerPanels[NORTH];
		if (splitPane.getTopComponent().isVisible()) {
			// System.out.println("north panel top is visible, showing divider");
			splitPane.setDividerSize(2);
		} else {
			splitPane.setDividerSize(0);
		}

		splitPane = outerPanels[WEST];
		if (splitPane.getLeftComponent().isVisible()) {
			// System.out.println("west panel left is visible, showing divider");
			splitPane.setDividerSize(2);
		} else {
			splitPane.setDividerSize(0);
		}

		splitPane = outerPanels[SOUTH];
		if (splitPane.getBottomComponent().isVisible()) {
			// System.out.println("south panel bottom is visible, showing divider");
			splitPane.setDividerSize(2);
		} else {
			splitPane.setDividerSize(0);
		}

		splitPane = outerPanels[EAST];
		if (splitPane.getRightComponent().isVisible()) {
			// System.out.println("south panel right is visible, showing divider");
			splitPane.setDividerSize(2);
		} else {
			splitPane.setDividerSize(0);
		}

	}

	/**
	 * Adds a plug-in container to this SplitPanel ata given location.
	 * 
	 * @param c
	 *            the plug-in container to add
	 * @param location
	 *            the location to add to (SplitPanel.NORTH, SOUTH, etc.)
	 */
	public void addComponent(JComponent component, int location) {
		if (majorAxis == SplitPanel.MAJOR_AXIS_VERTICAL) {
			// System.out.println("SplitPanel.addComponent majorAxis = vertical");
			switch (location) {
			case CENTER:
				// System.out.println("SplitPanel.addComponent CENTER");
				outerPanels[NORTH].remove(outerPanels[NORTH]
						.getBottomComponent());
				outerPanels[NORTH].setBottomComponent(component);
				break;
			case NORTH:
				// System.out.println("SplitPanel.addComponent NORTH");
				outerPanels[NORTH].remove(outerPanels[NORTH].getTopComponent());
				outerPanels[NORTH].setTopComponent(component);
				break;
			case WEST:
				// System.out.println("SplitPanel.addComponent WEST");
				outerPanels[WEST].remove(outerPanels[WEST].getLeftComponent());
				outerPanels[WEST].setLeftComponent(component);
				break;
			case SOUTH:
				// System.out.println("SplitPanel.addComponent SOUTH");
				outerPanels[SOUTH].remove(outerPanels[SOUTH]
						.getBottomComponent());
				outerPanels[SOUTH].setBottomComponent(component);
				break;
			case EAST:
				// System.out.println("SplitPanel.addComponent EAST");
				outerPanels[EAST].remove(outerPanels[EAST].getRightComponent());
				outerPanels[EAST].setRightComponent(component);
				break;
			}
		} else {
			// pending...
			switch (location) {
			case CENTER:
				break;
			case NORTH:
				break;
			case WEST:
				break;
			case SOUTH:
				break;
			case EAST:
				break;
			}
		}
	}

}
