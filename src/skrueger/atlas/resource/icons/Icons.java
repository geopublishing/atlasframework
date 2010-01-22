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
package skrueger.atlas.resource.icons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons {

	public static final ImageIcon ICON_ADD_SMALL = new ImageIcon(Icons.class
			.getResource("small/add.png"));

	public static final ImageIcon ICON_DUPLICATE_SMALL = new ImageIcon(
			Icons.class.getResource("small/duplicate.png"));

	public static final ImageIcon ICON_DIR_UP_BIG = new ImageIcon(Icons.class
			.getResource("big/back2.gif"));

	public static final ImageIcon ICON_DIR_UP_SMALL = new ImageIcon(Icons.class
			.getResource("small/back.png"));

	public static final ImageIcon ICON_MAP_BIG = new ImageIcon(Icons.class
			.getResource("big/map.png"));

	public static final ImageIcon ICON_MAP_SMALL = new ImageIcon(Icons.class
			.getResource("small/map.png"));

	public static final ImageIcon ICON_PDF_BIG = new ImageIcon(Icons.class
			.getResource("big/pdf.png"));

	public static final ImageIcon ICON_PDF_SMALL = new ImageIcon(Icons.class
			.getResource("small/pdf.png"));

	public static final ImageIcon ICON_HTML_BIG = new ImageIcon(Icons.class
			.getResource("big/html.png"));

	public static final ImageIcon ICON_HTML_SMALL = new ImageIcon(Icons.class
			.getResource("small/html.png"));

	public static final ImageIcon ICON_VIDEO_BIG = new ImageIcon(Icons.class
			.getResource("big/video.png"));

	public static final ImageIcon ICON_VIDEO_SMALL = new ImageIcon(Icons.class
			.getResource("small/video.png"));

	public static final ImageIcon ICON_RASTER_BIG = new ImageIcon(Icons.class
			.getResource("big/raster.png"));

	public static final ImageIcon ICON_RASTER_SMALL = new ImageIcon(Icons.class
			.getResource("small/raster.png"));

	public static final ImageIcon ICON_TASKRUNNING_BIG = new ImageIcon(
			Icons.class.getResource("big/taskrunning.png"));

	public static final ImageIcon ICON_TASKDONE_BIG = new ImageIcon(Icons.class
			.getResource("big/taskdone.png"));

	public static final ImageIcon ICON_OK_SMALL = new ImageIcon(Icons.class
			.getResource("small/ok.png"));

	public static final ImageIcon ICON_CANCEL_SMALL = new ImageIcon(Icons.class
			.getResource("small/cancel.png"));

	public static final ImageIcon ICON_EXIT_SMALL = new ImageIcon(Icons.class
			.getResource("small/exit.png"));

	public static final ImageIcon ICON_SCREENSHOT_SMALL = new ImageIcon(
			Icons.class.getResource("small/screenshot.png"));

	public static final ImageIcon ICON_VECTOR_BIG = new ImageIcon(Icons.class
			.getResource("big/vector.png"));

	public static final ImageIcon ICON_VECTOR_SMALL = new ImageIcon(Icons.class
			.getResource("small/vector.png"));

	public static final ImageIcon ICON_VECTOR_SMALL_POINT = new ImageIcon(
			Icons.class.getResource("small/vector_point.png"));

	public static final ImageIcon ICON_VECTOR_BIG_POINT = new ImageIcon(
			Icons.class.getResource("big/vector_point.png"));

	public static final ImageIcon ICON_VECTOR_SMALL_LINE = new ImageIcon(
			Icons.class.getResource("small/vector_line.png"));

	public static final ImageIcon ICON_VECTOR_BIG_LINE = new ImageIcon(
			Icons.class.getResource("big/vector_line.png"));

	public static final ImageIcon ICON_VECTOR_BIG_POLY = new ImageIcon(
			Icons.class.getResource("big/vector_poly.png"));

	public static final ImageIcon ICON_VECTOR_SMALL_POLY = new ImageIcon(
			Icons.class.getResource("small/vector_poly.png"));

	public static final ImageIcon ICON_SELECTED = new ImageIcon(Icons.class
			.getResource("selected.png"));

	public static final ImageIcon ICON_FLAGS_SMALL = new ImageIcon(Icons.class
			.getResource("small/flags.png"));

	public static final ImageIcon ICON_UNKOWN_BIG = new ImageIcon(Icons.class
			.getResource("big/error.png"));

	public static final ImageIcon ICON_UNKOWN_SMALL = new ImageIcon(Icons.class
			.getResource("small/error.png"));

	public static final ImageIcon ICON_SEARCH = new ImageIcon(Icons.class
			.getResource("small/search.png"));

	public static final ImageIcon ICON_LOCAL = new ImageIcon(Icons.class
			.getResource("small/local.png"));

	public static final ImageIcon ICON_ONLINE = new ImageIcon(Icons.class
			.getResource("small/online.png"));

	public static final Icon ICON_CHART_SMALL = new ImageIcon(Icons.class
			.getResource("small/chart.png"));

	public static final Icon ICON_CHART_BIG = new ImageIcon(Icons.class
			.getResource("big/chart.png"));

	public static final Icon ICON_PRINT_24 = new ImageIcon(Icons.class
			.getResource("small/printer1_20.png"));

	public static final Icon ICON_PRINT_SMALL = new ImageIcon(Icons.class
			.getResource("small/printer1.png"));

	public static final Icon ICON_SAVEAS_24 = new ImageIcon(Icons.class
			.getResource("small/save_20.png"));

	public static final Icon ICON_SAVEAS_SMALL = new ImageIcon(Icons.class
			.getResource("small/save.png"));

	// TODO Wrong icon
	public static final Icon ICON_MAPEXTEND_BBOX = new ImageIcon(Icons.class
			.getResource("small/mapExtendTool.png"));

	// TODO Wrong icon
	public static final Icon ICON_MAPEXTEND_BBOX_RESET = new ImageIcon(
			Icons.class.getResource("small/mapExtendToolReset.png"));

	private static ImageIcon upArrow;

	private static ImageIcon downArrow;

	/**
	 * @return A self-rendered Up-Arrow Icon at 10x10 pixel
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
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
