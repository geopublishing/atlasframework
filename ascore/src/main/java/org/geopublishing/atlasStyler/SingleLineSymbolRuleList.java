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
package org.geopublishing.atlasStyler;

import java.awt.Color;

import org.geotools.styling.LineSymbolizer;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.expression.Expression;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.LineString;

public class SingleLineSymbolRuleList extends SingleRuleList<LineSymbolizer> {


	protected org.apache.log4j.Logger LOGGER = ASUtil.createLogger(this);

	public SingleLineSymbolRuleList(String title) {
		super(title, GeometryForm.LINE);
	}

	public SingleLineSymbolRuleList(Translation title) {
		super(title, GeometryForm.LINE);
	}

	@Override
	public void addNewDefaultLayer() {
		addSymbolizer(ASUtil
				.createDefaultSymbolizer(getGeometryDescriptor()));
	}

	/**
	 * Clones this {@link SingleLineSymbolRuleList}
	 * 
	 * @param copyListeners
	 *            If <code>true</code> the listeners are copied also.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public SingleRuleList clone(boolean copyListeners) {
		SingleLineSymbolRuleList clone = new SingleLineSymbolRuleList(getTitle());
		copyTo(clone);
		return clone;
	}

	@Override
	public SingleRuleList<LineSymbolizer> copy() {
		return copyTo(new SingleLineSymbolRuleList(getTitle()));
	}

	@Override
	public Color getColor() {

		for (LineSymbolizer ps : getSymbolizers()) {

			Color foundColor = StylingUtil.getLineSymbolizerColor(ps);
			if (foundColor != null)
				return foundColor;
		}

		return null;
	}

	@Override
	public GeometryDescriptor getGeometryDescriptor() {
		return FeatureUtil.createFeatureType(LineString.class)
				.getGeometryDescriptor();
	}

	@Override
	public String getLayerTypeDesc(int idx) {
		return getSymbolizers().get(idx).getClass().getSimpleName();
	}

	@Override
	public Double getRotation() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This refers to the WIDTH of the line
	 */
	@Override
	public Float getSizeBiggest() {
		Float biggestSize = -1f;
		for (LineSymbolizer ps : getSymbolizers()) {
			biggestSize = ASUtil.getBiggestWidth(ps, biggestSize);
		}
		return biggestSize;
	}

	@Override
	public RulesListType getTypeID() {
		return RulesListType.SINGLE_SYMBOL_LINE;
	}

	@Override
	public boolean hasRotation() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * The {@link Color} returned by {@link #getColor()} is replaced against the
	 * given color paramter. Any other occurence of the original color will also
	 * be replaced.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public void setColor(Color newColor) {

		for (LineSymbolizer ps : getSymbolizers()) {

			StylingUtil.replaceLineSymbolizerColor(ps, getColor(), newColor);

		}
		fireEvents(new RuleChangedEvent("setColor to " + newColor, this));
	}

	@Override
	public void setRotation(Double size) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setSizeBiggest(Float newMax) {

		if (getSizeBiggest() < 0f)
			return;
		if (getSizeBiggest() == newMax)
			return;

		Float factor = newMax / getSizeBiggest();

		for (LineSymbolizer ps : getSymbolizers()) {

			if (ps.getStroke() == null)
				continue;

			Expression width = ps.getStroke().getWidth();
			if (width != null) {
				float newSize = Float.valueOf(width.toString()) * factor;

				ps.getStroke().setWidth(ASUtil.ff2.literal(newSize));
			}

			// TODO What about graphic lines etc...
		}

		fireEvents(new RuleChangedEvent("SetWidth to " + newMax, this));
	}

}
