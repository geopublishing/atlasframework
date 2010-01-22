/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
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
package skrueger.sld.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeatureType;

import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.sld.SingleRuleList;

public class SymbolButton extends JButton {

	final ImageIcon image;

	private BufferedImage bImage;

	// public static LegendIconFeatureRenderer renderer = new
	// LegendIconFeatureRenderer();

	public SymbolButton(SingleRuleList singleSymbolRuleList,
			SimpleFeatureType featureTyp) {
		this(singleSymbolRuleList, AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	public SymbolButton(SingleRuleList singleSymbolRuleList, Dimension size) {
		bImage = singleSymbolRuleList.getImage(size);
		System.out
				.println("Creating a button for "
						+ singleSymbolRuleList.getSymbolizers().size()
						+ " symbolizers");
		image = new ImageIcon(bImage);
		init();
	}

	public SymbolButton(Symbolizer symbolizer, SimpleFeatureType featureType) {
		image = new ImageIcon(ASUtil
				.getSymbolizerImage(symbolizer, featureType));
		init();
	}

	private void init() {
		setIcon(image);
		setContentAreaFilled(false);
		setIconTextGap(0);
		setSize(new Dimension(image.getIconWidth(), image.getIconHeight()));

	}

	@Override
	public Icon getIcon() {
		return image;
	}

	@Override
	public void paint(Graphics g) {
		// super.paint(g);
		g.drawImage(bImage, 0, 0, null);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		g.drawRect(1, 1, bImage.getWidth() - 1, bImage.getHeight() - 1);
		g.setColor(Color.WHITE);
		g.drawString(getText(), 3, bImage.getHeight() - 3);
	}

}
