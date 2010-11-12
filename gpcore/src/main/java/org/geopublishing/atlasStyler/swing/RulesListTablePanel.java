package org.geopublishing.atlasStyler.swing;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.AbstractRuleList;
import org.geopublishing.atlasStyler.AtlasStyler;

import schmitzm.swing.JPanel;
import skrueger.swing.SmallButton;

public class RulesListTablePanel extends JPanel {
	private RulesListTable rulesListTable;
	private final AtlasStyler atlasStyler;
	private SmallButton downButton;
	private SmallButton upButton;
	private SmallButton addButton;
	private SmallButton removeButton;

	public RulesListTablePanel(AtlasStyler atlasStyler) {
		super(new MigLayout("", "grow", "[grow][]"));
		this.atlasStyler = atlasStyler;

		add(new JScrollPane(getRulesListTable()), "growy, wrap");
		add(getAddButton(), "split 4, align left, growx");
		add(getRemoveButton(), "align left, gapx");
		add(getUpButton(), "align right");
		add(getDownButton(), "align right");
	}

	private JButton getDownButton() {
		if (downButton == null) {
			downButton = new SmallButton("down");
		}
		return downButton;
	}

	private JButton getUpButton() {
		if (upButton == null) {
			upButton = new SmallButton("up");
		}
		return upButton;
	}

	private JButton getRemoveButton() {
		if (removeButton == null) {
			removeButton = new SmallButton(new AbstractAction("remove") {

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
					for (int idx : idxList) {
						atlasStyler.getRuleLists().remove(idx);
					}
				}
			});
		}
		return removeButton;
	}

	private JButton getAddButton() {
		if (addButton == null) {
			addButton = new SmallButton(new AbstractAction("add") {

				@Override
				public void actionPerformed(ActionEvent e) {
					
					AbstractRuleList importedThisAbstractRuleList = atlasStyler
							.getRlf().createGraduatedColorRuleList(
									atlasStyler.getStyledFeatures()
											.getGeometryForm(), true);
					
					atlasStyler.addRulesList(importedThisAbstractRuleList);
				}

			});
		}
		return addButton;
	}

	public RulesListTable getRulesListTable() {
		if (rulesListTable == null) {
			rulesListTable = new RulesListTable(atlasStyler);
		}
		return rulesListTable;
	}

}
