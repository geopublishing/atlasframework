package org.geopublishing.geopublisher.swing;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Ignore;
import org.junit.Test;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;


public class JHTMLEditorTest extends TestingClass {
  
  @Test
  @Ignore
  public void testJHTMLEditor() throws Exception {
    if ( !hasGui() )
      return;
    if ( !isInteractive() )
      return;
    
    UIUtils.setPreferredLookAndFeel();   
    NativeInterface.open();
    
    SwingUtilities.invokeAndWait( new Runnable() {
      public void run() {
        try {
          JHTMLEditor editor = new JHTMLEditor();
          
          editor.setPreferredSize( new Dimension(600,500) );

          JDialog dialog = new JDialog();
          dialog.setModal(true);
          dialog.getContentPane().add(editor);
          dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
          dialog.pack();
          dialog.setVisible( true );
        } catch (Throwable err) {
          err.printStackTrace();
        }
      }
    });
    
  }
}
