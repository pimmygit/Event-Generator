/******************************************************************
*
* IBM Tivoli Event Generator
*
* IBM Confidential
* OCO Source Materials
*
* 5724-S45
*
* (C) Copyright IBM Corp. 2005, 2009
*
* The source code for this program is not published or otherwise
* divested of its trade secrets, irrespective of what has
* been deposited with the U.S. Copyright Office.
*
******************************************************************/
package gui;

import log.LogManager;
import log.LogHandle;

import utils.GENTableModel;

import javax.swing.*;
import javax.swing.table.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.Properties;

import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: Pimmy
 * Date: Feb 11, 2004
 * Time: 10:21:56 AM
 * To change this template use Options | File Templates.
 */
public class EventTable
{
    final int COL_PROPS_LENGTH = 6;
    final int COL_PROPS_INDEX = COL_PROPS_LENGTH - 1;

    final int DEF_COL_MIN = 100;
    final int DEF_COL_PRE = 300;
    final int DEF_COL_MAX = 500;

    private String _genhome;

    final String TEMPLATE_TABLE = "/props/template.xml";

    String _eventTableName;
    String _tableFile;
    String _dbTable;

    public GENTableModel eventTableModel;
    public JTable EventsTable;

    Object[][] Columns;
    Object[][] Data;

    Properties _columnType;
    
    private int min_w;
    private int pre_w;
    private int max_w;

    public EventTable()
    {
        // Get the property (EGEN_HOME env var must be set)
        _genhome = System.getProperty("EGEN_HOME");
        // Several checks to determine that the Program starts from the correct location
        File home = new File(_genhome);
        if (_genhome == null)
        {
            System.out.println("Event Table Builder  - Home Directory not set. Exit.\n\n");
            System.exit(-1);
        }
        if (!home.exists())
        {
            System.out.println("Event Table Builder  - Home Directory ["+_genhome+"] does not exist. Exit.\n\n");
            System.exit(-1);
        }
    }

    public JTable createEventTable(String tableName)
    {
        LogManager.write("Event Table Builder  - INFO:     Creating Event Table [" +tableName+ "].",LogHandle.ALL,LogManager.INFO);

        // Determine the XML File to load as a table
        if (tableName == null || tableName.trim() == "" || tableName.toLowerCase().trim().matches("template"))
        {
            _tableFile = _genhome.concat(TEMPLATE_TABLE);
        }
        else
        {
            if (!tableName.startsWith("props"))
            {
                _tableFile = _genhome.concat("/props/").concat(tableName).concat(".xml");
            }
            File tabFile = new File(_tableFile);
            if (!tabFile.isFile())
            {
                LogManager.write("Event Table Builder  - MAJOR:    File [" +_tableFile+ "] does not exist. Using the template instead.",LogHandle.ALL,LogManager.MAJOR);
                _tableFile = _genhome.concat(TEMPLATE_TABLE);
            }
        }

        LogManager.write("Event Table Builder  - DEBUG:    Creating \"Event Table\" from file [" +_tableFile+ "].", LogHandle.ALL,LogManager.DEBUG);

        //setTableName(_tableFile.substring(_tableFile.lastIndexOf("/")+1, _tableFile.lastIndexOf(".")));
        setTableName(getTableAttribute("name"));
        setDbTable(getDbTableDefinition());
        
        // Get all column properties
        Columns = getColumns();

        // Build the table data
        Data = createDataMatrix();

        // Get the names of the Columns to build the table model
        Object[] colNames = new Object[Columns.length];
        for (int i=0; i<Columns.length; i++)
        {
            colNames[i] = Columns[i][0];
            //LogManager.write("Event Table Builder  - Column No. [" +i+ "] has name [" +Columns[i][0]+ "].",LogHandle.ALL,LogManager.DEBUG);
        }
        eventTableModel = new GENTableModel(Data, colNames);

        EventsTable = new JTable(eventTableModel);
        EventsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //EventsTable.setDragEnabled(false);
        //EventsTable.setRowSelectionAllowed(true);
        //EventsTable.setColumnSelectionAllowed(true);

        setColumnSize();

        return EventsTable;
    }
    
