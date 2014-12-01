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

import java.util.Set;
import java.util.UUID;

/**
 * Abstract base class for classes that make up the grep expression item tree.
 * 
 * @author msched
 */
public abstract class AbstractGrepModelElement
{
	/** Unique ID. */
	private String id;
	
	/** Optional human readable name. */
	private String name;
	
	/** Parent group. <code>null</code> for the root element. */
	private GrepExpressionFolder parent;
	
	/** Whether this element is enabled by default. */
	private boolean defaultEnabled;
	
	/** Whether this element is filtered to the Grep View by default. */
	private boolean defaultFilter;
	
	/** Whether this element contributes to the Statistics View by default. */
	private boolean defaultStatistics;
	
	/** Whether notifications for this element are activared by default. */
	private boolean defaultNotifications;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id ID. If this is <code>null</code>, a new ID is generated.
	 * 		Otherwise, it is the responsibility of the caller to make sure that the
	 * 		ID is unique.
	 */
	public AbstractGrepModelElement(String id)
	{
		if(id == null)
		{
			this.id = generateId();
		}
		else
		{
			this.id = id;
		}
		
		defaultEnabled = true;
		defaultFilter = true;
		defaultStatistics = true;
		defaultNotifications = true;
	}

	/**
	 * Creates a new instance by copying the specified source element.
	 * 
	 * @param src Source element.
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 */
	protected AbstractGrepModelElement(AbstractGrepModelElement src, boolean identityCopy)
	{
		copyFrom(src, identityCopy);
	}

	/**
	 * Creates a copy of this element.
	 * 
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 * 
	 * @return Copy.
	 */
	public abstract AbstractGrepModelElement copy(boolean identityCopy);
	
	/**
	 * Copies the contents of this element from the specified source element.
	 * 
	 * @param src Source element.
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 */
	protected void copyFrom(AbstractGrepModelElement src, boolean identityCopy)
	{
		id = identityCopy ? src.id : generateId();
		name = src.name;
		parent = src.parent;
		defaultEnabled = src.defaultEnabled;
		defaultFilter = src.defaultFilter;
		defaultStatistics = src.defaultStatistics;
		defaultNotifications = src.defaultNotifications;
	}
	
	/**
	 * Generates a new unique ID.
	 * 
	 * @return ID.
	 */
	private static String generateId()
	{
		return UUID.randomUUID().toString();
	}

	/**
	 * Returns the ID.
	 * 
	 * @return ID.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Sets the human readable name.
	 * 
	 * @param name Name. May be <code>null</code>.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the human readable name.
	 * 
	 * @return Name. May be <code>null</code>.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns the parent group.
	 * 
	 * @return Parent group. <code>null</code> if this is a root element.
	 */
	public GrepExpressionFolder getParent()
	{
		return parent;
	}
	
	/**
	 * Sets the parent element.
	 * 
	 * @param parent Parent element. May be <code>null</code>.
	 */
	protected void setParent(GrepExpressionFolder parent)
	{
		this.parent = parent;
	}
	
	/**
	 * Returns the root group.
	 * 
	 * @return Root group.
	 */
	public GrepExpressionRootFolder getRoot()
	{
		AbstractGrepModelElement root = this;
		
		while(root != null && !(root instanceof GrepExpressionRootFolder))
		{
			root = root.getParent();
		}
		
		return (GrepExpressionRootFolder) root;
	}
	
	/**
	 * Fills a set of all IDs in the element tree starting with this element.
	 * 
	 * @param ids Set to which the gathered IDs are added. 
	 * 
	 * @return A reference to the specified <code>ids</code> set, for convenience.
	 */
	public Set<String> getAllIds(Set<String> ids)
	{
		ids.add(id);
		return ids;
	}
	
	/**
	 * Starting at this element in the tree, regenerates the IDs for all elements
	 * that have IDs contained in the specified exclusion set.
	 * 
	 * @param excludeIds Set of IDs which should not be used. During execution,
	 * 		this method may add new IDs to this set. 
	 */
	public void rewriteDuplicateIds(Set<String> excludeIds)
	{
		if(excludeIds.contains(id))
		{
			id = generateId();
			excludeIds.add(id);
		}
	}

	/**
	 * Returns whether this element is enabled by default.
	 * 
	 * @return Whether this element is enabled by default.
	 */
	public boolean isDefaultEnabled()
	{
		return defaultEnabled;
	}

	/**
	 * Sets whether this element is enabled by default.
	 * 
	 * @param defaultEnabled Whether this element is enabled by default.
	 */
	public void setDefaultEnabled(boolean defaultEnabled)
	{
		this.defaultEnabled = defaultEnabled;
	}

	/**
	 * Returns whether this element is filtered to the Grep View by default.
	 * 
	 * @return Whether this element is filtered by default.
	 */
	public boolean isDefaultFilter()
	{
		return defaultFilter;
	}

	/**
	 * Sets whether this element is filtered to the Grep View by default.
	 * 
	 * @param defaultFilter Whether this element is filtered by default.
	 */
	public void setDefaultFilter(boolean defaultFilter)
	{
		this.defaultFilter = defaultFilter;
	}
	
	/**
	 * Returns whether this element contributes to the Statistics View by default.
	 * 
	 * @return Whether this element contributes statistics by default.
	 */
	public boolean isDefaultStatistics()
	{
		return defaultStatistics;
	}
	
	/**
	 * Sets whether this element contributes to the Statistics View by default.
	 * 
	 * @param defaultFilter Whether this element contributes statistics by default.
	 */
	public void setDefaultStatistics(boolean defaultStatistics)
	{
		this.defaultStatistics = defaultStatistics;
	}
	
	/**
	 * Returns whether notifications for this element are active by default.
	 * 
	 * @return Whether notifications for this element are active by default.
	 */
	public boolean isDefaultNotifications()
	{
		return defaultNotifications;
	}
	
	/**
	 * Sets whether notifications for this element are active by default.
	 * 
	 * @param defaultNotifications Whether notifications for this element are
	 * 		active by default.
	 */
	public void setDefaultNotifications(boolean defaultNotifications)
	{
		this.defaultNotifications = defaultNotifications;
	}
	
	/**
	 * Fills the specified set with all items in the tree, starting with this
	 * element, that use the specified style.
	 * 
	 * @param style Style.
	 * @param items Set to be filled with items using the style.
	 */
	public abstract void findStyleUses(GrepStyle style,
			Set<GrepExpressionItem> items);

	/**
	 * Re-sets all style references based on their IDs. Call this if style
	 * instances have changed in the root element.  
	 */
	protected abstract void refreshStyles();
}
