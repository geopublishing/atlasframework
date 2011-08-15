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
package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.concurrent.CancellationException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.rulesLists.UniqueValuesRulesListInterface;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;

import de.schmitzm.swing.CancellableDialogAdapter;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.ThinButton;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class UniqueValuesAddGUI<VALUETYPE> extends CancellableDialogAdapter {
	private static final Logger log = Logger.getLogger(UniqueValuesAddGUI.class);

	private JPanel jContentPane = null;

	private JPanel jPanel = null;

	private JPanel jPanelButtons = null;

	private JPanel jPanel2 = null;

	private JTextField jTextField = null;

	private ThinButton jButtonAddToList = null;

	private JLabel jLabelSelectTheValueToAdd = null;

	private JScrollPane jScrollPane = null;

	private JList jListValues = null;

	private final UniqueValuesRulesListInterface<VALUETYPE> rulesList;

	/**
	 * @param owner
	 * @param featureSource_polygon
	 */
	public UniqueValuesAddGUI(Component owner, final UniqueValuesRulesListInterface<VALUETYPE> rulesList) {
		super(owner);
		this.rulesList = rulesList;
		initialize();

		String title = ASUtil.R("UniqueValuesRuleList.AddAllValues.SearchingMsg");
		final AtlasStatusDialog statusDialog = new AtlasStatusDialog(owner, title, title);

		AtlasSwingWorker<Set<VALUETYPE>> findUniques = new AtlasSwingWorker<Set<VALUETYPE>>(statusDialog) {

			@Override
			protected Set<VALUETYPE> doInBackground() throws Exception {
				return rulesList.getAllUniqueValuesThatAreNotYetIncluded();
			}

		};
		try {
			Set<VALUETYPE> uniqueValues = findUniques.executeModal();

			if (uniqueValues.size() == 0)
				AVSwingUtil.showMessageDialog(UniqueValuesAddGUI.this, ASUtil.R("UniqueValues.NothingToAdd"));

			DefaultListModel defaultListModel = new DefaultListModel();
			for (VALUETYPE uv : uniqueValues) {
				defaultListModel.addElement(uv);
			}
			getJListValues().setModel(defaultListModel);

			SwingUtil.setRelativeFramePosition(UniqueValuesAddGUI.this, owner, SwingUtil.BOUNDS_OUTER,
					SwingUtil.NORTHEAST);
			// setVisible(true);

		} catch (CancellationException e) {
			dispose();
			return;
		} catch (Exception e) {
			ExceptionDialog.show(e);
			dispose();
			return;
		}
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setTitle(ASUtil.R("UniqueValuesAddGUI.DialogTitle"));
		this.setModal(true);
		pack();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridwidth = 2;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints1.fill = GridBagConstraints.NONE;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridy = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJPanel(), gridBagConstraints);
			jContentPane.add(getJPanelButtons(), gridBagConstraints1);
			jContentPane.add(getJPanel2(), gridBagConstraints2);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.BOTH;
			gridBagConstraints9.gridy = 1;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.weighty = 1.0;
			gridBagConstraints9.gridx = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.WEST;
			gridBagConstraints8.insets = new Insets(5, 1, 1, 5);
			gridBagConstraints8.gridy = 0;
			jLabelSelectTheValueToAdd = new JLabel(ASUtil.R("UniqueValuesAddGUI.SelectTheValuesMsg"));
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(jLabelSelectTheValueToAdd, gridBagConstraints8);
			jPanel.add(getJScrollPane(), gridBagConstraints9);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.insets = new Insets(5, 10, 5, 5);
			gridBagConstraints7.anchor = GridBagConstraints.NORTH;
			gridBagConstraints7.gridy = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.insets = new Insets(5, 10, 5, 5);
			gridBagConstraints6.anchor = GridBagConstraints.NORTH;
			gridBagConstraints6.gridy = 0;
			jPanelButtons = new JPanel();
			jPanelButtons.setLayout(new GridBagLayout());
			jPanelButtons.add(getOkButton(), gridBagConstraints6);
			jPanelButtons.add(getCancelButton(), gridBagConstraints7);
		}
		return jPanelButtons;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 2;
			gridBagConstraints5.insets = new Insets(5, 0, 5, 5);
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.gridy = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridy = 1;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.gridx = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridwidth = 3;
			gridBagConstraints3.gridy = 0;
			JLabel addNewValueToList = new JLabel(ASUtil.R("UniqueValuesAddGUI.AddNewValueToList.Msg"));
			jPanel2 = new JPanel();
			jPanel2.setToolTipText(ASUtil.R("UniqueValuesAddGUI.AddNewValueToList.TT"));
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setBorder(BorderFactory.createTitledBorder(ASUtil
					.R("UniqueValuesAddGUI.AddNewValueToList.BorderTitle")));
			jPanel2.add(addNewValueToList, gridBagConstraints3);
			jPanel2.add(getJTextField(), gridBagConstraints4);
			jPanel2.add(getJButtonAddToList(), gridBagConstraints5);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
		}
		return jTextField;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonAddToList() {
		if (jButtonAddToList == null) {
			jButtonAddToList = new ThinButton(new AbstractAction(
					ASUtil.R("UniqueValuesAddGUI.AddNewValueToList.AddButton")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					String text = getJTextField().getText();

					Object val = text;

					((DefaultListModel) getJListValues().getModel()).addElement(val);
				}

			});

			jButtonAddToList.setEnabled(false);

			getJTextField().addCaretListener(new CaretListener() {

				@Override
				public void caretUpdate(CaretEvent e) {
					String str = getJTextField().getText();

					jButtonAddToList
							.setEnabled(((str != null) && (!str.trim().equals("")) && (!((DefaultListModel) getJListValues()
									.getModel()).contains(str))));
				}

			});
		}
		return jButtonAddToList;
	}

	// /**
	// * This method initializes jButton
	// *
	// * @return javax.swing.JButton
	// */
	// private JButton getJButtonOk() {
	// if (jButtonOk == null) {
	// jButtonOk = new OkButton();
	//
	// jButtonOk.addActionListener(new ActionListener() {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	//
	// UniqueValuesAddGUI.this.dispose();
	// }
	//
	// });
	//
	// getJListValues().addListSelectionListener(
	// new ListSelectionListener() {
	//
	// @Override
	// public void valueChanged(ListSelectionEvent e) {
	// jButtonOk.setEnabled((getJListValues()
	// .getSelectedValues().length > 0));
	// }
	//
	// });
	//
	// jButtonOk.setEnabled(false);
	// }
	// return jButtonOk;
	// }

	// /**
	// * This method initializes jButton
	// *
	// * @return javax.swing.JButton
	// */
	// private JButton getJButtonCancel() {
	// if (jButtonCancel == null) {
	// jButtonCancel = new CancelButton();
	// jButtonCancel.addActionListener(new ActionListener() {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// UniqueValuesAddGUI.this.dispose();
	// }
	//
	// });
	// }
	// return jButtonCancel;
	// }

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJListValues());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jList
	 * 
	 * @return javax.swing.JList
	 */
	private JList getJListValues() {
		if (jListValues == null) {
			jListValues = new JList();
		}
		return jListValues;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean okClose() {
		rulesList.pushQuite();
		try {

			for (Object obj : (Object[]) getJListValues().getSelectedValues()) {

				try {
					// UGLY UGLY
					try {
						rulesList.addUniqueValue((VALUETYPE) obj);
					} catch (Exception e) {
						try {
							rulesList.addUniqueValue((VALUETYPE) (Double.valueOf(obj.toString())));
						} catch (Exception ee) {
							try {

								rulesList.addUniqueValue((VALUETYPE) (Integer.valueOf(obj.toString())));
							} catch (Exception eee) {
								rulesList.addUniqueValue((VALUETYPE) (Byte.valueOf(obj.toString())));
							}
						}
					}
				} catch (RuntimeException e2) {
					ExceptionDialog.show(SwingUtil.getParentWindow(UniqueValuesAddGUI.this), e2);
				}
			}
		} finally {
			rulesList.popQuite(new RuleChangedEvent("Manually added more unique values...", rulesList));
		}
		return super.okClose();
	}

}
