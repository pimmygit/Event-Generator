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
package generator;

import log.LogManager;
import log.LogHandle;
import utils.GENProps;
import utils.SingleEvent;
import utils.EventQueue;
import utils.GENOptionPane;
import utils.GenConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import gui.ThreadTable;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Mar 15, 2005
 * Time: 2:22:49 PM
 * To change this template use Options | File Templates.
 */
public class Generator extends ThreadTable implements Runnable, GenConstants
{
	// Get the runtime object
	private Runtime oRuntime = Runtime.getRuntime();
	private int timeToClean = 0;
	
    // Priority of the main thread. Default: MAXIMUM_PRIORITY
    private final int MAXIMUM_PRIORITY = Thread.MAX_PRIORITY;
    private final int NORMAL_PRIORITY = Thread.NORM_PRIORITY;
    private final int MINIMUM_PRIORITY = Thread.MIN_PRIORITY;

    // Aims the buffer not to exceed that limit
    // If this value is reached the single threads will slow down
    private final int BUFFER_LIMIT_SIZE = 100;

    // Used to make the thread die if "STOP" button is pressed
    public static boolean RUN = false;

    // Used to make the thread sleep if the "PAUSE" button is pressed
    public static boolean PAUSE = false;

    // Generator thread
    Thread sender = null;

    // OS properties for the connection
    private GENProps _vars;
    // Event table document
    private Document _document;
    
    // Database table to populate
    private String _dbTable;
    
    // Column name
    private String colName;
    // Cell value
    private String cellValue;
    // Table columns
    Object[] Clmns = null;
    // Table data
    Object[][] Data = null;

    Properties _columnType;

    // Array of single event threads
    EventThread[] threadList = null;

    // Event queue
    protected static EventQueue _eventQueue;
    // Event to be send to OS
    private SingleEvent _eventToSend;
    // Object Server Connection
    protected OSConnection osConnection;

    // Integers for TMP purposes
    private long num_01;
    private long num_02; // Used to calculate events/min

    // Time is used to make the info update not sooner than a second time
    private long _prevTime01;
    private long _prevTime02;
    private long _prevTime03;

    // Defines the generator sleep time depending on the buffer size
    private int _sleepTime;
    private int _maxSleepTime;
    private int _prevBufSize;
    // Theoretical calculation of the events per minute
    // Used to define the calculation interval
    protected int _eventPerMinCountInterval;
    // Events per minute
    private long _e_min;
    // Generic incrementor
    private int increm_i;
    // Active thread calculations
    int _actThreadCount = 0;
    int _pollSum = 0;

    
    public Generator()
    {
    	_dbTable = DEF_DB_TABLE;
        _vars = new GENProps();
        _eventQueue = new EventQueue();
        _prevTime01 = System.currentTimeMillis();
        _prevTime02 = System.currentTimeMillis();
        _prevTime03 = System.currentTimeMillis();
        _sleepTime = 10;
        _prevBufSize = 0;
        num_02 = 0;
    }

