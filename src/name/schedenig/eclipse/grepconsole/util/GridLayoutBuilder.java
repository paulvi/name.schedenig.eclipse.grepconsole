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

package name.schedenig.eclipse.grepconsole.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Builder for grid layout instances. Left, right, top and bottom margins are
 * automatically initialised to the correct dialogue margins based on the 
 * current font. Horizontal and vertical margins are set to 0.
 *
 * Calls to setter methods return the builder instance and can therefore be
 * chained.
 * 
 * @author msched
 */
public class GridLayoutBuilder
{
	/** Created layout. */
	private GridLayout layout;
	
	/** Font metrics used for standard margins. */
	private FontMetrics fontMetrics;
	
	/** Control providing the current font. */
	private Composite control;

	/**
	 * Returns the font metrics for the specified control.
	 * 
	 * @param control Control.
	 * 
	 * @return
	 */
	private static FontMetrics getFontMetrics(Control control)
	{
		GC gc = new GC(control);
		
		try
		{
			gc.setFont(control.getFont());
			return gc.getFontMetrics();
		}
		finally
		{
			gc.dispose();
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param control Control for which the layout is created.
	 * @param numColumns Number of columns.
	 * @param makeColumnsEqualWidth Whether to make all columns equally wide.
	 */
	public GridLayoutBuilder(Composite control, int numColumns, boolean makeColumnsEqualWidth)
	{
		this(control, getFontMetrics(control));
		
		setNumColumns(numColumns);
		setMakeColumnsEqualWidth(makeColumnsEqualWidth);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param control Control for which the layout is created.
	 */
	public GridLayoutBuilder(Composite control)
	{
		this(control, getFontMetrics(control));
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param fontMetrics Font metrics to use for default margins.
	 */
	public GridLayoutBuilder(FontMetrics fontMetrics)
	{
		this(null, fontMetrics);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param control Control for which the layout is created.
	 * @param fontMetrics Font metrics to use for default margins.
	 */
	public GridLayoutBuilder(Composite control, FontMetrics fontMetrics)
	{
		this.control = control;
		this.fontMetrics = fontMetrics;
		
		init();
	}

	/**
	 * Initialises the layout and its margins based on the current font.
	 */
	private void init()
	{
		layout = new GridLayout();

		if(fontMetrics != null)
		{
			layout.marginHeight = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_MARGIN);
			layout.verticalSpacing = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_SPACING);
		}
		
		layout.marginLeft = layout.marginRight = layout.marginWidth;
		layout.marginTop = layout.marginBottom = layout.marginHeight;
		layout.marginWidth = layout.marginHeight = 0;
	}
	
	/**
	 * Returns the layout.
	 * 
	 * @return Layout.
	 */
	public GridLayout getLayout()
	{
		return layout;
	}
	
	/**
	 * Applies the layout to the control.
	 */
	public void apply()
	{
		control.setLayout(layout);
	}
	
	/**
	 * Sets the number of columns.
	 * 
	 * @param numColumns Number of columns.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setNumColumns(int numColumns)
	{
		layout.numColumns = numColumns;
		return this;
	}
	
	/**
	 * Sets whether columns are equally wide.
	 * 
	 * @param makeColumnsEqualWidth Whether columns are equally wide.
	 *  
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setMakeColumnsEqualWidth(boolean makeColumnsEqualWidth)
	{
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		return this;
	}
	
	/**
	 * Sets the top and bottom margins.
	 * 
	 * @param marginHeight Margin in pixels.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setMarginHeight(int marginHeight)
	{
		layout.marginTop = layout.marginBottom = marginHeight;
		return this;
	}
	
	/**
	 * Sets the left and right margins.
	 * 
	 * @param marginWidth Margin in pixels.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setMarginWidth(int marginWidth)
	{
		layout.marginLeft = layout.marginRight = marginWidth;
		return this;
	}
	
	/**
	 * Sets the left margin.
	 * 
	 * @param marginLeft Margin in pixels.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setMarginLeft(int marginLeft)
	{
		layout.marginLeft = marginLeft;
		return this;
	}
	
	/**
	 * Sets the top margin.
	 * 
	 * @param marginTop Margin in pixels.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setMarginTop(int marginTop)
	{
		layout.marginTop = marginTop;
		return this;
	}
	
	/**
	 * Sets the right margin.
	 * 
	 * @param marginRight Margin in pixels.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setMarginRight(int marginRight)
	{
		layout.marginRight = marginRight;
		return this;
	}
	
	/**
	 * Sets the bottom margin.
	 * 
	 * @param marginBottom Margin in pixels.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setMarginBottom(int marginBottom)
	{
		layout.marginBottom = marginBottom;
		return this;
	}
	
	/**
	 * Sets the horizontal spacing.
	 * 
	 * @param horizontalSpacing Spacing in pixels.
	 * 
	 * @return
	 */
	public GridLayoutBuilder setHorizontalSpacing(int horizontalSpacing)
	{
		layout.horizontalSpacing = horizontalSpacing;
		return this;
	}
	
	/**
	 * Sets the vertical spacing.
	 * 
	 * @param verticalSpacing Spacing in pixels.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setVerticalSpacing(int verticalSpacing)
	{
		layout.verticalSpacing = verticalSpacing;
		return this;
	}

	/**
	 * Sets all margins.
	 * 
	 * @param margins Margin in pixels.
	 * 
	 * @return This builder instance.
	 */
	public GridLayoutBuilder setMargins(int margins)
	{
		return setMarginWidth(margins).setMarginHeight(margins);
	}
}
