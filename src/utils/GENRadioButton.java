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

import java.awt.Font;
import javax.swing.*;


public class GENRadioButton extends JRadioButtonMenuItem
{
    public GENRadioButton()
    {
        this("");
    }
    
    public GENRadioButton(String text)
    {
        super(text);
        
        setFont(new Font("Dialog", Font.PLAIN, 12));
    }

	public GENRadioButton(String text, Icon icon)
	{
		super(text,icon);

		setFont(new Font("Dialog", Font.PLAIN, 12));
	}
}