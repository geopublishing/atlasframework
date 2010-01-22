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
package skrueger.atlas.dp;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import skrueger.atlas.resource.icons.Icons;

/**
 * An {@link Enum} with all file types that can be put into the {@link DataPool}
 * 
 */
public enum DpEntryType {
	UNKNOWN, PDF, GML, RASTER, VECTOR, VIDEO, RASTER_PYRAMID, RASTER_GEOTIFF, RASTER_ARCASCII, RASTER_IMAGEWORLD, VECTOR_SHP_POINT, VECTOR_SHP_LINE, VECTOR_SHP_POLY;

	/**
	 * Provides a small icon this {@link DpEntryType}
	 */
	public ImageIcon getIconSmall() {
		return getIconSmallFor(this);
	}

	/**
	 * Provides a big icon this {@link DpEntryType}
	 */
	public ImageIcon getIconBig() {
		return getIconBigFor(this);
	}

	/**
	 * Provides a small icon this {@link DpEntryType}
	 */
	public static ImageIcon getIconSmallFor(DpEntryType type) {
		ImageIcon icon;
		if (type == DpEntryType.VIDEO) {
			icon = Icons.ICON_VIDEO_SMALL;

		} else if (type == DpEntryType.RASTER) {
			icon = Icons.ICON_RASTER_SMALL;

		} else if (type == DpEntryType.RASTER_ARCASCII) {
			icon = Icons.ICON_RASTER_SMALL;

		} else if (type == DpEntryType.RASTER_GEOTIFF) {
			icon = Icons.ICON_RASTER_SMALL;

		} else if (type == DpEntryType.RASTER_IMAGEWORLD) {
			icon = Icons.ICON_RASTER_SMALL;

		} else if (type == DpEntryType.RASTER_PYRAMID) {
			icon = Icons.ICON_RASTER_SMALL;

		} else if (type == DpEntryType.VECTOR) {
			icon = Icons.ICON_VECTOR_SMALL;

		} else if (type == DpEntryType.VECTOR_SHP_POINT) {
			icon = Icons.ICON_VECTOR_SMALL_POINT;

		} else if (type == DpEntryType.VECTOR_SHP_LINE) {
			icon = Icons.ICON_VECTOR_SMALL_LINE;

		} else if (type == DpEntryType.VECTOR_SHP_POLY) {
			icon = Icons.ICON_VECTOR_SMALL_POLY;

		} else if (type == DpEntryType.GML) {
			icon = Icons.ICON_VECTOR_SMALL;

		} else if (type == DpEntryType.PDF) {
			icon = Icons.ICON_PDF_SMALL;

		} else {
			icon = Icons.ICON_UNKOWN_SMALL;
		}

		if (icon == null) {
			Logger.getLogger(DpEntryType.class).warn(
					"No icon for DpEntryType " + type);
		}
		return icon;
	}

	/**
	 * Provides a big icon this {@link DpEntryType}
	 */
	public static ImageIcon getIconBigFor(DpEntryType type) {
		ImageIcon icon;
		if (type == DpEntryType.VIDEO) {
			icon = Icons.ICON_VIDEO_BIG;

		} else if (type == DpEntryType.RASTER) {
			icon = Icons.ICON_RASTER_BIG;

		} else if (type == DpEntryType.RASTER_ARCASCII) {
			icon = Icons.ICON_RASTER_BIG;

		} else if (type == DpEntryType.RASTER_GEOTIFF) {
			icon = Icons.ICON_RASTER_BIG;

		} else if (type == DpEntryType.RASTER_IMAGEWORLD) {
			icon = Icons.ICON_RASTER_BIG;

		} else if (type == DpEntryType.RASTER_PYRAMID) {
			icon = Icons.ICON_RASTER_BIG;

		} else if (type == DpEntryType.VECTOR) {
			icon = Icons.ICON_VECTOR_BIG;

		} else if (type == DpEntryType.VECTOR_SHP_POINT) {
			icon = Icons.ICON_VECTOR_BIG_POINT;

		} else if (type == DpEntryType.VECTOR_SHP_LINE) {
			icon = Icons.ICON_VECTOR_BIG_LINE;

		} else if (type == DpEntryType.VECTOR_SHP_POLY) {
			icon = Icons.ICON_VECTOR_BIG_POLY;

		} else if (type == DpEntryType.GML) {
			icon = Icons.ICON_VECTOR_BIG;

		} else if (type == DpEntryType.PDF) {
			icon = Icons.ICON_PDF_BIG;

		} else {
			icon = Icons.ICON_UNKOWN_BIG;
		}

		if (icon == null) {
			Logger.getLogger(DpEntryType.class).warn(
					"No icon for DpEntryType " + type);
		}
		return icon;
	}

