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
package org.geopublishing.atlasStyler.rulesLists;

import java.awt.Color;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.styling.LineSymbolizer;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.LineString;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;

public class SingleLineSymbolRuleList extends SingleRuleList<LineSymbolizer> {

	protected org.apache.log4j.Logger LOGGER = LangUtil.createLogger(this);

	public SingleLineSymbolRuleList(String title) {
		super(RulesListType.SINGLE_SYMBOL_LINE, title, GeometryForm.LINE);
	}

	public SingleLineSymbolRuleList(Translation title) {
		super(RulesListType.SINGLE_SYMBOL_LINE, title, GeometryForm.LINE);
	}

	@Override
	public void addNewDefaultLayer() {
		addSymbolizer(ASUtil.createDefaultSymbolizer(getGeometryDescriptor()));
	}

	/**
	 * Clones this {@link SingleLineSymbolRuleList}
	 * 
	 * @param copyListeners
	 *            If <code>true</code> the listeners are copied also.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
		return FeatureUtil.createFeatureType(LineString.class).getGeometryDescriptor();
	}

	@Override
	public String getLayerTypeDesc(int idx) {
		return getSymbolizers().get(idx).getClass().getSimpleName();
	}

	/**
	 * return <code>null</code> if not rotatable graphicStroke is used. Return
	 * 0. if one is used but it is not rotated.
	 */
	@Override
	public Double getRotation() {
		for (LineSymbolizer ls : getSymbolizers()) {
			if (ls.getStroke() == null)
				continue;
			if (ls.getStroke().getGraphicStroke() == null)
				continue;
			Expression rotation = ls.getStroke().getGraphicStroke().getRotation();
			return rotation != null ? Double.valueOf(rotation.toString()) : 0.;
		}
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
	public boolean hasRotation() {
		return getRotation() != null;
	}

	/**
	 * The {@link Color} returned by {@link #getColor()} is replaced against the
	 * given color paramter. Any other occurence of the original color will also
	 * be replaced.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public void setColor(Color newColor) {

		for (LineSymbolizer ps : getSymbolizers()) {

			StylingUtil.replaceLineSymbolizerColor(ps, getColor(), newColor);

		}
		fireEvents(new RuleChangedEvent("setColor to " + newColor, this));
	}

	@Override
	public void setRotation(Double rot) {
		pushQuite();
		try {
			for (LineSymbolizer ls : getSymbolizers()) {

				if (ls.getStroke() == null)
					continue;

				if (ls.getStroke().getGraphicStroke() != null) {

					Double dif = rot - getRotation();

					Double newRot = (Double.valueOf(ls.getStroke().getGraphicStroke().getRotation().toString()) + dif) % 360;
					ls.getStroke().getGraphicStroke().setRotation(ASUtil.ff2.literal(newRot));
				}
				fireEvents(new RuleChangedEvent("setRotation to " + rot, this));
			}
		} finally {
			popQuite();
		}
	}

	@Override
	public void setSizeBiggest(Float newMax) {

		if (getSizeBiggest() < 0f)
			return;
		if (getSizeBiggest() == newMax)
			return;

		Float factor = newMax / getSizeBiggest();

		for (LineSymbolizer ls : getSymbolizers()) {

			if (ls.getStroke() == null)
				continue;

			if (ls.getStroke().getGraphicStroke() != null) {
				Expression size = ls.getStroke().getGraphicStroke().getSize();
				if (size != null) {
					float newSize = Float.valueOf(size.toString()) * factor;
					ls.getStroke().getGraphicStroke().setSize(ASUtil.ff2.literal(newSize));
				}

			} else {

				Expression width = ls.getStroke().getWidth();
				if (width != null) {
					float newSize = Float.valueOf(width.toString()) * factor;
					ls.getStroke().setWidth(ASUtil.ff2.literal(newSize));
				}
			}

		}

		fireEvents(new RuleChangedEvent("SetWidth to " + newMax, this));
	}

}
