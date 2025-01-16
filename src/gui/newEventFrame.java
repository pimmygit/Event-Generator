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
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

import utils.GENOptionPane;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Jan 28, 2005
 * Time: 8:55:26 AM
 * To change this template use Options | File Templates.
 */
public class newEventFrame extends JDialog
{
	private static final long serialVersionUID = 1L;

	private Frame frame;
    public newEventFrame eventFrame;
    public newColumnFrame colFrame;
    private JTable newEvent;
    private EventTable eventTable;
    public CustTableModel tModel;

    public newEventFrame(Frame frm, EventTable eventList, String title)
    {
        super(frm, title);
        eventFrame = this;

        eventTable = eventList;
        frame = frm;
        frame.setEnabled(false);
        Container contentPane = getContentPane();
        contentPane.add(emptyPanel(), BorderLayout.NORTH);
        contentPane.add(fieldsPanel(), BorderLayout.CENTER);
        pack();

        int w = 300;
        int h = 250;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        eventFrame.setBounds((d.width-w)/2, (d.height-h)/2, w, h);
        eventFrame.setSize(w, h);
        eventFrame.setResizable(false);
        eventFrame.setVisible(true);

        WindowListener exitListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.setEnabled(true);
                eventFrame = null;
                dispose();
            }
        };
        eventFrame.addWindowListener(exitListener);

        LogManager.write("Add Event Frame      - Ready.",LogHandle.ALL,LogManager.DEBUG);
    }

    public void newEventFrame(Component cmp, EventTable eventList, String title)
    {
        if (eventFrame == null)
        {
            eventFrame = new newEventFrame(JOptionPane.getFrameForComponent(cmp), eventList, title);

            int w = 300;
            int h = 250;
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            eventFrame.setBounds((d.width-w)/2, (d.height-h)/2, w, h);
            eventFrame.setSize(w, h);
            eventFrame.setResizable(false);
            eventFrame.setVisible(true);

            WindowListener exitListener = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    frame.setEnabled(true);
                    eventFrame = null;
                    dispose();
                }
            };
            eventFrame.addWindowListener(exitListener);

        }
    }

    private JPanel emptyPanel()
    {
        JPanel space = new JPanel();
        space.setLayout(new BoxLayout(space, BoxLayout.X_AXIS));

        space.setMinimumSize(new Dimension(270, 2));
        space.setPreferredSize(new Dimension(290, 5));
        space.setMaximumSize(new Dimension(290,5));

        return space;
    }

    private JPanel fieldsPanel()
    {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setMinimumSize(new Dimension(280, 230));
        fieldsPanel.setPreferredSize(new Dimension(290, 240));
        fieldsPanel.setMaximumSize(new Dimension(290, 240));

        JTable newEventTable = createNewEventTable();

        JScrollPane eventList = new JScrollPane(newEventTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        eventList.setMinimumSize(new Dimension(270, 180));
        eventList.setPreferredSize(new Dimension(280, 182));
        eventList.setMaximumSize(new Dimension(280, 182));

        fieldsPanel.add(eventList);
        fieldsPanel.add(buttonPanel());

        return fieldsPanel;
    }

    public class CustTableModel extends DefaultTableModel
    {
		private static final long serialVersionUID = 1L;
		
		public CustTableModel(Object[] o, int s)
        {
            super(o, s);
        }
        public boolean isCellEditable(int row, int column)
        {
            if(column == 0 || row == 0)
                return false;
            return true;
        }
    }

    private JTable createNewEventTable()
    {
        int tableColumns;

        tableColumns = eventTable.eventTableModel.getColumnCount();
        LogManager.write("Add Event Frame      - Columns in Event Table - [" +tableColumns+ "].",LogHandle.ALL,LogManager.DEBUG);
        Object[] tCls = {"Property", "Value"};
        tModel = new CustTableModel(tCls, 0);

        newEvent = new JTable(tModel);
        newEvent.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        newEvent.setBorder(BorderFactory.createLineBorder(Color.black));
        newEvent.setColumnSelectionAllowed(false);
        newEvent.setRowSelectionAllowed(false);
        newEvent.setDragEnabled(false);

        //newEvent.setMinimumSize(new Dimension(350, 162));
        //newEvent.setPreferredSize(new Dimension(400, 163));
        //newEvent.setMaximumSize(new Dimension(400, 350));

        newEvent.getColumnModel().getColumn(0).setMaxWidth(80);
        newEvent.getColumnModel().getColumn(0).setMinWidth(80);
        newEvent.getColumnModel().getColumn(0).setWidth(80);

        newEvent.getColumnModel().getColumn(1).setMaxWidth(400);
        newEvent.getColumnModel().getColumn(1).setMinWidth(130);
        newEvent.getColumnModel().getColumn(1).setWidth(300);


        for (int i=0; i<tableColumns; i++)
        {
            if (eventTable.eventTableModel.getColumnName(i).trim() != "")
            {
                if (eventTable.eventTableModel.getColumnName(i).trim().matches("_ID"))
                {

                    Object[] prop = {eventTable.eventTableModel.getColumnName(i), new Integer(eventTable.eventTableModel.getRowCount())};
                    tModel.addRow(prop);
                }
                else if (eventTable.eventTableModel.getColumnName(i).trim().matches("_Act"))
                {
                    // The value is boolean, so we put a tickbox in the cell.
                    Object[] prop = {eventTable.eventTableModel.getColumnName(i), new Boolean("false")};
                    tModel.addRow(prop);
                }
                else if (eventTable.eventTableModel.getColumnName(i).trim().matches("_Poll"))
                {
                    // The value is integer, so we put digit in the cell.
                    Object[] prop = {eventTable.eventTableModel.getColumnName(i), "60"}; //new Integer(60)};
                    tModel.addRow(prop);
                }
                else if (eventTable.eventTableModel.getColumnName(i).trim().matches("_Stop"))
                {
                    // The value is integer, so we put digit in the cell.
                    Object[] prop = {eventTable.eventTableModel.getColumnName(i), "00"}; //new Integer(0)};
                    tModel.addRow(prop);
                }
                else
                {
                    Object[] prop = {eventTable.eventTableModel.getColumnName(i), ""};
                    tModel.addRow(prop);
                }
            }
        }
        //LogManager.write("Add Event Frame      - \"Event List\" field created with [" +tableColumns+ "] elements.",LogHandle.ALL,LogManager.DEBUG);

        return newEvent;
    }

    private JPanel buttonPanel()
    {
        JPanel buttons = new JPanel();
        buttons.setMinimumSize(new Dimension(200, 22));
        buttons.setPreferredSize(new Dimension(290, 30));
        buttons.setMaximumSize(new Dimension(290,30));

        // Create button and its action for adding an event
        Action Col = new AbstractAction("Add Col"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                colFrame = new newColumnFrame(frame, eventTable, "Add column ");
                colFrame.newColumnFrame(frame, eventTable, "Add column ", tModel);
            }
        };
        JButton colButton = new JButton(Col);
        colButton.setFont(new Font("Dialog", Font.BOLD, 11));
        colButton.setBorder(BorderFactory.createRaisedBevelBorder());
        colButton.setPreferredSize(new Dimension(55, 22));

        // Create button and its action for adding an event
        Action Add = new AbstractAction("Add"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    Object[] tempEvent = new Object[newEvent.getRowCount()];
                    for (int i=0; i<newEvent.getRowCount(); i++)
                    {
                        tempEvent[i] = newEvent.getValueAt(i, 1);
                    }

                    if (eventTable.appendRow(tempEvent))
                    {
                        LogManager.write("Add Event Frame      - Event added to the Event Table.",LogHandle.ALL,LogManager.DEBUG);
                    }
                    else
                    {
                        LogManager.write("Add Event Frame      - Event  FAILED be added to the Event Table.",LogHandle.ALL,LogManager.MAJOR);
                    }
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "Failed to add new row to the Event Table. ", "  Add Row  ", GENOptionPane.ERROR_MESSAGE);
                }
                frame.setEnabled(true);
                eventFrame = null;
                dispose();
            }
        };
        JButton addButton = new JButton(Add);
        addButton.setFont(new Font("Dialog", Font.BOLD, 11));
        addButton.setBorder(BorderFactory.createRaisedBevelBorder());
        addButton.setPreferredSize(new Dimension(55, 22));

        // Create button and its action for deletin an event
        Action Cancel = new AbstractAction("Cancel"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                frame.setEnabled(true);
                eventFrame = null;
                dispose();
            }
        };
        JButton cancelButton = new JButton(Cancel);
        cancelButton.setFont(new Font("Dialog", Font.BOLD, 11));
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        cancelButton.setPreferredSize(new Dimension(60, 22));

        buttons.add(colButton, BorderLayout.WEST);
        buttons.add(Box.createRigidArea(new Dimension(60,10)));
        buttons.add(addButton, BorderLayout.WEST);
        buttons.add(Box.createRigidArea(new Dimension(5,10)));
        buttons.add(cancelButton, BorderLayout.WEST);

        LogManager.write("Add Event Frame      - \"Buttons\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return buttons;
    }
}
