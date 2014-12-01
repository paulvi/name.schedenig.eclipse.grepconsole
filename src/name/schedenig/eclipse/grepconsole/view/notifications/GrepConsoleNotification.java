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

package name.schedenig.eclipse.grepconsole.view.notifications;

import name.schedenig.eclipse.grepconsole.adapters.links.LinkMatch;
import name.schedenig.eclipse.popupnotifications.Notification;

/**
 * A Grep Console specific notification which stores a link match.
 *  
 * @author msched
 */
public class GrepConsoleNotification extends Notification
{
	/** The link match associated with the notification. */
	private LinkMatch linkMatch;

	/**
	 * Creates a new instances.
	 * 
	 * @param linkMatch A link match.
	 */
	public GrepConsoleNotification(LinkMatch linkMatch)
	{
		this.linkMatch = linkMatch;
	}

	/**
	 * Returns the link match.
	 * 
	 * @return Link match.
	 */
	public LinkMatch getLinkMatch()
	{
		return linkMatch;
	}
}
