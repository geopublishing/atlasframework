package org.geopublishing.geopublisher.gui.importwizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.AMLImport;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.internal.CheckableAtlasJTree;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.swing.SmallButton;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class ImportWizardPage_GPA_Select_DPEs_And_Maps_To_Import extends
		WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(GeopublisherGUI
			.R("ImportWizard.GPA.AtlasContentSelection.Explanation"));

	private final String validationImportSourceTypeFailedMsg_NoneSelected = GeopublisherGUI
			.R("ImportWizard.GPA.AtlasContentSelection.ValidationError.NoneSelected");

	JTextField folderJTextField;

	/**
	 * Represents the atlas.gpa of the atlas that is successfully loaded and
	 * shown in the {@link CheckableAtlasJTree}
	 **/
	private File gpaFile = null;

	private CheckableAtlasJTree atlasTree;
	private AtlasConfig externalAtlasConfig = null;

	public static String getDescription() {
		return GeopublisherGUI.R("ImportWizard.GPA.AtlasContentSelection");
	}

	public ImportWizardPage_GPA_Select_DPEs_And_Maps_To_Import() {
		initGui();
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		try {

			testForUpdateJTree();

		} catch (Exception e) {
			return e.getLocalizedMessage();
		}

		if (getAtlasTree().getSelectedIds().size() == 0) {
			return validationImportSourceTypeFailedMsg_NoneSelected;
		}

		// Store the list of selected ID in the Wizard
		getWizardDataMap().put(ImportWizard.IMPORT_GPA_IDLIST,
				getAtlasTree().getSelectedIds());

		// Store the list of selected ID in the Wizard
		getWizardDataMap().put(ImportWizard.IMPORT_GPA_ATLASCONFIG,
				externalAtlasConfig);

		return null;
	}

	private void testForUpdateJTree() {

		String tf = (String) getWizardData(ImportWizard.IMPORT_GPA_FOLDER);

		if (tf == null)
			return;
		//
		// File atlasxmlFile_InWizardData = new File(new File(tf)
		// .getParentFile(), AtlasConfig.ATLASDATA_DIRNAME + "/"
		// + AtlasConfig.ATLAS_XML_FILENAME);

		File gpaFile_InWizardData = new File(tf);

		if (gpaFile == null
				|| !gpaFile_InWizardData.getAbsolutePath().equals(
						gpaFile.getAbsolutePath())) {

			gpaFile = gpaFile_InWizardData;

			AtlasStatusDialog statusDialog = new AtlasStatusDialog(getParent());
			try {
				AtlasSwingWorker<AtlasConfigEditable> atlasSwingWorker = new AtlasSwingWorker<AtlasConfigEditable>(
						statusDialog) {

					@Override
					protected AtlasConfigEditable doInBackground()
							throws Exception {

						return new AMLImport().parseAtlasConfig(statusDialog,
								gpaFile);
					}
				};

				// atlasSwingWorker.execute();
				// externalAtlasConfig = atlasSwingWorker.get();

				externalAtlasConfig = atlasSwingWorker.executeModal();

				getAtlasTree().setAtlasConfig(externalAtlasConfig);

				return;
			} catch (Exception e) {
				gpaFile = null;
				e.printStackTrace();

			}
		}
	}

	private CheckableAtlasJTree getAtlasTree() {
		if (atlasTree == null) {
			// atlasxmlFile = atlasxmlFile_InWizardData;
			atlasTree = new CheckableAtlasJTree();

			atlasTree.setName("asdasdas");
		}
		return atlasTree;
	}

	private void initGui() {
		// setSize(ImportWizard.DEFAULT_WPANEL_SIZE);
		// setPreferredSize(ImportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 1","[grow]","[grow]"));

		add(explanationJLabel);
		add(new JScrollPane(getAtlasTree()), "growy 2000");

		/*
		 * =All ImportWizard.GPA.AtlasContentSelection.AllMapsButton=All maps
		 * ImportWizard.GPA.AtlasContentSelection.AllDpeButton=All dpe
		 * ImportWizard.GPA.AtlasContentSelection.NoneButton
		 */

		// Button to select all
		add(new SmallButton(new AbstractAction(GeopublisherGUI
				.R("ImportWizard.GPA.AtlasContentSelection.AllButton")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				getAtlasTree().getSelectedIds().addAll(
						externalAtlasConfig.getMapPool().keySet());
				getAtlasTree().getSelectedIds().addAll(
						externalAtlasConfig.getDataPool().keySet());
				getAtlasTree().updateModel();
				getAtlasTree().getCellEditor().stopCellEditing();
			}
		}), "split 4");

		// Button to select all maps
		add(new SmallButton(new AbstractAction(GeopublisherGUI
				.R("ImportWizard.GPA.AtlasContentSelection.AllMapsButton")) {

			@Override
			public void actionPerformed(ActionEvent e) {

				for (String mapID : externalAtlasConfig.getMapPool().keySet()) {

					// Adding dependent DPEs also
					for (DpRef<DpLayer<?, ? extends ChartStyle>> dpl : externalAtlasConfig
							.getMapPool().get(mapID).getLayers()) {
						getAtlasTree().getSelectedIds().add(dpl.getTargetId());
						getAtlasTree().getSelectedIds().add(mapID);
					}

				}

				getAtlasTree().updateModel();
				getAtlasTree().getCellEditor().stopCellEditing();
			}
		}));

		// Button to select all dpes
		add(new SmallButton(new AbstractAction(GeopublisherGUI
				.R("ImportWizard.GPA.AtlasContentSelection.AllDpeButton")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				getAtlasTree().getSelectedIds().addAll(
						externalAtlasConfig.getDataPool().keySet());
				getAtlasTree().updateModel();
				getAtlasTree().getCellEditor().stopCellEditing();
			}
		}));

		// Button to select all dpes
		add(new SmallButton(new AbstractAction(GeopublisherGUI
				.R("ImportWizard.GPA.AtlasContentSelection.NoneButton")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				getAtlasTree().getSelectedIds().clear();
				getAtlasTree().updateModel();
				getAtlasTree().getCellEditor().stopCellEditing();
			}
		}));

	}

	@Override
	protected CustomComponentListener createCustomComponentListener() {
		return new CCL();
	}

	private static final class CCL extends CustomComponentListener implements
			CellEditorListener {
		private CustomComponentNotifier notifier;
		private CheckableAtlasJTree checkableAtlasJTree;

		@Override
		public boolean accept(Component c) {
			return c instanceof CheckableAtlasJTree;
		}

		@Override
		public void startListeningTo(Component c, CustomComponentNotifier n) {
			notifier = n;
			checkableAtlasJTree = (CheckableAtlasJTree) c;
			checkableAtlasJTree.getCellEditor().addCellEditorListener(this);
		}

		@Override
		public void stopListeningTo(Component c) {
			((CheckableAtlasJTree) c).getCellEditor().removeCellEditorListener(
					this);
		}

		@Override
		public Object valueFor(Component c) {
			return ((CheckableAtlasJTree) c).getSelectedIds();
		}

		@Override
		public void editingCanceled(ChangeEvent e) {
			notifier.userInputReceived(checkableAtlasJTree, e);
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			notifier.userInputReceived(checkableAtlasJTree, e);
		}

	}

}
