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
package skrueger.sld;

import java.awt.Color;
import java.awt.Stroke;

import org.apache.log4j.Logger;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.Polygon;

public class SinglePolygonSymbolRuleList extends
		SingleRuleList<PolygonSymbolizer> {
	protected final static Logger LOGGER = ASUtil
			.createLogger(SinglePolygonSymbolRuleList.class);

	public SinglePolygonSymbolRuleList(String title) {
		super(title);
	}

	public SinglePolygonSymbolRuleList(Translation title) {
		super(title);
	}

	/**
	 * Clones this {@link SinglePolygonSymbolRuleList}
	 * 
	 * @param copyListeners
	 *            If <code>true</code> the listeners are copied also.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	@Override
	public SinglePolygonSymbolRuleList clone(boolean copyListeners) {
		SinglePolygonSymbolRuleList clone = new SinglePolygonSymbolRuleList(getTitle());

		clone.getSymbolizers().clear();

		for (PolygonSymbolizer ps : layers) {

			Symbolizer clonedSymbolizer = StylingUtil.clone(ps);
			clone.addSymbolizer((PolygonSymbolizer) clonedSymbolizer);
		}

		if (copyListeners) {
			for (RuleChangeListener rcl : getListeners()) {
				clone.addListener(rcl);
			}
		}

		return clone;

	}

	/**
	 * The {@link Color} returned by {@link #getColor()} is replaced against the
	 * given color paramter. Any other occurence of the original color will also
	 * be replaced.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	@Override
	public void setColor(Color color) {

		for (PolygonSymbolizer ps : getSymbolizers()) {
			StylingUtil.replacePolygonSymbolizerColor(ps, getColor(), color);
		}

		fireEvents(new RuleChangedEvent("setColor to " + color, this));
	}

	/**
	 * @return The first Color used. Checks the {@link PolygonSymbolizer}s,
	 *         first {@link Fill} then {@link Stroke}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
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

	/** returns the Rotation if a GraphicFIll is used * */
	@Override
	public Double getRotation() {
		// TODO getRotation() all! :-)
		return 0.;
	}

	/** Sets the rotation of any subelement where it makes sense * */
	@Override
	public void setRotation(Double rot) {
		// TODO setRotation all ;-)
		// fireEvents( new RuleChangedEvent("setRotation to "+rot, this) );
	}

	/**
	 * @return <code>true</code> if a rotatable element is used by these
	 *         {@link Rule}s. Rotatable {@link LineSymbolizer}s are not
	 *         inspected.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
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

	/** @return <code>0.</code> if no {@link Graphic}Fill is used * */
	@Override
	public Float getSizeBiggest() {
		Float biggestSize = -1f;
		for (PolygonSymbolizer ps : getSymbolizers()) {
			biggestSize = ASUtil.getBiggestSize(ps, biggestSize);
		}
		return biggestSize;
	}

	/** Sets the size of any subelement where it makes sense * */
	@Override
	public synchronized void setSizeBiggest(Float newMax) {
		if (getSizeBiggest() < 0f)
			return;
		if (getSizeBiggest().floatValue() == newMax.floatValue())
			return;

		Float factor = newMax / getSizeBiggest();
		System.out.println("setSize Polygon by factor = " + factor + "for "
				+ newMax + "  / " + getSizeBiggest() + " ");

		for (PolygonSymbolizer ps : getSymbolizers()) {

			ASUtil.replacePolygonSymbolizerSize(ps, factor);

		}

		fireEvents(new RuleChangedEvent("setSize to " + newMax, this));
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

	@Override
	public void addNewDefaultLayer() {
		org.geotools.styling.Stroke stroke = ASUtil.createDefaultStroke();
		Fill fill = ASUtil.createDefaultFill();

		PolygonSymbolizer newPS = ASUtil.SB.createPolygonSymbolizer(stroke,
				fill);

		addSymbolizer(newPS);
	}

	@Override
	public RulesListType getTypeID() {
		return RulesListType.SINGLE_SYMBOL_POLYGON;
	}

	@Override
	public SingleRuleList<PolygonSymbolizer> copy() {
		return copyTo(new SinglePolygonSymbolRuleList(getTitle()));
	}

}
