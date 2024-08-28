package com.ericsson.eniq.etl.sasn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.helpers.DefaultHandler;

import com.distocraft.dc5000.etl.parser.Main;
import com.distocraft.dc5000.etl.parser.MeasurementFile;
import com.distocraft.dc5000.etl.parser.Parser;
import com.distocraft.dc5000.etl.parser.SourceFile;

/**
 * 
 * <br>
 * <table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr bgcolor="#CCCCFF" class="TableHeasingColor">
 * <td colspan="4"><font size="+2"><b>Parameter Summary</b></font></td>
 * </tr>
 * <tr>
 * <td><b>Name</b></td>
 * <td><b>Key</b></td>
 * <td><b>Description</b></td>
 * <td><b>Default</b></td>
 * </tr>
 * <tr>
 * <td>Column delimiter</td>
 * <td>column_delimiter</td>
 * <td> Character (String) that separates different columns in sourcefile. Yet
 * empty "columns" are to be skipped.</td>
 * <td>Whitespace (\\s)</td>
 * </tr>
 * <tr>
 * <td>TagID mode</td>
 * <td>tag_id_mode</td>
 * <td>Defines the discovery method of mesurement identification (TAGID).<br>
 * 0 = TAGID is is predefined in parameter named tag_id<br>
 * 1 = TAGID is parsed from name pf sourcefile using regexp pattern defined in
 * parameter named tag_id.</td>
 * <td>1 (from name of sourcefile)</td>
 * </tr>
 * <tr>
 * <td>TagID / TagID filename pattern</td>
 * <td>tag_id</td>
 * <td>Defines predefined TAGID for measurement type or defines regexp pattern
 * that is used to parse TAGID from the name of sourcefile.</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>Header rows</td>
 * <td>parser.header.<i>headername</i></td>
 * <td>Defines column names (comma separated) for given measurement types. An
 * example for server measurement type: <br>
 * Key: parser.header.process <br>
 * Value: DATE,TIME,PID,PROCNAME,%CPU,%MEM,TIME,VIRTUAL,RSS,PARTITION <br>
 * In addition the parser renames column names which are not unique. Renaming
 * convention is as follows TIME,TIME[1],TIME[2],...,TIME[n]</td>
 * <td>&nbsp;</td>
 * </tr>
 * </table> <br>
 * <br>
 * <table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr bgcolor="#CCCCFF" class="TableHeasingColor">
 * <td colspan="2"><font size="+2"><b>Added DataColumns</b></font></td>
 * </tr>
 * <tr>
 * <td><b>Column name</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td>filename</td>
 * <td>Contains the filename of the inputdatafile.</td>
 * </tr>
 * <tr>
 * <td>DATETIME_ID</td>
 * <td>Contains the data from DATE column.</td>
 * </tr>
 * <tr>
 * <td>DC_SUSPECTFLAG</td>
 * <td>EMPTY</td>
 * </tr>
 * <tr>
 * <td>DIRNAME</td>
 * <td>Contains full path to the inputdatafile.</td>
 * </tr>
 * <tr>
 * <td>JVM_TIMEZONE</td>
 * <td>Contains the JVM timezone (example. +0200) </td>
 * </tr>
 * <tr>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * </table> <br>
 * <br>
 * 
 * @author eharrka <br>
 *         <br>
 * 
 */
public final class SASNParser extends DefaultHandler implements Parser {
  
  private static final String JVM_TIMEZONE = new SimpleDateFormat("Z").format(new Date());

  private String[] header;

  private String columnDelimiter;

  private String dateColumn;

  private String headerLineStartsWith;

  private Logger log;

  private SourceFile sf;

  private String filename;

  private String tagIDMode;

  private String tagID;

  private Pattern tagPattern;

  private int bufferSize;

  // ***************** Worker stuff ****************************

  private String techPack;

  private String setType;

  private String setName;

  private int status = 0;

  private Main mainParserObject = null;

  private final static String suspectFlag = "";

  private String workerName = "";
  
  private long parseStartTime;
  private long fileSize = 0L;
  private long totalParseTime = 0L;
  private int fileCount = 0;

  public void init(final Main main, final String techPack, final String setType, final String setName,
      final String workerName) {
    this.mainParserObject = main;
    this.techPack = techPack;
    this.setType = setType;
    this.setName = setName;
    this.status = 1;
    this.workerName = workerName;

    String logWorkerName = "";
    if (workerName.length() > 0) {
      logWorkerName = "." + workerName;
    }

    log = Logger.getLogger("etl." + techPack + "." + setType + "." + setName + ".parser.SASN" + logWorkerName);

  }

