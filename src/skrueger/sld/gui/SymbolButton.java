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

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.sld.SingleRuleList;

public class SymbolButton extends JButton {

	// final ImageIcon image;

	private BufferedImage bImage;
	private final Dimension iconSize;
	private SingleRuleList singleSymbolRuleList;

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the default size
	 * {@link AtlasStyler#DEFAULT_SYMBOL_PREVIEW_SIZE}.
	 */
	public SymbolButton(SingleRuleList singleSymbolRuleList) {
		this(singleSymbolRuleList, AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the given dimensions.
	 */
	public SymbolButton(SingleRuleList singleSymbolRuleList, Dimension iconSize) {

		this.iconSize = iconSize;
		// bImage = singleSymbolRuleList.getImage(iconSize);

		setSingleSymbolRuleList(singleSymbolRuleList);

		init();
	}

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link Symbolizer} applied to a given {@link SimpleFeatureType}. The
	 * button image will have the default size
	 * {@link AtlasStyler#DEFAULT_SYMBOL_PREVIEW_SIZE}.
	 */
	public SymbolButton(Symbolizer symbolizer, SimpleFeatureType featureType) {
		this(symbolizer, featureType, AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link Symbolizer} applied to a given {@link SimpleFeatureType}. The
	 * button image will have the given dimensions.
	 */
	public SymbolButton(Symbolizer symbolizer, SimpleFeatureType featureType,
			Dimension iconSize) {
		// image = new ImageIcon(ASUtil.getSymbolizerImage(symbolizer, iconSize,
		// featureType));

		this.iconSize = iconSize;
		bImage = ASUtil.getSymbolizerImage(symbolizer, iconSize, featureType);
		init();
	}

	private void init() {
		setIcon(new ImageIcon(bImage));
//		setContentAreaFilled(false);
		setIconTextGap(0);
//		setSize(iconSize);
	}

	// @Override
	// public Icon getIcon() {
	// return image;
	// }

	@Override
	public void paint(Graphics g) {
		 super.paint(g);
////		setIcon(new ImageIcon(bImage));
//		g.drawImage(bImage, 0, 0, null);
//		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
//		g.drawRect(1, 1, bImage.getWidth() - 1, bImage.getHeight() - 1);
//		g.setColor(Color.WHITE);
//		g.drawString(getText(), 3, bImage.getHeight() - 3);
	}

	public void setSingleSymbolRuleList(SingleRuleList singleSymbolRuleList) {
		this.singleSymbolRuleList = singleSymbolRuleList;
		
		if (singleSymbolRuleList != null) {
			bImage = singleSymbolRuleList.getImage(iconSize);
		} else {
			bImage = new BufferedImage(iconSize.width, iconSize.height, BufferedImage.TYPE_INT_ARGB);
		}
		init();
		repaint();
		
	}

	public SingleRuleList getSingleSymbolRuleList() {
		return singleSymbolRuleList;
	}

}
