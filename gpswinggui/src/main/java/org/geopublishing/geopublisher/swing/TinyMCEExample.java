/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package org.geopublishing.geopublisher.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorAdapter;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorSaveEvent;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;

/**
 * @author Christopher Deckers
 */
public class TinyMCEExample {

	protected static final String LS = System.getProperty("line.separator");

	public static JComponent createContent() {
		final JPanel contentPane = new JPanel(new BorderLayout());
		Map<String, String> optionMap = new HashMap<String, String>();
		optionMap
				.put("theme_advanced_buttons1",
						"'bold,italic,underline,strikethrough,sub,sup,|,charmap,|,justifyleft,justifycenter,justifyright,justifyfull,|,hr,removeformat'");
		optionMap
				.put("theme_advanced_buttons2",
						"'undo,redo,|,cut,copy,paste,pastetext,pasteword,|,search,replace,|,forecolor,backcolor,bullist,numlist,|,outdent,indent,blockquote,|,table'");
		optionMap.put("theme_advanced_buttons3", "''");
		optionMap.put("theme_advanced_toolbar_location", "'top'");
		optionMap.put("theme_advanced_toolbar_align", "'left'");
		optionMap.put("relative_urls", "true");
		// optionMap.put("document_base_url","/");
		// Language can be configured when language packs are added to the
		// classpath. Language packs can be found here:asda
		// http://tinymce.moxiecode.com/download_i18n.php
		// optionMap.put("language", "'de'");
		optionMap
		.put("plugins",
				"image");
//		optionMapasfasd
//				.put("plugins",
//						"'advlist autolink link image lists charmap print preview hr anchor pagebreak spellchecker searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking save table contextmenu directionality emoticons template paste textcolor'");
		final JHTMLEditor htmlEditor = new JHTMLEditor(
				JHTMLEditor.HTMLEditorImplementation.TinyMCE,
				JHTMLEditor.TinyMCEOptions.setOptions(optionMap));
		htmlEditor.addHTMLEditorListener(new HTMLEditorAdapter() {
			@Override
			public void saveHTML(HTMLEditorSaveEvent e) {
				JOptionPane
						.showMessageDialog(contentPane,
								"The data of the HTML editor could be saved anywhere...");
			}
		});
		contentPane.add(htmlEditor, BorderLayout.CENTER);
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(BorderFactory
				.createTitledBorder("Custom Controls"));
		JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton setHTMLButton = new JButton("Set HTML");
		middlePanel.add(setHTMLButton);
		JButton getHTMLButton = new JButton("Get HTML");
		middlePanel.add(getHTMLButton);
		southPanel.add(middlePanel, BorderLayout.NORTH);
		final JTextArea htmlTextArea = new JTextArea();
		htmlTextArea
				.setText("<p style=\"text-align: center\">This is an <b>HTML editor</b>, in a <u><i>Swing</i></u> application.<br />"
						+ LS
						+ "<img alt=\"DJ Project Logo\" src=\"http://djproject.sourceforge.net/common/logo.png\" /><br />"
						+ LS
						+ "<a href=\"http://djproject.sourceforge.net/ns/\">DJ Project - Native Swing</a></p>");

		htmlTextArea.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(htmlTextArea);
		scrollPane.setPreferredSize(new Dimension(0, 100));
		southPanel.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(southPanel, BorderLayout.SOUTH);
		getHTMLButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				htmlTextArea.setText(htmlEditor.getHTMLContent());
				htmlTextArea.setCaretPosition(0);
			}
		});
		setHTMLButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				htmlEditor.setHTMLContent(htmlTextArea.getText());
			}
		});

		htmlEditor.setHTMLContent(htmlTextArea.getText());
		return contentPane;
	}

	/* Standard main method to try that test as a standalone application. */
	public static void main(String[] args) {
		NativeInterface.open();
		UIUtils.setPreferredLookAndFeel();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("DJ Native Swing Test");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane()
						.add(createContent(), BorderLayout.CENTER);
				frame.setSize(800, 600);
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
			}
		});
		NativeInterface.runEventPump();
	}

}