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

package name.schedenig.eclipse.grepconsole.util;

import java.io.File;
import java.io.IOException;

/**
 * Executes a system command.
 * 
 * @author msched
 */
public class RuntimeExecutor
{
	/** System command. */
	private String cmd;
	
	/** Optional working directory. */
	private File workingDir;

	/**
	 * Creates a new instance.
	 *
	 * If the optional working directory is not specified, the command is run in
	 * the current working directory.
	 *
	 * @param cmd System command.
	 * @param workingDir Working directory.
	 */
	public RuntimeExecutor(String cmd, File workingDir)
	{
		this.cmd = cmd;
		this.workingDir = workingDir;
	}
	
	/**
	 * Executes the command (asynchronously).
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void run() throws IOException
	{
		Runtime runtime = Runtime.getRuntime();
		
		Process p = runtime.exec(cmd, null, workingDir);
		
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
		StreamGobbler outGobbler = new StreamGobbler(p.getInputStream());
		
		errorGobbler.start();
		outGobbler.start();
	}
}

