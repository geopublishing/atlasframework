package org.geopublishing.geopublisher.gui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GPProps.Keys;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.export.GpHosterServerSettings;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.swing.Cancellable;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SmallButton;
import de.schmitzm.swing.SwingUtil;

public class GpHostingOptionsTab extends JPanel implements Cancellable {

	private final GeopublisherGUI gpg;
	private JButton dbEditJButton;
	private GpHosterServerListJComboBox dbJComboBox;
	private JButton dbAddJButton;
	private SmallButton dbDelJButton;
	private final Map backupProperties = new HashMap();

	public GpHostingOptionsTab(GeopublisherGUI gpg) {
		super(new MigLayout("wrap 1, w ::400px"));
		this.gpg = gpg;

		// i8n
		JLabel explanationJLabel = new JLabel(
				"<html>This server setting defines which server to contact when Geopublisher is exporting to the Internet. Usually the geopublishing.org Server is the correct choice and should not be changed.</html>");
		JLabel dbServerSelectionJLabel = new JLabel("GpHoster Server"); // i8n
		add(explanationJLabel, "growx, push");

		add(dbServerSelectionJLabel, "split 2, gapy unrelated, push");
		add(getDbJComboBox(), "growx, w 200::, left");

		add(getNewDbJButton(), "split 3, align right, push");
		add(getEditServerJButton(), "align right");
		add(getDeleteDbJButton(), "align right");

		// Backup properties so we can cancel
		for (Object key : GPProps.getProperties().keySet()) {
			backupProperties.put(key, GPProps.getProperties().get(key));
		}
	}

	/**
	 * A Button to add a new DB
	 */
	private JButton getNewDbJButton() {
		if (dbAddJButton == null) {
			AbstractAction a = new AbstractAction("+") {

				@Override
				public void actionPerformed(ActionEvent e) {
					createOrEditDb(null);
				}
			};
			dbAddJButton = new SmallButton(a);

		}
		return dbAddJButton;
	}

	public static String getDescription() {
		return GpUtil.R(GeopublisherGUI.R("GpHostingSettings.title"));
	}

	/**
	 * A Button to delete a DB configuration
	 */
	private JButton getDeleteDbJButton() {
		if (dbDelJButton == null) {
			AbstractAction a = new AbstractAction("-") {

				@Override
				public void actionPerformed(ActionEvent e) {

					GpHosterServerSettings db = (GpHosterServerSettings) getDbJComboBox().getSelectedItem();

					GpHosterServerList oldList = getDbJComboBox().getDbList();

					oldList.remove(db);

					updateComboboxModel();

					getDbJComboBox().setSelectedIndex(getDbJComboBox().getItemCount() - 1);

				}

			};
			dbDelJButton = new SmallButton(a);

			dbDelJButton.setEnabled(getDbJComboBox().getSelectedIndex() >= 0);
			getDbJComboBox().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dbDelJButton.setEnabled(getDbJComboBox().getSelectedIndex() >= 0);
				}
			});

		}
		return dbDelJButton;
	}

	/**
	 * Updates the Combobox Model with the list of server stored in the porperties. This does not automatically trigger
	 * a validation, since the selected Item is not explicitly set.
	 */
	private void updateComboboxModel() {
		getDbJComboBox().listChanged();
		// Save the new settings
		GPProps.set(Keys.gpHosterServerList, getDbJComboBox().getDbList().toPropertiesString());
		GPProps.set(Keys.lastGpHosterServerIdx, getDbJComboBox().getSelectedIndex());
		GPProps.store();
	}

	private void createOrEditDb(GpHosterServerSettings dbServer) {
		GpHosterServerSettings newOrEditedDbServer = GpHosterServerSettings.createOrEdit(GpHostingOptionsTab.this,
				dbServer);

		if (newOrEditedDbServer == null)
			return;

		try {
			GpHosterServerList dbList = getDbJComboBox().getDbList();
			if (dbServer == null) {
				dbList.add(newOrEditedDbServer);
			}

			updateComboboxModel();

			getDbJComboBox().setSelectedItem(newOrEditedDbServer);

		} catch (Exception ee) {
			ExceptionDialog.show(ee);
		}
	}

	private GpHosterServerListJComboBox getDbJComboBox() {
		if (dbJComboBox == null) {

			final String propertiesString = GPProps.get(GPProps.Keys.gpHosterServerList,
					GpHosterServerSettings.DEFAULT.toPropertiesString());
			dbJComboBox = new GpHosterServerListJComboBox(new GpHosterServerList(propertiesString));

			SwingUtil.addMouseWheelForCombobox(dbJComboBox);

			// Select the last used server
			if (GPProps.get(Keys.lastGpHosterServerIdx) != null) {

				// TODO

				Integer idx = GPProps.getInt(Keys.lastGpHosterServerIdx, -1);
				if (idx < dbJComboBox.getDbList().size())
					dbJComboBox.setSelectedIndex(idx);
			}

			dbJComboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// Store the last used server index
					GPProps.set(Keys.lastGpHosterServerIdx, dbJComboBox.getSelectedIndex());
				}
			});

		}

		return dbJComboBox;
	}

	/**
	 * A Button to edit an existsing DB configuration
	 */
	private JButton getEditServerJButton() {
		if (dbEditJButton == null) {
			AbstractAction a = new AbstractAction("Edit") {// i8n

				@Override
				public void actionPerformed(ActionEvent e) {

					GpHosterServerSettings db = (GpHosterServerSettings) getDbJComboBox().getSelectedItem();

					createOrEditDb(db);

				}

			};
			dbEditJButton = new SmallButton(a);

			dbEditJButton.setEnabled(getDbJComboBox().getSelectedIndex() >= 0);
			getDbJComboBox().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dbEditJButton.setEnabled(getDbJComboBox().getSelectedIndex() >= 0);
				}
			});

		}
		return dbEditJButton;
	}

	@Override
	public void cancel() {
		// Settings in GPProps revert
		GPProps.getProperties().clear();
		GPProps.getProperties().putAll(backupProperties);
		GPProps.store();
	}
}
