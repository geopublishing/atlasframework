package org.geopublishing.atlasStyler.swing;

import java.awt.Component;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AbstractRulesList;
import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geopublishing.atlasStyler.AtlasStyler;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.swing.SwingUtil;
import skrueger.swing.Cancellable;
import skrueger.swing.CancellableDialogAdapter;

public class AddRulesListDialog extends CancellableDialogAdapter implements
		Cancellable {

	private final RulesListJComboBox jComboBoxRuleListType;

	private final AtlasStyler atlasStyler;

	private final Component owner;

	public AddRulesListDialog(Component owner, AtlasStyler atlasStyler) {
		super(owner, ASUtil.R("AddRulesListDialog.title", atlasStyler
				.getStyledFeatures().getTitle()));
		this.owner = owner;
		this.atlasStyler = atlasStyler;

		jComboBoxRuleListType = new RulesListJComboBox(atlasStyler
				.getStyledFeatures().getGeometryForm(), atlasStyler);

		initGui();
	}

	private void initGui() {
		setLayout(new MigLayout("wrap 1", "[grow]"));

		int countNumAttr = FeatureUtil.getNumericalFieldNames(
				atlasStyler.getStyledFeatures().getSchema()).size();

		int countTextAttr = FeatureUtil.getValueFieldNames(
				atlasStyler.getStyledFeatures().getSchema()).size()
				- countNumAttr;

		GeometryForm geomType = atlasStyler.getStyledFeatures()
				.getGeometryForm();

		add(new JLabel(ASUtil.R("AddRulesListDialog.explanation", geomType,
				countTextAttr, countNumAttr)), "top, grow, width ::400");

		add(jComboBoxRuleListType);

		add(getOkButton(), "gapy unrel, split 2, tag ok, bottom");
		add(getCancelButton(), "tag ok, bottom");

		setModal(true);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		pack();

		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_INNER,
				SwingUtil.NORTHWEST);

	}

	void insert(AbstractRulesList rl) {

		atlasStyler.addRulesList(rl);
	}

	@Override
	public void cancel() {
	}

	@Override
	public boolean okClose() {
		if (jComboBoxRuleListType.getSelectedIndex() >= 0) {
			AbstractRulesList rulelist = atlasStyler.getRlf().createRulesList(
					(RulesListType) jComboBoxRuleListType.getSelectedItem(),
					true);
			atlasStyler.addRulesList(rulelist);
		}
		return super.okClose();
	}

}
