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
package org.geopublishing.atlasViewer.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.geopublishing.atlasViewer.swing.plaf.BasicMapLayerLegendPaneUI;

public class Icons {

	public static final ImageIcon ICON_EXPORT = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/export.png"));

	public static final ImageIcon ICON_VISIBLE = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/visible.png"));

	public static final ImageIcon ICON_HALFVISIBLE = new ImageIcon(
			BasicMapLayerLegendPaneUI.class
					.getResource("/icons/visible_half.png"));

	public static final ImageIcon ICON_NOTVISIBLE = new ImageIcon(
			BasicMapLayerLegendPaneUI.class
					.getResource("/icons/not_visible.png"));

	public static final ImageIcon ICON_REMOVE = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/remove.png"));

	public static final ImageIcon ICON_INFO = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/info.png"));

	public static final ImageIcon ICON_TOOL = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/tool.png"));

	public static final ImageIcon ICON_UPARROW = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/up_arrow.png"));

	public static final ImageIcon ICON_DOWNARROW = new ImageIcon(
			BasicMapLayerLegendPaneUI.class
					.getResource("/icons/down_arrow.png"));

	public static final ImageIcon ICON_RASTER = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/raster.png"));

	public static final ImageIcon ICON_VECTOR = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/vector.png"));

	public static final ImageIcon ICON_FILTER = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/filter.png"));

	public static final ImageIcon ICON_REMOVE_FILTER = new ImageIcon(
			BasicMapLayerLegendPaneUI.class
					.getResource("/icons/filter_remove.png"));

	public static final ImageIcon ICON_STYLE = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/style.png"));

	public static final ImageIcon ICON_TABLE = new ImageIcon(
			BasicMapLayerLegendPaneUI.class.getResource("/icons/table.png"));

	public static final ImageIcon ICON_ADD_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/add.png"));

	public static final ImageIcon ICON_DUPLICATE_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/duplicate.png"));

	public static final ImageIcon ICON_DIR_UP_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/back2.gif"));

	public static final ImageIcon ICON_DIR_UP_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/back.png"));

	public static final ImageIcon ICON_MAP_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/map.png"));

	public static final ImageIcon ICON_MAP_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/map.png"));

	public static final ImageIcon ICON_PDF_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/pdf.png"));

	public static final ImageIcon ICON_PDF_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/pdf.png"));

	public static final ImageIcon ICON_HTML_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/html.png"));

	public static final ImageIcon ICON_HTML_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/html.png"));

	public static final ImageIcon ICON_VIDEO_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/video.png"));

	public static final ImageIcon ICON_VIDEO_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/video.png"));

	public static final ImageIcon ICON_RASTER_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/raster.png"));

	public static final ImageIcon ICON_RASTER_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/raster.png"));

	public static final ImageIcon ICON_TASKRUNNING_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/taskrunning.png"));

	public static final ImageIcon ICON_TASKDONE_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/taskdone.png"));

	public static final ImageIcon ICON_OK_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/ok.png"));

	public static final ImageIcon ICON_CANCEL_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/cancel.png"));

	public static final ImageIcon ICON_EXIT_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/exit.png"));

	public static final ImageIcon ICON_SCREENSHOT_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/screenshot.png"));

	public static final ImageIcon ICON_VECTOR_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/vector.png"));

	public static final ImageIcon ICON_VECTOR_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/vector.png"));

	public static final ImageIcon ICON_VECTOR_SMALL_POINT = new ImageIcon(
			Icons.class.getResource("/icons/small/vector_point.png"));

	public static final ImageIcon ICON_VECTOR_BIG_POINT = new ImageIcon(
			Icons.class.getResource("/icons/big/vector_point.png"));

	public static final ImageIcon ICON_VECTOR_SMALL_LINE = new ImageIcon(
			Icons.class.getResource("/icons/small/vector_line.png"));

	public static final ImageIcon ICON_VECTOR_BIG_LINE = new ImageIcon(
			Icons.class.getResource("/icons/big/vector_line.png"));

	public static final ImageIcon ICON_VECTOR_BIG_POLY = new ImageIcon(
			Icons.class.getResource("/icons/big/vector_poly.png"));

