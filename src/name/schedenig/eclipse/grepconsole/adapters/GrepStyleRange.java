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

package name.schedenig.eclipse.grepconsole.adapters;

import java.util.regex.Matcher;

import name.schedenig.eclipse.grepconsole.adapters.links.LinkMatch;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.custom.StyleRange;

/**
 * Stores match data for a single expression group.
 * 
 * @author msched
 */
public class GrepStyleRange
{
	/** Originating expression item, if any. */
	private GrepExpressionItem expressionItem;
	
	/** Index of the matched expression. Used to ensure matches are processed
	 *  in the correct order. */
	private int expressionIndex;
	
	/** Whether the match refers to the whole line or a sub string (via a
	 *  capture group). */
	private boolean wholeLine;
	
	/** Index of the first matched character. */
	private int firstIndex;
	
	/** Index of the last matched character. */
	private int lastIndex;
	
	/** Style (if any). */
	private GrepStyle style;
	
	/** Link match (if any). */
	private LinkMatch linkMatch;
	
	/** SWT style range. This is used to represent the original style of a line
	 *  before Grep Console's own styling. Therefore, when this is set, style and
	 *  linkMatch will be null. */
	private StyleRange styleRange;

	/**
	 * Creates a new instance base on an previous SWT style range.
	 *
	 * @param firstIndex Index of the first matched character.
	 * @param lastIndex Index of the last matched character.
	 * @param styleRange SWT style range.
	 */
	public GrepStyleRange(int firstIndex, int lastIndex, StyleRange styleRange)
	{
		this.firstIndex = firstIndex;
		this.lastIndex = lastIndex;
		this.styleRange = styleRange;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param expressionItem Original expression item.
	 * @param expressionIndex Index of the matched expression. Used to ensure
	 * 		matches are processed in the correct order.
	 * @param wholeLine Whether the match refers to the whole line or a sub
	 * 		string (via a capture group).
	 * @param firstIndex Index of the first matched character.
	 * @param lastIndex Index of the last matched character.
	 * @param style Style (if any).
	 * @param link Link (if any).
	 * @param matcher Matcher.
	 * @param wholeLineText Text of the whole line.
	 * @param group Index of the matched group.
	 * @param project Optional Eclipse project.
	 */
	public GrepStyleRange(GrepExpressionItem expressionItem, int expressionIndex, boolean wholeLine,
			int firstIndex, int lastIndex, GrepStyle style, IGrepLink link,
			Matcher matcher, String wholeLineText, int group, IProject project)
	{
		this.expressionItem = expressionItem;
		this.expressionIndex = expressionIndex;
		this.wholeLine = wholeLine;
		this.firstIndex = firstIndex;
		this.lastIndex = lastIndex;
		this.style = style;

		linkMatch = link == null ? null : new LinkMatch(expressionItem, link, matcher.toMatchResult(), matcher.start(), wholeLineText, group, project);
	}

	/**
	 * Returns the expression index.
	 * 
	 * @return Expression index.
	 */
	public int getExpressionIndex()
	{
		return expressionIndex;
	}

	/**
	 * Returns whether the match refers to the whole line.
	 * 
	 * @return Whether the match refers to the whole line.
	 */
	public boolean isWholeLine()
	{
		return wholeLine;
	}

	/**
	 * Returns the index of the first matched character.
	 * 
	 * @return First character index.
	 */
	public int getFirstIndex()
	{
		return firstIndex;
	}

	/**
	 * Returns the index of the last matched character.
	 * 
	 * @return Last character index.
	 */
	public int getLastIndex()
	{
		return lastIndex;
	}

	/**
	 * Returns the style.
	 * 
	 * @return Style. May be <code>null</code>.
	 */
	public GrepStyle getStyle()
	{
		return style;
	}
	
	/**
	 * Returns the link.
	 * 
	 * @return Link. May be <code>null</code>.
	 */
	public LinkMatch getLinkMatch()
	{
		return linkMatch;
	}
	
	/**
	 * Returns the SWT style range.
	 * 
	 * @return SWT style range.
	 */
	public StyleRange getStyleRange()
	{
		return styleRange;
	}
	
	/**
	 * Returns the original expression item.
	 * 
	 * @return Expression item. May be <code>null</code>.
	 */
	public GrepExpressionItem getExpressionItem()
	{
		return expressionItem;
	}
}