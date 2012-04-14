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
package org.geopublishing.atlasStyler.swing;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeatureType;

public class SymbolButton extends JButton {

	// final ImageIcon image;

	private BufferedImage bImage;
	private final Dimension iconSize;
	SingleRuleList singleSymbolRuleList;
	final protected AtlasStylerVector asv;

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the default size
	 * {@link AtlasStylerVector#DEFAULT_SYMBOL_PREVIEW_SIZE}.
	 */
	public SymbolButton(AtlasStylerVector as, SingleRuleList singleSymbolRuleList) {
		this(as, singleSymbolRuleList,
				AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the given dimensions.
	 */
	public SymbolButton(AtlasStylerVector as, SingleRuleList singleSymbolRuleList, Dimension iconSize) {
		
		this.asv = as;

		this.iconSize = iconSize;
		// bImage = singleSymbolRuleList.getImage(iconSize);

		setSingleSymbolRuleList(singleSymbolRuleList);

		init();
	}

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link Symbolizer} applied to a given {@link SimpleFeatureType}. The
	 * button image will have the default size
	 * {@link AtlasStylerVector#DEFAULT_SYMBOL_PREVIEW_SIZE}.
	 */
	public SymbolButton(AtlasStylerVector asv, Symbolizer symbolizer, SimpleFeatureType featureType) {
		this(asv, symbolizer, featureType,
				AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link Symbolizer} applied to a given {@link SimpleFeatureType}. The
	 * button image will have the given dimensions.
	 */
	public SymbolButton(AtlasStylerVector asv, Symbolizer symbolizer, SimpleFeatureType featureType,
			Dimension iconSize) {
		this.asv = asv;
		
		this.iconSize = iconSize;
		bImage = ASUtil.getSymbolizerImage(symbolizer, iconSize, featureType);
		init();
	}

	private void init() {
		setIcon(new ImageIcon(bImage));
		setIconTextGap(0);
	}

	public void setSingleSymbolRuleList(SingleRuleList singleSymbolRuleList) {
		this.singleSymbolRuleList = singleSymbolRuleList;

		if (singleSymbolRuleList != null) {
			bImage = singleSymbolRuleList.getImage(iconSize);
		} else {
			bImage = new BufferedImage(iconSize.width, iconSize.height,
					BufferedImage.TYPE_INT_ARGB);
		}
		init();
		repaint();

	}

	public SingleRuleList getSingleSymbolRuleList() {
		return singleSymbolRuleList;
	}

}
