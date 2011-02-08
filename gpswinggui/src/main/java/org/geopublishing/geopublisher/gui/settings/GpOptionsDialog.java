package org.geopublishing.geopublisher.gui.settings;

import java.awt.Component;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

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
		super(parentWindow);
		this.gpg = gpg;

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
		/*** Build GUI ***/
		{
			/** A tab for name, desc and keywords... **/
			getTabbedPane().insertTab("Hosting", null,
					new GpHostingOptionsTab(gpg), null,
					getTabbedPane().getTabCount());

			/**
			 * Building the content pane
			 */
			final JPanel contentPane = new JPanel(new MigLayout("wrap 1"));
			contentPane.add(getTabbedPane());
			contentPane.add(buttons);

			setContentPane(contentPane);
			pack();
			SwingUtil.setRelativeFramePosition(this, gpg.getJFrame(), .5, .5);
		}

	}

	@Override
	public boolean okClose() {

		GPProps.store();

		return super.okClose();
	}

}
