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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.netbeans.spi.wizard.WizardPage;

import skrueger.creator.AtlasCreator;
import skrueger.creator.GPProps;
import skrueger.creator.GPProps.Keys;
import skrueger.creator.export.JarExportUtil;

public class ExportWizardPage_JNLPDefinition extends WizardPage {
	private static final JLabel JnlpURLLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JNLP.JNLPURL.Label"));
	private static final JLabel AtlasURLLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JNLP.AtlasURL.Label"));
	private static final JLabel JSCodeLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JNLP.JavaScriptCode.Label"));
	private static final JLabel linkExplanationJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JNLP.Link.Explanation"));

	private static final String jsTemplate = GPProps.get(Keys.JWSStartScript);
	/*
	 * The short description label that appears on the left side of the wizard
	 */
	static String desc = AtlasCreator.R("ExportWizard.JNLP");

	JLabel explanation = new JLabel(AtlasCreator.R(
			"ExportWizard.JNLP.Explanation", JarExportUtil.JNLP_FILENAME));

	private JTextField jnlpCodebaseJTextField;
	private JTextArea linkJavaScriptJTextArea;
	private JTextField atlasURLJTextField;
	private String validationErrorNoSlash = AtlasCreator
			.R("ExportWizard.JNLP.ValidationError.NoSlash");

	public static String getDescription() {
		return desc;
	}

	public ExportWizardPage_JNLPDefinition() {
		setPreferredSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		setSize(ExportWizard.DEFAULT_WPANEL_SIZE);
		initGui();
	}

	@Override
	protected void renderingPage() {
	}

	private void initGui() {
		setLayout(new MigLayout("wrap 2, width "
				+ (ExportWizard.DEFAULT_WPANEL_SIZE.width)));

		String jwidth = "w " + (ExportWizard.DEFAULT_WPANEL_SIZE.width - 20)
				+ "!";

		add(explanation, "span 2, growy, shrinkx, " + jwidth);
		add(JnlpURLLabel, "sgx A, right");
		add(getJnlpCodebaseJTextField(), "sgx B, growx");

		add(linkExplanationJLabel, "span 2, gapy unrelated");

		add(AtlasURLLabel, "sgx A, right");
		add(getAtlasURLJTextField(), "sgx B, growx");

		add(JSCodeLabel, "span 2");
		add(new JScrollPane(getLinkJavaScriptJTextArea()),
				"span 2, growy, growprio 200, " + jwidth);
	}

	private JTextField getAtlasURLJTextField() {
		if (atlasURLJTextField == null) {
			atlasURLJTextField = new JTextField(getJnlpCodebaseJTextField()
					.getText()
					+ "/" + JarExportUtil.JNLP_FILENAME);
			atlasURLJTextField.setEditable(false);

			getJnlpCodebaseJTextField().addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
					atlasURLJTextField.setText(getJnlpCodebaseJTextField()
							.getText()
							+ "/" + JarExportUtil.JNLP_FILENAME);
				}
			});
		}

		return atlasURLJTextField;
	}

	public JTextField getJnlpCodebaseJTextField() {
		if (jnlpCodebaseJTextField == null) {
			jnlpCodebaseJTextField = new JTextField(GPProps.get(
					GPProps.Keys.jnlpURL, "http://www.domain.com/atlas"));
			jnlpCodebaseJTextField.setName(ExportWizard.JNLPURL);
		}

		return jnlpCodebaseJTextField;
	}

	/**
	 * Checks whether the entered Base-URL looks valid.
	 */
	@Override
	protected String validateContents(final Component component,
			final Object event) {

		if (getJnlpCodebaseJTextField().getText() == null
				|| (!getJnlpCodebaseJTextField().getText().endsWith("/"))) {
			return validationErrorNoSlash;
		}

		try {
			URL testUrl = new URL(getJnlpCodebaseJTextField().getText());
		} catch (MalformedURLException e) {
			return AtlasCreator.R("ExportWizard.JNLP.ValidationError.Invalid",
					e.getLocalizedMessage());
		}

		return null;

	}

	public JTextArea getLinkJavaScriptJTextArea() {
		if (linkJavaScriptJTextArea == null) {
			linkJavaScriptJTextArea = new JTextArea(40, 5);
			linkJavaScriptJTextArea.setLineWrap(true);
			linkJavaScriptJTextArea.setWrapStyleWord(true);
			linkJavaScriptJTextArea.setEditable(false);
			getJnlpCodebaseJTextField().addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
					updateJavaScriptCodeTextArea();
				}

			});

			updateJavaScriptCodeTextArea();
		}

		return linkJavaScriptJTextArea;
	}

	private void updateJavaScriptCodeTextArea() {
		String javaScriptCode = jsTemplate.replace("__JNLPURL__",
				getJnlpCodebaseJTextField().getText() + "/"
						+ JarExportUtil.JNLP_FILENAME);
		javaScriptCode = javaScriptCode.replace("__MINJAVAVERSION__", GPProps
				.get(Keys.MinimumJavaVersion));
		linkJavaScriptJTextArea.setText(javaScriptCode);
	}
}
