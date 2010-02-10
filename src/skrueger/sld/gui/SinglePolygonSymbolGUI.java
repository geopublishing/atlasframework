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
/**
 * Copyright 2008 Stefan Alfons Krüger
 * 
 * atlas-framework - This file is part of the Atlas Framework
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston,
 * MA 02110, USA
 * 
 * Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der
 * GNU Lesser General Public License, wie von der Free Software Foundation
 * veröffentlicht, weiterverteilen und/oder modifizieren; entweder gemäß Version
 * 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version. Diese
 * Bibliothek wird in der Hoffnung weiterverbreitet, daß sie nützlich sein wird,
 * jedoch OHNE IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der
 * MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Mehr Details
 * finden Sie in der GNU Lesser General Public License. Sie sollten eine Kopie
 * der GNU Lesser General Public License zusammen mit dieser Bibliothek erhalten
 * haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110, USA.
 */
// package skrueger.sld.gui;
//
// import java.awt.Dimension;
// import java.awt.GridBagConstraints;
// import java.awt.GridBagLayout;
// import java.awt.Insets;
// import java.awt.event.ActionEvent;
// import java.beans.PropertyChangeEvent;
// import java.beans.PropertyChangeListener;
//
// import javax.swing.AbstractAction;
// import javax.swing.BorderFactory;
// import javax.swing.ImageIcon;
// import javax.swing.JDialog;
// import javax.swing.JLabel;
// import javax.swing.JPanel;
// import javax.swing.JToggleButton;
//
// import org.apache.log4j.Logger;
//
// import schmitzm.swing.SwingUtil;
// import skrueger.Utilities;
// import skrueger.sld.RuleChangeListener;
// import skrueger.sld.SinglePointSymbolRuleList;
// import skrueger.sld.SinglePolygonSymbolRuleList;
//
// public class SinglePolygonSymbolGUI extends JPanel {
// protected Logger LOGGER = Utilities.createLogger(this);
//
// public final static String PROPERTY_STYLE_UPDATED = "STYLE UPDATED";
//
// private static final long serialVersionUID = 1L;
//
// private JLabel jLabel = null;
//
// private JPanel jPanelSymbol = null;
//
// private JToggleButton jButtonSymbol = null;
//
// private JPanel jPanelLabel = null;
//
// private final SinglePolygonSymbolRuleList singlePolygonSymbolRuleList;
//
// /**
// * This is the default constructor
// *
// * @param singleSymbolRuleList
// */
// public SinglePolygonSymbolGUI(
// SinglePolygonSymbolRuleList singleSymbolRuleList) {
// super();
// if (singleSymbolRuleList == null)
// throw new IllegalStateException("may not be null");
// this.singlePolygonSymbolRuleList = singleSymbolRuleList;
// initialize();
// }
//
// /**
// * This method initializes this
// *
// * @return void
// */
// private void initialize() {
// GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
// gridBagConstraints21.gridx = 0;
// gridBagConstraints21.gridy = 2;
// GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
// gridBagConstraints1.gridx = 0;
// gridBagConstraints1.gridwidth = 2;
// gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
// gridBagConstraints1.anchor = GridBagConstraints.NORTH;
// gridBagConstraints1.gridy = 1;
// GridBagConstraints gridBagConstraints = new GridBagConstraints();
// gridBagConstraints.gridx = 1;
// gridBagConstraints.anchor = GridBagConstraints.NORTH;
// gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
// gridBagConstraints.gridy = 0;
// jLabel = new JLabel();
// jLabel.setText("Draw all features using the same symbol.");
// this.setSize(new Dimension(435, 350));
// this.setLayout(new GridBagLayout());
// this.add(jLabel, gridBagConstraints);
// this.add(getJPanelSymbol(), gridBagConstraints1);
// this.add(getJPanelLabel(), gridBagConstraints21);
// }
//
// /**
// * This method initializes jPanel
// *
// * @return javax.swing.JPanel
// */
// private JPanel getJPanelSymbol() {
// if (jPanelSymbol == null) {
// GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
// gridBagConstraints2.gridx = 0;
// gridBagConstraints2.insets = new Insets(5, 10, 5, 10);
// gridBagConstraints2.gridy = 0;
// jPanelSymbol = new JPanel();
// jPanelSymbol.setLayout(new GridBagLayout());
// jPanelSymbol.setBorder(BorderFactory.createTitledBorder("Symbol"));
// jPanelSymbol.add(getJButtonSymbol(), gridBagConstraints2);
// jPanelSymbol.setPreferredSize(new Dimension(435 - 5, 100)); // TODO
// // CONSTANSTs
// }
// return jPanelSymbol;
// }
//
// /**
// * This method initializes jButton
// *
// * @return javax.swing.JButton
// */
// private JToggleButton getJButtonSymbol() {
// if (jButtonSymbol == null) {
// jButtonSymbol = new JToggleButton();
// jButtonSymbol.setIcon(new ImageIcon(singlePolygonSymbolRuleList
// .getSymbolImage()));
//
// /**
// * Adding a listener that will update the Button-Image when the
// * rulelist has been altered *
// */
// singlePolygonSymbolRuleList.addListener(new RuleChangeListener() {
//
// public void changed() {
// jButtonSymbol.setIcon(new ImageIcon(
// singlePolygonSymbolRuleList.getSymbolImage()));
// }
//
// });
//
// jButtonSymbol.setAction(new AbstractAction("") {
//
// private SymbolSelectorGUI symbolSelector;
//
// public void actionPerformed(ActionEvent e) {
// if (symbolSelector == null) {
// SinglePolygonSymbolRuleList clone = singlePolygonSymbolRuleList
// .clone();
// final SinglePolygonSymbolRuleList copyOfRuleList;
// copyOfRuleList = clone;
// //if (clone != null)
// // else
// // copyOfRuleList = new SinglePolygonSymbolRuleList();
//
// symbolSelector = new SymbolSelectorGUI(SwingUtil
// .getParentWindow(SinglePolygonSymbolGUI.this),
// copyOfRuleList);
//
//
// /** Listen for Apply **/
// symbolSelector
// .addPropertyChangeListener(new PropertyChangeListener() {
//
// public void propertyChange(
// PropertyChangeEvent evt) {
// if (!evt
// .getPropertyName()
// .equals(
// SymbolSelectorGUI.PROPERTY_APPLY_CHANGES))
// return;
//
// System.out
// .println("COpying all values of the working RuleList to the real rule lsit");
// SinglePointSymbolRuleList
// .copyAllValues(copyOfRuleList,
// singlePolygonSymbolRuleList);
//
// // Saving the selection
//
// firePropertyChange(
// PROPERTY_STYLE_UPDATED, null,
// null);
//
// jButtonSymbol.setIcon(new ImageIcon(
// singlePolygonSymbolRuleList
// .getSymbolImage()));
//
// }
//
// });
//						
//
// /** Listen for CLOSE**/
// symbolSelector
// .addPropertyChangeListener(new PropertyChangeListener() {
//
// public void propertyChange(
// PropertyChangeEvent evt) {
// if (evt
// .getPropertyName()
// .equals(
// SymbolSelectorGUI.PROPERTY_CLOSED)) {
// jButtonSymbol.setSelected(false);
// // Wegschmeissen?
// symbolSelector = null;
// }
// }
//
// });
// }
//
// symbolSelector
// .setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
// symbolSelector.setVisible(jButtonSymbol.isSelected());
// }
//
// });
// }
// return jButtonSymbol;
// }
//
// /**
// * This method initializes jPanel1
// *
// * @return javax.swing.JPanel
// */
// private JPanel getJPanelLabel() {
// if (jPanelLabel == null) {
// jPanelLabel = new JPanel();
// jPanelLabel.setLayout(new GridBagLayout());
// jPanelLabel.setBorder(BorderFactory.createTitledBorder("Label"));
// }
// return jPanelLabel;
// }
// }
