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
import name.schedenig.eclipse.grepconsole.model.links.JavaLink;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel for editing Java links.
 * 
 * @author msched
 */
public class JavaLinkPanel extends LinkPanel
{
	// --- GUI variables ---
	private Label labelPattern;
	private Text textPattern;
	private Label labelLineNumberPattern;
	private Text textLineNumberPattern;
	private Label labelOffsetPattern;
	private Text textOffsetPattern;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent control.
	 */
	public JavaLinkPanel(Composite parent, boolean withCaptureGroup)
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

		labelPattern = new Label(this, SWT.NONE);
		labelPattern.setText(Messages.JavaLinkPanel_type);
		labelPattern.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		textPattern = new Text(this, SWT.BORDER);
		textPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textPattern.setToolTipText(getPatternTooltipText());

		textPattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				((JavaLink) getLink()).setTypePattern(textPattern.getText());
			}
		});
		
		labelLineNumberPattern = new Label(this, SWT.NONE);
		labelLineNumberPattern.setText(Messages.JavaLinkPanel_line_number);
		labelLineNumberPattern.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textLineNumberPattern = new Text(this, SWT.BORDER);
		textLineNumberPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textLineNumberPattern.setToolTipText(getPatternTooltipText());

		textLineNumberPattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				((JavaLink) getLink()).setLineNumberPattern(textLineNumberPattern.getText());
			}
		});
		
		labelOffsetPattern = new Label(this, SWT.NONE);
		labelOffsetPattern.setText(Messages.FileLinkPanel_offset);
		labelOffsetPattern.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textOffsetPattern = new Text(this, SWT.BORDER);
		textOffsetPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textOffsetPattern.setToolTipText(getPatternTooltipText());
		
		textOffsetPattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				((JavaLink) getLink()).setOffsetPattern(textOffsetPattern.getText());
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
		
		String pattern = ((JavaLink) getLink()).getTypePattern();
		textPattern.setText(pattern == null ? "" : pattern); //$NON-NLS-1$

		String lineNumberPattern = ((JavaLink) getLink()).getLineNumberPattern();
		textLineNumberPattern.setText(lineNumberPattern == null ? "" : lineNumberPattern); //$NON-NLS-1$
		
		String offsetPattern = ((JavaLink) getLink()).getOffsetPattern();
		textOffsetPattern.setText(offsetPattern == null ? "" : offsetPattern); //$NON-NLS-1$
	}
}
