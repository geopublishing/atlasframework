package org.geopublishing.atlasViewer.swing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.geopublishing.atlasViewer.GpCoreUtil;

import de.schmitzm.io.IOUtil;

public class AvUtil extends GpCoreUtil{

	/**
	 * This method fixes an error where the bg-color value was not set correctly and therefore the lobo renderer failed
	 * to parse it
	 * 
	 * @return fixed Html
	 */
	public static String fixBrokenBgColor(String oldHtml) {
		return oldHtml.replaceAll("color\\s?=\\s?[\",']([0-9,a-f,A-F]{6})[\",']", "#$1");
	}

	/**
	 * This method fixes an error where the bg-color value was not set correctly and therefore the lobo html renderer
	 * failed to parse it
	 * 
	 * @return
	 */
	public static boolean fixBrokenBgColor(File oldHtml) throws IOException {

		final String unfixed = IOUtil.readFileAsString(oldHtml);
		final String fixed = fixBrokenBgColor(unfixed);

		if (!fixed.equals(unfixed)) {
			// SVN friendyl only write the file if the is a change
			FileUtils.writeStringToFile(oldHtml, fixed);
			return true;
		}
		return false;
	}

}
