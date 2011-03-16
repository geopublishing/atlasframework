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
import java.awt.Stroke;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Polygon;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;

public class SinglePolygonSymbolRuleList extends
		SingleRuleList<PolygonSymbolizer> {
	protected final static Logger LOGGER = LangUtil
			.createLogger(SinglePolygonSymbolRuleList.class);

	public SinglePolygonSymbolRuleList(String title) {
		super(RulesListType.SINGLE_SYMBOL_POLYGON, title, GeometryForm.POLYGON);
	}

	public SinglePolygonSymbolRuleList(Translation title) {
		super(RulesListType.SINGLE_SYMBOL_POLYGON, title, GeometryForm.POLYGON);
	}

	@Override
	public void addNewDefaultLayer() {
		org.geotools.styling.Stroke stroke = ASUtil.createDefaultStroke();
		Fill fill = ASUtil.createDefaultFill();

		PolygonSymbolizer newPS = ASUtil.SB.createPolygonSymbolizer(stroke,
				fill);

		addSymbolizer(newPS);
	}

	/**
	 * Clones this {@link SinglePolygonSymbolRuleList}
	 * 
	 * @param copyListeners
	 *            If <code>true</code> the listeners are copied also.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public SinglePolygonSymbolRuleList clone(boolean copyListeners) {
		SinglePolygonSymbolRuleList clone = new SinglePolygonSymbolRuleList(getTitle());

		clone.getSymbolizers().clear();

		for (PolygonSymbolizer ps : layers) {

			Symbolizer clonedSymbolizer = StylingUtil.copy(ps);
			clone.addSymbolizer(clonedSymbolizer);
		}

		if (copyListeners) {
			for (RuleChangeListener rcl : getListeners()) {
				clone.addListener(rcl);
			}
		}

		return clone;

	}

	@Override
	public SingleRuleList<PolygonSymbolizer> copy() {
		SinglePolygonSymbolRuleList to = new SinglePolygonSymbolRuleList(getTitle());
		return copyTo(to);
	}

	/**
	 * @return The first Color used. Checks the {@link PolygonSymbolizer}s,
	 *         first {@link Fill} then {@link Stroke}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public Color getColor() {

		for (PolygonSymbolizer ps : getSymbolizers()) {

			Color foundColor = StylingUtil.getPolygonSymbolizerColor(ps);
			if (foundColor != null)
				return foundColor;
		}

		return null;
	}

	@Override
	public GeometryDescriptor getGeometryDescriptor() {
		return FeatureUtil.createFeatureType(Polygon.class)
				.getGeometryDescriptor();
	}

	@Override
	public String getLayerTypeDesc(int idx) {
		return getSymbolizers().get(idx).getClass().getSimpleName();
	}

	/** returns the Rotation if a GraphicFIll is used * */
	@Override
	public Double getRotation() {
		// TODO getRotation() all! :-)
		return 0.;
	}

	/** @return <code>0.</code> if no {@link Graphic}Fill is used * */
	@Override
	public Float getSizeBiggest() {
		Float biggestSize = -1f;
		for (PolygonSymbolizer ps : getSymbolizers()) {
			biggestSize = ASUtil.getBiggestSize(ps, biggestSize);
		}
		return biggestSize;
	}


	/**
	 * @return <code>true</code> if a rotatable element is used by these
	 *         {@link Rule}s. Rotatable {@link LineSymbolizer}s are not
	 *         inspected.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public boolean hasRotation() {

		for (PolygonSymbolizer ps : getSymbolizers()) {
			if (ps.getStroke() != null) {
				if (ps.getStroke().getGraphicStroke() != null)
					return true;
			}

			if (ps.getFill() != null) {
				if (ps.getFill().getGraphicFill() != null)
					return true;
			}

		}

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
	public void setColor(Color color) {

		for (PolygonSymbolizer ps : getSymbolizers()) {
			StylingUtil.replacePolygonSymbolizerColor(ps, getColor(), color);
		}

		fireEvents(new RuleChangedEvent("setColor to " + color, this));
	}

	/** Sets the rotation of any subelement where it makes sense * */
	@Override
	public void setRotation(Double rot) {
		// TODO setRotation all ;-)
		// fireEvents( new RuleChangedEvent("setRotation to "+rot, this) );
	}

	/** Sets the size of any subelement where it makes sense * */
	@Override
	public synchronized void setSizeBiggest(Float newMax) {
		if (getSizeBiggest() < 0f)
			return;
		if (getSizeBiggest().floatValue() == newMax.floatValue())
			return;

		Float factor = newMax / getSizeBiggest();
		LOGGER.debug("setSize Polygon by factor = " + factor + "for "
				+ newMax + "  / " + getSizeBiggest() + " ");

		for (PolygonSymbolizer ps : getSymbolizers()) {

			ASUtil.replacePolygonSymbolizerSize(ps, factor);

		}

		fireEvents(new RuleChangedEvent("setSize to " + newMax, this));
	}

}
