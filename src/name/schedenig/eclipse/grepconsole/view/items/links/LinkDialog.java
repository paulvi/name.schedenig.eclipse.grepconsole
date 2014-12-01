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

import java.util.HashMap;
import java.util.Map;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.CommandLink;
import name.schedenig.eclipse.grepconsole.model.links.FileLink;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;
import name.schedenig.eclipse.grepconsole.model.links.JavaLink;
import name.schedenig.eclipse.grepconsole.model.links.ScriptLink;
import name.schedenig.eclipse.grepconsole.model.links.UrlLink;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;
import name.schedenig.eclipse.grepconsole.view.DefaultSizeDialog;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A dialog for editing a link. The user may change the link type in the dialog,
 * so new link instances are created when necessary and the returned link
 * instance may not be the same as the one the dialog was opened with.
 * 
 * To edit the actual links, subclasses of LinkPanel are used.
 * 
 * @author msched
 */
public class LinkDialog extends DefaultSizeDialog
{
	/** Dialog settings section key. */
	public static final String DIALOG_SETTINGS_SECTION = "linkDialog"; //$NON-NLS-1$
	
	/** Maps link classes to display names. */
	public static final Map<Class<? extends IGrepLink>, String> TYPE_NAMES;
	
	/** Current link. */
	private IGrepLink link;

	/** Remembers one link instance per link type, so that it can be reactivated
	 *  if the user switches back to that type. */
	private Map<Class<? extends IGrepLink>, IGrepLink> cachedLinks = new HashMap<Class<? extends IGrepLink>, IGrepLink>();
	
	/** Maps link classes to the panel instance used to edit links of that
	 *  class. */
	private Map<Class<? extends IGrepLink>, LinkPanel> panels = new HashMap<Class<? extends IGrepLink>, LinkPanel>();

	/** Whether capture group information is available for the link. */
	private boolean withCaptureGroup;
	
	// --- GUI variables ---
	private Label labelType;
	private Combo comboType;
	private ComboViewer viewerType;
	private UrlLinkPanel panelUrl;
	private ScriptLinkPanel panelScript;
	private Composite panelLinkSpecific;
	private FileLinkPanel panelFile;
	private JavaLinkPanel panelJava;
	private CommandLinkPanel panelCommand;

	/**
	 * Static initialisations.
	 */
	static
	{
		TYPE_NAMES = new HashMap<Class<? extends IGrepLink>, String>();
		TYPE_NAMES.put(NoLink.class, Messages.LinkDialog_no_link);
		TYPE_NAMES.put(FileLink.class, Messages.LinkDialog_link_file);
		TYPE_NAMES.put(JavaLink.class, Messages.LinkDialog_link_java_type);
		TYPE_NAMES.put(UrlLink.class, Messages.LinkDialog_link_url);
		TYPE_NAMES.put(ScriptLink.class, Messages.LinkDialog_link_script);
		TYPE_NAMES.put(CommandLink.class, Messages.LinkDialog_link_command);
	}

