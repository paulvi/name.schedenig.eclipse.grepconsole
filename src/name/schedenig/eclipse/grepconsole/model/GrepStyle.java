/*******************************************************************************
 * Copyright (c) 2008 - 2014 Marian Schedenig
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marian Schedenig - initial API and implementation
 *******************************************************************************/

package name.schedenig.eclipse.grepconsole.model;

import java.util.UUID;

import org.eclipse.swt.graphics.RGB;

/**
 * Defines a text style which can be applied to an entire line or a capture
 * group.
 * 
 * @author msched
 */
public class GrepStyle
{
	/** Style ID. Must be unique among all styles belonging to the same root
	 *  group. */
	private String id;

	/** Human readable name. */
	private String name;
	
	/** Foreground colour. */
	private RGB foreground;
	
	/** Background colour. */
	private RGB background;
	
	/** Whether to use a bold font. */
	private boolean bold;
	
	/** Whether to use an italic font. */
	private boolean italic;
	
	/** Whether to underline. */
	private boolean underline;
	
	/** Underline colour. When none is set, the text colour is used. */
	private RGB underlineColor;
	
	/** Whether to strikeout. */
	private boolean strikeout;

	/** Strikeout colour. When none is set, the text colour is used. */
	private RGB strikeoutColor;
	
	/** Whether to draw a border. */
	private boolean border;
	
	/** Border colour. When none is set, the text colour is used. */
	private RGB borderColor;

	/**
	 * Creates a new instance, generating a new ID.
	 */
	public GrepStyle()
	{
		this((String) null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id ID. It is the caller's responsibility to ensure the ID is unique.
	 */
	public GrepStyle(String id)
	{
		if(id == null)
		{
			this.id = UUID.randomUUID().toString();
		}
		else
		{
			this.id = id;
		}
	}

	/**
	 * Creates a new instance by copying the specified source style.
	 * 
	 * @param src Source style.
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 */
	private GrepStyle(GrepStyle src, boolean identityCopy)
	{
		if(!identityCopy)
		{
			this.id = UUID.randomUUID().toString();
		}
		else
		{
			this.id = src.id;
		}
		
		this.name = src.name;
		this.foreground = src.foreground;
		this.background = src.background;
		this.bold = src.bold;
		this.italic = src.italic;
		this.underline = src.underline;
		this.underlineColor = src.underlineColor;
		this.strikeout = src.strikeout;
		this.strikeoutColor = src.strikeoutColor;
		this.border = src.border;
		this.borderColor = src.borderColor;
	}

	/**
	 * Creates a copy of this style.
	 * 
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 * 
	 * @return Copy.
	 */
	public GrepStyle copy(boolean identityCopy)
	{
		return new GrepStyle(this, identityCopy);
	}

	/**
	 * Calculates a new ID.
	 */
	public void regenerateId()
	{
		this.id = UUID.randomUUID().toString();
	}

	/**
	 * Returns the ID.
	 * 
	 * @return ID.
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Returns the human readable name.
	 * 
	 * @return Name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the human readable name.
	 * 
	 * @param name Name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the foreground colour.
	 * 
	 * @return Foreground colour.
	 */
	public RGB getForeground()
	{
		return foreground;
	}

	/**
	 * Sets the foreground colour.
	 * 
	 * @param foreground Foreground colour.
	 */
	public void setForeground(RGB foreground)
	{
		this.foreground = foreground;
	}

	/**
	 * Returns the background colour.
	 * 
	 * @return Background colour.
	 */
	public RGB getBackground()
	{
		return background;
	}

	/**
	 * Sets the background colour.
	 * 
	 * @param background Foreground colour.
	 */
	public void setBackground(RGB background)
	{
		this.background = background;
	}

	/**
	 * Returns whether to use a bold font.
	 * 
	 * @return Whether to use a bold font.
	 */
	public boolean isBold()
	{
		return bold;
	}

	/**
	 * Sets whether to use a bold font.
	 * 
	 * @param bold Whether to use a bold font.
	 */
	public void setBold(boolean bold)
	{
		this.bold = bold;
	}

	/**
	 * Returns whether to use an italic font.
	 * 
	 * @return Whether to use an italic font.
	 */
	public boolean isItalic()
	{
		return italic;
	}

	/**
	 * Sets whether to use an italic font.
	 * 
	 * @param italic Whether to use an italic font.
	 */
	public void setItalic(boolean italic)
	{
		this.italic = italic;
	}

	/**
	 * Returns whether to underline.
	 * 
	 * @return Whether to underline.
	 */
	public boolean isUnderline()
	{
		return underline;
	}
	
	/**
	 * Sets whether to underline.
	 * 
	 * @param underline Whether to underline.
	 */
	public void setUnderline(boolean underline)
	{
		this.underline = underline;
	}

	/**
	 * Returns the underline colour. When none is set, the text colour is used.
	 * 
	 * @return Underline colour.
	 */
	public RGB getUnderlineColor()
	{
		return underlineColor;
	}

	/**
	 * Sets the underline colour. When none is set, the text colour is used.
	 * 
	 * @param underlineColour Under line colour.
	 */
	public void setUnderlineColor(RGB underlineColor)
	{
		this.underlineColor = underlineColor;
	}

	/**
	 * Returns whether to strikeout.
	 * 
	 * @return Whether to strikeout.
	 */
	public boolean isStrikeout()
	{
		return strikeout;
	}

	/**
	 * Sets whether to strikeout.
	 * 
	 * @param strikeout Whether to strikeout.
	 */
	public void setStrikeout(boolean strikeout)
	{
		this.strikeout = strikeout;
	}

	/**
	 * Returns the strikeout colour. When none is set, the text colour is used.
	 * 
	 * @return Strikeout colour.
	 */
	public RGB getStrikeoutColor()
	{
		return strikeoutColor;
	}

	/**
	 * Sets the strikeout colour. When none is set, the text colour is used. 
	 * 
	 * @param strikeoutColor Strikeout colour.
	 */
	public void setStrikeoutColor(RGB strikeoutColor)
	{
		this.strikeoutColor = strikeoutColor;
	}

	/**
	 * Returns whether to draw a border.
	 * 
	 * @return Whether to draw a border.
	 */
	public boolean isBorder()
	{
		return border;
	}

	/**
	 * Sets whether to draw a border. 
	 * 
	 * @param border Whether to draw a border.
	 */
	public void setBorder(boolean border)
	{
		this.border = border;
	}

	/**
	 * Returns the border colour. When none is set, the text colour is used.
	 * 
	 * @return Border colour.
	 */
	public RGB getBorderColor()
	{
		return borderColor;
	}

	/**
	 * Sets the border colour. When none is set, the text colour is used. 
	 * 
	 * @param borderColor Border colour.
	 */
	public void setBorderColor(RGB borderColor)
	{
		this.borderColor = borderColor;
	}
}
