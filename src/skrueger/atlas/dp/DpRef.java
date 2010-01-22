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
package skrueger.atlas.dp;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.AtlasRefInterface;
import skrueger.atlas.dp.layer.DpLayer;

/**
 * A {@link DpRef} is a reference to any Element in the {@link DpEntry} It can
 * be put into a {@link JTree} The target ID can't be changed after construction
 * 
 * @author Stefan Alfons Krüger
 */
public class DpRef<T extends DpEntry<? extends ChartStyle>> extends
		DefaultMutableTreeNode implements AtlasRefInterface<T> {
	Logger log = Logger.getLogger(DpRef.class);

	private String targetId;

	private DataPool dataPool;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DpRef) {
			return ((DpRef<?>) obj).getTargetId().equals(getTargetId());
		}
		return super.equals(obj);
	}

	@Override
	public DpRef<T> clone() {
		DpRef<T> clone = (DpRef<T>) super.clone();
		// log.debug("Calling clone on DatapoolRef...");
		// DpRef<T> dpr = new DpRef<T>(getTargetId(), getDataPool());
		clone.setTargetId(getTargetId());
		clone.setDataPool(dataPool);
		return clone;
	}

	/**
	 * @return The ID of the {@link DpEntry} referenced
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * @return true if the target {@link DpEntry} is a {@link DpLayer}
	 */
	public boolean isTargetLayer() {
		DpEntry dpe = getDataPool().get(getTargetId());
		if (dpe == null) {
			log
					.warn("The targetID of this reference doesn't belong to an item in the Datapool!");
			return false;
		}
		return dpe.isLayer();
	}

	/**
	 * Creates a {@link DpRef} to the given {@link DpEntry} in the
	 * 
	 * @param de
	 *            target {@link DpEntry}
	 */
	public DpRef(T de) {
		this.setDataPool(de.getAc().getDataPool());
		// i8nerr
		if (de.getId() == null)
			throw new IllegalArgumentException(
					"Can't create a media reference to a datapoolEntry that doesn't have an ID.");
		this.setTargetId(de.getId());
	}

	/**
	 * Creates a {@link DpRef} to the given ID in the {@link DataPool}
	 * 
	 * @param dp
	 *            target {@link DpEntry}
	 */
	public DpRef(String targetID, DataPool dp) {
		this.setDataPool(dp);
		this.setTargetId(targetID);
	}

	/**
	 * Return the translated getName() of the referenced DatapoolEntry
	 */
	@Override
	public String toString() {
		if (getTarget() != null) {
			return getTarget().getTitle().toString();
		} else
			return super.toString();
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
	 * No allowed
	 */
	@SuppressWarnings("unused")
	private DpRef() {
		super();
		setTargetId(null);
		setDataPool(null);
	}

	/**
	 * No allowed
	 */
	public DpRef(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
		setTargetId(null);
		setDataPool(null);
	}

	/**
	 * No allowed
	 */
	public DpRef(Object userObject) {
		super(userObject);
		setTargetId(null);
		setDataPool(null);
	}

	public final DataPool getDataPool() {
		return dataPool;
	}

	/**
	 * @return the targeted {@link DpRef}
	 */
	public T getTarget() {
		// TODO check validity... might return null if the dpe was deleted!!!
		return (T) getDataPool().get(getTargetId());
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public void setDataPool(DataPool dataPool) {
		this.dataPool = dataPool;
	}

}
