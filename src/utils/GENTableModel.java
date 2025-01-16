/******************************************************************
*
* IBM Tivoli Event Generator
*
* IBM Confidential
* OCO Source Materials
*
* 5724-S45
*
* (C) Copyright IBM Corp. 2004
*
* The source code for this program is not published or otherwise
* divested of its trade secrets, irrespective of what has
* been deposited with the U.S. Copyright Office.
*
******************************************************************/
package utils;

import javax.swing.table.*;

/**
 * Created by IntelliJ IDEA.
 * User: Pimmy
 * Date: Feb 10, 2004
 * Time: 3:24:22 PM
 * To change this template use Options | File Templates.
 */

public class GENTableModel extends AbstractTableModel
{
    private Object[][] Data;
    private Object[] Header;
    private int NumRows;

    public GENTableModel()
    {
        super();
//        Column = new boolean[0];
    }

    public GENTableModel(Object columnNames[], int rows)
    {
        //super(columnNames, numRows);
        Header = columnNames;
        NumRows = rows;
        //setColumns(columnNames.length);
    }

    public GENTableModel(Object[][] matrix, Object[] columnNames)
    {
        //super(Data, columnNames);
        Data = matrix;
        Header = columnNames;
        //setColumns(columnNames.length);
    }

    public int getColumnCount()
    {
        return Header.length;
    }

    public int getRowCount()
    {
        return Data.length;
    }

    public String getColumnName(int col)
    {
        return Header[col].toString();
    }

    public Object getValueAt(int row, int col)
    {
        return Data[row][col];
    }

    /* JTable uses this method to determine the default renderer
     * editor for each cell. If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box. */
    public Class getColumnClass(int c)
    {
        return getValueAt(0, c).getClass();
    }

    /* * Don't need to implement this method unless your table is editable. */
    public boolean isCellEditable(int row, int col)
    {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (this.getColumnName(col).equalsIgnoreCase("ID"))//if (col == 1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /* * Don't need to implement this method unless your table's * data can change. */
    public void setValueAt(Object value, int row, int col)
    {
        Data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void updateTable(Object[][] newData, Object[] newColumns)
    {
        Data = newData;
        Header = newColumns;
        fireTableStructureChanged();
    }
}
