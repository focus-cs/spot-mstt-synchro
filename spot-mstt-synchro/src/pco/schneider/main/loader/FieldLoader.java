/**
 * 
 */
package pco.schneider.main.loader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * This class is a loader able to load metadata from a given properties file and construct a list of Fields
 * 
 * @author nbenhmidane
 * @version 1.0
 */
public class FieldLoader
{
  /**
   * Internal FieldLoader logger
   */
  private static final Logger LOG = Logger.getLogger(FieldLoader.class);

  /**
   * Input properties filename
   */
  private String propertiesFile;

  /**
   * Field list contains all Fields generated from loaded metadata
   */
  private List<Field> fieldList;

  /**
   * Separator of input properties file
   */
  private static final String SEPARATOR = ";";

  /**
   * Default constructor
   * 
   * @param propertiesFile Input properties filename
   */
  public FieldLoader(String propertiesFile)
  {
    this.propertiesFile = propertiesFile;
    fieldList = new ArrayList<Field>();
  }

  /**
   * Load from input properties file and creates field list
   * 
   * @throws Exception Possible exceptions FileNotFoundException, NumberFormatException, IOException
   */
  public void load() throws Exception
  {
    String line = "";
    BufferedReader bReader = null;

    try
    {
      bReader = new BufferedReader(new FileReader(propertiesFile));
      while ((line = bReader.readLine()) != null)
      {
        LOG.debug("Loading from line " + line);

        String[] fieldProperties = line.split(SEPARATOR);

        if (fieldProperties.length < 4)
        {
          LOG.error("Inconsitent data found in line : " + line + ". Not enough arguments. \nField generation skipped. Loading process continues.");
          continue;
        }

        if (fieldProperties.length >= 5)
        {
          fieldList.add(new Field(Integer.parseInt(fieldProperties[0]), fieldProperties[1], fieldProperties[2], FieldFormat.valueOf(fieldProperties[3]), fieldProperties[4]));
        }
        else
        {
          fieldList.add(new Field(Integer.parseInt(fieldProperties[0]), fieldProperties[1], fieldProperties[2], FieldFormat.valueOf(fieldProperties[3]), null));
        }
      }
    }
    catch (FileNotFoundException e)
    {
      LOG.error("Unable to open " + propertiesFile, e);
      throw e;
    }
    catch (NumberFormatException e)
    {
      LOG.error("Inconsitent data found in line : " + line + "Field generation skipped. Loading process continues.", e);
    }
    catch (IOException e)
    {
      LOG.error("Unable to read lines in " + propertiesFile, e);
      throw e;
    }
    catch (Exception e)
    {
      LOG.error("Unexpected error while loading/creating field list. One possible reason is unsupported field format in this line : " + line, e);
      throw e;
    }
    finally
    {
      if (bReader != null)
      {
        try
        {
          bReader.close();
        }
        catch (IOException e)
        {
          LOG.error("Unable to close " + propertiesFile, e);
          throw e;
        }
      }
    }
  }

  /**
   * Return current field list
   * 
   * @param sort If TRUE, sort field list
   * @return field list
   */
  @SuppressWarnings("unchecked")
  public List<Field> getFieldList(boolean sort)
  {
    // TODO verify in case of null list (client didn't load() for example)
    if (sort)
    {
      Collections.sort(fieldList);
    }
    return fieldList;
  }

  /**
   * Search field by given name and return it if found
   * 
   * @param fieldName field name to search
   * @return field if found
   */
  public Field getFieldByName(String fieldName)
  {
    if (fieldList != null)
    {
      for (Field field : fieldList)
      {
        if (field.getName().equals(fieldName))
        {
          return field;
        }
      }
    }
    return null;
  }
}
