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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import generator.Generator;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Mar 18, 2005
 * Time: 12:00:15 PM
 * To change this template use Options | File Templates.
 */
public class ThreadTable extends Configurator
{
    // Template tabe in case "tableName" is NULL
    final String TEMPLATE_TABLE = "/props/template.xml";

    // The name of the table
    protected String _eventTableName;
    // Full path name of the xml file
    protected String _tableFile;

    // Event table
    protected Document _document;

    // Event data
    protected Properties _SingleEventData;

    // Column names
    final private Object[] Columns = {"ID", "Freq.", "Status", "Sent", "Restart"};
    // Data matrix
    protected Object[][] Data;

    protected CustTableModel threadsTableModel;
    public static JTable threadControlTable;

    // Event Generator
    private Generator eventGenerator;

    private int num = -1;

    public ThreadTable()
    {

    }

    public class CustTableModel extends DefaultTableModel
    {
		private static final long serialVersionUID = 1L;

		public CustTableModel(Object[] o, int s)
        {
            super(o, s);
        }

        /* * JTable uses this method to determine the default renderer/ * editor for each cell. If we didn't implement this method, * then the last column would contain text ("true"/"false"), * rather than a check box. */
        @SuppressWarnings("unchecked")
		public Class getColumnClass(int c)
        {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int column)
        {
            //if(column != 4)
            return false;
            //return true;
        }
    }

    public JTable createThreadTable(String tableName)
    {
        LogManager.write("Thread Table Builder - DEBUG:    Creating Table [" +tableName+ "].",LogHandle.ALL,LogManager.DEBUG);

        // Determine the XML File to load as a table
        if (tableName == null || tableName.trim() == "" || tableName.toLowerCase().trim().matches("template"))
        {
            _tableFile = _genhome.concat(TEMPLATE_TABLE);
        }
        else
        {
            if (!tableName.endsWith(".xml"))
            {
                _tableFile = _genhome.concat("/props/".concat(tableName.concat(".xml")));
            }
            File tabFile = new File(_tableFile);
            if (!tabFile.isFile())
            {
                LogManager.write("Thread Table Builder - MAJOR:    File [" +_tableFile+ "] does not exist.",LogHandle.ALL,LogManager.MAJOR);
                _tableFile = _genhome.concat(TEMPLATE_TABLE);
            }
        }

        LogManager.write("Thread Table Builder - DEBUG:    Creating \"Thread Table\" from file [" +_tableFile+ "].", LogHandle.ALL,LogManager.DEBUG);

        setTableName(_tableFile.substring(_tableFile.lastIndexOf("/")+1, _tableFile.lastIndexOf(".")));

        Data = createDataMatrix();

        threadsTableModel = new CustTableModel(Columns, 0);
        threadsTableModel.setDataVector(Data, Columns);

        threadControlTable = new JTable(threadsTableModel);
        //threadControlTable.getColumn("Restart").setCellRenderer(new ButtonRenderer());
        //threadControlTable.getColumn("Restart").setCellEditor(new ButtonEditor(new JCheckBox()));

        threadControlTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        threadControlTable.setBorder(BorderFactory.createLineBorder(Color.black));
        threadControlTable.setColumnSelectionAllowed(false);
        threadControlTable.setRowSelectionAllowed(false);
        threadControlTable.setDragEnabled(false);

        // If you fix the sixe of the table, the scroll bar will not work
        //threadControlTable.setMinimumSize(new Dimension(240, 195));
        //threadControlTable.setPreferredSize(new Dimension(250, 201));
        //threadControlTable.setMaximumSize(new Dimension(250, 201));

        threadControlTable.getColumnModel().getColumn(0).setMaxWidth(35);
        threadControlTable.getColumnModel().getColumn(0).setMinWidth(30);
        threadControlTable.getColumnModel().getColumn(0).setWidth(35);

        threadControlTable.getColumnModel().getColumn(1).setMaxWidth(50);
        threadControlTable.getColumnModel().getColumn(1).setMinWidth(40);
        threadControlTable.getColumnModel().getColumn(1).setWidth(50);

        threadControlTable.getColumnModel().getColumn(2).setMaxWidth(50);
        threadControlTable.getColumnModel().getColumn(2).setMinWidth(40);
        threadControlTable.getColumnModel().getColumn(2).setWidth(50);

        threadControlTable.getColumnModel().getColumn(3).setMaxWidth(65);
        threadControlTable.getColumnModel().getColumn(3).setMinWidth(60);
        threadControlTable.getColumnModel().getColumn(3).setWidth(65);

        threadControlTable.getColumnModel().getColumn(4).setMaxWidth(65);
        threadControlTable.getColumnModel().getColumn(4).setMinWidth(60);
        threadControlTable.getColumnModel().getColumn(4).setWidth(65);

        LogManager.write("Thread Table Builder - DEBUG:    \"Thread Table\" field created with [" +threadControlTable.getRowCount()+ "] elements.",LogHandle.ALL,LogManager.DEBUG);

        //printMatrix(Data);

        return threadControlTable;
    }

