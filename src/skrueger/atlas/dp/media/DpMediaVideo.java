/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.dp.media;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DpEntryType;
import skrueger.i8n.Translation;

public class DpMediaVideo extends DpMedia<ChartStyle> implements ActionListener {
	static Logger LOGGER = Logger.getLogger(DpMediaVideo.class);

//	private Player mediaPlayer = null;

//	JDialog playerDialog = null;

	/**
	 * This constructor is used when loading an {@link AtlasConfig} from
	 * atlas.xml file. The AVI is already copied and
	 * {@link #setFilename(String)} is called later.
	 * 
	 * @param ac
	 *            {@link AtlasConfig} to create the {@link DpMediaVideo} in
	 */
	public DpMediaVideo(AtlasConfig ac) {
		super(ac);
		setTitle(new Translation(getAtlasConfig().getLanguages(), getFilename())); 
		setDesc(new Translation());
		setType(DpEntryType.VIDEO);
	}
//
//	@Override
//	public DpMediaVideo copy() {
//		return copyTo(new DpMediaVideo(ac));
//	}
//	
//	@Override
//	public DpMediaVideo copyTo(Object t) {
//		throw new RuntimeException("not implemented yet");
//	}

	@Override
	/*
	 * Shows the Video
	 */
	public Object show(Component parent) {

		uncache();
//
//		playerDialog = new JDialog(SwingUtil.getParentWindow(parent));
//		playerDialog
//				.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//
//		// ****************************************************************************
//		// Also when the window is closed by the Frame's X-button, we nead a
//		// cleanup
//		// ****************************************************************************
//		playerDialog.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosing(WindowEvent e) {
//				uncache();
//			}
//		});
//
//		JPanel cp = new JPanel(new BorderLayout());
//		try {
//			URL mediaURL = getLocalCopy(parent).toURI().toURL();
//			LOGGER.debug("showing now = " + mediaURL);
//
//			// Use lightweight components for Swing compatibility
//			Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
//			mediaPlayer = Manager.createRealizedPlayer(mediaURL);
//
//			// create a player to play the media specified in the URL
//			JMFVideoPanel videoPanel = new JMFVideoPanel(mediaPlayer);
//			cp.add(videoPanel, BorderLayout.CENTER);
//			final OkButton okButton = new OkButton();
//			okButton.addActionListener(this);
//			cp.add(okButton, BorderLayout.SOUTH);
//
//			playerDialog.setContentPane(cp);
//			playerDialog.setTitle(getTitle().toString());
//			playerDialog.setModal(false);
//			playerDialog.pack();
//			mediaPlayer.start(); // start playing the media clip
//			playerDialog.setVisible(true);
//		} catch (Exception e) {
//			uncache();
//			ExceptionDialog.show(parent, e);
//		} finally {
//		}

		return null;
	}

	/**
	 * Called only by the OK/Close Button of the VideoPlayer...
	 */
	public void actionPerformed(ActionEvent e) {
		uncache();
	}

	/**
	 * Clears all memory-intensive cache objects
	 */
	@Override
	public void uncache() {
		super.uncache();
		LOGGER.debug("uncaching video " + getTitle());
//
//		if (mediaPlayer != null) {
//			mediaPlayer.stop();
//			mediaPlayer.close();
//			mediaPlayer.deallocate();
//			mediaPlayer = null;
//		}
//		if (playerDialog != null) {
//			playerDialog.setVisible(false);
//			playerDialog.dispose();
//			playerDialog = null;
//		}
	}

	public void exportWithGUI(Component owner) {
		// TODO exportWithGUI PDF
		throw new RuntimeException("Sorry, export of VIDEO with GUI is not yet implemented.");
	}



}
