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

package name.schedenig.eclipse.grepconsole.actions;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.view.common.GrepConsoleView;

import org.eclipse.jface.action.Action;

/**
 * Toggles Grep View's console link.
 * 
 * @author msched
 */
public class LinkedToConsoleAction extends Action
{
	/** Action ID. */
	public static final String ACTION_ID = "name.schedenig.eclipse.grepconsole.actions.LinkedToConsole"; //$NON-NLS-1$
	
	/** Grep View. */
	private GrepConsoleView view;

	/**
	 * Creates a new instance.
	 * 
	 * @param view Grep View.
	 */
	public LinkedToConsoleAction(GrepConsoleView view)
	{
		super(Messages.LinkedToConsoleAction_linked_to_console, AS_CHECK_BOX);
		
		this.view = view;

		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_ACTION_LINKED_TO_CONSOLE));
		setChecked(view.isLinkedToConsole());
	}
	
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run()
	{
		boolean link = !view.isLinkedToConsole();
		view.setLinkedToConsole(link);
		setChecked(view.isLinkedToConsole());
		
		if(link)
		{
			Activator activator = Activator.getDefault();
			GrepPageParticipant participant = activator.getActiveParticipant();
			
			if(participant != null)
			{
				view.setConsole(participant.getConsole());
			}
		}
	}
}
