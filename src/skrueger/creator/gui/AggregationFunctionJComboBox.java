package skrueger.creator.gui;

import javax.swing.JComboBox;

import schmitzm.jfree.feature.AggregationFunction;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import skrueger.sld.ASUtil;

public class AggregationFunctionJComboBox extends JComboBox {
	public AggregationFunctionJComboBox() {
		super(AggregationFunction.values());
		setRenderer(AggregationFunction.getListCellRenderer());
		ASUtil.addMouseWheelForCombobox(this, false);
	}

}
