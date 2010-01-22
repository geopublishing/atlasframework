/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
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
package skrueger.atlas.map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import skrueger.atlas.AtlasRefInterface;

/**
 * A {@link MapRef} is a reference to a {@link Map} in {@link MapPool} A
 * {@link MapRef} can be added to a {@link JTree}, because it extends
 * {@link DefaultMutableTreeNode}
 * 
 * @author Stefan Alfons Krüger
 * 
 */
public class MapRef extends DefaultMutableTreeNode implements
		AtlasRefInterface<Map> {
	final static private Logger LOGGER = Logger.getLogger(MapRef.class);

	private final String targetId;

	private final MapPool mapPool;

	/**
	 * Creates a {@link MapRef} to the targetMap in maps
	 * 
	 * @param mapPool
	 *            {@link MapPool} where the target {@link Map} exists in
	 */
	public MapRef(Map targetMap, MapPool mapPool) {
		this.mapPool = mapPool;
		this.targetId = targetMap.getId();
	}

	/**
	 * Return the translated Title of the referenced Map
	 */
	@Override
	public String toString() {
		if (getTarget() != null)
			return getTarget().getTitle().toString();
		else return super.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.DefaultMutableTreeNode#getAllowsChildren()
	 */
	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.DefaultMutableTreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return true;
	}

	/**
	 * @return the referenced {@link Map} directly
	 */
	public Map getTarget() {
		return getMapPool().get(targetId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.atlas.AtlasRefInterface#isTargetLayer()
	 */
	public boolean isTargetLayer() {
		return false;
	}

	/**
	 * @return The ID of the {@link Map} referenced
	 */
	public final String getTargetId() {
		return targetId;
	}

	public MapPool getMapPool() {
		return mapPool;
	}
}
