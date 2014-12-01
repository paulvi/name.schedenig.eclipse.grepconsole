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

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;
import name.schedenig.eclipse.grepconsole.view.grepview.GrepView;
import name.schedenig.eclipse.grepconsole.view.items.ItemsAndStylesDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Opens the manage expressions dialog.
 * 
 * @author msched
 */
public class ManageExpressionsAction extends Action implements IViewActionDelegate
{
	/** Source view. Should be an IConsoleView of GrepView instance. */
	private IViewPart view;

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view)
	{
		this.view = view;
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
		Activator activator = Activator.getDefault();
		GrepExpressionsWithSelection expressions = new GrepExpressionsWithSelection(activator.getExpressions().copy(true), null, null, null, null);
		GrepPageParticipant activeParticipant = getActiveParticipant();
		
		if(activeParticipant != null)
		{
			expressions.setEnablementMap(new HashMap<String, Boolean>(activeParticipant.getEnablementMap()));
			expressions.setFilterMap(new HashMap<String, Boolean>(activeParticipant.getFilterMap()));
			expressions.setStatisticsMap(new HashMap<String, Boolean>(activeParticipant.getStatisticsMap()));
			expressions.setNotificationsMap(new HashMap<String, Boolean>(activeParticipant.getNotificationsMap()));
		}
		
		ItemsAndStylesDialog dlg = new ItemsAndStylesDialog(view.getSite().getShell());
		dlg.setExpressions(expressions);

		dlg.setLaunchConfiguration(activeParticipant == null ? null : activeParticipant.getLaunchConfig());
		
		GrepPageParticipant participant = getActiveParticipant();
		
		if(participant != null)
		{
			String previewText = participant.getStyledText().getText();
			dlg.setPreviewText(previewText.trim().length() == 0 ? null : previewText);
		}
		
		if(dlg.open() == ItemsAndStylesDialog.OK)
		{
			activator.getExpressions().copyFrom(expressions.getRootFolder(), true);
			
			try
			{
				activator.saveSettings();
			}
			catch(ParserConfigurationException ex)
			{
				Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_save_settings, ex);
			}
			catch(TransformerException ex)
			{
				Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_save_settings, ex);
			}
			catch(BackingStoreException ex)
			{
				Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_save_settings, ex);
			}
			
			if(activeParticipant != null)
			{
				activeParticipant.setEnablementMap(expressions.getEnablementMap());
				activeParticipant.setFilterMap(expressions.getFilterMap());
				activeParticipant.setStatisticsMap(expressions.getStatisticsMap());
				activeParticipant.setNotificationsMap(expressions.getNotificationsMap());
				activeParticipant.saveLaunchConfig();
			}
			
			Activator.getDefault().doSettingsChanged();
		}
	}

	/**
	 * Returns the current console's active participant.
	 * 
	 * @return Active participant, or <code>null</code>.
	 */
	private GrepPageParticipant getActiveParticipant()
	{
		IConsole console = getConsole();
		
		return console == null ? null : Activator.getDefault().getParticipant(console);
	}

	/**
	 * Returns the current console.
	 * 
	 * @return Console, or <code>null</code>.
	 */
	private IConsole getConsole()
	{
		if(view instanceof IConsoleView)
		{
			return ((IConsoleView) view).getConsole();
		}
		else if(view instanceof GrepView)
		{
			return ((GrepView) view).getConsole();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
	}
}
