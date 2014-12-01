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

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Similar to JFace's ListDialog, but does not care about or return the
 * selection the user makes. In particular, double clicking into the list does
 * not close the dialog. The user has to click "Ok" or "Cancel" to close the
 * dialog.
 * 
 * @author msched
 */
public class ReadOnlyListDialog extends SelectionDialog
{
	/** Viewer. */
	private TableViewer viewer;
	
	/** Content provider. */
	private IContentProvider contentProvider;
	
	/** Label provider. */
	private IBaseLabelProvider labelProvider;
	
	/** List input. */
	private Object input;
	
	/** Default width hint in chars. */
  private int widthInChars = 55;
	
	/** Default height hint in chars. */
  private int heightInChars = 15;

	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell Parent shell.
	 */
	public ReadOnlyListDialog(Shell parentShell)
	{
		super(parentShell);
		
		setHelpAvailable(false);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
  protected Control createDialogArea(Composite container) {
    Composite parent = (Composite) super.createDialogArea(container);
    createMessageArea(parent);
    
    viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    viewer.setContentProvider(contentProvider);
    viewer.setLabelProvider(labelProvider);
    viewer.setInput(input);
    
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = convertHeightInCharsToPixels(heightInChars);
    gd.widthHint = convertWidthInCharsToPixels(widthInChars);
    viewer.getTable().setLayoutData(gd);
    
    return parent;
  }

  /**
   * Returns the content provider.
   * 
   * @return Content provider.
   */
	public IContentProvider getContentProvider()
	{
		return contentProvider;
	}

	/**
	 * Sets the content provider.
	 * 
	 * @param contentProvider Content provider.
	 */
	public void setContentProvider(IContentProvider contentProvider)
	{
		this.contentProvider = contentProvider;
	}

	/**
	 * Returns the label provider.
	 * 
	 * @return Label provider.
	 */
	public IBaseLabelProvider getLabelProvider()
	{
		return labelProvider;
	}

	/**
	 * Sets the label provider.
	 * 
	 * @param labelProvider Label provider.
	 */
	public void setLabelProvider(IBaseLabelProvider labelProvider)
	{
		this.labelProvider = labelProvider;
	}

	/**
	 * Returns the list input.
	 * 
	 * @return Input.
	 */
	public Object getInput()
	{
		return input;
	}

	/**
	 * Sets the list input.
	 *  
	 * @param input Input.
	 */
	public void setInput(Object input)
	{
		this.input = input;
	}
}
