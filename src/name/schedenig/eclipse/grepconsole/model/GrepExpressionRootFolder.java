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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * A root group element. May contain other elements. Additionally, the root
 * group stores a set of styles used by the items in the tree.
 * 
 * A root group should always be a root element, i.e. not have a parent.
 *  
 * @author msched
 */
public class GrepExpressionRootFolder extends GrepExpressionFolder
{
	/** Map of styles (id/style). */
	private HashMap<String, GrepStyle> styles;
	
	/**
	 * Creates a new instance, generating a new ID. 
	 */
	public GrepExpressionRootFolder()
	{
		super();
		
		styles = new HashMap<String, GrepStyle>();
	}
	
	/**
	 * Creates a new instance by copying the specified source group.
	 * 
	 * @param src Source group.
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 */
	public GrepExpressionRootFolder(GrepExpressionFolder src, boolean identityCopy)
	{
		super(src, identityCopy);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder#copy(boolean)
	 */
	@Override
	public GrepExpressionRootFolder copy(boolean identityCopy)
	{
		return new GrepExpressionRootFolder(this, identityCopy);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder#copyFrom(name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement, boolean)
	 */
	@Override
	public void copyFrom(AbstractGrepModelElement src, boolean identityCopy)
	{
		styles = new HashMap<String, GrepStyle>();
		
		for(GrepStyle style: ((GrepExpressionRootFolder) src).getStyles())
		{
			GrepStyle newStyle = style.copy(true);
			styles.put(newStyle.getId(), newStyle);
		}
			
		super.copyFrom(src, identityCopy);
		
		// Remap all item styles to our styles
		LinkedList<AbstractGrepModelElement> queue = new LinkedList<AbstractGrepModelElement>();
		queue.add(this);
		
		while(!queue.isEmpty())
		{
			AbstractGrepModelElement element = queue.removeFirst();
			
			if(element instanceof GrepExpressionFolder)
			{
				queue.addAll(((GrepExpressionFolder) element).getChildren());
			}
			else if(element instanceof GrepExpressionItem)
			{
				GrepExpressionItem item = (GrepExpressionItem) element;
				GrepGroup[] itemGroups = item.getGroups();
				
				for(int i = 0; i < itemGroups.length; i++)
				{
					GrepStyle srcStyle = itemGroups[i].getStyle();
					itemGroups[i].setStyle(srcStyle == null ? null : getStyle(srcStyle.getId()));
				}
			}
		}
		
		// Regenerate style IDs if required
		if(!identityCopy)
		{
			Collection<GrepStyle> newStyles = styles.values();
			
			for(GrepStyle style: newStyles)
			{
				style.regenerateId();
			}
			
			setStyles(newStyles);
		}
	}
	
	/**
	 * Returns an unmodifiable collection of all styles.
	 * 
	 * @return Styles.
	 */
	public Collection<GrepStyle> getStyles()
	{
		return styles.values();
	}

	/**
	 * Sets a new collection of styles. Style references of all items will be
	 * updated using their ID.
	 * 
	 * @param styles New styles.
	 */
	public void setStyles(Collection<GrepStyle> styles)
	{
		this.styles.clear();
		
		for(GrepStyle style: styles)
		{
			if(style == null)
			{
				continue;
			}
			
			this.styles.put(style.getId(), style);
		}
		
		refreshStyles();
	}

	/**
	 * Returns the style with the specified ID.
	 * 
	 * @param id Style ID.
	 * 
	 * @return Style. <code>null</code> if no matching style is found.
	 */
	public GrepStyle getStyle(String id)
	{
		return styles.get(id);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#isDefaultEnabled()
	 */
	@Override
	public boolean isDefaultEnabled()
	{
		return true;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#isDefaultFilter()
	 */
	@Override
	public boolean isDefaultFilter()
	{
		return true;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#isDefaultStatistics()
	 */
	@Override
	public boolean isDefaultStatistics()
	{
		return true;
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#isDefaultNotifications()
	 */
	@Override
	public boolean isDefaultNotifications()
	{
		return true;
	}
	
	/**
	 * Adds a style.
	 * 
	 * @param style New style.
	 */
	public void addStyle(GrepStyle style)
	{
		styles.put(style.getId(), style);
	}

	/**
	 * Removes a style.
	 * 
	 * @param style Style.
	 */
	public void removeStyle(GrepStyle style)
	{
		styles.remove(style.getId());
	}

	/**
	 * Iterates through all child elements and adds all referenced styles that are
	 * not yet included in the styles map to the map.
	 * 
	 * Call this after new elements with new styles have been added.
	 */
	public void addMissingStyles()
	{
		LinkedList<AbstractGrepModelElement> queue = new LinkedList<AbstractGrepModelElement>();
		queue.add(this);
		
		while(!queue.isEmpty())
		{
			AbstractGrepModelElement element = queue.removeFirst();
			
			if(element instanceof GrepExpressionFolder)
			{
				queue.addAll(((GrepExpressionFolder) element).getChildren());
			}
			else if(element instanceof GrepExpressionItem)
			{
				GrepExpressionItem item = (GrepExpressionItem) element;
				GrepGroup[] itemGroups = item.getGroups();
				
				for(int i = 0; i < itemGroups.length; i++)
				{
					GrepStyle style = itemGroups[i].getStyle();
					
					if(style == null)
					{
						continue;
					}
					
					if(!styles.containsKey(style.getId()))
					{
						styles.put(style.getId(), style);
					}
				}
			}
		}
	}
}
