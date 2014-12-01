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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Base class for editing support which reacts on double clicks.
 * 
 * @author msched
 */
public abstract class DoubleClickEditingSupport extends EditingSupport
{
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer to which the editing support will be bound.
	 */
	public DoubleClickEditingSupport(ColumnViewer viewer)
	{
		super(viewer);
	}

	/**
	 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void setValue(Object element, Object value)
	{
	}
	
	/**
	 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
	 */
	@Override
	protected Object getValue(Object element)
	{
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
	 */
	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return new CellEditor()
		{
			@Override
			protected Control createControl(Composite parent)
			{
				return null;
			}

			@Override
			protected Object doGetValue()
			{
				return null;
			}

			@Override
			protected void doSetFocus()
			{
			}

			@Override
			protected void doSetValue(Object value)
			{
				doEdit(value);
			}
		};
	}

	/**
	 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
	 */
	@Override
	protected boolean canEdit(Object element)
	{
		return true;
	}

	/**
	 * Called when the editor is invoked.
	 * 
	 * @param value Value to be edited.
	 */
	protected abstract void doEdit(Object value);
}
