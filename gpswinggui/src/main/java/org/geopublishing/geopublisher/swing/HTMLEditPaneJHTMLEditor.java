package org.geopublishing.geopublisher.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorListener;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorSaveEvent;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.JPanel;

/**
 * A html editor based on SimplyHTML. 
 */
public class HTMLEditPaneJHTMLEditor extends JPanel implements HTMLEditPaneInterface, HTMLEditorListener {
  private final Logger LOGGER = LangUtil.createLogger(this);

  /** Type of (javascript) editor used in {@link JHTMLEditor} (currently supported values:
   *  "FCK", "TinyMCE" */
  protected String editorType = null;
  
  /** Holds a {@link JHTMLEditor} for each (language) page to edit. */
  protected JTabbedPane tabs = null;
  
  /** Holds the source URL / File for each editing tab. */
  protected Map<JHTMLEditor,URL> editURLs = new HashMap<JHTMLEditor, URL>();
  
  
  /**
   * Creates a new editor based on {@link JWebBrowser} with FCK.
   */
  public HTMLEditPaneJHTMLEditor() {
    this(null);
  }
  
  /**
   * Creates a new editor based on {@link JWebBrowser}.
   * @param editorType type of editor (currently only "FCK" and "TinyMCE" is supported);
   *                   if <code>null</code> "FCK" is used
   */
  public HTMLEditPaneJHTMLEditor(String editorType) {
    super(new BorderLayout());
    if ( editorType == null )
      editorType = "FCK";
    NativeInterface.open();
    this.editorType = editorType;
    this.tabs = new JTabbedPane();
    this.tabs.setTabPlacement(JTabbedPane.TOP);
    this.add(tabs,BorderLayout.CENTER);
    this.setPreferredSize( new Dimension(800,500) );
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
   * @param title  tab title
   * @param url    URL of the document to be edit
   * @param idx    index number for the document title if a new
   *               file is created
   */
  @Override
  public void addEditorTab(String title, URL url, int idx) {
    JHTMLEditor editor = createJHTMLEditor(editorType);
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
//    for (int i=0; i<tabs.getTabCount(); i++) {
//      TabbedJHTMLEditor editor = (TabbedJHTMLEditor)tabs.getTabComponentAt(i);
//      editor.htmlEditor.dispose(); // something like that necessary???
//    }
    editURLs.clear();
    tabs.removeAll();
  }
  
  /**
   * Called, when SAVE button of {@link JHTMLEditor} (of any tab!)
   * is performed.
   */
  @Override
  public void saveHTML(HTMLEditorSaveEvent event) {
    saveHTML(event.getHTMLEditor());
  }
  
  protected void saveHTML(JHTMLEditor editor) {
    URL sourceURL = editURLs.get(editor);
    String htmlContent = editor.getHTMLContent();
    BufferedWriter writer = null;
    try {
      File sourceFile = IOUtil.urlToFile(sourceURL);
      writer = new BufferedWriter( new FileWriter(sourceFile,false) );
      writer.write(htmlContent);
      writer.flush();
      JOptionPane.showMessageDialog(
          this,
          GpSwingUtil.R("HTMLEditPaneJHTMLEditor.save.success"),
          GpSwingUtil.R("HTMLEditPaneJHTMLEditor.save.title"),
          JOptionPane.INFORMATION_MESSAGE
      );
    } catch (Exception err) {
      ExceptionDialog.show(
          editor,
          err,
          GpSwingUtil.R("HTMLEditPaneJHTMLEditor.save.title"),
          GpSwingUtil.R("HTMLEditPaneJHTMLEditor.save.error")
      );
    } finally {
      IOUtil.closeWriter(writer);
    }
  }

  /**
   * Called when surrounded window/dialog/application is closed.
   * Should perform editor specific actions (e.g. save operation).
   * @param source object which initiates the closing
   * @return {@code false} if possible dialog is canceled; {@code true} otherwise to
   *         force the surrounding application to close the editor frame  
   */
  @Override
  public boolean performClosing(Object source) {
    // check whether one of the files is not already saved
    Vector<JHTMLEditor> changedURLs = new Vector<JHTMLEditor>();
    for ( JHTMLEditor editor : editURLs.keySet() ) {
      URL url = editURLs.get(editor);
      // TODO: Unfortunately the compare between file and content is
      //       ever unequal! Probably because readURLasString(.) does
      //       manually inserts "\n" for line breaks.
      if ( !editor.getHTMLContent().equals( IOUtil.readURLasString(url) ) )
        changedURLs.add(editor);
   }
    
    // in case of unsaved changes, ask for save 
    if ( !changedURLs.isEmpty() ) {
      int ret = JOptionPane.showConfirmDialog(this, GpSwingUtil.R("HTMLEditPaneJHTMLEditor.SaveQuestion"));
      switch ( ret ) {
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
   * @param editorType supported (javascript) editors: "FCK", "TinyMCE"
   */
  protected JHTMLEditor createJHTMLEditor(String editorType) {
    JHTMLEditor htmlEditor = null;
    
    if ( editorType.equalsIgnoreCase("FCK" ) ) {
      // Create FCK as editor
      String configScript =
        "FCKConfig.ToolbarSets[\"Default\"] = [\n" +
        FCKUtil.createFCKToolbarConfigString("Source","DocProps","-","Save","NewPage","Preview","-","Templates")+
        FCKUtil.createFCKToolbarConfigString("Cut","Copy","Paste","PasteText","PasteWord","-","Print","SpellCheck")+
        FCKUtil.createFCKToolbarConfigString("Undo","Redo","-","Find","Replace","-","SelectAll","RemoveFormat")+
//        FCKUtil.createFCKToolbarConfigString("Form","Checkbox","Radio","TextField","Textarea","Select","Button","ImageButton","HiddenField")+
        FCKUtil.createFCKConfigString("/")+",\n"+
        FCKUtil.createFCKToolbarConfigString("FontFormat","FontName","FontSize")+
        FCKUtil.createFCKToolbarConfigString("TextColor","BGColor")+
        FCKUtil.createFCKConfigString("/")+",\n"+
        FCKUtil.createFCKToolbarConfigString("Italic","Underline","StrikeThrough","-","Subscript","Superscript")+
        FCKUtil.createFCKToolbarConfigString("OrderedList","UnorderedList","-","Outdent","Indent","Blockquote")+
        FCKUtil.createFCKToolbarConfigString("JustifyLeft","JustifyCenter","JustifyRight","JustifyFull")+
        FCKUtil.createFCKToolbarConfigString("Link","Unlink","Anchor")+
        FCKUtil.createFCKToolbarConfigString("Image","Table","Rule","Smiley","SpecialChar","PageBreak","-","ShowBlocks")+
        "];\n" +   
        "FCKConfig.ToolbarCanCollapse = false;\n"; 
      // Also possible actions (but not useful for GP:
      // "Style", "Flash"
//      LOGGER.info(configScript);
      htmlEditor = new JHTMLEditor(   
//          JHTMLEditor.setEditorImplementation(JHTMLEditor.HTMLEditorImplementation.FCKEditor),   
          JHTMLEditor.setCustomJavascriptConfiguration(configScript)
      );
      return htmlEditor;
    }

    if ( editorType.equalsIgnoreCase("TinyMCE" ) ) {
      // Create TinyMCE as editor
      final String configScript =   
          "theme_advanced_buttons1: &apos;bold,italic,underline,strikethrough,sub,sup,|,charmap,|,justifyleft,justifycenter,justifyright,justifyfull,|,hr,removeformat&apos;," +   
          "theme_advanced_buttons2: &apos;undo,redo,|,cut,copy,paste,pastetext,pasteword,|,search,replace,|,forecolor,backcolor,bullist,numlist,|,outdent,indent,blockquote,|,table&apos;," +   
          "theme_advanced_buttons3: &apos;&apos;," +   
          "theme_advanced_toolbar_location: &apos;top&apos;," +   
          "theme_advanced_toolbar_align: &apos;left&apos;," +   
          // Language can be configured when language packs are added to the classpath. Language packs can be found here: http://tinymce.moxiecode.com/download_i18n.php   
//            "language: &apos;de&apos;," +   
          "plugins: &apos;table,paste&apos;";   
 
        htmlEditor = new JHTMLEditor(   
            JHTMLEditor.setEditorImplementation(JHTMLEditor.HTMLEditorImplementation.TinyMCE),   
            JHTMLEditor.setCustomJavascriptConfiguration(configScript)
        );
        return htmlEditor;
      }
    throw new UnsupportedOperationException("Unknown editor type to create JHTMLEditor: "+editorType);
  }
}
