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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Contains a number of sub model elements.
 * 
 * @author msched
 */
public class GrepExpressionFolder extends AbstractGrepModelElement
{
	/** List of child elements. */
	protected List<AbstractGrepModelElement> children;

	/**
	 * Creates a new instance, generating a new ID. 
	 */
	public GrepExpressionFolder()
	{
		this((String) null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id ID. It is the caller's responsibility to ensure the ID is unique.
	 */
	public GrepExpressionFolder(String id)
	{
		super(id);
		
		children = new LinkedList<AbstractGrepModelElement>();
	}

	/**
	 * Creates a new instance by copying the specified source group.
	 * 
	 * @param src Source group.
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 */
	public GrepExpressionFolder(GrepExpressionFolder src, boolean identityCopy)
	{
		super(src, identityCopy);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#copy(boolean)
	 */
	@Override
	public GrepExpressionFolder copy(boolean identityCopy)
	{
		return new GrepExpressionFolder(this, identityCopy);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#copyFrom(name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement, boolean)
	 */
	@Override
	public void copyFrom(AbstractGrepModelElement src, boolean identityCopy)
	{
		super.copyFrom(src, identityCopy);

		children = new LinkedList<AbstractGrepModelElement>();
		
		for(AbstractGrepModelElement child: ((GrepExpressionFolder) src).children)
		{
			add(child.copy(identityCopy));
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getName();
	}

	/**
	 * Adds a new element to the end of the child list.
	 * 
	 * @param element New element.
	 */
	public void add(AbstractGrepModelElement element)
	{
		add(element, -1);
	}

	/**
	 * Adds a new element at the specified index in the child list.
	 * 
	 * @param element New element.
	 * @param index Index. Values smaller than 0 refer to the end of the list.
	 */
	public void add(AbstractGrepModelElement element, int index)
	{
		if(element.getParent() != null)
		{
			element.getParent().remove(element);
		}
		
		if(index < 0)
		{
			index = children.size();
		}
		else if(index >= 0)
		{
			index = Math.min(index, children.size());
		}
		
		children.add(index, element);
		element.setParent(this);
	}

	/**
	 * Removes the specified child.
	 * 
	 * @param element Element to remove.
	 * 
	 * @return <code>true</code> if the element was removed, or <code>false</code>
	 * 		if it was not a child of this group.
	 */
	public boolean remove(AbstractGrepModelElement element)
	{
		if(children.remove(element))
		{
			element.setParent(null);
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns an unmodifiable list of all children.
	 * 
	 * @return Children.
	 */
	public List<AbstractGrepModelElement> getChildren()
	{
		return Collections.unmodifiableList(children);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#getAllIds(java.util.Set)
	 */
	@Override
	public Set<String> getAllIds(Set<String> ids)
	{
		super.getAllIds(ids);
		
		for(AbstractGrepModelElement child: children)
		{
			child.getAllIds(ids);
		}
		
		return ids;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#rewriteDuplicateIds(java.util.Set)
	 */
	@Override
	public void rewriteDuplicateIds(Set<String> excludeIds)
	{
		super.rewriteDuplicateIds(excludeIds);
		
		for(AbstractGrepModelElement child: children)
		{
			child.rewriteDuplicateIds(excludeIds);
		}
	}

	/**
	 * Recursively finds the element with the specified ID among the children of
	 * this group.
	 * 
	 * Note: This group itself is not found by the search.
	 * 
	 * @param id Search ID.
	 * 
	 * @return Element with a matching ID. <code>null</code> if no such element
	 * 		is found.
	 */
	public AbstractGrepModelElement findById(String id)
	{
		for(AbstractGrepModelElement child: children)
		{
			if(id.equals(child.getId()))
			{
				return child;
			}
			else if(child instanceof GrepExpressionFolder)
			{
				AbstractGrepModelElement found = ((GrepExpressionFolder) child).findById(id);
				
				if(found != null)
				{
					return found;
				}
			}
		}
		
		return null;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#findStyleUses(name.schedenig.eclipse.grepconsole.model.GrepStyle, java.util.Set)
	 */
	@Override
	public void findStyleUses(GrepStyle style, Set<GrepExpressionItem> items)
	{
		for(AbstractGrepModelElement child: getChildren())
		{
			child.findStyleUses(style, items);
		}
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#refreshStyles()
	 */
	@Override
	protected void refreshStyles()
	{
		for(AbstractGrepModelElement child: children)
		{
			child.refreshStyles();
		}
	}
}
