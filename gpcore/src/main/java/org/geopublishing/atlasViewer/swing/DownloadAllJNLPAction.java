package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.jnlp.UnavailableServiceException;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;

import schmitzm.swing.ExceptionDialog;

public class DownloadAllJNLPAction extends AbstractAction {

	private final AtlasViewerGUI atlasViewer;
	private final Component owner;

	public DownloadAllJNLPAction(AtlasViewerGUI atlasViewer) {
		super(AtlasViewerGUI.R("AtlasViewer.FileMenu.downloadAllRessources"));
		this.atlasViewer = atlasViewer;
		this.owner = atlasViewer.getJFrame();
		putValue(Action.LONG_DESCRIPTION,
				AtlasViewerGUI
						.R("lasViewer.FileMenu.downloadAllRessources.tooltip"));
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
								AtlasViewerGUI
										.R("AtlasViewer.FileMenu.downloadAllRessources.AlreadyDownloaded"));

				return;

			} else {

				// There is stuf to down load

				final String[] parts = haveToDownload
						.toArray(new String[haveToDownload.size()]);

				LOGGER.info("Parts length = " + parts.length);

				AtlasStatusDialog statusDialog = new AtlasStatusDialog(owner);
				statusDialog.setCancelAllowed(false); //TODO how to cancel a donwload?
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
								AtlasViewerGUI
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
