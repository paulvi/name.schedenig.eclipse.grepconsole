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

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.ScriptLink;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel for editing script links.
 * 
 * @author msched
 */
public class ScriptLinkPanel extends LinkPanel
{
	// --- GUI variables ---
	private Label labelLanguage;
	private Combo comboLanguage;
	private Label labelCode;
	private Text textCode;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent control.
	 */
	public ScriptLinkPanel(Composite parent, boolean withCaptureGroup)
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
		
		labelLanguage = new Label(this, SWT.NONE);
		labelLanguage.setText(Messages.ScriptLinkPanel_language);
		labelLanguage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		comboLanguage = new Combo(this, SWT.DROP_DOWN);// new Text(this, SWT.NONE);
		comboLanguage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		comboLanguage.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				((ScriptLink) getLink()).setLanguage(comboLanguage.getText());
			}
		});
		
		labelCode = new Label(this, SWT.NONE);
		labelCode.setText(Messages.ScriptLinkPanel_code);
		labelCode.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
	
		textCode = new Text(this, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		textCode.setFont(JFaceResources.getTextFont());
		textCode.setToolTipText(Messages.ScriptLinkPanel_parameters_tooltip);

		textCode.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				((ScriptLink) getLink()).setCode(textCode.getText());
			}
		});
		
		ScriptEngineManager factory = new ScriptEngineManager();
		
		for(ScriptEngineFactory f: factory.getEngineFactories())
		{
			for(String s: f.getNames())
			{
				comboLanguage.add(s);
			}
		}
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.links.LinkPanel#refresh()
	 */
	@Override
	protected void refresh()
	{
		super.refresh();
		
		ScriptLink link = ((ScriptLink) getLink());
		comboLanguage.setText(link.getLanguage() == null ? "" : link.getLanguage());; //$NON-NLS-1$
		textCode.setText(link.getCode() == null ? "" : link.getCode());; //$NON-NLS-1$
	}
}
