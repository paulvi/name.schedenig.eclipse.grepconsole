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

package name.schedenig.eclipse.grepconsole.view.whatsnew;

import java.io.IOException;
import java.net.URL;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

/**
 * Displays a "what's new" message.
 *  
 * @author msched
 */
public class WhatsNewDialog extends Dialog
{
	/**
	 * A shell listener that shows the dialog the first time the associated
	 * shell is activated.
	 *  
	 * @author msched
	 */
	public static class ShellListener extends ShellAdapter
	{
		/** <code>true</code> at the beginning, switches to <code>false</code> after
		 *  shellActivated() has been called for the first time. */
		private boolean first = true;

		/**
		 * @see org.eclipse.swt.events.ShellAdapter#shellActivated(org.eclipse.swt.events.ShellEvent)
		 */
		@Override
		public void shellActivated(ShellEvent e)
		{
			if(first)
			{
				first = false;
				
				WhatsNewDialog.showIfEnabled((Shell) e.widget);
			}
		}
	}

	/** What's new text. Automatically loaded on class initialisation. */
	private static String text = null;
	
	/** GUI variables. */
	private Browser stMessage;
	private Button cbDisplayAgain;

	/**
	 * Load internationalised "what's new" text when class is initialised.
	 */
	static
	{
		Bundle rcpBundle = Platform.getBundle(Activator.PLUGIN_ID);
		Path path = new Path("$nl$/resources/html/whatsnew.html"); //$NON-NLS-1$
		
		try
		{
			URL url = FileLocator.find(rcpBundle, path, null);
			
			if(url != null)
			{
				text = GrepConsoleUtil.readText(url.openStream());
			}
		}
		catch(IOException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.WhatsNewDialog_could_not_load_whats_new, ex);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell
	 */
	protected WhatsNewDialog(Shell parentShell)
	{
		super(parentShell);
	}

	/**
	 * Makes the dialog resizable.
	 * 
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
		
		newShell.setText(Messages.WhatsNewDialog_title_whats_new);
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize()
	{
		return new Point(400, 400); // it's ugly to use hardcoded pixel sizes, but it's only a what's new dialog...
	}
	
	/**
	 * Creates only an Ok button.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Dialog.applyDialogFont(parent);
		
		return super.createContents(parent);
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite content = new Composite(parent, SWT.NONE);
		new GridLayoutBuilder(content, 1, false).apply();
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		stMessage = new Browser(content, SWT.BORDER);
		stMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stMessage.setText(text);

		cbDisplayAgain = new Button(content, SWT.CHECK);
		cbDisplayAgain.setText(Messages.WhatsNewDialog_show_again);
		cbDisplayAgain.setSelection(true);
		cbDisplayAgain.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		return content;
	}
	
	/**
	 * Stores whether or not to show the dialog again next time.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
		Activator.getDefault().setShowWhatsNew(cbDisplayAgain.getSelection());
		
		super.okPressed();
	}

	/**
	 * Creates and shows the dialog only if the plug-in says that it may be
	 * displayed.
	 *  
	 * @param parentShell Parent shell.
	 */
	public static void showIfEnabled(Shell parentShell)
	{
		Activator.getDefault().displayLegacyWarningIfEnabled(parentShell);
		
		if(text == null)
		{
			return;
		}
		
		if(!Activator.getDefault().isShowWhatsNew())
		{
			return;
		}
		
		new WhatsNewDialog(parentShell).open();
	}
}
