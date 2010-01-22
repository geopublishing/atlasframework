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

import org.geotools.styling.LineSymbolizer;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.expression.Expression;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.LineString;

public class SingleLineSymbolRuleList extends SingleRuleList<LineSymbolizer> {


	protected org.apache.log4j.Logger LOGGER = ASUtil.createLogger(this);

	public SingleLineSymbolRuleList(String title) {
		super(title);
	}

	public SingleLineSymbolRuleList(Translation title) {
		super(title);
	}

	/**
	 * Clones this {@link SingleLineSymbolRuleList}
	 * 
	 * @param copyListeners
	 *            If <code>true</code> the listeners are copied also.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	@Override
	public SingleRuleList clone(boolean copyListeners) {
		SingleLineSymbolRuleList clone = new SingleLineSymbolRuleList(getTitle());
		copyTo(clone);
		return clone;
	}

	@Override
	public GeometryDescriptor getGeometryDescriptor() {
		return FeatureUtil.createFeatureType(LineString.class)
				.getGeometryDescriptor();
	}

	@Override
	public RulesListType getTypeID() {
		return RulesListType.SINGLE_SYMBOL_LINE;
	}

	@Override
	public void addNewDefaultLayer() {
		addSymbolizer((LineSymbolizer) ASUtil
				.createDefaultSymbolizer(getGeometryDescriptor()));
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
	 *         Kr&uuml;ger</a>
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

	@Override
	public SingleRuleList<LineSymbolizer> copy() {
		return copyTo(new SingleLineSymbolRuleList(getTitle()));
	}

}
