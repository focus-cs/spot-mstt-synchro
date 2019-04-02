/**
 *
 */
package pco.schneider.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import pco.schneider.api.SciformaManager;
import pco.schneider.api.dao.DBProcessor;
import pco.schneider.api.dao.ProjectDTO;
import pco.schneider.main.exception.ProjectIdListException;
import pco.schneider.main.loader.Field;
import pco.schneider.main.loader.FieldFormat;
import pco.schneider.main.loader.FieldLoader;

import com.sciforma.psnext.api.AccessException;
import com.sciforma.psnext.api.DataFormatException;
import com.sciforma.psnext.api.DataViewRow;
import com.sciforma.psnext.api.FieldAccessor;
import com.sciforma.psnext.api.PSException;

import fr.sciforma.psconnect.runner.CmdLinePSConnectRunner;
import fr.sciforma.psconnect.service.exception.BusinessException;
import fr.sciforma.psconnect.service.exception.TechnicalException;
import org.apache.log4j.Logger;

import org.apache.log4j.PropertyConfigurator;

/**
 * Main class
 *
 * @author nbenhmidane
 * @version 1.0
 */
public class SpotMsttSynchro extends CmdLinePSConnectRunner {

    /**
     * Application informations
     */
    public static final String APP_INFO = "MSTT-SPOT Synchro v2.1 - 2016/02/11";

    /**
     * Configuration input folder path
     */
    private static String CONF_DIRECTORY = "conf";

    /**
     * Output folder path
     */
    private static String OUTPUT_DIRECTORY = "out";

    /**
     * Properties filename for logging system
     */
    private static String LOG_FILE = "log4j.properties";

    /**
     * Sciforma connection suffix filename
     */
    private static final String CONNECT = "connect";

    /**
     * Filename for match process between SPOT and MSTT
     */
    private static final String SPOT_MSTT_MATCH_FILENAME = "spot-mstt-match";

    /**
     * Properties file extension
     */
    private static final String PROPERTIES_FILE_EXTENSION = ".properties";

    /**
     * CSV file extension
     */
    private static final String CSV_FILE_EXTENSION = ".csv";

    /**
     * Represents which action will be done
     * <p>
     * Only 2 possible actions : match or extract
     */
    private static int action = 0;

    /**
     * Match action
     */
    private static final int ACTION_MATCH = 1;

    /**
     * Extract action
     */
    private static final int ACTION_EXTRACT = 2;

    /**
     * Target of extract action : SPOT or MSTT
     */
    private static String target;

    // project id column rank
    private int rankProjectID;

    /**
     * Manage projects in working version
     */
    private SciformaManager workingManager;

    /**
     * Separator for input and output files
     */
    private static final String SEPARATOR = ";";

    /**
     * Field name to detect project id column (MSTT or SPOT id)
     */
    private static final String COLUMN_PROJECT_ID = "COLUMN_PROJECT_ID";

    /**
     * Used to write into output files (whatever the action is)
     */
    private BufferedWriter bufferedWriter;

    /**
     * Load all input fields from properties files
     */
    private FieldLoader fieldLoader;

    /**
     * SPOT_MSTT_MATCH data view row name
     */
    private static final String SPOT_MSTT_MATCH = "SPOT_MSTT_MATCH";

    private static final Logger log = Logger.getLogger(SpotMsttSynchro.class);

