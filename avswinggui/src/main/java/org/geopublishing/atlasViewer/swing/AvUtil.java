package org.geopublishing.atlasViewer.swing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geopublishing.atlasViewer.GpCoreUtil;

public class AvUtil extends GpCoreUtil{
	
	/**
	 * This method fixes an error where the bg-color value was not set 
	 * correctly and therefore our html renderer failed to parse it
	 * 
	 * @return fixed Html
	 */
	public String fixBrokenBgColor(String oldHtml){
		Matcher m = Pattern.compile(".*<body bgcolor=\"([^#].*)\">").matcher(oldHtml);
		System.out.println(m.toString());
		
		return m.group(0);
	}

}
