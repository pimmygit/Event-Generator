/******************************************************************
*
* IBM Tivoli Event Generator
*
* IBM Confidential
* OCO Source Materials
*
* 5724-S45
*
* (C) Copyright IBM Corp. 2005, 2010
*
* The source code for this program is not published or otherwise
* divested of its trade secrets, irrespective of what has
* been deposited with the U.S. Copyright Office.
*
******************************************************************/
package generator;

import utils.EventValueParser;
import utils.SingleEvent;
import log.LogManager;
import log.LogHandle;

import javax.swing.*;
import java.util.Random;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Mar 15, 2005
 * Time: 11:28:01 AM
 * To change this template use Options | File Templates.
 */
public class EventThread extends Generator implements Runnable
{
    private final int INT_NORMAL = 0;
    private final int INT_INCR = 1;
    private final int INT_RAND = 2;
    private final int INT_TIME = 3;
    private final int CHAR_NORMAL = 4;
    private final int CHAR_INCR = 5;
    private final int CHAR_RAND = 6;
    private final int CHAR_TIME = 7;
    private final int GIS = 8;

    // Incremented at each loop of the thread
    private int LOOP;

    // Random generator
    private Random randomGenerator;

    // Permission to run
    private boolean _go;
    // Pause the thread
    private boolean PAUSED;
    // Status of the thread
    public boolean ACTIVE;
    // Completed set of sent events
    public boolean COMPLETED = false;
    // In case the buffer grows too much, force the thread to slow down
    public int _forcedDelay = 0;
    // Increase the forced delay by:
    private final int DELAY = 10;
    // Event data
    protected SingleEvent _event;
    // Number of events to be sent
    protected int _stopAt = 0;

    // Single event thread
    Thread worker = null;

    public EventThread(SingleEvent event)
    {
        LOOP = 0;

        randomGenerator = new Random();

        _event = event;

        _stopAt = _event.getEventsNumber();

        _go = _event.isActive();
    }

    public void startThread()
    {
        if (worker == null)
        {
            worker = new Thread(this);
            worker.start();
        }
    }

    public void stopThread()
    {
        if (worker != null)
        {
            LogManager.write("Single Event Thread  - INFO:     Single thread [" +_event.getID()+ "] stopped.", LogHandle.ALL,LogManager.INFO);
            //worker.stop();
            try{Thread.sleep(10);}catch(Exception e){};
            worker = null;
        }
    }

