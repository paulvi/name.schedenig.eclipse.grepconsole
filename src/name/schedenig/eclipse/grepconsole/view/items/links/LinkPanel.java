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

package name.schedenig.eclipse.grepconsole.view.items.links;

import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Base class for panels that are used to edit specific link types.
 * 
 * Instances never create new link instances but always change the link instance
 * provided by the caller.
 * 
 * @author msched
 */
public abstract class LinkPanel extends Composite
{
	/** The link being edited. */
	private IGrepLink link;

	/** Whether a caption group is available for the action. */
	private boolean withCaptureGroup;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent control.
	 */
	public LinkPanel(Composite parent, boolean withCaptureGroup)
	{
		super(parent, SWT.NONE);
		
		this.withCaptureGroup = withCaptureGroup; 
		init();
	}

	/**
	 * Initialises the panel and creates its content.
	 */
	protected void init()
	{
	}

	/**
	 * Sets the link being edited.
	 * 
	 * @param link New link.
	 */
	public void setLink(IGrepLink link)
	{
		if(link != this.link)
		{
			this.link = link;
			refresh();
		}
	}

	/**
	 * Returns the link being edited.
	 * 
	 * @return Link.
	 */
	public IGrepLink getLink()
	{
		return link;
	}

	/**
	 * Refreshes the panel's content.
	 */
	protected void refresh()
	{
	}
	
	/**
	 * Returns the tooltip text.
	 * 
	 * @return Tooltip text.
	 */
	protected String getPatternTooltipText()
	{
		return withCaptureGroup ? 
				Messages.LinkPanel_pattern_tooltip_with_capture_group :
					Messages.LinkPanel_pattern_tooltip_without_capture_group;
	}
}
