package skrueger.creator.dp;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.geotools.data.DataUtilities;

import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.atlas.swing.AtlasSwingWorker;
import skrueger.creator.AtlasCreator;
import skrueger.creator.exceptions.AtlasImportCancelledException;
import skrueger.i8n.Translation;
import skrueger.swing.TranslationAskJDialog;
import skrueger.swing.TranslationEditJPanel;

public class DpeImportUtil {

	public static void askTranslationsBeforeCopy(DpEntry dpe, Component owner) {

		// ****************************************************************************
		// Edit the Title, Description and Keywords modally. If the user
		// cancels here, we don't even copy stuff over...
		// ****************************************************************************
		final List<String> langs = dpe.getAc().getLanguages();
		dpe.setTitle(new Translation(langs, dpe.getFilename()));
		dpe.setDesc(new Translation());
		dpe.setKeywords(new Translation());

		TranslationAskJDialog translationAskJDialog = new TranslationAskJDialog(
				owner, new TranslationEditJPanel(AtlasCreator
						.R("EditDPEDialog.TranslateTitle"), dpe.getTitle(),
						langs), new TranslationEditJPanel(AtlasCreator
						.R("EditDPEDialog.TranslateDescription"),
						dpe.getDesc(), langs), new TranslationEditJPanel(
						AtlasCreator.R("EditDPEDialog.TranslateKeywords"), dpe
								.getKeywords(), langs));

		translationAskJDialog.setModal(true);
		translationAskJDialog.setVisible(true);

		if (translationAskJDialog.isCancelled())
			throw new AtlasImportCancelledException();
	}

	public static void copyFilesWithOrWithoutGUI(
			final DpEditableInterface dped, final URL sourceUrl,
			final Component owner, final File targetDir)
			throws AtlasImportException {
		// Creating a dialog to show while copying data

		final Boolean guiInteraction = owner != null;

		if (guiInteraction) {
			final DpEntry dpe = (DpEntry) dped;

			DpeImportUtil.askTranslationsBeforeCopy(dpe, owner);

			final AtlasStatusDialog atlasStatusDialog = new AtlasStatusDialog(
					owner, AtlasViewer.R("AtlasTask.jDialogTitle.please_wait"),
					AtlasCreator.R("ImportingDPE.StatusMessage", dpe.getTitle()
							.toString()));

			AtlasSwingWorker<Exception> copyWorker = new AtlasSwingWorker<Exception>(
					atlasStatusDialog) {

				@Override
				protected Exception doInBackground() throws Exception {
					try {
						targetDir.mkdirs();
						
						AVUtil.copyHTMLInfoFiles(statusDialog, DataUtilities.urlToFile(sourceUrl), dpe.getAc(), targetDir, null);

						if (!targetDir.exists())
							throw new IOException("Couldn't create "
									+ targetDir.getAbsolutePath());
						
						dped.copyFiles(sourceUrl, owner, targetDir,
								atlasStatusDialog);
					} catch (Exception e) {
						return e;
					}
					return null;
				}
			};

			Exception execEx;
			try {
				execEx = copyWorker.executeModal();
			} catch (Exception e) {
				execEx = e;
			}

			if (execEx != null) {
				if (execEx instanceof AtlasImportException)
					throw (AtlasImportException) execEx;
				else
					throw new AtlasImportException(execEx);
			}

		} else {
			// We are not doing GUI
			try {
				dped.copyFiles(sourceUrl, owner, targetDir, null);
			} catch (Exception e) {
				if (e instanceof AtlasImportException)
					throw (AtlasImportException) e;
				else
					throw new AtlasImportException(e);
			}
		}

	}

	public static void copyFilesWithOrWithoutGUI(DpLayerRasterPyramidEd dped,
			File sourceFile, Component owner, File targetDir) {
		copyFilesWithOrWithoutGUI(dped, DataUtilities.fileToURL(sourceFile),
				owner, targetDir);
	}

}
