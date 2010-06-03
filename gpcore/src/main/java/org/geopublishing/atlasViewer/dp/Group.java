/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.dp;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasRefInterface;
import org.geopublishing.atlasViewer.map.Map;

import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

/**
 * A {@link Group} is an rather abstract container to organize the information
 * of the atlas. A {@link Group} can contain subgroups. The root {@link Group}
 * is stored in the {@link AtlasConfig}. A {@link Group} has a {@link #title}, a
 * {@link #desc} and {@link #keywords}. The children of a {@link Group} can be:
 * 
 * <li>a sub-{@link Group} <li>a {@link DpRef} to any {@link DpEntry}.
 * 
 */
public class Group extends DefaultMutableTreeNode implements Transferable,
		Serializable {
	private static Logger LOGGER = Logger.getLogger(Group.class);

	private boolean helpMenu = false;
	private boolean fileMenu = false;

	private AtlasConfig ac;

	private Translation title;

	private Translation desc;

	private Translation keywords;

	// ****************************************************************************
	// D'n'D stuff
	// ****************************************************************************
	final public static DataFlavor INFO_FLAVOR = new DataFlavor(Group.class,
			"Thematic Atlas group");

	static DataFlavor flavors[] = { INFO_FLAVOR };

	/**
	 * A {@link Group} is an rather abstract container to organize the
	 * information of the atlas. A {@link Group} can contain subgroups. The root
	 * {@link Group} is stored in the {@link AtlasConfig}. A {@link Group} has a
	 * {@link #title}, a {@link #desc} and {@link #keywords} The children of a
	 * {@link Group} can be:
	 * 
	 * <li>a sub-{@link Group} <li>a {@link DpRef} to any {@link DpEntry}.
	 * 
	 * @param ac
	 *            {@link AtlasConfig} the Atlas that this {@link Group} exists
	 *            in.
	 */
	public Group(AtlasConfig ac) {
		super();
		this.ac = ac;
		title = new Translation(getAc().getLanguages(), AVUtil
				.R("NewGroup.DefaultTitle"));
		desc = new Translation(getAc().getLanguages(), "");
		keywords = new Translation(getAc().getLanguages(), "");
	}

	/**
	 * This {@link #toString()} method uses the title of the {@link Group}
	 */
	@Override
	public String toString() {
		String returnme = "";
		if (title != null)
			returnme = title.toString();
		return returnme;
	}

	/**
	 * Copy this group to parameter backup
	 * 
	 * @param backup
	 *            target for the copy
	 */
	public void copy(Group backup) {
		if (backup == null)
			throw new IllegalArgumentException("Copy-target must not be null!");
		title.copyTo(backup.getTitle());
		desc.copyTo(backup.getDesc());
		backup.keywords = keywords;
	}

	/**
	 * For debugging...
	 */
	public final void debug() {
		LOGGER.debug(" Debugging GROUP:");
		LOGGER.debug(" name = " + title);
		LOGGER.debug(" isHelpMenu = " + isHelpMenu());
		LOGGER.debug(" Anz children= " + getChildCount());
		if (getChildCount() > 0)
			for (Object nodeO : children) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeO;
				if (node instanceof Group) {
					LOGGER.debug(" Debugging subgroup");
					((Group) node).debug();
				} else if (node instanceof DpRef) {
					LOGGER.debug(" has a MediaRef to "
							+ ((DpRef) node).getTargetId());
				}
			}
		LOGGER.debug(" End group " + title);
	}

	/**
	 * Search the whole {@link Group} tree of the {@link AtlasConfig} and adds
	 * all {@link AtlasRefInterface} to the list OR delete them
	 * 
	 * @param references
	 *            A List<DatapoolRef>
	 * @param delete
	 *            if true, than all references will be deleted
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Tzeggai</a>
	 */
	public static void findReferencesTo(AtlasConfig ac, Object dpeOrMap,
			LinkedList<AtlasRefInterface<?>> references, boolean delete) {
		findReferencesTo(ac.getFirstGroup(), dpeOrMap, references, delete);
	}

	/**
	 * Search the Group tree and add all references to the list OR delete them
	 * 
	 * @param dpeOrMap
	 * @param references
	 *            A List<DatapoolRef>
	 * @param delete
	 *            if true, than all references will be deleted
	 */
	public static void findReferencesTo(Group g, Object dpeOrMap,
			LinkedList<AtlasRefInterface<?>> references, boolean delete) {
		final Enumeration<DefaultMutableTreeNode> children = g.children();

		// ****************************************************************************
		// Create a reference to the searched object
		// ****************************************************************************
		String id;
		if (dpeOrMap instanceof DpEntry) {
			id = ((DpEntry) dpeOrMap).getId();
		} else if (dpeOrMap instanceof Map) {
			id = ((Map) dpeOrMap).getId();
		} else {
			LOGGER
					.warn("findReferencesTo was called with an unidentifies Class="
							+ dpeOrMap.getClass());
			return;
		}

		int childIndex = -1;
		while (children.hasMoreElements()) {
			Object item = children.nextElement();
			childIndex++;

			if (item instanceof AtlasRefInterface) {
				final AtlasRefInterface<?> testref = (AtlasRefInterface<?>) item;
				if (testref.getTargetId().equals(id)) {
					if (delete) {
						LOGGER.debug("removing ref " + item.toString()
								+ " from group. targetid = " + id);
						g.remove(childIndex);
					} else {
						references.add(testref);
					}
				}
			} else if (item instanceof Group) {
				findReferencesTo((Group) item, dpeOrMap, references, delete);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.
	 * datatransfer.DataFlavor)
	 */
	public final boolean isDataFlavorSupported(DataFlavor df) {
		return df.equals(INFO_FLAVOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer
	 * .DataFlavor)
	 */
	public final Object getTransferData(DataFlavor df)
			throws UnsupportedFlavorException, IOException {
		if (df.equals(INFO_FLAVOR)) {
			return this;
		} else
			throw new UnsupportedFlavorException(df);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public final DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	/**
	 * @return {@link Translation} of comma-seperated keywords
	 */
	public final Translation getKeywords() {
		return keywords;
	}

	/**
	 * Set {@link Translation} of comma-seperated keywords
	 * 
	 * @param keywords
	 */
	public final void setKeywords(Translation keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return {@link Translation} of the group's name
	 */
	public final Translation getTitle() {
		return title;
	}

	/**
	 * Set {@link Translation} of the group's name
	 * 
	 * @param name
	 *            {@link Translation} to set
	 */
	public final void setTitle(Translation name) {
		this.title = name;
	}

	/**
	 * @return {@link Translation} of the group's desciption
	 */
	public Translation getDesc() {
		return desc;
	}

	/**
	 * Set {@link Translation} of the group's description
	 * 
	 * @param desc
	 *            {@link Translation} to set
	 */
	public void setDesc(Translation desc) {
		this.desc = desc;
	}

	/**
	 * @return {@link AtlasConfig}
	 */
	public final AtlasConfig getAc() {
		return ac;
	}

	/**
	 * Set the {@link AtlasConfig}
	 * 
	 * @param ac
	 *            {@link AtlasConfig}
	 */
	public final void setAc(AtlasConfig ac) {
		this.ac = ac;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.DefaultMutableTreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.DefaultMutableTreeNode#getAllowsChildren()
	 */
	@Override
	public boolean getAllowsChildren() {
		// Every Group can have children

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.DefaultMutableTreeNode#getUserObject()
	 */
	@Override
	public Object getUserObject() {
		return this;
	}

	public Double getQuality() {
		Double result;

		List<String> languages = getAc().getLanguages();
		result = (I8NUtil.qmTranslation(languages, getTitle()) * 4.
				+ I8NUtil.qmTranslation(languages, getDesc()) * 2. + I8NUtil
				.qmTranslation(languages, getKeywords()) * 1.) / 7.;

		return result;
	}

	public void setHelpMenu(boolean isHelpMenu) {
		this.helpMenu = isHelpMenu;
	}

	public boolean isHelpMenu() {
		return helpMenu;
	}

	public void setFileMenu(boolean fileMenu) {
		this.fileMenu = fileMenu;
	}

	public boolean isFileMenu() {
		return fileMenu;
	}

}
