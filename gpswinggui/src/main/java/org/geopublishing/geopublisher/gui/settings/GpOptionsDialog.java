package org.geopublishing.geopublisher.gui.settings;

import java.awt.Component;

import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.CancellableTabbedDialogAdapter;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

public class GpOptionsDialog extends CancellableTabbedDialogAdapter {

	private final GeopublisherGUI gpg;
	/**
	 * Prepare buttons
	 */
	final JPanel buttons = createButtons();

	public GpOptionsDialog(final Component parentWindow, GeopublisherGUI gpg) {
		super(parentWindow, GpUtil.R("GpOptionsDialog.title"));
		this.gpg = gpg;
		initGUI();

		SwingUtil.setRelativeFramePosition(this, parentWindow, 0.5, 0.5);

		setVisible(true);


		SwingUtil.setPreferredHeight(this, (int) (getPreferredSize().getHeight() * 1.2));
		SwingUtil.setPreferredWidth(this, (int) (getPreferredSize().getWidth() * 1.2));
		pack();
		LangUtil.sleepExceptionless(100);
		pack();

		setModal(true);
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
		/*** Build GUI ***/
		{
			/** A tab for name, desc and keywords... **/
			getTabbedPane().insertTab(GpUtil.R("GpHostingSettings.title"),
					null, new GpHostingOptionsTab(gpg), null,
					getTabbedPane().getTabCount());

			SwingUtil.setRelativeFramePosition(this, gpg.getJFrame(), .5, .5);
		}

	}

	@Override
	public boolean okClose() {
		GPProps.store();
		return super.okClose();
	}

}
