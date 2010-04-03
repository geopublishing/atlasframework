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
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Dialog to set user preferences for application SimplyHTML.
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

class PrefsDialog extends DialogShell implements ActionListener {

	/** the look and feels avaliable in the system */
	private UIManager.LookAndFeelInfo[] lfinfo;

	/** reference for user preferences for this class */
	protected Preferences prefs = Preferences.userNodeForPackage(getClass());

	/** constant for dock location setting in preferences file */
	public static final String PREFSID_LOOK_AND_FEEL = "Laf";

	public static final String PREFS_USE_STD_STYLE_SHEET = "use_std_styles";

	private String lafName = UIManager.getLookAndFeel().getName();

	private JComboBox lafCombo;
	private JCheckBox useStdStyleSheet;

	/** the help id for this dialog */
	private static final String helpTopicId = "item167";

	public PrefsDialog(Frame parent, String title) {
		super(parent, title, helpTopicId);

		// have a grid bag layout ready to use
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel layoutPanel = new JPanel(g);

		// build a panel for preferences related to the application
		JPanel appPrefsPanel = new JPanel(g);
		Util.addGridBagComponent(appPrefsPanel, new JLabel(Util
				.getResourceString("prfLafLabel")), g, c, 0, 0,
				GridBagConstraints.EAST);
		lafCombo = new JComboBox();
		initLfComboBox();
		Util.addGridBagComponent(appPrefsPanel, lafCombo, g, c, 1, 0,
				GridBagConstraints.EAST);

		// build a panel for preferences related to documents
		/*
		 * JPanel docPrefsPanel = new JPanel(g);
		 * Util.addGridBagComponent(docPrefsPanel, new JCheckBox(
		 * FrmMain.dynRes.getResourceString( FrmMain.resources,
		 * "prfShareDocResourcesLabel")), g, c, 0, 1, GridBagConstraints.EAST);
		 */

		Util.addGridBagComponent(layoutPanel, appPrefsPanel, g, c, 0, 0,
				GridBagConstraints.WEST);

		// add option for standard stlye sheet
		useStdStyleSheet = new JCheckBox(Util
				.getResourceString("linkDefaultStyleSheetLabel"));
		boolean useStyle = prefs.getBoolean(
				PrefsDialog.PREFS_USE_STD_STYLE_SHEET, false);
		useStdStyleSheet.setSelected(useStyle);
		Util.addGridBagComponent(layoutPanel, useStdStyleSheet, g, c, 0, 2,
				GridBagConstraints.WEST);

		// add to content pane of DialogShell
		Container contentPane = super.getContentPane();
		contentPane.add(layoutPanel, BorderLayout.CENTER);
		// contentPane.add(appPrefsPanel, BorderLayout.NORTH);
		// contentPane.add(docPrefsPanel, BorderLayout.CENTER);

		// cause optimal placement of all elements
		pack();
	}

	private void initLfComboBox() {
		lfinfo = UIManager.getInstalledLookAndFeels();
		int count = lfinfo.length;
		String[] lfNames = new String[count];
		for (int i = 0; i < count; i++) {
			lfNames[i] = lfinfo[i].getName();
		}
		lafCombo.setModel(new DefaultComboBoxModel(lfNames));
		lafCombo.setSelectedItem(lafName);
	}

	/**
	 * implements the ActionListener interface to be notified of clicks onto the
	 * ok and cancel button.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Component src = (Component) e.getSource();
		if (src == okButton) {
			savePrefs(src);
		}
		super.actionPerformed(e);
	}

	private void savePrefs(final Component src) {
		try {
			String newLaf = lfinfo[lafCombo.getSelectedIndex()].getClassName();
			if (!lafName.equalsIgnoreCase(newLaf)) {
				prefs.put(PREFSID_LOOK_AND_FEEL, newLaf);
				UIManager.setLookAndFeel(newLaf);
				SwingUtilities.updateComponentTreeUI(JOptionPane
						.getFrameForComponent(src));
			}
			prefs.putBoolean(PREFS_USE_STD_STYLE_SHEET, useStdStyleSheet
					.isSelected());
		} catch (Exception ex) {
			Util.errMsg(this, ex.getMessage(), ex);
		}
	}
}
