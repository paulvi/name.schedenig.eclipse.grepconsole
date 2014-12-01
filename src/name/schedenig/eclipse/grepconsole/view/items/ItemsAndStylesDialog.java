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

package name.schedenig.eclipse.grepconsole.view.items;

import java.text.MessageFormat;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.DefaultSizeTitleAreaDialog;
import name.schedenig.eclipse.grepconsole.view.styles.StylesPanel;
import name.schedenig.eclipse.grepconsole.view.whatsnew.WhatsNewDialog;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;

/**
 * A modal dialog displaying an tab container with an editable items and a
 * styles page.
 * 
 * The dialog may be used to edit the settings associated with a launch
 * configuration or the global config.
 * 
 * @author msched
 */
public class ItemsAndStylesDialog extends DefaultSizeTitleAreaDialog
{
	/** Dialog settings section key. */
	public static final String DIALOG_SETTINGS_SECTION = "itemsDialog"; //$NON-NLS-1$
	
	/** Dialog settings key for sash position. */
	public static final String DIALOG_SETTINGS_SASH_POSITION = "sashPosition"; //$NON-NLS-1$

	/** Model root element. */
	private GrepExpressionsWithSelection expressions;

	/** Text to be displayed in the preview panel. */
	private String previewText;
	
	/** Launch configuration being edited (if any). */
	private ILaunchConfiguration launchConfig;
	
	/** GUI variables. */
	private EditableItemsPanel panelExpressions;
	private TabFolder tabFolder;
	private TabItem tiExpressions;
	private TabItem tiStyles;
	private StylesPanel panelStyles;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell Parent shell.
	 */
	public ItemsAndStylesDialog(Shell parentShell)
	{
		super(parentShell);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create()
	{
		super.create();

		// We do this here after the bounds have been initialised
		panelExpressions.setExpressions(expressions);
		panelExpressions.setPreviewText(previewText);
		panelStyles.setRoot(expressions.getRootFolder());
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
		
		newShell.setText(Messages.ItemsAndStylesDialog_title_manage_expressions);
		newShell.addShellListener(new WhatsNewDialog.ShellListener());
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, Activator.PLUGIN_ID + ".manage_expressions"); //$NON-NLS-1$
		
		Control content = super.createContents(parent);
		
		setTitle(Messages.ItemsAndStylesDialog_title_manage_expressions);
		setTitleImage(Activator.getDefault().getImageRegistry().get(Activator.IMG_LOGO_LARGE));
		
		updateMessage();
		
		return content;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Dialog.applyDialogFont(parent);

		setHelpAvailable(true);
		
		tabFolder = new TabFolder(parent, SWT.TOP);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tiExpressions = new TabItem(tabFolder, SWT.NONE);
		tiExpressions.setText(Messages.ItemsAndStylesDialog_expressions);
		panelExpressions = createItemsPanel(tabFolder);
		tiExpressions.setControl(panelExpressions);

		tiStyles = new TabItem(tabFolder, SWT.NONE);
		tiStyles.setText(Messages.ItemsAndStylesDialog_styles);
		panelStyles = createStylesPanel(tabFolder);
		tiStyles.setControl(panelStyles);
		
		tabFolder.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if(tabFolder.getSelection()[0] == tiExpressions)
				{
					panelExpressions.refresh();
				}
				else
				{
					panelStyles.refresh();
				}
			}
		});

		return tabFolder;
	}

	/**
	 * Creates the items panel.
	 * 
	 * @param parent Parent component.
	 * 
	 * @return Items panel.
	 */
	private EditableItemsPanel createItemsPanel(TabFolder parent)
	{
		EditableItemsPanel panel = new EditableItemsPanel(parent, SWT.NONE);
		return panel;
	}

	/**
	 * Creates the styles panel.
	 * 
	 * @param parent Parent component.
	 * 
	 * @return Styles panel.
	 */
	private StylesPanel createStylesPanel(TabFolder parent)
	{
		StylesPanel panel = new StylesPanel(parent, SWT.NONE);
		return panel;
	}

	/**
	 * Sets the model root to be used.
	 * 
	 * @param expressions Model root.
	 */
	public void setExpressions(GrepExpressionsWithSelection expressions)
	{
		this.expressions = expressions;
	}
	
	/**
	 * Returns the used model root.
	 * 
	 * @return Model root.
	 */
	public GrepExpressionsWithSelection getExpressions()
	{
		return expressions;
	}
	
	/**
	 * Returns the items panel.
	 * 
	 * @return Items panel.
	 */
	public EditableItemsPanel getItemsPanel()
	{
		return panelExpressions;
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
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close()
	{
		Integer sashPosition = panelExpressions.getSashPosition();
		
		if(sashPosition != null)
		{
			getDialogBoundsSettings().put(DIALOG_SETTINGS_SASH_POSITION, sashPosition);
		}
		
		return super.close();
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.view.DefaultSizeTitleAreaDialog#getDefaultSize()
	 */
	protected Point getDefaultSize()
	{
		return GrepConsoleUtil.charsToPixelDimensions(getShell(), 90, 35);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
	 */
	@Override
	protected void initializeBounds()
	{
		super.initializeBounds();

		Shell shell = getShell();
		shell.setMinimumSize(GrepConsoleUtil.charsToPixelDimensions(shell, 60, 28));
		
		IDialogSettings settings = getDialogBoundsSettings();
		
		if(settings.get(DIALOG_SETTINGS_SASH_POSITION) != null)
		{
			panelExpressions.setSashPosition(settings.getInt(DIALOG_SETTINGS_SASH_POSITION));
		}
	}

	/**
	 * Sets the text to be displayed in the preview panel.
	 * 
	 * @param text Preview text.
	 */
	public void setPreviewText(String text)
	{
		previewText = text;
		
		if(panelExpressions != null)
		{
			panelExpressions.setPreviewText(text);
		}
	}
	
	/**
	 * Returns the text displayed in the preview panel.
	 * 
	 * @return Preview text.
	 */
	public String getPreviewText()
	{
		return previewText;
	}

	/**
	 * Sets the launch configuration being edited (if any).
	 * 
	 * @param launchConfig Launch configuration. May be <code>null</code>.
	 */
	public void setLaunchConfiguration(ILaunchConfiguration launchConfig)
	{
		this.launchConfig = launchConfig;
		
		if(panelExpressions != null)
		{
			updateMessage();
		}
	}
	
	/**
	 * Updates the header message explaining which settings are being edited.
	 */
	public void updateMessage()
	{
		if(launchConfig == null)
		{
			setMessage(Messages.ItemsAndStylesDialog_check_boxes_global, IMessageProvider.INFORMATION);
		}
		else
		{
			setMessage(MessageFormat.format(Messages.ItemsAndStylesDialog_check_boxes_launch_config, launchConfig.getName()), IMessageProvider.INFORMATION);
		}
	}
}
