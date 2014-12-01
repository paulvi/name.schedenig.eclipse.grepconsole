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
import name.schedenig.eclipse.grepconsole.model.links.UrlLink;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel for editing URL links.
 * 
 * @author msched
 */
public class UrlLinkPanel extends LinkPanel
{
	// --- GUI variables ---
	private Label labelPattern;
	private Text textPattern;
	private Button cbExternal;

	/**
	 * Creates a new parent control.
	 * 
	 * @param parent
	 */
	public UrlLinkPanel(Composite parent, boolean withCaptureGroup)
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
		labelPattern.setText(Messages.UrlLinkPanel_url);
		labelPattern.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		textPattern = new Text(this, SWT.BORDER);
		textPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textPattern.setToolTipText(getPatternTooltipText());
		
		textPattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				((UrlLink) getLink()).setUrlPattern(textPattern.getText());
			}
		});
		
		cbExternal = new Button(this, SWT.CHECK);
		cbExternal.setText(Messages.UrlLinkPanel_open_link_externally);
		cbExternal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		cbExternal.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				((UrlLink) getLink()).setExternal(cbExternal.getSelection());
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
		
		String pattern = ((UrlLink) getLink()).getUrlPattern();
		textPattern.setText(pattern == null ? "" : pattern); //$NON-NLS-1$
		cbExternal.setSelection(((UrlLink) getLink()).isExternal());
	}
}
