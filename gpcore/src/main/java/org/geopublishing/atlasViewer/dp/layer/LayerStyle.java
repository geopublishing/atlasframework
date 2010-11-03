/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.dp.layer;

import java.net.URL;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geotools.styling.Style;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.i8n.Translation;

public class LayerStyle {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private String filename;

	private Translation title;

	private Translation desc;
	
	/** This filter is SUPPOSED to define which geometries will not be styled and not labelled not be part of the statistics. **/
	private Filter filter = Filter.INCLUDE;

	private final DpLayer dpLayer;

	private Style style;

	public LayerStyle(String filename, Translation name, Translation desc,
			DpLayer dpLayer) {
		this.filename = filename;
		this.title = name;
		this.desc = desc;
		this.dpLayer = dpLayer;
		
		LOGGER.debug("Creating new LayerStyle with "+filename+" "+title);
	}

	public Translation getDesc() {
		return desc;
	}

	public Translation getTitle() {
		return title;
	}

	public void setDesc(Translation desc) {
		this.desc = desc;
	}

	public void setTitle(Translation name) {
		this.title = name;
	}

	/**
	 * The {@link #getFilename()} is used as an ID
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public String getID() {
		return filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Force a reload of the style
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public void uncache() {
		style = null;
	}

	/**
	 * Override the toString() to make debugging easier.
	 */
	@Override
	public String toString() {
		return getTitle().toString();
	}

	public Style getStyle() {
		if (style == null) {
			try {
				URL url = dpLayer.getUrl();

				URL parentURL = IOUtil.getParentUrl(url);
				final URL styleUrl = IOUtil.extendURL(parentURL, getFilename());

				style = StylingUtil.loadSLD(styleUrl)[0];
				
				// Correcting any wrongly upper/lowercased attribute names
				if (dpLayer instanceof DpLayerVectorFeatureSource) {
					style = StylingUtil.correctPropertyNames(style, ((DpLayerVectorFeatureSource)dpLayer).getSchema());
				} else {
					style = StylingUtil.correctPropertyNames(style, null);
				}

			} catch (Exception e) {
				ExceptionDialog.show(null, new AtlasException(
						"Style could not be loaded. File " + getFilename()
								+ " is missing?\nUsing default style.", e));
				// Returning the default style
				return dpLayer.getStyle();
			}

		}

		return style;
	}

	/**
	 * Setting the Style does not automatically save the Style to a file. It
	 * only overwrites whatever has been loaded before.
	 * 
	 * @param newStyle
	 *            Style to set.
	 */
	public void setStyle(Style newStyle) {
		style = newStyle;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public Filter getFilter() {
		return filter;
	}

}
