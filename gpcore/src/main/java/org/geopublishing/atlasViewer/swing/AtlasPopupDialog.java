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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.AtlasConfig;

import schmitzm.swing.SwingUtil;
import skrueger.swing.OkButton;

/**
 * A {@link JDialog} showing some HTML-Info.
 * 
 * @author Stefan A. Krueger
 */
public class AtlasPopupDialog extends javax.swing.JDialog {

	private final AtlasConfig atlasConfig;

	JLabel titleJLabel;
	JLabel logoJLabel;
	HTMLInfoJPane htmlInfoJPane;
	OkButton okButton;

	/** Creates new form AtlasAboutDialog2 */
	public AtlasPopupDialog(Component parentGUI, boolean modal,
			AtlasConfig atlasConfig) {
		super(SwingUtil.getParentWindow(parentGUI),
				ModalityType.MODELESS);

		this.atlasConfig = atlasConfig;

		initGUI();
	}

	private void initGUI() {
		// setExtendedState(Frame.MAXIMIZED_BOTH);
		Dimension fullScreen = getToolkit().getScreenSize();
		Dimension dialogSize = new Dimension((int) (fullScreen.width * 0.9),
				(int) (fullScreen.height * 0.85));

		setTitle(atlasConfig.getTitle().toString());

		JPanel contentPane = new JPanel(new MigLayout("wrap 2, fillx, w "
				+ (dialogSize.width - 5) + "!"));

		contentPane.add(getTitleJLabel(), "growx, push");
		contentPane.add(getLogoJLabel(), "growy, right");
		contentPane.add(new JScrollPane(getHtmlInfoJPane()),
				"span 2, grow, pushy 200");
		contentPane.add(getOkButton(), "tag ok, span 2");

		setContentPane(contentPane);

		// try {
		// Thread.sleep(300);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		setSize(dialogSize);
		// pack();
		SwingUtil.centerFrameOnScreen(this);
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

	public JLabel getTitleJLabel() {
		if (titleJLabel == null) {
			titleJLabel = new JLabel("<html><h1>" + atlasConfig.getTitle()
					+ "</h2></html>");
		}

		return titleJLabel;
	}

	public JLabel getLogoJLabel() {
		if (logoJLabel == null) {
			logoJLabel = new JLabel();

			if (atlasConfig.getIconURL() != null)
				logoJLabel.setIcon(new ImageIcon(atlasConfig.getIconURL()));
			logoJLabel.setText(null);

		}
		return logoJLabel;
	}

	public HTMLInfoJPane getHtmlInfoJPane() {
		if (htmlInfoJPane == null) {
			htmlInfoJPane = new HTMLInfoJPane(atlasConfig.getPopupHTMLURL(),
					atlasConfig);
			// htmlInfoJPane.setPreferredSize(new Dimension(500, 320));
		}
		return htmlInfoJPane;
	}

	public OkButton getOkButton() {
		if (okButton == null) {
			okButton = new OkButton();
			okButton.addKeyListener(keyEscDispose);
			okButton.requestFocus();
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
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

			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

}
