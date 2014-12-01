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

import java.util.Map;

import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

/**
 * Abstract base class for editing support implementations that can handle two
 * state and tristate values.
 * 
 * Implementations have to provide methods for getting and setting the state
 * of an element. They may also provide an inheritance map. If an inheritance
 * map is available, tristate values (<code>true</code> or <code>false</code>
 * stored in the map, or no entry in the map to inherit the element's own
 * default value) will be used. Without an inheritance map, only two state
 * values (boolean) directly on the element will be used.
 * 
 * @author msched
 */
public abstract class TristateEditingSupport extends EditingSupport
{
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer to which the new editing support will be bound.
	 */
	public TristateEditingSupport(ColumnViewer viewer)
	{
		super(viewer);
	}

	/**
	 * If an inheritance map is available, treats values as tristate values
	 * (Boolean entries in the map), otherwise as two state (boolean) values
	 * via toggleElementValue().
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void setValue(Object element, Object value)
	{
		Boolean check = (Boolean) value;
		Map<String, Boolean> map = getInheritanceMap();
		AbstractGrepModelElement modelElement = ((AbstractGrepModelElement) element);
		String id = modelElement.getId();
		
		if(map == null)
		{
			toggleElementValue(modelElement);
		}
		else
		{
			if(check == null)
			{
				map.remove(id);
			}
			else if(check)
			{
				map.put(id, true);
			}
			else
			{
				map.put(id, false);
			}
		}

		getViewer().refresh(element);
	}
	
	/**
	 * If an inheritance map is provided, the element's value is read from the 
	 * map as a tristate (Boolean) value. Otherwise, the value is read directly
	 * from the element (two state/boolean).
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
	 */
	@Override
	protected Object getValue(Object element)
	{
		Map<String, Boolean> inheritanceMap = getInheritanceMap();
		Boolean enablement;
		
		if(inheritanceMap == null)
		{
			enablement = getElementValue((AbstractGrepModelElement) element);
		}
		else
		{
			enablement = inheritanceMap.get(((AbstractGrepModelElement) element).getId());
		}
		
		return enablement;
	}
	
	/**
	 * Toggles the boolean value directly on the element.
	 * 
	 * @param element Element.
	 */
	protected abstract void toggleElementValue(AbstractGrepModelElement element);
	
	/**
	 * Reads the boolean value directly from the element.
	 * 
	 * @param element Element.
	 * 
	 * @return Value.
	 */
	protected abstract boolean getElementValue(AbstractGrepModelElement element);

	/**
	 * Provides the inheritance map, if one is available.
	 * 
	 * @return Inheritance map, or <code>null</code>.
	 */
	protected abstract Map<String, Boolean> getInheritanceMap();

	/**
	 * Returns a checkbox editor in two state mode or a tristate editor in
	 * tristate mode.
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
	 */
	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return getInheritanceMap() == null ? new CheckboxCellEditor() : new TristateCellEditor();
	}
	
	/**
	 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
	 */
	@Override
	protected boolean canEdit(Object element)
	{
		return true;
	}
}
