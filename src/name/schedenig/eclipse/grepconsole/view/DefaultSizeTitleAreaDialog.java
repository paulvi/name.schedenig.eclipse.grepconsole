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

package name.schedenig.eclipse.grepconsole.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A title area dialog that can provide a default size for use when no preferred
 * size has been persisted in the dialog settings.
 * 
 * Mostly copies code from Dialog, which implements most of the necessary
 * functionality where it cannot be changed.
 *  
 * @author msched
 */
public class DefaultSizeTitleAreaDialog extends TitleAreaDialog
{
	/** These are copied from Dialog class, where they are private. */
	public static final String DIALOG_FONT_DATA = "DIALOG_FONT_NAME"; //$NON-NLS-1$
	public static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	public static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$

	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell Parent shell.
	 */
	public DefaultSizeTitleAreaDialog(Shell parentShell)
	{
		super(parentShell);
	}

	/**
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		
		Label imageLabel = getTitleImageLabel();
		FormData formData = (FormData) imageLabel.getLayoutData();
		formData.right.offset = -4;

		Dialog.applyDialogFont(parent);

		return contents;
	}
	
	/**
	 * Mostly a copy of the same method in Dialog, but with a call to a separate
	 * method for providing a default size that is used if no persisted dialog
	 * settings are available.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize()
	{
		Point result = getDefaultSize();

		if((getDialogBoundsStrategy() & DIALOG_PERSISTSIZE) != 0)
		{
			IDialogSettings settings = getDialogBoundsSettings();

			if(settings != null)
			{
				boolean useStoredBounds = true;
				String previousDialogFontData = settings.get(DIALOG_FONT_DATA);

				if(previousDialogFontData != null && previousDialogFontData.length() > 0)
				{
					FontData[] fontDatas = JFaceResources.getDialogFont().getFontData();
					if(fontDatas.length > 0)
					{
						String currentDialogFontData = fontDatas[0].toString();
						useStoredBounds = currentDialogFontData.equalsIgnoreCase(previousDialogFontData);
					}
				}
				
				if(useStoredBounds)
				{
					try
					{
						int width = settings.getInt(DIALOG_WIDTH);
						int height = settings.getInt(DIALOG_HEIGHT);

						if(width != DIALOG_DEFAULT_BOUNDS)
						{
							result.x = width;
						}
						
						if(height != DIALOG_DEFAULT_BOUNDS)
						{
							result.y = height;
						}

					}
					catch(NumberFormatException e)
					{
					}
				}
			}
		}

		return result;
	}

	/**
	 * Provides the dialog's default size. Duplicates the behaviour of JFace's
	 * standard dialog. Subclasses may override.
	 * 
	 * @return Default size.
	 */
	protected Point getDefaultSize()
	{
		return getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}
}
