/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
/** 
 Copyright 2009 Stefan Alfons Tzeggai 

 atlas-framework - This file is part of the Atlas Framework

 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA

 Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der GNU Lesser General Public License, wie von der Free Software Foundation veröffentlicht, weiterverteilen und/oder modifizieren; entweder gemäß Version 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version.
 Diese Bibliothek wird in der Hoffnung weiterverbreitet, daß sie nützlich sein wird, jedoch OHNE IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
 Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
 **/

package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;

import de.schmitzm.swing.OkButton;
import de.schmitzm.swing.SwingUtil;

/**
 * A {@link JDialog} solely showing a given picture
 * 
 */
public class PicturePopupDialog extends javax.swing.JDialog {

	private final AtlasConfig atlasConfig;

	HTMLInfoPaneInterface htmlInfoJPane;
	OkButton okButton;
	URL url;
	File tmp;

	public PicturePopupDialog(Component parentGUI, AtlasConfig atlasConfig,
			URL url) throws IOException {
		super(SwingUtil.getParentWindow(parentGUI), ModalityType.MODELESS);

		this.tmp=File.createTempFile("picture", ".html");
		this.url = url;
		this.atlasConfig = atlasConfig;

		initGUI();
	}
	
	public Dimension getImageDimensions(URL url){
		BufferedImage img = null;
		try {
		    img = ImageIO.read(url);
		} catch (IOException e) {
		}
		Dimension dim = new Dimension(img.getWidth()+60,img.getHeight()+100);
		return dim;
		
	}

	public File getTempHtmlFile(URL url) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write("<html><img src=\"" + url + "\"></html>");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp;
	}

	private void initGUI() throws MalformedURLException {
		Dimension dialogSize = getImageDimensions(url);
		setTitle(atlasConfig.getTitle().toString());

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new MigLayout("fill"));
		contentPane.add(getHtmlInfoJPane(), "grow, push, wrap");
		contentPane.add(getOkButton(), "tag ok");
		setContentPane(contentPane);

		setSize(dialogSize);
		SwingUtil.centerFrameOnScreen(this);

		setVisible(true);

	}

	// Pressing ESC disposes the Dialog
	KeyAdapter keyEscDispose = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_ESCAPE) {
				dispose();
			}
		}
	};

	public JComponent getHtmlInfoJPane() throws MalformedURLException {
		if (htmlInfoJPane == null) {
			htmlInfoJPane = GpCoreUtil.createHTMLInfoPane(getTempHtmlFile(url).toURI().toURL(), atlasConfig);
		}
		return htmlInfoJPane.getComponent();
	}

	public OkButton getOkButton() {
		if (okButton == null) {
			okButton = new OkButton();
			okButton.addKeyListener(keyEscDispose);
			okButton.requestFocus();
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					tmp.delete();
					dispose();
				}

			});
			// okButton.setText(AtlasViewer.R("HtmlBrowserWindow.button.close"));
		}

		return okButton;
	}

	/**
	 * Since the registerKeyboardAction() method is part of the JComponent class
	 * definition, you must define the Escape keystroke and register the
	 * keyboard action with a JComponent, not with a JDialog. The JRootPane for
	 * the JDialog serves as an excellent choice to associate the registration,
	 * as this will always be visible. If you override the protected
	 * createRootPane() method of JDialog, you can return your custom JRootPane
	 * with the keystroke enabled:
	 */
	@Override
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

}
