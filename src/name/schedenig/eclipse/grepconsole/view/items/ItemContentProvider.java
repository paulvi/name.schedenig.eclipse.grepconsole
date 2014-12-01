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

import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for model elements. Takes a GrepExpressionsWithSelection
 * instance as the input and provides content by iterating through the item
 * tree.
 * 
 * @author msched
 */
public class ItemContentProvider implements ITreeContentProvider
{
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
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement)
	{
		return getChildren(((GrepExpressionsWithSelection) inputElement).getRootFolder());
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement)
	{
		if(parentElement instanceof GrepExpressionFolder)
		{
			return ((GrepExpressionFolder) parentElement).getChildren().toArray();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element)
	{
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element)
	{
		if(element instanceof GrepExpressionFolder)
		{
			return !((GrepExpressionFolder) element).getChildren().isEmpty();
		}
		else
		{
			return false;
		}
	}
}
