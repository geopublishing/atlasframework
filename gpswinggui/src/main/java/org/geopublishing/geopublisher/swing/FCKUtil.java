package org.geopublishing.geopublisher.swing;

/**
 * Unitity methods to configure the FCK html editor.
 */
public class FCKUtil {
  /**
   * Creates a comma separated string, where every of the given
   * properties is masked with "&apos;".
   */
  public static String createFCKConfigString(String... props) {
    StringBuffer propStr = new StringBuffer();
    for (int i=0; i<props.length; i++) {
      if ( i > 0 )
        propStr.append(",");
      propStr.append("&apos;").append(props[i]).append("&apos;");
    }
    return propStr.toString();
  }
  
  /**
   * Creates a comma separated string, where every of the given
   * properties is masked with "&apos;". Furthermore the whole
   * property list is surrounded with brackets: <code>[prop,prop,...],\n"</code>
   */
  public static String createFCKToolbarConfigString(String... props) {
    StringBuffer propStr = new StringBuffer("[");
    propStr.append( createFCKConfigString(props) );
    propStr.append("],\n");
    return propStr.toString();
  }

}
