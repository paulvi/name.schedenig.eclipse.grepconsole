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
 * A link for opening a file (external to the Eclipse workspace) in an Eclipse
 * editor.
 * 
 * @author msched
 */
public class FileLink implements IGrepLink
{
	/** File name. */
	private String filePattern;
	
	/** Optional base directory. */
	private String baseDirPattern;
	
	/** Optional file number. */
	private String lineNumberPattern;
	
	/** Optional offset. */
	private String offsetPattern;
	
	/**
	 * Creates a new instance.
	 */
	public FileLink()
	{
	}
	
	/**
	 * Copies a file link.
	 * 
	 * @param src Source link.
	 */
	public FileLink(FileLink src)
	{
		filePattern = src.filePattern;
		baseDirPattern = src.baseDirPattern;
		lineNumberPattern = src.lineNumberPattern;
		offsetPattern = src.offsetPattern;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.links.IGrepLink#copy()
	 */
	@Override
	public FileLink copy()
	{
		return new FileLink(this);
	}

	/**
	 * Returns the file name.
	 * 
	 * @return File name.
	 */
	public String getFilePattern()
	{
		return filePattern;
	}

	/**
	 * Sets the file name.
	 * 
	 * @param filePattern File name.
	 */
	public void setFilePattern(String filePattern)
	{
		this.filePattern = filePattern;
	}

	/**
	 * Returns the base directory.
	 * 
	 * @return Base directory.
	 */
	public String getBaseDirPattern()
	{
		return baseDirPattern;
	}

	/**
	 * Sets the base directory.
	 * 
	 * @param baseDirPattern Base directory.
	 */
	public void setBaseDirPattern(String baseDirPattern)
	{
		this.baseDirPattern = baseDirPattern;
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
