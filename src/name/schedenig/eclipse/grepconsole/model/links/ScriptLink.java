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
 * A link for executing a script via the Java Scripting API.
 * 
 * @author msched
 */
public class ScriptLink implements IGrepLink
{
	/** Script language. */
	private String language;
	
	/** Script code. */
	private String code;
	
	/**
	 * Creates a new instance.
	 */
	public ScriptLink()
	{
	}
	
	/**
	 * Copies a script link.
	 * 
	 * @param src Source link.
	 */
	public ScriptLink(ScriptLink src)
	{
		language = src.language;
		code = src.code;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.links.IGrepLink#copy()
	 */
	@Override
	public ScriptLink copy()
	{
		return new ScriptLink(this);
	}

	/**
	 * Returns the script language.
	 * 
	 * @return Script language.
	 */
	public String getLanguage()
	{
		return language;
	}
	
	/**
	 * Sets the script language.
	 * 
	 * @param language Script language.
	 */
	public void setLanguage(String language)
	{
		this.language = language;
	}
	
	/**
	 * Returns the script code.
	 * 
	 * @return Script code.
	 */
	public String getCode()
	{
		return code;
	}
	
	/**
	 * Sets the script code.
	 * 
	 * @param code Script code.
	 */
	public void setCode(String code)
	{
		this.code = code;
	}
}
