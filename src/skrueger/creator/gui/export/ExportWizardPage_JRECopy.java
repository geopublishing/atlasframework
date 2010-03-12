/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.export;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.WizardPage;

import skrueger.atlas.AVUtil;
import skrueger.atlas.AVUtil.OSfamiliy;
import skrueger.creator.AtlasCreator;

public class ExportWizardPage_JRECopy extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JRE.Explanation"));

	JLabel javaVersionLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JRE.JavaVersionLabel"));
	private JLabel javaVersionJLabel;

	JLabel osLabel = new JLabel(AtlasCreator.R("ExportWizard.JRE.OS"));
	private JLabel osJLabel;

	private JCheckBox copyJRECheckBox;

	private final String validationFailedMsg_notWindows = AtlasCreator
			.R("ExportWizard.JRE.ValidationError.NotWindows");

	public ExportWizardPage_JRECopy() {
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 2", "[50%|50%]"));

		add(explanationJLabel, "span 2");

		add(javaVersionLabel, "gapy unrelated");
		add(getJavaVersionJLabel());

		add(osLabel);
		add(getOSJLabel());

		add(getCopyJRECheckBox(), "gapy unrelated, span 2");

	}

	private JLabel getJavaVersionJLabel() {
		if (javaVersionJLabel == null) {
			javaVersionJLabel = new JLabel(System.getProperty("java.version"));
		}
		return javaVersionJLabel;
	}

	private JLabel getOSJLabel() {
		if (osJLabel == null) {
			osJLabel = new JLabel(System.getProperty("os.name"));
		}
		return osJLabel;
	}

	public static String getDescription() {
		return AtlasCreator.R("ExportWizard.JRE");
	}

	public JCheckBox getCopyJRECheckBox() {
		if (copyJRECheckBox == null) {
			copyJRECheckBox = new JCheckBox(AtlasCreator
					.R("ExportWizard.JRE.Checkbox"));
			copyJRECheckBox.setName(ExportWizard.COPYJRE);
			copyJRECheckBox
					.setSelected(AVUtil.getOSType() == OSfamiliy.windows);
		}

		return copyJRECheckBox;
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		if ((AVUtil.getOSType() != OSfamiliy.windows)
				&& (getCopyJRECheckBox().isSelected())) {
			return validationFailedMsg_notWindows;
		}

		return null;
	}

}
