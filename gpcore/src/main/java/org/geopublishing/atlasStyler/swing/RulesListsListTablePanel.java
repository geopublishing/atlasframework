package org.geopublishing.atlasStyler.swing;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.ArrayUtils;
import org.geopublishing.atlasStyler.AbstractRulesList;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.RulesListsList;
import org.geopublishing.atlasViewer.swing.Icons;

import schmitzm.swing.JPanel;
import skrueger.swing.SmallButton;

public class RulesListsListTablePanel extends JPanel {
	private SmallButton addButton;
	private final AtlasStyler atlasStyler;
	private SmallButton jButtonLayerDown;
	private SmallButton jButtonLayerUp;
	private SmallButton removeButton;
	private RulesListTable rulesListTable;

	public RulesListsListTablePanel(AtlasStyler atlasStyler) {
		super(new MigLayout("", "grow", "[grow][]"));
		this.atlasStyler = atlasStyler;

		add(new JScrollPane(getRulesListTable()), "growy, wrap");
		add(getAddButton(), "split 4, align left, growx");
		add(getRemoveButton(), "align left, gapx");
		add(getUpButton(), "align right");
		add(getDownButton(), "align right");
	}

	private JButton getAddButton() {
		if (addButton == null) {
			addButton = new SmallButton(new AbstractAction("add") {

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
						RulesListsList rll = atlasStyler.getRuleLists();

						int[] selectedRows = getRulesListTable()
								.getSelectedRows();
						ArrayUtils.reverse(selectedRows);
						for (int sr : selectedRows) {

							if (sr < rll.size() - 1) {
								AbstractRulesList rl = rll.remove(sr);
								rll.add(sr + 1, rl);

							}

						}

						// Reslect the ruleslists
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
							if ((lsm.getMinSelectionIndex() > 0)
									|| (lsm.getMinSelectionIndex() >= getRulesListTable()
											.getModel().getRowCount() - 1)) {
								jButtonLayerDown.setEnabled(false);
							} else {
								jButtonLayerDown.setEnabled(true);
							}
						}

					});

			jButtonLayerDown.setEnabled(false);
			jButtonLayerDown.setToolTipText(AtlasStyler
					.R("RulesListsList.Action.MoveRulesListDown.TT"));

		}
		return jButtonLayerDown;
	}

	private JButton getRemoveButton() {
		if (removeButton == null) {
			removeButton = new SmallButton(new AbstractAction("remove") {

				@Override
				public void actionPerformed(ActionEvent e) {
					int[] selectedRows = getRulesListTable().getSelectedRows();

					// TODO Ask the user

					List<Integer> idxList = new ArrayList<Integer>();
					for (int i : selectedRows) {
						if (i >= 0)
							idxList.add(i);
					}
					Collections.sort(idxList);
					Collections.reverse(idxList);
					for (int idx : idxList) {
						atlasStyler.getRuleLists().remove(idx);
					}
				}
			});
		}
		return removeButton;
	}

	public RulesListTable getRulesListTable() {
		if (rulesListTable == null) {
			rulesListTable = new RulesListTable(atlasStyler);
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
						for (int sr : selectedRows) {

							RulesListsList rll = atlasStyler.getRuleLists();

							if (sr > 0) {
								AbstractRulesList rl = rll.remove(sr);
								rll.add(sr - 1, rl);

							}

						}

						// Reslect the ruleslists
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
			jButtonLayerUp.setToolTipText(AtlasStyler
					.R("RulesListsList.Action.MoveRulesListUp.TT"));

		}
		return jButtonLayerUp;
	}

}
