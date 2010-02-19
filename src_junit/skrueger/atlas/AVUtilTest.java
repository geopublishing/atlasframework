/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.atlas;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

public class AVUtilTest extends TestCase {
	
	public void testCacheEPSG() throws InterruptedException, InvocationTargetException {
		final JFrame frame = new JFrame();
		frame.setContentPane(new JLabel("sdsd"));
		frame.pack();
		frame.setVisible(true);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				try {
					AVUtil.cacheEPSG(frame);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	
	
	/**
	 * If an URL points to the folder of atlas.jar or atlas.gpa, we expect these files NOT to be inside a jar, but 
	 * @throws MalformedURLException
	 */
	public void testLaunchPDFViewerOnline() throws MalformedURLException {
		URL pdfUrl;
		Exception result;
//		
//		// From atlas preview
//		pdfUrl = new URL("file:/home/stefan/EigeneDateien/Desktop/GP/Atlanten/IIDA2/IIDA2%20Arbeitskopie/ad/html/about/../../../impetus_atlas_du_maroc_fr.pdf");
//		result = AVUtil.launchPDFViewer(null, pdfUrl, "testing to open a pdf from the atlas preview.");
//		assertNull(result);
//		
//		pdfUrl = new URL("jar:http://www.geopublishing.org/iida2.5/atlas_resources.jar!/impetus_atlas_benin_foreword_en.pdf");
//		result = AVUtil.launchPDFViewer(null, pdfUrl, "testing to open a pdf withing a JAR.");
//		assertNull(result);
//
//		pdfUrl = new URL("jar:http://www.geopublishing.org/iida2.5/atlas_resources.jar!/ad/html/about/../../../impetus_atlas_benin_foreword_en.pdf");
//		result = AVUtil.launchPDFViewer(null, pdfUrl, "testing to open a pdf withing a JAR described using with \"../..\".");
//		assertNull(result);
	}
}