    public Generator(Document tableDocument)
    {
        _document = tableDocument;
        _dbTable = DEF_DB_TABLE;
        _vars = new GENProps();
        _prevTime01 = System.currentTimeMillis();
        _prevTime02 = System.currentTimeMillis();
        _prevTime03 = System.currentTimeMillis();
        _sleepTime = 10;
        _prevBufSize = 0;
        _eventPerMinCountInterval = 0;
        num_02 = 0;

        // Init the column type property
        _columnType = new Properties();

    	// Get the database.table where the data will be sent to.
        Element mainNode = _document.getDocumentElement();
    	if (mainNode.getAttribute("dbTarget") != null && mainNode.getAttribute("dbTarget").length() > 0) {
    		_dbTable = mainNode.getAttribute("dbTarget");
            LogManager.write("Event Generator      - DEBUG:    Target database set to: [" +_dbTable+ "].",LogHandle.ALL,LogManager.DEBUG);
    	} else {
            LogManager.write("Event Generator      - MAJOR:    Target database not defined. Setting to: [alerts.status]",LogHandle.ALL,LogManager.MAJOR);
            _dbTable = "alerts.status";
    	}

        // Getting content of the table(s) and building each SingleEvent
        NodeList tableNode = _document.getElementsByTagName("table");
        
        for (int i=0; i<tableNode.getLength(); i++) {
        	
            NodeList columnNode = _document.getElementsByTagName("column");
            NodeList cellNode = _document.getElementsByTagName("cell");

            int cls = columnNode.getLength();
            int rws = cellNode.getLength() / cls;

            Clmns = new Object[cls];
            Data = new Object[rws][cls];

            //LogManager.write("Event Generator      - Cells [" +cellNode.getLength()+ "], Rows [" +rws+ "], Columns ["+cls+"].",LogHandle.ALL,LogManager.DEBUG);
            // In order to use them as indexes - decrement by one
            cls--;
            for (int k=cellNode.getLength(); k>0; k--)
            {
                // Getting content of the table
                rws--;
                Element cell = (Element)cellNode.item(k-1);
                Element col = (Element)columnNode.item(cls);
                
                colName = col.getAttribute("name");
                cellValue = cell.getAttribute("value");
                _columnType.setProperty(col.getAttribute("name"), col.getAttribute("type"));
                
                // There should be no chance for this not to be Integer
                int type = 1;
                try {
                	type = Integer.valueOf(_columnType.getProperty(colName)).intValue();
                } catch(NumberFormatException nfe) {
                    LogManager.write("Event Generator      - MAJOR:    User should not see this -> column[" +col.getAttribute("name")+ "] has invalid type defined: [" +col.getAttribute("type")+ "]. Type forced to [Char]. Please check the column type in the XML file.",LogHandle.ALL,LogManager.MAJOR);
        		}
            	String typeStr = (type != 0) ? "Char" : "Int";
            	
                LogManager.write("Event Generator      - DEBUG:    Col: [" +cls+ "] has name: [" +colName+ "] and type [" +typeStr+ "], Row: [" +rws+ "] has value: [" +cellValue.toString()+ "].",LogHandle.ALL,LogManager.DEBUG);

                // Add the column name to the array
                // Later it will be used to create the event field pairs
                Clmns[cls] = colName;

                // Enumerating each event with ID number
                if (cls == 0)
                {
                    Data[rws][cls] = new Integer(rws);
                }
                // Defining the type of the cell content
                else if (cellValue.toString().toLowerCase().trim().matches("false"))
                {
                    // The value is boolean, so we put a tickbox in the cell.
                    LogManager.write("Event Generator      - DEBUG:    Data[" +rws+ "][" +cls+ "] - [" +cellValue+ "] - FALSE.",LogHandle.ALL,LogManager.DEBUG);
                    Data[rws][cls] = new Boolean("false");
                }
                else if (cellValue.toString().toLowerCase().trim().matches("true"))
                {
                    // The value is boolean, so we put a tickbox in the cell.
                    LogManager.write("Event Generator      - DEBUG:    Data[" +rws+ "][" +cls+ "] - [" +cellValue+ "] - TRUE.",LogHandle.ALL,LogManager.DEBUG);
                    Data[rws][cls] = new Boolean("true");
                }
                else
                {
                    num_01 = -1;
                    try
                    {
                        num_01 = Integer.valueOf(cellValue.toString()).intValue();
                        if (num_01 >= 0)
                        {
                            // The value is numerical so we put number
                            LogManager.write("Event Generator      - DEBUG:    Data[" +rws+ "][" +cls+ "] - [" +cellValue+ "] - NUMBER.",LogHandle.ALL,LogManager.DEBUG);
                            Data[rws][cls] = new Integer(cellValue.toString());
                        }
                        else
                        {
                            // The value is numerical, but negative, so we define it as invalid.
                            LogManager.write("Event Generator      - DEBUG:    Data[" +rws+ "][" +cls+ "] - [" +cellValue+ "] - INVALID VALUE.",LogHandle.ALL,LogManager.MINOR);
                            Data[rws][cls] = "INVALID VALUE";
                        }
                    }
                    catch(NumberFormatException nfe)
                    {
                        // The value is possibly a simple string, so we put it directly into the cell.
                        LogManager.write("Event Generator      - DEBUG:    Data[" +rws+ "][" +cls+ "] - [" +cellValue+ "] - STRING.",LogHandle.ALL,LogManager.DEBUG);
                        Data[rws][cls] = cellValue;
                    }
                }

                if (rws == 0)
                {
                    rws = cellNode.getLength() / columnNode.getLength();
                    cls--;
                }
            }
        }

        _eventQueue = new EventQueue();
    }

