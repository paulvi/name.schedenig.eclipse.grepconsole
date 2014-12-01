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
 * A link for opening a Java type from the current project in an Eclipse editor.
 * 
 * @author msched
 */
public class JavaLink implements IGrepLink
{
	/** Type name. */
	private String typePattern;
	
	/** Optional line number. */
	private String lineNumberPattern;
	
	/** Optional offset. */
	private String offsetPattern;
	
	/**
	 * Creates a new instance.
	 */
	public JavaLink()
	{
	}
	
	/**
	 * Copies a Java link.
	 * 
	 * @param src Source link.
	 */
	public JavaLink(JavaLink src)
	{
		typePattern = src.typePattern;
		lineNumberPattern = src.lineNumberPattern;
		offsetPattern = src.offsetPattern;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.links.IGrepLink#copy()
	 */
	@Override
	public JavaLink copy()
	{
		return new JavaLink(this);
	}

	/**
	 * Returns the type name.
	 * 
	 * @return Type name.
	 */
	public String getTypePattern()
	{
		return typePattern;
	}

	/**
	 * Sets the type name.
	 * 
	 * @param typePattern Type name.
	 */
	public void setTypePattern(String typePattern)
	{
		this.typePattern = typePattern;
	}

	/**
	 * Returns the line number.
	 * 
	 * @return Line number.
	 */
	public String getLineNumberPattern()
	{
		return lineNumberPattern;
	}
	
	/**
	 * Sets the line number.
	 * 
	 * @param lineNumberPattern Line number.
	 */
	public void setLineNumberPattern(String lineNumberPattern)
	{
		this.lineNumberPattern = lineNumberPattern;
	}

	/**
	 * Returns the offset.
	 * 
	 * @return Offset.
	 */
	public String getOffsetPattern()
	{
		return offsetPattern;
	}

	/**
	 * Sets the offset.
	 * 
	 * @param offsetPattern Offset.
	 */
	public void setOffsetPattern(String offsetPattern)
	{
		this.offsetPattern = offsetPattern;
	}
}
