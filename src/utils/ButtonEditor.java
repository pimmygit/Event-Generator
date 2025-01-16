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

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Mar 17, 2005
 * Time: 3:28:05 PM
 * To change this template use Options | File Templates.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * @version 1.0 11/09/98
 */
public class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String    label = "Restart";
    private boolean   isPushed;

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        button.setSize(50, 18);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showMessageDialog(button ,label + ": Ouch!");

                fireEditingStopped();
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
   {
        if (isSelected)
        {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        }
        else
        {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }

        //label = (value == null) ? "" : value.toString();
        if (label.equalsIgnoreCase("Restart"))
        {
            label = "Pause";
        }
        else if (label.equalsIgnoreCase("Pause"))
        {
            label = "Restart";
        }
        else
        {
            label = "Start";
        }

        button.setText( label );
        button.setFont(new Font("Dialog", Font.BOLD, 8));
        isPushed = true;
        return button;
    }

    public Object getCellEditorValue() {
        if (isPushed)
        {
            //JOptionPane.showMessageDialog(button ,label + ": Ouch!");
        }
        isPushed = false;

        return new String( label ) ;
    }

    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
