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
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;
import name.schedenig.eclipse.grepconsole.view.items.ExpressionDialog;
import name.schedenig.eclipse.grepconsole.view.items.FolderDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Opens the add expression dialog to add a new expression based on a selected
 * string to a specific folder.
 * 
 * @author msched
 */
public class AddExpressionToFolderAction extends Action
{
	/** Target folder. If this is <code>null</code>, a new folder will be
	 *  created. */
	private GrepExpressionFolder folder;
	
	/** Source participant. */
	private GrepPageParticipant participant;
	
	/** Source text. The added expression will match this string. */
	private String text;
	
	/** Shell. Used as parent for the add expression dialog. */
	private Shell shell;

	/**
	 * Creates a new instance.
	 * 
	 * @param shell Shell. Used as parent for the add expression dialog.
	 * @param participant Source participant.
	 * @param folder Target folder. If this is <code>null</code>, a new folder will
	 * 		be created.
	 * @param text Source text. The added expression will match this string.
	 */
	public AddExpressionToFolderAction(Shell shell, GrepPageParticipant participant, GrepExpressionFolder folder, String text)
	{
		this.shell = shell;
		this.participant = participant;
		this.folder = folder;
		this.text = text;
		
		setText(folder == null ? Messages.AddExpressionToFolderAction_new_folder : folder.getName());
	}
	
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run()
	{
		Activator activator = Activator.getDefault();
		GrepExpressionsWithSelection expressions = new GrepExpressionsWithSelection(activator.getExpressions().copy(true), null, null, null, null);

		if(participant != null)
		{
			expressions.setEnablementMap(new HashMap<String, Boolean>(participant.getEnablementMap()));
			expressions.setFilterMap(new HashMap<String, Boolean>(participant.getFilterMap()));
			expressions.setStatisticsMap(new HashMap<String, Boolean>(participant.getStatisticsMap()));
			expressions.setNotificationsMap(new HashMap<String, Boolean>(participant.getNotificationsMap()));
		}

		GrepExpressionFolder folder = this.folder;
		
		if(folder == null)
		{
			folder = new GrepExpressionFolder();
			
			String title;
			
			if(participant == null || participant.getLaunchConfig() == null)
			{
				title = Messages.AddExpressionToFolderAction_new_folder;
			}
			else
			{
				title = participant.getLaunchConfig().getName();
			}
			
			folder.setName(title);
			
			FolderDialog dlg = new FolderDialog(shell, folder, true);
			
			if(dlg.open() != FolderDialog.OK)
			{
				return;
			}
			
			expressions.getRootFolder().add(folder);
			
			if(expressions.getEnablementMap() != null)
			{
				expressions.getEnablementMap().put(folder.getId(), true);
			}
		}
		else
		{
			folder = (GrepExpressionFolder) expressions.getRootFolder().findById(folder.getId());
		}

		GrepExpressionItem expression = new GrepExpressionItem();
		expression.setGrepExpression("(" + Pattern.quote(text) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		folder.add(expression);

		ExpressionDialog dlg = new ExpressionDialog(shell, true);
		dlg.setPreviewText(participant.getStyledText().getText());
		dlg.setItem(expression);
		
		if(dlg.open() == ExpressionDialog.OK)
		{
			activator.getExpressions().copyFrom(expressions.getRootFolder(), true);

			if(participant != null)
			{
				participant.saveLaunchConfig();
			}
			
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
			
			Activator.getDefault().doSettingsChanged();
		}
	}
}
