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

import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.view.styles.StyleLabelProvider;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the style column of the style assignments table. Takes
 * integer numbers as elements. Provides thumbnails and name labels for the
 * corresponding styles. 
 *  
 * @author msched
 */
public class StyleAssignmentsStyleLabelProvider extends StyleLabelProvider
{
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer Viewer to which the provider will be bound. 
	 */
	public StyleAssignmentsStyleLabelProvider(ColumnViewer viewer)
	{
		super(viewer);
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element)
	{
		return super.getText(getStyle(element));
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.styles.StyleLabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element)
	{
		return super.getImage(getStyle(element));
	}
	
	/**
	 * Returns the style for content element.
	 * 
	 * @param element Element. Will be treated as an integer index to the viewer's
	 * 		input instance, which is assumed to be an array of style instances.
	 * 
	 * @return Style.
	 */
	private GrepStyle getStyle(Object element)
	{
		Integer index = (Integer) element;
		GrepGroup[] groups = (GrepGroup[]) getViewer().getInput();

		return groups[index] == null ? null : groups[index].getStyle();
	}
}
