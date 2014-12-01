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
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.view.grepview.GrepView;

import org.eclipse.jface.action.Action;

/**
 * Toggles Grep View's auto scroll feature.
 * 
 * @author msched
 */
public class ScrollLockAction extends Action
{
	/** Action ID. */
	public static final String ACTION_ID = "name.schedenig.eclipse.grepconsole.actions.ScrollLock"; //$NON-NLS-1$
	
	/** Grep View. */
	private GrepView view;

	/**
	 * Creates a new instance.
	 * 
	 * @param view Grep View. 
	 */
	public ScrollLockAction(GrepView view)
	{
		super(Messages.ScrollLockAction_scroll_lock, AS_CHECK_BOX);
		
		this.view = view;
		
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_ACTION_SCROLL_LOCK));
		setChecked(view.isScrollLock());
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run()
	{
		boolean scrollLock = !view.isScrollLock();
		view.setScrollLock(scrollLock);
		setChecked(view.isScrollLock());
	}
}
