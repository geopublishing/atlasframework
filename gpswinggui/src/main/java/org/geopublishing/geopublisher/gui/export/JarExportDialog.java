package org.geopublishing.geopublisher.gui.export;
/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Tzeggai.
// * 
// * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
// * http://www.geopublishing.org
// * 
// * Geopublisher is part of the Geopublishing Framework hosted at:
// * http://wald.intevation.org/projects/atlas-framework/
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License (license.txt)
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// * or try this link: http://www.gnu.org/licenses/gpl.html
// * 
// * Contributors:
// *     Stefan A. Tzeggai - initial API and implementation
// ******************************************************************************/
///** 
// Copyright 2008 Stefan Alfons Kueger 
// 
// atlas-framework - This file is part of the Atlas Framework
//
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
//
// Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der GNU Lesser General Public License, wie von der Free Software Foundation veröffentlicht, weiterverteilen und/oder modifizieren; entweder gemäß Version 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version.
// Diese Bibliothek wird in der Hoffnung weiterverbreitet, daß sie nützlich sein wird, jedoch OHNE IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
// Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
// **/
//package skrueger.creator.gui.export;
//
//import java.awt.Component;
//import java.awt.Container;
//import java.awt.TextField;
//import java.awt.event.ActionEvent;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.File;
//import java.io.IOException;
//import java.util.concurrent.ExecutionException;
//
//import javax.swing.AbstractAction;
//import javax.swing.JCheckBox;
//import javax.swing.JDialog;
//import javax.swing.JFileChooser;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.WindowConstants;
//
//import net.miginfocom.swing.MigLayout;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.log4j.Logger;
//import org.geotools.swing.ExceptionDialog;
//import org.netbeans.spi.wizard.ResultProgressHandle;
//
//import schmitzm.swing.SwingUtil;
//import skrueger.atlas.AVUtil;
//import skrueger.atlas.AVUtil.OSfamiliy;
//import org.geopublishing.atlasViewer.swing.internal.AtlasExportTask;
//import skrueger.atlas.internal.ProgressListener;
//import skrueger.creator.AtlasConfigEditable;
//import skrueger.creator.AtlasCreator;
//import skrueger.creator.GPProps;
//import skrueger.creator.export.AtlasExportCancelledException;
//import skrueger.creator.export.JarExportUtil;
//import skrueger.creator.gui.EditAtlasParamsDialog;
//import skrueger.swing.CancelButton;
//import skrueger.swing.OkButton;
//
///**
// * This GUI allows the user to define some export parameters
// * 
// * 
// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
// * 
// */
//public class JarExportDialog {
//	static private final Logger LOGGER = Logger
//			.getLogger(JarExportDialog.class);
//
//	/** Shall all needed libraries be exported and signed? * */
//	private static boolean exportAllLibs;
//
//	private static boolean signJARs;
//
//	protected static boolean cancel = false;
//
//	/**
//	 * Interacts with the user to export the Atlas as a collection of JARs
//	 * 
//	 * @param ace
//	 *            {@link AtlasConfigEditable} to export
//	 * 
//	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	 *         Tzeggai</a>
//	 */
//
//	public static void export(Component owner, final AtlasConfigEditable ace) {
//
//		while (!checkForRequiredMetadata(owner, ace)) {
//			EditAtlasParamsDialog editAtlasParamsDialog = new EditAtlasParamsDialog(
//					owner, ace);
//			editAtlasParamsDialog.setVisible(true);
//			if (editAtlasParamsDialog.isCancelled())
//				return;
//		}
//
//		// Increase the Version by 0,01 every time we export...
//		ace.setAtlasversion(ace.getAtlasversion() + .01f);
////
//		final JFileChooser dc = new JFileChooser(new File(GPProps.get(
//				GPProps.Keys.LastExportFolder, "")));
//		dc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		dc.setAcceptAllFileFilterUsed(false);
//
//		dc.setDialogTitle(AtlasCreator.R("Export.Dialog.WhereTo"));
//		dc.setMultiSelectionEnabled(false);
//		if ((dc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION)
//				|| dc.getSelectedFile() == null) {
//			return;
//		}
//
//		final File exportJWSandDISKdir = dc.getSelectedFile();
//
//		if (exportJWSandDISKdir == null)
//			return;
//
//		GPProps.set(GPProps.Keys.LastExportFolder, exportJWSandDISKdir
//				.getAbsolutePath());
//
//		/**
//		 * If the sub-folders DISK or JWS exists and is not, offer to delete it!
//		 */
//		File DISKdir = new File(exportJWSandDISKdir, "DISK");
//		File JWSdir = new File(exportJWSandDISKdir, "JWS");
//
//		if (DISKdir.exists() || JWSdir.exists()) {
//			// DISK and JWS folders already exist. OK to overwrite them?
//
//			if (!AVUtil.askYesNo(owner, AtlasCreator
//					.R("Export.Dialog.ConfimDelete")))
//				return;
//
//			try {
//				FileUtils.deleteDirectory(DISKdir);
//				FileUtils.deleteDirectory(JWSdir);
//			} catch (IOException e) {
//				ExceptionDialog.show(owner, e);
//			}
//		}
//		DISKdir.mkdirs();
//		JWSdir.mkdirs();
//
//		// ****************************************************************************
//		// Asking the user for the Base URL of the JNLP file when deployed
//		// ****************************************************************************
//		String jnlpLocation = GPProps.get(GPProps.Keys.jnlpURL,
//				"http://localhost/atlas");
//		final JDialog exportDialog = new JDialog(SwingUtil
//				.getParentWindow(owner));
//
//		exportDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//		exportDialog.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosing(WindowEvent e) {
//				super.windowClosing(e);
//				cancel = true;
//				exportDialog.dispose();
//			}
//		});
//
//		JLabel jnlpLabel = new JLabel("JNLP URL"); //i8n
//		TextField jnlpTextField = new TextField(jnlpLocation);
//
//		JCheckBox exportLibsCheckbox = new JCheckBox("export all libs"); // i8n
//		JCheckBox signJarsCheckbox = new JCheckBox("sign JARs"); // i8n
//
//		JPanel panel = new JPanel(new MigLayout("fillx, debug",
//				"[right]rel[grow,fill]", "[]10[]"));
//		panel.add(jnlpLabel);
//		panel.add(jnlpTextField, "span, grow");
//
//		exportLibsCheckbox.setSelected(true);
//		panel.add(exportLibsCheckbox, "gap unrelated");
//		panel.add(signJarsCheckbox, "wrap");
//
//		panel.add(new OkButton(new AbstractAction() {
//
//			public void actionPerformed(ActionEvent e) {
//				cancel = false;
//				exportDialog.dispose();
//			}
//		}), "tag ok, split 2, span 2, right");
//
//		panel.add(new CancelButton(new AbstractAction() {
//
//			public void actionPerformed(ActionEvent e) {
//				exportDialog.dispose();
//				cancel = true;
//			}
//		}), "tag cancel");
//
//		exportDialog.setContentPane(panel);
//
//		exportDialog.pack();
//		SwingUtil.centerFrameOnScreenRandom(exportDialog);
//		exportDialog.setModal(true);
//
//		exportDialog.setVisible(true);
//
//		if (cancel) {
//			return;
//		}
//
//		// ****************************************************************************
//		// Interpreting the input from the dialog
//		// ****************************************************************************
//		jnlpLocation = jnlpTextField.getText();
//		GPProps.set(GPProps.Keys.jnlpURL, jnlpLocation);
//
//		exportAllLibs = exportLibsCheckbox.isSelected();
//		signJARs = signJarsCheckbox.isSelected();
//
//		AtlasExportTask exportTask = new AtlasExportTask(owner, AtlasCreator
//				.R("ExportDialog.processWindowTitle.Exporting")) {
//
//			@Override
//			protected Boolean doInBackground() throws Exception {
//				try {
//					ProgressListener pl = new ProgressListener() {
//						public void info(String msg) {
//							publish(msg);
//						}
//					};
//
//					JarExportUtil jarExportUtil = new JarExportUtil(ace,
//							exportJWSandDISKdir, true, true, true);
//					jarExportUtil.export(new ResultProgressHandle(){
//
//						@Override
//						public void addProgressComponents(Container panel) {
//							// TODO Auto-generated method stub
//							
//						}
//
//						@Override
//						public void failed(String message,
//								boolean canNavigateBack) {
//							// TODO Auto-generated method stub
//							
//						}
//
//						@Override
//						public void finished(Object result) {
//							// TODO Auto-generated method stub
//							
//						}
//
//						@Override
//						public boolean isRunning() {
//							// TODO Auto-generated method stub
//							return false;
//						}
//
//						@Override
//						public void setBusy(String description) {
//							// TODO Auto-generated method stub
//							
//						}
//
//						@Override
//						public void setProgress(int currentStep, int totalSteps) {
//							// TODO Auto-generated method stub
//							
//						}
//
//						@Override
//						public void setProgress(String description,
//								int currentStep, int totalSteps) {
//							// TODO Auto-generated method stub
//							
//						}});
//
//				} catch (AtlasExportCancelledException e) {
//					return false;
//				} catch (Exception e) {
//					ExceptionDialog.show(owner, e);
//					return false;
//				}
//				return true;
//			}
//
//			@Override
//			protected void done() {
//				super.done();
//				Boolean result = false;
//				try {
//					result = get();
//				} catch (InterruptedException e) {
//					LOGGER.error(e);
//				} catch (ExecutionException e) {
//					LOGGER.error(e);
//				}
//				if (result) {
//
//					String exportJWSandDISKdirRepresentation = exportJWSandDISKdir
//							.getAbsolutePath();
//					if (AVUtil.getOSType() == OSfamiliy.windows) {
//						// Otherwise all Windows paths are missing the slashes
//						exportJWSandDISKdirRepresentation = exportJWSandDISKdirRepresentation
//								.replace("\\", "\\\\");
//					}
//
//					if (AVUtil.askYesNo(owner, AtlasCreator.R(
//							"Export.Dialog.Finished.Msg",
//							exportJWSandDISKdirRepresentation))) {
//						AVUtil.openOSFolder(exportJWSandDISKdir);
//					}
//				} else {
//					JOptionPane
//							.showMessageDialog(owner, AtlasCreator
//									.R("Export.Error.Msg"), AtlasCreator
//									.R("Export.Error.Title"),
//									JOptionPane.ERROR_MESSAGE);
//				}
//
//				progressWindow.complete();
//				progressWindow.dispose();
//			}
//		};
//
//		exportTask.execute();
//	}
//
//	
//
//	while (!checkForRequiredMetadata(owner, ace)) {
//		EditAtlasParamsDialog editAtlasParamsDialog = new EditAtlasParamsDialog(
//				owner, ace);
//		editAtlasParamsDialog.setVisible(true);
//		if (editAtlasParamsDialog.isCancelled())
//			return;
//	}
//	/**
//	 * @return whether all required meta-data has been supplied that is need for
//	 *         a successful export. (Title, Desc, Creator/Vendor in ALL
//	 *         languages)
//	 */
//	private static boolean checkForRequiredMetadata(Component owner,
//			AtlasConfigEditable ace) {
//		for (String lang : ace.getLanguages()) {
//			if (ace.getTitle().get(lang) == null
//					|| ace.getTitle().get(lang).equals("")
//					|| ace.getDesc().get(lang) == null
//					|| ace.getDesc().get(lang).equals("")
//					|| ace.getCreator().get(lang) == null
//					|| ace.getCreator().get(lang).equals("")) {
//				AVUtil.showMessageDialog(owner, AtlasCreator
//						.R("Export.Error.MissingMetaData"));
//				return false;
//			}
//		}
//
//		return true;
//	}
//
//}
