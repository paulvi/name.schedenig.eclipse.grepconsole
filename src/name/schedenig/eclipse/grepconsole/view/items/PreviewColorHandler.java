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

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Provides colours for the console preview and updates them automatically when
 * the preferences change.
 * 
 * Also adds a menu to the preview control letting the user toggle between
 * console and Grep View colours.
 *  
 * @author msched
 */
public class PreviewColorHandler implements IPropertyChangeListener
{
	/** Whether to use console or Grep View colours for the preview. */
	public static enum ColorChoice { CONSOLE, GREP_VIEW };
	
	/** Name of the preference entry that stores which stores the ColorChoice
	 *  setting. */  
	public static final String PREF_PREVIEW_COLORS = "previewColors"; //$NON-NLS-1$
	
	/** Control for which the preview colours are handled. */
	private Control control;

	/** Whether to use console or Grep View colours for the preview. */
	private ColorChoice choice = ColorChoice.CONSOLE;
	
	/** Stores the used colour instances. */
	
	private ColorRegistry colorRegistry;
	/** GUI variables. */
	private Menu parentMenu;
	private MenuItem miConsole;
	private MenuItem miGrepView;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param control Preview control.
	 */
	public PreviewColorHandler(Control control)
	{
		this(control, null);
	}
	
	/**
	 * Creates a new instance for a control with a menu.
	 * 
	 * @param control Preview control.
	 * @param parentMenu Menu item to which the colour control menus should be
	 * 		added.
	 */
	public PreviewColorHandler(Control control, Menu parentMenu)
	{
		this.control = control;
		this.parentMenu = parentMenu;
		
		init();
	}
	
	/**
	 * Initialises the GUI.
	 */
	private void init()
	{
		final IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		
		colorRegistry = new ColorRegistry(Activator.getDefault().getColorRegistry());

		control.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				prefStore.removePropertyChangeListener(PreviewColorHandler.this);
				colorRegistry.disposeColors();
			}
		});

		prefStore.addPropertyChangeListener(this);
		
		Menu menu;
		
		if(parentMenu != null)
		{
			MenuItem miColors = new MenuItem(parentMenu, SWT.CASCADE);
			miColors.setText(Messages.PreviewColorHandler_menu_colors);
			
			menu = new Menu(control.getShell(), SWT.DROP_DOWN);
			miColors.setMenu(menu);
		}
		else
		{
			menu = new Menu(control);
			control.setMenu(menu);
		}
		
		miConsole = new MenuItem(menu, SWT.RADIO);
		miConsole.setText(Messages.PreviewColorHandler_menu_colors_console);
		miConsole.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setChoice(ColorChoice.CONSOLE);
			}
		});
		
		
		miGrepView = new MenuItem(menu, SWT.RADIO);
		miGrepView.setText(Messages.PreviewColorHandler_menu_colors_grepview);
		miGrepView.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setChoice(ColorChoice.GREP_VIEW);
			}
		});
		
		loadSettings();
		refresh();
	}
	
	/**
	 * Updates the preview control colours and the menu.
	 */
	private void refresh()
	{
		switch(choice)
		{
			case CONSOLE:
				control.setBackground(colorRegistry.get(GrepConsoleUtil.getConsoleBackgroundColor()));
				control.setForeground(colorRegistry.get(GrepConsoleUtil.getConsoleTextColor()));
				break;
				
			case GREP_VIEW:
				control.setBackground(colorRegistry.get(GrepConsoleUtil.getGrepViewBackgroundColor()));
				control.setForeground(colorRegistry.get(GrepConsoleUtil.getGrepViewTextColor()));
				break;
		}

		miConsole.setSelection(choice == ColorChoice.CONSOLE);
		miGrepView.setSelection(choice == ColorChoice.GREP_VIEW);
	}

	/**
	 * Returns the current preview colour choice.
	 * 
	 * @return Colour choice.
	 */
	public ColorChoice getChoice()
	{
		return choice;
	}

	/**
	 * Sets the preview colour choice.
	 * 
	 * @param choice New colour choice.
	 */
	public void setChoice(ColorChoice choice)
	{
		if(choice != this.choice)
		{
			this.choice = choice;
			refresh();
			
			saveSettings();
		}
	}

	/**
	 * Saves the preview colour choice setting to the preferences.
	 */
	private void saveSettings()
	{
		Activator.getDefault().getPreferenceStore().setValue(PREF_PREVIEW_COLORS, choice.toString());
	}
	
	/**
	 * Loads the preview colour choice setting from the preferences.
	 */
	private void loadSettings()
	{
		String s = Activator.getDefault().getPreferenceStore().getString(PREF_PREVIEW_COLORS);
		
		if(s != null && s.length() > 0)
		{
			try
			{
				choice = ColorChoice.valueOf(s);
			}
			catch(IllegalArgumentException ex)
			{
				Activator.getDefault().log(IStatus.WARNING, "Could not read preview color configuration.", ex); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Updates the colours when the preferences change.
	 * 
	 * @param event Property change event.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if(PREF_PREVIEW_COLORS.equals(event.getProperty()))
		{
			loadSettings();
			refresh();
		}
	}
}
