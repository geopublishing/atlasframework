package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList.RulesListType;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.Cancellable;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

public class AddRulesListDialog extends AtlasDialog implements Cancellable {

	private final RulesListJComboBox jComboBoxRuleListType;

	private final AtlasStyler atlasStyler;

	private final Component owner;

	private JPanel imagelabel;

	public AddRulesListDialog(Component owner, AtlasStyler atlasStyler) {
		super(owner, ASUtil.R("AddRulesListDialog.title",
				atlasStyler.getTitle()));

		// Just copy the title of StylerDialog now..
		// atlasStyler.getStyledFeatures().getTitle()
		this.owner = owner;
		this.atlasStyler = atlasStyler;

		jComboBoxRuleListType = new RulesListJComboBox(atlasStyler);

		jComboBoxRuleListType.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateImage();
			}

		});

		initGui();
	}

	private void updateImage() {
		final JPanel il = getImageLabel();
		il.removeAll();
		RulesListType rlt = (RulesListType) jComboBoxRuleListType
				.getSelectedItem();
		il.add(new JLabel(rlt.getImage()));
		il.invalidate();
		il.validate();
		il.repaint();

		// pack();

	}

	private JPanel getImageLabel() {
		if (imagelabel == null) {
			imagelabel = new JPanel(new MigLayout());
		}
		return imagelabel;

	}

	private void initGui() {
		setLayout(new MigLayout("wrap 2", "[grow 200][grow]"));

		if (atlasStyler instanceof AtlasStylerVector) {

			AtlasStylerVector asv = (AtlasStylerVector) atlasStyler;

			int countNumAttr = FeatureUtil.getNumericalFieldNames(
					asv.getStyledFeatures().getSchema()).size();

			int countTextAttr = FeatureUtil.getValueFieldNames(
					asv.getStyledFeatures().getSchema()).size()
					- countNumAttr;

			GeometryForm geomType = asv.getStyledFeatures().getGeometryForm();

			add(new JLabel(ASUtil.R("AddRulesListDialog.Vector.explanation",
					geomType, countTextAttr, countNumAttr)),
					"top, grow, width ::500");
		} else {
			add(new JLabel(ASUtil.R("AddRulesListDialog.Raster.explanation")),
					"top, grow, width ::500");
		}

		add(getImageLabel(), "growx 10, width 100, height 80");

		add(jComboBoxRuleListType, "span 2, growx");

		add(getOkButton(), "gapy unrel, span 2, split 2, tag ok, bottom");
		add(getCancelButton(), "tag cancel, bottom");

		setModal(true);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);

		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_INNER,
				SwingUtil.NORTHWEST);

		updateImage();

		pack();

	}

	@Override
	public void cancel() {
	}

	@Override
	public boolean close() {
		if (super.close()) {
			if (jComboBoxRuleListType.getSelectedIndex() >= 0) {
				AbstractRulesList rulelist = atlasStyler
						.getRlf()
						.createRulesList(
								(RulesListType) jComboBoxRuleListType
										.getSelectedItem(),
								true);
				atlasStyler.addRulesList(rulelist);
			}
			return true;
		} else
			return false;
	}

}
