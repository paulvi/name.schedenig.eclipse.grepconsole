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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IStatus;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;

/**
 * A thread which consumes all data from an input stream as soon as it is
 * available. The thread terminates when the stream is closed. 
 * 
 * @author msched
 */
public class StreamGobbler extends Thread
{
	/** Input stream. */
	private InputStream in;

	/**
	 * Creates a new instance.
	 * 
	 * @param in Input stream to be consumed.
	 */
	public StreamGobbler(InputStream in)
	{
		this.in = in;
	}
	
	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			for(;;)
			{
				String s = reader.readLine();
				
				if(s == null)
				{
					break;
				}
			}
		}
		catch(IOException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.StreamGobbler_failed_to_read_external_process_output, ex);
		}
	}
}
