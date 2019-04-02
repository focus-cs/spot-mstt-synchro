package pco.schneider.api.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

/**
 * @author brt
 * 
 */
public class ProjectDTO implements Serializable
{
  private static final long serialVersionUID = 1L;

  private Hashtable<Integer, Object> fields = new Hashtable<Integer, Object>();

  public void setField(int key, Object value)
  {
    if (value != null)
    {
      if (value instanceof String)
        fields.put(key, ((String) value).replaceAll("\n", " "));
      else
        fields.put(key, value);
    }
  }

  public Date getDateField(int key)
  {
    if (fields.containsKey(key))
      return (Date) fields.get(key);
    else
      return new Date(0);
  }

  public String getStringField(String key)
  {
    return (String) fields.get(Integer.parseInt(key));
  }

  public String getStringField(int key)
  {
    return (String) fields.get(key);
  }

  public Integer getIntField(int key)
  {
    return (Integer) fields.get(key);
  }
}
