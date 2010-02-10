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
package skrueger.atlas.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import rachel.http.loader.ClassResourceLoader;
import rachel.http.loader.WebResourceManager;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.atlas.map.Map;
import skrueger.creator.TestingUtil;

/**
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class WebServerTest extends TestCase {

	@Test
	@Ignore
	public void testAtlasMLXSDAccess() throws IOException {

		URL url = new URL(
				"http://localhost:7272/skrueger/atlas/resource/AtlasML.xsd");

		// Read all the text returned by the server
		final InputStream openStream = url.openStream();
		assertNotNull(openStream);

		BufferedReader in = new BufferedReader(
				new InputStreamReader(openStream));

		// In the fourth line the namespace is mentioned
		String str;
		str = in.readLine();
		str = in.readLine();
		str = in.readLine();
		str = in.readLine().trim();
		assertEquals("targetNamespace=\"http://www.wikisquare.de/AtlasML\"",
				str);

		in.close();
	}

	@Test
	public void testHTML() throws InterruptedException, IOException, AtlasException, FactoryException, TransformException, SAXException, ParserConfigurationException {

		WebResourceManager.addResourceLoader(new ClassResourceLoader(
				AtlasViewer.class));

		Map map = TestingUtil.getAtlasConfigE().getMapPool().get(0);
		assertNotNull(map);

		URL infoURL = map.getInfoURL();
		assertNotNull(infoURL);
		System.out.println(infoURL);
		assertEquals("http://127.0.0.1:" + Webserver.PORT
				+ "/ad/html/map_00005012843/index_de.html", infoURL.toString());

		// Testing the content of the HTML page (this test might only work in
		// english?!)
		final InputStream openStream = infoURL.openStream();
		assertNotNull(openStream);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(openStream));
		String str = in.readLine();
		str = in.readLine();
		str = in.readLine();
		str = in.readLine();
		in.close();
		assertNotNull(str);
		// System.out.println(str);
		assertEquals("Test HTML Desciption DE", str);

		// JFrame showaloneFrame = GuiAndTools.showalone( new HTMLInfoJPane(map)
		// );
		// Thread.sleep(1000);
		// showaloneFrame.dispose();

	}
//
//	public void testDispose() throws InterruptedException, AtlasFatalException, IOException{
//		Webserver ws = new Webserver(TestingUtil.INTERACTIVE);
//		Thread.sleep(100);
//		assert(Webserver.isRunning());
//		Webserver.dispose();
//		Thread.sleep(100);
//		assert(!Webserver.isRunning());
//	}
//
//	public void testDisposeALot() throws InterruptedException, AtlasFatalException, IOException{
//		assert(!Webserver.isRunning());
//		for (int i = 0; i<100; i++){
//			Webserver ws = new Webserver(TestingUtil.INTERACTIVE);
//			assert(Webserver.isRunning());
//		}
//		Thread.sleep(1000);
//		Webserver.dispose();
//		Thread.sleep(100);
//		assert(!Webserver.isRunning());
//	}
}
