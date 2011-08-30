package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.ArrayUtils;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasViewer.swing.Icons;

import de.schmitzm.swing.ButtonGroup;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SmallButton;
import de.schmitzm.swing.SwingUtil;

/**
 * This {@link JPanel} contains the {@link RulesListTable} and some
 * labels/buttons arround it.
 */
public class RulesListsListTablePanel extends JPanel {

	private final AtlasStyler atlasStyler;

	private SmallButton addButton;

	private SmallButton gdalInfoButton;

	private SmallButton removeButton;

	private SmallButton jButtonLayerDown;

	private SmallButton jButtonLayerUp;

	private RulesListTable rulesListTable;

	private final StylerDialog asd;

	JLabel popupMenuExplanationJLabel = new JLabel(
			ASUtil.R("RulesListsListTablePanel.Explanation"));

	private final PropertyChangeListener updatePreviewScaleLabelListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updateScaleInPreviewJLabel();
		}
	};

	private final JLabel scaleInPreviewValueJLabel = new JLabel();

	private JPanel modeButtons;

	private SmallButton duplicateButton;

	private JPanel bandModeSelectionPanel;

	public RulesListsListTablePanel(final StylerDialog asd) {
		super(new MigLayout("", "grow", "[grow][]"));
		this.asd = asd;
		this.atlasStyler = asd.getAtlasStyler();

		// /**
		// * Beim Raster-Atlas-Styler kann u.U. ausgewählt werden, ob ein
		// einzelen Band (von vielen) oder ein
		// * RGB/Fehlfarbenbild gestyled werden soll.
		// */
		// if (atlasStyler instanceof AtlasStylerRaster) {
		// AtlasStylerRaster asr = (AtlasStylerRaster) atlasStyler;
		// if (asr.getStyledRaster().getBandCount() > 1) {
		// // Es gibt mehr als ein Band => diese Optim wird gerendert
		// add(getBandModeCombobox(asr), "growy, sgx, wrap");
		// }
		// }

		add(getModeButtons(asd), "growy, sgx, wrap");
		add(popupMenuExplanationJLabel, "growy, sgx, wrap");

		add(new JScrollPane(getRulesListTable()), "growy, sgx, wrap");
		getRulesListTable().getSelectionModel().clearSelection();

		if (asd.getPreviewMapPane() != null) {
			add(scaleInPreviewValueJLabel, "growy, wrap");
			asd.addScaleChangeListener(updatePreviewScaleLabelListener);
			updateScaleInPreviewJLabel();
		}

		add(getAddButton(), "split 6, align left");
		add(getDuplicateButton(), "align left");
		add(getRemoveButton(), "align left, gapx");
		if (atlasStyler instanceof AtlasStylerRaster)
			add(getGdalInfoButton(), "align center, gapx");
		add(new JLabel(), "growx");
		add(getUpButton(), "align right");
		add(getDownButton(), "align right");

		// Automatically select the first RulesList
		if (getRulesListTable().getModel().getRowCount() > 0) {
			getRulesListTable().getSelectionModel().addSelectionInterval(0, 0);
		}
	}

	private Component getGdalInfoButton() {
		if (gdalInfoButton == null) {

			gdalInfoButton = new SmallButton(new AbstractAction("gdal") {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					GdalInfo2RulesListDialog gdalInfo2RulesListDialog = new GdalInfo2RulesListDialog(
							RulesListsListTablePanel.this,
							(AtlasStylerRaster) atlasStyler);
					gdalInfo2RulesListDialog.setModal(true);
					gdalInfo2RulesListDialog.setVisible(true);
				}
			});

		}
		return gdalInfoButton;
	}

	private JPanel getModeButtons(final StylerDialog asd) {
		if (modeButtons == null) {

			modeButtons = new JPanel(new MigLayout());

			modeButtons.add(new JLabel(ASUtil.R("UserMode.Label")));

			final JRadioButton easyButton = new JRadioButton(
					new AbstractAction(ASUtil.R("UserMode.Easy.Label")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							asd.setEasy(true);
							popupMenuExplanationJLabel.setVisible(false);
							getRulesListTable().updateColumnsLook();
							getRulesListTable().updateColumnsLook();
							// getUpButton().setVisible(false);
							// getDownButton().setVisible(false);
							scaleInPreviewValueJLabel.setVisible(false);
						}
					});

			modeButtons.add(easyButton);
			final JRadioButton expertButton = new JRadioButton(
					new AbstractAction(ASUtil.R("UserMode.Expert.Label")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							asd.setEasy(false);
							popupMenuExplanationJLabel.setVisible(true);
							getRulesListTable().updateColumnsLook();
							getRulesListTable().updateColumnsLook();
							scaleInPreviewValueJLabel.setVisible(true);
							// getUpButton().setVisible(true);
							// getDownButton().setVisible(true);
						}
					});
			modeButtons.add(expertButton);

			ButtonGroup bg = new ButtonGroup();
			bg.add(easyButton);
			bg.add(expertButton);

			easyButton.doClick();
		}

		return modeButtons;
	}

	private void updateScaleInPreviewJLabel() {
		if (asd.getPreviewMapPane() == null)
			return;

		final String formated = NumberFormat.getIntegerInstance().format(
				asd.getPreviewMapPane().getScaleDenominator());

		String label = ASUtil.R(
				"RulesListsListTablePanel.OGCScaleDenominatorInPreview",
				formated);

		scaleInPreviewValueJLabel.setText(label);
	}

	private JButton getAddButton() {
		if (addButton == null) {
			addButton = new SmallButton(new AbstractAction(
					ASUtil.R("RulesListsList.Action.AddRulesList")) {

				@Override
				public void actionPerformed(ActionEvent e) {

					AddRulesListDialog addRulesListDialog = new AddRulesListDialog(
							RulesListsListTablePanel.this, atlasStyler);
					addRulesListDialog.setVisible(true);
				}

			});
		}
		return addButton;
	}

	private JButton getDownButton() {
		if (jButtonLayerDown == null) {
			jButtonLayerDown = new SmallButton(new AbstractAction("",
					Icons.getDownArrowIcon()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (getRulesListTable().getSelectedRow() >= 0) {
						int[] selectedRows = getRulesListTable()
								.getSelectedRows();

						ArrayUtils.reverse(selectedRows);

						atlasStyler.getRuleLists().pushQuite();
						try {
							for (int sr : selectedRows) {
								if (sr < atlasStyler.getRuleLists().size() - 1) {
									AbstractRulesList rl = atlasStyler
											.getRuleLists().remove(sr);
									atlasStyler.getRuleLists().add(sr + 1, rl);
								}

							}
						} finally {
							atlasStyler.getRuleLists().popQuite();
						}

						// Reslect the ruleslists
						getRulesListTable().getSelectionModel()
								.clearSelection();
						for (int sr : selectedRows) {
							getRulesListTable().getSelectionModel()
									.addSelectionInterval(sr + 1, sr + 1);
						}

					}
				}

			});
			getRulesListTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							ListSelectionModel lsm = ((ListSelectionModel) e
									.getSource());
							if ((lsm.getMinSelectionIndex() < 0)
									|| (lsm.getMinSelectionIndex() >= getRulesListTable()
											.getModel().getRowCount() - 1)) {
								jButtonLayerDown.setEnabled(false);
							} else {
								jButtonLayerDown.setEnabled(true);
							}
						}

					});

			jButtonLayerDown.setEnabled(false);
			jButtonLayerDown.setToolTipText(ASUtil
					.R("RulesListsList.Action.MoveRulesListDown.TT"));

		}
		return jButtonLayerDown;
	}

	private JButton getRemoveButton() {
		if (removeButton == null) {
			removeButton = new SmallButton(new AbstractAction(
					ASUtil.R("RulesListsList.Action.RemoveRulesLists")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					int[] selectedRows = getRulesListTable().getSelectedRows();

					List<Integer> idxList = new ArrayList<Integer>();
					for (int i : selectedRows) {
						if (i >= 0)
							idxList.add(i);
					}
					Collections.sort(idxList);
					Collections.reverse(idxList);

					if (!SwingUtil.askYesNo(
							RulesListsListTablePanel.this,
							ASUtil.R(
									"RulesListsList.Action.RemoveRulesLists.Ask",
									idxList.size())))
						return;

					atlasStyler.getRuleLists().pushQuite();
					try {
						for (int idx : idxList) {
							atlasStyler.getRuleLists().remove(idx);
						}
					} finally {
						atlasStyler.getRuleLists().popQuite();
					}
				}
			});

			// Enable/Disable the button depending on active selections in the
			// list
			getRulesListTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							removeButton.setEnabled(e.getFirstIndex() >= 0);
						}
					});
			removeButton.setEnabled(getRulesListTable().getSelectedRow() >= 0);
		}
		return removeButton;
	}

	private JButton getDuplicateButton() {
		if (duplicateButton == null) {
			duplicateButton = new SmallButton(new AbstractAction(
					ASUtil.R("RulesListsList.Action.DuplicateRulesLists")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					int[] selectedRows = getRulesListTable().getSelectedRows();

					List<Integer> idxList = new ArrayList<Integer>();
					for (int i : selectedRows) {
						if (i >= 0)
							idxList.add(i);
					}

					atlasStyler.getRuleLists().pushQuite();
					try {
						for (int idx : idxList) {
							RulesListInterface rl = atlasStyler.getRuleLists()
									.get(idx);
							AbstractRulesList duplicate = atlasStyler
									.copyRulesList(rl);
							duplicate.setTitle("Copy" + rl.getTitle());

							atlasStyler.getRuleLists().add(duplicate);
						}
					} finally {
						atlasStyler.getRuleLists().popQuite();
					}
				}
			});

			// Enable/Disable the button depending on active selections in the
			// list
			getRulesListTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							duplicateButton.setEnabled(e.getFirstIndex() >= 0);
						}
					});
			duplicateButton
					.setEnabled(getRulesListTable().getSelectedRow() >= 0);
		}
		return duplicateButton;
	}

	public RulesListTable getRulesListTable() {
		if (rulesListTable == null) {
			rulesListTable = new RulesListTable(asd);
		}
		return rulesListTable;
	}

	private JButton getUpButton() {
		if (jButtonLayerUp == null) {
			jButtonLayerUp = new SmallButton(new AbstractAction("",
					Icons.getUpArrowIcon()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (getRulesListTable().getSelectedRow() >= 0) {

						int[] selectedRows = getRulesListTable()
								.getSelectedRows();
						// ArrayUtils.reverse(selectedRows);
						atlasStyler.getRuleLists().pushQuite();
						try {
							for (int sr : selectedRows) {

								if (sr > 0) {
									AbstractRulesList rl = atlasStyler
											.getRuleLists().remove(sr);
									atlasStyler.getRuleLists().add(sr - 1, rl);
								}

							}
						} finally {
							atlasStyler.getRuleLists().popQuite();
						}

						// Reslect the ruleslists
						getRulesListTable().getSelectionModel()
								.clearSelection();
						for (int sr : selectedRows) {
							getRulesListTable().getSelectionModel()
									.addSelectionInterval(sr - 1, sr - 1);
						}
					}
				}

			});
			getRulesListTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							if (((ListSelectionModel) e.getSource())
									.getMinSelectionIndex() < 1) {
								jButtonLayerUp.setEnabled(false);
							} else {
								jButtonLayerUp.setEnabled(true);
							}
						}

					});

			jButtonLayerUp.setEnabled(false);
			jButtonLayerUp.setToolTipText(ASUtil
					.R("RulesListsList.Action.MoveRulesListUp.TT"));

		}
		return jButtonLayerUp;
	}

}
