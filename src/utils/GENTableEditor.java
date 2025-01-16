/******************************************************************
*
* IBM Tivoli Event Generator
*
* IBM Confidential
* OCO Source Materials
*
* 5724-S45
*
* (C) Copyright IBM Corp. 2005
*
* The source code for this program is not published or otherwise
* divested of its trade secrets, irrespective of what has
* been deposited with the U.S. Copyright Office.
*
******************************************************************/
package utils;

import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GENTableEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
 {
    String currentButton;
    JButton button;
    protected static final String RESTART = "Restart";
    protected static final String PAUSE = "Pause";

    public GENTableEditor()
    {
        //Set up the editor (from the table's point of view),
        //which is a button.
        //This button brings up the color chooser dialog,
        //which is the editor from the user's point of view.
        button = new JButton();
        //button.setActionCommand(LABEL);
        button.addActionListener(this);
        button.setBorderPainted(false);
    }

    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (PAUSE.equals(e.getActionCommand()))
        {
            // Pause the thread

            // Change the button label
            button.setText(RESTART);
        }
        else if (RESTART.equals(e.getActionCommand()))
        {
            // Restart the thread

            // Change the button label
            button.setText(PAUSE);
        }
    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue()
    {
        return button.getText();
    }

    //Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        currentButton = value.toString();
        return button;
    }
}

