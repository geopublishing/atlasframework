/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Krüger.
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
// *     Stefan A. Krüger - initial API and implementation
// ******************************************************************************/
//package skrueger.creator.gui.datapool.media;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Font;
//import java.awt.event.ActionEvent;
//import java.io.File;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URL;
//
//import javax.media.CannotRealizeException;
//import javax.media.Manager;
//import javax.media.NoPlayerException;
//import javax.media.Player;
//import javax.media.Time;
//import javax.swing.AbstractAction;
//import javax.swing.JButton;
//import javax.swing.JDialog;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JTextArea;
//import javax.swing.SpringLayout;
//import javax.swing.WindowConstants;
//
//import org.apache.log4j.Logger;
//
//import schmitzm.swing.SpringUtilities;
//import schmitzm.swing.SwingUtil;
//import skrueger.atlas.exceptions.AtlasRecoverableException;
//import skrueger.atlas.gui.datapool.media.JMFVideoPanel;
//
//public class TestVideoDialog extends JDialog {
//	private static final long serialVersionUID = -2160387027240738986L;
//
//	static final Logger log = Logger.getLogger(TestVideoDialog.class);
//
//	private JPanel videoInfoPanel;
//
//	private URL url;
//
//	private JPanel infoPanel;
//
//	boolean accepted = false;
//
//	protected String filename;
//
//	/**
//	 * After the modal dialog is closed, this is true if the video was accepted
//	 * for import.
//	 * 
//	 * @return
//	 */
//	public boolean isAccepted() {
//		return accepted;
//	}
//
//	/**
//	 * Tries to show the Video. Dialog is modal, and clears all Video ressources
//	 * after display.
//	 * 
//	 * @param parentFrame
//	 * @param mediaURL
//	 *            Video to play. Cinepak + PCA save on any java system
//	 * @throws NoPlayerException
//	 * @throws CannotRealizeException
//	 * @throws IOException
//	 */
//	public TestVideoDialog(Component owner, URL mediaURL)
//			throws AtlasRecoverableException {
//		super(SwingUtil.getParentWindow(owner));
//		this.url = mediaURL;
//
//		// Window can only be closed by pressing import or reject
//		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//
//		setLayout(new BorderLayout()); // use a BorderLayout
//
//		// Use lightweight components for Swing compatibility
//		Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
//
//		// create a player to play the media specified in the URL
//		Player mediaPlayer = null;
//		try {
//			mediaPlayer = Manager.createRealizedPlayer(mediaURL);
//		} catch (Exception e) {
//			// Convert IOException, NoPlayerException, CannotRealizeException to
//			// recoverable AtlasRecoverableException
//			throw new AtlasRecoverableException("Media " + mediaURL
//					+ " could not be played.", e);
//		}
//		add(getInfoPanel(mediaPlayer), BorderLayout.NORTH);
//		add(new JMFVideoPanel(mediaPlayer), BorderLayout.CENTER);
//		add(getButtonPanel(), BorderLayout.SOUTH);
//
//		// i8nAC
//		setTitle("Import video");
//		setModal(true);
//		pack();
//		mediaPlayer.start(); // start playing the media clip
//		setVisible(true);
//		mediaPlayer.stop(); // start playing the media clip
//		mediaPlayer.deallocate();
//		mediaPlayer = null;
//
//	} // end MediaPanel constructor
//
//	/**
//	 * @return A {@link JPanel} asking the user if he wants to import the video
//	 *         or not
//	 */
//
//	private JPanel getButtonPanel() {
//		JPanel buttonPanel = new JPanel();
//		// i8nAC
//		buttonPanel.add(new JLabel("Do you want to import this video?"));
//		JButton importButton = new JButton(new AbstractAction("import") { // i8nAC
//
//					public void actionPerformed(ActionEvent e) {
//						accepted = true;
//						log.debug("Movie " + filename
//								+ " accepted for import by user.");
//						setVisible(false);
//					}
//
//				});
//		buttonPanel.add(importButton);
//
//		// Reject Button
//		// i8nAC
//		JButton rejectButton = new JButton(new AbstractAction("reject") {
//
//			public void actionPerformed(ActionEvent e) {
//				accepted = false;
//				log.debug("Movie " + filename + " rejected by user.");
//				setVisible(false);
//			}
//
//		});
//		buttonPanel.add(rejectButton);
//
//		return buttonPanel;
//	}
//
//	private JPanel getInfoPanel(Player mediaPlayer) {
//		if (infoPanel == null) {
//			infoPanel = new JPanel();
//			infoPanel.setLayout(new BorderLayout());
//
//			// i8nAC
//			JTextArea text = new JTextArea(
//					"AtlasCreator is testing if the movie is compatible with pure Java playback.\n"
//							+ "You should only import if you can see and hear it correctly now.");
//			text.setLineWrap(true);
//			text.setWrapStyleWord(true);
//			text.setBackground(new Color(0.9f, 0.9f, 0.9f));
//			text.setFont(new Font(null, Font.BOLD, 16));
//
//			infoPanel.add(text, BorderLayout.NORTH);
//			infoPanel.add(getVideoInfoPanel(mediaPlayer), BorderLayout.CENTER);
//		}
//		return infoPanel;
//	}
//
//	private JPanel getVideoInfoPanel(Player mediaPlayer) {
//		if (videoInfoPanel == null) {
//			videoInfoPanel = new JPanel(new SpringLayout());
//
//			// Dateiname
//			File mediaFile = new File(url.getFile());
//			filename = mediaFile.getName();
//			JLabel fileNameDesc = new JLabel("Dateiname"); // i8nAC
//			JLabel fileNameLabel = new JLabel(filename);
//			fileNameDesc.setLabelFor(fileNameLabel);
//			videoInfoPanel.add(fileNameDesc);
//			videoInfoPanel.add(fileNameLabel);
//
//			// Duration
//			Time duration = mediaPlayer.getDuration();
//			JLabel durationDesc = new JLabel("Duration:"); // i8nAC
//			String niceDur = String.format("%2.2f sec", duration.getSeconds());
//			JLabel durationLabel = new JLabel(niceDur);
//			durationDesc.setLabelFor(durationLabel);
//			videoInfoPanel.add(durationDesc);
//			videoInfoPanel.add(durationLabel);
//
//			// Lay out the panel.
//			SpringUtilities.makeCompactGrid(videoInfoPanel, 2, 2, // rows,
//					// cols
//					6, 6, // initX, initY
//					6, 6); // xPad, yPad
//
//		}
//		return videoInfoPanel;
//	}
//
//	public static void main(String[] args) throws MalformedURLException {
//		URL mediaURL = new URL(
//				"file:/home/stefan/Desktop/AtlasData/ausgabe.avi");
//		try {
//			TestVideoDialog dialog = new TestVideoDialog(null, mediaURL);
//			dialog.setVisible(true);
//		} catch (AtlasRecoverableException e) {
//			log.error(e);
//		}
//
//	}
//}
////
