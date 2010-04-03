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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * User interface for changing plug-in settings.
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

class PluginManagerDialog extends DialogShell implements ListSelectionListener,
		ActionListener {
	/** combo box for selecting the dock location */
	private JComboBox dockLocation;

	/** indicates if we can ignore changes (when happenig programmatically */
	private boolean ignoreChanges = false;

	/** the list with available plug-ins */
	private JList pluginNames;

	/** constant for activation button label */
	private String activateName = Util.getResourceString("activatePlugin");

	/** constant for deactivation button label */
	private String deactivateName = Util.getResourceString("deactivatePlugin");

	/** button to toggle plug-in activation state */
	private JButton toggleActivationButton;

	/** checkbox to toggle plug-in activation state */
	private JCheckBox toggleActivationCheckbox;

	/**
	 * construct a new <code>PluginManagerDialog</code>
	 * 
	 * @param parent
	 *            the parent frame
	 * @param title
	 *            the title of the dialog
	 */
	public PluginManagerDialog(Frame parent, String title) {
		super(parent, title);
		Container contentPane = super.getContentPane();

		okButton.setText(Util.getResourceString("close"));
		cancelButton.setVisible(false);

		GridBagLayout g;
		GridBagConstraints c = new GridBagConstraints();

		/** create panel to show and select plug-ins */
		JPanel pluginPanel = new JPanel(new BorderLayout());
		pluginPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), Util
				.getResourceString("pluginPanelTitle")));
		// pluginPanel.setMinimumSize(new Dimension(400, 400));

		/** load a list with plug-in names and add it to the plug-in panel */
		Enumeration plugins = SHTMLPanelImpl.pluginManager.plugins();
		pluginNames = new JList(SHTMLPanelImpl.pluginManager.getPluginNames());
		pluginNames.addListSelectionListener(this);
		pluginNames.setMinimumSize(new Dimension(250, 400));
		pluginNames.setPreferredSize(new Dimension(250, 400));
		pluginPanel.add(new JScrollPane(pluginNames), BorderLayout.CENTER);

		/** create panel for actins on loaded plug-ins */
		JPanel actionPanel = new JPanel();
		toggleActivationButton = new JButton(activateName);
		toggleActivationButton.setEnabled(false);
		toggleActivationButton.addActionListener(this);
		actionPanel.add(toggleActivationButton);
		pluginPanel.add(actionPanel, BorderLayout.SOUTH);

		/** create panel to edit settings for a plug-in */
		g = new GridBagLayout();
		JPanel pluginSettingsPanel = new JPanel(g);
		toggleActivationCheckbox = new JCheckBox(
				"togglePluginActivationCheckbox");
		toggleActivationCheckbox.setEnabled(false);
		toggleActivationCheckbox.addActionListener(this);
		Util.addGridBagComponent(pluginSettingsPanel, toggleActivationCheckbox,
				g, c, 0, 0, GridBagConstraints.WEST);

		Util.addGridBagComponent(pluginSettingsPanel, new JLabel(Util
				.getResourceString("dockLocationLabel")), g, c, 0, 1,
				GridBagConstraints.EAST);
		String[] locations = {
				Util.getResourceString("pluginDockLocationNone"),
				Util.getResourceString("pluginDockLocationTop"),
				Util.getResourceString("pluginDockLocationRight"),
				Util.getResourceString("pluginDockLocationBottom"),
				Util.getResourceString("pluginDockLocationLeft"), };
		dockLocation = new JComboBox(locations);
		dockLocation.setEnabled(false);
		dockLocation.addActionListener(this);
		Util.addGridBagComponent(pluginSettingsPanel, dockLocation, g, c, 1, 1,
				GridBagConstraints.WEST);

		/** add components to dialog */
		contentPane.add(pluginPanel, BorderLayout.WEST);
		JPanel centerPanel = new JPanel(new BorderLayout());
		JPanel centerWestPanel = new JPanel(new BorderLayout());
		centerWestPanel.add(pluginSettingsPanel, BorderLayout.NORTH);
		centerPanel.add(centerWestPanel, BorderLayout.WEST);
		centerPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), Util
				.getResourceString("pluginSettingsPanelTitle")));
		// centerPanel.setPreferredSize(new Dimension(200,400));
		contentPane.add(centerPanel, BorderLayout.CENTER);

		pack();
	}

	/**
	 * ListSelectionListener implementation
	 */
	public void valueChanged(ListSelectionEvent e) {
		ignoreChanges = true;
		if (pluginNames.getSelectedIndex() > -1) {
			SHTMLPlugin p = getSelectedPlugin();
			boolean active = p.isActive();
			updateActivationButtonText(active);
			toggleActivationButton.setEnabled(true);
			toggleActivationCheckbox.setEnabled(true);
			toggleActivationCheckbox.setSelected(active);
			dockLocation.setSelectedIndex(p.getDockLocation());
			dockLocation.setEnabled(true);
		} else {
			toggleActivationButton.setEnabled(false);
			toggleActivationCheckbox.setEnabled(false);
			dockLocation.setEnabled(false);
		}
		ignoreChanges = false;
	}

	/**
	 * helper method for getting the currently selected line in the list of
	 * plug-ins
	 */
	private SHTMLPlugin getSelectedPlugin() {
		String name = (String) pluginNames.getSelectedValue();
		return SHTMLPanelImpl.pluginManager.pluginForName(name);
	}

	/**
	 * helper method to toggle the button text between activate and deactivate
	 */
	private void updateActivationButtonText(boolean active) {
		if (active) {
			toggleActivationButton.setText(deactivateName);
		} else {
			toggleActivationButton.setText(activateName);
		}
	}

	/**
	 * ActionListener implementation
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if ((pluginNames.getSelectedIndex() > -1) && (!ignoreChanges)) {
			ignoreChanges = true;
			SHTMLPlugin p = getSelectedPlugin();
			if (source.equals(toggleActivationButton)) {
				p.setStatus(!p.isActive());
			} else if (source.equals(toggleActivationCheckbox)) {
				p.setStatus(!p.isActive());
			} else if (source.equals(dockLocation)) {
				p.setDockLocation(dockLocation.getSelectedIndex());
			} else {
				super.actionPerformed(e);
			}
			boolean active = p.isActive();
			toggleActivationCheckbox.setSelected(active);
			updateActivationButtonText(active);
			ignoreChanges = false;
		} else {
			super.actionPerformed(e);
		}
	}

}
