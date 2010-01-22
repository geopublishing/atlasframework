package skrueger.sld.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;
import schmitzm.swing.SwingUtil;
import skrueger.sld.ASProps;
import skrueger.sld.AtlasStyler;
import skrueger.sld.ASProps.Keys;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

public class ASOptionsDialog extends CancellableDialogAdapter {
	final JCheckBox overideLocaleCB = new JCheckBox();
	final JComboBox langComboBox = new JComboBox(new String[] { "en", "fr",
			"de" });

	public ASOptionsDialog(final Component parentWindow) {
		super(parentWindow);
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

		add(overideLocaleCB);
		add(new JLabel(AtlasStyler.R("Options.ForceLocaleCheckboxLabel")),
				"span 2");

		// second line
		add(new JLabel());
		add(langSelectionLabel);
		add(langComboBox);

		// third line
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
		
		// Buttons
		OkButton okBtn = new OkButton(new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				okClose();
			}
		});
		
		CancelButton cancelBtn = new CancelButton( new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelClose();
			}
		});
		
		add(okBtn,"tag ok, span 3, split 2");
		add(cancelBtn,"tag cancel");

		// Initialize what is enabled
		boolean defaultInUse = ASProps.get(Keys.language, "system")
				.equalsIgnoreCase("system");
		overideLocaleCB.setSelected(!defaultInUse);
		langSelectionLabel.setEnabled(!defaultInUse);
		langComboBox.setEnabled(!defaultInUse);

		if (defaultInUse) {
			langComboBox.setSelectedItem(ASProps.get(Keys.language, "en"));
		}
		
	}

	@Override
	public void cancel() {
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