    public void run()
    {
        int poll;
        int type = 0;
        
        String[] columnNames;
        String[] eventValues;
        
        EventValueParser eventParser;
        
        long intStart = 0;
        long intStep = 0;
        long step = 0;
        long intValIncr = 0;
        
        int intFrom = 0;
        int intTo = 0;
        int intValRand = 0;
        
        long charIncrValue = 0;
        long charIncrStart = 0;
        long charIncrStep = 0;
        
        int charRandValue = 0;
        int charRandFrom = 0;
        int charRandTo = 0;
        
        double valMinLat = 0;
        double valMaxLat = 0;
        double valMinLong = 0;
        double valMaxLong = 0;
        double offset = 0;
        double tmpLat = 0;
        double tmpLong = 0;
        double randLat = 0;
        double randLong = 0;
        
        if (_event.getPoll() > 0) {
            poll = 60000/_event.getPoll();
        } else {
            poll = 5;
        }

        if (_go) {
            setActive(true);
            LogManager.write("Single Event Thread  - INFO:     Single thread [" +_event.getID()+ "] has started to poll every [" +poll+ "] millisecond.", LogHandle.ALL,LogManager.INFO);
        } else {
            setActive(false);
            threadControlTable.setValueAt(new ImageIcon(_genhome + "/images/thread_start.png"), _event.getID(), 4);
            LogManager.write("Single Event Thread  - INFO:     Single thread [" +_event.getID()+ "] not started to poll every [" +poll+ "] millisecond.", LogHandle.ALL,LogManager.INFO);
        }
        
        // run for indefinite period
        if (_stopAt == 0) {
        	
            while (_go) {
            	
                // Forced delay
                if ((_event.getPoll() == 0) && (getForcedDelay() > 0)) {
                    try{Thread.sleep(getForcedDelay());}catch(Exception e){};
                }

                // Determine field values where we have TIME, INCR_INT, RAND_INT.
                columnNames = _event.getEventColumns();
                eventValues = _event.getEventValues();
                
                for (int i=0; i < columnNames.length; i++) {
                	
                    LogManager.write("Single Event Thread  - DEBUG:    Parsing column[" +columnNames[i]+ "], eventID[" + _event.getID() + "], cellValue[" + eventValues[i] + "].",LogHandle.ALL,LogManager.DEBUG);	
                	eventParser = new EventValueParser(eventValues[i]);
                	
                    if (columnNames[i].equalsIgnoreCase("_ID") ||
                            columnNames[i].equalsIgnoreCase("_Act") ||
                            columnNames[i].equalsIgnoreCase("_Poll") ||
                            columnNames[i].equalsIgnoreCase("_Stop")) {
                        continue;
                    } else {
                        // Determine the generation type of the data cell by its format and column type
                        type = eventParser.getDataType(_event.getColumnType(columnNames[i]));
                        LogManager.write("Single Event Thread  - DEBUG:    Value [" +_event.getEventValues()[i]+ "] for column [" +columnNames[i]+ "] is of type [" +type+ "].",LogHandle.ALL,LogManager.DEBUG);
                        
                        _event.setEventValueType(columnNames[i], Integer.valueOf(type).toString());
                    }

                    switch (type)
                    {
                        case INT_NORMAL :
                            break;

                        case INT_INCR :

                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase(""))
                            {
                                continue;
                            }

                            // "Start" and "Step" values are determined from the cell value, which
                            // has the following format "Start:xxx, Step:xxx".
                            // We convert this to: "Start:xxx, Step:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            intStart = eventParser.getValueStart();
                            intStep = eventParser.getValueStep();
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has Start:[" +intStart+ "], Step:[" +intStep+ "].", LogHandle.ALL,LogManager.DEBUG);
                            
                            step = intStep*LOOP;
                            intValIncr = intStart + step;
                            _event.setEventValue(_event.getEventColumns()[i], "Start:"+intStart+", Step:"+intStep+", Value:"+intValIncr);
                            break;
                        
                        case INT_RAND :

                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase(""))
                            {
                                continue;
                            }

                            // "From" and "To" values are determined from the cell value, which
                            // has the following format "From:xxx, To:xxx"
                            // We convert this to: "From:xxx, To:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            intFrom = eventParser.getValueFrom();
                            intTo = eventParser.getValueTo();
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has From:[" +intFrom+ "], To:[" +intTo+ "].", LogHandle.ALL,LogManager.DEBUG);

                            // Generate a value from "from" to "to"
                            try {
                            	intValRand = randomGenerator.nextInt(intTo-intFrom) + intFrom;
                            } catch (IllegalArgumentException eae) {
                                LogManager.write("Single Event Thread  - MINOR:    Event[" +_event.getID()+ "] failed to generate random number due to negative bound [" +(intTo-intFrom)+ "] Setting to From:[" +intFrom+ "].", LogHandle.ALL,LogManager.MINOR);
                            	charRandValue = charRandFrom;
                            }
                            _event.setEventValue(_event.getEventColumns()[i], "From:"+intFrom+", To:"+intTo+", Value:"+intValRand);
                            break;

                        case INT_TIME :
                            break;

                        case CHAR_NORMAL :
                            break;
                            
                        case CHAR_INCR :

                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase("")) {
                                continue;
                            }

                            // "Start" and "Step" values are determined from the cell value, which
                            // has the following format "Start:xxx, Step:xxx".
                            // We convert this to: "Start:xxx, Step:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            // We also check for any Prefix or Suffix.
                            charIncrValue = 0;
                            charIncrStart = eventParser.getValueStart();
                            charIncrStep = eventParser.getValueStep();
                            String charIncrPref = eventParser.getValuePref();
                            String charIncrSuff = eventParser.getValueSuff();                                                        
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has Start:[" +charIncrStart+ "], Step:[" +charIncrStep+ "], Pref: [" +charIncrPref+ "], Suff: [" +charIncrSuff+ "].", LogHandle.ALL,LogManager.DEBUG);
                            
                            step = charIncrStep*LOOP;
                            charIncrValue = charIncrStart + step;

                            if (charIncrPref != "" && charIncrSuff != "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "Start:"+charIncrStart+", Step:"+charIncrStep+", Pref:" +charIncrPref+ ", Suff:" +charIncrSuff+ ", Value:"+charIncrPref+charIncrValue+charIncrSuff);
                            }
                            else if (charIncrPref == "" && charIncrSuff != "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "Start:"+charIncrStart+", Step:"+charIncrStep+", Suff:" +charIncrSuff+ ", Value:"+charIncrValue+charIncrSuff);
                            }
                            else if (charIncrPref != "" && charIncrSuff == "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "Start:"+charIncrStart+", Step:"+charIncrStep+", Pref:" +charIncrPref+ ", Value:"+charIncrPref+charIncrValue);
                            }
                            else
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "Start:"+charIncrStart+", Step:"+charIncrStep+", Value:"+charIncrValue);
                            }
                            break;
                            
