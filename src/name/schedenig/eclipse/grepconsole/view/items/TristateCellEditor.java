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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor for handling tristate values. Values are toggled when the
 * user clicks on the editor. The toggle order is: <code>null</code>,
 * <code>true</code>, <code>false</code>.
 * 
 * @author msched
 */
public class TristateCellEditor extends CellEditor
{
	/** Current value. */
	private Boolean value;
	
	/**
	 * Creates a new instance.
	 */
	public TristateCellEditor()
	{
		setStyle(SWT.NONE);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent control.
	 */
	public TristateCellEditor(Composite parent)
	{
		this(parent, SWT.NONE);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent control.
	 * @param style Style flags.
	 */
	public TristateCellEditor(Composite parent, int style)
	{
		super(parent, style);
	}

	/**
	 * @see org.eclipse.jface.viewers.CellEditor#activate()
	 */
	@Override
	public void activate()
	{
		if(value == null)
		{
			value = true;
		}
		else if(value == true)
		{
			value = false;
		}
		else
		{
			value = null;
		}
		
		fireApplyEditorValue();
	}

	/**
	 * @see org.eclipse.jface.viewers.CellEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent)
	{
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
	 */
	@Override
	protected Object doGetValue()
	{
		return value == null ? null : value ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
	 */
	@Override
	protected void doSetFocus()
	{
	}

	/**
	 * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
	 */
	@Override
  protected void doSetValue(Object value)
	{
		Assert.isTrue(value == null || value instanceof Boolean);
		this.value = value == null ? null : ((Boolean) value).booleanValue();
	}

	/**
	 * @see org.eclipse.jface.viewers.CellEditor#activate(org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent)
	 */
	@Override
	public void activate(ColumnViewerEditorActivationEvent activationEvent)
	{
		if(activationEvent.eventType != ColumnViewerEditorActivationEvent.TRAVERSAL)
		{
			super.activate(activationEvent);
		}
	}
}
