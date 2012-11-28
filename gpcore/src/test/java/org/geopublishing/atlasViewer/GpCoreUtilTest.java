package org.geopublishing.atlasViewer;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import de.schmitzm.testing.TestingClass;

public class GpCoreUtilTest extends TestingClass{

	@Test
	public void testStrangeStuff(){
		String current = "<p><img alt=\"mein text mit backslash \\\" vspace=\"1\" align=\"middle\" width=\"50\" height=\"30\" src=\"images\\a\\asdasd\\flagge_dt&#46;jpg\" /></p>";
		String expected = "<p><img alt=\"mein text mit backslash \\\" vspace=\"1\" align=\"middle\" width=\"50\" height=\"30\" src=\"images/a/asdasd/flagge_dt&#46;jpg\" /></p>";
		assertEquals(expected, convert(current));
	}

	@Test
	public void testMoreStrangeStuff(){
		String current =  "hallo\n<p><a\n href=\"foo\\bar.html\">Mein Pansen\\Labmagen-Filet</a></p>\n<!-- toller html comment --><p><a href=\"foo\\bar.html\">Mein Pansen\\Labmagen-Filet</a></p>";
		String expected = "hallo\n<p><a\n href=\"foo/bar.html\">Mein Pansen\\Labmagen-Filet</a></p>\n<!-- toller html comment --><p><a href=\"foo/bar.html\">Mein Pansen\\Labmagen-Filet</a></p>";
		assertEquals(expected, convert(current));
	}


	@Test
	public void testForBothHrefAndSrc(){
		String current = "<a alt=\"bla\\foo\" href=\"x\\y\" src=\"a\\b\">";
		String expected  = "<a alt=\"bla\\foo\" href=\"x/y\" src=\"a/b\">";
		assertEquals(expected, convert(current));
	}


	@Test
	public void testForSrc(){
		String current = "<img src=\"foo\\bar.jpg\" />";
		String expected = "<img src=\"foo/bar.jpg\" />";
		assertEquals(expected, convert(current));
	}

	@Test
	public void testForSrcWithIgnoreCase(){
		String current = "<img sRc=\"foo\\bar.jpg\" />";
		String expected = "<img sRc=\"foo/bar.jpg\" />";
		assertEquals(expected, convert(current));
	}

	@Test
	public void testForHref(){
		String current = "<img href=\"foo\\bar.jpg\" />";
		String expected = "<img href=\"foo/bar.jpg\" />";
		assertEquals(expected, convert(current));
	}

	@Test
	public void testForSrcTwoTimes(){
		String current = "<img src=\"foo\\bar.jpg\" /><img src=\"foo\\bar.jpg\" />";
		String expected = "<img src=\"foo/bar.jpg\" /><img src=\"foo/bar.jpg\" />";
		assertEquals(expected, convert(current));
	}



	@Test
	public void testNonAffectedInputOutsideTags(){
		assertEquals("<pre>\\</pre>hallo\n\n hi", convert("<pre>\\</pre>hallo\n\n hi"));
	}


	public String convert(String current){

		Pattern p = Pattern.compile("(<[^>]+(?:src|href)=\"[^\"]*)\\\\([^\"]*\"[^>]*>)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(current);

		while(m.find()){
			current = m.replaceAll("$1/$2");
			m = p.matcher(current);
		}

		return current;
	}

}
