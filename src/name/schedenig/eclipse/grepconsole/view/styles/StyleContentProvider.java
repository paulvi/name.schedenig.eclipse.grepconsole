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

package name.schedenig.eclipse.grepconsole.view.styles;

import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for styles.
 * 
 * Expects a GrepExpressionRootFolder instance as the input.
 * 
 * @author msched
 */
public class StyleContentProvider implements IStructuredContentProvider
{
	/** Null element, used to display a "no style" option. The
	 *  <code>includeNull</code> flag controls whether the null element is
	 *  listed. */
	public static final Object NULL_ELEMENT = new Object();
	
	/** Whether to list the null element. */
	private boolean includeNull;

	/**
	 * Creates a new instance.
	 * 
	 * @param includeNull Whether to list the null element.
	 */
	public StyleContentProvider(boolean includeNull)
	{
		this.includeNull = includeNull;
	}
	
	/**
	 * Sets whether to list the null element.
	 * 
	 * @param includeNull Whether to list the null element.
	 */
	public void setIncludeNull(boolean includeNull)
	{
		this.includeNull = includeNull;
	}
	
	/**
	 * Returns whether the null element is listed.
	 * 
	 * @return Whether the null element is listed.
	 */
	public boolean isIncludeNull()
	{
		return includeNull;
	}
	
	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose()
	{
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement)
	{
		GrepExpressionRootFolder root = (GrepExpressionRootFolder) inputElement;
		Object[] elementArray = root.getStyles().toArray();
		
		if(includeNull)
		{
			Object[] extendedArray = new Object[elementArray.length + 1];
			extendedArray[0] = NULL_ELEMENT;
			System.arraycopy(elementArray, 0, extendedArray, 1, elementArray.length);
			
			return extendedArray;
		}
		else
		{
			return elementArray;
		}
	}
}