  public int status() {
    return status;
  }

  public void run() {

    try {

      this.status = 2;
      SourceFile sf = null;
	  parseStartTime = System.currentTimeMillis();

      while ((sf = mainParserObject.nextSourceFile()) != null) {
        try {
		  fileCount++;
		  fileSize += sf.fileSize();					
          mainParserObject.preParse(sf);
          parse(sf, techPack, setType, setName);
          mainParserObject.postParse(sf);
        } catch (Exception e) {
          mainParserObject.errorParse(e, sf);
        } finally {
          mainParserObject.finallyParse(sf);
        }
      }
	  totalParseTime = System.currentTimeMillis() - parseStartTime;
	  if (totalParseTime != 0) {
			log.info("Parsing Performance :: " + fileCount
						+ " files parsed in " + totalParseTime 
						+ " ms, filesize is " + fileSize 
						+ " bytes and throughput : " + (fileSize / totalParseTime)
						+ " bytes/ms.");
			}
    } catch (Exception e) {
      // Exception catched at top level. No good.
      log.log(Level.WARNING, "Worker parser failed to exception", e);
    } finally {
      this.status = 3;
    }
  }

  public void parse(final SourceFile sf, final String techPack, final String setType, final String setName)
      throws Exception {

    this.sf = sf;
    this.filename = sf.getName();
    MeasurementFile mFile = null;

    try {
      final String strBufferSize = sf.getProperty("buffer_size", "-1");
      log.finest("buffer_size: " + bufferSize);
      if (strBufferSize.length() == 0) {
        bufferSize = -1;
      } else {
        bufferSize = Integer.parseInt(strBufferSize);
      }

      dateColumn = sf.getProperty("date_column", "DATE");
      log.finest("date_column: " + dateColumn);

      headerLineStartsWith = sf.getProperty("header_line_starts_with", "DATE");
      log.finest("header_line_starts_with: " + headerLineStartsWith);

      columnDelimiter = sf.getProperty("column_delimiter", "\\s");
      if (columnDelimiter.length() == 0) {
        columnDelimiter = "\\s";
      }
      log.finest("col_delim: " + columnDelimiter);

      tagIDMode = sf.getProperty("tag_id_mode", "1");
      log.finest("tag_id_mode: " + tagIDMode);

      tagID = sf.getProperty("tag_id", ".+-(.+)-.+");
      log.finest("tag_id: " + tagID);

      try {
        // read pattern from filename
        if (tagIDMode.equalsIgnoreCase("1")) {
          final String patt = tagID;
          tagPattern = Pattern.compile(patt);
          final Matcher m = tagPattern.matcher(filename);
          if (m.find()) {
            tagID = m.group(1);
          }
        }
      } catch (Exception e) {
        log.log(Level.WARNING, "Error while matching pattern " + tagID + " from filename " + filename + " for tag_id",
            e);
      }

      final String headerRows = sf.getProperty("parser.header." + tagID, "").trim();
      log.fine("header_row from config: " + headerRows);

      mFile = Main.createMeasurementFile(sf, tagID, techPack, setType, setName, this.workerName, log);

      log.fine("Parsing File: " + sf.getName());

      if ("".equals(headerRows)) {
        // read header from file
        header = readHeader();
      } else {
        // read header from configuration
        header = headerRows.split(",");
      }

      if (header == null || header.length == 0) {
        throw new Exception("Error. No header found.");
      } else {
        header = unifyHeaderNames(header);
      }

      parseData(mFile);

    } catch (Exception e) {

      log.log(Level.WARNING, "General Failure", e);

    } finally {
      if (mFile != null) {
        try {
          mFile.close();
        } catch (Exception e) {
          log.log(Level.WARNING, "Error closing MeasurementFile", e);
        }
      }
    }
  }

  public static void main(final String[] args) {

  }

