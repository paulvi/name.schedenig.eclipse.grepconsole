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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.CommandLink;
import name.schedenig.eclipse.grepconsole.util.RuntimeExecutor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Adapter for command links.
 * 
 * @author msched
 */
public class CommandLinkAdapter extends GrepLinkAdapter
{
	/** Resolved command string. */
	private String command;
	
	/** Resolved working directory, if any. */
	private File workingDir;
	
	/** Tooltip text. */
	private String toolTipText;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param match Link match. Must have a CommandLink assigned.
	 * @param shell Shell.
	 */
	public CommandLinkAdapter(LinkMatch match, Shell shell)
	{
		super(match, shell);
	}

	/**
	 * Calculates the adapter's fields (if it hasn't been initialised before).
	 */
	private void init()
	{
		if(command != null || toolTipText != null)
		{
			return;
		}
		
		CommandLink link = getLink();
		command = replaceParams(link.getCommandPattern());

		String s = replaceParams(link.getWorkingDirPattern());
		
		if(s != null)
		{
			s = s.trim();
			
			if(s.length() == 0)
			{
				s = null;
			}
		}
		
		workingDir = s == null ? null : new File(s);

		toolTipText = command;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#getLink()
	 */
	@Override
	public CommandLink getLink()
	{
		return (CommandLink) super.getLink();
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#openLink()
	 */
	@Override
	public void openLink()
	{
		init();
		
		RuntimeExecutor executor = new RuntimeExecutor(command, workingDir);
		
		try
		{
			executor.run();
		}
		catch(IOException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.CommandLinkAdapter_could_not_execute_command, ex);
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.CommandLinkAdapter_external_command_execution_failed, ex.getLocalizedMessage()));
		}
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#getToolTipText()
	 */
	@Override
	public String getToolTipText()
	{
		init();
		
		return toolTipText;
	}
}
