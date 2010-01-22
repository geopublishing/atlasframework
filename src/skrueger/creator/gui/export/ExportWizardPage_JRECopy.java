/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
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
	static String desc = AtlasCreator.R("ExportWizard.JRE");

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
		return desc;
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
