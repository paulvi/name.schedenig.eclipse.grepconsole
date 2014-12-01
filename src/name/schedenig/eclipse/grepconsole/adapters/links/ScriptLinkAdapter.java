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

import java.text.MessageFormat;
import java.util.regex.MatchResult;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.ScriptLink;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Adapter for script links.
 * 
 * @author msched
 */
public class ScriptLinkAdapter extends GrepLinkAdapter
{
	/** Script parameter containing the match result instance. */
  public static final String PARAM_MATCH_RESULT = "matchResult"; //$NON-NLS-1$
  
  /** Script parameter containing an array of all capture group strings. */
  public static final String PARAM_GROUPS = "groups"; //$NON-NLS-1$
  
  /** Script parameter containing the whole text of the matched line. */
  public static final String PARAM_WHOLE_LINE = "wholeLine"; //$NON-NLS-1$
  
  /** Script parameter containing the index of the capture group associated
   *  with the link. */
  public static final String PARAM_MATCHED_GROUP = "matchedGroup"; //$NON-NLS-1$
  
	/**
	 * Creates a new instance.
	 * 
	 * @param match Link match. Must have a CommandLink assigned.
	 * @param shell Shell.
	 */
	public ScriptLinkAdapter(LinkMatch match, Shell shell)
	{
		super(match, shell);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#getLink()
	 */
	@Override
	public ScriptLink getLink()
	{
		return (ScriptLink) super.getLink();
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#openLink()
	 */
	@Override
	public void openLink()
	{
		ScriptLink link = getLink();
		MatchResult matchResult = getMatchResult();
		
		ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName(link.getLanguage());

    if(engine == null)
    {
			Activator.getDefault().log(IStatus.ERROR, MessageFormat.format(Messages.ScriptLinkAdapter_unknown_script_language, link.getLanguage()), null);
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.ScriptLinkAdapter_unknown_script_language, link.getLanguage()));
    }
    
    String[] groups = new String[matchResult.groupCount() + 1];
    
    for(int i = 0; i < groups.length; i++)
    {
    	groups[i] = matchResult.group(i);
    }
    
    engine.put(PARAM_MATCH_RESULT, matchResult);
    engine.put(PARAM_GROUPS, groups);
    engine.put(PARAM_WHOLE_LINE, getMatch().getWholeLine());
    engine.put(PARAM_MATCHED_GROUP, getMatch().getGroup());
    
    try
		{
			engine.eval(link.getCode());
		}
		catch(ScriptException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.ScriptLinkAdapter_could_not_execute_script, ex);
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.ScriptLinkAdapter_script_execution_failed, ex.getLocalizedMessage()));
		}
   }

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#getToolTipText()
	 */
	@Override
	public String getToolTipText()
	{
		return MessageFormat.format(Messages.ScriptLinkAdapter_language_script, getLink().getLanguage());
	}
}
