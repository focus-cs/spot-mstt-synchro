/**
 * 
 */
package pco.schneider.api;

import com.sciforma.psnext.api.DataViewRow;
import com.sciforma.psnext.api.Global;
import com.sciforma.psnext.api.Organization;
import com.sciforma.psnext.api.PSException;
import com.sciforma.psnext.api.Session;
import com.sciforma.psnext.api.SystemData;
import com.sciforma.psnext.api.User;
import fr.sciforma.psconnect.service.exception.TechnicalException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jboss.logging.Logger;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import pco.schneider.api.dao.DBProcessor;

/**
 * This class provides all needed functions to communicate with Sciforma (MSTT & SPOT)
 * 
 * @author nbenhmidane
 * @version 1.0
 */
public class SciformaManager
{
  private static final String NAME_FIELD = "Name";

  /**
   * Internal SciformaManager logger
   */
  private static final Logger LOG = Logger.getLogger(SciformaManager.class);

  /**
   * Session to manage
   */
  private final transient Session session;

  /**
   * List of User objects, each of which represents a Sciforma User
   */
  private transient List<User> userList = null;

  /**
   * List of User objects, each of which represents a Sciforma User
   */
  private transient Map<String, Organization> orgaList = new HashMap<>();

  /**
   * SciformaManager constructor
   * 
   * @param session session to manage
   * @param version version of projects to manage
   * @throws PSException if unable to get user list from given session
   */
  @SuppressWarnings("unchecked")
  public SciformaManager(Session session) throws PSException
  {
    this.session = session;
    this.userList = session.getUserList();
    getOrganisations();
  }

  @SuppressWarnings("unchecked")
  public void getOrganisations() throws PSException
  {
    Organization rootOrga = (Organization) session.getSystemData(SystemData.ORGANIZATIONS);
    orgaList.put(rootOrga.getStringField(NAME_FIELD), rootOrga);
    List<Organization> orgaChildList = rootOrga.getChildren();
    for (Organization orga : orgaChildList)
    {
      orgaList.put(orga.getParent() + "." + orga.getStringField(NAME_FIELD), orga);
      getOrganisations(orga);
    }
  }

  @SuppressWarnings("unchecked")
  private void getOrganisations(Organization orga) throws PSException
  {
    List<Organization> orgaChildList = orga.getChildren();
    for (Organization node : orgaChildList)
    {
      orgaList.put(node.getParent() + "." + node.getStringField(NAME_FIELD), node);
      getOrganisations(node);
    }
  }

  public String getOrganisationValue(String name, String fieldName) throws PSException
  {
    if (name != null && !"".equals(name))
    {
      Organization orga = orgaList.get(name);
      if (orga != null) return orga.getStringField(fieldName);
    }
    return "";
  }

  /**
   * Load data view row list corresponding to the given view name
   * 
   * @param session related session
   * @param dataViewName view name
   * @return data view row list
   */
  @SuppressWarnings("unchecked")
  public List<DataViewRow> getDataViewRowList(Session session, String dataViewName) throws NullPointerException
  {
    List<DataViewRow> dataViewRows = null;
    try
    {
      dataViewRows = session.getDataViewRowList(dataViewName, new Global());
    }
    catch (PSException e)
    {
      LOG.error("Unexpected error while loading " + dataViewName, e);
    }

    if (dataViewRows == null)
    {
      LOG.error(dataViewName + " not found.");
      throw new NullPointerException(dataViewName + " not found.");
    }

    return dataViewRows;
  }

  public String getUserName(String name, String fieldName) throws PSException
  {
    if (name != null && !"".equals(name))
    {
      for (User user : userList)
      {
        if (name.equals(user.getStringField(NAME_FIELD))) return user.getStringField(fieldName);
      }
    }
    return "";
  }

  public DBProcessor getDBProcessor(String dbPropertiesFile)
  {
    Properties dbProperties;
    try
    {
      dbProperties = getProperties(dbPropertiesFile);
    }
    catch (FileNotFoundException e)
    {
      throw new TechnicalException(e, "<" + dbPropertiesFile + "> can't be found in classpath.");
    }

    String driver = dbProperties.getProperty("psnext.driver", "net.sourceforge.jtds.jdbc.Driver");
    try
    {
      Class.forName(driver);
    }
    catch (ClassNotFoundException e)
    {
      throw new TechnicalException(e, "JDBC driver <" + driver + "> can't be found in classpath.");
    }

    SingleConnectionDataSource dataSource = new SingleConnectionDataSource(dbProperties.getProperty("psnext_db.url"), dbProperties.getProperty("psnext_db.login"),
          dbProperties.getProperty("psnext_db.password"), false);

    return new DBProcessor(dbProperties, dataSource);
  }

  protected Properties getProperties(String path) throws FileNotFoundException
  {
    Properties dbProperties = new Properties();
    // InputStream resourceAsStream = getClass().getResourceAsStream(path);
    BufferedReader resourceAsStream = new BufferedReader(new FileReader(path));

    // if (resourceAsStream == null)
    // {
    // throw new RuntimeException("<" + path + "> not found in classpath.");
    // }
    //
    try
    {
      dbProperties.load(resourceAsStream);
    }
    catch (IOException e)
    {
      throw new RuntimeException("<" + path + "> can't be read.");
    }

    return dbProperties;
  }
}
