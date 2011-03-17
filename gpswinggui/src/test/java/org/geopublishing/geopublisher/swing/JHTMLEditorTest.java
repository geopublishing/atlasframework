package org.geopublishing.geopublisher.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Ignore;
import org.junit.Test;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;


public class JHTMLEditorTest extends TestingClass {
  
  @Test
//  @Ignore
  public void testJHTMLEditor() throws Exception {
    if ( !hasGui() )
      return;
    if ( !isInteractive() )
      return;
    
    UIUtils.setPreferredLookAndFeel();   
    NativeInterface.open();
    final JFrame dialog = new JFrame();
//    final JDialog dialog = new JDialog();
    SwingUtilities.invokeAndWait( new Runnable() {
      public void run() {
        try {
          JHTMLEditor editor = new JHTMLEditor();
          editor.setPreferredSize( new Dimension(600,500) );

//          dialog.setModal(true);
          dialog.getContentPane().add(editor, BorderLayout.CENTER);
          dialog.setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
          dialog.setLocationByPlatform(true);
          dialog.pack();
          dialog.setVisible( true );
        } catch (Throwable err) {
          err.printStackTrace();
        }
      }
    });
    NativeInterface.runEventPump();   
    while (dialog.isVisible()) {
//      LangUtil.sleepExceptionless(20);
    };
  }
}
