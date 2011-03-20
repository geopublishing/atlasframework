package org.geopublishing.atlasStyler;

import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList.RulesListType;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasStyler.rulesLists.TextRuleList;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;

import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.versionnumber.ReleaseUtil;

public abstract class AtlasStyler {
	/**
	 * The {@link AtlasStylerVector} can run in two {@link LANGUAGE_MODE}s.
	 */
	public enum LANGUAGE_MODE {

		/**
		 * Use atlas extension which codes many translations into the title
		 * field of rules.
		 */
		ATLAS_MULTILANGUAGE,

		/**
		 * Follow OGC standard and put simple Strings into the title field of
		 * rules.
		 */
		OGC_SINGLELANGUAGE
	}

	/** These DIRNAMEs describe paths to application files on the local machines */
	final static String DIRNAME_TEMPLATES = "templates";

	/**
	 * Default languageMode is LANGUAGE_MODE.OGC_SINGLELANGUAGE
	 */
	static LANGUAGE_MODE languageMode = LANGUAGE_MODE.OGC_SINGLELANGUAGE;

	/**
	 * List of LanguageCodes that the targeted Atlas is supposed to "speak".
	 */
	private static List<String> languages = new LinkedList<String>();

	private final static Logger LOGGER = LangUtil
			.createLogger(AtlasStyler.class);

	/**
	 * Key for a parameter of type List<Font> of additional Fonts that are
	 * available
	 */
	public final static String PARAM_FONTS_LIST_FONT = "PARAM_FONTS_LIST_FONT";

	/**
	 * Key for a parameter of type List<String> that contains the languages the
	 * SLD is created for (not SLD standard, only used if AS is run within GP)
	 */
	public final static String PARAM_LANGUAGES_LIST_STRING = "PARAM_LANGUAGES_LIST_STRING";

	/**
	 * The {@link AtlasStylerVector} can run in two {@link LANGUAGE_MODE}s. TODO
	 * remove static
	 */
	public static LANGUAGE_MODE getLanguageMode() {
		return languageMode;
	}

	/**
	 * If we are running in {@link LANGUAGE_MODE} ATLAS_MULTILANGUAGE, these are
	 * the supported languages.<br/>
	 * TODO remove static
	 */
	public final static List<String> getLanguages() {
		return languages;
	}

	/**
	 * Because the rule title may not be empty, we check different sources here.
	 * the translated title of the styled layer would be first choice.
	 * 
	 * @return never <code>null</code> and never ""
	 */
	public static Translation getRuleTitleFor(StyledLayerInterface sf) {

		if (!I18NUtil.isEmpty(sf.getTitle()))
			return sf.getTitle();

		if (sf instanceof StyledFeaturesInterface) {
			return new Translation(getLanguages(),
					((StyledFeaturesInterface) sf).getSchema().getName()
							.getLocalPart());
		} else {
			// RASTER
			return new Translation("raster this baby");
		}
	}

	/**
	 * @Deprecated use AsUtil.R
	 */
	@Deprecated
	// use AsUtil.R
	public static String R(String key, final Object... values) {
		return ASUtil.R(key, values);
	}

	/**
	 * If false, the {@link AtlasStylerVector} only fires
	 * {@link StyleChangedEvent}s when the dialog is closed.
	 */
	boolean automaticPreview = ASProps.getInt(ASProps.Keys.automaticPreview, 1) == 1;

	/**
	 * *Backup of the {@link Style} as it was before the AtlasStyle touched it.
	 * Used when {@link #cancel()} is called.
	 */
	protected Style backupStyle;

	/**
	 * A list of fonts that will be available for styling in extension to the
	 * default font families. {@link #getDefaultFontFamilies()}
	 */
	private List<Font> fonts = new ArrayList<Font>();

	/**
	 * A list of all exceptions/problems that occurred during import.
	 */
	final private List<Exception> importErrorLog = new ArrayList<Exception>();

	private RulesListInterface lastChangedRuleList;

