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

import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A modal dialog for editing a model folder element.
 * 
 * @author msched
 */
public class FolderDialog extends InputDialog
{
	/** The folder being edited. */
	private GrepExpressionFolder folder;
	
	/** GUI variables. */
	private Button cbActiveByDefault;
	private Button cbFilterDefault;
	private Button cbNotificationsDefault;

	private Button cbStatisticsDefault;

	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell Parent shell.
	 * @param folder The folder being edited.
	 * @param add 
	 */
	public FolderDialog(Shell parentShell, GrepExpressionFolder folder, boolean add)
	{
		super(parentShell, add ? Messages.FolderDialog_title_add_folder : Messages.FolderDialog_title_edit_folder, Messages.FolderDialog_name, "", new IInputValidator() //$NON-NLS-1$
		{
			@Override
			public String isValid(String newText)
			{
				return newText.trim().length() == 0 ? Messages.FolderDialog_please_enter_a_name : null;
			}
		});
		
		setFolder(folder);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		Dialog.applyDialogFont(parent);
		
		return contents;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.InputDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		
		cbActiveByDefault = new Button(composite, SWT.CHECK);
		cbActiveByDefault.setText(Messages.FolderDialog_active_by_default);
		cbActiveByDefault.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		cbFilterDefault = new Button(composite, SWT.CHECK);
		cbFilterDefault.setText(Messages.FolderDialog_filter_by_default);
		cbFilterDefault.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		cbStatisticsDefault = new Button(composite, SWT.CHECK);
		cbStatisticsDefault.setText(Messages.FolderDialog_statistics_by_default);
		cbStatisticsDefault.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		cbNotificationsDefault = new Button(composite, SWT.CHECK);
		cbNotificationsDefault.setText(Messages.FolderDialog_notifications_active_by_default);
		cbNotificationsDefault.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		return composite;
	}

	/**
	 * @see org.eclipse.jface.dialogs.InputDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);

		// Do this here because InputDialog also does and would otherwise override
		// our settings.
		refresh();
	}
	
	/**
	 * Updates GUI content to match the current folder.
	 */
	private void refresh()
	{
		String name = folder.getName();
		getText().setText(name == null ? "" : name); //$NON-NLS-1$
		getText().selectAll();
		cbActiveByDefault.setSelection(folder.isDefaultEnabled());
		cbFilterDefault.setSelection(folder.isDefaultFilter());
		cbStatisticsDefault.setSelection(folder.isDefaultStatistics());
		cbNotificationsDefault.setSelection(folder.isDefaultNotifications());
	}

	/**
	 * Sets the folder being edited.
	 * 
	 * @param folder Folder.
	 */
	public void setFolder(GrepExpressionFolder folder)
	{
		this.folder = folder;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
		folder.setName(getValue().trim());
		folder.setDefaultEnabled(cbActiveByDefault.getSelection());
		folder.setDefaultFilter(cbFilterDefault.getSelection());
		folder.setDefaultStatistics(cbStatisticsDefault.getSelection());
		folder.setDefaultNotifications(cbNotificationsDefault.getSelection());
		
		super.okPressed();
	}
}
