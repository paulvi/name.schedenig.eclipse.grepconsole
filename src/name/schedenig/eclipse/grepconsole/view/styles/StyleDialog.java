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

package name.schedenig.eclipse.grepconsole.view.styles;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.DefaultSizeDialog;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A modal dialog containing a style panel.
 * 
 * @author msched
 */
public class StyleDialog extends DefaultSizeDialog
{
	/** Dialog settings section key. */
	public static final String DIALOG_SETTINGS_SECTION = "styleDialog"; //$NON-NLS-1$
	
	/** Whether the dialogue is used to add a new style or edit an existing one. */
	private boolean add;
	
	/** The style being edited. */
	private GrepStyle grepStyle;
	
	/** The style panel. */
	private StylePanel panelStyle;

	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell Parent shell.
	 * @param add Whether the dialogue is used to add a new style or edit an
	 * 		existing one.
	 */
	public StyleDialog(Shell parentShell, boolean add)
	{
		super(parentShell);
		
		this.add = add;
	}

	/**
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	@Override
	protected int getShellStyle()
	{
		return super.getShellStyle() | SWT.RESIZE;
	}
	
	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		
		newShell.setText(add ? Messages.StyleDialog_title_add_style : Messages.StyleDialog_title_edit_style);
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, Activator.PLUGIN_ID + ".edit_style"); //$NON-NLS-1$

		panelStyle = new StylePanel(parent, SWT.NONE);
		panelStyle.setGrepStyle(grepStyle);
		panelStyle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		return panelStyle;
	}

	/**
	 * Sets the style being edited.
	 * 
	 * @param style Stlye.
	 */
	public void setGrepStyle(GrepStyle grepStyle)
	{
		this.grepStyle = grepStyle;
		
		if(panelStyle != null)
		{
			panelStyle.setGrepStyle(grepStyle);
		}
	}
	
	/**
	 * Returns the style being edited.
	 * 
	 * @return Style.
	 */
	public GrepStyle getGrepStyle()
	{
		return grepStyle;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
		panelStyle.updateGrepStyle();
		
		super.okPressed();
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings()
	{
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_SECTION);
		
		if(settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS_SECTION);
		}
		
		return settings;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.DefaultSizeTitleAreaDialog#getDefaultSize()
	 */
	protected Point getDefaultSize()
	{
		Shell shell = getShell();
		return new Point(GrepConsoleUtil.charsToPixelDimensions(shell, 50, 0).x, shell.getMinimumSize().y);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
	 */
	@Override
	protected void initializeBounds()
	{
		super.initializeBounds();

		Shell shell = getShell();
		shell.setMinimumSize(GrepConsoleUtil.charsToPixelDimensions(shell, 45, 0).x, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}
}
