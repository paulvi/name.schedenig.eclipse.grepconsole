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

import name.schedenig.eclipse.grepconsole.adapters.GroupParameterResolver;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil.IVariableResolver;

import org.eclipse.swt.widgets.Shell;

/**
 * Base class for classes that represent link instances bound to a specific
 * displayed text strnig. Used to execute links and provide tooltip texts.
 * 
 * Among other things, this class provides the logic for replacing parameters
 * in pattern string depending on the matched text. See the PARAM_* constants
 * for available parameters.
 * 
 * @author msched
 */
public abstract class GrepLinkAdapter
{
	/** Prefix for Eclipse variable parameters. */
	public static final String PARAM_ECLIPSE_PREFIX = "eclipse:"; //$NON-NLS-1$
	
	/** Parameter representing the text of the entire matched line. */
	public static final String PARAM_WHOLE_LINE = "line"; //$NON-NLS-1$
	
	/** Parameter representing the text of the entire match. */
	public static final String PARAM_WHOLE_MATCH = "match"; //$NON-NLS-1$
	
	/** Parameter representing the text of the specific capture group to which
	 *  the link is assigned. */
	public static final String PARAM_GROUP_MATCH = "group"; //$NON-NLS-1$
	
	/** Shell. */
	private Shell shell;
	
	/** Link match. */
	private LinkMatch match;

	/** Parameter resolver for pattern strings. */
	private GroupParameterResolver parameterResolver;

	/**
	 * Creates a new instance.
	 * 
	 * @param match Link match.
	 * @param shell Shell.
	 */
	public GrepLinkAdapter(LinkMatch match, Shell shell)
	{
		this.match = match;
		this.shell = shell;
	}

	/**
	 * Returns the link match's match result.
	 * 
	 * @return Match result.
	 */
	public MatchResult getMatchResult()
	{
		return match.getMatchResult();
	}
	
	/**
	 * Returns the link match's link.
	 * 
	 * @return Link.
	 */
	public IGrepLink getLink()
	{
		return match.getLink();
	}
	
	/**
	 * Returns the link match.
	 * 
	 * @return Link match.
	 */
	public LinkMatch getMatch()
	{
		return match;
	}
	
	/**
	 * Opens the link.
	 */
	public abstract void openLink();
	
	/**
	 * Returns the tooltip text to be shown for this link.
	 * 
	 * @return Tooltip text.
	 */
	public abstract String getToolTipText();

	/**
	 * Returns the shell used when opening dialogs.
	 * 
	 * @return Shell.
	 */
	protected Shell getShell()
	{
		return shell;
	}

	/**
	 * Replaces the parameters in the specified pattern using the match and
	 * parameter resolver.
	 * 
	 * @param pattern Pattern string.
	 * 
	 * @return Resolved string.
	 */
	protected String replaceParams(String pattern)
	{
		return GrepConsoleUtil.replaceParams(pattern, getMatch(), getParameterResolver());
	}

	/**
	 * Returns the parameter resolver allowing group names to be used as pattern
	 * parameters. Creates the resolver if necessary.
	 * 
	 * @return Parameter resolver.
	 */
	private IVariableResolver getParameterResolver()
	{
		if(parameterResolver == null)
		{
			parameterResolver = new GroupParameterResolver(match);
		}
		
		return parameterResolver;
	}
	
	/**
	 * Resolves an integer pattern.
	 * 
	 * @param pattern Pattern.
	 * 
	 * @return Resolved integer value. <code>null</code> if no value was
	 * 		specified.
	 */
	protected Integer readOptionalIntPattern(String pattern)
	{
		if(pattern != null)
		{
			pattern = pattern.trim();
		}
		
		if(pattern == null || pattern.length() == 0)
		{
			return null;
		}
		else
		{
			try
			{
				return Integer.parseInt(replaceParams(pattern));
			}
			catch(NumberFormatException ex)
			{
				return null;
			}
		}
	}
}
