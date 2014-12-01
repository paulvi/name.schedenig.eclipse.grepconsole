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

package name.schedenig.eclipse.grepconsole.model.xml;

/**
 * Exception thrown by the XmlHandler class.
 * 
 * @author msched
 */
public class XmlHandlerException extends Exception
{
	/** Serialisation ID. */
	private static final long serialVersionUID = 3046025902325348626L;

	/**
	 * Creates a new instance without a nested exception.
	 * 
	 * @param msg Error message.
	 */
	public XmlHandlerException(String msg)
	{
		super(msg);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param cause Nested exception.
	 */
	public XmlHandlerException(Exception cause)
	{
		super(cause);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param msg Error message.
	 * @param cause Nested exception.
	 */
	public XmlHandlerException(String msg, Exception cause)
	{
		super(msg, cause);
	}
}
