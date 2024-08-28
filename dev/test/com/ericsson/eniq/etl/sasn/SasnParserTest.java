package com.ericsson.eniq.etl.sasn;
import static org.junit.Assert.assertEquals;

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

import org.junit.Test;
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
public class SasnParserTest  {
	 private String techPack;

	  private String setType;

	  private String setName;

	  private int status = 0;

	  private Main mainParserObject = null;

	  private final static String suspectFlag = "";

	  private String workerName = "";
  
  SASNParser sasn = new SASNParser();
  
  @Test
	public void test1() {
	  sasn.init( null,  techPack,  setType,  setName, workerName);
	  assertEquals(0, 0);
	}
  
  @Test
	public void testStatus() {
		int result = sasn.status();
		assertEquals(result, result);
	}
  
  
}