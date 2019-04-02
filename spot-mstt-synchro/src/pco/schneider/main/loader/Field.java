/**
 * 
 */
package pco.schneider.main.loader;

/**
 * Represents a field as metadata
 * 
 * @author nbenhmidane
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class Field implements Comparable
{
  /**
   * Used to order and compare with another field
   */
  private int rank;

  /**
   * Field name
   */
  private String name;

  /**
   * Field name
   */
  private String dbName;

  /**
   * Field format
   * <p>
   * For available values @see FieldFormat
   */
  private FieldFormat format;

  /**
   * Field value
   */
  private String value;

  /**
   * Default Field constructor
   * 
   * @param rank field rank
   * @param name field name
   * @param format field format
   * @param value field value (optional : can be null)
   */
  public Field(int rank, String name, String dbName, FieldFormat format, String value)
  {
    super();
    this.rank = rank;
    this.name = name;
    this.dbName = dbName;
    this.format = format;
    this.value = value;
  }

  /**
   * Return current field rank
   * 
   * @return Integer that represents current field rank
   */
  public int getRank()
  {
    return rank;
  }

  /**
   * Set given value to field rank
   * 
   * @param Integer that represents field rank
   */
  public void setRank(int rank)
  {
    this.rank = rank;
  }

  /**
   * Return current field name
   * 
   * @return String that represents current field name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Set given value to field name
   * 
   * @param String that represents field name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Return current field format
   * 
   * @return FieldFormat that represents current field format
   *         <p>
   * @see FieldFormat
   */
  public FieldFormat getFormat()
  {
    return format;
  }

  /**
   * Set given value to field format
   * 
   * @param FieldFormat that represents field format
   *          <p>
   * @see FieldFormat for available values
   */
  public void setFormat(FieldFormat format)
  {
    this.format = format;
  }

  /**
   * Set given value to field value
   * 
   * @param String that represents field value
   */
  public String getValue()
  {
    return value;
  }

  /**
   * Set given value to field value
   * 
   * @param String that represents field value
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * method to compare field to another one
   */
  public int compareTo(Object otherField)
  {
    int rank1 = ((Field) otherField).getRank();
    int rank2 = this.getRank();
    if (rank1 > rank2)
      return -1;
    else if (rank1 == rank2)
      return 0;
    else
      return 1;
  }

  public String getDbName()
  {
    return this.dbName;
  }

  public void setDbName(String dbName)
  {
    this.dbName = dbName;
  }
}
