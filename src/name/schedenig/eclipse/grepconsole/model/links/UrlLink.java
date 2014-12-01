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
 * A link for opening a URL, either in an internal browser or via whatever
 * system application is registered for the URL.
 * 
 * @author msched
 */
public class UrlLink implements IGrepLink
{
	/** URL. */
	private String urlPattern;
	
	/** Whether to open the URL in an external application. */
	private boolean external;
	
	/**
	 * Creates a new instance.
	 */
	public UrlLink()
	{
	}
	
	/**
	 * Creates a copy of a URL link.
	 * 
	 * @param src Source link.
	 */
	public UrlLink(UrlLink src)
	{
		urlPattern = src.urlPattern;
		external = src.external;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.links.IGrepLink#copy()
	 */
	@Override
	public UrlLink copy()
	{
		return new UrlLink(this);
	}

	/**
	 * Returns the URL.
	 * 
	 * @return URL.
	 */
	public String getUrlPattern()
	{
		return urlPattern;
	}

	/**
	 * Sets the URL.
	 * 
	 * @param urlPattern URL.
	 */
	public void setUrlPattern(String urlPattern)
	{
		this.urlPattern = urlPattern;
	}

	/**
	 * Returns whether the URL will be opened in an external application.
	 * 
	 * @return Whether the URL is opened externally.
	 */
	public boolean isExternal()
	{
		return external;
	}

	/**
	 * Sets whether the URL will be opened in an external application.
	 * 
	 * @param external Whether the URL is opened externally.
	 */
	public void setExternal(boolean external)
	{
		this.external = external;
	}
}