    public Object[][] getData()
    {
        return Data;
    }

    public boolean generatorStart()
    {
        RUN = true;
        //LogManager.write("Event Generator      - Start threads [" +RUN+ "].", LogHandle.ALL,LogManager.DEBUG);
        SingleEvent singleEvent;
        _vars.loadProps();

        osConnection = new OSConnection(
                _vars.getOSName(),
                _vars.getOSHost(),
                _vars.getOSPort(),
                _vars.getOSUser(),
                _vars.getOSPass(),
                _vars.isOSSecure());

        // Connect to the OS, and keep the connection until the STOP button is pressed
        // While connection exists - send events from the queue
        if (!osConnection.connect())
        {
            return false;
        }
        
        threadList = new EventThread[Data.length];

        // Generate the event threads
        for (int row=0; row<Data.length; row++)
        {
            singleEvent = new SingleEvent(row, Data[0].length);

            //System.out.print("Event ID [" +row+ "], No. fields [" +Data[0].length+ "].\n");
            for (int prop=0; prop<Data[0].length; prop++)
            {
                // This is always the event ID
                if (prop == 0)
                {
                    singleEvent.setID(Integer.valueOf(Data[row][prop].toString()).intValue());
                }
                // This is always the event state - Act/Deact
                else if (prop == 1)
                {
                    singleEvent.setActive(Boolean.valueOf(Data[row][prop].toString()).booleanValue());
                }
                // This is always the poll interval
                else if (prop == 2)
                {
                    singleEvent.setPoll(Integer.valueOf(Data[row][prop].toString()).intValue());
                }
                // This is always the number of events to be sent
                else if (prop == 3)
                {
                    singleEvent.setEventsNumber(Integer.valueOf(Data[row][prop].toString()).intValue());
                }
                // The rest are the actual event fields
                else
                {
                    singleEvent.setEventValue(Clmns[prop].toString(),Data[row][prop].toString());
                }
            }
            singleEvent.setTargetDb(_dbTable);
            singleEvent.setColumnsTypes(_columnType);
            singleEvent.getEventColumns();

            // Build the threads
            threadList[row] = new EventThread(singleEvent);

            // Set the thread priority of the single threads
            if (row > 3)
            {
                threadList[row].setPriority(Thread.MIN_PRIORITY);
            }
            else
            {
                threadList[row].setPriority(Thread.NORM_PRIORITY);
            }
        }

        threadControlTable.addMouseListener(new MouseListener(){
            public void mouseEntered(MouseEvent me)
            {
                if (threadControlTable.getSelectedColumn() == 4)
                {
                    if (threadList[threadControlTable.getSelectedRow()].getActive())
                    {
                        if (!threadList[threadControlTable.getSelectedRow()].isPaused())
                        {
                            // Change the icon from "PAUSE" to "PAUSE_OVER"
                            //threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_restart_over.png"), threadControlTable.getSelectedRow(), 4);
                        }
                        else
                        {
                            // Change the icon from "RESTART" to "RESTART_OVER"
                            //threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_pause_over.png"), threadControlTable.getSelectedRow(), 4);
                        }
                    }
                }
            }
            public void mouseExited(MouseEvent me)
            {

            }
            public void mousePressed(MouseEvent me)
            {
                if (threadControlTable.getSelectedColumn() == 4)
                {
                    if (threadList[threadControlTable.getSelectedRow()].getActive())
                    {
                        if (!threadList[threadControlTable.getSelectedRow()].isPaused())
                        {
                            // Change the icon from "PAUSE" to "PAUSE_PRESSED"
                            //threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_restart_click.png"), threadControlTable.getSelectedRow(), 4);
                        }
                        else
                        {
                            // Change the icon from "RESTART" to "RESTART_PRESSED"
                            //threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_pause_click.png"), threadControlTable.getSelectedRow(), 4);
                        }
                    }
                }
            }
            public void mouseReleased(MouseEvent me)
            {
            }
            public void mouseClicked(MouseEvent me)
            {
                if (me.getClickCount() < 2)
                {
                    if (threadControlTable.getSelectedColumn() == 4)
                    {
                        // Thread is running (not completed) - "PAUSE" button is pressed
                        if (threadList[threadControlTable.getSelectedRow()].getActive() && !threadList[threadControlTable.getSelectedRow()].isCompleted() && !threadList[threadControlTable.getSelectedRow()].isPaused())
                        {
                            // Change the icon from "PAUSE" to "RESTART"
                            threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_restart.png"), threadControlTable.getSelectedRow(), 4);
                            // Pause the thread
                            threadList[threadControlTable.getSelectedRow()].setPauseThread(true);
                            // Set green/red status in the thread table
                            //threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/status_red.png"), threadControlTable.getSelectedRow(), 2);
                        }
                        // Thread is paused (not completed) - "RESTART" button is pressed
                        else if (!threadList[threadControlTable.getSelectedRow()].getActive() && !threadList[threadControlTable.getSelectedRow()].isCompleted() && threadList[threadControlTable.getSelectedRow()].isPaused())
                        {
                            // Change the icon from "RESTART" to "PAUSE"
                            threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_pause.png"), threadControlTable.getSelectedRow(), 4);
                            // Restart the thread
                            threadList[threadControlTable.getSelectedRow()].setPauseThread(false);
                            // Set green/red status in the thread table
                            //threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/status_green.png"), threadControlTable.getSelectedRow(), 2);
                        }
                        // Thread is completed - "START" button is pressed
                        else if (!threadList[threadControlTable.getSelectedRow()].getActive() && threadList[threadControlTable.getSelectedRow()].isCompleted() && !threadList[threadControlTable.getSelectedRow()].isPaused())
                        {
                            // Change the icon from "RESTART" to "PAUSE"
                            threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_pause.png"), threadControlTable.getSelectedRow(), 4);
                            // Restart the thread
                            threadList[threadControlTable.getSelectedRow()].setCompleted(false);
                            // Set green/red status in the thread table
                            //threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/status_green.png"), threadControlTable.getSelectedRow(), 2);
                        }
                        //JOptionPane.showMessageDialog(threadControlTable , "Button pressed: "+ new Integer(threadControlTable.getSelectedRow()));
                        //LogManager.write("Event Table Builder  - Click-Click.",LogHandle.ALL,LogManager.DEBUG);
                        //EventsTable.setColumnSelectionInterval(0,EventsTable.getColumnCount()-1);
                        //EventsTable.setRowSelectionInterval(0,EventsTable.getRowCount()-1);
                    }
                }
                else
                {
                    if (threadControlTable.getSelectedColumn() == 3)
                    {
                        threadControlTable.setValueAt(new Integer(0), threadControlTable.getSelectedRow(), 3);
                    }
                }
            }
        });


        // Start the generator threads
        if (sender == null)
        {
            sender = new Thread(this);
            // Define the main thread priority
            if (_vars.getThreadPriority().equalsIgnoreCase("MAX"))
            {
                sender.setPriority(MAXIMUM_PRIORITY);
            }
            else if (_vars.getThreadPriority().equalsIgnoreCase("MIN"))
            {
                sender.setPriority(MINIMUM_PRIORITY);
            }
            else
            {
                sender.setPriority(NORMAL_PRIORITY);
            }

            sender.start();
        }

        // Start the event threads
        for (int threadID=0; threadID<threadList.length; threadID++)
        {
            threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_pause.png"), threadID, 4);
            threadList[threadID].startThread();
        }

        // Set the amount of total threads in the status table
        _numThreads.setText(Integer.toString(threadList.length));

        return true;
    }

