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
/*
 * $Id$
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package skrueger.atlas.gui.plaf;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXTaskPane;

import skrueger.atlas.gui.MapLayerLegend;

/**
 * Paints the JXTaskPane with a gradient in the title bar.
 * 
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class GlossyMapLayerLegendPaneUI extends BasicMapLayerLegendPaneUI {

	public static ComponentUI createUI(JComponent c) {
		return new GlossyMapLayerLegendPaneUI();
	}

	@Override
	protected Border createPaneBorder() {
		return new GlossyPaneBorder();
	}

	/**
	 * Overriden to paint the background of the component but keeping the
	 * rounded corners.
	 */
	@Override
	public void update(Graphics g, JComponent c) {
		if (c.isOpaque()) {
			g.setColor(c.getParent().getBackground());
			g.fillRect(0, 0, c.getWidth(), c.getHeight());
			g.setColor(c.getBackground());
			g.fillRect(0, getRoundHeight(), c.getWidth(), c.getHeight()
					- getRoundHeight());
		}
		paint(g, c);
	}

	/**
	 * The border of the taskpane group paints the "text", the "icon", the
	 * "expanded" status and the "special" type.
	 * 
	 */
	class GlossyPaneBorder extends PaneBorder {

		@Override
		protected void paintTitleBackground(MapLayerLegend layerLegend,
				Graphics g) {
			if (layerLegend.isSpecial()) {
				g.setColor(specialTitleBackground);
				g.fillRoundRect(0, 0, layerLegend.getWidth(),
						getRoundHeight() * 2, getRoundHeight(),
						getRoundHeight());
				g.fillRect(0, getRoundHeight(), layerLegend.getWidth(),
						getTitleHeight(layerLegend) - getRoundHeight());
			} else {
				Paint oldPaint = ((Graphics2D) g).getPaint();
				GradientPaint gradient = new GradientPaint(0f, 0f, // group.getWidth()
						// / 2,
						titleBackgroundGradientStart, 0f, // group.getWidth(),
						getTitleHeight(layerLegend), titleBackgroundGradientEnd);

				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_COLOR_RENDERING,
						RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				((Graphics2D) g).setPaint(gradient);

				g.fillRoundRect(0, 0, layerLegend.getWidth(),
						getRoundHeight() * 2, getRoundHeight(),
						getRoundHeight());
				g.fillRect(0, getRoundHeight(), layerLegend.getWidth(),
						getTitleHeight(layerLegend) - getRoundHeight());
				((Graphics2D) g).setPaint(oldPaint);
			}

			g.setColor(borderColor);
			g.drawRoundRect(0, 0, layerLegend.getWidth() - 1,
					getTitleHeight(layerLegend) + getRoundHeight(),
					getRoundHeight(), getRoundHeight());
			g.drawLine(0, getTitleHeight(layerLegend) - 1, layerLegend
					.getWidth(), getTitleHeight(layerLegend) - 1);
		}

		@Override
		protected void paintExpandedControls(JXTaskPane mapLayerLegend,
				Graphics g, int x, int y, int width, int height) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			paintOvalAroundControls(mapLayerLegend, g, x, y, width, height);
			g.setColor(getPaintColor(mapLayerLegend));
			paintChevronControls(mapLayerLegend, g, x, y, width, height);

			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}

		@Override
		protected boolean isMouseOverBorder() {
			return true;
		}

	}

}
