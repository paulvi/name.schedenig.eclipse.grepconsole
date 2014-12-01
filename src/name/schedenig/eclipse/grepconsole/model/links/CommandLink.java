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

package name.schedenig.eclipse.grepconsole.model.links;

/**
 * A link for executing a system command. Consists of a command and an optional
 * working directory. Both are patterns.
 * 
 * @author msched
 */
public class CommandLink implements IGrepLink
{
	/** Command to be executed. */
	private String commandPattern;
	
	/** Optional working directory. */
	private String workingDirPattern;
	
	/**
	 * Creates a new instance.
	 */
	public CommandLink()
	{
	}
	
	/**
	 * Clones a command link.
	 * 
	 * @param src Source link.
	 */
	public CommandLink(CommandLink src)
	{
		commandPattern = src.commandPattern;
		workingDirPattern = src.workingDirPattern;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.links.IGrepLink#copy()
	 */
	@Override
	public CommandLink copy()
	{
		return new CommandLink(this);
	}

	/**
	 * Returns the command.
	 * 
	 * @return Command.
	 */
	public String getCommandPattern()
	{
		return commandPattern;
	}

	/**
	 * Sets the command.
	 * 
	 * @param commandPattern Command.
	 */
	public void setCommandPattern(String commandPattern)
	{
		this.commandPattern = commandPattern;
	}

	/**
	 * Returns the working directory.
	 * 
	 * @return Working directory.
	 */
	public String getWorkingDirPattern()
	{
		return workingDirPattern;
	}

	/**
	 * Sets the working directory.
	 * 
	 * @param workingDirPattern Working directory.
	 */
	public void setWorkingDirPattern(String workingDirPattern)
	{
		this.workingDirPattern = workingDirPattern;
	}
}
