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
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.geotools.styling.Description;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.geotools.Copyable;
import skrueger.geotools.LegendIconFeatureRenderer;
import skrueger.geotools.StyledLayerUtil;
import skrueger.i8n.Translation;

public abstract class SingleRuleList<SymbolizerType extends Symbolizer> extends
		AbstractRulesList implements Copyable<SingleRuleList<SymbolizerType>> {
	final static protected Logger LOGGER = ASUtil
			.createLogger(SingleRuleList.class);

	/**
	 * This {@link Vector} represents a list of all {@link Symbolizer}s that
	 * will be used to paint the symbols
	 */
	protected Vector<SymbolizerType> layers = new Vector<SymbolizerType>();

	private String styleAbstract;

	private String styleName;

	private String styleTitle;

	private String ruleTitle = "title missing";

	/**
	 * This boolean defines whether the entry shall be shown the legend. <b>This
	 * is only interpreted in GP/Atlas context.</b>
	 */
	private boolean visibleInLegend = true;

	/**
	 * @param title
	 *            label for the rule
	 */
	public SingleRuleList(String title, GeometryForm geometryForm) {
		super(geometryForm);
		pushQuite();
		setTitle(title);
		popQuite();
	}

	/**
	 * @param title
	 *            label for the rule
	 */
	public SingleRuleList(Translation title, GeometryForm geometryForm) {
		super(geometryForm);
		setTitle(title);
	}

	/**
	 * Creates a new Symbolizer and adds it to the layers. This fires an event
	 * to all {@link RuleChangeListener}s.
	 */
	public abstract void addNewDefaultLayer();

	/**
	 * Adds a symbolizer to the {@link SingleRuleList}
	 * 
	 * @param symbolizer
	 *            The symbolizer to add.
	 */
	public boolean addSymbolizer(Symbolizer symbolizer) {
		boolean add = layers.add((SymbolizerType) symbolizer);
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
	public boolean addSymbolizers(List<? extends Symbolizer> symbolizers) {
		boolean add = layers
				.addAll((Collection<? extends SymbolizerType>) symbolizers);
		if (add)
			fireEvents(new RuleChangedEvent("Added " + symbolizers.size()
					+ " symbolizers", this));
		return add;
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public abstract SingleRuleList<? extends SymbolizerType> clone(
			boolean copyListeners);

	/**
	 * Copies all values from the first {@link SinglePointSymbolRuleList} to the
	 * second {@link SinglePointSymbolRuleList}. The {@link RuleChangeListener}s
	 * are not changed. The {@link RuleChangeListener}s of the second
	 * {@link SinglePointSymbolRuleList} are fired afterwards.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public SingleRuleList<SymbolizerType> copyTo(SingleRuleList to) {

		to.pushQuite();

		to.getSymbolizers().clear();

		to.setVisibleInLegend(isVisibleInLegend());

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

	@Override
	public String getAtlasMetaInfoForFTSName() {
		return getType().toString();
	}

	@Override
	void parseMetaInfoString(String metaInfoString, FeatureTypeStyle fts) {
	}

	/**
	 * The {@link Color} returned by {@link #getColor()} is replaced against the
	 * given color parameter. Any other occurrence of the original color will
	 * also be replaced.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	abstract public Color getColor();

	/**
	 * @return The {@link GeometryDescriptor} that this symbolization rules
	 *         works on.
	 */
	public abstract GeometryDescriptor getGeometryDescriptor();

	/**
	 * Return a {@link BufferedImage} that represent the symbol with default
	 * dimensions.
	 * 
	 * @return {@link BufferedImage} representing this SinglesRulesList
	 */
	public BufferedImage getImage() {
		return getImage(AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	/**
	 * Return a {@link BufferedImage} that represent the symbol.
	 * 
	 * @param size
	 *            Width and Height of the {@link BufferedImage} to create
	 */
	public BufferedImage getImage(Dimension size) {

		BufferedImage image = LegendIconFeatureRenderer
				.getInstance()
				.createImageForRule(getRule(),
						ASUtil.createFeatureType(getGeometryDescriptor()), size);

		return image;
	}

	/** Returns a description or the type of the {@link Symbolizer} */
	public abstract String getLayerTypeDesc(int idx);

	/** returns the Rotation if it makes sense or null* */
	abstract public Double getRotation();

	/**
	 * Because a {@link SingleRuleList} only contains one {@link Rule}, this is
	 * a convenience method to get it.
	 */
	public Rule getRule() {
		return getRules().get(0);
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
			symbolizers.add(ps);
		}
		Collections.reverse(symbolizers);

		// TODO Add support for NODATA

		Rule rule = ASUtil.SB.createRule(symbolizers
				.toArray(new Symbolizer[symbolizers.size()]));

		applyScaleDominators(rule);

		/** Saving the legend label * */
		rule.setTitle(getTitle());

		addFilters(rule);

		ArrayList<Rule> rList = new ArrayList<Rule>();
		rList.add(rule);

		// If this RuleList is disabled, add a HIDE IN LEGEND hint to the
		// Legend, so schmitzm will ignore the layer
		if (!isEnabled()) {
			rule.setName(rule.getName() + "_"
					+ StyledLayerUtil.HIDE_IN_LAYER_LEGEND_HINT);
		}

		System.out.println(rule.getName());

		return rList;
	}

	private void addFilters(Rule rule) {
		Filter filter = FilterUtil.ALLWAYS_TRUE_FILTER;

		// The order is important! This is parsed the reverse way. The last
		// thing added to the filter equals the first level in the XML.
		filter = addAbstractRlSettings(filter);

		rule.setFilter(filter);

	}

	/**
	 * *************************************************************************
	 * ABSTRACT METHODS
	 * *************************************************************************
	 */

	/**
	 * @return Returns the biggest Size used if any Size is used. If no size
	 *         used returns 0.
	 */
	public abstract Float getSizeBiggest();

	public String getStyleAbstract() {
		return styleAbstract;
	}

	/**
	 * Used for the filename without .sld
	 */
	public String getStyleName() {
		return styleName;
	}

	/**
	 * The Title is used for the author
	 * 
	 * @return
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public String getStyleTitle() {
		return styleTitle;
	}

	/**
	 * @return A {@link Vector} of {@link PointSymbolizer}s that paint the
	 *         symbols. The are painted in reverse order.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public Vector<SymbolizerType> getSymbolizers() {
		return layers;
	}

	/**
	 * @return The title of the first and only {@link Rule}. This is used as the
	 *         label for this rule in the legend.
	 */
	public String getRuleTitle() {
		return ruleTitle;
	}

	/**
	 * @return <code>True</code> if any item is used that has a changeable
	 *         {@link Color} TODO think again .. do we need that?
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean hasColor() {
		return getColor() != null;
	}

	abstract public boolean hasRotation();

	public boolean hasSize() {
		return getSizeBiggest() >= 0.;
	}

	/**
	 * This boolean defines whether the entry shall be shown the legend. <b>This
	 * is only interpreted in GP/Atlas context.</b>
	 */
	public boolean isVisibleInLegend() {
		return visibleInLegend;
	}

	/**
	 * This fires an event to all {@link RuleChangeListener}s.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean loadURL(URL url) {
		pushQuite();
		try {
			Style[] styles = StylingUtil.loadSLD(url);
			// LOGGER.debug("Anzahl Styles in URL " + styles.length);

			if (styles == null || styles.length == 0 || styles[0] == null)
				throw new RuntimeException("Symbol von " + url
						+ " konnte nicht geladen werden.");

			setStyleTitle(styles[0].getTitle());
			setStyleAbstract(styles[0].getAbstract());

			if (StylingUtil.sldToString(styles[0]).contains("the_geom")) {
				LOGGER.warn("The imported symbol contains a ref to the_geom!");
			}

			// Transforming
			// http://en.geopublishing.org/openmapsymbols/point/Circle.sld to
			// Circle
			String fileName = new File(url.getFile()).getName();
			String fileNameWithoutSLD = fileName.substring(0,
					fileName.lastIndexOf('.'));

			setStyleName(fileNameWithoutSLD);

			try {
				FeatureTypeStyle featureTypeStyle = styles[0]
						.featureTypeStyles().get(0);
				Rule rule = featureTypeStyle.rules().get(0);
				Symbolizer[] symbolizers = rule.getSymbolizers();
				for (Symbolizer s : symbolizers) {
					addSymbolizer(s);
				}
				// System.out.println("SingleRuleList loaded "+symbolizers.length+"
				// symbolizers from URL "+url.getFile());
			} catch (Exception e) {
				LOGGER.warn("Error loading " + url + ": "
						+ e.getLocalizedMessage());
				return false;
			}

			fireEvents(new RuleChangedEvent("Loaded from URL " + url, this));

			return true;

		} catch (RuntimeException e) {
			LOGGER.error("Error reading URL " + url, e);
			throw e;
		} catch (TransformerException e) {
			LOGGER.error("Error reading URL " + url, e);
			throw new RuntimeException("Error reading URL " + url, e);
		} finally {
			pushQuite();
		}
	}

	public SymbolizerType removeSymbolizer(int index) {
		SymbolizerType rmvd = layers.remove(index);
		if (rmvd != null)
			fireEvents(new RuleChangedEvent("Removed a Symbolizer", this));
		return rmvd;
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
		style.featureTypeStyles().get(0).rules().get(0).symbolizers()
				.addAll(layers);

		style.getDescription().setTitle(getStyleTitle());
		style.getDescription().setAbstract(getStyleAbstract());
		style.setName(getStyleName()); // Not really needed... we evaluate the
		// filename

		StylingUtil.saveStyleToSld(style, file);

		// TODO Override and call super from the specific implementations ??????
		// really?
	}

	/**
	 * The {@link Color} returned by {@link #getColor()} is replaced against the
	 * given color parameter. Any other occurrence of the original color will
	 * also be replaced. This fires an event to all {@link RuleChangeListener}s.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	abstract public void setColor(Color newColor);

	/**
	 * Sets the rotation of any subelement where it makes sense. This fires an
	 * event to all {@link RuleChangeListener}s.
	 */
	public abstract void setRotation(Double size);

	/**
	 * Sets the size of any sub-element where it makes sense. This fires an
	 * event to all {@link RuleChangeListener}s.
	 */
	public abstract void setSizeBiggest(Float size);

	/**
	 * The Abstract is used as a description line for a Symbol
	 * 
	 * @param styleAbstract
	 *            Description
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
					+ ruleTitle, this));
		}

	}

	/**
	 * Used for the filename without .sld
	 * 
	 * @param styleName
	 */
	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	/**
	 * Use for Author
	 * 
	 * @param styleTitle
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void setStyleTitle(String styleTitle) {
		this.styleTitle = styleTitle;

	}

	/**
	 * @return A {@link Vector} of {@link PointSymbolizer}s that paint the
	 *         symbols. The are painted in reverse order.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void setSymbolizers(Vector<SymbolizerType> newlayers) {
		layers = newlayers;
		fireEvents(new RuleChangedEvent("All symbolizers have been replaced",
				this));
	}

	/**
	 * Set the title of the first and only {@link Rule}. This is used as the
	 * label for this rule in the legend.
	 * 
	 */
	public void setRuleTitle(String ruleTitle) {

		if (ruleTitle == null || ruleTitle.equals("")) {
			// LOGGER.warn("rule title may not be empty");
			ruleTitle = "title missing";
		}

		// Is the new title really different from the old one?
		boolean change = true;
		if (this.ruleTitle != null && ruleTitle != null
				&& this.ruleTitle.equals(ruleTitle))
			change = false;

		if (change) {
			// Update the title and fire an event
			this.ruleTitle = ruleTitle;
			fireEvents(new RuleChangedEvent("Single Legend Label changed to "
					+ ruleTitle, this));
		}
	}

	/**
	 * Set the title of the first and only {@link Rule}. This is used as the
	 * label for this rule in the legend.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void setTitle(Translation translation) {
		setTitle(translation.toOneLine());
	}

	/**
	 * /** This boolean defines whether the entry shall be shown the legend.
	 * <b>This is only interpreted in GP/Atlas context.</b> Changing this
	 * property will automatically fire a {@link RuleChangedEvent}
	 */
	public void setVisibleInLegend(boolean visibleInLegend) {
		if (visibleInLegend != this.visibleInLegend) {
			this.visibleInLegend = visibleInLegend;
			fireEvents(new RuleChangedEvent("visiblility in legend changed",
					this));
		}
	}

	/**
	 * This stuff is the same for all three SINGLE_RULES types.
	 */
	@Override
	public void importRules(List<Rule> rules) {
		pushQuite();

		if (rules.size() > 1) {
			LOGGER.warn("Importing a " + this.getClass().getSimpleName()
					+ " with " + rules.size() + " rules, strange!");
		}

		Rule rule = rules.get(0);

		try {

			final List<? extends Symbolizer> symbs = rule.symbolizers();
			addSymbolizers(symbs);
			reverseSymbolizers();

			// We had some stupid AbstractMethodException here...
			try {
				final Description description = rule.getDescription();
				final InternationalString title2 = description.getTitle();
				setTitle(title2.toString());
			} catch (final NullPointerException e) {
				LOGGER.warn("The title style to import has been null!");
				setTitle("");
			} catch (final Exception e) {
				LOGGER.error("The title style to import could not been set!", e);
				setTitle("");
			}

			// Analyse the filters...
			Filter filter = rule.getFilter();
			filter = parseAbstractRlSettings(filter);

		} finally {
			popQuite();
		}
	}

	public void setRuleTitle(Translation translation) {
		setRuleTitle(translation.toOneLine());
	}

}
