package org.geopublishing.atlasStyler.swing;

import java.awt.Component;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.TextRuleList;

import de.schmitzm.i18n.LanguagesComboBox;
import de.schmitzm.swing.CancellableDialogAdapter;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

public class TextLabelingClassLanguageSelectorDialog extends
		CancellableDialogAdapter {

	private final TextRuleList rulesList;
	private final LanguagesComboBox lcb;

	@Override
	public boolean close() {
		if (lcb.getSelectedIndex() == -1)
			return false;
		return super.close();
	}

	public TextLabelingClassLanguageSelectorDialog(Component parentGui,
			TextRuleList rulesList) {
		super(parentGui, ASUtil
				.R("TextSymbolizerClass.CreateALanguageDefault.DialogTitle"));
		this.rulesList = rulesList;

		lcb = new LanguagesComboBox(AtlasStylerVector.getLanguages(),
				rulesList.getDefaultLanguages());

		setContentPane(new JPanel(new MigLayout("wrap 1, w 400")));
		getContentPane()
				.add(new JLabel(
						ASUtil.R("TextSymbolizerClass.CreateALanguageDefault.Explanation")),
						"");
		getContentPane().add(lcb, "w 300, center");
		getContentPane().add(getOkButton(), "split 2, tag ok");
		getContentPane().add(getCancelButton(), "tag cancel");

		pack();

		SwingUtil.setRelativeFramePosition(this, parentGui, 0.5, 0.5);
	}

	@Override
	public void cancel() {

	}

	public String getSelectedLanguage() {
		return lcb.getSelectedLanguage();
	}

}
