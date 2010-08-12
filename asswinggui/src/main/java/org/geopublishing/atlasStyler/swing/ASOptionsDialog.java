/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.ASProps.Keys;

import schmitzm.swing.SwingUtil;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

public class ASOptionsDialog extends CancellableDialogAdapter {
	final JCheckBox overideLocaleCB = new JCheckBox();

	// final JComboBox langComboBox = new JComboBox(new String[] { "en", "fr",
	// "de" });

	final JComboBox langComboBox = new JComboBox(ASUtil.getSupportedLanguages());

	private final AtlasStylerGUI asg;

	public ASOptionsDialog(final Component parentWindow, AtlasStylerGUI asg) {
		super(parentWindow);
		this.asg = asg;

		initGUI();

		pack();

		SwingUtil.setRelativeFramePosition(this, parentWindow, 0.5, 0.5);

		setModal(true);
		setVisible(true);
	}

	/**
	 * Options.ForceLocaleCheckboxLabel=Override system locale =Locale to use
	 * instead: Options.ForceLocaleSettingNoteRestart=Note: Enabling / changing
	 * overide on the locale requires an application restart.
	 * Options.SystemLocaleInformation=Additional Language Information
	 * Options.SystemLocaleInformationSystemLocale=Detected active locale on
	 * your system: ${0}
	 */
	private void initGUI() {

		setTitle(AtlasStyler.R("Options.ButtonLabel"));

		setLayout(new MigLayout("wrap 3"));

		final JLabel langSelectionLabel = new JLabel(AtlasStyler
				.R("Options.ForceLocaleSetting"));

		final JLabel noteLocaleChangesNeedRestartLabel = new JLabel(AtlasStyler
				.R("Options.ForceLocaleSettingNoteRestart"));
		overideLocaleCB.setAction(new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				boolean enabled = overideLocaleCB.isSelected();

				langSelectionLabel.setEnabled(enabled);
				langComboBox.setEnabled(enabled);
			}
		});

		// Fist line
		add(overideLocaleCB);
		add(new JLabel(AtlasStyler.R("Options.ForceLocaleCheckboxLabel")),
				"span 2");

		// Second line
		add(new JLabel());
		add(langSelectionLabel);
		add(langComboBox);

		// Third line
		add(new JLabel());
		add(noteLocaleChangesNeedRestartLabel, "span 2");

		// fourth line
		add(new JLabel(AtlasStyler.R("Options.SystemLocaleInformation")),
				"span 3");

		// fifth line
		add(new JLabel());
		add(new JLabel(AtlasStyler.R(
				"Options.SystemLocaleInformationSystemLocale", Locale
						.getDefault())), "span 2");

		// Sixth line
		add(new JLabel(AtlasStyler.R("Options.Performance")), "span 3");
		
		// 7th line
		add(new JLabel());
//		add(new JLabel(AtlasStyler.R("Options.Performance.Antialiasing")), "span 1");
		add(getJButtonAntiAliasing(), "span 2");

		// End

		// Buttons
		OkButton okBtn = new OkButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				okClose();
			}
		});

		CancelButton cancelBtn = new CancelButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancelClose();
			}
		});

		add(okBtn, "tag ok, span 3, split 2");
		add(cancelBtn, "tag cancel");

		// Initialize what is enabled
		boolean defaultInUse = ASProps.get(Keys.language, "system")
				.equalsIgnoreCase("system");
		overideLocaleCB.setSelected(!defaultInUse);
		langSelectionLabel.setEnabled(!defaultInUse);
		langComboBox.setEnabled(!defaultInUse);

		// if (defaultInUse) {
		langComboBox.setSelectedItem(ASProps.get(Keys.language, "en"));
		// }

	}

	@Override
	public void cancel() {
	}

	private JCheckBox getJButtonAntiAliasing() {
		final JCheckBox jCheckboxAntiAliasing = new JCheckBox(
				AtlasStyler.R("Options.Performance.Antialiasing"));
		jCheckboxAntiAliasing.setSelected(ASProps.getInt(
				ASProps.Keys.antialiasingMaps, 1) != 1);

		jCheckboxAntiAliasing.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						boolean b = !jCheckboxAntiAliasing.isSelected();
						ASProps.set(ASProps.Keys.antialiasingMaps, b ? "1"
								: "0");

						asg.getStylerMapView().getMapPane().setAntiAliasing(b);
						asg.getStylerMapView().getMapPane().repaint();
					}
				});
			}
		});
		return jCheckboxAntiAliasing;
	}

	@Override
	public boolean okClose() {

		if (!overideLocaleCB.isSelected()) {
			ASProps.set(Keys.language, "system");
		} else {
			ASProps.set(Keys.language, (String) langComboBox.getSelectedItem());
		}

		ASProps.store();

		return super.okClose();
	}

}