    public boolean restartGenerator()
    {
        LogManager.write("Event Generator      - DEBUG:    Restarting generator.", LogHandle.ALL,LogManager.DEBUG);

        return true;
    }

    public boolean generatorStop()
    {
        RUN = false;

        _vars = null;

        // Stop the event threads
        for (int threadID=0; threadID<threadList.length; threadID++)
        {
            threadList[threadID].stopThread();
        }

        for (int i=0; i<threadControlTable.getRowCount(); i++)
        {
            threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/status_red.png"), i, 2);
            threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_start.png"), i, 4);
        }

        // Stop the generator
        if (sender != null)
        {
            LogManager.write("Event Generator      - DEBUG:    Generator stopped.", LogHandle.ALL,LogManager.DEBUG);
            //sender.stop();
            sender = null;
        }

        // Clear all events from the queue
        _eventQueue.clearQueue();

        // Close the connection to the Object Server
        if (!osConnection.disconnect()) {
        	return false;
        }

        return true;
    }

    public void run()
    {
        LogManager.write("Event Generator      - DEBUG:    Generator started.", LogHandle.ALL,LogManager.DEBUG);
        
        NodeList tableNode;
        Element tableName;
        
        while (RUN)
        {
            //LogManager.write("Event Generator      - Event queue size: [" +_eventQueue.getSize()+ "] and Empty: [" +_eventQueue.isEmpty()+ "].", LogHandle.ALL,LogManager.DEBUG);

            if (_eventQueue.getSize() > 0)
            {
                _eventToSend = getEvent();
                if (!osConnection.sendEvent(_eventToSend)) {
                	
                    tableNode = _document.getElementsByTagName("table");
                    tableName = (Element)tableNode.item(0);

                    LogManager.write("Event Generator      - MAJOR:    Possible syntax error in table: [" +tableName.getAttribute("name")+ "].", LogHandle.ALL,LogManager.MAJOR);
                    GENOptionPane.showMessageDialog(null,
                            "Error while sending event ID:[" +_eventToSend.getID()+ "] to [" +_vars.getOSName()+ "]\n" +
                            "Please check the log: [" +log_file+ "].\n" +
                            "Verify column types and values in table: [" +tableName.getAttribute("name")+ "].\n" +
                            "Event generation terminated.", "  SQL Execution.  ", GENOptionPane.ERROR_MESSAGE);
                    startMenuItem.setEnabled(true);
                    startButton.setEnabled(true);
                    stopMenuItem.setEnabled(false);
                    stopButton.setEnabled(false);
                    setEditableOSSettings(true);
                    setEditableEventTable(true);
                    _eventPerMin.setText("0");
                    _minuteInfo.setText("0");
                    
                    generatorStop();
                }
                
                // Increment each thread count
                updateSingleThreadCount(_eventToSend.getID());

                // Increment total events sent
                updateTotalSent();

                // Calculate events/min
                updateEventsPerMinute();

                // Get buffer size
                updateBuffer();

                setSleepTime();
                
                // Perform garbage collection on every 10000 event sent
                if (timeToClean > 10000) {
                	timeToClean = 0;
                    oRuntime.gc();
                }
                timeToClean++;
                
                try{Thread.sleep(_sleepTime);}catch(Exception e){};
            }
            else
            {
                try{Thread.sleep(100);}catch(Exception e){};
            }
            // Determine amount of active threads
            updateActiveThreads();

        }
    }

