/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.export;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.SystemUtils;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;


public class ExportWizardPage_JRECopy extends WizardPage {
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	JLabel explanationJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JRE.Explanation"));

	JLabel javaVersionLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JRE.JavaVersionLabel"));
	private final static JLabel javaVersionJLabel = new JLabel(System.getProperty("java.version"));

	JLabel osLabel = new JLabel(GeopublisherGUI.R("ExportWizard.JRE.OS"));
	private JLabel osJLabel;

	private JCheckBox copyJRECheckBox;

	private final String validationFailedMsg_notWindows = GeopublisherGUI
			.R("ExportWizard.JRE.ValidationError.NotWindows");

	public ExportWizardPage_JRECopy() {
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);

		setLayout(new MigLayout("wrap 4", "[50%|50%]"));

		add(explanationJLabel, "span 4");

		add(javaVersionLabel,"gapy unrelated" );
		add(javaVersionJLabel );

		add(osLabel, "gapx unrel");
		add(getOSJLabel());

		add(getCopyJRECheckBox(), "span 4, center");
	}

	private JLabel getOSJLabel() {
		if (osJLabel == null) {
			osJLabel = new JLabel(System.getProperty("os.name"));
		}
		return osJLabel;
	}

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.JRE");
	}

	public JCheckBox getCopyJRECheckBox() {
		if (copyJRECheckBox == null) {
			copyJRECheckBox = new JCheckBox(GeopublisherGUI
					.R("ExportWizard.JRE.Checkbox"));
			copyJRECheckBox.setName(ExportWizard.COPYJRE);
			copyJRECheckBox
					.setSelected(SystemUtils.IS_OS_WINDOWS);
		}

		return copyJRECheckBox;
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		if ((!SystemUtils.IS_OS_WINDOWS)
				&& (getCopyJRECheckBox().isSelected())) {
			return validationFailedMsg_notWindows;
		}

		return null;
	}

}