                        case CHAR_RAND :
                        	
                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase("")) {
                                continue;
                            }

                            // "From" and "To" values are determined from the cell value, which
                            // has the following format "From:xxx, To:xxx"
                            // We convert this to: "From:xxx, To:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            // We also check for any Prefix or Suffix.
                            charRandValue = 0;
                            charRandFrom = eventParser.getValueFrom();
                            charRandTo = eventParser.getValueTo();
                            String charRandPref = eventParser.getValuePref();
                            String charRandSuff = eventParser.getValueSuff();                                                        
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has From:[" +charRandFrom+ "], To:[" +charRandTo+ "], Pref: [" +charRandPref+ "], Suff: [" +charRandSuff+ "].", LogHandle.ALL,LogManager.DEBUG);

                            // Generate a value from "from" to "to"
                            try {
                            	charRandValue = randomGenerator.nextInt(charRandTo-charRandFrom) + charRandFrom;
                            } catch (IllegalArgumentException eae) {
                                LogManager.write("Single Event Thread  - MINOR:    Event[" +_event.getID()+ "] failed to generate random number due to negative bound [" +(charRandTo-charRandFrom)+ "] Setting to From:[" +charRandFrom+ "].", LogHandle.ALL,LogManager.MINOR);
                            	charRandValue = charRandFrom;
                            }

                            if (charRandPref != "" && charRandSuff != "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "From:"+charRandFrom+", To:"+charRandTo+", Pref:" +charRandPref+ ", Suff:" +charRandSuff+ ", Value:"+charRandPref+charRandValue+charRandSuff);
                            }
                            else if (charRandPref == "" && charRandSuff != "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "From:"+charRandFrom+", To:"+charRandTo+", Suff:" +charRandSuff+ ", Value:"+charRandValue+charRandSuff);
                            }
                            else if (charRandPref != "" && charRandSuff == "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "From:"+charRandFrom+", To:"+charRandTo+", Pref:" +charRandPref+ ", Value:"+charRandPref+charRandValue);
                            }
                            else
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "From:"+charRandFrom+", To:"+charRandTo+", Value:"+charRandValue);
                            }
                            break;

                        case CHAR_TIME :
                            break;
                            
                        case GIS :

                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase("")) {
                                continue;
                            }
                            
                            // "minLat", "maxLat", "minLong" and "maxLong" values are determined from the cell value, which
                            // has the following format "minLat:xxx, maxLat:xxx, minLong:xxx, maxLong:xxx"
                            // We convert this to: "minLat:xxx, maxLat:xxx, minLong:xxx, maxLong:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            valMinLat = eventParser.getValueMinLat();
                            valMaxLat = eventParser.getValueMaxLat();
                            valMinLong = eventParser.getValueMinLong();
                            valMaxLong = eventParser.getValueMaxLong();
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has minLat:[" +valMinLat+ "], maxLat:[" +valMaxLat+ "], minLong:[" +valMinLong+ "], maxLong:[" +valMaxLong+ "].", LogHandle.ALL,LogManager.DEBUG);

                            offset = 0;
                            tmpLat = 0;
                            tmpLong = 0;
                            randLat = 0;
                            randLong = 0;

                            // Verify the correct sizes
                            if (valMinLat > valMaxLat)
                            {
                                LogManager.write("Single Event Thread  - MINOR:    minLat:[" +valMinLat+ "] cannot be bigger than maxLat:[" +valMaxLat+ "]. Setting world boundary.", LogHandle.ALL,LogManager.MINOR);
                                valMinLat = -90;
                                valMaxLat = 90;
                            }
                            if (valMinLong > valMaxLong)
                            {
                                LogManager.write("Single Event Thread  - MINOR:    minLong:[" +valMinLong+ "] cannot be bigger than maxLong:[" +valMaxLong+ "]. Setting world boundary.", LogHandle.ALL,LogManager.MINOR);
                                valMinLong = -180;
                                valMaxLong = 180;
                            }

                            // Generate a value from "minLat" to "maxLat" and "minLong" and maxLong"
                            if (valMinLat < 0 && valMaxLat < 0)
                            {
                            	offset = valMaxLat - valMinLat;
                                tmpLat = randomGenerator.nextDouble() * offset;
                                randLat = tmpLat + valMinLat;
                            }
                            else if (valMinLat < 0 && valMaxLat > 0)
                            {
                            	offset = valMinLat * (-1) + valMaxLat;
                                tmpLat = randomGenerator.nextDouble() * offset;
                                randLat =  tmpLat + valMinLat;
                            }
                            else if (valMinLat >= 0 && valMaxLat >= 0)
                            {
                            	offset = valMaxLat - valMinLat;
                                tmpLat = randomGenerator.nextDouble() * offset;
                                randLat = tmpLat + valMinLat;
                            }

                            // Generate a value from "minLat" to "maxLat" and "minLong" and maxLong"
                            if (valMinLong < 0 && valMaxLong < 0)
                            {
                            	offset = valMaxLong - valMinLong;
                                tmpLong = randomGenerator.nextDouble() * offset;
                                randLong = tmpLong + valMinLong;
                            }
                            else if (valMinLong < 0 && valMaxLong > 0)
                            {
                            	offset = valMinLong * (-1) + valMaxLong;
                                tmpLong = randomGenerator.nextDouble() * offset;
                                randLong = tmpLong + valMinLong;
                            }
                            else if (valMinLong >= 0 && valMaxLong >= 0)
                            {
                            	offset = valMaxLong - valMinLong;
                                tmpLong = randomGenerator.nextDouble() * offset;
                                randLong = tmpLong + valMinLong;
                            }

                            //Format the output to have only 4 digits after the decimal point
                            DecimalFormat myFormatter = new DecimalFormat("###.####");
                            
                            randLat = Double.parseDouble(myFormatter.format(randLat));
                            randLong = Double.parseDouble(myFormatter.format(randLong));

                            _event.setEventValue(_event.getEventColumns()[i], "minLat:"+valMinLat+", maxLat:"+valMaxLat+", minLong:"+valMinLong+", maxLong:"+valMaxLong+", Value:Lat:"+randLat+" Long:"+randLong);
                            break;

                        default :

                            break;
                    }
                }

                LOOP++;

                // Make the thread sleep
                while (isPaused()) {
                    setActive(false);
                    try{Thread.sleep(500);}catch(Exception e){};
                }

                if (super.doRun()) {
                    setActive(true);
                    try{Thread.sleep(poll);}catch(Exception e){};
                    LogManager.write("Single Event Thread  - INFO:     Single thread [" +_event.getID()+ "] appends event to the queue.", LogHandle.ALL,LogManager.INFO);
                    addEvent(_event);
                } else {
                    setActive(false);
                    _go = false;
                    stopThread();
                }
            }
        }
        // run exactly "_stopAt" times
        else
        {
            while (_go) {
            	
                if (isCompleted()) {
                    _stopAt = _event.getEventsNumber();
                    try{Thread.sleep(500);}catch(Exception e){};
                    continue;
                }

                // Forced delay
                if ((_event.getPoll() == 0) && (getForcedDelay() > 0)) {
                    try{Thread.sleep(getForcedDelay());}catch(Exception e){};
                }

                // Determine field values where we have TIME, INCR_INT, RAND_INT.
                columnNames = _event.getEventColumns();
                eventValues = _event.getEventValues();
                
                for (int i=0; i < columnNames.length; i++) {
                	
                    LogManager.write("Single Event Thread  - DEBUG:    Parsing column[" +columnNames[i]+ "], eventID[" + _event.getID() + "], cellValue[" + eventValues[i] + "].",LogHandle.ALL,LogManager.DEBUG);	

                	eventParser = new EventValueParser(eventValues[i]);
                	
                    if (columnNames[i].equalsIgnoreCase("_ID") ||
                            columnNames[i].equalsIgnoreCase("_Act") ||
                            columnNames[i].equalsIgnoreCase("_Poll") ||
                            columnNames[i].equalsIgnoreCase("_Stop")) {
                        continue;
                    } else {
                    	// Determine the generation type of the data cell by its format and column type
                        type = eventParser.getDataType(_event.getColumnType(columnNames[i]));
                        LogManager.write("Single Event Thread  - DEBUG:    Value [" +_event.getEventValues()[i]+ "] for column [" +columnNames[i]+ "] is of type [" +Integer.valueOf(type).toString()+ "].",LogHandle.ALL,LogManager.DEBUG);
                        
                        _event.setEventValueType(columnNames[i], Integer.valueOf(type).toString());
                    }

                    switch (type)
                    {
                        case INT_NORMAL :
                            break;

                        case INT_INCR :

                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase(""))
                            {
                                continue;
                            }

                            // "Start" and "Step" values are determined from the cell value, which
                            // has the following format "Start:xxx, Step:xxx".
                            // We convert this to: "Start:xxx, Step:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            intStart = eventParser.getValueStart();
                            intStep = eventParser.getValueStep();
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has Start:[" +intStart+ "], Step:[" +intStep+ "].", LogHandle.ALL,LogManager.DEBUG);
                            
                            step = intStep*LOOP;
                            intValIncr = intStart + step;
                            _event.setEventValue(_event.getEventColumns()[i], "Start:"+intStart+", Step:"+intStep+", Value:"+intValIncr);
                            break;
                        
                        case INT_RAND :

                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase(""))
                            {
                                continue;
                            }

                            // "From" and "To" values are determined from the cell value, which
                            // has the following format "From:xxx, To:xxx"
                            // We convert this to: "From:xxx, To:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            intFrom = eventParser.getValueFrom();
                            intTo = eventParser.getValueTo();
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has From:[" +intFrom+ "], To:[" +intTo+ "].", LogHandle.ALL,LogManager.DEBUG);

                            // Generate a value from "from" to "to"
                            try {
                            	intValRand = randomGenerator.nextInt(intTo-intFrom) + intFrom;
                            } catch (IllegalArgumentException eae) {
                                LogManager.write("Single Event Thread  - MINOR:    Event[" +_event.getID()+ "] failed to generate random number due to negative bound [" +(intTo-intFrom)+ "] Setting to From:[" +intFrom+ "].", LogHandle.ALL,LogManager.MINOR);
                            	charRandValue = charRandFrom;
                            }
                            _event.setEventValue(_event.getEventColumns()[i], "From:"+intFrom+", To:"+intTo+", Value:"+intValRand);
                            break;

                        case INT_TIME :
                            break;

                        case CHAR_NORMAL :
                            break;
                            
                        case CHAR_INCR :

                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase("")) {
                                continue;
                            }

                            // "Start" and "Step" values are determined from the cell value, which
                            // has the following format "Start:xxx, Step:xxx".
                            // We convert this to: "Start:xxx, Step:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            // We also check for any Prefix or Suffix.
                            charIncrValue = 0;
                            charIncrStart = eventParser.getValueStart();
                            charIncrStep = eventParser.getValueStep();
                            String charIncrPref = eventParser.getValuePref();
                            String charIncrSuff = eventParser.getValueSuff();                                                        
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has Start:[" +charIncrStart+ "], Step:[" +charIncrStep+ "], Pref: [" +charIncrPref+ "], Suff: [" +charIncrSuff+ "].", LogHandle.ALL,LogManager.DEBUG);
                            
                            step = charIncrStep*LOOP;
                            charIncrValue = charIncrStart + step;

                            if (charIncrPref != "" && charIncrSuff != "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "Start:"+charIncrStart+", Step:"+charIncrStep+", Pref:" +charIncrPref+ ", Suff:" +charIncrSuff+ ", Value:"+charIncrPref+charIncrValue+charIncrSuff);
                            }
                            else if (charIncrPref == "" && charIncrSuff != "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "Start:"+charIncrStart+", Step:"+charIncrStep+", Suff:" +charIncrSuff+ ", Value:"+charIncrValue+charIncrSuff);
                            }
                            else if (charIncrPref != "" && charIncrSuff == "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "Start:"+charIncrStart+", Step:"+charIncrStep+", Pref:" +charIncrPref+ ", Value:"+charIncrPref+charIncrValue);
                            }
                            else
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "Start:"+charIncrStart+", Step:"+charIncrStep+", Value:"+charIncrValue);
                            }
                            break;
                            
                        case CHAR_RAND :
                        	
                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase("")) {
                                continue;
                            }

                            // "From" and "To" values are determined from the cell value, which
                            // has the following format "From:xxx, To:xxx"
                            // We convert this to: "From:xxx, To:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            // We also check for any Prefix or Suffix.
                            charRandValue = 0;
                            charRandFrom = eventParser.getValueFrom();
                            charRandTo = eventParser.getValueTo();
                            String charRandPref = eventParser.getValuePref();
                            String charRandSuff = eventParser.getValueSuff();                                                        
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has From:[" +charRandFrom+ "], To:[" +charRandTo+ "], Pref: [" +charRandPref+ "], Suff: [" +charRandSuff+ "].", LogHandle.ALL,LogManager.DEBUG);

                            // Generate a value from "from" to "to"
                            try {
                            	charRandValue = randomGenerator.nextInt(charRandTo-charRandFrom) + charRandFrom;
                            } catch (IllegalArgumentException eae) {
                                LogManager.write("Single Event Thread  - MINOR:    Event[" +_event.getID()+ "] failed to generate random number due to negative bound [" +(charRandTo-charRandFrom)+ "] Setting to From:[" +charRandFrom+ "].", LogHandle.ALL,LogManager.MINOR);
                            	charRandValue = charRandFrom;
                            }

                            if (charRandPref != "" && charRandSuff != "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "From:"+charRandFrom+", To:"+charRandTo+", Pref:" +charRandPref+ ", Suff:" +charRandSuff+ ", Value:"+charRandPref+charRandValue+charRandSuff);
                            }
                            else if (charRandPref == "" && charRandSuff != "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "From:"+charRandFrom+", To:"+charRandTo+", Suff:" +charRandSuff+ ", Value:"+charRandValue+charRandSuff);
                            }
                            else if (charRandPref != "" && charRandSuff == "")
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "From:"+charRandFrom+", To:"+charRandTo+", Pref:" +charRandPref+ ", Value:"+charRandPref+charRandValue);
                            }
                            else
                            {
                                _event.setEventValue(_event.getEventColumns()[i], "From:"+charRandFrom+", To:"+charRandTo+", Value:"+charRandValue);
                            }
                            break;

                        case CHAR_TIME :
                            break;
                            
                        case GIS :

                            // If the cell is empty we do nothing
                            if (_event.getEventValues()[i].trim().equalsIgnoreCase("")) {
                                continue;
                            }
                            
                            // "minLat", "maxLat", "minLong" and "maxLong" values are determined from the cell value, which
                            // has the following format "minLat:xxx, maxLat:xxx, minLong:xxx, maxLong:xxx"
                            // We convert this to: "minLat:xxx, maxLat:xxx, minLong:xxx, maxLong:xxx, Value:xxx", where the "Value"
                            // is the calculated value to send.
                            valMinLat = eventParser.getValueMinLat();
                            valMaxLat = eventParser.getValueMaxLat();
                            valMinLong = eventParser.getValueMinLong();
                            valMaxLong = eventParser.getValueMaxLong();
                            LogManager.write("Single Event Thread  - DEBUG:    Event[" +_event.getID()+ "], column[" +columnNames[i]+ "] with cell_value[" +_event.getEventValues()[i]+ "] has minLat:[" +valMinLat+ "], maxLat:[" +valMaxLat+ "], minLong:[" +valMinLong+ "], maxLong:[" +valMaxLong+ "].", LogHandle.ALL,LogManager.DEBUG);

                            offset = 0;
                            tmpLat = 0;
                            tmpLong = 0;
                            randLat = 0;
                            randLong = 0;

                            // Verify the correct sizes
                            if (valMinLat > valMaxLat)
                            {
                                LogManager.write("Single Event Thread  - MINOR:    minLat:[" +valMinLat+ "] cannot be bigger than maxLat:[" +valMaxLat+ "]. Setting world boundary.", LogHandle.ALL,LogManager.MINOR);
                                valMinLat = -90;
                                valMaxLat = 90;
                            }
                            if (valMinLong > valMaxLong)
                            {
                                LogManager.write("Single Event Thread  - MINOR:    minLong:[" +valMinLong+ "] cannot be bigger than maxLong:[" +valMaxLong+ "]. Setting world boundary.", LogHandle.ALL,LogManager.MINOR);
                                valMinLong = -180;
                                valMaxLong = 180;
                            }

                            // Generate a value from "minLat" to "maxLat" and "minLong" and maxLong"
                            if (valMinLat < 0 && valMaxLat < 0)
                            {
                            	offset = valMaxLat - valMinLat;
                                tmpLat = randomGenerator.nextDouble() * offset;
                                randLat = tmpLat + valMinLat;
                            }
                            else if (valMinLat < 0 && valMaxLat > 0)
                            {
                            	offset = valMinLat * (-1) + valMaxLat;
                                tmpLat = randomGenerator.nextDouble() * offset;
                                randLat =  tmpLat + valMinLat;
                            }
                            else if (valMinLat >= 0 && valMaxLat >= 0)
                            {
                            	offset = valMaxLat - valMinLat;
                                tmpLat = randomGenerator.nextDouble() * offset;
                                randLat = tmpLat + valMinLat;
                            }

                            // Generate a value from "minLat" to "maxLat" and "minLong" and maxLong"
                            if (valMinLong < 0 && valMaxLong < 0)
                            {
                            	offset = valMaxLong - valMinLong;
                                tmpLong = randomGenerator.nextDouble() * offset;
                                randLong = tmpLong + valMinLong;
                            }
                            else if (valMinLong < 0 && valMaxLong > 0)
                            {
                            	offset = valMinLong * (-1) + valMaxLong;
                                tmpLong = randomGenerator.nextDouble() * offset;
                                randLong = tmpLong + valMinLong;
                            }
                            else if (valMinLong >= 0 && valMaxLong >= 0)
                            {
                            	offset = valMaxLong - valMinLong;
                                tmpLong = randomGenerator.nextDouble() * offset;
                                randLong = tmpLong + valMinLong;
                            }

                            //Format the output to have only 4 digits after the decimal point
                            DecimalFormat myFormatter = new DecimalFormat("###.####");
                            
                            randLat = Double.parseDouble(myFormatter.format(randLat));
                            randLong = Double.parseDouble(myFormatter.format(randLong));

                            _event.setEventValue(_event.getEventColumns()[i], "minLat:"+valMinLat+", maxLat:"+valMaxLat+", minLong:"+valMinLong+", maxLong:"+valMaxLong+", Value:Lat:"+randLat+" Long:"+randLong);
                            break;

                        default :

                            break;
                    }
                }

                LOOP++;

                // Make the thread sleep
                while (isPaused()) {
                    setActive(false);
                    try{Thread.sleep(500);}catch(Exception e){};
                }
                
                if (super.doRun()) {
                	
                    setActive(true);
                    try{Thread.sleep(poll);}catch(Exception e){};
                    
                    addEvent(_event);
                    LogManager.write("Single Event Thread  - INFO:     Event ID:[" +_event.getID()+ "] appended [" +LOOP+ "] times to the generator queue.", LogHandle.ALL,LogManager.INFO);

                    _stopAt--;
                    if (_stopAt <= 0) {
                        setActive(false);
                        setCompleted(true);
                    }
                    
                } else {
                    setActive(false);
                    _go = false;
                    stopThread();
                }
            }
        }
    }

    public void setActive(boolean active)
    {
        ACTIVE = active;
    }

    public boolean getActive()
    {
        return ACTIVE;
    }

    public void setPauseThread(boolean paused)
    {
        PAUSED = paused;
    }

    public boolean isPaused()
    {
        return PAUSED;
    }

    public void setCompleted(boolean completed)
    {
        COMPLETED = completed;
    }

    public boolean isCompleted()
    {
        return COMPLETED;
    }

    public void incForcedDelay()
    {
        _forcedDelay = _forcedDelay + DELAY;
    }

    public void decForcedDelay()
    {
        if (_forcedDelay >= DELAY)
        {
            _forcedDelay = _forcedDelay - DELAY;
        }
    }

    public int getForcedDelay()
    {
        return _forcedDelay;
    }

    public void setPriority(int priority)
    {
        if (worker != null)
        {
            worker.setPriority(priority);
        }
    }
}