	public static final ImageIcon ICON_VECTOR_SMALL_POLY = new ImageIcon(
			Icons.class.getResource("/icons/small/vector_poly.png"));

	public static final ImageIcon ICON_SELECTED = new ImageIcon(
			Icons.class.getResource("/icons/selected.png"));

	public static final ImageIcon ICON_FLAGS_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/flags.png"));

	public static final ImageIcon ICON_UNKOWN_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/error.png"));

	public static final ImageIcon ICON_UNKOWN_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/error.png"));

	public static final ImageIcon ICON_SEARCH = new ImageIcon(
			Icons.class.getResource("/icons/small/search.png"));

	public static final ImageIcon ICON_LOCAL = new ImageIcon(
			Icons.class.getResource("/icons/small/local.png"));

	public static final ImageIcon ICON_ONLINE = new ImageIcon(
			Icons.class.getResource("/icons/small/online.png"));

	public static final Icon ICON_CHART_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/chart.png"));

	public static final Icon ICON_CHART_MEDIUM = new ImageIcon(
			Icons.class.getResource("/icons/medium/chart.png"));

	public static final Icon ICON_CHART_BIG = new ImageIcon(
			Icons.class.getResource("/icons/big/chart.png"));

	public static final Icon ICON_PRINT_24 = new ImageIcon(
			Icons.class.getResource("/icons/small/printer1_20.png"));

	public static final Icon ICON_PRINT_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/printer1.png"));

	public static final Icon ICON_SAVEAS_24 = new ImageIcon(
			Icons.class.getResource("/icons/small/save_20.png"));

	public static final Icon ICON_SAVEAS_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/save.png"));

	// TODO Wrong icon
	public static final Icon ICON_MAPEXTEND_BBOX = new ImageIcon(
			Icons.class.getResource("/icons/small/mapExtendTool.png"));

	// TODO Wrong icon
	public static final Icon ICON_MAPEXTEND_BBOX_RESET = new ImageIcon(
			Icons.class.getResource("/icons/small/mapExtendToolReset.png"));

	public static final Icon ICON_DEFAULTMAPAREA_BBOX_RESET = new ImageIcon(
			Icons.class.getResource("/icons/small/mapExtendToolReset.png"));

	public static final ImageIcon AS_REVERSE_COLORORDER = new ImageIcon(
			Icons.class.getResource("/images/reverseColorOrder.gif"));

	public static final ImageIcon ICON_FONTS_SMALL = new ImageIcon(
			Icons.class.getResource("/icons/small/fonts.png"));

	private static ImageIcon upArrow;

	private static ImageIcon downArrow;

	/**
	 * @return A self-rendered Up-Arrow Icon at 10x10 pixel
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static Icon getUpArrowIcon() {
		if (upArrow == null) {
			BufferedImage image = new BufferedImage(10, 10,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			g2.setBackground(null);
			g2.setColor(Color.BLACK);
			g2.draw(new Line2D.Double(5, 1, 1, 9));
			g2.draw(new Line2D.Double(2, 9, 5, 3));
			g2.draw(new Line2D.Double(5, 3, 2, 9));
			g2.draw(new Line2D.Double(9, 9, 5, 1));
			upArrow = new ImageIcon(image);
		}
		return upArrow;
	}

	/**
	 * @return A self-rendered Down-Arrow Icon at 10x10 pixel
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static Icon getDownArrowIcon() {
		if (downArrow == null) {

			BufferedImage image = new BufferedImage(10, 10,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			g2.setBackground(null);
			g2.setColor(Color.GRAY);
			g2.draw(new Line2D.Double(4, 8, 0, 0));
			g2.draw(new Line2D.Double(1, 0, 4, 6));
			g2.draw(new Line2D.Double(4, 8, 1, 0));
			g2.draw(new Line2D.Double(8, 0, 4, 8));
			g2.setColor(Color.BLACK);
			g2.draw(new Line2D.Double(5, 9, 1, 1));
			g2.draw(new Line2D.Double(2, 1, 5, 7));
			g2.draw(new Line2D.Double(5, 7, 2, 1));
			g2.draw(new Line2D.Double(9, 1, 5, 9));
			downArrow = new ImageIcon(image);
		}
		return downArrow;
	}

}
