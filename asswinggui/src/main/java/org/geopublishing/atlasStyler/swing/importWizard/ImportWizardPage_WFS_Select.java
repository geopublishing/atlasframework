package org.geopublishing.atlasStyler.swing.importWizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CancellationException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASProps.Keys;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.swing.AtlasSwingWorker;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.netbeans.spi.wizard.WizardPage;

import schmitzm.swing.ExceptionDialog;
import skrueger.geotools.io.WfsServerList;
import skrueger.geotools.io.WfsServerSettings;
import skrueger.geotools.io.WfsSettingsJComboBox;
import skrueger.swing.SmallButton;

public class ImportWizardPage_WFS_Select extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(
			ASUtil.R("ImportWizard.WFS.WfsUrlSelection.Explanation"));

	private final String validationImportSourceTypeFailedMsg_CantRead = ASUtil
			.R("ImportWizard.WFS.WfsUrlSelection.ValidationError.CantRead");

	WfsSettingsJComboBox wfsUrlJComboBox;

	private SmallButton wfsAddJButton;

	private SmallButton wfsEditJButton;

	private SmallButton wfsDelJButton;
	final static private JLabel wfsServerSelectionJLabel = new JLabel(
			ASUtil.R("ImportWizard.WFS.SelectionComboboxLabel"));

	public static String getDescription() {
		return ASUtil.R("ImportWizard.WFS.WfsUrlSelection");
	}

	public ImportWizardPage_WFS_Select() {
		initGui();
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		final WfsServerSettings wfsServer = (WfsServerSettings) getWfsUrlJComboBox()
				.getSelectedItem();

		if (wfsServer == null)
			return "Must select a WFS";

		final URL url = wfsServer.getCapabilitiesUrl();

		try {
			url.openStream().close();
		} catch (Exception e) {
			return validationImportSourceTypeFailedMsg_CantRead;
		}

		// final Map m = new HashMap();
		// m.put(WFSDataStoreFactory.URL.key, url);
		// // m.put(WFSDataStoreFactory.LENIENT.key, new
		// // Boolean(true));
		// m.put(WFSDataStoreFactory.TIMEOUT.key, new java.lang.Integer(10000));
		// m.put(WFSDataStoreFactory.MAXFEATURES.key,
		// new java.lang.Integer(ASProps.get(ASProps.Keys.m)));

		AtlasSwingWorker<String[]> wfsGetTypeNames = new AtlasSwingWorker<String[]>(
				ImportWizardPage_WFS_Select.this) {

			@Override
			protected String[] doInBackground() throws IOException,
					InterruptedException {
				final WFSDataStore wfsDs = (new WFSDataStoreFactory())
						.createDataStore(wfsServer);

				try {
					// final String typeName = (String) cfgGui[1];
					final String[] typeNames = wfsDs.getTypeNames();

					wfsServer.setCachedTypeNames(typeNames);

					return typeNames;

				} catch (Exception e1) {
					throw new InterruptedException(e1.getMessage());
				} finally {
					wfsDs.dispose();
				}

			}

		};

		try {

			String[] typeNames = wfsServer.getCachedTypeNames();
			if (typeNames == null) {
				typeNames = wfsGetTypeNames.executeModal();
			}

			if (typeNames.length < 1)
				return "Server connected, but no WFS layers are advertised."; // i8n

			putWizardData(ImportWizard.TYPENAMES, typeNames);
		} catch (CancellationException e) {
			return e.getLocalizedMessage();
		} catch (Exception e) {
			// validationImportSourceTypeFailedMsg_CantRead ?
			return e.getLocalizedMessage();
		}

		return null;
	}

	private void initGui() {
		// setSize(ImportWizard.DEFAULT_WPANEL_SIZE);
		// setPreferredSize(ImportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1"));

		add(explanationJLabel);
		add(wfsServerSelectionJLabel, "gapy unrelated");
		// add(getFileChooserJButton(), "split 2");
		add(getWfsUrlJComboBox(), "growx, left");

		add(getNewWfsJButton(), "split 3, align right");
		add(getEditWfsJButton(), "align right");
		add(getDeleteWfsJButton(), "align right");
	}

	/**
	 * A Button to add a new WFS
	 */
	private JButton getDeleteWfsJButton() {
		if (wfsDelJButton == null) {
			AbstractAction a = new AbstractAction("-") {

				@Override
				public void actionPerformed(ActionEvent e) {

					WfsServerSettings wfs = (WfsServerSettings) getWfsUrlJComboBox()
							.getSelectedItem();

					WfsServerList list = getWfsUrlJComboBox().getWfsList();

					list.remove(wfs);

					updateComboboxModel();

					getWfsUrlJComboBox().setSelectedIndex(
							getWfsUrlJComboBox().getItemCount() - 1);

				}

			};
			wfsDelJButton = new SmallButton(a);

			setEnabled(getWfsUrlJComboBox().getSelectedIndex() >= 0);
			getWfsUrlJComboBox().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setEnabled(getWfsUrlJComboBox().getSelectedIndex() >= 0);
				}
			});

		}
		return wfsDelJButton;
	}

	/**
	 * A Button to edit an existsing WFS configuration
	 */
	private JButton getEditWfsJButton() {
		if (wfsEditJButton == null) {
			AbstractAction a = new AbstractAction("Edit") {

				@Override
				public void actionPerformed(ActionEvent e) {

					WfsServerSettings wfs = (WfsServerSettings) getWfsUrlJComboBox()
							.getSelectedItem();

					createOrEditWfs(wfs);

				}

			};
			wfsEditJButton = new SmallButton(a);

			setEnabled(getWfsUrlJComboBox().getSelectedIndex() >= 0);
			getWfsUrlJComboBox().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setEnabled(getWfsUrlJComboBox().getSelectedIndex() >= 0);
				}
			});

		}
		return wfsEditJButton;
	}

	private void createOrEditWfs(WfsServerSettings wfs) {

		WfsServerSettings newOrEditedDbServer = WfsServerSettings.createOrEdit(
				ImportWizardPage_WFS_Select.this, wfs);

		if (newOrEditedDbServer == null)
			return;

		try {
			WfsServerList list = getWfsUrlJComboBox().getWfsList();
			if (wfs == null) {
				list.add(newOrEditedDbServer);
			}

			updateComboboxModel();

			getWfsUrlJComboBox().setSelectedItem(wfs);

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
		getWfsUrlJComboBox().listChanged();
		// Save the new settings
		ASProps.set(Keys.wfsList, getWfsUrlJComboBox().getWfsList()
				.toPropertiesString());
	}

	/**
	 * A Button to add a new WFS
	 */
	private JButton getNewWfsJButton() {
		if (wfsAddJButton == null) {
			AbstractAction a = new AbstractAction("+") {

				@Override
				public void actionPerformed(ActionEvent e) {
					createOrEditWfs(null);
				}
			};
			wfsAddJButton = new SmallButton(a);

		}
		return wfsAddJButton;
	}

	private WfsSettingsJComboBox getWfsUrlJComboBox() {
		if (wfsUrlJComboBox == null) {

			wfsUrlJComboBox = new WfsSettingsJComboBox(
					WfsServerList.parsePropertiesString(ASProps
							.get(ASProps.Keys.wfsList)));

			wfsUrlJComboBox.setName(ImportWizard.IMPORT_WFS_URL);

			wfsUrlJComboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					WfsServerSettings val = (WfsServerSettings) wfsUrlJComboBox
							.getSelectedItem();
					putWizardData(ImportWizard.IMPORT_WFS_URL, val);
				}
			});
		}

		return wfsUrlJComboBox;
	}
}
