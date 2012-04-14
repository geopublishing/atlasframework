package org.geopublishing.atlasStyler.chartgraphic;


public interface ChartGraphicChangeListener {
	/**
	 * Called when a Symbolization RL has changed.
	 */

	public abstract void changed(ChartGraphicChangedEvent e);
}
