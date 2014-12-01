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
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.common.GrepConsoleView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.console.IConsole;

/**
 * Provides the drop down action to select a console source in Grep View.
 * 
 * Instances can represent either the action that toggles the drop down list of
 * available consoles or a drop down menu entry linked to a specific console.
 * 
 * @author msched
 */
public class SelectConsoleAction extends Action implements IMenuCreator
{
	/** Console for instances representing a specific console.<code>null</code>
	 *  for the instance that provides the drop down menu. */
	private IConsole console;
	
	/** Menu for the drop down instance. Generated on demand. */
	private Menu menu;
	
	/** Grep view. */
	private GrepConsoleView view;

	/**
	 * Creates a new instance of the drop down action.
	 * 
	 * @param view Grep View.
	 */
	public SelectConsoleAction(GrepConsoleView view)
	{
		super(Messages.SelectConsoleAction_display_selected_console, AS_DROP_DOWN_MENU);
		
		this.view = view;
		
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_ACTION_SELECT_CONSOLE));
		setMenuCreator(this);
	}

	/**
	 * Creates a new instance of a menu entry action.
	 * 
	 * @param view Grep View.
	 * @param console Linked console.
	 */
	public SelectConsoleAction(GrepConsoleView view, IConsole console)
	{
		super(console.getName(), AS_RADIO_BUTTON);

		this.view = view;
		this.console = console;
		
		setChecked(view.getConsole() == console);
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	@Override
	public void dispose()
	{
    if(menu != null)
    {
      menu.dispose();
      menu = null;
    }
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run()
	{
		view.setConsole(console);
		
		if(view.isLinkedToConsole())
		{
			GrepPageParticipant participant = Activator.getDefault().getParticipant(console);
			
			if(participant != null)
			{
				participant.setFocus();
			}
		}
	}
	
	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public Menu getMenu(Control parent)
	{
		dispose();
	  menu = new Menu(parent);

		Activator plugin = Activator.getDefault();
		
		for(IConsole console: plugin.getParticipants().keySet())
		{
			GrepConsoleUtil.addActionToMenu(menu, new SelectConsoleAction(view, console));
		}
		
	  return menu;
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	@Override
	public Menu getMenu(Menu parent)
	{
		return null;
	}
}
