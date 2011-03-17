package org.geopublishing.geopublisher.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.JPanel;
import org.geopublishing.geopublisher.swing.FCKUtil;

/**
 * A html editor based on SimplyHTML. 
 */
public class HTMLEditPaneJHTMLEditor extends JPanel implements HTMLEditPaneInterface {
  // contains a JHTMLEditor for each (language) page
  // to edit
  private JTabbedPane tabs = null;
  
  private final Logger LOGGER = LangUtil.createLogger(this);

  protected String editorType = null;
  
  
  /**
   * Creates a new editor based on {@link JWebBrowser} with FCK.
   */
  public HTMLEditPaneJHTMLEditor() {
    this(null);
  }
  
  /**
   * Creates a new editor based on {@link JWebBrowser}.
   * @param editorType type of editor (currently only "FCK" is supported);
   *                   if <code>null</code> "FCK" is used
   */
  public HTMLEditPaneJHTMLEditor(String editorType) {
    super();
    if ( editorType == null )
      editorType = "FCK";
    NativeInterface.open();
    this.editorType = editorType;
    this.setLayout(new BorderLayout());
    this.tabs = new JTabbedPane();
    this.tabs.setTabPlacement(JTabbedPane.TOP);
    add(tabs,BorderLayout.CENTER);
    setPreferredSize( new Dimension(800,500) );
  }
  
  protected JHTMLEditor createJHTMLEditor(String editorType) {
    JHTMLEditor htmlEditor = null;
    
    if ( editorType.equalsIgnoreCase("FCK" ) ) {
      // Create FCK as editor
      final String configScript =
        "FCKConfig.ToolbarSets[\"Default\"] = [\n" +
        FCKUtil.createFCKToolbarConfigString("Source","DocProps","-","Save","NewPage","Preview","-","Templates")+
        FCKUtil.createFCKToolbarConfigString("Cut","Copy","Paste","PasteText","PasteWord","-","Print","SpellCheck")+
        FCKUtil.createFCKToolbarConfigString("Undo","Redo","-","Find","Replace","-","SelectAll","RemoveFormat")+
        FCKUtil.createFCKToolbarConfigString("Form","Checkbox","Radio","TextField","Textarea","Select","Button","ImageButton","HiddenField")+
        FCKUtil.createFCKConfigString("/")+",\n"+
        FCKUtil.createFCKToolbarConfigString("Style","FontFormat","FontName","FontSize")+
        FCKUtil.createFCKToolbarConfigString("TextColor","BGColor")+
        FCKUtil.createFCKConfigString("/")+",\n"+
        FCKUtil.createFCKToolbarConfigString("Italic","Underline","StrikeThrough","-","Subscript","Superscript")+
        FCKUtil.createFCKToolbarConfigString("OrderedList","UnorderedList","-","Outdent","Indent","Blockquote")+
        FCKUtil.createFCKToolbarConfigString("JustifyLeft","JustifyCenter","JustifyRight","JustifyFull")+
        FCKUtil.createFCKToolbarConfigString("Link","Unlink","Anchor")+
        FCKUtil.createFCKToolbarConfigString("Image","Flash","Table","Rule","Smiley","SpecialChar","PageBreak","-","ShowBlocks")+
        "];\n" +   
        "FCKConfig.ToolbarCanCollapse = false;\n";          
      htmlEditor = new JHTMLEditor(   
          JHTMLEditor.setEditorImplementation(JHTMLEditor.HTMLEditorImplementation.FCKEditor),   
          JHTMLEditor.setCustomJavascriptConfiguration(configScript)
      );
  //    htmlEditor = new JHTMLEditor();
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

  /**
   * Returns {@code this}.
   */
  @Override
  public JComponent getComponent() {
    return this;
  }

  /**
   * Returns {@code true}, because {@link JWebBrowser} already provides
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
    JHTMLEditor newEditor = createJHTMLEditor(editorType);
    newEditor.setHTMLContent(IOUtil.readURLasString(url));
    tabs.addTab(title, newEditor);
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
    tabs.removeAll();
  }
  
  /**
   * Called when surrounded window/dialog/application is closed.
   * Should perform editor specific actions (e.g. save operation).
   * @param source object which initiates the closing 
   */
  @Override
  public boolean performClosing(Object source) {
    return true;
  }
}
