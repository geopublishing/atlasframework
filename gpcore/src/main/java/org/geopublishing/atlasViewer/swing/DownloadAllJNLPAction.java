package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.jnlp.UnavailableServiceException;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.JNLPUtil;

import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class DownloadAllJNLPAction extends AbstractAction {

	private final AtlasViewerGUI atlasViewer;
	private final Component owner;

	public DownloadAllJNLPAction(AtlasViewerGUI atlasViewer) {
		super(GpCoreUtil.R("AtlasViewer.FileMenu.downloadAllRessources"));
		this.atlasViewer = atlasViewer;
		this.owner = atlasViewer.getJFrame();
		putValue(
				Action.LONG_DESCRIPTION,
				GpCoreUtil
						.R("AtlasViewer.FileMenu.downloadAllRessources.tooltip"));
	}

	public static Logger LOGGER = Logger.getLogger(DownloadAllJNLPAction.class);

	@Override
	public void actionPerformed(ActionEvent e) {
		// **************************************************************
		// Download all JWS resources into the local cache
		// **************************************************************

		// LOGGER.info("Action Command: downloadAllJWS");
		try {

			ArrayList<String> haveToDownload = JNLPUtil
					.countPartsToDownload(atlasViewer.getAtlasConfig()
							.getDataPool());
			if (haveToDownload.size() == 0) {
				/*
				 * Nothing to download
				 */
				atlasViewer.getAtlasMenuBar().getJWSDownloadAllMenuItem()
						.setEnabled(false);

				AVSwingUtil
						.showMessageDialog(
								atlasViewer.getJFrame(),
								GpCoreUtil
										.R("AtlasViewer.FileMenu.downloadAllRessources.AlreadyDownloaded"));

				return;

			} else {

				/*
				 * check whether terms of use have been accepted, display them
				 * if not
				 */

				if (atlasViewer.getAtlasConfig().getTermsOfUseHTMLURL() != null) {
					// atlasViewer
					// .getAtlasConfig()
					// .getProperties()
					// .getBoolean(
					// org.geopublishing.atlasViewer.AVProps.Keys.termsOfUseAccepted,
					// true)
					AtlasTermsOfUseDialog aboutWindow = new AtlasTermsOfUseDialog(
							owner, atlasViewer.getAtlasConfig(), true);
				}

				final String[] parts = haveToDownload
						.toArray(new String[haveToDownload.size()]);

				LOGGER.info("Parts length = " + parts.length);

				AtlasStatusDialog statusDialog = new AtlasStatusDialog(owner);
				statusDialog.setCancelAllowed(false); // TODO how to cancel
				// a
				// donwload?
				AtlasSwingWorker<Void> atlasSwingWorker = new AtlasSwingWorker<Void>(
						statusDialog) {

					@Override
					protected Void doInBackground() throws Exception {
						JNLPSwingUtil.loadPart(parts, statusDialog);
						return null;
					}

				};
				atlasSwingWorker.executeModalNoEx();

				AVSwingUtil
						.showMessageDialog(
								atlasViewer.getJFrame(),
								GpCoreUtil
										.R("AtlasViewer.FileMenu.downloadAllRessources.Success"));
			}
		} catch (UnavailableServiceException e1) {
			ExceptionDialog.show(atlasViewer.getJFrame(), e1);
		} catch (Throwable ee) {
			LOGGER.error("Downloading all JARs via JWS", ee);
			ExceptionDialog.show(atlasViewer.getJFrame(), ee);
		}

	}
}
