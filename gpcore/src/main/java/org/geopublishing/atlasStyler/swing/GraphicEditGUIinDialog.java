/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.SwingUtil;
import skrueger.swing.CancelButton;
import skrueger.swing.OkButton;

import com.vividsolutions.jts.geom.Polygon;

public class GraphicEditGUIinDialog extends JDialog {
	protected Logger LOGGER = ASUtil.createLogger(this);

	

	private JPanel jContentPane = null;

	private JPanel jPanel1 = null;

	private JButton jButtonOk = null;

	private GraphicEditGUI_Mig jPanelEditGUI;

	private JButton jButtonCancel = null;

	private final Fill fill;

	/** The graphic fill that this dialog deals with **/
	private final Graphic graphicFill;

	/** Backup fill of the graphic that this dialog deals with **/
	private final Graphic backupGraphicFill;

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
				cancel();
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

	public GraphicEditGUIinDialog(Component owner, final Fill fill) {
		super(SwingUtil.getParentWindow(owner), AtlasStyler
				.R("GraphicEditGUIinDialog.Title"));

		this.fill = fill;
		graphicFill = fill.getGraphicFill();
		backupGraphicFill = StylingUtil.clone(graphicFill);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				// cancel();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}

		});

		pack();
		SwingUtil.centerFrameOnScreenRandom(this);
		setModal(true);
	}

	protected void cancel() {
		fill.setGraphicFill(backupGraphicFill);
		// firePropertyChange(AbstractEditGUI.PROPERTY_UPDATED, null, null);
		dispose();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.fill = GridBagConstraints.NONE;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJPanelEditGUI(), gridBagConstraints);
			jContentPane.add(getJPanelButtons(), gridBagConstraints1);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelEditGUI() {
		if (jPanelEditGUI == null) {

			jPanelEditGUI = new GraphicEditGUI_Mig(graphicFill, FeatureUtil
					.createFeatureType(Polygon.class));

			// Update the Preview if the Icon is changed ... will change
			// with clone?!
			jPanelEditGUI
					.addPropertyChangeListener(new PropertyChangeListener() {

						public void propertyChange(PropertyChangeEvent evt) {
							if (evt.getPropertyName().equals(
									AbstractEditGUI.PROPERTY_UPDATED)) {
								try {
									// LOGGER.info("Our graphic fill now has "
									// + fill.getGraphicFill()
									// .getExternalGraphics()[0]
									// .getLocation());
									fill.setGraphicFill(graphicFill);
									// LOGGER.info("Our graphic fill now has "
									// + fill.getGraphicFill()
									// .getExternalGraphics()[0]
									// .getLocation());
								} catch (Exception e) {
									LOGGER.error(
											"fill.setGraphicFill(graphicFill)",
											e);
								}

								GraphicEditGUIinDialog.this.firePropertyChange(
										AbstractEditGUI.PROPERTY_UPDATED, null,
										null);
							}
						}

					});
		}
		return jPanelEditGUI;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelButtons() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new FlowLayout(FlowLayout.RIGHT));
			jPanel1.add(getJButtonOk());
			jPanel1.add(getJButtonCancel());
		}
		return jPanel1;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new OkButton();
			jButtonOk.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					dispose();
				}

			});
		}
		return jButtonOk;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new CancelButton();
			jButtonCancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					cancel();
				}

			});
		}
		return jButtonCancel;
	}

	@Override
	public void dispose() {

		firePropertyChange(AbstractEditGUI.PROPERTY_UPDATED, null, null);

		super.dispose();

		// Removing all listeners when the dialog is disposed
		for (PropertyChangeListener pcl : getPropertyChangeListeners()) {
			removePropertyChangeListener(pcl);
		}
	}

}
