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
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.feature.type.GeometryDescriptor;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.geotools.Copyable;
import skrueger.geotools.LegendIconFeatureRenderer;
import skrueger.i8n.Translation;

public abstract class SingleRuleList<SymbolizerType extends Symbolizer> extends
		AbstractRuleList implements Copyable<SingleRuleList<SymbolizerType>> {
	final static protected Logger LOGGER = ASUtil
			.createLogger(SingleRuleList.class);

	@Deprecated 
	public SingleRuleList() {
		setTitle(AtlasStyler.R("GraduatedColorQuantities.Column.Label")); 
	}
	
	/**
	 * @param title label for the rule
	 */
	public SingleRuleList(Translation title) {
		setTitle(title); 
	}

	/**
	 * @param title label for the rule
	 */
	public SingleRuleList(String title) {
		setTitle(title); 
	}

	/**
	 * This {@link Vector} represents a list of all {@link Symbolizer}s that
	 * will be used to paint the symbols
	 */
	protected Vector<SymbolizerType> layers = new Vector<SymbolizerType>();

	private String title = "title missing"; 

	private String styleTitle;

	private String styleAbstract;

	private String styleName;

	private double maxScaleDenominator = Double.MAX_VALUE;
	private double minScaleDenominator = Double.MIN_VALUE;

	/**
	 * Adds a symbolizer to the {@link SingleRuleList}
	 * 
	 * @param symbolizer
	 *            The symbolizer to add.
	 */
	public boolean addSymbolizer(SymbolizerType symbolizer) {
		boolean add = layers.add(symbolizer);
		if (add)
			fireEvents(new RuleChangedEvent("Added a Symbolizer", this));
		return add;
	}

	/**
	 * Adds a {@link List} of symbolizers to the {@link SingleRuleList}
	 * 
	 * @param symbolizer
	 *            The symbolizers to add.
	 */
	public boolean addSymbolizers(List symbolizers) {
		boolean add = layers.addAll(symbolizers);
		if (add)
			fireEvents(new RuleChangedEvent("Added " + symbolizers.size()
					+ " symbolizers", this));
		return add;
	}

	/**
	 * Constantly reverses the order of symbolizers. Fires an event if the order
	 * of symbolizers has changed.
	 * 
	 */
	public void reverseSymbolizers() {
		if (layers.size() > 1) {
			Collections.reverse(layers);
			fireEvents(new RuleChangedEvent("Changed the order of symbolizers",
					this));
		}
	}

	/**
	 * Removes a symbolizer from the list of symbolizers.
	 * 
	 * @return <code>true</code> if the symbolizer has actually been removed.
	 */
	public boolean removeSymbolizer(SymbolizerType ps) {
		boolean rmvd = layers.remove(ps);
		if (rmvd)
			fireEvents(new RuleChangedEvent("Removed a Symbolizer", this));
		return rmvd;
	}

	public SymbolizerType removeSymbolizer(int index) {
		SymbolizerType rmvd = layers.remove(index);
		if (rmvd != null)
			fireEvents(new RuleChangedEvent("Removed a Symbolizer", this));
		return rmvd;
	}

	/**
	 * Implementing the {@link Cloneable} interface, this method overwrites the
	 * {@link Object}'s clone method.
	 * 
	 * @param copyListeners
	 *            If <code>true</code> the listeners are copied, too. They are
	 *            not cloned. If <code>false</code> they are ignored (e.g. left
	 *            behind)
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public abstract SingleRuleList<? extends SymbolizerType> clone(
			boolean copyListeners);

	/**
	 * @return The {@link GeometryDescriptor} that this symbolization rules
	 *         works on.
	 */
	public abstract GeometryDescriptor getGeometryDescriptor();

	/**
	 * Wraps the Symbol in a {@link Style} and saves it as an SLD to the
	 * {@link File}
	 * 
	 * @param file
	 *            Where to save the SLD
	 * @throws TransformerException
	 * @throws IOException
	 */
	public void saveSymbolToFile(File file) throws TransformerException,
			IOException {
		if (layers.size() == 0)
			return;
		Style style = ASUtil.SB.createStyle(layers.get(0));
		style.featureTypeStyles().get(0).rules().get(0).symbolizers().clear();
		style.featureTypeStyles().get(0).rules().get(0).symbolizers().addAll(
				layers);

		style.getDescription().setTitle(getStyleTitle());
		style.getDescription().setAbstract(getStyleAbstract());
		style.setName(getStyleName()); // Not really needed... we evaluate the
		// filename

		StylingUtil.saveStyleToSLD(style, file);

		// TODO Override and call super from the specific implementations ??????
		// really?
	}

	/**
	 * This fires an event to all {@link RuleChangeListener}s.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public boolean loadURL(URL url) {
		String urlString;
		try {
			// urlString = new File( url.toURI() ).getName();
			urlString = url.toURI().getPath();
		} catch (URISyntaxException e) {
			LOGGER.error(e);

			urlString = url.toString();
		}
		// LOGGER.info("url to load = " + urlString);
		Style[] styles = StylingUtil.loadSLD(url);
		//
		// LOGGER.debug("URL " + styles.length);
		// LOGGER.debug("Anzahl Styles in URL " + styles.length);

		setStyleTitle(styles[0].getTitle());
		setStyleAbstract(styles[0].getAbstract());

		String fileNameWithoutSLD;
		try {
			fileNameWithoutSLD = urlString.substring(
					urlString.lastIndexOf("/") + 1, urlString.length() - 4);
			// LOGGER.debug("urlstr=" + urlString);
		} catch (Exception e) {
			ExceptionDialog.show(null, e);
			fileNameWithoutSLD = "ILLEGALFILENAME";
		}
		setStyleName(fileNameWithoutSLD);

		try {
			FeatureTypeStyle featureTypeStyle = styles[0].featureTypeStyles()
					.get(0);
			Rule rule = featureTypeStyle.rules().get(0);
			Symbolizer[] symbolizers = rule.getSymbolizers();
			for (Symbolizer s : symbolizers) {
				addSymbolizer((SymbolizerType) s);
			}
			// System.out.println("SingleRuleList loaded "+symbolizers.length+"
			// symbolizers from URL "+url.getFile());
		} catch (Exception e) {
			LOGGER
					.warn("Error loading " + url + ": "
							+ e.getLocalizedMessage());
			return false;
		}

		fireEvents(new RuleChangedEvent("Loaded from URL " + url, this));

		return true;
	}

	/**
	 * Copies all values from the first {@link SinglePointSymbolRuleList} to the
	 * second {@link SinglePointSymbolRuleList}. The {@link RuleChangeListener}s
	 * are not changed. The {@link RuleChangeListener}s of the second
	 * {@link SinglePointSymbolRuleList} are fired afterwards.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	@Override
	public SingleRuleList<SymbolizerType> copyTo(SingleRuleList to) {

		to.pushQuite();

		to.getSymbolizers().clear();

		// Wrong: This did't make a deep copy!
		// for (SymbolizerType ps : getSymbolizers()) {
		// to.addSymbolizer(ps);
		// }

		// Thats better:
		for (SymbolizerType ps : getSymbolizers()) {
			final DuplicatingStyleVisitor duplicatingStyleVisitor = new DuplicatingStyleVisitor();
			duplicatingStyleVisitor.visit(ps);
			SymbolizerType ps2 = (SymbolizerType) duplicatingStyleVisitor
					.getCopy();
			to.addSymbolizer(ps2);
		}

		to.setStyleAbstract(getStyleAbstract());
		to.setStyleName(getStyleName());
		to.setStyleTitle(getStyleTitle());

		to.setMinScaleDenominator(getMinScaleDenominator());
		to.setMaxScaleDenominator(getMaxScaleDenominator());

		to.popQuite();
		return to;
	}

	/**
	 * @return A {@link Vector} of {@link PointSymbolizer}s that paint the
	 *         symbols. The are painted in reverse order.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public Vector<SymbolizerType> getSymbolizers() {
		return layers;
	}

	/**
	 * @return A {@link Vector} of {@link PointSymbolizer}s that paint the
	 *         symbols. The are painted in reverse order.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void setSymbolizers(Vector<SymbolizerType> newlayers) {
		layers = newlayers;
		fireEvents(new RuleChangedEvent("All symbolizers have been replaced",
				this));
	}

	/**
	 * Return a {@link BufferedImage} that represent the symbol with default
	 * dimensions.
	 * 
	 * @return {@link BufferedImage} representing this SinglesRulesList
	 */
	public BufferedImage getImage() {
		return getImage(AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	// TODO Doppelt mit public static BufferedImage
	// getSymbolizerImage(Symbolizer symb, int width, ???
	/**
	 * Return a {@link BufferedImage} that represent the symbol.
	 * 
	 * @param size
	 *            Width and Height of the {@link BufferedImage} to create
	 */
	public BufferedImage getImage(Dimension size) {

		final Rule rule = getRules().get(0);

		BufferedImage image = LegendIconFeatureRenderer
				.getInstance()
				.createImageForRule(rule,
						ASUtil.createFeatureType(getGeometryDescriptor()), size);

		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Rule> getRules() {

		// Reversing the order of symbols. Is also done in
		// AtlasStyler.importStyle when importing a SingleRulesList
		List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
		for (Symbolizer ps : getSymbolizers()) {
			symbolizers.add(ps
					);
		}
		Collections.reverse(symbolizers);

		Rule rule = ASUtil.SB.createRule(symbolizers
				.toArray(new Symbolizer[symbolizers.size()]));

		rule.setName("AS:" + this.getClass().getSimpleName());

		rule.setMaxScaleDenominator(getMaxScaleDenominator());
		rule.setMinScaleDenominator(getMinScaleDenominator());

		/** Saving the legend label * */
		rule.setTitle(getTitle());

		ArrayList<Rule> rList = new ArrayList<Rule>();
		rList.add(rule);

		return rList;
	}

	/**
	 * *************************************************************************
	 * ABSTRACT METHODS
	 * *************************************************************************
	 */

	/**
	 * The {@link Color} returned by {@link #getColor()} is replaced against the
	 * given color parameter. Any other occurrence of the original color will
	 * also be replaced.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	abstract public Color getColor();

	/**
	 * The {@link Color} returned by {@link #getColor()} is replaced against the
	 * given color parameter. Any other occurrence of the original color will
	 * also be replaced. This fires an event to all {@link RuleChangeListener}s.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	abstract public void setColor(Color newColor);

	/**
	 * @return <code>True</code> if any item is used that has a changeable
	 *         {@link Color} TODO think again .. do we need that?
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public boolean hasColor() {
		return getColor() != null;
	}

	/** returns the Rotation if it makes sense or null* */
	abstract public Double getRotation();

	abstract public boolean hasRotation();

	/**
	 * Sets the rotation of any subelement where it makes sense. This fires an
	 * event to all {@link RuleChangeListener}s.
	 */
	public abstract void setRotation(Double size);

	/**
	 * @return Returns the biggest Size used if any Size is used. If no size
	 *         used returns 0.
	 */
	public abstract Float getSizeBiggest();

	/**
	 * Sets the size of any sub-element where it makes sense. This fires an
	 * event to all {@link RuleChangeListener}s.
	 */
	public abstract void setSizeBiggest(Float size);

	/** Returns a description or the type of the {@link Symbolizer} */
	public abstract String getLayerTypeDesc(int idx);

	/**
	 * Creates a new Symbolizer and adds it to the layers. This fires an event
	 * to all {@link RuleChangeListener}s.
	 */
	public abstract void addNewDefaultLayer();

	public boolean hasSize() {
		return getSizeBiggest() >= 0.;
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		return getTypeID().toString();
	}

	/**
	 * @return The title of the first and only {@link Rule}. This is used as the
	 *         label for this rule in the legend.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title of the first and only {@link Rule}. This is used as the
	 * label for this rule in the legend.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void setTitle(String title) {
		
		if (title == null || title.equals("")) {
//			LOGGER.warn("rule title may not be empty");
			title = "title missing";
		} 

		// Is the new title really different from the old one?
		boolean change = true;
		if (this.title == null && title == null)
			change = false;
		if (this.title != null && title != null && this.title.equals(title))
			change = false;

		if (change) {
			// Update the title and fire an event
			this.title = title;
			fireEvents(new RuleChangedEvent("Single Legend Label changed to "
					+ title, this));
		}
	}

	/**
	 * Set the title of the first and only {@link Rule}. This is used as the
	 * label for this rule in the legend.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void setTitle(Translation translation) {
		setTitle(translation.toOneLine());
	}

	public String getStyleAbstract() {
		return styleAbstract;
	}

	/**
	 * The Abstract is used as a description line for a Symbol
	 * 
	 * @param styleAbstract
	 *            Description
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void setStyleAbstract(final String styleAbstract) {

		// Is the new title really different from the old one?
		boolean change = true;
		if (this.styleAbstract == null && styleAbstract == null)
			change = false;
		if (this.styleAbstract != null && styleAbstract != null
				&& this.styleAbstract.equals(styleAbstract))
			change = false;

		if (change) {
			// Update the title and fire an event
			this.styleAbstract = styleAbstract;
			fireEvents(new RuleChangedEvent("Single Legend Label changed to "
					+ title, this));
		}

	}

	/**
	 * The Title is used for the author
	 * 
	 * @return
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public String getStyleTitle() {
		return styleTitle;
	}

	/**
	 * Use for Author
	 * 
	 * @param styleTitle
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void setStyleTitle(String styleTitle) {
		this.styleTitle = styleTitle;

	}

	/**
	 * Used for the filename without .sld
	 */
	public String getStyleName() {
		return styleName;
	}

	/**
	 * Used for the filename without .sld
	 * 
	 * @param styleName
	 */
	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	public void setMaxScaleDenominator(double maxScaleDenominator) {
		this.maxScaleDenominator = maxScaleDenominator;
		fireEvents(new RuleChangedEvent("maxScale changed", this));
	}

	public double getMaxScaleDenominator() {
		return maxScaleDenominator;
	}

	public void setMinScaleDenominator(double minScaleDenominator) {
		this.minScaleDenominator = minScaleDenominator;
		// TODO test for real change
		fireEvents(new RuleChangedEvent("minScale changed", this));
	}

	public double getMinScaleDenominator() {
		return minScaleDenominator;
	}

}
