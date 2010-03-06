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
///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Krüger.
// * 
// * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
// * http://www.geopublishing.org
// * 
// * AtlasViewer is part of the Geopublishing Framework hosted at:
// * http://wald.intevation.org/projects/atlas-framework/
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License (license.txt)
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// * or try this link: http://www.gnu.org/licenses/lgpl.html
// * 
// * Contributors:
// *     Stefan A. Krüger - initial API and implementation
// ******************************************************************************/
//package skrueger.atlas.gui.datapool.media;
//
///**
// * A JPanel the plays media from a URL
// * 
// * Based on :
// * http://www.deitel.com/articles/java_tutorials/20060422/PlayingVideowithJMF/
// */
//import java.awt.BorderLayout;
//import java.awt.Component;
//import java.io.IOException;
//import java.net.URL;
//
//import javax.media.CannotRealizeException;
//import javax.media.NoPlayerException;
//import javax.media.Player;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//
///**
// * SUCKS! Seems to have a memory leak...
// * 
// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
// * 
// */
//public class JMFVideoPanel extends JPanel {
//	// public JMFVideoPanel(URL mediaURL) throws NoPlayerException,
//	// CannotRealizeException, IOException {
//	// setLayout(new BorderLayout()); // use a BorderLayout
//	//
//	// // Use lightweight components for Swing compatibility
//	// Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
//	//
//	// // create a player to play the media specified in the URL
//	// Player mediaPlayer = Manager.createRealizedPlayer(mediaURL);
//	//
//	// // get the components for the video and the playback controls
//	// Component video = mediaPlayer.getVisualComponent();
//	// Component controls = mediaPlayer.getControlPanelComponent();
//	//
//	// if (video != null)
//	// add(video, BorderLayout.CENTER); // add video component
//	//
//	// if (controls != null)
//	// add(controls, BorderLayout.SOUTH); // add controls
//	//
//	// mediaPlayer.start(); // start playing the media clip
//	// } // end MediaPanel constructor
//	//	
//
//	/**
//	 * does not start automatically
//	 * 
//	 * @param mediaPlayer
//	 */
//	public JMFVideoPanel(Player mediaPlayer) {
//		setLayout(new BorderLayout()); // use a BorderLayout
//
//		// get the components for the video and the playback controls
//		Component video = mediaPlayer.getVisualComponent();
//		Component controls = mediaPlayer.getControlPanelComponent();
//
//		if (video != null)
//			add(video, BorderLayout.CENTER); // add video component
//
//		if (controls != null)
//			add(controls, BorderLayout.SOUTH); // add controls
//	} // end MediaPanel constructor
//
//	public static void main(String[] args) throws NoPlayerException,
//			CannotRealizeException, IOException {
//		URL mediaURL = new URL(
//				"file:/home/stefan/Desktop/AtlasData/ausgabe.avi");
//		JFrame frame = new JFrame();
//		// JMFVideoPanel videoPanel = new JMFVideoPanel(mediaURL);
//		// frame.add(videoPanel);
//		frame.pack();
//		frame.setVisible(true);
//	}
//} // end class MediaPanel
