/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;

import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.CancellableTabbedDialogAdapter;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.TranslationEditJPanel;
import de.schmitzm.swing.TranslationsAskJPanel;

public class EditMapJDialog extends CancellableTabbedDialogAdapter {

	private Map backupMap;
	private final Map map;
	private final AtlasConfigEditable ace;

	public EditMapJDialog(Component owner, final Map map) {
		super(owner);

		ace = (AtlasConfigEditable) map.getAc();
		this.map = map;

		// Create a backup to we can cancel
		backupMap = map.copy();

		setTitle(map.getTitle().toString());

		final ArrayList<Image> icons = new ArrayList<Image>();
		icons.add(Icons.ICON_MAP_BIG.getImage());
		icons.add(Icons.ICON_MAP_SMALL.getImage());
		setIconImages(icons);

		/** A tab for name, desc and keywords... **/
		getTabbedPane().insertTab(
				GeopublisherGUI.R("EditMapEntryGUI.labels.tab"), 
				null, createTranslationsTab(), null,
				getTabbedPane().getTabCount());

		/** A tab for name, desc and keywords... **/
		getTabbedPane().insertTab(GeopublisherGUI.R("EditMapEntryGUI.html.tab"), 
				null, createGeneralTab(), null, getTabbedPane().getTabCount());

		pack();
		
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER, SwingUtil.NORTHEAST);
	}

	/**
	 * Creates a JPanel for the TabbedPane that contains general information
	 */
	private JPanel createGeneralTab() {

		JPanel generalPanel = new JPanel(new MigLayout("w 100%"));

		JPanel htmlPanel = new JPanel(new MigLayout("w 100%, wrap 3",
				"[grow]"));
		htmlPanel.setBorder(BorderFactory
				.createTitledBorder("Map HTML description")); // i8n

		// preview
		DesignHTMLInfoPane html = GpSwingUtil.createDesignHTMLInfoPane(ace, map);
		JComponent htmlPreview = html.getComponent();
		if ( !html.hasScrollPane() )
		    htmlPreview = new JScrollPane(htmlPreview);
		htmlPanel.add(htmlPreview, "growx, wrap, height 255, w 100%");
		SwingUtil.setPreferredHeight(htmlPanel, 270);

		/**
		 * This button allows to delete all HTML files
		 */
		final JButton deleteHtmlButton = new JButton(
				new MapPoolDeleteAllHTMLAction(EditMapJDialog.this, map));
		deleteHtmlButton.setBorder(BorderFactory.createEtchedBorder());
		// Adding a listener to enable/disable the button; Doesn't have to be
		// removed, because MP uses a WeakHashSet.
		ace.getMapPool().addChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() instanceof Map) {
					if (((Map) evt.getNewValue()).equals(map))
						deleteHtmlButton.setEnabled(map
								.getMissingHTMLLanguages().size() > 0);
				}
			}
		});
		htmlPanel.add(deleteHtmlButton, "bottom, right, span 3, split 3");

		/**
		 * This button allows to edit the HTML text
		 */
		JButton editHtmlButton = new JButton(new MapPoolEditHTMLAction(map));
		editHtmlButton.setBorder(BorderFactory.createEtchedBorder());
		htmlPanel.add(editHtmlButton);

		/***
		 * This buttons allows to open the folder with the map's HTML files
		 */
		JButton openContainingFolderButton = new JButton(
				new AbstractAction(GeopublisherGUI
						.R("MapPreferences_ButtonOpenHTMLDirectory_label")) {

					@Override
					public void actionPerformed(ActionEvent e) {
						File htmlDir = new File(((AtlasConfigEditable) map
								.getAc()).getHtmlDir(), map.getId());
						if (!htmlDir.exists())
							htmlDir.mkdirs();
						SwingUtil.openOSFolder(htmlDir);
					}

				});
		openContainingFolderButton.setToolTipText(GeopublisherGUI
				.R("MapPreferences_ButtonOpenHTMLDirectory_tt"));
		openContainingFolderButton
				.setBorder(BorderFactory.createEtchedBorder());

		htmlPanel.add(openContainingFolderButton);

		generalPanel.add(htmlPanel, "growx, wrap");

		JPanel linktoPanel = new JPanel(new MigLayout("w 100%, wrap"));
		linktoPanel
				.setBorder(BorderFactory.createTitledBorder("Link to this map")); // i8n

		linktoPanel.add(new JLabel(GeopublisherGUI
				.R("MapPreferences.LinkToThisMap.Label")));

		Box box = Box.createVerticalBox();
		final List<String> languages = map.getAc().getLanguages();

		for (String lang : ace.getLanguages()) {
			final JTextField linkToMeTextfield;
			linkToMeTextfield = new JTextField(map.getInternalLink(lang));
			linkToMeTextfield.setEditable(false);
			JPanel oneLine = new JPanel(new BorderLayout());
			oneLine.add(linkToMeTextfield, BorderLayout.CENTER);
			JButton copyButton = new JButton(new AbstractAction(GeopublisherGUI
					.R("CopyButton.Label")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					LangUtil.copyToClipboard(linkToMeTextfield.getText());
				}

			});
			copyButton.setToolTipText(GeopublisherGUI
					.R("CopyButton.TT"));

			copyButton.setBorder(BorderFactory.createEtchedBorder());

			oneLine.add(copyButton, BorderLayout.EAST);
			box.add(oneLine);

		}
		linktoPanel.add(box, "growx");

		generalPanel.add(linktoPanel, "grow x");
		return generalPanel;
	}

	/** A tab for name, desc and keywords... **/
	private TranslationsAskJPanel createTranslationsTab() {
		TranslationsAskJPanel dpeTranslationTab;
		{
			final List<String> langs = map.getAc().getLanguages();
			if (map.getTitle() == null)
				map.setTitle(new Translation(langs, "untitled"));
			if (map.getDesc() == null)
				map.setDesc(new Translation());
			if (map.getKeywords() == null)
				map.setKeywords(new Translation());

			List<String> languages = map.getAc().getLanguages();

			final TranslationEditJPanel namePanel = new TranslationEditJPanel(
					GeopublisherGUI.R("MapPreferences_translateTheMapsName"), map
							.getTitle(), languages);
			final TranslationEditJPanel descPanel = new TranslationEditJPanel(
					GeopublisherGUI
							.R("MapPreferences_translateTheMapsDescription"),
					map.getDesc(), languages);
			final TranslationEditJPanel keywordsPanel = new TranslationEditJPanel(
					GeopublisherGUI.R("MapPreferences_translateTheMapsKeywords"),
					map.getKeywords(), languages);

			dpeTranslationTab = new TranslationsAskJPanel(namePanel, descPanel,
					keywordsPanel);
		}
		return dpeTranslationTab;
	}

	@Override
	public void cancel() {
		super.cancel();
		backupMap.copyTo(map);
	}

	@Override
	public boolean okClose() {
		if (super.okClose()) {

			/**
			 * Inform the DataPool about the changes
			 */
			map.getAc().getMapPool().fireChangeEvents(this,
					org.geopublishing.atlasViewer.map.MapPool.EventTypes.changeMap, map);

			return true;
		}
		return false;
	}

}
