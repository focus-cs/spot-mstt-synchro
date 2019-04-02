/**
 * 
 */
package pco.schneider.main.loader;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Possible field formats that can be managed by the application
 * 
 * @author nbenhmidane
 * @version 1.0
 */
public enum FieldFormat
{
  TEXT, DATE, DECIMAL, USER, CONF, INTEGER, ORGANISATION;

  public String getAllFormats()
  {
    String allFormats = "";

    List<FieldFormat> fieldFormatList = new ArrayList<FieldFormat>(EnumSet.allOf(FieldFormat.class));
    for (FieldFormat fieldFormat : fieldFormatList)
    {
      allFormats.concat(fieldFormat.toString());
    }
    return allFormats;
  }
}
