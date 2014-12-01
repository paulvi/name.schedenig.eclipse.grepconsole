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

package name.schedenig.eclipse.grepconsole.launch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.items.ItemsAndStylesDialog;
import name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel;
import name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener;
import name.schedenig.eclipse.grepconsole.view.whatsnew.WhatsNewDialog;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Grep Console launch configuration tab.
 * 
 * @author msched
 */
public class GrepConsoleLaunchConfigurationTab extends AbstractLaunchConfigurationTab implements IItemTreeListener
{
	/** Map of enabled/disabled item IDs. */ 
	private Map<String, Boolean> enablementMap;
	
	/** Map of filtered/unfiltered item IDs. */
	private Map<String, Boolean> filterMap;
	
	/** Map of active/inactive statistics item IDs. */
	private Map<String, Boolean> statisticsMap;
	
	/** Map of active/inactive notification item IDs. */
	private Map<String, Boolean> notificationsMap;
	
	/** Active launch configuration. */
	private ILaunchConfiguration configuration;
	
	// --- GUI variables ---
	private ItemsTreePanel treePanel;
	private Button btnConfigure;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName()
	{
		return Messages.GrepConsoleLaunchConfigurationTab_title_grep_console;
	}
	
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage()
	{
		return Activator.getDefault().getImageRegistry().get(Activator.IMG_LOGO_SMALL);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		Composite root = new Composite(parent, SWT.NONE);
		setControl(root);
		
		GridLayout layout = new GridLayout(1, false);
		root.setLayout(layout);

		treePanel = new ItemsTreePanel(root, SWT.BORDER);
		treePanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treePanel.addListener(this);

		btnConfigure = new Button(root, SWT.PUSH);
		btnConfigure.setText(Messages.GrepConsoleLaunchConfigurationTab_configure_expressions);
		btnConfigure.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 1, 1));
		btnConfigure.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doConfigureExpressions();
			}
		});
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy)
	{
		super.activated(workingCopy);
		
		WhatsNewDialog.showIfEnabled(getShell());
	}
	
	/**
	 * Opens the manage expressions dialog.
	 */
	protected void doConfigureExpressions()
	{
		Activator activator = Activator.getDefault();
		GrepExpressionRootFolder activatorExpressions = activator.getExpressions();

		GrepExpressionsWithSelection expressions = new GrepExpressionsWithSelection(activatorExpressions.copy(true), null, null, null, null);
		
		ItemsAndStylesDialog dlg = new ItemsAndStylesDialog(getShell());
		dlg.setExpressions(expressions);
		dlg.setLaunchConfiguration(configuration);
		
		if(dlg.open() == ItemsAndStylesDialog.OK)
		{
			activatorExpressions.copyFrom(expressions.getRootFolder(), true);
			treePanel.refresh();
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		this.configuration = configuration;
		
		Activator activator = Activator.getDefault();
		
		try
		{
			try
			{
				enablementMap = GrepConsoleUtil.loadIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_DISABLED_IDS, configuration);
			}
			catch(CoreException ex)
			{
				enablementMap = new HashMap<String, Boolean>();
				throw ex;
			}
			
			try
			{
				filterMap = GrepConsoleUtil.loadIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_FILTER_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_FILTER_DISABLED_IDS, configuration);
			}
			catch(CoreException ex)
			{
				filterMap = new HashMap<String, Boolean>();
				throw ex;
			}
			
			try
			{
				statisticsMap = GrepConsoleUtil.loadIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_STATISTICS_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_STATISTICS_DISABLED_IDS, configuration);
			}
			catch(CoreException ex)
			{
				statisticsMap = new HashMap<String, Boolean>();
				throw ex;
			}
			
			try
			{
				notificationsMap = GrepConsoleUtil.loadIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_NOTIFICATIONS_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_NOTIFICATIONS_DISABLED_IDS, configuration);
			}
			catch(CoreException ex)
			{
				notificationsMap = new HashMap<String, Boolean>();
				throw ex;
			}
		}
		catch(CoreException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_read_launch_configuration, ex);
		}
		
		treePanel.setExpressions(new GrepExpressionsWithSelection(activator.getExpressions(), enablementMap, filterMap, statisticsMap, notificationsMap));
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		GrepConsoleUtil.storeIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_DISABLED_IDS, enablementMap, configuration);
		GrepConsoleUtil.storeIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_FILTER_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_FILTER_DISABLED_IDS, filterMap, configuration);
		GrepConsoleUtil.storeIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_STATISTICS_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_STATISTICS_DISABLED_IDS, statisticsMap, configuration);
	}

	/**
	 * Updates the dialog when enabled expressions have changed.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IEnabledExpressionsListener#enabledExpressionsChanged()
	 */
	@Override
	public void enabledExpressionsChanged()
	{
		updateLaunchConfigurationDialog();
	}

	/**
	 * Updates the dialog when filtered expressions have changed.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#filterExpressionsChanged()
	 */
	@Override
	public void filterExpressionsChanged()
	{
		updateLaunchConfigurationDialog();
	}

	/**
	 * Updates the dialog when statistics expressions have changed.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#statisticsExpressionsChanged()
	 */
	@Override
	public void statisticsExpressionsChanged()
	{
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Updates the dialog when notifications expressions have changed.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#notificationsExpressionsChanged()
	 */
	@Override
	public void notificationsExpressionsChanged()
	{
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#elementDoubleClicked(name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement)
	 */
	@Override
	public void elementDoubleClicked(AbstractGrepModelElement element)
	{
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#elementSelectionChanged(java.util.Set)
	 */
	@Override
	public void elementSelectionChanged(Set<AbstractGrepModelElement> elements)
	{
	}
}