  private String[] readHeader() {
    String[] retHeader = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    String line = null;
    boolean headerFound = false;
    final List headerList = new ArrayList();

    try {

      inputStreamReader = new InputStreamReader(sf.getFileInputStream());
      if (bufferSize == -1) {
        bufferedReader = new BufferedReader(inputStreamReader);
      } else {
        bufferedReader = new BufferedReader(inputStreamReader, bufferSize);
      }

      final Pattern p = Pattern.compile(columnDelimiter);
      String[] columns;

      line = bufferedReader.readLine();
      while (line != null && !headerFound) {
        if (isHeaderLine(line)) {
          columns = p.split(line);
          for (int i = 0; i < columns.length; i++) {
            if (columns[i].length() > 0) {
              headerList.add(columns[i]);
            }
          }
          headerFound = true;
        }
        line = bufferedReader.readLine();
      }

      retHeader = (String[]) headerList.toArray(new String[headerList.size()]);

    } catch (Exception e) {
      log.log(Level.WARNING, "Error occurred in header reading.\n", e);
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (Exception e) {
          log.log(Level.WARNING, "Error closing Reader (BufferedReader)", e);
        }
      }

      if (inputStreamReader != null) {
        try {
          inputStreamReader.close();
        } catch (Exception e) {
          log.log(Level.WARNING, "Error closing Reader (InputStreamReader)", e);
        }
      }
    }
    return retHeader;
  }

  private String[] unifyHeaderNames(final String[] header) {
    final String[] unifiedHeader = new String[header.length];

    for (int i = 0; i < header.length; i++) {
      final String baseName = header[i].trim();
      if (!isUnifiedAlready(unifiedHeader, baseName)) {
        unifiedHeader[i] = baseName.trim();
        int serialNo = 1;
        for (int y = i + 1; y < header.length; y++) {
          final String checkName = header[y].trim();
          if (checkName.equalsIgnoreCase(baseName)) {
            unifiedHeader[y] = checkName + "[" + serialNo + "]";
            serialNo++;
          }
        }
      }
    }
    return unifiedHeader;
  }

  private boolean isUnifiedAlready(final String[] unifiedHeaders, final String headerName) {
    boolean returnValue = false;
    for (int i = 0; i < unifiedHeaders.length && !returnValue; i++) {
      if (unifiedHeaders[i] != null) {
        returnValue = (unifiedHeaders[i].equals(headerName));
      }
    }
    return returnValue;
  }

  private void parseData(final MeasurementFile measurementFile) {
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    String line = null;
    final String filename = sf.getName();
    final String directory = sf.getDir();

    try {
      inputStreamReader = new InputStreamReader(sf.getFileInputStream());
      if (bufferSize == -1) {
        bufferedReader = new BufferedReader(inputStreamReader);
      } else {
        bufferedReader = new BufferedReader(inputStreamReader, bufferSize);
      }

      final Pattern p = Pattern.compile(columnDelimiter);
      String[] columns;

      line = bufferedReader.readLine();
      while (line != null) {
        // check that read line is not header line 
        if (!isHeaderLine(line)) {
          columns = p.split(line);
          
          //check that line has some data
          if (hasData(columns)) {
            int headerIndex = 0;
            for (int i = 0; i < columns.length && headerIndex < header.length; i++) {
              if (columns[i].length() > 0) {
                if (header[headerIndex].equalsIgnoreCase(dateColumn)) {
                  measurementFile.addData("DATETIME_ID", columns[i]);
                }
  
                measurementFile.addData(header[headerIndex], columns[i]);
                headerIndex++;
              }
            }
  
            measurementFile.addData("filename", filename);
            measurementFile.addData("DC_SUSPECTFLAG", suspectFlag);
            measurementFile.addData("DIRNAME", directory);
            measurementFile.addData("JVM_TIMEZONE", JVM_TIMEZONE);
  
            measurementFile.saveData();
          }
        }
        line = bufferedReader.readLine();
      }

    } catch (Exception e) {
      log.log(Level.WARNING, "Error in data parsing.\n" + e.toString());
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (Exception e) {
          log.log(Level.WARNING, "Error closing Reader (BufferedReader)", e);
        }
      }

      if (inputStreamReader != null) {
        try {
          inputStreamReader.close();
        } catch (Exception e) {
          log.log(Level.WARNING, "Error closing Reader (InputStreamReader)", e);
        }
      }
    }
  }

  private boolean isHeaderLine(final String line) {
    return line.startsWith(headerLineStartsWith);
  }
  
  private boolean hasData(final String[] columns) {
    boolean returnValue = false;
    if(columns.length > 0){
      if(columns[0].length() > 0){
        returnValue = true;
      }
    }
    return returnValue;
  }
}
