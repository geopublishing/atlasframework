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
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GPProps.Keys;
import org.geopublishing.geopublisher.export.JarExportUtil;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.netbeans.spi.wizard.WizardPage;

public class ExportWizardPage_JNLPDefinition extends WizardPage {
	private final JLabel JnlpURLLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JNLP.JNLPURL.Label"));
	private final JLabel AtlasURLLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JNLP.AtlasURL.Label"));
	private final JLabel JSCodeLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JNLP.JavaScriptCode.Label"));
	private final JLabel linkExplanationJLabel = new JLabel(GeopublisherGUI
			.R("ExportWizard.JNLP.Link.Explanation"));

	private final String jsTemplate = GPProps.get(Keys.JWSStartScript);
	JLabel explanation = new JLabel(GeopublisherGUI.R(
			"ExportWizard.JNLP.Explanation", JarExportUtil.JNLP_FILENAME));

	private JTextField jnlpCodebaseJTextField;
	private JTextArea linkJavaScriptJTextArea;
	private JTextField atlasURLJTextField;
	private String validationErrorNoSlash = GeopublisherGUI
			.R("ExportWizard.JNLP.ValidationError.NoSlash");

	public static String getDescription() {
		return GeopublisherGUI.R("ExportWizard.JNLP");
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

			getJnlpCodebaseJTextField().getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void removeUpdate(DocumentEvent e) {
							changedUpdate(e);
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							changedUpdate(e);
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							atlasURLJTextField
									.setText(getJnlpCodebaseJTextField()
											.getText()
											+ JarExportUtil.JNLP_FILENAME);
						}
					});
		}

		return atlasURLJTextField;
	}

	public JTextField getJnlpCodebaseJTextField() {
		if (jnlpCodebaseJTextField == null) {
			jnlpCodebaseJTextField = new JTextField(GPProps.get(Keys.jnlpURL,
					"http://www.domain.com/atlas"));
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
			return GeopublisherGUI.R(
					"ExportWizard.JNLP.ValidationError.Invalid", e
							.getLocalizedMessage());
		}

		return null;

	}

	public JTextArea getLinkJavaScriptJTextArea() {
		if (linkJavaScriptJTextArea == null) {
			linkJavaScriptJTextArea = new JTextArea(40, 5);
			linkJavaScriptJTextArea.setLineWrap(true);
			linkJavaScriptJTextArea.setWrapStyleWord(true);
			linkJavaScriptJTextArea.setEditable(false);
			getJnlpCodebaseJTextField().getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void removeUpdate(DocumentEvent e) {
							changedUpdate(e);
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							changedUpdate(e);
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							updateJavaScriptCodeTextArea();
						}
					});
			updateJavaScriptCodeTextArea();
		}

		return linkJavaScriptJTextArea;
	}

	/**
	 * @param lastChar
	 *            A hack to get the char that is entered as the source for this
	 *            {@link KeyEvent}
	 */
	private void updateJavaScriptCodeTextArea() {
		String javaScriptCode = jsTemplate.replace("__JNLPURL__",
				getJnlpCodebaseJTextField().getText()
						+ JarExportUtil.JNLP_FILENAME);
		javaScriptCode = javaScriptCode.replace("__MINJAVAVERSION__", GPProps
				.get(Keys.MinimumJavaVersion));
		linkJavaScriptJTextArea.setText(javaScriptCode);
	}
}