	/**
	 * This listener is attached to all rule lists and propagates any events as
	 * {@link StyleChangedEvent}s to the listeners of the
	 * {@link AtlasStylerVector} *
	 */
	protected final RuleChangeListener listenerFireStyleChange = new RuleChangeListener() {

		@Override
		public void changed(final RuleChangedEvent e) {
			styleCached = null;

			// SPEED OPTIMIZATION: Here we check whether the RuleChangedEvent is
			// a min/max change. Next we check if a preview pane is set..
			// TODO not finished SPEED OPTIMIZATION
			if (RuleChangedEvent.RULE_CHANGE_EVENT_MINMAXSCALE_STRING.equals(e
					.getReason())) {
				// Object ov = e.getOldValue();
				// Object nv = e.getNewValue();
				// if (getPreviewScale() != null) {
				//
				// }
			}

			final RulesListInterface someRuleList = e.getSourceRL();

			lastChangedRuleList = someRuleList;

			fireStyleChangedEvents();
		}
	};

	/***************************************************************************
	 * Listeners to style changes
	 */
	Set<StyleChangeListener> listeners = new HashSet<StyleChangeListener>();

	// private final MapLegend mapLegend;
	protected final MapLayer mapLayer;

	/**
	 * If not <code>null</code>, swing dialogs might popup.
	 */
	protected Component owner = null;

	/** If true, no Events will be fired to listeners */
	private boolean quite = true;

	/**
	 * This factory is used to create empty or default rule lists.
	 */
	protected RuleListFactory rlf;

	/**
	 * List of {@link AbstractRulesList}s that {@link AtlasStylerVector} is
	 * combining to one {@link Style}.
	 */
	protected RulesListsList ruleLists;