    private void updateSingleThreadCount(int eventId) {
        threadControlTable.setValueAt(new Integer(Integer.valueOf(threadControlTable.getValueAt(eventId, 3).toString()).intValue()+1), eventId, 3);
    }

    private void updateActiveThreads()
    {
        _actThreadCount = 0;
        _pollSum = 0;
        _maxSleepTime = 0;

        if (System.currentTimeMillis() > (_prevTime01 + 1000))
        {
            for(increm_i=0; increm_i<threadList.length; increm_i++)
            {
                if (threadList[increm_i].getActive())
                {
                    _actThreadCount++;
                    // Set green status in the thread table
                    threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/status_green.png"), increm_i, 2);

                    // Calculate the sum of the polls per minute
                    _pollSum = _pollSum + Integer.valueOf(threadControlTable.getValueAt(increm_i, 1).toString()).intValue();
                }
                else if (threadList[increm_i].isCompleted())
                {
                    // Set the "START" button
                    threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_start.png"), increm_i, 4);
                    // Set red status in the thread table
                    threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/status_red.png"), increm_i, 2);
                }
                else
                {
                    // Set red status in the thread table
                    threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/status_red.png"), increm_i, 2);
                }
            }

            // Calculate max sleep time
            // It should not be longer than the sum of all poll intervals
            // In case we have only one thread, it is not a bad idea if we divide the time by two
            _maxSleepTime = _pollSum / 2;
            // If a thread is polling at max speed then "_maxSleepTime" will be NULL
            if (_maxSleepTime > 0)
            {
                _maxSleepTime = 60000 / _maxSleepTime;
                if (_maxSleepTime > 100)
                {
                    _maxSleepTime = 100;
                }
            }


            //LogManager.write("Event Generator      - Sum of poll intervals: [" +pollSum+ "] per minute.", LogHandle.ALL,LogManager.DEBUG);
            // Determine the event per minute calculation time
            if (_pollSum > 600)
            {
                _eventPerMinCountInterval = 1000;
            }
            else if (_pollSum <= 600 && _pollSum > 300)
            {
                _eventPerMinCountInterval = 3000;
            }
            else if (_pollSum <= 300 && _pollSum > 60)
            {
                _eventPerMinCountInterval = 6000;
            }
            else if (_pollSum <= 60)
            {
                _eventPerMinCountInterval = 30000;
            }

            _prevTime01 = System.currentTimeMillis();
            _actThreads.setText(Integer.toString(_actThreadCount));
        }
    }

