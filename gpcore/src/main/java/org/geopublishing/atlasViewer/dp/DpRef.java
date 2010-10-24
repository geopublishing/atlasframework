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
package org.geopublishing.atlasViewer.dp;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasRefInterface;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;

import schmitzm.jfree.chart.style.ChartStyle;

/**
 * A {@link DpRef} is a reference to any Element in the {@link DpEntry} It can
 * be put into a {@link JTree} The target ID can't be changed after construction
 * 
 * @author Stefan Alfons Tzeggai
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
	@Override
	public String getTargetId() {
		return targetId;
	}

	/**
	 * @return true if the target {@link DpEntry} is a {@link DpLayer}
	 */
	@Override
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
		this.setDataPool(de.getAtlasConfig().getDataPool());
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
	@Override
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
