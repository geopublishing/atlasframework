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

import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.expression.Expression;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.Point;

public class SinglePointSymbolRuleList extends SingleRuleList<PointSymbolizer> {

	public SinglePointSymbolRuleList(String title) {
		super(title);
	}

	public SinglePointSymbolRuleList(Translation title) {
		super(title);
	}

	/**
	 * Clones this {@link SinglePointSymbolRuleList}
	 * 
	 * @param copyListeners
	 *            If <code>true</code> the listeners are copied also.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public SinglePointSymbolRuleList clone(boolean copyListeners) {

		// Clone the Symbolizers
		SinglePointSymbolRuleList clone = new SinglePointSymbolRuleList(getTitle());
		clone.getSymbolizers().clear();
		for (PointSymbolizer ps : getSymbolizers()) {

			Symbolizer clonedSymbolizer = StylingUtil.clone(ps);
			clone.addSymbolizer((PointSymbolizer) clonedSymbolizer);
		}
		if (copyListeners) {
			for (RuleChangeListener rcl : getListeners()) {
				clone.addListener(rcl);
			}
		}

		return clone;

	}

	/**
	 * A symbol may contain many layers with different sizes. This method return
	 * the biggest size found in all layers.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public Float getSizeBiggest() {
		Float maxSize = -1f;
		for (PointSymbolizer ps : getSymbolizers()) {

			maxSize = ASUtil.getBiggestSize(ps, maxSize);
		}
		return maxSize;
	}

	/**
	 * A symbol may contain many layers with different sizes. This method
	 * changes the size of the biggest layer to the given value. The size of the
	 * other layers is calculated by the ratio of their size to the maximum
	 * size.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public void setSizeBiggest(Float newMax) {

		if (getSizeBiggest() < 0f)
			return;
		if (getSizeBiggest() == newMax)
			return;

		Float factor = newMax / getSizeBiggest();

		for (PointSymbolizer ps : getSymbolizers()) {

			float newSize = Float.valueOf(ps.getGraphic().getSize().toString())
					* factor;

			ps.getGraphic().setSize(ASUtil.ff2.literal(newSize));
		}

		fireEvents(new RuleChangedEvent("SetSize to " + newMax, this)); // noI8n
	}

	/**
	 * @return The rotation of the first layer
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public Double getRotation() {
		Double rot = null;
		if ((getSymbolizers() != null) && (getSymbolizers().size() > 0)) {
			Expression rotation = getSymbolizers().get(0).getGraphic()
					.getRotation();
			rot = rotation != null ? Double.valueOf(rotation.toString()) : 0.0;
		}
		return rot;
	}

	/**
	 * @return The rotation of the first layer
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public void setRotation(Double rot) {
		if ((getSymbolizers() != null) && (getSymbolizers().size() > 0)) {
			Double dif = rot - getRotation();
			for (PointSymbolizer ps : getSymbolizers()) {
				Double newRot = (Double.valueOf(ps.getGraphic().getRotation()
						.toString()) + dif) % 360;
				ps.getGraphic().setRotation(ASUtil.ff2.literal(newRot));
			}
			fireEvents(new RuleChangedEvent("setRotation to " + rot, this));
		}
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

		for (PointSymbolizer ps : getSymbolizers()) {

			StylingUtil.replacePointSymbolizerColor(ps, getColor(), newColor);

		}
		fireEvents(new RuleChangedEvent("setColor to " + newColor, this));
	}

	/**
	 * @return The first Color used in this Symbols (can be fill or stroke).
	 *         <code>null</code> is returned if no {@link Color} is found.
	 *         {@link ExternalGraphic}s have no {@link Color} and return
	 *         <code>null</code>
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	@Override
	public Color getColor() {
		for (PointSymbolizer ps : getSymbolizers()) {

			Color color = StylingUtil.getPointSymbolizerColor(ps);

			if (color != null)
				return color;

		}
		return null;
	}

	@Override
	public boolean hasRotation() {
		// TODO Only wenn was anderes als Circle!
		return true;
	}

	@Override
	public GeometryDescriptor getGeometryDescriptor() {
		return FeatureUtil.createFeatureType(Point.class)
				.getGeometryDescriptor();
	}

	@Override
	public String getLayerTypeDesc(int idx) {
		org.geotools.styling.Graphic g = getSymbolizers().get(idx).getGraphic();

		/** Checking for Marks */
		if (g.getMarks().length > 0) {
			return "mark:" + g.getMarks()[0].getWellKnownName();
		} else
		/** Checking for ExternalGraphics */
		if ((g.getExternalGraphics() != null)
				&& (g.getExternalGraphics().length > 0)) {
			if (g.getExternalGraphics()[0] != null)
				return "external:" + g.getExternalGraphics()[0].getFormat();
			else
				return "external";
		}
		return "???";
		// throw new RuntimeException("Type not recognized for layer# " + idx
		// + " in " + getTypeID() + "  Title=" + getTitle());
	}

	@Override
	public void addNewDefaultLayer() {
		Graphic graphic = ASUtil.createDefaultGraphic();

		PointSymbolizer newPS = ASUtil.SB.createPointSymbolizer(graphic);

		/**
		 * StyleBuilder has a bug since 2.4.5 or ealier. The Graphic.getSize is
		 * a NilExpression, which can not yet be transformed to XML :-(
		 */
		if (newPS.getGraphic() != null && newPS.getGraphic().getSize() != null) {
			newPS.getGraphic().setSize(ASUtil.ff.literal(10.0));
		}

		addSymbolizer(newPS);

	}

	@Override
	public RulesListType getTypeID() {
		return RulesListType.SINGLE_SYMBOL_POINT;
	}

	@Override
	public SingleRuleList<PointSymbolizer> copy() {
		return copyTo( new SinglePointSymbolRuleList(getTitle()));
	}

	public static SinglePointSymbolRuleList createDefaultInstance() {
		return null;
	}


}
