/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.dp.layer;

import java.net.URL;

import org.apache.log4j.Logger;
import org.geotools.styling.Style;
import org.opengis.filter.Filter;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.i8n.Translation;
import skrueger.sld.ASUtil;

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
	 *         Kr&uuml;ger</a>
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
	 *         Kr&uuml;ger</a>
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
