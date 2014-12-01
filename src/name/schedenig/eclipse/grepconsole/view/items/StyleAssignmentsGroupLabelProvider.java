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

package name.schedenig.eclipse.grepconsole.view.items;

import java.text.MessageFormat;

import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;

/**
 * Label provider for the group column of the style assignments table. Takes
 * integer numbers as elements. The first (zero) index will be displayed as a
 * "Whole line" label, the remaining indexes will be displayed as "Group n".
 * 
 * @author msched
 */
public class StyleAssignmentsGroupLabelProvider extends ColumnLabelProvider
{
	/** Viewer to which this label provider belongs. Used to partition indexes
	 *  into capture and rewrite groups. */ 
	private ColumnViewer viewer;
	
	/** Number of capture groups for the item. */
	private int captureGroupCount;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer Viewer to which the provider will be bound. 
	 */
	public StyleAssignmentsGroupLabelProvider(ColumnViewer viewer)
	{
		this.viewer = viewer;
	}

	/**
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element)
	{
		Integer index = (Integer) element;
		return getText(index, true);
	}
	
	/**
	 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element)
	{
		Integer index = (Integer) element;
		return getText(index, false);
	}

	/**
	 * Returns the text for the specified index.
	 *  
	 * @param index Group index.
	 * @param useName Whether to use the group name (if assigned).
	 * 
	 * @return Text.
	 */
	protected String getText(int index, boolean useName)
	{
		String name = useName ? getName(index) : null;
		
		if(name != null)
		{
			return name;
		}
		else if(index < captureGroupCount)
		{
			if(index == 0)
			{
				return Messages.StyleAssignmentsGroupLabelProvider_whole_line;
			}
			else
			{
				return MessageFormat.format(Messages.StyleAssignmentsGroupLabelProvider_group_0, index);
			}
		}
		else
		{
			if(index - captureGroupCount == 0)
			{
				return Messages.StyleAssignmentsGroupLabelProvider_rewritten_line;
			}
			else
			{
				return MessageFormat.format(Messages.StyleAssignmentsGroupLabelProvider_rewritten_group, index - captureGroupCount);
			}
		}
	}

	/**
	 * Sets the number of capture groups for the item.
	 * 
	 * @param captureGroupCount Number of capture groups.
	 */
	public void setCaptureGroupCount(int captureGroupCount)
	{
		this.captureGroupCount = captureGroupCount;
	}

	/**
	 * Returns the viewer to which this label provider is assigned.
	 * 
	 * @return Viewer.
	 */
	public ColumnViewer getViewer()
	{
		return viewer;
	}
	
	/**
	 * Returns the link for content element.
	 * 
	 * @param element Element. Will be treated as an integer index to the viewer's
	 * 		input instance, which is assumed to be an array of style instances.
	 * 
	 * @return Link.
	 */
	private String getName(Object element)
	{
		Integer index = (Integer) element;
		GrepGroup[] groups = (GrepGroup[]) getViewer().getInput();
		return groups[index] == null ? null : groups[index].getName();
	}
}
