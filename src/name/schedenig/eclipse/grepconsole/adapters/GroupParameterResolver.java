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

package name.schedenig.eclipse.grepconsole.adapters;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;

import name.schedenig.eclipse.grepconsole.adapters.links.LinkMatch;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;

/**
 * Resolves string pattern parameters referring to named groups in a pattern
 * match.
 * 
 * @author msched
 */
public class GroupParameterResolver implements GrepConsoleUtil.IVariableResolver
{
	/** Link match. */
	private LinkMatch match;
	
	/** Maps group names to match group indexes. */
	private Map<String, Integer> map;

	public GroupParameterResolver(LinkMatch match)
	{
		this.match = match;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil.IVariableResolver#getValue(java.lang.String)
	 */
	@Override
	public String getValue(String name)
	{
		if(name == null)
		{
			return null;
		}
		
		if(map == null)
		{
			buildMap();
		}
		
		Integer index = map.get(name.toLowerCase());
		MatchResult matchResult = match.getMatchResult();
		
		if(index == null || index > matchResult.groupCount())
		{
			return null;
		}

		return matchResult.group(index);
	}

	/**
	 * Creates the group index map.
	 */
	private void buildMap()
	{
		map = new HashMap<String, Integer>();
		GrepExpressionItem item = match.getItem();
		
		for(int i = 0; i < item.getGroups().length; i++)
		{
			GrepGroup group = item.getGroups()[i];
			String name = group.getName();
			
			if(name != null)
			{
				map.put(name.toLowerCase(), i);
			}
		}
	}
}
