package org.geopublishing.geopublisher.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.AtlasConfigEditable;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorDirtyStateEvent;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorListener;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorSaveEvent;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor.FCKEditorOptions;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor.TinyMCEOptions;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.JPanel;

/**
 * A html editor based on SimplyHTML.
 */
public class HTMLEditPaneJHTMLEditor extends JPanel implements
		HTMLEditPaneInterface, HTMLEditorListener {
	private final Logger LOGGER = LangUtil.createLogger(this);

	/**
	 * Type of (javascript) editor used in {@link JHTMLEditor} (currently
	 * supported values: "FCK", "TinyMCE"
	 */
	protected String editorType = null;

	/** Holds a {@link JHTMLEditor} for each (language) page to edit. */
	protected JTabbedPane tabs = null;

	/** Holds the source URL / File for each editing tab. */
	protected Map<JHTMLEditor, URL> editURLs = new HashMap<JHTMLEditor, URL>();

	private final AtlasConfigEditable ace;

	/**
	 * Creates a new editor based on {@link JWebBrowser} with FCK.
	 */
	public HTMLEditPaneJHTMLEditor(AtlasConfigEditable ace) {
		this(null, ace);
	}

	/**
	 * Creates a new editor based on {@link JWebBrowser}.
	 * 
	 * @param editorType
	 *            type of editor (currently only "FCK" and "TinyMCE" is
	 *            supported); if <code>null</code> "FCK" is used
	 */
	public HTMLEditPaneJHTMLEditor(String editorType, AtlasConfigEditable ace) {
		super(new BorderLayout());
		this.ace = ace;
		if (editorType == null)
			editorType = "FCK";
		NativeInterface.open();
		this.editorType = editorType;
		this.tabs = new JTabbedPane();
		this.tabs.setTabPlacement(JTabbedPane.TOP);
		this.add(tabs, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(800, 500));
	}

	/**
	 * Returns {@code this}.
	 */
	@Override
	public JComponent getComponent() {
		return this;
	}

	/**
	 * Returns {@code true}, because {@link JHTMLEditor} already provides
	 * scrolling.
	 */
	@Override
	public boolean hasScrollPane() {
		return true;
	}

	/**
	 * Adds a tab pane to edit a HTML document.
	 * 
	 * @param title
	 *            tab title
	 * @param url
	 *            URL of the document to be edit
	 * @param idx
	 *            index number for the document title if a new file is created
	 */
	@Override
	public void addEditorTab(String title, URL url, int idx) {
		JHTMLEditor editor = createJHTMLEditor(editorType, url);

		// add a listener for the save operation
		editor.addHTMLEditorListener(this);
		// add source file to map (for the new editor tab)
		editURLs.put(editor, url);

		String htmlContent = IOUtil.readURLasString(url);
		editor.setHTMLContent(htmlContent);
		tabs.addTab(title, editor);
	}

	/**
	 * Removes all tabs.
	 */
	@Override
	public void removeAllTabs() {
		// for (int i=0; i<tabs.getTabCount(); i++) {
		// TabbedJHTMLEditor editor =
		// (TabbedJHTMLEditor)tabs.getTabComponentAt(i);
		// editor.htmlEditor.dispose(); // something like that necessary???
		// }
		editURLs.clear();
		tabs.removeAll();
	}

	/**
	 * Called, when SAVE button of {@link JHTMLEditor} (of any tab!) is
	 * performed.
	 */
	@Override
	public void saveHTML(HTMLEditorSaveEvent event) {
		saveHTML(event.getHTMLEditor());
	}

	protected void saveHTML(JHTMLEditor editor) {
		URL sourceURL = editURLs.get(editor);
		String htmlContent = editor.getHTMLContent();

		if (htmlContent == null) {
			LOGGER.warn(JHTMLEditor.class.getSimpleName()
					+ " asked to save a NULL HTML content! Ignoring...");
			return;
		}

		BufferedWriter writer = null;
		try {
			File sourceHTMLFile = IOUtil.urlToFile(sourceURL);

			// parse the html content for image references to the
			// local file system,
			Map<String, File> replaceRef = GpSwingUtil
					.findFileReferencesToReplace(htmlContent);
			// copy references to the image folder and replace
			// the reference in the html content
			Vector<String> copiedFiles = new Vector<String>();
			for (String absImageRef : replaceRef.keySet()) {
				File sourceRefFile = replaceRef.get(absImageRef);
				// replace html content with relative URL
				String relImageRef = "images/" + sourceRefFile.getName();
				htmlContent = htmlContent.replace(absImageRef, relImageRef);
				// copy file
				File destRefFile = new File(sourceHTMLFile.getParent(),
						relImageRef);
				if (!destRefFile.equals(sourceRefFile)) {
					IOUtil.copyFile(LOGGER, sourceRefFile, destRefFile, false);
					copiedFiles.add(sourceRefFile.getName());
				}
			}
			// replace editor content with reworked content
			editor.setHTMLContent(htmlContent);
			// write html file

			// Explicitly writing the HTML file in UTF-8 - of course the HTML
			// should only use &uuml; etc. and no special characters anyways..
			// BUT! Laotic &'...; tags are converted by FCKEditor no real UTF!
			// SO we have to force saving the html as real UTF8 -even on
			// windows.
			FileUtils.writeStringToFile(sourceHTMLFile, htmlContent, "UTF-8");
			//
			// writer = new BufferedWriter(new FileWriter(sourceHTMLFile,
			// false));
			// writer.write(htmlContent);
			// writer.flush();

			JOptionPane.showMessageDialog(
					this,
					copiedFiles.size() == 0 ? GpSwingUtil
							.R("HTMLEditPaneJHTMLEditor.save.success")
							: GpSwingUtil.R(
									"HTMLEditPaneJHTMLEditor.save.success2",
									copiedFiles.size(),
									"\n- "
											+ LangUtil.stringConcatWithSep(
													"\n- ",
													(Collection) copiedFiles)),
					GpSwingUtil.R("HTMLEditPaneJHTMLEditor.save.title"),
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception err) {
			ExceptionDialog.show(editor, err,
					GpSwingUtil.R("HTMLEditPaneJHTMLEditor.save.title"),
					GpSwingUtil.R("HTMLEditPaneJHTMLEditor.save.error"));
		} finally {
			IOUtil.closeWriter(writer);
		}
	}

	/**
	 * Called when surrounded window/dialog/application is closed. Should
	 * perform editor specific actions (e.g. save operation).
	 * 
	 * @param source
	 *            object which initiates the closing
	 * @return {@code false} if possible dialog is canceled; {@code true}
	 *         otherwise to force the surrounding application to close the
	 *         editor frame
	 */
	@Override
	public boolean performClosing(Object source) {
		// check whether one of the files is not already saved
		Vector<JHTMLEditor> changedURLs = new Vector<JHTMLEditor>();
		for (JHTMLEditor editor : editURLs.keySet()) {
			URL url = editURLs.get(editor);
			// TODO: Unfortunately the compare between file and content is
			// ever unequal! Probably because readURLasString(.) does
			// manually inserts "\n" for line breaks.
			if (editor.getHTMLContent() != null &&
			    !editor.getHTMLContent().equals(IOUtil.readURLasString(url)))
				changedURLs.add(editor);
		}

		// in case of unsaved changes, ask for save
		if (!changedURLs.isEmpty()) {
			int ret = JOptionPane.showConfirmDialog(this,
					GpSwingUtil.R("HTMLEditPaneJHTMLEditor.SaveQuestion"));
			switch (ret) {
			case JOptionPane.CANCEL_OPTION:
				// do nothing, just return FALSE to NOT close the dialog
				return false;
			case JOptionPane.YES_OPTION:
				// save changed files
				for (JHTMLEditor editor : changedURLs)
					saveHTML(editor);
				return true;
			case JOptionPane.NO_OPTION:
				// do not save the changes, just return TRUE to close the dialog
				return true;
			}
		}
		return true;
	}

	/**
	 * Creates an {@link JHTMLEditor} instance.
	 * 
	 * @param editorType
	 *            supported (javascript) editors: "FCK", "TinyMCE"
	 */
	protected JHTMLEditor createJHTMLEditor(String editorType, URL sourceURL) {
		JHTMLEditor htmlEditor = null;

		/**
		 * A File pointing to the local directory where to start the IMage/File
		 * browser Swing Dialog
		 */
		File browseStartupFolder = IOUtil.urlToFile(sourceURL).getParentFile();

		/**
		 * The base url for the WebBrowser of the FCK HTML Editor
		 */
		String baseHrefStr = null;
		try {

			baseHrefStr = ace.getBrowserURLString(IOUtil
					.getParentUrl(sourceURL));
		} catch (Exception err) {
			LOGGER.warn("Could not determine parent URL for '" + sourceURL
					+ "'");
		}

		//
		// String awcAbsURLStr = IOUtil.fileToURL(
		// ace.getAtlasDir().getAbsoluteFile()).toString();
		// String sourceAbsURLStr = IOUtil.getParentUrl(sourceURL).toString();
		// int relURLStartIdx = sourceAbsURLStr.indexOf(awcAbsURLStr);
		// String sourceRelURLStr = sourceAbsURLStr.substring(relURLStartIdx
		// + awcAbsURLStr.length());
		// baseURLStr = "http://localhost:" + Webserver.PORT + "/"
		// + sourceRelURLStr;
		// if (!baseURLStr.endsWith("/"))
		// baseURLStr += "/";
		//
		// // Startup folder for file chooser
		// // = Directory of the html file
		// browseStartupFolder = IOUtil.urlToFile(sourceURL).getParentFile();
		// } catch (Exception err) {
		// LOGGER.warn("Could not determine parent URL for '" + sourceURL
		// + "'");
		// }

		if (editorType.equalsIgnoreCase("FCK")) {
			// Create FCK as editor
			String configScript = "";
			// Configure toolbars
			// Also possible actions (but not useful for GP:
			// 'Style', 'Flash'
			// 'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select',
			// 'Button', 'ImageButton', 'HiddenField'
			configScript += "FCKConfig.ToolbarSets[\"Default\"] = [\n"
					+ "['Source','DocProps','-','Save','NewPage','Preview','-','Templates'],\n"
					+ "['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],\n"
					+ "['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],\n"
					+ "'/',\n"
					+ "['FontFormat','FontName','FontSize'],\n"
					+ "['TextColor','BGColor'],\n"
					+ "'/',\n"
					+ "['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],\n"
					+ "['OrderedList','UnorderedList','-','Outdent','Indent','Blockquote'],\n"
					+ "['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],\n"
					+ "['Link','Unlink','Anchor'],\n"
					+ "['Image','Table','Rule','Smiley','SpecialChar','PageBreak', '-', 'ShowBlocks'],\n"
					+ "];\n" + "FCKConfig.ToolbarCanCollapse = false;\n";
			// Configure base URL so that the images with relative URLs are
			// also shown
			if (baseHrefStr != null)
				configScript += "FCKConfig.BaseHref = '" + baseHrefStr + "';\n";
			// Hide "target" options for links because in GP
			// we can only show links in the same frame
			configScript += "FCKConfig.LinkDlgHideTarget = true;\n";
			// Hide complete "link" tab for images to avoid
			// the "target" property (see above!)
			configScript += "FCKConfig.ImageDlgHideLink = true;\n";
			// Set starting focus to editing area
			configScript += "FCKConfig.StartupFocus = true;\n";
			// // Set auto formatting html code (on output = saving)
			// configScript +=
			// "FCKConfig.FormatOutput = true; FCKConfig.FormatSource = true;\n";
			// Set language used in GP
			configScript += "FCKConfig.AutoDetectLanguage = false; FCKConfig.DefaultLanguage = \""
					+ Locale.getDefault() + "\";\n";
			// Disable Upload buttons
			configScript += "FCKConfig.ImageUpload = false; FCKConfig.LinkUpload = false;\n";
			// Disable Browse buttons for links
			configScript += "FCKConfig.LinkBrowser = false;\n";
			// We use our own file chooser (hack in DJ WebServer), so the unfortunately
			// necessary browser window should be as small as possible! 
			configScript += "FCKConfig.ImageBrowserWindowWidth = \"1\"; FCKConfig.ImageBrowserWindowHeight = \"1\";\n";

			// Use of a custom browser (does only work if this
			// runs on the same web server as FCK!
			// configScript += "FCKConfig.ImageBrowserURL = \""
			// + myimageBrowserUrl + "\";\n";
			// configScript +=
			// "FCKConfig.ImageBrowserURL = FCKConfig.BasePath + 'filemanager/browser/default/browser.html?Connector=../../connectors/' + _FileBrowserLanguage + '/connector.' + _FileBrowserExtension;\n";

//			// Enable FCK debugging
//			configScript += "FCKConfig.debug = true;\n";

			// Log the editor configuration
			LOGGER.debug(configScript);

			// Create editor instance
			htmlEditor = new JHTMLEditor(
					JHTMLEditor.HTMLEditorImplementation.FCKEditor,
					FCKEditorOptions
							.setCustomJavascriptConfiguration(configScript));

			htmlEditor.setFileBrowserStartFolder(browseStartupFolder);
			return htmlEditor;
		}

		if (editorType.equalsIgnoreCase("TinyMCE")) {
			// Create TinyMCE as editor
			final String configScript = "theme_advanced_buttons1: 'bold,italic,underline,strikethrough,sub,sup,|,charmap,|,justifyleft,justifycenter,justifyright,justifyfull,|,hr,removeformat',"
					+ "theme_advanced_buttons2: 'undo,redo,|,cut,copy,paste,pastetext,pasteword,|,search,replace,|,forecolor,backcolor,bullist,numlist,|,outdent,indent,blockquote,|,table',"
					+ "theme_advanced_buttons3: '',"
					+ "theme_advanced_toolbar_location: 'top',"
					+ "theme_advanced_toolbar_align: 'left'," +
					// Language can be configured when language packs are added
					// to the classpath. Language packs can be found here:
					// http://tinymce.moxiecode.com/download_i18n.php
					// "language: 'de'," +
					"plugins: 'table,paste'";

			htmlEditor = new JHTMLEditor(
					JHTMLEditor.HTMLEditorImplementation.TinyMCE,
					TinyMCEOptions.setCustomHTMLHeaders(configScript));
			return htmlEditor;
		}
		throw new UnsupportedOperationException(
				"Unknown editor type to create JHTMLEditor: " + editorType);
	}

	@Override
	public void notifyDirtyStateChanged(HTMLEditorDirtyStateEvent arg0) {
		LOGGER.info(arg0);

	}
}