    /**
     * Main method that checks command arguments and starts the corresponding
     * process (match or extract)
     *
     * @param args 3 possible arguments :
     * <p>
     * 1 - action : match or extract
     * <p>
     * 2- target : spot or mstt
     * <p>
     */
    public static void main(String[] args) {
        // Check command line arguments
        if (args.length >= 3) {
            // Define action
            if ("match".equals(args[0])) {
                action = ACTION_MATCH;
            } else if ("extract".equals(args[0])) {
                action = ACTION_EXTRACT;
            } else {
                printExitError();
                System.exit(1);
            }

            // Define target
            target = args[1];

            // Define configuration folder
            CONF_DIRECTORY = args[2];
            // Check configuration folder consistency
            File confDirectory = new File(CONF_DIRECTORY);
            if (!confDirectory.exists()) {
                System.out.println("Configuration directory " + CONF_DIRECTORY + " does not exists.");
                System.out.println("Make sure to provide a consistent configuration folder.");
                printUsage();
                System.exit(2);
            }

            // Define output folder
            if (args.length >= 4) {
                OUTPUT_DIRECTORY = args[3];
            }

            // If the output folder does not exist, create it
            File outputDirectory = new File(OUTPUT_DIRECTORY);
            if (!outputDirectory.exists()) {
                boolean result = outputDirectory.mkdir();

                if (!result) {
                    System.out.println("Unable to create output directory : " + OUTPUT_DIRECTORY);
                    System.out.println("Application stopped.");
                    System.exit(3);
                }
            }
            // Configure log4j logger
            PropertyConfigurator.configure(CONF_DIRECTORY + File.separator + LOG_FILE);

            // Launch process
            int status = new SpotMsttSynchro()
                    .withPsconnectPropertiesFilename(CONF_DIRECTORY + File.separator + target + CONNECT + PROPERTIES_FILE_EXTENSION)
                    .withLog4jPropertiesFilename(CONF_DIRECTORY + File.separator + LOG_FILE)
                    .execute(args)
                    .getStatusExit();

            // Exit process
            Runtime.getRuntime().exit(status);
        } else {
            printExitError();
            System.exit(1);
        }
    }

    /**
     * Log application information and creates manager for projects in working
     * version
     *
     * @see CmdLinePSConnectRunner
     */
    @Override
    protected void init() throws TechnicalException, BusinessException {
        log.info(APP_INFO);

        try {
            workingManager = new SciformaManager(getSession());
        } catch (PSException e) {
            log.error("Unable to get user list. SciformaManager cannot be created properly.", e);
            throw new TechnicalException("SciformaManager creation failed. Access to User list problem.");
        }
    }

    /**
     * Launch corresponding action process (match or extract)
     *
     * @see CmdLinePSConnectRunner
     */
    @Override
    protected void process() throws TechnicalException {
        switch (action) {
            case ACTION_MATCH:
                try {
                    log.info("*** Matching from " + target + " starts ***");
                    processMatch();
                    log.info("*** Matching done ***");
                } catch (Exception e) {
                    log.error("Match process fail.", e);
                }
                break;

            case ACTION_EXTRACT:
                try {
                    log.info("*** Extraction from " + target + " starts ***");
                    processExtract(target);
                    log.info("*** Extraction done ***");
                } catch (Exception e) {
                    log.error("Extract process fail (for " + target + ").", e);
                }
                break;

            default:
                log.info("No valid action detected, DO NOTHING.");
                log.info("Valid actions are match and extract.");
                break;
        }
    }