	/**
	 * Provides an short name for this {@link DpEntryType}
	 */
	public String getLine1() {
		return getLine1For(this);
	}

	/**
	 * Provides an additional detail for this {@link DpEntryType}
	 */
	public String getLine2() {
		return getLine2For(this);
	}

	/**
	 * Provides an short name for this {@link DpEntryType}
	 */
	public static String getLine1For(DpEntryType type) {
		String line1 = null;
		if (type == DpEntryType.VIDEO) {
			line1 = "Video"; // i8n

		} else if (type == DpEntryType.RASTER) {
			line1 = "Raster"; // i8n

		} else if (type == DpEntryType.RASTER_ARCASCII) {
			line1 = "Raster"; // i8n

		} else if (type == DpEntryType.RASTER_GEOTIFF) {
			line1 = "Raster"; // i8n

		} else if (type == DpEntryType.RASTER_IMAGEWORLD) {
			line1 = "Raster"; // i8n

		} else if (type == DpEntryType.RASTER_PYRAMID) {
			line1 = "Raster"; // i8n

		} else if (type == DpEntryType.VECTOR) {
			line1 = "Vector"; // i8n

		} else if (type == DpEntryType.VECTOR_SHP_POINT
				|| type == DpEntryType.VECTOR_SHP_LINE
				|| type == DpEntryType.VECTOR_SHP_POLY) {
			line1 = "Shape"; // i8n

		} else if (type == DpEntryType.GML) {
			line1 = "GML"; // i8n

		} else if (type == DpEntryType.PDF) {
			line1 = "PDF"; // i8n

		} else {
			line1 = "can't";
		}

		if (line1 == null) {
			Logger.getLogger(DpEntryType.class).warn(
					"No line1 for DpEntryType " + type);
			line1 = "";
		}
		return line1;
	}

	/**
	 * Provides an additional detail for this {@link DpEntryType}
	 */
	public static String getLine2For(DpEntryType type) {
		String line2 = null;
		if (type == DpEntryType.VIDEO) {
			line2 = "Video"; // i8n

		} else if (type == DpEntryType.RASTER) {

		} else if (type == DpEntryType.RASTER_ARCASCII) {
			line2 = "ArcASCII"; // i8n

		} else if (type == DpEntryType.RASTER_GEOTIFF) {
			line2 = "GeoTIFF"; // i8n

		} else if (type == DpEntryType.RASTER_IMAGEWORLD) {
			line2 = "world"; // i8n

		} else if (type == DpEntryType.RASTER_PYRAMID) {
			line2 = "Pyramid"; // i8n

		} else if (type == DpEntryType.VECTOR) {

		} else if (type == DpEntryType.VECTOR_SHP_POINT) {
			line2 = "point"; // i8n
		} else if (type == DpEntryType.VECTOR_SHP_LINE) {
			line2 = "line"; // i8n
		} else if (type == DpEntryType.VECTOR_SHP_POLY) {
			line2 = "poly"; // i8n

		} else if (type == DpEntryType.GML) {

		} else if (type == DpEntryType.PDF) {

		} else {
			line2 = "read!"; // i8n //TODO
		}

		if (line2 == null) {
			line2 = "";
		}
		return line2;
	}

	/**
	 * @return A localized one-line description of that {@link DpEntryType}
	 */
	public String getDesc() {
		return getLine1() + " " + getLine2();
	}
}
