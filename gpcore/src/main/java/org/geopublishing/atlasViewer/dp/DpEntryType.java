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
package org.geopublishing.atlasViewer.dp;

import java.util.Comparator;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.swing.Icons;

/**
 * An {@link Enum} with all file types that can be put into the {@link DataPool}
 * 
 */
public enum DpEntryType {
	UNKNOWN, PDF, GML, PICTURE, RASTER, VECTOR, VIDEO, RASTER_GEOTIFF, RASTER_ARCASCII, RASTER_IMAGEWORLD, VECTOR_SHP_POINT, VECTOR_SHP_LINE, VECTOR_SHP_POLY;

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

		} else if (type == DpEntryType.PICTURE) {
			icon = Icons.ICON_PICTURE_SMALL;

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

		} else if (type == DpEntryType.PICTURE) {
			icon = Icons.ICON_PICTURE_BIG;

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
		return GpCoreUtil.R("DpEntryType." + type.toString() + ".line1");

		// String line1 = null;
		// if (type == DpEntryType.VIDEO) {
		// line1 = "Video";
		//
		// } else if (type == DpEntryType.RASTER) {
		// line1 = "Raster";
		//
		// } else if (type == DpEntryType.RASTER_ARCASCII) {
		// line1 = "Raster";
		//
		// } else if (type == DpEntryType.RASTER_GEOTIFF) {
		// line1 = "Raster";
		//
		// } else if (type == DpEntryType.RASTER_IMAGEWORLD) {
		// line1 = "Raster";
		//
		// } else if (type == DpEntryType.VECTOR) {
		// line1 = "Vector";
		//
		// } else if (type == DpEntryType.VECTOR_SHP_POINT
		// || type == DpEntryType.VECTOR_SHP_LINE
		// || type == DpEntryType.VECTOR_SHP_POLY) {
		// line1 = "Shape";
		//
		// } else if (type == DpEntryType.GML) {
		// line1 = "GML";
		//
		// } else if (type == DpEntryType.PDF) {
		// line1 = "PDF";
		//
		// } else {
		// line1 = "can't";
		// }
		//
		// if (line1 == null) {
		// Logger.getLogger(DpEntryType.class).warn(
		// "No line1 for DpEntryType " + type);
		// line1 = "";
		// }
		// return line1;
	}

	/**
	 * Provides an additional detail for this {@link DpEntryType}
	 */
	public static String getLine2For(DpEntryType type) {
		return GpCoreUtil.R("DpEntryType." + type.toString() + ".line2");
		// String line2 = null;
		// if (type == DpEntryType.VIDEO) {
		//
		// } else if (type == DpEntryType.RASTER) {
		//
		// } else if (type == DpEntryType.RASTER_ARCASCII) {
		// line2 = "ArcASCII";
		//
		// } else if (type == DpEntryType.RASTER_GEOTIFF) {
		// line2 = "GeoTIFF";
		//
		// } else if (type == DpEntryType.RASTER_IMAGEWORLD) {
		// line2 = "world";
		//
		// } else if (type == DpEntryType.VECTOR) {
		//
		// } else if (type == DpEntryType.VECTOR_SHP_POINT) {
		// line2 = "point";
		// } else if (type == DpEntryType.VECTOR_SHP_LINE) {
		// line2 = "line";
		// } else if (type == DpEntryType.VECTOR_SHP_POLY) {
		// line2 = "poly";
		//
		// } else if (type == DpEntryType.GML) {
		//
		// } else if (type == DpEntryType.PDF) {
		//
		// } else {
		// line2 = "read!";
		// }
		//
		// if (line2 == null) {
		// line2 = "";
		// }
		// return line2;
	}

	/**
	 * @return A localized one-line description of that {@link DpEntryType}
	 */
	public String getDesc() {
		return getLine1() + " " + getLine2();
	}

	/**
	 * @return a Comparator that compares the textual descriptions of the types,
	 *         that the compared {@link DpEntry}s have. Usefull to sort a table
	 *         row that returns {@link DpEntry}s.
	 */
	public static Comparator<DpEntry<?>> getComparatorForDpe() {
		return new Comparator<DpEntry<?>>() {

			@Override
			public int compare(DpEntry<?> t1, DpEntry<?> t2) {
				if (t1 == null && t2 == null)
					return 0;
				return t1.getType().getDesc().compareTo(t2.getType().getDesc());
			}
		};
	}
}