    /**
     * Load fields from given properties file and extract corresponding values
     * from SPOT_MSTT_MATCH view then write these values in output CSV file
     *
     * @throws Exception Possible exceptions : NullPointerException,
     * IOException, DataFormatException, PSException
     */
    private void processMatch() throws Exception {
        // Load field list
        loadFields(CONF_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + PROPERTIES_FILE_EXTENSION);

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION));

            List<Field> fieldList = removeConfigurationFields(true);

            writeHeader(fieldList);

            List<DataViewRow> dataViewRowList = workingManager.getDataViewRowList(getSession(), SPOT_MSTT_MATCH);

            if (dataViewRowList != null) {
                for (DataViewRow dataViewRow : dataViewRowList) {
                    log.debug(dataViewRow);
                    writeField(fieldList, dataViewRow);
                }
            } else {
                log.error("Unable to load data view row list of " + SPOT_MSTT_MATCH + "view.");
                throw new NullPointerException("No data view row list of " + SPOT_MSTT_MATCH + "view.");
            }
        } catch (NullPointerException e) {
            log.error("Unable to process match using " + SPOT_MSTT_MATCH, e);
            throw e;
        } catch (IOException e) {
            log.error("Unable to process match using " + SPOT_MSTT_MATCH, e);
            throw e;
        } catch (DataFormatException e) {
            log.error("Unable to process match using " + SPOT_MSTT_MATCH, e);
            throw e;
        } catch (PSException e) {
            log.error("Unable to process match using " + SPOT_MSTT_MATCH, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while match process using " + SPOT_MSTT_MATCH, e);
            throw e;
        } finally {
            try {
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                log.error("Unable to properly end match process.", e);
                throw e;
            }
        }
    }

    /**
     * Load fields from given properties file and extract corresponding values
     * from chosen target then write these values in output CSV file
     *
     * @param target
     * @throws Exception Possible exceptions : NullPointerException,
     * ProjectIdListException, AccessException, PSException, IOException
     */
    private void processExtract(String target) throws Exception {
        // Project id list of the target
        List<String> projectIDList;
        // Field list to extract
        List<Field> fieldList;

        // Load field list
        loadFields(CONF_DIRECTORY + File.separator + target + PROPERTIES_FILE_EXTENSION);

        try {
            log.debug("Loading project id column rank from field " + COLUMN_PROJECT_ID + "...");

            if (fieldLoader.getFieldByName(COLUMN_PROJECT_ID) != null && fieldLoader.getFieldByName(COLUMN_PROJECT_ID).getValue() != null) {
                rankProjectID = Integer.parseInt(fieldLoader.getFieldByName(COLUMN_PROJECT_ID).getValue());
                log.debug("Project id column rank = " + rankProjectID);
            } else {
                throw new NullPointerException("Problem occurred with the field " + COLUMN_PROJECT_ID);
            }

            log.debug("Project id column rank has been successfully loaded.");

            log.debug("Loading project id list from " + OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION + "...");
            projectIDList = loadProjectIDList(rankProjectID);
            log.debug("Project id list have been successfully loaded.");
        } catch (NullPointerException e) {
            log.error("Project rank id inconsistency. Be sure to insert " + COLUMN_PROJECT_ID + " field with correct value.", e);
            throw e;
        } catch (ProjectIdListException e) {
            log.error("Unable to load project id list", e);
            throw e;
        }

        // Get loaded field list and remove useless fields (with CONF field format)
        fieldList = removeConfigurationFields(true);
        processSqlProjects(projectIDList, fieldList, target);
    }

    private void processSqlProjects(List<String> projectIDList, List<Field> fieldList, String target) throws Exception, AccessException, PSException, IOException {
        DBProcessor dbProcessor = workingManager.getDBProcessor(CONF_DIRECTORY + File.separator + target + CONNECT + PROPERTIES_FILE_EXTENSION);

        try {
            log.debug("Extracting projects and writing values in " + OUTPUT_DIRECTORY + File.separator + target + CSV_FILE_EXTENSION + "...");

            bufferedWriter = new BufferedWriter(new FileWriter(OUTPUT_DIRECTORY + File.separator + target + CSV_FILE_EXTENSION));

            writeHeader(fieldList);

            Collection<ProjectDTO> projects = dbProcessor.getProjects(fieldList);

            if (projects != null) {
                int rankID = fieldLoader.getFieldByName("ID").getRank();
                for (ProjectDTO project : projects) {
                    String projectId = project.getStringField(rankID);
                    if (projectIDList.contains(projectId)) {
                        log.debug("Extracting project " + projectId + "...");
                        writeField(fieldList, project);
                    }
                }
            } else {
                log.error("Unable to load projects from SQL select.");
                throw new NullPointerException("Unable to load projects from SQL select.");
            }
            log.debug("Extracting projects and writing values have been successfully done.");
        } catch (Exception e) {
            log.error("Unable to extract properly into " + OUTPUT_DIRECTORY + File.separator + target + CSV_FILE_EXTENSION + ".", e);
            throw e;
        } finally {
            try {
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                log.error("Unable to end the extract process properly.", e);
                throw e;
            }
        }
    }

    /**
     * Load fields from properties file input
     *
     * @param propertiesFile properties file with field informations
     * @throws Exception @see {@link FieldLoader}
     */
    private void loadFields(String propertiesFile) throws Exception {
        log.debug("Loading fields from " + propertiesFile + "...");

        fieldLoader = new FieldLoader(propertiesFile);
        fieldLoader.load();

        log.debug("Fields have been successfully loaded.");
    }

    /**
     * For each field in given list, loads the corresponding value depending on
     * its format then writes it in current output files
     *
     * @param fieldList field list
     * @param fieldAccessor Contain field values (can be Project,
     * DataViewRowList...)
     * @param userFieldAttribute data to extract from USER field
     * @throws DataFormatException @see {@link DataFormatException}
     * @throws IOException @see {@link IOException}
     * @throws PSException @see {@link PSException}
     */
    private void writeField(List<Field> fieldList, FieldAccessor fieldAccessor) throws DataFormatException, IOException, PSException {
        for (Field field : fieldList) {
            String separator = (fieldList.indexOf(field) == (fieldList.size() - 1) ? "\n" : SEPARATOR);
            switch (field.getFormat()) {
                case DATE:
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        bufferedWriter.write(simpleDateFormat.format(((FieldAccessor) fieldAccessor).getDateField(field.getName())));
                    } catch (IOException e) {
                        log.error("Unable to write " + field.getName(), e);
                    } catch (DataFormatException e) {
                        log.error("Unable to write " + field.getName(), e);
                    } catch (PSException e) {
                        log.error("Unable to write " + field.getName(), e);
                    }
                    break;
                case TEXT:
                    try {
                        bufferedWriter.write(((FieldAccessor) fieldAccessor).getStringField(field.getName()));
                    } catch (Exception e) {
                        log.error("Unable to write " + field.getName(), e);
                    }
                    break;
                case DECIMAL:
                    try {
                        bufferedWriter.write(String.valueOf(((FieldAccessor) fieldAccessor).getDoubleField(field.getName())));
                    } catch (Exception e) {
                        log.error("Unable to write " + field.getName(), e);
                    }
                    break;
                case INTEGER:
                    try {
                        bufferedWriter.write(String.valueOf(((FieldAccessor) fieldAccessor).getIntField(field.getName())));
                    } catch (Exception e) {
                        log.error("Unable to write " + field.getName(), e);
                    }
                    break;
                case CONF:
                    log.error("Unexpected " + FieldFormat.CONF + " Field Format : " + field.getName() + ".");
                    log.error(field.getName() + " treatment skipped.");
                    break;
                default:
                    log.error("Unexpected Field Format for " + field.getName() + ". Format should be one of those values : " + FieldFormat.TEXT.getAllFormats());
                    log.error(field.getName() + " treatment skipped.");
                    break;
            }
            try {
                bufferedWriter.write(separator);
            } catch (IOException e) {
                log.error("Unable to write separator : " + separator);
            }
        }
    }

    private void writeField(List<Field> fieldList, ProjectDTO project) throws DataFormatException, IOException, PSException {
        for (Field field : fieldList) {
            String separator = (fieldList.indexOf(field) == (fieldList.size() - 1) ? "\n" : SEPARATOR);
            switch (field.getFormat()) {
                case DATE:
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        bufferedWriter.write(simpleDateFormat.format(project.getDateField(field.getRank())));
                    } catch (IOException e) {
                        log.error("Unable to write " + field.getName(), e);
                    } catch (NullPointerException e) {
                        bufferedWriter.write("");
                    }
                    break;
                case TEXT:
                    try {
                        bufferedWriter.write(project.getStringField(field.getRank()));
                    } catch (IOException e) {
                        log.error("Unable to write " + field.getName(), e);
                    } catch (NullPointerException e) {
                        bufferedWriter.write("");
                    }
                    break;
                case DECIMAL:
                    try {
                        bufferedWriter.write(String.valueOf(project.getStringField(field.getRank())));
                    } catch (IOException e) {
                        log.error("Unable to write " + field.getName(), e);
                    } catch (NullPointerException e) {
                        bufferedWriter.write("");
                    }
                    break;
                case INTEGER:
                    try {
                        bufferedWriter.write(String.valueOf(project.getIntField(field.getRank())));
                    } catch (IOException e) {
                        log.error("Unable to write " + field.getName(), e);
                    } catch (NullPointerException e) {
                        bufferedWriter.write("");
                    }
                    break;
                case USER:
                    try {
                        bufferedWriter.write(workingManager.getUserName(project.getStringField(field.getDbName()), field.getName()));
                    } catch (Exception e) {
                        log.error("Unable to write " + field.getName() + " of " + field.getDbName(), e);
                    }
                    break;
                case ORGANISATION:
                    try {
                        bufferedWriter.write(workingManager.getOrganisationValue(project.getStringField(field.getDbName()), field.getName()));
                    } catch (Exception e) {
                        log.error("Unable to write " + field.getName() + " of " + field.getDbName(), e);
                    }
                    break;
                case CONF:
                    log.error("Unexpected " + FieldFormat.CONF + " Field Format : " + field.getName() + ".");
                    log.error(field.getName() + " treatment skipped.");
                    break;
                default:
                    log.error("Unexpected Field Format for " + field.getName() + ". Format should be one of those values : " + FieldFormat.TEXT.getAllFormats());
                    log.error(field.getName() + " treatment skipped.");
                    break;
            }
            try {
                bufferedWriter.write(separator);
            } catch (IOException e) {
                log.error("Unable to write separator : " + separator);
            }
        }
    }

    private void writeHeader(List<Field> fieldList) {
        for (Field field : fieldList) {
            String separator = (fieldList.indexOf(field) == (fieldList.size() - 1) ? "\n" : SEPARATOR);
            try {
                bufferedWriter.write(field.getName());
                bufferedWriter.write(separator);
            } catch (IOException e) {
                log.error("Unable to write header");
            }
        }
    }

    /**
     * Remove from the field Loader all Configuration fields (Fields with CONF
     * value)
     *
     * @param If TRUE, sort list
     * @return Field list without configuration fields
     */
    private List<Field> removeConfigurationFields(boolean sort) {
        boolean removed = false;
        log.debug("Removing " + FieldFormat.CONF + " fields...");

        List<Field> fieldList;
        fieldList = fieldLoader.getFieldList(sort);
        for (Iterator<Field> fieldIterator = fieldList.listIterator(); fieldIterator.hasNext();) {
            Field field = fieldIterator.next();
            if (field.getFormat() != null && field.getFormat().equals(FieldFormat.CONF)) {
                fieldIterator.remove();
                log.debug(field.getName() + " removed.");
                removed = true;
            }
        }

        if (removed) {
            log.debug(FieldFormat.CONF + " fields have been successfully removed.");
        } else {
            log.debug("No " + FieldFormat.CONF + " fields to remove found.");
        }
        return fieldList;
    }

    /**
     * Load project id list from generated CSV (after a match action)
     *
     * @param rankProjectID rank of the project id in the input CSV file
     * @return project id list
     * @throws ProjectIdListException @see {@link ProjectIdListException}
     */
    private List<String> loadProjectIDList(int rankProjectID) throws ProjectIdListException {
        List<String> projectIDList = new ArrayList<String>();

        String line = "";
        BufferedReader bReader = null;

        try {
            bReader = new BufferedReader(new FileReader(OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION));
            bReader.readLine(); // skip first line, it's the header line

            while ((line = bReader.readLine()) != null) {
                String[] fieldProperties = line.split(SEPARATOR);
                try {
                    String projectID = fieldProperties[rankProjectID];
                    if (projectID != null && !"".equals(projectID) && !projectIDList.contains(projectID)) {
                        projectIDList.add(fieldProperties[rankProjectID]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // nothing to do, case of last line empty
                }
            }
        } catch (FileNotFoundException e) {
            log.error(OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION + " not found.", e);
            throw new ProjectIdListException(OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION + " not found.", e);
        } catch (IOException e) {
            log.error("Trouble while opening " + OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION + ".", e);
            throw new ProjectIdListException("Trouble while opening " + OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION + ".", e);
        } finally {
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e) {
                    log.error("Trouble while closing " + OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION + ".", e);
                    throw new ProjectIdListException("Trouble while closing " + OUTPUT_DIRECTORY + File.separator + SPOT_MSTT_MATCH_FILENAME + CSV_FILE_EXTENSION + ".", e);
                }
            }
        }
        return projectIDList;
    }

    /**
     * Print exit error for command line arguments inconsistency
     */
    private static void printExitError() {
        System.out.println("Invalid command line arguments.");
        System.out.println();
        printUsage();
    }

    /**
     * Print usage reminder
     */
    private static void printUsage() {
        System.out.println();
        System.out.println("Usage : ");
        System.out.println("SpotMsttSynchro [action] [target] [configuration folder] [output folder]");
        System.out.println();
        System.out.println("action                    = match or extract");
        System.out.println("target                    = mstt or spot");
        System.out.println("configuration folder      = path to all configuration files");
        System.out.println("output folder (optional)  = path for output folder. By default in out/ directory");
        System.out.println();
        System.out.println("Example for match action : MsttSpotSynchro match mstt C:/schneider/app/conf/");
        System.out.println();
        System.out.println("Example for extract action : MsttSpotSynchro extract mstt C:/schneider/app/conf/");
        System.out.println();
    }
}
