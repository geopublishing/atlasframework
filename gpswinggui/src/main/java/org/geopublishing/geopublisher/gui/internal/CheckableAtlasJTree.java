package org.geopublishing.geopublisher.gui.internal;

import java.awt.Component;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.lang.LangUtil;
import skrueger.swing.checkboxtree.CheckBoxNode;
import skrueger.swing.checkboxtree.CheckBoxNodeEditor;
import skrueger.swing.checkboxtree.CheckBoxNodeRenderer;
import skrueger.swing.checkboxtree.NamedVector;

public class CheckableAtlasJTree extends JTree {
	
	protected final static Logger LOGGER = LangUtil
	.createLogger(CheckableAtlasJTree.class);


	private AtlasConfig atlasConfig;
	private TreeSet<String> selectedIds = new TreeSet<String>();

	@Override
	public String convertValueToText(Object value, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		return super.convertValueToText(value, selected, expanded, leaf, row,
				hasFocus);
	}

	public CheckableAtlasJTree() {
		setCellRenderer(new CheckBoxNodeRenderer() {

			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {

				Component returnValue;
				if (leaf) {

					String stringValue = tree.convertValueToText(value,
							selected, expanded, leaf, row, false);
					leafRenderer.setText(stringValue);
					leafRenderer.setSelected(false);

					leafRenderer.setEnabled(tree.isEnabled());

					if (selected) {
						leafRenderer.setForeground(selectionForeground);
						leafRenderer.setBackground(selectionBackground);
					} else {
						leafRenderer.setForeground(textForeground);
						leafRenderer.setBackground(textBackground);
					}

					if ((value != null)
							&& (value instanceof DefaultMutableTreeNode)) {
						Object userObject = ((DefaultMutableTreeNode) value)
								.getUserObject();
						if (userObject instanceof AtlasJTreeCheckBoxNode) {
							AtlasJTreeCheckBoxNode node = (AtlasJTreeCheckBoxNode) userObject;
							leafRenderer.setText(node.getTitle());
							leafRenderer.setSelected(node.isSelected());
						}

					}
					returnValue = leafRenderer;
				} else {
					returnValue = nonLeafRenderer.getTreeCellRendererComponent(
							tree, value, selected, expanded, leaf, row,
							hasFocus);
				}
				return returnValue;
			}
		});

		setCellEditor(new CheckBoxNodeEditor(this) {

			@Override
			public Object getCellEditorValue() {
				JCheckBox checkbox = renderer.getLeafRenderer();

				String id = checkbox.getText();

				if (atlasConfig.getDataPool().containsKey(id))
					return new AtlasJTreeCheckBoxNode(atlasConfig.getDataPool()
							.get(id), checkbox.isSelected());
				else
					return new AtlasJTreeCheckBoxNode(atlasConfig.getMapPool()
							.get(id), checkbox.isSelected());
			}

		});
		setEditable(true);
		setRootVisible(false);

		getCellEditor().addCellEditorListener(new CellEditorListener() {

			@Override
			public void editingStopped(ChangeEvent e) {
				CheckBoxNodeEditor cbne = (CheckBoxNodeEditor) e.getSource();
				Object cellEditorValue = cbne.getCellEditorValue();
				CheckBoxNode cbn = (CheckBoxNode) cellEditorValue;

				String id = cbn.getText();
				if (!cbn.isSelected()) {
					if (atlasConfig.getDataPool().containsKey(id)) {
						selectedIds.remove(id);

						// Also remove the maps, that were dependent on this
						// layer
						Set<Map> mapsUsing = atlasConfig
								.getMapPool()
								.getMapsUsing(atlasConfig.getDataPool().get(id));
						for (Map map : mapsUsing)
							selectedIds.remove(map.getId());

					} else if (atlasConfig.getMapPool().containsKey(id))
						selectedIds.remove(id);
				} else {
					if (atlasConfig.getDataPool().containsKey(id))
						selectedIds.add(id);
					else if (atlasConfig.getMapPool().containsKey(id)) {

						selectedIds.add(id);

						// Adding dependent DPEs also
						for (DpRef<DpLayer<?, ? extends ChartStyle>> dpl : atlasConfig
								.getMapPool().get(id).getLayers()) {
							selectedIds.add(dpl.getTargetId());
						}

						// update the jtree model
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								updateModel();
							}
						});
					}

				}
				// System.out.println(selectedIDs.size());
			}

			@Override
			public void editingCanceled(ChangeEvent e) {
				LOGGER.debug("editingCanceled"+ e);
			}
		});

	}

	public CheckableAtlasJTree(AtlasConfig ac) {
		setAtlasConfig(ac);
	}

	//
	// public List<Map> getCheckedMaps() {
	// ArrayList<Map> maps = new ArrayList<Map>();
	// return maps;
	// }
	//
	// public List<DpEntry> getCheckedDpes() {
	// ArrayList<DpEntry> dpes = new ArrayList<DpEntry>();
	// return dpes;
	// }

	public void setAtlasConfig(AtlasConfig atlasConfig) {

		this.atlasConfig = atlasConfig;

		selectedIds.clear();

		updateModel();
	}

	public void updateModel() {

		TreePath backup = getSelectionPath();

		AtlasJTreeCheckBoxNode[] mapOptions = new AtlasJTreeCheckBoxNode[] {};
		AtlasJTreeCheckBoxNode[] dpeOptions = new AtlasJTreeCheckBoxNode[] {};

		for (Map map : atlasConfig.getMapPool().values()) {
			mapOptions = LangUtil.extendArray(mapOptions,
					new AtlasJTreeCheckBoxNode(map, selectedIds.contains(map
							.getId())));
		}

		for (DpEntry dpe : atlasConfig.getDataPool().values()) {
			dpeOptions = LangUtil.extendArray(dpeOptions,
					new AtlasJTreeCheckBoxNode(dpe, selectedIds.contains(dpe
							.getId())));
		}

		Vector mapVector = new NamedVector(GeopublisherGUI
				.R("MapPoolJTable.Border.Title"), mapOptions);
		Vector dpeVector = new NamedVector(GeopublisherGUI
				.R("DataPoolJTable.Border.Title"), dpeOptions);
		Object rootNodes[] = { mapVector, dpeVector };
		Vector rootVector = new NamedVector(atlasConfig.getTitle().toString(),
				rootNodes);
		setModel(createTreeModel(rootVector));

		expandRow(1);
		expandRow(0);

		expandPath(backup);
	}

	public Set<String> getSelectedIds() {
		return selectedIds;
	}

	public class AtlasJTreeCheckBoxNode extends CheckBoxNode {
		public String id;
		String title;

		public AtlasJTreeCheckBoxNode(Map map, boolean selected) {
			super(map.getId(), selected);
			id = map.getId();
			title = map.getTitle().toString();
		}

		//		
		// public AtlasJTreeCheckBoxNode(String id, boolean selected) {
		// super(id, selected);
		// this.id = id;
		// title = id+" doof";
		// }

		public AtlasJTreeCheckBoxNode(DpEntry dpe, boolean selected) {
			super(dpe.getId(), selected);

			id = dpe.getId();
			title = dpe.getTitle().toString();
		}

		@Override
		public String toString() {
			return id;
		}

		public String getTitle() {
			return title;
		}
	}

}
