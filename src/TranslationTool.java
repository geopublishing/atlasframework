import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


/**
 * This tool helps to find missing frensh translations 
 *
 */
public class TranslationTool {
	
	static File GPenglish = new File ("/home/stefan/EigeneDateien/code/atlas/trunk/Atlas-Framework/bin/skrueger/creator/resource/locales/Geopublisher.properties");
	static File GPfrensh = new File ("/home/stefan/EigeneDateien/code/atlas/trunk/Atlas-Framework/bin/skrueger/creator/resource/locales/Geopublisher_fr.properties");
	
	static File ASenglish = new File ("/home/stefan/EigeneDateien/code/atlas/trunk/Atlas-Framework/src/skrueger/sld/resources/locales/AtlasStylerTranslation.properties");
	static File ASfrensh = new File ("/home/stefan/EigeneDateien/code/atlas/trunk/Atlas-Framework/src/skrueger/sld/resources/locales/AtlasStylerTranslation_fr.properties");
	
	static File AVenglish = new File ("/home/stefan/EigeneDateien/code/atlas/trunk/Atlas-Framework/src/skrueger/atlas/resource/locales/AtlasViewerTranslation.properties");
	static File AVfrensh = new File ("/home/stefan/EigeneDateien/code/atlas/trunk/Atlas-Framework/src/skrueger/atlas/resource/locales/AtlasViewerTranslation_fr.properties");
	
	
	private static HashMap<String,String> frenshTranslations = new HashMap<String, String>();

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		
		BufferedReader en_Reader; 
		BufferedReader fr_Reader;
		
		int mode = 3;
		
		if (mode == 1) {
			en_Reader = new BufferedReader(new FileReader(GPenglish));
			fr_Reader = new BufferedReader(new FileReader(GPfrensh));
		} else if (mode == 2) {
			en_Reader = new BufferedReader(new FileReader(ASenglish));
			fr_Reader = new BufferedReader(new FileReader(ASfrensh));
		} else {
			en_Reader = new BufferedReader(new FileReader(AVenglish));
			fr_Reader = new BufferedReader(new FileReader(AVfrensh));
		}
		

		// Read all frensh translations keys into a HashMap
		String  readLine = "start";
		while (readLine != null) {
			readLine = fr_Reader.readLine();
			if (readLine != null && !readLine.startsWith("#")) {
				int indexOf = readLine.indexOf("=");
				if (indexOf == -1) continue; 
				String key =  readLine.substring(0,indexOf).trim();
				String value =  readLine.substring(indexOf+1).trim();
				frenshTranslations .put(key.trim(),value.trim());
			}
		}
		
		System.out.println(frenshTranslations.size()+" frensh imported");

		readLine = "start";
		int missing =0;
		while (readLine != null) {
			readLine = en_Reader.readLine();
			
			if (readLine != null && !readLine.startsWith("#")) {
				int indexOf = readLine.indexOf("=");
				if (indexOf == -1) continue;
				String key =  readLine.substring(0,indexOf).trim();
				String value =  readLine.substring(indexOf+1).trim();
				if (!frenshTranslations.containsKey(key)) {
					System.out.println("#"+key+"="+value+"\n");
					missing++;
				}
			}
		}
		System.out.println("missing = "+missing);
	}



}