    private void setTableAttribute(String attName, String attValue) throws Exception {

    	Document	document;
        File		eventFile = new File(_tableFile);
        
        LogManager.write("Event Table Builder  - DEBUG:    Parsing [" +eventFile.getAbsolutePath()+ "] to set the value of attribute [" +attName+ "] to [" +attValue+ "].",LogHandle.ALL,LogManager.DEBUG);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Configuring the Error Handling
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                // ignore fatal errors (an exception is guaranteed)
                public void fatalError(SAXParseException exception) throws SAXException {}

                // treat validation errors as fatal
                public void error(SAXParseException e) throws SAXParseException {
                    throw e;
                }

                // dump warnings too
                public void warning(SAXParseException err) throws SAXParseException {
                    throw err;
                }
            });

            // Fetching the Event File
            document = builder.parse( eventFile );

            // Getting content of the table(s)
            Element mainNode = document.getDocumentElement();
            
            mainNode.setAttribute(attName, attValue);
            
            // Prepare the DOM document for writing
            Source source = new DOMSource(document);
            // Prepare the output
            Result result = new StreamResult(eventFile);
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            
        } catch (TransformerConfigurationException tce) {
            LogManager.write("Event Table Builder  - MAJOR:    Failed to set the value of attribute [" +attName+ "] to [" +attValue+ "] due to Transformer Configuration Exception [" +tce+ "].",LogHandle.ALL,LogManager.MAJOR);
            throw new Exception(tce);
        } catch (TransformerException te) {
            LogManager.write("Event Table Builder  - MAJOR:    Failed to set the value of attribute [" +attName+ "] to [" +attValue+ "] due to Transformer Exception [" +te+ "].",LogHandle.ALL,LogManager.MAJOR);
            throw new Exception(te);
        } catch (Exception e) {
            LogManager.write("Event Table Builder  - MAJOR:    XML Parse Error for [" +eventFile.getName()+ "]: Failed to set the value of attribute [" +attName+ "] to [" +attValue+ "].",LogHandle.ALL,LogManager.MAJOR);
            throw new Exception(e);
        }
    }
    
    private String getTableAttribute(String attName) {
    	
    	String		attValue = "";
    	Document	document;
        File		eventFile = new File(_tableFile);
        
        LogManager.write("Event Table Builder  - DEBUG:    Parsing [" +eventFile.getAbsolutePath()+ "] to retrieve value of attribute [" +attName+ "].",LogHandle.ALL,LogManager.DEBUG);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Configuring the Error Handling
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                // ignore fatal errors (an exception is guaranteed)
                public void fatalError(SAXParseException exception) throws SAXException {}

                // treat validation errors as fatal
                public void error(SAXParseException e) throws SAXParseException {
                    throw e;
                }

                // dump warnings too
                public void warning(SAXParseException err) throws SAXParseException {
                    throw err;
                }
            });

            // Fetching the Event File
            document = builder.parse( eventFile );

            // Getting content of the table(s)
            Element mainNode = document.getDocumentElement();
            
            attValue = mainNode.getAttribute(attName);
            
            if (attValue.isEmpty() || attValue.trim() == "") {
            	LogManager.write("Event Table Builder  - MINOR:    [" +attName+ "] has no value.",LogHandle.ALL,LogManager.MINOR);
            	return "";
            } else {
            	LogManager.write("Event Table Builder  - DEBUG:    [" +attName+ "] -> [" +attValue+ "].",LogHandle.ALL,LogManager.DEBUG);
            	return attValue;
            }
        } catch (Exception e) {
            LogManager.write("Event Table Builder  - MINOR:    XML Parse Error for [" +eventFile.getName()+ "]: Failed to get value of attribute [" +attName+ "].",LogHandle.ALL,LogManager.MINOR);
            return "";
        }
    }
    
    private String getDbTableDefinition() {
    	
    	String dbTable = getTableAttribute("dbTarget");
    	
    	if (dbTable.isEmpty() || dbTable.trim() == "") {
        	// By default we return alerts.status
            LogManager.write("Event Table Builder  - MINOR:    [database.table] set to defauld value of: [alerts.status].",LogHandle.ALL,LogManager.MINOR);
            return "alerts.status";
    	} else {
        	// By default we return alerts.status
            LogManager.write("Event Table Builder  - DEBUG:    [database.table] set to: [" +dbTable+ "].",LogHandle.ALL,LogManager.DEBUG);
            return dbTable;
    	}
    }
    
    private Object[][] getColumns()
    {
        Object[][] colProps = null;
        Document document;

        // Init the column type property
        _columnType = new Properties();

        File eventFile = new File(_tableFile);
        LogManager.write("Event Table Builder  - DEBUG:    Parsing [" +eventFile.getAbsolutePath()+ "] to create the Columns.",LogHandle.ALL,LogManager.DEBUG);

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Configuring the Error Handling
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                // ignore fatal errors (an exception is guaranteed)
                public void fatalError(SAXParseException exception)
                        throws SAXException {}

                // treat validation errors as fatal
                public void error(SAXParseException e)
                        throws SAXParseException
                {
                    throw e;
                }

                // dump warnings too
                public void warning(SAXParseException err)
                        throws SAXParseException
                {
                    throw err;
                }
            });

            // Fetching the Event File
            document = builder.parse( eventFile );

            // Getting content of the table(s)
            NodeList tableNode = document.getElementsByTagName("table");
            for (int i=0; i<tableNode.getLength(); i++)
            {
                // Getting content of the Columns
                NodeList columnNode = document.getElementsByTagName("column");

                // Building column attribute table, which holds the properties for each column
                // Current properties are:  - name
                //                          - position
                //                          - minWidth
                //                          - prefWidth
                //                          - maxWidth
                //                          - type

                colProps = new Object[columnNode.getLength()][COL_PROPS_LENGTH];
                for (int j=0; j<columnNode.getLength(); j++)
                {
                    Element col = (Element)columnNode.item(j);
                    colProps[j][0] = col.getAttribute("name");
                    colProps[j][1] = col.getAttribute("position");
                    colProps[j][2] = col.getAttribute("minWidth");
                    colProps[j][3] = col.getAttribute("prefWidth");
                    colProps[j][4] = col.getAttribute("maxWidth");
                    colProps[j][5] = col.getAttribute("type");
                    
                    // By this in future we will determine if a column with this name already exists
                    // or what type is the value in this column.
                    _columnType.setProperty(col.getAttribute("name"), col.getAttribute("type"));
                }
            }
        }
        catch (SAXParseException spe)
        {LogManager.write("Event Table Builder  - MAJOR:    XML Parse Error for [" +eventFile.getName()+ "] at line number [" +spe.getLineNumber()+ "].",LogHandle.ALL,LogManager.MAJOR);}
        catch (SAXException se)
        {LogManager.write("Event Table Builder  - MAJOR:    Cannot build the event _document for [" +eventFile.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);}
        catch (IOException ioe)
        {LogManager.write("Event Table Builder  - MAJOR:    Cannot parse the event file [" +eventFile.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);}
        catch (ParserConfigurationException pce)
        {LogManager.write("Event Table Builder  - MAJOR:    Cannot configure _document parser for [" +eventFile.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);}
        catch (Exception exptn)
        {LogManager.write("Event Table Builder  - MAJOR:    Cannot configure columns table for [" +eventFile.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);}

        return colProps;
    }

    private Object[][] createDataMatrix()
    {
        Object temp = null;
        Object[][] Data = null;
        Document document;

        File eventFile = new File(_tableFile);
        LogManager.write("Event Table Builder  - DEBUG:    Parsing [" +eventFile.getAbsolutePath()+ "] to build the table.",LogHandle.ALL,LogManager.DEBUG);

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Configuring the Error Handling
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                // ignore fatal errors (an exception is guaranteed)
                public void fatalError(SAXParseException exception)
                        throws SAXException {}

                // treat validation errors as fatal
                public void error(SAXParseException e)
                        throws SAXParseException
                {
                    throw e;
                }

                // dump warnings too
                public void warning(SAXParseException err)
                        throws SAXParseException
                {
                    throw err;
                }
            });

            // Fetching the Event File
            document = builder.parse( eventFile );

            // Getting content of the table(s)
            NodeList tableNode = document.getElementsByTagName("table");
            for (int i=0; i<tableNode.getLength(); i++)
            {
                NodeList columnNode = document.getElementsByTagName("column");
                NodeList cellNode = document.getElementsByTagName("cell");

                int cls = columnNode.getLength();
                int rws = cellNode.getLength() / cls;


                Data = new Object[rws][cls];
                //LogManager.write("Event Table Builder  - Cells [" +cellNode.getLength()+ "], Rows [" +rws+ "], Columns ["+cls+"].",LogHandle.ALL,LogManager.DEBUG);
                // In order to use them as indexes - decrement by one
                cls--;
                for (int k=cellNode.getLength(); k>0; k--)
                {
                    Element columnElement = (Element)columnNode.item(cls);
                    String columnName = columnElement.getAttribute("name");
                    int type = -1;

                    try {
                        type = Integer.valueOf(_columnType.getProperty(columnName)).intValue();
                        if ( type < 0 || type > 1) {
                            LogManager.write("Event Table Builder  - MAJOR:    Invalid type [" +type+ "] for column [" +columnName+ "]. Valid values are 0(int) and 1(char). Please upgade the event table.",LogHandle.ALL,LogManager.MAJOR);
                            type = 1;
                        } else {
                        	String typeStr = (type != 0) ? "Char" : "Int";
                            LogManager.write("Event Table Builder  - DEBUG:        - Column [" +columnName+ "] is of type [" + typeStr + "].",LogHandle.ALL,LogManager.DEBUG);	
                        }
                    } catch (Exception e) {
                        LogManager.write("Event Table Builder  - MAJOR:    Cannot determine the type of column [" +columnName+ "].",LogHandle.ALL,LogManager.MAJOR);
                        type = 1;
                    }
                    
                    // Getting content of the table
                    rws--;
                    Element cell = (Element)cellNode.item(k-1);
                    temp = cell.getAttribute("value");
                    // Enumerating each event with ID number
                    if (cls == 0)
                    {
                        Data[rws][cls] = new Integer(rws);
                    } else {
                    	// If the value type is Integer
                    	if ( type == 0 ) {
                    		try {
                    			if ( Integer.valueOf(temp.toString()).intValue() < 0 ) {
                                    LogManager.write("Event Table Builder  - MINOR:    Cell value [" +temp.toString()+ "] has negative value. Setting value to [0].",LogHandle.ALL,LogManager.MINOR);
                                    Data[rws][cls] = new Integer(0);
                    			} else {
                                    // The value is numerical so we put number
                                    LogManager.write("Event Table Builder  - DEBUG:        - Data[" +rws+ "][" +cls+ "] - Name:[" +columnName+ "] - Value:[" +temp+ "] - Type:[INTEGER].",LogHandle.ALL,LogManager.DEBUG);
                                    Data[rws][cls] = new Integer(temp.toString());
                    			}
                    		} catch (NumberFormatException nfe) {
                    			Data[rws][cls] = temp.toString();
                    		}
                    	// Otherwise its a Char
                    	} else {
                            // Defining the type of the cell content
                            if (temp.toString().toLowerCase().trim().matches("false"))
                            {
                                // The value is boolean, so we put a tickbox in the cell.
                                LogManager.write("Event Table Builder  - DEBUG:        - Data[" +rws+ "][" +cls+ "] - Name:[" +columnName+ "] - Value:[" +temp+ "] - Type:[FALSE].",LogHandle.ALL,LogManager.DEBUG);
                                Data[rws][cls] = new Boolean("false");
                            }
                            else if (temp.toString().toLowerCase().trim().matches("true"))
                            {
                                // The value is boolean, so we put a tickbox in the cell.
                                LogManager.write("Event Table Builder  - DEBUG:        - Data[" +rws+ "][" +cls+ "] - Name:[" +columnName+ "] - Value:[" +temp+ "] - Type:[TRUE].",LogHandle.ALL,LogManager.DEBUG);
                                Data[rws][cls] = new Boolean("true");
                            }
                            else
                            {
                                LogManager.write("Event Table Builder  - DEBUG:        - Data[" +rws+ "][" +cls+ "] - Name:[" +columnName+ "] - Value:[" +temp+ "] - Type:[VARCHAR].",LogHandle.ALL,LogManager.DEBUG);
                                Data[rws][cls] = temp.toString();
                            }
                    	}
                    }

                    if (rws == 0)
                    {
                        rws = cellNode.getLength() / columnNode.getLength();
                        cls--;
                    }
                }
            }
        }
        catch (SAXParseException spe)
        {LogManager.write("Event Table Builder  - MAJOR:    XML Parse Error for [" +eventFile.getName()+ "] at line number [" +spe.getLineNumber()+ "].",LogHandle.ALL,LogManager.MAJOR);}
        catch (SAXException se)
        {LogManager.write("Event Table Builder  - MAJOR:    Cannot build the event _document for [" +eventFile.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);}
        catch (IOException ioe)
        {LogManager.write("Event Table Builder  - MAJOR:    Cannot parse the event file [" +eventFile.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);}
        catch (ParserConfigurationException pce)
        {LogManager.write("Event Table Builder  - MAJOR:    Cannot configure _document parser for [" +eventFile.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);}
        return Data;
    }

    public boolean appendColumn(String name, String defVal, int type)
    {
        LogManager.write("Event Table Builder  - INFO:     Adding new column [" +name+ "] of type [" +type+ "] with value [" +defVal+ "].",LogHandle.ALL,LogManager.INFO);

        Object[][] tempArray;

        // Determine if a column with this name already exist
        if (_columnType.getProperty(name) != null)
        {
            LogManager.write("Event Table Builder  - MINOR:    Column [" +name+ "] already exist.",LogHandle.ALL,LogManager.MINOR);
            return false;
        }
        else
        {
            _columnType.setProperty(name, String.valueOf(type));
        }

        // Get current settings of the column width
        int[][] columnWidth = new int[Columns.length][4];

        for (int i=0; i < Columns.length; i++)
        {
            columnWidth[i][0] = EventsTable.getColumnModel().getColumn(i).getMinWidth();
            columnWidth[i][1] = EventsTable.getColumnModel().getColumn(i).getPreferredWidth();
            columnWidth[i][2] = EventsTable.getColumnModel().getColumn(i).getMaxWidth();
            columnWidth[i][3] = EventsTable.getColumnModel().getColumn(i).getWidth();

//            LogManager.write("Event Table Builder  - Column [" +i+ "] - " +
//                    "minWidth [" +EventsTable.getColumnModel().getColumn(i).getMinWidth()+ "], " +
//                    "preWidth [" +EventsTable.getColumnModel().getColumn(i).getPreferredWidth()+ "], " +
//                    "maxWidth [" +EventsTable.getColumnModel().getColumn(i).getMaxWidth()+ "], " +
//                    "Width [" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "].",LogHandle.ALL,LogManager.DEBUG);
        }

        // Create a matrix with one additional element for the columns
        tempArray = new Object[Columns.length+1][COL_PROPS_LENGTH];
        for (int i=0; i<Columns.length; i++)
        {
            for (int j=0; j<COL_PROPS_INDEX; j++)
            {
                tempArray[i][j] = Columns[i][j];
            }
        }

        // Set the values of the additional element
        tempArray[Columns.length][0] = name;
        tempArray[Columns.length][1] = new Integer(Columns.length);
        tempArray[Columns.length][2] = new Integer(30);
        tempArray[Columns.length][3] = new Integer(50);
        tempArray[Columns.length][4] = new Integer(100);
        tempArray[Columns.length][5] = new Integer(type);

        // Copy the temp matrix to the main one, so the main has the new element added
        Columns = new Object[tempArray.length][tempArray[0].length];
        Columns = tempArray;

        //printMatrix(Columns);

        // Create a matrix with one additional column for the data
        tempArray = new Object[Data.length][Columns.length];
        for (int i=0; i<tempArray.length; i++)
        {
            for (int j=0; j<tempArray[0].length; j++)
            {
                if (j == (tempArray[0].length - 1))
                {
                    tempArray[i][j] = defVal;
                }
                else
                {
                    tempArray[i][j] = Data[i][j];
                }
            }
        }

        // Copy the temp matrix to the main one, so the main has the new column added
        Data = new Object[tempArray.length][tempArray[0].length];
        Data = tempArray;

        //printMatrix(Data);

        // Get the names of the Columns from the Column Property matrix
        Object[] colNames = new Object[Columns.length];
        for (int i=0; i<Columns.length; i++)
        {
            colNames[i] = Columns[i][0];
        }

        eventTableModel.updateTable(Data, colNames);

        // Restore the users settings column sizes
        for (int i=0; i<Columns.length-1; i++)
        {
            EventsTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i][0]);
            EventsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i][1]);
            EventsTable.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i][2]);
            EventsTable.getColumnModel().getColumn(i).setWidth(columnWidth[i][3]);
        }
        // No need to define the size for the new column.

        return true;
    }

    public boolean appendRow(Object[] event)
    {
        LogManager.write("Event Table Builder  - INFO:     Adding new row to the Event Table.",LogHandle.ALL,LogManager.INFO);

        Object[][] tempArray;

        // Get current settings of the column width
        int[][] columnWidth = new int[Columns.length][4];

        for (int i=0; i < Columns.length; i++)
        {
            columnWidth[i][0] = EventsTable.getColumnModel().getColumn(i).getMinWidth();
            columnWidth[i][1] = EventsTable.getColumnModel().getColumn(i).getPreferredWidth();
            columnWidth[i][2] = EventsTable.getColumnModel().getColumn(i).getMaxWidth();
            columnWidth[i][3] = EventsTable.getColumnModel().getColumn(i).getWidth();

//            LogManager.write("Event Table Builder  - Column [" +i+ "] - " +
//                    "minWidth [" +EventsTable.getColumnModel().getColumn(i).getMinWidth()+ "], " +
//                    "preWidth [" +EventsTable.getColumnModel().getColumn(i).getPreferredWidth()+ "], " +
//                    "maxWidth [" +EventsTable.getColumnModel().getColumn(i).getMaxWidth()+ "], " +
//                    "Width [" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "].",LogHandle.ALL,LogManager.DEBUG);
        }

        // Create a matrix with one additional element for the new event
        tempArray = new Object[Data.length+1][Data[0].length];
        for (int i=0; i<tempArray.length; i++)
        {
            for (int j=0; j<tempArray[0].length; j++)
            {
                if (i == (tempArray.length - 1))
                {
                    tempArray[i][j] = event[j];
                }
                else
                {
                    tempArray[i][j] = Data[i][j];
                }
            }
        }

        // Copy the temp matrix to the main one, so the main has the new column added
        Data = new Object[tempArray.length][tempArray[0].length];
        Data = tempArray;

        // Get the names of the Columns from the Column Property matrix
        Object[] colNames = new Object[Columns.length];
        for (int i=0; i<Columns.length; i++)
        {
            colNames[i] = Columns[i][0];
        }

        // Rebuild the table
        eventTableModel.updateTable(Data, colNames);

        // Restore the users settings column sizes
        for (int i=0; i<Columns.length; i++)
        {
            EventsTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i][0]);
            EventsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i][1]);
            EventsTable.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i][2]);
            EventsTable.getColumnModel().getColumn(i).setWidth(columnWidth[i][3]);
        }

        return true;
    }

    public boolean copyRow()
    {
        int rowIndex;
        rowIndex = EventsTable.getSelectedRow();

        LogManager.write("Event Table Builder  - INFO:     Duplicating row [" +rowIndex+ "].",LogHandle.ALL,LogManager.INFO);
        
        if (rowIndex >= 0)
        {
            Object[][] tempArray;

            // Get current settings of the column width
            int[][] columnWidth = new int[Columns.length][4];

            for (int i=0; i < Columns.length; i++)
            {
                columnWidth[i][0] = EventsTable.getColumnModel().getColumn(i).getMinWidth();
                columnWidth[i][1] = EventsTable.getColumnModel().getColumn(i).getPreferredWidth();
                columnWidth[i][2] = EventsTable.getColumnModel().getColumn(i).getMaxWidth();
                columnWidth[i][3] = EventsTable.getColumnModel().getColumn(i).getWidth();

//            LogManager.write("Event Table Builder  - Column [" +i+ "] - " +
//                    "minWidth [" +EventsTable.getColumnModel().getColumn(i).getMinWidth()+ "], " +
//                    "preWidth [" +EventsTable.getColumnModel().getColumn(i).getPreferredWidth()+ "], " +
//                    "maxWidth [" +EventsTable.getColumnModel().getColumn(i).getMaxWidth()+ "], " +
//                    "Width [" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "].",LogHandle.ALL,LogManager.DEBUG);
            }

            // Create a matrix with one additional element for the new event
            tempArray = new Object[Data.length+1][Data[0].length];
            for (int i=0; i<tempArray.length; i++)
            {
                for (int j=0; j<tempArray[0].length; j++)
                {
                    if (i == (tempArray.length - 1))
                    {
                        if (j == 0)
                        {
                            // Increment the Event ID
                            tempArray[i][j] = new Integer(i);
                        }
                        else
                        {
                            tempArray[i][j] = Data[rowIndex][j];
                        }
                    }
                    else
                    {
                        tempArray[i][j] = Data[i][j];
                    }
                }
            }

            // Copy the temp matrix to the main one, so the main has the new column added
            Data = new Object[tempArray.length][tempArray[0].length];
            Data = tempArray;

            // Get the names of the Columns from the Column Property matrix
            Object[] colNames = new Object[Columns.length];
            for (int i=0; i<Columns.length; i++)
            {
                colNames[i] = Columns[i][0];
            }

            // Rebuild the table
            eventTableModel.updateTable(Data, colNames);

            // Restore the users settings column sizes
            for (int i=0; i<Columns.length; i++)
            {
                EventsTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i][0]);
                EventsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i][1]);
                EventsTable.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i][2]);
                EventsTable.getColumnModel().getColumn(i).setWidth(columnWidth[i][3]);
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean removeColumn()
    {
        int colIndex;
        String colName;

        colIndex = EventsTable.getSelectedColumn();
        colName = EventsTable.getColumnName(colIndex);
        
        LogManager.write("Event Table Builder  - INFO:     Removing column [" +colName+ "].",LogHandle.ALL,LogManager.INFO);

        // Check if the user tries to remove a protected column
        if (    colName.equalsIgnoreCase("_ID") ||
                colName.equalsIgnoreCase("_Act") ||
                colName.equalsIgnoreCase("_Poll") ||
                colName.equalsIgnoreCase("_Stop"))
        {
            LogManager.write("Event Table Builder  - MINOR:    Column [" +EventsTable.getColumnName(colIndex)+ "] is protected and cannot be removed.",LogHandle.ALL,LogManager.MINOR);
            return false;
        }

        Object[][] tempArray;

        // Get current settings of the column width
        int[][] columnWidth = new int[Columns.length][4];

        for (int i=0; i < Columns.length; i++)
        {
            columnWidth[i][0] = EventsTable.getColumnModel().getColumn(i).getMinWidth();
            columnWidth[i][1] = EventsTable.getColumnModel().getColumn(i).getPreferredWidth();
            columnWidth[i][2] = EventsTable.getColumnModel().getColumn(i).getMaxWidth();
            columnWidth[i][3] = EventsTable.getColumnModel().getColumn(i).getWidth();

//            LogManager.write("Event Table Builder  - Column [" +i+ "] - " +
//                    "minWidth [" +EventsTable.getColumnModel().getColumn(i).getMinWidth()+ "], " +
//                    "preWidth [" +EventsTable.getColumnModel().getColumn(i).getPreferredWidth()+ "], " +
//                    "maxWidth [" +EventsTable.getColumnModel().getColumn(i).getMaxWidth()+ "], " +
//                    "Width [" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "].",LogHandle.ALL,LogManager.DEBUG);
        }

        // Create a matrix with the excluded element for the columns
        tempArray = new Object[Columns.length-1][COL_PROPS_LENGTH];
        try
        {
            for (int i=0; i<tempArray.length; i++)
            {
                if ( i < colIndex)
                {
                    for (int j=0; j<COL_PROPS_INDEX; j++)
                    {
                        tempArray[i][j] = Columns[i][j];
                    }
                }
                else
                {
                    for (int j=0; j<COL_PROPS_INDEX; j++)
                    {
                        tempArray[i][j] = Columns[i+1][j];
                    }
                }
            }
            // Copy the temp matrix to the main one, so the main has the old element removed
            Columns = new Object[tempArray.length][tempArray[0].length];
            Columns = tempArray;

            //printMatrix(Data);

            // Create a matrix with the specified DATA column removed
            tempArray = new Object[Data.length][Columns.length];
            for (int i=0; i<tempArray.length; i++)
            {
                for (int j=0; j<tempArray[0].length; j++)
                {
                    if (j < colIndex)
                    {
                        tempArray[i][j] = Data[i][j];
                    }
                    else
                    {
                        tempArray[i][j] = Data[i][j+1];
                    }
                }
            }

            // Copy the temp matrix to the main one, so the main has the column removed
            Data = new Object[tempArray.length][tempArray[0].length];
            Data = tempArray;

            //printMatrix(Data);

            // Get the names of the Columns from the Column Property matrix
            Object[] colNames = new Object[Columns.length];
            for (int i=0; i<Columns.length; i++)
            {
                colNames[i] = Columns[i][0];
            }

            // Rebuild the table
            eventTableModel.updateTable(Data, colNames);

            // Restore the users settings column sizes
            for (int i=0; i<Columns.length; i++)
            {
                if (i != colIndex)
                {
                    EventsTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i][0]);
                    EventsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i][1]);
                    EventsTable.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i][2]);
                    EventsTable.getColumnModel().getColumn(i).setWidth(columnWidth[i][3]);
                }
            }
        }
        catch (Exception e)
        {
            LogManager.write("Event Table Builder  - MAJOR:    Cannot remove column with index [" +colIndex+ "].",LogHandle.ALL,LogManager.MAJOR);
            return false;
        }

        // Remove this column type from the column name list.
        _columnType.remove(colName);

        return true;
    }

    public boolean removeRow()
    {
        int rowIndex;
        rowIndex = EventsTable.getSelectedRow();

        LogManager.write("Event Table Builder  - INFO:     Removing row index [" +rowIndex+ "].",LogHandle.ALL,LogManager.INFO);

        Object[][] tempArray;

        // Get current settings of the column width
        int[][] columnWidth = new int[Columns.length][4];

        for (int i=0; i < Columns.length; i++)
        {
            columnWidth[i][0] = EventsTable.getColumnModel().getColumn(i).getMinWidth();
            columnWidth[i][1] = EventsTable.getColumnModel().getColumn(i).getPreferredWidth();
            columnWidth[i][2] = EventsTable.getColumnModel().getColumn(i).getMaxWidth();
            columnWidth[i][3] = EventsTable.getColumnModel().getColumn(i).getWidth();

//            LogManager.write("Event Table Builder  - Column [" +i+ "] - " +
//                    "minWidth [" +EventsTable.getColumnModel().getColumn(i).getMinWidth()+ "], " +
//                    "preWidth [" +EventsTable.getColumnModel().getColumn(i).getPreferredWidth()+ "], " +
//                    "maxWidth [" +EventsTable.getColumnModel().getColumn(i).getMaxWidth()+ "], " +
//                    "Width [" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "].",LogHandle.ALL,LogManager.DEBUG);
        }

        try
        {
            //printMatrix(Data);

            // Create a matrix with the specified DATA row removed
            tempArray = new Object[Data.length-1][Columns.length];
            for (int i=0; i<tempArray.length; i++)
            {
                if (i < rowIndex)
                {
                    for (int j=0; j<tempArray[0].length; j++)
                    {
                        tempArray[i][j] = Data[i][j];
                    }
                }
                else
                {
                    for (int j=0; j<tempArray[0].length; j++)
                    {
                        tempArray[i][j] = Data[i+1][j];
                    }
                }
            }

            // Copy the temp matrix to the main one, so the main has the column removed
            Data = new Object[tempArray.length][tempArray[0].length];
            Data = tempArray;

            //printMatrix(Data);

            // Get the names of the Columns from the Column Property matrix
            Object[] colNames = new Object[Columns.length];
            for (int i=0; i<Columns.length; i++)
            {
                colNames[i] = Columns[i][0];
            }

            // Rebuild the table
            eventTableModel.updateTable(Data, colNames);

            // Restore the users settings column sizes
            for (int i=0; i<Columns.length; i++)
            {
                EventsTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i][0]);
                EventsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i][1]);
                EventsTable.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i][2]);
                EventsTable.getColumnModel().getColumn(i).setWidth(columnWidth[i][3]);
            }
        }
        catch (Exception e)
        {
            LogManager.write("Event Table Builder  - MAJOR:    Cannot remove row with index [" +rowIndex+ "].",LogHandle.ALL,LogManager.MAJOR);
            return false;
        }

        return true;
    }

    public boolean clearTable()
    {
        LogManager.write("Event Table Builder  - INFO:     Clearing entire table [" +getTableName()+ "].",LogHandle.ALL,LogManager.INFO);

        _tableFile = _genhome.concat(TEMPLATE_TABLE);

        // Get all column properties
        Columns = getColumns();

        // Build the table data
        Data = createDataMatrix();

        // Get the names of the Columns from the Column Property matrix
        Object[] colNames = new Object[Columns.length];
        for (int i=0; i<Columns.length; i++)
        {
            colNames[i] = Columns[i][0];
        }
        eventTableModel.updateTable(Data, colNames);

        setColumnSize();

        return true;
    }

    public boolean saveTable()
    {
        FileOutputStream destination;   // Stream for writing the destination.
        OutputStreamWriter xmlWriter;   // Output stream writer

        int idMin   = 30, idMax   = 40;
        int actMin  = 25, actMax  = 40;
        int pollMin = 30, pollMax = 50;
        int stopMin = 30, stopMax = 50;
        int minWdth = 50, maxWdth = 700;

        LogManager.write("Event Table Builder  - DEBUG:    Saving table [" +getTableName()+ "].",LogHandle.ALL,LogManager.DEBUG);

        // Create a backup of the original table
        File tmpTable = new File(_genhome + "/props/", "orig_".concat(getTableName().concat(".xml")));
        File newTable = new File(_genhome + "/props/", getTableName().concat(".xml"));

        if (copyTable(newTable.getAbsolutePath(), tmpTable.getAbsolutePath()) != 0)
        {
            LogManager.write("Event Table Builder  - MAJOR:    Cannot create backup file [" +tmpTable.getAbsolutePath()+ "].",LogHandle.ALL,LogManager.MAJOR);
            return false;
        }

        // Delete original file
        if (!newTable.delete())
        {
            LogManager.write("Event Table Builder  - MAJOR:    Cannot delete original file [" +newTable.getAbsolutePath()+ "].",LogHandle.ALL,LogManager.MAJOR);
            return false;
        }

        // Create the new file stream
        try
        {
            destination = new FileOutputStream(newTable);
            xmlWriter = new OutputStreamWriter(destination, "8859_1");
        }
        catch (IOException e)
        {
            LogManager.write("Event Table Builder  - MAJOR:    File [" +newTable.getName()+ "] cannot be created.",LogHandle.ALL,LogManager.MAJOR);
            return false;
        }

        try
        {
			/*for (int i=0; i<EventsTable.getColumnCount(); i++) {
				LogManager.write("Event Table Builder  - Column [" +EventsTable.getColumnName(i)+ "] \t- [" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "].",LogHandle.ALL,LogManager.DEBUG);
			}
			*/
            xmlWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
            xmlWriter.write("<!-- Define Table  -->" + "\r\n");
            xmlWriter.write("<table name=\"" +_eventTableName+ "\" dbTarget=\"" +getDbTable()+ "\">" + "\r\n");
            for(int i=0; i<EventsTable.getColumnCount(); i++)
            {
                // Determine the type of the column value

                if (EventsTable.getColumnName(i).trim().matches("_ID"))
                {
                    xmlWriter.write("\t<!-- This cell defines the event ID -->" + "\r\n");
                    xmlWriter.write("\t<column name=\"" +EventsTable.getColumnName(i)+ "\" position=\"" +i+ "\" minWidth=\"" +idMin+ "\" prefWidth=\"" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "\" maxWidth=\"" +idMax+ "\" type=\"" +_columnType.getProperty(EventsTable.getColumnName(i))+ "\">" + "\r\n");
                }
                else if (EventsTable.getColumnName(i).trim().matches("_Act"))
                {
                    xmlWriter.write("\t<!-- This cell activates and deactivates the event poll -->" + "\r\n");
                    xmlWriter.write("\t<column name=\"" +EventsTable.getColumnName(i)+ "\" position=\"" +i+ "\" minWidth=\"" +actMin+ "\" prefWidth=\"" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "\" maxWidth=\"" +actMax+ "\" type=\"" +_columnType.getProperty(EventsTable.getColumnName(i))+ "\">" + "\r\n");
                }
                else if (EventsTable.getColumnName(i).trim().matches("_Poll"))
                {
                    xmlWriter.write("\t<!-- This cell defines the value of the  poll interval. Default: 60, 0 - Max speed  -->" + "\r\n");
                    xmlWriter.write("\t<column name=\"" +EventsTable.getColumnName(i)+ "\" position=\"" +i+ "\" minWidth=\"" +pollMin+ "\" prefWidth=\"" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "\" maxWidth=\"" +pollMax+ "\" type=\"" +_columnType.getProperty(EventsTable.getColumnName(i))+ "\">" + "\r\n");
                }
                else if (EventsTable.getColumnName(i).trim().matches("_Stop"))
                {
                    xmlWriter.write("\t<!-- This cell defines how many events to be sent. Default: 0 - indefinite  -->" + "\r\n");
                    xmlWriter.write("\t<column name=\"" +EventsTable.getColumnName(i)+ "\" position=\"" +i+ "\" minWidth=\"" +stopMin+ "\" prefWidth=\"" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "\" maxWidth=\"" +stopMax+ "\" type=\"" +_columnType.getProperty(EventsTable.getColumnName(i))+ "\">" + "\r\n");
                }
                else
                {
                	// 
                	int type = 1; // Char by default
                	try {
                		type = Integer.valueOf(_columnType.getProperty(EventsTable.getColumnName(i))).intValue();
                	} catch(NumberFormatException nfe) {
                        LogManager.write("Event Table Builder  - MAJOR:    Failed to set type for column [" +EventsTable.getColumnName(i)+ "] while saving the table. Setting to Char.",LogHandle.ALL,LogManager.MAJOR);
            		}
                    //LogManager.write("Event Table Builder  - PRINT: \t<!-- This cell contains the \"" +EventsTable.getColumnName(i)+ "\" value of the event -->",LogHandle.ALL,LogManager.DEBUG);
                    //LogManager.write("Event Table Builder  - PRINT: \t<column name=\"" +EventsTable.getColumnName(i)+ "\" position=\"" +i+ "\" minWidth=\"" +minWdth+ "\" prefWidth=\"" +preWdth+ "\" maxWidth=\"" +maxWdth+ "\" Width=\"" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "\"",LogHandle.ALL,LogManager.DEBUG);
                    xmlWriter.write("\t<!-- This cell contains the \"" +EventsTable.getColumnName(i)+ "\" value of the event -->" + "\r\n");
                    xmlWriter.write("\t<column name=\"" +EventsTable.getColumnName(i)+ "\" position=\"" +i+ "\" minWidth=\"" +minWdth+ "\" prefWidth=\"" +EventsTable.getColumnModel().getColumn(i).getWidth()+ "\" maxWidth=\"" +maxWdth+ "\" type=\"" +type+ "\">" + "\r\n");
                }

                //LogManager.write("Event Table Builder  - PRINT: \t\t<rows>" + "\r\n",LogHandle.ALL,LogManager.DEBUG);
                xmlWriter.write("\t\t<rows>" + "\r\n");
                for (int j=0; j<EventsTable.getRowCount(); j++)
                {
                    //LogManager.write("Event Table Builder  - PRINT: \t\t\t<cell value=\"" +eventTableModel.getValueAt(j, i)+ "\"/>",LogHandle.ALL,LogManager.DEBUG);
                    if (EventsTable.getColumnName(i).trim().matches("ID"))
                    {
                        // Re-number the events so it will match the numbers of the threads in the Thread Table
                        xmlWriter.write("\t\t\t<cell value=\"" +j+ "\"/>" + "\r\n");
                    }
                    else
                    {
                        xmlWriter.write("\t\t\t<cell value=\"" +EventsTable.getValueAt(j, i)+ "\"/>" + "\r\n");
                    }
                }
                xmlWriter.write("\t\t</rows>" + "\r\n");
                xmlWriter.write("\t</column>" + "\r\n");
            }
            xmlWriter.write("</table>" + "\r\n");

            xmlWriter.close();
            destination.close();
            LogManager.write("Event Table Builder  - DEBUG:    File [" +newTable.getName()+ "] created successfully.",LogHandle.ALL,LogManager.DEBUG);
        }
        catch (Exception e)
        {
            LogManager.write("Event Table Builder  - MAJOR:    Error occured while copying file [" +newTable.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);
            return false;
        }

        // Delete the TEMP file
        if (!tmpTable.delete())
        {
            LogManager.write("Event Table Builder  - MINOR:    Cannot delete TEMP file [" +tmpTable.getAbsolutePath()+ "].",LogHandle.ALL,LogManager.MINOR);
        }

        return true;
    }

    private void setColumnSize()
    {
        LogManager.write("Event Table Builder  - DEBUG:    Setting column sizes:",LogHandle.ALL,LogManager.DEBUG);

        // Setting column preferences
        for (int i=0; i<Columns.length; i++)
        {
            TableColumn tCol;
            tCol = EventsTable.getColumnModel().getColumn(i);
            try
            {
                min_w = -1;
                pre_w = -1;
                max_w = -1;
                min_w = Integer.valueOf(Columns[i][2].toString()).intValue();
                pre_w = Integer.valueOf(Columns[i][3].toString()).intValue();
                max_w = Integer.valueOf(Columns[i][4].toString()).intValue();
                if (min_w >= 0 && pre_w >= 0 && max_w >= 0)
                {
                    LogManager.write("Event Table Builder  - DEBUG:        - Column [" +tCol.getHeaderValue().toString()+ "] has sizes - [min/pre/max - " +min_w+ "/" +pre_w+ "/" +max_w+ "].",LogHandle.ALL,LogManager.DEBUG);
                    // What is the minimal width of the column?
                    tCol.setMinWidth(min_w);
                    // What is the preferred width of the column? - The recorded size of the column at "SAVE" time.
                    tCol.setPreferredWidth(pre_w);
                    // What is the maximal width of the column?
                    tCol.setMaxWidth(max_w);
                }
                else
                {
                    // The values is numerical, but negative, so we define it as invalid and set the default sizes.
                    LogManager.write("Event Table Builder  - MINOR:        - Column [" +tCol.getHeaderValue().toString()+ "] has NEGATIVE sizes - [min/pre/max - " +min_w+ "/" +pre_w+ "/" +max_w+ "]. Setting default - [min/pre/max - 30/40/100].",LogHandle.ALL,LogManager.MINOR);
                    tCol.setMinWidth(DEF_COL_MIN);
                    tCol.setPreferredWidth(DEF_COL_PRE);
                    tCol.setMaxWidth(DEF_COL_MAX);
                }
            }
            catch(NumberFormatException nfe)
            {
                LogManager.write("Event Table Builder  - MINOR:        - Column [" +tCol.getHeaderValue().toString()+ "] has INVALID sizes - [min/pre/max - " +Columns[i][2].toString()+ "/" +Columns[i][3].toString()+ "/" +Columns[i][4].toString()+ "]. Setting default - [min/pre/max - 30/40/100].",LogHandle.ALL,LogManager.MINOR);
                tCol.setMinWidth(DEF_COL_MIN);
                tCol.setPreferredWidth(DEF_COL_PRE);
                tCol.setMaxWidth(DEF_COL_MAX);
            }
        }
    }

    public String[] getTableList()
    {
        LogManager.write("Event Table Builder  - INFO:     Building table list:",LogHandle.ALL,LogManager.INFO);
        File location = new File(_genhome + "/props");

        String[] fileList = location.list(new XMLFileFilter());

        for (int i=0; i<fileList.length; i++)
        {
            fileList[i] = fileList[i].substring(fileList[i].lastIndexOf("/")+1, fileList[i].lastIndexOf("."));
            LogManager.write("Event Table Builder  - DEBUG:        - Table [" +i+ "] - [" +fileList[i]+ "].",LogHandle.ALL,LogManager.DEBUG);
        }
        return fileList;
    }

    // Filename Filter to filter files with specified extension
    public class XMLFileFilter implements FilenameFilter
    {
        public boolean accept(File file, String ext)
        {
            return ext.toLowerCase().endsWith(".xml");
        }
    }

    public boolean deleteTable(String tableName)
    {
        LogManager.write("Event Table Builder  - INFO:     Deleting table [" +tableName+ "].",LogHandle.ALL,LogManager.INFO);

        tableName = tableName.concat(".xml");
        File tableFile = new File(_genhome + "/props/", tableName);

        if (tableFile.delete())
        {
            return true;
        }
        return false;
    }

    public int createTable(String tableName) {
    	
        LogManager.write("Event Table Builder  - INFO:     Creating new table [" +tableName+ "].",LogHandle.ALL,LogManager.INFO);
        
        _tableFile = _genhome.concat("/props/").concat(tableName).concat(".xml");
        
        int retVal = copyTable(_genhome.concat(TEMPLATE_TABLE), _tableFile);
        
        try {
            LogManager.write("Event Table Builder  - DEBUG:    Setting table name in XML to [" +tableName+ "].",LogHandle.ALL,LogManager.DEBUG);
        	setTableAttribute("name", tableName);
        } catch (Exception e) {
        	retVal = 5; // This will mean that the user have to manually go and fix the table name in the XML file
        }
        
        return retVal;
    }

    public int copyTable(String sourceFilename, String destFilename)
    {
        LogManager.write("Event Table Builder  - DEBUG:    Copying template table to: [" +destFilename+ "].",LogHandle.ALL,LogManager.DEBUG);

        InputStream source;  // Stream for reading from the source file.
        OutputStream destination;   // Stream for writing the destination.

        int byteCount;

        File newTable = new File(destFilename);
        File templateTable = new File(sourceFilename);

        try
        {
            source = new FileInputStream(templateTable);
        }
        catch (FileNotFoundException e)
        {
            LogManager.write("Event Table Builder  - MAJOR:    Source file [" +sourceFilename+ "] corrupt or does not exist.",LogHandle.ALL,LogManager.MAJOR);
            return 1;
        }

        if (newTable.exists())
        {
            LogManager.write("Event Table Builder  - MINOR:    File [" +newTable.getName()+ "] already exist.",LogHandle.ALL,LogManager.MINOR);
            return 2;
        }

        /* Create the output stream.  If an error occurs, exit with error code. */

        try
        {
            destination = new FileOutputStream(newTable);
        }
        catch (IOException e)
        {
            LogManager.write("Event Table Builder  - MAJOR:    File [" +newTable.getName()+ "] cannot be created.",LogHandle.ALL,LogManager.MAJOR);
            return 3;
        }

        byteCount = 0;

        try
        {
            while (true)
            {
                int data = source.read();
                if (data < 0)
                    break;
                destination.write(data);
                byteCount++;
            }
            source.close();
            destination.close();
            LogManager.write("Event Table Builder  - DEBUG:    File [" +newTable.getName()+ "] created with [" +byteCount+ "] bytes of data.",LogHandle.ALL,LogManager.DEBUG);
        }
        catch (Exception e)
        {
            LogManager.write("Event Table Builder  - MAJOR:    Error occured at byte [" +byteCount+ "] while copying file [" +newTable.getName()+ "].",LogHandle.ALL,LogManager.MAJOR);
            return 4;
        }
        
        // If everything went OK, we have to modify the table name into the XML file
        
        
        return 0;
    }

    public void setTableName(String name)
    {
        _eventTableName = name;
    }

    public String getTableName()
    {
        if (_eventTableName == null || _eventTableName.trim() == "")
        {
            _eventTableName = TEMPLATE_TABLE;

            // Substring the crap and leave only the table name
            _eventTableName = _eventTableName.substring(_eventTableName.lastIndexOf("/")+1, _eventTableName.lastIndexOf("."));
        }
        return _eventTableName;
    }

    public void setDbTable(String dbTable)
    {
        _dbTable = dbTable;
    }

    public String getDbTable()
    {
        return _dbTable;
    }

    public int getSelectedRow()
    {
        return EventsTable.getSelectedRow();
    }

    @SuppressWarnings("unused")
	private void printMatrix(Object[][] Matrix)
    {
        // Printout the Data Matrix
        //******************************************************************
        LogManager.write("Event Table Builder  - DEBUG:    Table matrix of the Event Table:",LogHandle.ALL,LogManager.DEBUG);
        for (int col=0; col<Matrix.length; col++)
        {
            System.out.print("\n");
            for (int prop=0; prop<Matrix[0].length; prop++)
            {
                System.out.print("\t" +Matrix[col][prop]);
            }
        }
        System.out.print("\n\n");
        //******************************************************************
    }
}