    private Object[][] createDataMatrix()
    {
        String colName;
        Object temp = null;
        Object[][] Data = null;

        File eventFile = new File(_tableFile);
        LogManager.write("Thread Table Builder - DEBUG:    Parsing [" +eventFile.getName()+ "] to build the table.",LogHandle.ALL,LogManager.DEBUG);

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
            _document = builder.parse( eventFile );

            // Getting content of the table(s)
            NodeList tableNode = _document.getElementsByTagName("table");
            for (int i=0; i<tableNode.getLength(); i++)
            {
                NodeList columnNode = _document.getElementsByTagName("column");
                NodeList cellNode = _document.getElementsByTagName("cell");

                int cls = columnNode.getLength();
                int rws = cellNode.getLength() / cls;
                Data = new Object[rws][cls];
                //LogManager.write("Thread Table Builder - DEBUG:    Cells [" +cellNode.getLength()+ "], Rows [" +rws+ "], Columns ["+cls+"].",LogHandle.ALL,LogManager.DEBUG);
                // In order to use them as indexes - decrement by one
                cls--;
                for (int k=cellNode.getLength(); k>0; k--)
                {
                    // Getting content of the table
                    rws--;
                    Element cell = (Element)cellNode.item(k-1);
                    Element col = (Element)columnNode.item(cls);

                    colName = col.getAttribute("name");
                    temp = cell.getAttribute("value");

                    //LogManager.write("Thread Table Builder - DEBUG:    Col: [" +cls+ "], Row: [" +rws+ "], Value: [" +temp.toString()+ "].",LogHandle.ALL,LogManager.DEBUG);
                    //LogManager.write("Thread Table Builder - DEBUG:    Column name: [" +colName+ "].",LogHandle.ALL,LogManager.DEBUG);

                    // Enumerating each event with ID number
                    if (colName.equalsIgnoreCase("_Poll"))
                    {
                        num = -1;
                        try
                        {
                            num = Integer.valueOf(temp.toString()).intValue();
                            if (num >= 0)
                            {
                                // The value is numerical so everything is OK
                                //LogManager.write("Event Table Builder  - Data[" +rws+ "][" +cls+ "] - [" +temp+ "] - NUMBER.",LogHandle.ALL,LogManager.DEBUG);
                                Data[rws][0] = new Integer(rws);
                                Data[rws][1] = new Integer(temp.toString());
                                Data[rws][2] = new ImageIcon(_genhome + "/images/status_red.png");
                                Data[rws][3] = new Integer(0);
                                Data[rws][4] = new ImageIcon(_genhome + "/images/thread_start.png");
                            }
                            else
                            {
                                // The valuse is numerical, but negative, so we define it as invalid.
                                LogManager.write("Thread Table Builder - MAJOR:    Data[" +rws+ "][" +cls+ "] - [" +temp+ "] - INVALID VALUE.",LogHandle.ALL,LogManager.MAJOR);
                                Data[rws][cls] = "xxx";
                            }
                        }
                        catch(NumberFormatException nfe)
                        {
                            // The valuse is possibly a simple string, so we put it directly into the cell.
                            LogManager.write("Thread Table Builder - MAJOR:    Data[" +rws+ "][" +cls+ "] - [" +temp+ "] - INVALID VALUE.",LogHandle.ALL,LogManager.MAJOR);
                            Data[rws][cls] = "xxx";
                        }

                        //LogManager.write("Thread Table Builder - Poll - [" +temp.toString()+ "].",LogHandle.ALL,LogManager.DEBUG);
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

    public void resetTableStat()
    {
        for (int i=0; i<threadControlTable.getRowCount(); i++)
        {
            threadControlTable.setValueAt(new Integer(0), i, 3);
        }
    }

    @SuppressWarnings("unused")
	private void printMatrix(Object[][] Matrix)
    {
        // Printout the Data Matrix
        //******************************************************************
        LogManager.write("Event Table Builder  - DEBUG:    Table matrix of the Threads Table:",LogHandle.ALL,LogManager.DEBUG);
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

    public boolean startGenerator()
    {
        if (eventGenerator != null)
        {
            eventGenerator = null;
        }

        eventGenerator = new Generator(_document);
        if (!eventGenerator.generatorStart())
        {
            eventGenerator = null;
            return false;
        }


        return true;
    }

    public void stopGenerator()
    {
        eventGenerator.generatorStop();
        eventGenerator = null;
    }

}
