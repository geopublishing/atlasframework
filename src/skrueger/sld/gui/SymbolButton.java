/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
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
