/**
 * 
 */
package pco.schneider.main.exception;

/**
 * Exception class: project id rank inconsistency
 * 
 * @author nbenhmidane
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ProjectIdListException extends Exception
{

  /**
   * creates a new instance of ProjectIdRankException
   */
  public ProjectIdListException()
  {
  }

  /**
   * creates a new instance of ProjectIdRankException
   * 
   * @param message
   */
  public ProjectIdListException(String message)
  {
    super(message);
  }

  /**
   * creates a new instance of ProjectIdRankException
   * 
   * @param cause
   */
  public ProjectIdListException(Throwable cause)
  {
    super(cause);
  }

  /**
   * creates a new instance of ProjectIdRankException
   * 
   * @param message
   * @param cause
   */
  public ProjectIdListException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