    private void updateTotalSent() {
    	
    	_numEventsSent++;
    	
    	if (_numEventsSent < 100000) {
    		_totalSent.setText(Long.toString(_numEventsSent));
    		_totalInfo.setText(Long.toString(_numEventsSent));
    	} else if (_numEventsSent < 1000000) {
    		_totalSent.setText(_numEventsSent/1000 + "k");
    		_totalInfo.setText(_numEventsSent/1000 + "k");
    	} else if (_numEventsSent < 10000000) {
    		
            //Format the output to have only 4 digits after the decimal point
            DecimalFormat myFormatter = new DecimalFormat("##.##");
            
    		_totalSent.setText(Double.parseDouble(myFormatter.format((double)_numEventsSent/1000000.0)) + "M");
    		_totalInfo.setText(Double.parseDouble(myFormatter.format((double)_numEventsSent/1000000.0)) + "M");
    	} else {
    		_totalSent.setText(_numEventsSent/1000000 + "M");
    		_totalInfo.setText(_numEventsSent/1000000 + "M");
    	}
    }

    private void updateEventsPerMinute()
    {
        //LogManager.write("Event Generator      - Counting interval: [" +_eventPerMinCountInterval+ "] seconds.", LogHandle.ALL,LogManager.DEBUG);

        if (System.currentTimeMillis() > (_prevTime03 + _eventPerMinCountInterval))
        {
            _e_min = _numEventsSent - num_02;
            switch (_eventPerMinCountInterval)
            {
                case 1000 :
                    //LogManager.write("Event Generator      - Counted [" +e_min+ "] events for [" +_eventPerMinCountInterval+ "] seconds. Multiply by 60: [" +(e_min * 60)+ "].", LogHandle.ALL,LogManager.DEBUG);
                    _e_min = _e_min * 60; // Events per minute calculated every second
                    break;
                case 3000 :
                    //LogManager.write("Event Generator      - Counted [" +e_min+ "] events for [" +_eventPerMinCountInterval+ "] seconds. Multiply by 20: [" +(e_min * 20)+ "].", LogHandle.ALL,LogManager.DEBUG);
                    _e_min = _e_min * 20; // Events per minute calculated every 3 seconds
                    break;
                case 6000 :
                    //LogManager.write("Event Generator      - Counted [" +e_min+ "] events for [" +_eventPerMinCountInterval+ "] seconds. Multiply by 10: [" +(e_min * 10)+ "].", LogHandle.ALL,LogManager.DEBUG);
                    _e_min = _e_min * 12; // Events per minute calculated every 5 seconds
                    break;
                default:
                    //LogManager.write("Event Generator      - Counted [" +e_min+ "] events for [" +_eventPerMinCountInterval+ "] seconds. Multiply by 2: [" +(e_min * 2)+ "].", LogHandle.ALL,LogManager.DEBUG);
                    _e_min = _e_min * 6; // Events per minute calculated every 10 seconds
                    break;
            }

            if (_e_min < 0)
            {
                _e_min = 0;
            }

            _minuteInfo.setText(Long.toString(_e_min));
            _eventPerMin.setText(Long.toString(_e_min));

            _prevTime03 = System.currentTimeMillis();
            num_02 = _numEventsSent;
        }
    }

