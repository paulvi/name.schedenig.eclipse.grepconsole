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

package name.schedenig.eclipse.grepconsole.view.items;

import java.text.MessageFormat;

import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.model.links.CommandLink;
import name.schedenig.eclipse.grepconsole.model.links.FileLink;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;
import name.schedenig.eclipse.grepconsole.model.links.JavaLink;
import name.schedenig.eclipse.grepconsole.model.links.ScriptLink;
import name.schedenig.eclipse.grepconsole.model.links.UrlLink;
import name.schedenig.eclipse.grepconsole.view.items.links.LinkDialog;
import name.schedenig.eclipse.grepconsole.view.styles.StyleLabelProvider;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the link column of the style assignments table. Takes
 * integer numbers as elements.
 *  
 * @author msched
 */
public class StyleAssignmentsLinkLabelProvider extends StyleLabelProvider
{
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer Viewer to which the provider will be bound. 
	 */
	public StyleAssignmentsLinkLabelProvider(ColumnViewer viewer)
	{
		super(viewer);
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element)
	{
		IGrepLink link = getLink(element);
		String s = getLinkDisplayText(link, false);
		
		if(s == null)
		{
			return link == null ? "" : link.toString(); //$NON-NLS-1$
		}
		else
		{
			return s;
		}
	}

	/**
	 * Returns the display text for a link.
	 * 
	 * @param link Link.
	 * 
	 * @return Display text.
	 */
	public static String getLinkDisplayText(IGrepLink link, boolean detailed)
	{
		String linkName = LinkDialog.TYPE_NAMES.get(link == null ? LinkDialog.NoLink.class : link.getClass());
		String detail;
		
		if(detailed)
		{
			detail = getLinkDetail(link);
		}
		else
		{
			detail = null;
		}
		
		return detail == null ? linkName : MessageFormat.format(Messages.StyleAssignmentsLinkLabelProvider_linkname_detail, linkName, detail);
	}

	/**
	 * Returns a string with action details for the label.
	 *  
	 * @param link Action.
	 * 
	 * @return Details string. <code>null</code> if no link was specified.
	 */
	private static String getLinkDetail(IGrepLink link)
	{
		if(link == null)
		{
			return null;
		}
		else if(link instanceof CommandLink)
		{
			return ((CommandLink) link).getCommandPattern();
		}
		else if(link instanceof FileLink)
		{
			return ((FileLink) link).getFilePattern();
		}
		else if(link instanceof JavaLink)
		{
			return ((JavaLink) link).getTypePattern();
		}
		else if(link instanceof ScriptLink)
		{
			return ((ScriptLink) link).getLanguage();
		}
		else if(link instanceof UrlLink)
		{
			return ((UrlLink) link).getUrlPattern();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.styles.StyleLabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element)
	{
		return null;
	}
	
	/**
	 * Returns the link for content element.
	 * 
	 * @param element Element. Will be treated as an integer index to the viewer's
	 * 		input instance, which is assumed to be an array of style instances.
	 * 
	 * @return Link.
	 */
	private IGrepLink getLink(Object element)
	{
		Integer index = (Integer) element;
		GrepGroup[] groups = (GrepGroup[]) getViewer().getInput();
		return groups[index] == null ? null : groups[index].getLink();
	}
}