	/**
	 * THis listener is attached to the one {@link RulesListsList}. If a new
	 * {@link RulesListsList} is added, moved or removed, it triggers an event.
	 */
	private final PropertyChangeListener rulesListsListListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			styleCached = null;
			fireStyleChangedEvents();
		}
	};

	/**
	 * The cache for the {@link Style} that is generated when
	 * {@link #getStyle()} is called.
	 */
	protected Style styleCached = null;

	private Translation title;

	public AtlasStyler(MapLayer mapLayer, HashMap<String, Object> params,
			Boolean withDefaults) {
		// this.mapLegend = mapLegend;
		this.mapLayer = mapLayer;

		// If no params were passed, use an empty List, so we don't have to
		// check against null
		if (params == null)
			params = new HashMap<String, Object>();

		setFonts((List<Font>) params.get(PARAM_FONTS_LIST_FONT));

		/***********************************************************************
		 * Configuring the AtlasStyler translation settings.
		 * 
		 */
		List<String> languages = (List<String>) params
				.get(PARAM_LANGUAGES_LIST_STRING);

		if (languages == null || languages.size() == 0) {
			setLanguageMode(AtlasStylerVector.LANGUAGE_MODE.OGC_SINGLELANGUAGE);
		} else {
			setLanguageMode(AtlasStylerVector.LANGUAGE_MODE.ATLAS_MULTILANGUAGE);
			setLanguages(languages);
		}

	}

	/**
	 * Adds a {@link StyleChangeListener} to the {@link AtlasStylerVector} which
	 * gets called whenever the {@link Style} has changed.
	 */
	public void addListener(final StyleChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Adds an {@link AbstractRulesList} to the {@link #ruleLists} and adds a
	 * listener to it.
	 */
	public void addRulesList(AbstractRulesList rulelist) {
		rulelist.addListener(listenerFireStyleChange);
		getRuleLists().add(0, rulelist);
		// fireStyleChangedEvents(rulelist);
	}

	/**
	 * Fires a {@link StyleChangedEvent} with the backup style to all listeners.
	 * Mainly used when cancelling any activity
	 */
	public void cancel() {
		styleCached = backupStyle;
	}

	abstract public AbstractRulesList copyRulesList(RulesListInterface rl);

	/**
	 * Disposes the {@link AtlasStylerVector}. Tries to help the Java GC by
	 * removing dependencies.
	 */
	public void dispose() {
		reset();
		listeners.clear();
	}

	/**
	 * Informs all {@link StyleChangeListener}s about changed in the
	 * {@link Style}
	 * 
	 * Use this for {@link TextRuleList}
	 */
	public void fireStyleChangedEvents() {
		fireStyleChangedEvents(false);
	}

	/**
	 * Informs all {@link StyleChangeListener}s about changed in the
	 * {@link Style}
	 * 
	 * Use this for {@link TextRuleList}
	 * 
	 * @param forced
	 *            Can be used to override isAutomaticPreview()
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void fireStyleChangedEvents(final boolean forced) {
		if (isQuite() && !forced) {
			// LOGGER.info("NOT FIREING EVENT because we are quite");
			return;
		}

		if ((!forced) && (!isAutomaticPreview())) {
			// LOGGER
			// .info("NOT FIREING EVENT because automaticPreview is deselected");
			return;
		}

		// LOGGER.info(" FIREING EVENT to " + listeners.size());

		styleCached = null;
		StyleChangedEvent ev = getStyleChangeEvent();
		if (ev == null)
			return;

		styleCached = ev.getStyle();
		if (styleCached == null)
			return;

		for (final StyleChangeListener l : listeners) {
			// LOGGER.debug("fires a StyleChangedEvent... ");
			try {
				l.changed(ev);
			} catch (Exception e) {
				LOGGER.error("Error while informing StyleChangeListener " + l,
						e);
			}
		}
	}

	/**
	 * Explicitly informs all {@link StyleChangeListener}s about changed in the
	 * {@link Style} due to the given parameter <code>ruleList</code>
	 * 
	 * Sets the parameter ruleList as the lastChangedRuleList
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	protected void fireStyleChangedEvents(final RulesListInterface ruleList) {
		if (!(ruleList instanceof TextRuleList))
			lastChangedRuleList = ruleList;

		fireStyleChangedEvents();
	}

	/**
	 * A list of fonts that will be available for styling in extension to the
	 * default font families. #getDefaultFontFamilies
	 */
	public List<Font> getFonts() {
		return fonts;
	}

	/**
	 * A list of all exceptions/problems that occurred during import.
	 */
	public List<Exception> getImportErrorLog() {
		return importErrorLog;
	}

	/***************************************************************************
	 * @return The last {@link AbstractRulesList} where change has been observed
	 *         via the {@link RuleChangeListener}. Never return the labeling
	 *         TextRulesList. {@link #listenerFireStyleChange}
	 */
	public RulesListInterface getLastChangedRuleList() {
		return lastChangedRuleList;
	}

	/**
	 * May return <code>null</code>, if no {@link MapLayer} is connected to the
	 * {@link AtlasStylerVector}.
	 */
	public MapLayer getMapLayer() {
		return mapLayer;
	}

	public Component getOwner() {
		return owner;
	}

	/**
	 * This factory is used to create rule-lists. Returns a
	 * {@link RuleListFactory}.
	 */
	public RuleListFactory getRlf() {
		return rlf;
	}

	/**
	 * List of {@link AbstractRulesList}s that {@link AtlasStylerVector} is
	 * combining to one {@link Style}.
	 */
	public RulesListsList getRuleLists() {
		if (ruleLists == null) {
			ruleLists = new RulesListsList();
			ruleLists.addListener(rulesListsListListener);
		}
		return ruleLists;
	}

	/**
	 * Returns a new {@link ArrayList} with a reversed order of the RulesLists.
	 * Changed to this {@link List} will not change the order of the RuleLists.
	 * Changes to the ruleLists will change the RuleLists.
	 */
	protected List<AbstractRulesList> getRuleListsReverse() {

		ArrayList<AbstractRulesList> arrayList = new ArrayList<AbstractRulesList>();

		arrayList.addAll(getRuleLists());
		Collections.reverse(arrayList);
		return arrayList;
	}

	/***************************************************************************
	 * @return A full {@link Style} that represents the last RuleList that has
	 *         been changed.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public Style getStyle() {
		if (styleCached == null) {

			// Create an empty Style without any FeatureTypeStlyes
			styleCached = ASUtil.SB.createStyle();
			styleCached.setName("AtlasStyler "
					+ ReleaseUtil.getVersion(ASUtil.class));

			if (getRuleLists().size() == 0) {
				final String msfg = "AS:Returning empty style because atlasStyler has no RulesLists";
				LOGGER.error(msfg);
				styleCached.getDescription().setTitle(msfg);
				return styleCached;
			}

			// TODO handle textRuleLists special at the end?
			for (RulesListInterface ruleList : getRuleListsReverse()) {
				styleCached.featureTypeStyles().add(ruleList.getFTS());
			}

			// Just for debugging
			Level level = Logger.getRootLogger().getLevel();
			try {
				Logger.getRootLogger().setLevel(Level.OFF);
				StylingUtil.saveStyleToSld(styleCached, new File(
						"/home/stefan/Desktop/update.sld"));
			} catch (final Throwable e) {
			} finally {
				Logger.getRootLogger().setLevel(level);
			}

			styleCached = sanitize(styleCached);

			// Just for debugging
			level = Logger.getRootLogger().getLevel();
			try {
				Logger.getRootLogger().setLevel(Level.OFF);
				StylingUtil.saveStyleToSld(styleCached, new File(
						"/home/stefan/Desktop/update_fixed.sld"));
			} catch (final Throwable e) {
			} finally {
				Logger.getRootLogger().setLevel(level);
			}
		}
		return styleCached;
	}

	/**
	 * Returns a StyleChangedEvent or a RasterStyleChangedEvent
	 * 
	 * @return
	 */
	abstract StyleChangedEvent getStyleChangeEvent();

	public abstract StyledLayerInterface<?> getStyledInterface();

	public Translation getTitle() {
		return title;
	}

	/**
	 * Tries to interpret a {@link Style} as {@link AbstractRulesList}s. Only
	 * {@link FeatureTypeStyle}s with parameter <code>name</code> starting with
	 * {@link RulesListType.SINGLE_SYMBOL_POINT} can be interpreted.
	 * 
	 * @param importStyle
	 *            {@link Style} to import.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void importStyle(Style importStyle) {
		reset();

		// Backup
		if (backupStyle == null) {
			backupStyle = StylingUtil.copy(importStyle);
		}

		// Makes a copy of the style before importing it. otherwise we might get
		// the same object and not recognize changes later...
		importStyle = StylingUtil.copy(importStyle);

		// Forget all RuleLists we might have imported before

		// Just for debugging at steve's PC.
		Level level = Logger.getRootLogger().getLevel();
		try {
			Logger.getRootLogger().setLevel(Level.OFF);
			StylingUtil.saveStyleToSld(importStyle, new File(
					"/home/stefan/Desktop/goingToImport.sld"));
		} catch (final Throwable e) {
		} finally {
			Logger.getRootLogger().setLevel(level);
		}

		for (final FeatureTypeStyle fts : importStyle.featureTypeStyles()) {
			try {
				setQuite(true); // Quite the AtlasStyler!

				AbstractRulesList importedThisAbstractRuleList = rlf.importFts(
						fts, false);
				if (importedThisAbstractRuleList != null) {

					// We are reverting the order while importing the SLD to the
					// RulesListsList. When exporting to getStyle it is reverted
					// again. This allows the RulesListTable to show the last
					// painted rulesList on top.
					ruleLists.add(0, importedThisAbstractRuleList);

					importedThisAbstractRuleList
							.addListener(listenerFireStyleChange);
					fireStyleChangedEvents(importedThisAbstractRuleList);
				}
				// else {

				// }
				// throw new AtlasParsingException("Importing fts " + fts
				// + " retuned null. No more information available.");

			} catch (final Exception importError) {
				LOGGER.warn(
						"Import error: " + importError.getLocalizedMessage(),
						importError);

				getImportErrorLog().add(importError);
			} finally {
				setQuite(false);
			}

		}
		final int ist = getRuleLists().size();
		final int soll = importStyle.featureTypeStyles().size();
		if (ist < soll) {
			LOGGER.debug("Only " + ist + " of all " + soll
					+ " RuleLists have been recognized fully...");
		}

		if (getRuleLists().size() > 0) {
			LOGGER.debug("Imported "
					+ getRuleLists().size()
					+ " valid FeatureTypeStyles, fireing StyleChangedEvents... ");
			fireStyleChangedEvents(getRuleLists().get(0));
		}

	}

	public boolean isAutomaticPreview() {
		return automaticPreview;
	}

	/**
	 * @return <code>true</code> means, that {@link AtlasStylerVector} will fire
	 *         {@link StyleChangedEvent}s
	 */
	public boolean isQuite() {
		return quite;
	}

	/**
	 * Before loading a style we have to forget everything we might have
	 * imported before. Does not remove the listeners!
	 */
	public void reset() {

		getRuleLists().clear();

		styleCached = null;

		lastChangedRuleList = null;

	}

	public abstract Style sanitize(Style style);

	public void setAutomaticPreview(final boolean automaticPreview) {
		ASProps.set(ASProps.Keys.automaticPreview, automaticPreview ? 1 : 0);
		this.automaticPreview = automaticPreview;
	}

	/**
	 * A list of fonts that will be available for styling. If set to
	 * <code>null</code>, {@link #getFonts()} will return a new and empty
	 * ArayList<Font>
	 */
	public void setFonts(List<Font> fonts) {
		if (fonts == null)
			fonts = new ArrayList<Font>();
		else
			this.fonts = fonts;
	}

	/**
	 * The {@link AtlasStylerVector} can run in two {@link LANGUAGE_MODE}s.
	 */
	public void setLanguageMode(final LANGUAGE_MODE languageMode) {
		AtlasStylerVector.languageMode = languageMode;
	}

	public final void setLanguages(final List<String> languages) {
		this.languages = languages;
	}

	/**
	 * Reset the {@link List} of supported Languages to the passed
	 * {@link String}
	 * 
	 * @param langs
	 *            new {@link List} of lang codes
	 */
	public void setLanguages(final String... langs) {
		if (langs.length > 0)
			languages.clear();

		for (String code : langs) {
			code = code.trim();
			if (I18NUtil.isValidISOLangCode(code)) {
				languages.add(code);
			} else
				throw new IllegalArgumentException("The ISO Language code '"
						+ code + "' is not known/valid." + "\nIgnoring it.");
		}
	}

	public void setLastChangedRuleList(
			final RulesListInterface lastChangedRuleList) {

		if (!(lastChangedRuleList instanceof TextRuleList))
			this.lastChangedRuleList = lastChangedRuleList;

		if (lastChangedRuleList != null) {
			LOGGER.info("Changing LCRL manually to "
					+ lastChangedRuleList.getClass().getSimpleName());
		} else {
			LOGGER.info("Changing LCRL to null");
		}
	}

	/**
	 * If not <code>null</code>, swing dialogs might popup.
	 */
	public void setOwner(Component owner) {
		this.owner = owner;
	}

	/**
	 * <code>true</code> means, that {@link AtlasStylerVector} will fire
	 * {@link StyleChangedEvent}s
	 */
	public void setQuite(final boolean quite) {
		this.quite = quite;
	}

	public void setTitle(final Translation title) {
		this.title = title;
	}

	public HashMap<String, Object> getDataMap() {
		return dataMap;
	}

	/**
	 * Store anything in here!
	 */
	private final HashMap<String, Object> dataMap = new HashMap<String, Object>();

}
