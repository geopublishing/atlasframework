package org.geopublishing.geopublisher.swing;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import de.schmitzm.io.IOUtil;
import de.schmitzm.testing.TestingClass;

public class HTMLSourceReplaceTest extends TestingClass {

	@Test
	public void testHTMLSourceReplace() throws Exception {
		String mapBasePath = "E:/Arbeit/Stefan Krüger/Impetus Atlas (2.0)/Daten/ChartDemoAtlas/ChartDemoAtlas/ad/html/map_01357691812/";
		String relImageFolder = "images";
		String htmlContentSource = "<p>The term <b>digital divide</b>&nbsp;refers to the gap between people with effective access to <a title=\"Digital\" href=\"browser://http://en.wikipedia.org/wiki/Digital\">digital</a>&nbsp; </p>\r\n<center><img height=\"480\" hspace=\"0\" width=\"480\" align=\"right\" vspace=\"5\" style=\"float: right\" alt=\"\" src=\"file:/E:/Arbeit/Stefan%20Krüger/Impetus%20Atlas%20(2.0)/Daten/ChartDemoAtlas/ChartDemoAtlas/ad/html/map_01357691812/images/digital-divide2.jpg\" /> </center>\r\n<h2>Introduction</h2>\r\n<p>In the 21st century, the emergence of the <a title=\"Knowledge society\" href=\"/wiki/Knowledge_society\">knowledge society</a>&nbsp;becomes pervasive. <img src=\"images/digital-divide1.jpg\"/> />";
		String htmlContentResultGoal = "<p>The term <b>digital divide</b>&nbsp;refers to the gap between people with effective access to <a title=\"Digital\" href=\"browser://http://en.wikipedia.org/wiki/Digital\">digital</a>&nbsp; </p>\r\n<center><img height=\"480\" hspace=\"0\" width=\"480\" align=\"right\" vspace=\"5\" style=\"float: right\" alt=\"\" src=\"images/digital-divide2.jpg\" /> </center>\r\n<h2>Introduction</h2>\r\n<p>In the 21st century, the emergence of the <a title=\"Knowledge society\" href=\"/wiki/Knowledge_society\">knowledge society</a>&nbsp;becomes pervasive. <img src=\"images/digital-divide1.jpg\"/> />";
		String htmlContentResult = htmlContentSource;

		Map<String,File> replaceRefs = GpSwingUtil.findFileReferencesToReplace(htmlContentSource);
		for (String refURL : replaceRefs.keySet()) {
		  File refFile = replaceRefs.get(refURL);
		  String relFilePath = relImageFolder + "/" + refFile.getName();
		  htmlContentResult = htmlContentResult.replace(refURL,
		      relFilePath);
		}
		System.out.println("Source html:\n" + htmlContentSource);
		System.out.println();
		System.out.println("Result html:\n" + htmlContentResult);
		System.out.println();
		System.out.println("Should be:\n" + htmlContentResultGoal);
		System.out.println();
		assertEquals("HTML result does not match.", htmlContentResultGoal,
				htmlContentResult);
	}
}
