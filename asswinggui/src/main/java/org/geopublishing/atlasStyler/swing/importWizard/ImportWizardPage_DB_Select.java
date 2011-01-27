package org.geopublishing.atlasStyler.swing.importWizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.CancellationException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASProps.Keys;
import org.geopublishing.atlasStyler.AsSwingUtil;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.geotools.io.DbServerList;
import de.schmitzm.geotools.io.DbSettingsJComboBox;
import de.schmitzm.geotools.io.GtDbServerSettings;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SmallButton;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class ImportWizardPage_DB_Select extends WizardPage {

	final static Logger log = Logger
			.getLogger(ImportWizardPage_DB_Select.class);
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(
			AsSwingUtil.R("ImportWizard.DB.ServerSelection.Explanation"));

	private final String validationImportSourceTypeFailedMsg_CantRead = AsSwingUtil
			.R("ImportWizard.DB.ServerSelection.ValidationError.CantRead");

	DbSettingsJComboBox dbJComboBox;

	private SmallButton dbAddJButton;

	private SmallButton dbEditJButton;

	private SmallButton dbDelJButton;
	final static private JLabel dbServerSelectionJLabel = new JLabel(
			AsSwingUtil.R("ImportWizard.DB.SelectionComboboxLabel"));

	public static String getDescription() {
		return AsSwingUtil.R("ImportWizard.DB.ServerSelection");
	}

	public ImportWizardPage_DB_Select() {
		initGui();
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		final GtDbServerSettings dbServer = (GtDbServerSettings) getDbJComboBox()
				.getSelectedItem();

		if (dbServer == null)
			return "Must select a DB";

		if (!dbServer.isWellDefined())
			return "Server not well defined.";

		AtlasSwingWorker<String[]> dbGetTypeNames = new AtlasSwingWorker<String[]>(
				ImportWizardPage_DB_Select.this) {

			@Override
			protected String[] doInBackground() throws IOException,
					InterruptedException {

				DataStore dbDs = DataStoreFinder.getDataStore(dbServer);

				if (dbDs == null)
					throw new InterruptedException(
							"DataStoreFinder returned null");

				try {
					String[] typeNames = dbDs.getTypeNames();

					String[] describedTablesWithGeometry = dbServer
							.getDescribedTablesWithGeometry();
					for (String s : typeNames.clone()) {
						if (!ArrayUtils
								.contains(describedTablesWithGeometry, s)) {
							typeNames = (String[]) ArrayUtils.remove(typeNames,
									ArrayUtils.indexOf(typeNames, s));
							log.debug("Table "
									+ s
									+ " has been removed from the list of available types, since it is not described in geometry columns");
						}
					}

					dbServer.setCachedTypeNames(typeNames);

					return typeNames;

				} catch (Exception e1) {
					throw new InterruptedException(e1.getMessage());
				} finally {
					dbDs.dispose();
				}

			}

		};

		try {

			String[] typeNames = dbServer.getCachedTypeNames();
			if (typeNames == null) {
				typeNames = dbGetTypeNames.executeModal();
			}

			if (typeNames.length < 1)
				return "Server connected, but no tables found."; // i8n

			putWizardData(ImportWizard.TYPENAMES, typeNames);
		} catch (CancellationException e) {
			return e.getLocalizedMessage();
		} catch (Exception e) {
			// validationImportSourceTypeFailedMsg_CantRead ?

			if (e.getMessage() != null
					&& (e.getMessage().contains("password") || e.getMessage()
							.contains("authent"))) {
				return ("Password and/or username rejected.");
			}

			return validationImportSourceTypeFailedMsg_CantRead;
		}

		return null;
	}

	private void initGui() {
		setLayout(new MigLayout("wrap 1"));

		add(explanationJLabel);
		add(dbServerSelectionJLabel, "gapy unrelated");
		add(getDbJComboBox(), "growx, left");

		add(getNewDbJButton(), "split 3, align right");
		add(getEditDbJButton(), "align right");
		add(getDeleteDbJButton(), "align right");
	}

	/**
	 * A Button to add a new DB
	 */
	private JButton getDeleteDbJButton() {
		if (dbDelJButton == null) {
			AbstractAction a = new AbstractAction("-") {

				@Override
				public void actionPerformed(ActionEvent e) {

					GtDbServerSettings db = (GtDbServerSettings) getDbJComboBox()
							.getSelectedItem();

					DbServerList oldList = getDbJComboBox().getDbList();

					oldList.remove(db);

					updateComboboxModel();

					getDbJComboBox().setSelectedIndex(
							getDbJComboBox().getItemCount() - 1);

				}

			};
			dbDelJButton = new SmallButton(a);

			setEnabled(getDbJComboBox().getSelectedIndex() >= 0);
			getDbJComboBox().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setEnabled(getDbJComboBox().getSelectedIndex() >= 0);
				}
			});

		}
		return dbDelJButton;
	}

	/**
	 * A Button to edit an existsing DB configuration
	 */
	private JButton getEditDbJButton() {
		if (dbEditJButton == null) {
			AbstractAction a = new AbstractAction("Edit") {

				@Override
				public void actionPerformed(ActionEvent e) {

					GtDbServerSettings db = (GtDbServerSettings) getDbJComboBox()
							.getSelectedItem();

					createOrEditDb(db);

				}

			};
			dbEditJButton = new SmallButton(a);

			setEnabled(getDbJComboBox().getSelectedIndex() >= 0);
			getDbJComboBox().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setEnabled(getDbJComboBox().getSelectedIndex() >= 0);
				}
			});

		}
		return dbEditJButton;
	}

	private void createOrEditDb(GtDbServerSettings dbServer) {
		GtDbServerSettings newOrEditedDbServer = GtDbServerSettings.createOrEdit(
				ImportWizardPage_DB_Select.this, dbServer);

		if (newOrEditedDbServer == null)
			return;

		try {
			DbServerList dbList = getDbJComboBox().getDbList();
			if (dbServer == null) {
				dbList.add(newOrEditedDbServer);
			}

			updateComboboxModel();

			getDbJComboBox().setSelectedItem(newOrEditedDbServer);

		} catch (Exception ee) {
			ExceptionDialog.show(ee);
		}
	}

	/**
	 * Updates the Combobox Model with the list of server stored in the
	 * porperties. This does not automatically trigger a validation, since the
	 * selected Item is not explicitly set.
	 */
	private void updateComboboxModel() {
		getDbJComboBox().listChanged();
		// Save the new settings
		ASProps.set(Keys.dbList, getDbJComboBox().getDbList()
				.toPropertiesString());
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

	private DbSettingsJComboBox getDbJComboBox() {
		if (dbJComboBox == null) {

			dbJComboBox = new DbSettingsJComboBox(
					new DbServerList(ASProps
							.get(ASProps.Keys.dbList)));
			
			SwingUtil.addMouseWheelForCombobox(dbJComboBox);

			dbJComboBox.setName(ImportWizard.IMPORT_DB);

			// Select the last used server
			if (ASProps.get(Keys.lastDbIdx) != null) {
				Integer idx = ASProps.getInt(Keys.lastDbIdx, -1);
				if (idx < dbJComboBox.getDbList().size())
					dbJComboBox.setSelectedIndex(idx);
			}

			dbJComboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					GtDbServerSettings val = (GtDbServerSettings) dbJComboBox
							.getSelectedItem();
					putWizardData(ImportWizard.IMPORT_DB, val);

					// Store the last used server index
					ASProps.set(Keys.lastDbIdx, dbJComboBox.getSelectedIndex());
				}
			});

		}

		return dbJComboBox;
	}
}