    private void updateBuffer()
    {
        //LogManager.write("Event Generator      - Current time: [" +System.currentTimeMillis()+ "].", LogHandle.ALL,LogManager.DEBUG);

        if (System.currentTimeMillis() > (_prevTime02 + 1000))
        {
            // Increase or decrease the speed of the single threads
            if (_eventQueue.getSize() >= BUFFER_LIMIT_SIZE)
            {
                for (increm_i=0; increm_i<threadList.length; increm_i++)
                {
                    threadList[increm_i].incForcedDelay();
                }
            }
            else
            {
                for (increm_i=0; increm_i<threadList.length; increm_i++)
                {
                    threadList[increm_i].decForcedDelay();
                }
            }

            // Update buffer size
            _buffer.setText(Integer.toString(_eventQueue.getSize()));
            _prevTime02 = System.currentTimeMillis();
            //LogManager.write("Event Generator      - Thread sleep time: [" +_sleepTime+ "].", LogHandle.ALL,LogManager.DEBUG);
        }
    }

    private void setSleepTime()
    {
        if ( ((_sleepTime >= _maxSleepTime) && (_maxSleepTime > 0)) || ((_eventQueue.getSize() > _prevBufSize + 5) && (_sleepTime >= 10)) )
        {
            _sleepTime = _sleepTime - 10;
        }
        else if (_eventQueue.getSize() < _prevBufSize + 5)
        {
            _sleepTime = _sleepTime + 10;
        }
    }

    // Add events to the queue
    public synchronized void addEvent(SingleEvent event)
    {
        if (_eventQueue != null) {
            _eventQueue.addEvent(event);
        } else {
            LogManager.write("Event Generator      - MAJOR:    Queue is NULL.", LogHandle.ALL,LogManager.MAJOR);
        }
    }

    public synchronized SingleEvent getEvent()
    {
        try {
            //LogManager.write("Event Generator      - Extracting event from the queue.", LogHandle.ALL,LogManager.DEBUG);
            return (SingleEvent)_eventQueue.getEvent();
        } catch (InterruptedException ie) {
            LogManager.write("Event Generator      - MAJOR:    Extracting event from queue failed.", LogHandle.ALL,LogManager.MAJOR);
            return null;
        }
    }

    public synchronized void setRun(boolean doRun)
    {
        RUN = doRun;
    }

    public synchronized boolean doRun()
    {
        return RUN;
    }
}
