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
import name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener;
import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;

/**
 * Displays the "add expression" menu.
 * 
 * @author msched
 */
public class AddExpressionAction implements IViewActionDelegate, IMenuCreator, IGrepConsoleListener
{
	/** Source view. */
	private IConsoleView view;
	
	/** Currently selected text. */
	private String selected;
	
	/** Menu. Created on demand. */
	private Menu menu;
	
	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view)
	{
		this.view = (IConsoleView) view;
		
		Activator.getDefault().addListener(this);
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	@Override
	public void dispose()
	{
		Activator.getDefault().removeListener(this);
		disposeMenu();
	}

	/**
	 * Disposes the menu.
	 */
	private void disposeMenu()
	{
    if(menu != null)
    {
      menu.dispose();
      menu = null;
    }
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
	}

	/**
	 * Updates the currently selected text.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
		if(action.getMenuCreator() != this)
		{
			action.setMenuCreator(this);
		}
		
		selected = null;
		
		if(selection instanceof ITextSelection)
		{
			ITextSelection ts = (ITextSelection) selection;
			
			if(ts.getStartLine() == ts.getEndLine() && ts.getText() != null
					&& ts.getText().length() > 0)
			{
				selected  = ts.getText();
			}
		}
		
		action.setEnabled(selected != null);
		
		if(menu != null)
		{
			updateMenu();
		}
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public Menu getMenu(Control parent)
	{
		return null;
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	@Override
	public Menu getMenu(Menu parent)
	{
		disposeMenu();
	  menu = new Menu(parent);
	  updateMenu();

		return menu;
	}
	
	/**
	 * Recreates the menu's content.
	 */
	private void updateMenu()
	{
		for(MenuItem item: menu.getItems())
		{
			item.dispose();
		}
		
		Activator plugin = Activator.getDefault();
		Shell shell = view.getViewSite().getShell();
		GrepPageParticipant participant = getActiveParticipant();
		
		for(AbstractGrepModelElement group: plugin.getExpressions().getChildren())
	  {
	  	GrepConsoleUtil.addActionToMenu(menu, new AddExpressionToFolderAction(shell, participant, (GrepExpressionFolder) group, selected));
	  }

  	ActionContributionItem aci = new ActionContributionItem(new AddExpressionToFolderAction(shell, participant, null, selected));
  	aci.fill(menu, -1);
	}

	/**
	 * Returns the console's active participant.
	 * 
	 * @return Participant, or <code>null</code>.
	 */
	private GrepPageParticipant getActiveParticipant()
	{
		IConsole console = view.getConsole();
		
		return console == null ? null : Activator.getDefault().getParticipant(console);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#participantAdded(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	public void participantAdded(GrepPageParticipant participant)
	{
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#participantRemoved(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	public void participantRemoved(GrepPageParticipant participant)
	{
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#participantActivated(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	public void participantActivated(GrepPageParticipant participant)
	{
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#settingsChanged()
	 */
	@Override
	public void settingsChanged()
	{
		if(menu != null)
		{
			updateMenu();
		}
	}
}
