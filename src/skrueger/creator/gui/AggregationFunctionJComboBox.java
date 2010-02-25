package skrueger.creator.gui;

import java.awt.event.ComponentListener;

import javax.swing.JComboBox;

import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.jfree.feature.style.FeatureChartUtil;
import schmitzm.jfree.feature.style.FeatureChartStyle.AggregationFunction;
import skrueger.sld.ASUtil;

public class AggregationFunctionJComboBox extends JComboBox {
	public AggregationFunctionJComboBox() {
		super(AggregationFunction.values());
		setRenderer(FeatureChartStyle.AggregationFunction.getListCellRenderer());
		ASUtil.addMouseWheelForCombobox(this, false);
	}

}
