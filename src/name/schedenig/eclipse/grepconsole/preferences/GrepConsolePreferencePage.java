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

package name.schedenig.eclipse.grepconsole.preferences;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.items.EditableItemsPanel;
import name.schedenig.eclipse.grepconsole.view.styles.StylesPanel;
import name.schedenig.eclipse.grepconsole.view.whatsnew.WhatsNewDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Grep Console preference page. Contains two tabs, one for editing the grep
 * expressions and one for editing the styles.
 * 
 * @author msched
 */
public class GrepConsolePreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
	// --- GUI variables ---
	private TabFolder tabFolder;
	private TabItem tiExpressions;
	private TabItem tiStyles;
	private EditableItemsPanel panelExpressions;
	private StylesPanel panelStyles;

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Activator activator = Activator.getDefault();
		GrepExpressionsWithSelection expressions = new GrepExpressionsWithSelection(activator.getExpressions().copy(true), null, null, null, null);

		tabFolder = new TabFolder(parent, SWT.TOP);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tiExpressions = new TabItem(tabFolder, SWT.NONE);
		tiExpressions.setText(Messages.GrepConsolePreferencePage_expressions);
		panelExpressions = createExpressionsPanel(tabFolder);
		tiExpressions.setControl(panelExpressions);
		panelExpressions.setExpressions(expressions);
		
		tiStyles = new TabItem(tabFolder, SWT.NONE);
		tiStyles.setText(Messages.GrepConsolePreferencePage_styles);
		panelStyles = createStylesPanel(tabFolder);
		tiStyles.setControl(panelStyles);
		panelStyles.setRoot(expressions.getRootFolder());

		return tabFolder;
	}

	/**
	 * By default, PreferencePage will return its content's preferred size, which
	 * in turn depends on the number of groups/items and styles displayed in the
	 * panels. This can lead to huge page sizes and also forces scroll bars on
	 * the panels instead of the tables inside them.
	 * 
	 * To prevent this, we return a predetermined minimum size similar to what
	 * we do for the items/styles dialog.
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#doComputeSize()
	 */
	@Override
	protected Point doComputeSize()
	{
		return GrepConsoleUtil.charsToPixelDimensions(getShell(), 40, 25);
	}
	
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		super.createControl(parent);
		
		panelExpressions.resizeColumns();
		
		WhatsNewDialog.showIfEnabled(parent.getShell());
	}
	
	/**
	 * Creates the contents of the expressions tab.
	 * 
	 * @param parent Parent tab folder.
	 * 
	 * @return Expressions panel.
	 */
	private EditableItemsPanel createExpressionsPanel(Composite parent)
	{
		EditableItemsPanel panel = new EditableItemsPanel(parent, SWT.NONE);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return panel;
	}

	/**
	 * Creates the contents of the styles tab.
	 * 
	 * @param parent Parent tab folder.
	 * 
	 * @return Styles panel.
	 */
	private StylesPanel createStylesPanel(Composite parent)
	{
		StylesPanel panel = new StylesPanel(parent, SWT.NONE);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return panel;
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench)
	{
	}
	
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk()
	{
		Activator activator = Activator.getDefault();
		GrepExpressionRootFolder activatorExpressions = activator.getExpressions();
		activatorExpressions.copyFrom(panelExpressions.getExpressions().getRootFolder(), true);
		
		try
		{
			activator.saveSettings();
		}
		catch (ParserConfigurationException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_save_settings, ex);
		}
		catch (TransformerException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_save_settings, ex);
		}
		catch (BackingStoreException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_save_settings, ex);
		}
		
		return super.performOk();
	}
	
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults()
	{
		GrepExpressionRootFolder expressions = Activator.getDefault().loadDefaults().copy(true);
		
		panelExpressions.setExpressions(new GrepExpressionsWithSelection(expressions, null, null, null, null));
		panelStyles.setRoot(expressions);
		
		super.performDefaults();
	}
}
