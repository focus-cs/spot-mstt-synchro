package pco.schneider.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import pco.schneider.main.loader.Field;

/**
 * this class allows to fetch a list of elements after have done a request
 */
public class DBProcessor extends NamedParameterJdbcDaoSupport
{
  public static final Logger LOG = Logger.getLogger(DBProcessor.class);
  private String select;

  /**
   * The auto-commit is disable, to apply the change call commit method.
   * 
   * @param query requete parametree
   * @param source datasource pour l'acces a la base
   */
  public DBProcessor(Properties properties, DataSource source)
  {
    select = properties.getProperty("psnext.select");
    setDataSource(source);

    // disable auto-commit
    try
    {
      getConnection().setAutoCommit(false);
    }
    catch (Exception e)
    {
      LOG.error("Connot disable auto-commit", e);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  public Collection<ProjectDTO> getProjects(final List<Field> fields)
  {
    LOG.debug("DBProcessor->getProjects");

    // select "Function", "Name", "_Unique_IID" from "Task_PSC_decision" where "ProjectID" = ? and "TaskID" = ?
    Collection<ProjectDTO> projects = getJdbcTemplate().query(select, new RowMapper()
    {
      public Object mapRow(ResultSet rs, int rowNum) throws SQLException
      {
        ProjectDTO project = new ProjectDTO();

        for (int i = 0; i < fields.size(); i++)
        {
          Field field = fields.get(i);
          switch (field.getFormat())
          {
            case DATE:
              try
              {
                project.setField(field.getRank(), rs.getTimestamp(field.getDbName()));
              }
              catch (Exception e)
              {
                // do nothing, no need, default value will be set
              }
              break;
            case TEXT:
              try
              {
                project.setField(field.getRank(), rs.getString(field.getDbName()));
              }
              catch (Exception e)
              {
                project.setField(field.getRank(), "No info available");
              }
              break;
            case DECIMAL:
              try
              {
                project.setField(field.getRank(), rs.getString(field.getDbName()));
              }
              catch (Exception e)
              {
                project.setField(field.getRank(), "No info available");
              }
              break;
            case INTEGER:
              try
              {
                project.setField(field.getRank(), rs.getInt(field.getDbName()));
              }
              catch (Exception e)
              {
                project.setField(field.getRank(), "No info available");
              }
              break;
            default:
              project.setField(field.getRank(), "No info available");
              break;
          }
        }
        return project;
      }
    });
    LOG.info("Nb Projects = " + projects.size());
    return projects;
  }
}
