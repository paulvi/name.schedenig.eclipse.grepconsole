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

package name.schedenig.eclipse.grepconsole.adapters.links;

import java.util.regex.MatchResult;

import org.eclipse.core.resources.IProject;

import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;

/**
 * Stores match data for a single link match.
 * 
 * @author msched
 */
public class LinkMatch
{
	/** Grep expression item. */
	private GrepExpressionItem item;
	
	/** Link. */
	private IGrepLink link;
	
	/** Grep match result. */
	private MatchResult matchResult;
	
	/** Start index of the match. */
	private int matchStart;
	
	/** Whole line text. */
	private String wholeLine;
	
	/** Index of the capture group to which the link belongs. */
	private int group;

	/** Optional Eclipse project. */
	private IProject project;

	/**
	 * Creates a new instance.
	 * 
	 * @param item Grep expression item.
	 * @param link Link.
	 * @param matchResult Grep match result.
	 * @param matchStart Start index of the match.
	 * @param wholeLine Whole line text.
	 * @param group Index of the capture group to which the link belongs.
	 * @param project Optional Eclipse project.
	 */
	public LinkMatch(GrepExpressionItem item, IGrepLink link, 
			MatchResult matchResult, int matchStart, String wholeLine, int group, 
			IProject project)
	{
		this.item = item;
		this.link = link;
		this.matchResult = matchResult;
		this.matchStart = matchStart;
		this.wholeLine = wholeLine;
		this.group = group;
		this.project = project;
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
	 * Returns the match result.
	 * 
	 * @return Match result.
	 */
	public MatchResult getMatchResult()
	{
		return matchResult;
	}

	/**
	 * Returns the match start index.
	 * 
	 * @return Start index.
	 */
	public int getMatchStart()
	{
		return matchStart;
	}
	
	/**
	 * Returns the full line text.
	 * 
	 * @return Full line text.
	 */
	public String getWholeLine()
	{
		return wholeLine;
	}
	
	/**
	 * Returns the capture group index.
	 * 
	 * @return Capture group index.
	 */
	public int getGroup()
	{
		return group;
	}

	/**
	 * Returns the Eclipse project.
	 * 
	 * @return Project. May be <code>null</code>.
	 */
	public IProject getProject()
	{
		return project;
	}
	
	/**
	 * Returns the grep expression item.
	 * 
	 * @return Item.
	 */
	public GrepExpressionItem getItem()
	{
		return item;
	}
}
