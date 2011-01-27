package org.geopublishing.geopublisher.gui.datapool;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;

import de.schmitzm.jfree.chart.style.ChartStyle;

/**
 * A table listsing the menu (groups) a {@link DpEntry} is used in.
 */
public class MenuusageTable extends JTable {

	private DefaultTableModel tm;
	private final ArrayList<Group> groupsUsing;
	private final Group rootGroup;
	private final DpEntry<? extends ChartStyle> dpe;

	public MenuusageTable(DpEntry<? extends ChartStyle> dpe, Group rootGroup) {
		super();
		this.dpe = dpe;
		this.rootGroup = rootGroup;

		groupsUsing = new ArrayList<Group>(rootGroup.getGroupsUsing(dpe));

		setModel(getTableModel());

		// Open a MapComposer when double-clicked
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				int selectedRow = getSelectedRow();

				if (selectedRow == -1)
					return;

				Group map = groupsUsing.get(selectedRow);

				// Select the map in the MapPoolJTable
				GeopublisherGUI.getInstance().getJFrame().getGroupJTree()
						.select(map);

				if (e.getClickCount() >= 2) {
				}

				super.mouseClicked(e);
			}
		});

	}

	private TableModel getTableModel() {
		if (tm == null) {
			tm = new DefaultTableModel() {
				public int getColumnCount() {
					return 1;
				};

				public int getRowCount() {
					return getGroupsUsing().size();
				};

				public Object getValueAt(int row, int column) {
					switch (column) {
					case 0:
						return getGroupsUsing().get(row).getTitle().toString();
					}
					return super.getValueAt(row, column);
				};

				public String getColumnName(int column) {
					switch (column) {
					case 0:
						return GpSwingUtil
								.R("EditDpEntryGUI.usage.Menu.col.name");
					case 1:
						return GpSwingUtil
								.R("EditDpEntryGUI.usage.Menu.col.visible");
					case 2:
						return GpSwingUtil
								.R("EditDpEntryGUI.usage.Menu.col.selectable");
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

	public ArrayList<Group> getGroupsUsing() {
		return groupsUsing;
	}

}
