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

import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;

/**
 * Stores model items related to the whole line or a specific capture group.
 * 
 * @author msched
 */
public class GrepGroup
{	
	/** Optional name. */
	private String name;
	
	/** Optional style. */
	private GrepStyle style;
	
	/** Optional link. */
	private IGrepLink link;
	
	/**
	 * Creates a new instance.
	 */
	public GrepGroup()
	{
	}
	
	/**
	 * Copies an existing instance.
	 * 
	 * @param src Source group.
	 * @param identityCopy Whether to perform a deep copy.
	 */
	public GrepGroup(GrepGroup src, boolean identityCopy)
	{
		name = src.name;
		style = src.style;
		link = identityCopy ? src.link : src.link == null ? null : src.link.copy();
	}

	/**
	 * Copies the instance.
	 * 
	 * @param identityCopy Whether to perform a deep copy.
	 * 
	 * @return Copy.
	 */
	public GrepGroup copy(boolean identityCopy)
	{
		return new GrepGroup(this, identityCopy);
	}

	/**
	 * Returns the name.
	 * 
	 * @return Name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name Name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the style.
	 * 
	 * @return Style.
	 */
	public GrepStyle getStyle()
	{
		return style;
	}
	
	/**
	 * Sets the style.
	 * 
	 * @param style Style.
	 */
	public void setStyle(GrepStyle style)
	{
		this.style = style;
	}
	
	/**
	 * Returns the link.
	 * 
	 * @return Link.
	 */
	public IGrepLink getLink()
	{
		return link;
	}
	
	/**
	 * Sets link.
	 * 
	 * @param link Link.
	 */
	public void setLink(IGrepLink link)
	{
		this.link = link;
	}
}
