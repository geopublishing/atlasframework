import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import schmitzm.swing.TestingUtil;

public class HtmlToImage {

	public static BufferedImage createImage(URL url) throws IOException,
			InterruptedException {
		// /*
		// * First, get the contents of the HTML file
		// */
		// StringBuilder sb = new StringBuilder();
		// BufferedReader reader = new BufferedReader(new InputStreamReader(
		// (InputStream) url
		// .getContent(new Class<?>[] { InputStream.class })));
		// try {
		// String line = null;
		// {
		// while ((line = reader.readLine()) != null) {
		// sb.append(line);
		// sb.append('\n');
		// }
		// }
		// } finally {
		// reader.close();
		// }
		/*
		 * Setup a JEditorPane
		 */
		JEditorPane pane = new JEditorPane();
		pane.setEditable(false);
		pane.setContentType("text/html");
		pane.setText("<html>asjdkALSD J<img src='http://forums.sun.com/im/silver-star.gif'/>ALödsj aLDJ SADKlsj aDLSJKL</html>");
		pane.setSize(pane.getPreferredSize());
		/*
		 * Create a BufferedImage
		 */
		BufferedImage image = new BufferedImage(pane.getWidth(),
				pane.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		/*
		 * Have the image painted by SwingUtilities
		 */
		JPanel container = new JPanel();
		SwingUtilities.paintComponent(g, pane, container, 0, 0,
				image.getWidth(), image.getHeight());
		g.dispose();
		return image;

	}

	public static void main(String[] args) throws Throwable {
			BufferedImage image = HtmlToImage.createImage(new URL(
					"http://forums.sun.com/thread.jspa?threadID=5345288"));
			TestingUtil.testGui(image,5);

	}

}