	/**
	 * A helper class representing no link.
	 *  
	 * @author msched
	 */
	public static abstract class NoLink implements IGrepLink
	{
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell Parent shell.
	 */
	public LinkDialog(Shell parentShell, boolean withCaptureGroup)
	{
		super(parentShell);
		
		this.withCaptureGroup = withCaptureGroup;
		
		cachedLinks.put(NoLink.class, null);
		cachedLinks.put(FileLink.class, new FileLink());
		cachedLinks.put(JavaLink.class, new JavaLink());
		cachedLinks.put(UrlLink.class, new UrlLink());
		cachedLinks.put(ScriptLink.class, new ScriptLink());
		cachedLinks.put(CommandLink.class, new CommandLink());
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
	 * @see name.schedenig.eclipse.grepconsole.view.DefaultSizeTitleAreaDialog#getDefaultSize()
	 */
	protected Point getDefaultSize()
	{
		Shell shell = getShell();
		return GrepConsoleUtil.charsToPixelDimensions(shell, 60, 15);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
	 */
	@Override
	protected void initializeBounds()
	{
		super.initializeBounds();
		
		Shell shell = getShell();
		shell.setMinimumSize(GrepConsoleUtil.charsToPixelDimensions(shell, 60, 18));
	}

	/**
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	protected int getShellStyle()
	{
		return super.getShellStyle() | SWT.RESIZE;
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		
		newShell.setText(Messages.LinkDialog_edit_link);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, Activator.PLUGIN_ID + ".edit_link"); //$NON-NLS-1$

		Composite composite = new Composite(parent, SWT.NONE);
		new GridLayoutBuilder(composite, 2, false).apply();
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		labelType = new Label(composite, SWT.NONE);
		labelType.setText(Messages.LinkDialog_type);
		labelType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		comboType = new Combo(composite, SWT.READ_ONLY);
		comboType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		viewerType = new ComboViewer(comboType);
		viewerType.setContentProvider(new ArrayContentProvider());
		viewerType.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				String name = TYPE_NAMES.get(element);
				
				return name == null ? String.valueOf(element) : name;
			}
		});
		
		viewerType.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				@SuppressWarnings("unchecked")
				Class<? extends IGrepLink> type = (Class<? extends IGrepLink>) ((IStructuredSelection) event.getSelection()).getFirstElement();
				typeChanged(type);
			}
		});
		
		viewerType.setInput(new Class[]{NoLink.class, FileLink.class, JavaLink.class, UrlLink.class, CommandLink.class, ScriptLink.class});
		
		panelLinkSpecific = new Composite(composite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gridData.verticalIndent = 10;
		panelLinkSpecific.setLayoutData(gridData);
		StackLayout linkSpecificLayout = new StackLayout();
		linkSpecificLayout.marginWidth = linkSpecificLayout.marginHeight = 0;
		panelLinkSpecific.setLayout(linkSpecificLayout);
		
		panelFile = new FileLinkPanel(panelLinkSpecific, withCaptureGroup);
		panels.put(FileLink.class, panelFile);
		
		panelJava = new JavaLinkPanel(panelLinkSpecific, withCaptureGroup);
		panels.put(JavaLink.class, panelJava);
		
		panelUrl = new UrlLinkPanel(panelLinkSpecific, withCaptureGroup);
		panels.put(UrlLink.class, panelUrl);
		
		panelCommand = new CommandLinkPanel(panelLinkSpecific, withCaptureGroup);
		panels.put(CommandLink.class, panelCommand);
		
		panelScript = new ScriptLinkPanel(panelLinkSpecific, withCaptureGroup);
		panels.put(ScriptLink.class, panelScript);
		
		refreshContent(true);
		
		return composite;
	}

	/**
	 * Called when the selected link type is changed. Changes the link accordingly
	 * and updates the dialog's content.
	 * 
	 * @param type New link type.
	 */
	protected void typeChanged(Class<? extends IGrepLink> type)
	{
		link = cachedLinks.get(type);
		
		refreshContent(false);
	}

	/**
	 * Sets the current link.
	 * 
	 * @param link New link.
	 */
	public void setLink(IGrepLink link)
	{
		this.link = link == null ? null : link.copy();

		if(this.link != null)
		{
			cachedLinks.put(this.link.getClass(), this.link);
		}
		
		if(viewerType != null)
		{
			refreshContent(true);
		}
	}
	
	/**
	 * Refreshes the parent's content.
	 * 
	 * @param refreshCombo Whether to also refresh the link type selection
	 * 		combo box.
	 */
	private void refreshContent(boolean refreshCombo)
	{
		if(refreshCombo)
		{
			viewerType.setSelection(new StructuredSelection(link == null ? 
					NoLink.class : link.getClass()));
		}
		
		if(link != null)
		{
			LinkPanel panel = panels.get(link.getClass());
			panel.setLink(link);
			((StackLayout) panelLinkSpecific.getLayout()).topControl = panel;
			panelLinkSpecific.layout();
			panelLinkSpecific.setVisible(true);
		}
		else
		{
			panelLinkSpecific.setVisible(false);
		}
	}

	/**
	 * Returns the link being edited.
	 * 
	 * @return Link.
	 */
	public IGrepLink getLink()
	{
		return link;
	}
}
