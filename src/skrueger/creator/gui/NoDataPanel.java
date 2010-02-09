package skrueger.creator.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JTextField;

import org.geotools.util.WeakHashSet;
import org.opengis.feature.simple.SimpleFeatureType;

import net.miginfocom.swing.MigLayout;
import schmitzm.swing.JPanel;
import skrueger.AttributeMetadata;
import skrueger.geotools.AttributeMetadataMap;

/**
 * This {@link JPanel} displays a short list of NODATA-Values for an attribute
 * and provides a button to edit the NODATA-Values ina modal dialog. The
 * attribute of which the NODATA is displayed can be changed by
 * {@link #setAttribute} methods.<br/> {@link PropertyChangeListener}s may be
 * registered to get Events of type {@link #PROPERTY_NODATAVALUES} when the
 * NODATA values are changed.
 */
public class NoDataPanel extends JPanel {

	/** Used for the PropertyChangeListener **/
	public static final String PROPERTY_NODATAVALUES = "NODATA Values";

	private JTextField noDataTextField;
	private final AttributeMetadataMap attributeMetaDataMap;
	private AttributeMetadata atm;
	private final SimpleFeatureType schema;

	private WeakHashSet<PropertyChangeListener> listeners;

	/**
	 * @param attributeMetaDataMap
	 * @param attributeName
	 *            The selected attribute
	 */
	public NoDataPanel(AttributeMetadataMap attributeMetaDataMap,
			String attributeName, final SimpleFeatureType schema) {
		super(new MigLayout());
		this.attributeMetaDataMap = attributeMetaDataMap;
		this.schema = schema;

		add(new JButton(new AbstractAction("NODATA-Werte" + ":") { // i8n

					@Override
					public void actionPerformed(ActionEvent e) {
						NoDataEditListDialog nodataEditListDialog = new NoDataEditListDialog(
								NoDataPanel.this, schema.getDescriptor(
										atm.getName()).getType().getBinding(),
								atm);
						nodataEditListDialog.setVisible(true);

						if (!nodataEditListDialog.isCancelled()){
							getNoDataTextfield().setText(atm.getNoDataValuesFormatted());
							getNoDataTextfield().setToolTipText(atm.getNoDataValuesFormatted());

							NoDataPanel.this.firePropertyChange(PROPERTY_NODATAVALUES, null, atm.getNodataValues());
						}
					}
				}));

		add(getNoDataTextfield());

		setAttribute(attributeName);
	}


	public void setAttribute(String attributeName) {

		this.atm = attributeMetaDataMap.get(attributeName);

		if (atm == null) {
			getNoDataTextfield().setText("");
			getNoDataTextfield().setToolTipText("");
			return;
		}

		getNoDataTextfield().setText(atm.getNoDataValuesFormatted());
		getNoDataTextfield().setToolTipText(atm.getNoDataValuesFormatted());
	}

	private JTextField getNoDataTextfield() {
		if (noDataTextField == null) {
			noDataTextField = new JTextField(60);
			noDataTextField.setEditable(false);
		}
		return noDataTextField;
	}

}
