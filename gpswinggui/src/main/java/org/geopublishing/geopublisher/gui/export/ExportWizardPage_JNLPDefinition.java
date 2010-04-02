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
package org.geopublishing.geopublisher.gui.export;

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

import org.geopublishing.geopublisher.AtlasCreator;
import org.geopublishing.geopublisher.export.JarExportUtil;
import org.netbeans.spi.wizard.WizardPage;

import skrueger.creator.GPProps;
import skrueger.creator.GPProps.Keys;


public class ExportWizardPage_JNLPDefinition extends WizardPage {
	private final JLabel JnlpURLLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JNLP.JNLPURL.Label"));
	private final JLabel AtlasURLLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JNLP.AtlasURL.Label"));
	private final JLabel JSCodeLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JNLP.JavaScriptCode.Label"));
	private final JLabel linkExplanationJLabel = new JLabel(AtlasCreator
			.R("ExportWizard.JNLP.Link.Explanation"));

	private final String jsTemplate = GPProps.get(Keys.JWSStartScript);
	JLabel explanation = new JLabel(AtlasCreator.R(
			"ExportWizard.JNLP.Explanation", JarExportUtil.JNLP_FILENAME));

	private JTextField jnlpCodebaseJTextField;
	private JTextArea linkJavaScriptJTextArea;
	private JTextField atlasURLJTextField;
	private String validationErrorNoSlash = AtlasCreator
			.R("ExportWizard.JNLP.ValidationError.NoSlash");

	public static String getDescription() {
		return  AtlasCreator.R("ExportWizard.JNLP");
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
					Keys.jnlpURL, "http://www.domain.com/atlas"));
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
