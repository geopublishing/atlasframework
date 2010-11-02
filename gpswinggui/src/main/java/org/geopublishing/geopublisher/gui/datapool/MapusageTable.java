package org.geopublishing.geopublisher.gui.datapool;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.SwingUtil;

/**
 * A table listsing the maps a {@link DpEntry} is used in.
 * 
 * @author stefan
 */
public class MapusageTable extends JTable {

	private DefaultTableModel tm;
	private ArrayList<Map> mapsUsing;
	private final MapPool mapPool;
	private final DpEntry<? extends ChartStyle> dpe;
	private PropertyChangeListener mapPoolChangeListener;

	public MapusageTable(final DpEntry<? extends ChartStyle> dpe,
			final MapPool mapPool) {
		super();
		this.dpe = dpe;
		this.mapPool = mapPool;

		mapsUsing = new ArrayList<Map>(mapPool.getMapsUsing(dpe));

		// Open a MapComposer when double-clicked
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Map map = mapsUsing.get(getSelectedRow());

				// Select the map in the MapPoolJTable
				GeopublisherGUI.getInstance().getJFrame().getMappoolJTable()
						.select(map.getId());

				if (e.getClickCount() >= 2) {
					GPDialogManager.dm_MapComposer.getInstanceFor(map,
							MapusageTable.this, map);
				}
				super.mouseClicked(e);
			}
		});

		setModel(getTableModel());

		// Add Listener to the mapPool, and update this table when needed
		mapPoolChangeListener = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				mapsUsing = new ArrayList<Map>(mapPool.getMapsUsing(dpe));
				getTableModel().fireTableDataChanged();
			}
		};
		mapPool.addChangeListener(mapPoolChangeListener);
		
		SwingUtil.setColumnLook(this, 0, null, 60, 80, null);

	}

	private DefaultTableModel getTableModel() {
		if (tm == null) {
			tm = new DefaultTableModel() {
				public int getColumnCount() {
					return (dpe instanceof DpLayer ? 4 : 1);
				};

				public int getRowCount() {
					return getMapsUsing().size();
				};

				public Object getValueAt(int row, int column) {
					switch (column) {
					case 0:
						return getMapsUsing().get(row).getTitle().toString();
					case 1:
						return getMapsUsing().get(row).isVisible(dpe);
					case 2:
						return getMapsUsing().get(row).isVisibleInLegend(dpe);
					case 3:
						return getMapsUsing().get(row).isSelectableFor(dpe.getId());
					}
					return super.getValueAt(row, column);
				};

				public String getColumnName(int column) {
					switch (column) {
					case 0:
						return GpSwingUtil
								.R("EditDpEntryGUI.usage.Maps.col.name");
					case 1:
						return GpSwingUtil
								.R("EditDpEntryGUI.usage.Maps.col.visible");
					case 2:
						return GpSwingUtil
								.R("EditDpEntryGUI.usage.Maps.col.visibleInLegend");
					case 3:
						return GpSwingUtil
								.R("EditDpEntryGUI.usage.Maps.col.selectable");
					}
					return super.getColumnName(column);
				};

				public boolean isCellEditable(int row, int column) {
					return false;
				};

			};
		}

		return tm;
	}

	public ArrayList<Map> getMapsUsing() {
		return mapsUsing;
	}

}
