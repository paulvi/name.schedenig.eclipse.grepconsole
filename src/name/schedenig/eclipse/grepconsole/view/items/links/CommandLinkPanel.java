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

package name.schedenig.eclipse.grepconsole.view.items.links;

import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.CommandLink;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel for editing command links.
 * 
 * @author msched
 */
public class CommandLinkPanel extends LinkPanel
{
	// --- GUI variables ---
	private Label labelCommandPattern;
	private Text textCommandPattern;
	private Label labelWorkingDirPattern;
	private Text textWorkingDirPattern;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent control.
	 */
	public CommandLinkPanel(Composite parent, boolean withCaptureGroup)
	{
		super(parent, withCaptureGroup);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.links.LinkPanel#init()
	 */
	@Override
	protected void init()
	{
		super.init();

		new GridLayoutBuilder(this, 2, false).setMargins(0).apply();
		
		labelCommandPattern = new Label(this, SWT.NONE);
		labelCommandPattern.setText(Messages.CommandLinkPanel_command);
		labelCommandPattern.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		textCommandPattern = new Text(this, SWT.BORDER);
		textCommandPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textCommandPattern.setToolTipText(getPatternTooltipText());
		
		textCommandPattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				((CommandLink) getLink()).setCommandPattern(textCommandPattern.getText());
			}
		});
		
		labelWorkingDirPattern = new Label(this, SWT.NONE);
		labelWorkingDirPattern.setText(Messages.CommandLinkPanel_working_directory);
		labelWorkingDirPattern.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		textWorkingDirPattern = new Text(this, SWT.BORDER);
		textWorkingDirPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textWorkingDirPattern.setToolTipText(getPatternTooltipText());

		textWorkingDirPattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				((CommandLink) getLink()).setWorkingDirPattern(textWorkingDirPattern.getText());
			}
		});
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.links.LinkPanel#refresh()
	 */
	@Override
	protected void refresh()
	{
		super.refresh();
		
		String command = ((CommandLink) getLink()).getCommandPattern();
		textCommandPattern.setText(command == null ? "" : command); //$NON-NLS-1$
		
		String workingDir = ((CommandLink) getLink()).getWorkingDirPattern();
		textWorkingDirPattern.setText(workingDir == null ? "" : workingDir); //$NON-NLS-1$
	}
}
