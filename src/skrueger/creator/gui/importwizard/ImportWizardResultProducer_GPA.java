package skrueger.creator.gui.importwizard;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FileUtils;
import org.geotools.data.DataUtilities;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardException;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DataPool.EventTypes;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.atlas.map.MapPool;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GpUtil;

/**
 * This ResultProducer is importing from a loaded {@link AtlasConfig} instance
 * 
 */
public class ImportWizardResultProducer_GPA extends ImportWizardResultProducer {

	public ImportWizardResultProducer_GPA(AtlasConfigEditable ace) {
		super(ace);
	}

	@Override
	public Object finish(Map wizardData) throws WizardException {

		// Read stuff from the wizard map
		final AtlasConfigEditable externalAtlasConfig = (AtlasConfigEditable) wizardData
				.get(ImportWizard.IMPORT_GPA_ATLASCONFIG);

		final Set<String> ids = (Set<String>) wizardData
				.get(ImportWizard.IMPORT_GPA_IDLIST);

		/**
		 * Start the export as a DeferredWizardResult
		 */
		DeferredWizardResult result = new DeferredWizardResult(true) {

			private ResultProgressHandle progress;

			/**
			 * If the user aborts the export, we tell it to JarImportUtil
			 * instance
			 */
			@Override
			public void abort() {
				// jarImportUtil.abort();
				progress.finished(getAbortSummary());
			};

			@Override
			public void start(Map wizardData, ResultProgressHandle progress) {
				this.progress = progress;
				
				try {
					

				JPanel summaryPanel = new JPanel(new MigLayout("wrap 1"));

				// Add any languages of the external atals to our atlas
				for (String langID : externalAtlasConfig.getLanguages()) {
					if (!atlasConfigEditable.getLanguages().contains(langID)) {
						atlasConfigEditable.getLanguages().add(langID);
						Locale locale = new Locale(langID);
						summaryPanel.add(new JLabel(AtlasCreator.R(
								"ImportWizard.GPA.Summary", locale
										.getDisplayLanguage(), locale
										.getDisplayLanguage(locale))));
					}
				}

				MapPool myMapPool = atlasConfigEditable.getMapPool();
				DataPool myDataPool = atlasConfigEditable.getDataPool();

				// Halt event wars
				myMapPool.pushQuite();
				myDataPool.pushQuite();

				MapPool extMapPool = externalAtlasConfig.getMapPool();
				DataPool extDataPool = externalAtlasConfig.getDataPool();

				// TODO what about icons, images?

				// Copy the DPE and MAP instances over
				for (String id : ids) {
					if (extMapPool.containsKey(id)) {
						skrueger.atlas.map.Map eMap = extMapPool.get(id);

						try {
							// Copy the Map folder
							progress.setBusy(eMap.getTitle().toString());

							if (myMapPool.containsKey(id)) {
								// TODO Ask whether to overwrite the existing
								// map, or assign a new id to the imported map.
								summaryPanel // i8n
										.add(new JLabel(
												"<html>Map "
														+ eMap.getTitle()
														+ " already existed in this atlas. It has been replaced."));
							}

							File mapDir = new File(externalAtlasConfig
									.getHtmlDir(), eMap.getId());
							if (mapDir.exists()) {
								File newMapDir = new File(atlasConfigEditable
										.getHtmlDir(), mapDir.getName());
								FileUtils.copyDirectory(mapDir, newMapDir,
										GpUtil.BlacklistedFoldersFilter);
							}

							// Copy which additional style is
							// available/activated
							eMap.setAtlasConfig(atlasConfigEditable);
							eMap.uncache();
							myMapPool.add(eMap);
							//
							// Map<String, ArrayList<String>>
							// extAdditionalStyles = eMap
							// .getAdditionalStyles();
							//
							// Map<String, ArrayList<String>> myAdditionalStyles
							// = myMapPool
							// .get(eMap.getId()).getAdditionalStyles();

						} catch (Exception e) {
							ExceptionDialog.show(new AtlasImportException(eMap
									+ " could not be imported", e)); // i8n
						}
					} else if (extDataPool.containsKey(id)) {

						DpEntry<? extends ChartStyle> eDpe = extDataPool
								.get(id);
						try {

							// Copy the DPE folder
							progress.setBusy(eDpe.getType().getLine1() + ":"
									+ eDpe.getTitle().toString());

							if (myDataPool.containsKey(id)) {
								// TODO Ask whether to overwrite the existing
								// map, or assign a new id to the imported map.
								summaryPanel // i8n
										.add(new JLabel(
												"<html>"
														+ eDpe.getType()
																.getLine1()
														+ " "
														+ eDpe.getTitle()
														+ " already existed in this atlas. It has been replaced."));
							}

							File dpeDir = DataUtilities
									.urlToFile(eDpe.getUrl()).getParentFile();
							File newDpeDir = new File(atlasConfigEditable
									.getDataDir(), eDpe.getDataDirname());
							FileUtils.copyDirectory(dpeDir, newDpeDir,
									GpUtil.BlacklistedFoldersFilter);

							eDpe.setAtlasConfig(atlasConfigEditable);
							eDpe.uncache();
							myDataPool.add(eDpe);

							// System.out.println("copy " + eDpe.getId());

						} catch (Exception e) {
							ExceptionDialog.show(new AtlasImportException(eDpe
									+ " could not be imported", e)); // i8n
						}
					}
				}

				// Update the GUI now
				myDataPool.popQuite(EventTypes.addDpe);
				myMapPool
				.popQuite(new PropertyChangeEvent(myMapPool,
						skrueger.atlas.map.MapPool.EventTypes.addMap
						.toString(), null, null));

				externalAtlasConfig.uncache();

				summaryPanel.add(new JLabel(AtlasCreator
						.R("ImportWizard.ImportWasSuccessfull")));

				Summary summary = Summary.create(new JScrollPane(summaryPanel),
						"ok");

				progress.finished(summary);
				} catch (Exception e) {
					progress.finished(Summary.create(new JScrollPane(getErrorPanel(e)),
						"error"));
				}
			}

		};

		return result;
	}
}
