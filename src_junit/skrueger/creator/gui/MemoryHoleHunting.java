package skrueger.creator.gui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.swing.JMapPane;
import org.junit.Test;

import schmitzm.geotools.GTUtil;
import schmitzm.geotools.gui.GeoMapPane;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.lang.LangUtil;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.layer.DpLayer;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.gui.AtlasMapLayer;
import skrueger.atlas.map.Map;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.GPDialogManager;
import skrueger.creator.TestingUtil;
import skrueger.creator.gui.map.DesignMapViewJDialog;
import skrueger.geotools.XMapPane;

import com.vividsolutions.jts.geom.Envelope;

public class MemoryHoleHunting extends TestCase {

	final static Random rand = new Random();

	@Test
	public static void testZoomAroundInTheSameMap() throws Exception {
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();

		final Map map = ace.getMapPool().get(ace.getMapPool().firstKey());

		final DesignMapViewJDialog mapComposer = GPDialogManager.dm_MapComposer
				.getInstanceFor(map, null, map);
		final SelectableXMapPane mapPane = mapComposer.getDesignMapView()
				.getGeoMapPane().getMapPane();

		int count = 0;
		while (count++ < 700) {

			LangUtil.gcTotal();
			System.out.println("\n" + TestingUtil.showHeap() + "% belegt im "
					+ count + ". durchlauf\n");

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					Rectangle bounds = mapPane.getBounds();
					Point center = new Point(
							(int) (bounds.x + bounds.width * rand.nextDouble()),
							(int) (bounds.y + bounds.height * rand.nextDouble()));
					mapPane.zoomTo(center, rand.nextDouble() + .5);
				}
			});

			if (count % 5 == 0)
				mapPane.zoomToLayer(rand.nextInt(map.getLayers().size() - 1));

			Thread.sleep(1000);
		}
		GPDialogManager.dm_MapComposer.disposeInstanceFor(map);
	}

	@Test
	public static void testAlwaysOpenTheSameMap() throws Exception {
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();

		final Map map = ace.getMapPool().get(ace.getMapPool().firstKey());

		int count = 0;
		while (count++ < 500) {

			LangUtil.gcTotal();
			System.out.println("\n" + TestingUtil.showHeap() + "% belegt im "
					+ count + ". durchlauf\n");

			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					DesignMapViewJDialog mapComposer = GPDialogManager.dm_MapComposer
							.getInstanceFor(map, null, map);

				}
			});

			Thread.sleep(5000);
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					GPDialogManager.dm_MapComposer.disposeInstanceFor(map);
				}
			});
		}
	}

	@Test
	public static void testAlwaysOpenTheSameMapManual() throws Exception {
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();

		final Map map = ace.getMapPool().get(ace.getMapPool().firstKey());

		int count = 0;
		while (count++ < 500) {
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			
			DesignMapViewJDialog mapComposer = GPDialogManager.dm_MapComposer
					.getInstanceFor(map, null, map);
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			
			while (mapComposer.isVisible()) {
				Thread.sleep(1000);
				LangUtil.gcTotal();
				System.out.println("\n" + TestingUtil.showHeap() + "% belegt im "
						+ count + ". durchlauf\n");
			}
			
		}
	}

	@Test
	public static void testAlwaysOpenTheSameGeoMapPane() throws Exception {
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();

		final Map map = ace.getMapPool().get(ace.getMapPool().firstKey());

		DpRef<DpLayer<?, ? extends ChartStyle>> dpRef = map.getLayers().get(0);
		final DpLayerVectorFeatureSource dpl1 = (DpLayerVectorFeatureSource) dpRef
				.getTarget();

		int count = 0;
		while (count++ < 500) {

			LangUtil.gcTotal();
			System.out.println("\n" + TestingUtil.showHeap() + "% belegt im "
					+ count + ". durchlauf\n");

			JFrame frame = new JFrame();
			GeoMapPane gmp = new GeoMapPane();
			gmp.getMapContext().addLayer(
					new AtlasMapLayer(dpl1.getGeoObject(), dpl1.getStyle()));
			frame.add(gmp);
			frame.setPreferredSize(new Dimension(400, 400));
			frame.pack();
			frame.setVisible(true);

			Thread.sleep(4000);

			gmp.dispose();
			frame.dispose();
		}
	}

	@Test
	public static void testAlwaysOpenTheSameXMapPane() throws Exception {
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();

		final Map map = ace.getMapPool().get(ace.getMapPool().firstKey());

		DpRef<DpLayer<?, ? extends ChartStyle>> dpRef = map.getLayers().get(0);
		final DpLayerVectorFeatureSource dpl1 = (DpLayerVectorFeatureSource) dpRef
				.getTarget();

		int count = 0;
		while (count++ < 500) {

			JFrame frame = new JFrame();
			XMapPane xmapPane = new XMapPane();
			xmapPane.getMapContext().addLayer(
					new AtlasMapLayer(dpl1.getGeoObject(), dpl1.getStyle()));
			frame.add(xmapPane);
			frame.setPreferredSize(new Dimension(600, 600));
			frame.pack();
			SwingUtil.setRelativeFramePosition(frame, null,
					SwingUtil.BOUNDS_OUTER, SwingUtil.EAST);
			frame.setVisible(true);

			LangUtil.gcTotal();
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			LangUtil.gcTotal();
			System.out.println("\n" + TestingUtil.showHeap() + "% belegt im "
					+ count + ". durchlauf\n");
			Thread.sleep(5000);
			
			xmapPane.dispose();
			frame.dispose();
			
			xmapPane.setLocalContext(null);
			xmapPane = null;
			frame = null;
			
//			Thread.sleep(1000);
			dpl1.uncache();

//			LangUtil.gcTotal();
//			LangUtil.gcTotal();
//			Thread.sleep(1000);
//			LangUtil.gcTotal();
//			LangUtil.gcTotal();
			
			Thread.sleep(5000);

		}
	}

	@Test
	public static void testAlwaysOpenTheSameRenderer() throws Exception {
		// AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();
		//
		// final Map map = ace.getMapPool().get(ace.getMapPool().firstKey());
		//		
		// DpRef<DpLayer<?, ? extends ChartStyle>> dpRef =
		// map.getLayers().get(0);
		// final DpLayerVectorFeatureSource dpl1 = (DpLayerVectorFeatureSource)
		// dpRef.getTarget();

		int count = 0;
		while (count++ < 500) {

			LangUtil.gcTotal();
			System.out.println("\n" + TestingUtil.showHeap() + "% belegt im "
					+ count + ". durchlauf\n");

			GTRenderer createGTRenderer = GTUtil.createGTRenderer();
			Thread.sleep(300);

			createGTRenderer = null;
		}
	}

	@Test
	public static void testRenderer() throws Exception {
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();

		final Map map = ace.getMapPool().get(ace.getMapPool().firstKey());

		DpRef<DpLayer<?, ? extends ChartStyle>> dpRef = map.getLayers().get(0);
		final DpLayerVectorFeatureSource dpl1 = (DpLayerVectorFeatureSource) dpRef
				.getTarget();

		int count = 0;
		while (count++ < 500) {

			LangUtil.gcTotal();
			System.out.println("\n" + TestingUtil.showHeap() + "% belegt im "
					+ count + ". durchlauf\n");

			GTRenderer renderer = GTUtil.createGTRenderer();
			JFrame frame = new JFrame();
			JMapPane xmapPane = new JMapPane();
			xmapPane.setPreferredSize(new Dimension(400, 400));
			DefaultMapContext context = new DefaultMapContext();
			context.addLayer(new DefaultMapLayer(dpl1.getGeoObject(), dpl1
					.getStyle()));
			frame.add(xmapPane);
			frame.pack();
			frame.setVisible(true);

			renderer.setContext(context);

			renderer.paint((Graphics2D) xmapPane.getGraphics(), xmapPane
					.getBounds(), dpl1.getEnvelope());

			Thread.sleep(3000);

			frame.dispose();

		}
	}

	@Test
	public static void testExecutor() throws Exception {
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();

		final Map map = ace.getMapPool().get(ace.getMapPool().firstKey());

		DpRef<DpLayer<?, ? extends ChartStyle>> dpRef = map.getLayers().get(0);
		final DpLayerVectorFeatureSource dpl1 = (DpLayerVectorFeatureSource) dpRef
				.getTarget();

		// skrueger.geotools.RenderingExecutor ex = new
		// skrueger.geotools.RenderingExecutor();

		int count = 0;
		while (count++ < 500) {

			LangUtil.gcTotal();
			System.out.println("\n" + TestingUtil.showHeap() + "% belegt im "
					+ count + ". durchlauf\n");

			GTRenderer renderer = GTUtil.createGTRenderer();
			JFrame frame = new JFrame();
			JMapPane xmapPane = new JMapPane();
			xmapPane.setPreferredSize(new Dimension(400, 400));
			DefaultMapContext context = new DefaultMapContext();
			context.addLayer(new DefaultMapLayer(dpl1.getGeoObject(), dpl1
					.getStyle()));

			frame.add(xmapPane);
			frame.pack();
			frame.setVisible(true);

			Envelope env = dpl1.getEnvelope();
			Rectangle bounds = xmapPane.getBounds();

			AffineTransform screenToWorld = new AffineTransform(
			// Genauso wie die Fenster-Koordinaten, werden die
			// Longitude-Koordinaten
					// nach rechts (Osten) hin groesser
					// --> positive Verschiebung
					env.getWidth() / bounds.getWidth(),
					// keine Verzerrung
					0.0, 0.0,
					// Waehrend die Fenster-Koordinaten nach unten hin groesser
					// werden,
					// werden Latitude-Koordinaten nach Sueden hin keiner
					// --> negative Verschiebung
					-env.getHeight() / bounds.getHeight(),
					// Die Longitude-Koordinaten werden nach Osten hin groesser
					// --> obere linke Ecke des Fensters hat also den
					// Minimalwert
					env.getMinX(),
					// Die Latitude-Koordinaten werden nach Norden hin groesser
					// --> obere linke Ecke des Fensters hat also den
					// Maximalwert
					env.getMaxY());

			AffineTransform worldToScreen = screenToWorld.createInverse();

			renderer.setContext(context);
			//			
			// ex.submit(new ReferencedEnvelope(env,
			// context.getCoordinateReferenceSystem()), bounds, (Graphics2D)
			// xmapPane.getGraphics(), renderer, worldToScreen);
			// ex.submit(new ReferencedEnvelope(env,
			// context.getCoordinateReferenceSystem()), bounds, (Graphics2D)
			// xmapPane.getGraphics(), renderer, worldToScreen);
			// ex.submit(new ReferencedEnvelope(env,
			// context.getCoordinateReferenceSystem()), bounds, (Graphics2D)
			// xmapPane.getGraphics(), renderer, worldToScreen);
			// ex.submit(new ReferencedEnvelope(env,
			// context.getCoordinateReferenceSystem()), bounds, (Graphics2D)
			// xmapPane.getGraphics(), renderer, worldToScreen);
			// ex.submit(new ReferencedEnvelope(env,
			// context.getCoordinateReferenceSystem()), bounds, (Graphics2D)
			// xmapPane.getGraphics(), renderer, worldToScreen);
			//			
			// renderer.paint((Graphics2D) xmapPane.getGraphics(),
			// xmapPane.getBounds(), dpl1.getEnvelope());
			Thread.sleep(1000);

			xmapPane.repaint();

			Thread.sleep(1000);

			frame.dispose();

		}
	}

	public static void main(String[] args) throws Exception {
		// testZoomAroundInTheSameMap();
		// testRenderer();
//		testAlwaysOpenTheSameXMapPane();
		// testExecutor();
//		testAlwaysOpenTheSameGeoMapPane();
		testAlwaysOpenTheSameMapManual();
//		testAlwaysOpenTheSameMap();
	}

}
