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

package name.schedenig.eclipse.grepconsole.view.common;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener;
import name.schedenig.eclipse.grepconsole.actions.LinkedToConsoleAction;
import name.schedenig.eclipse.grepconsole.actions.SelectConsoleAction;
import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant.IGrepPageParticipantListener;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * 
 * @author msched
 */
public class GrepConsoleView extends ViewPart implements IGrepConsoleListener, IGrepPageParticipantListener, IPropertyChangeListener
{
	// --- Memento keys --- 
	private static final String KEY_LINK_TO_CONSOLE = "linkToConsole"; //$NON-NLS-1$

	/** Console to which the view is currently bound. May be <code>null</code>. */
	private IConsole console;

	/** Whether the displayed console is linked to the console view. */
	private boolean linkedToConsole = true;

	/** Console link action instance. */
	private LinkedToConsoleAction linkedToConsoleAction;

	/** Console selection action instance. */
	private SelectConsoleAction selectConsoleAction;

	private GrepPageParticipant participant;

	/**
	 * Creates a new instance.
	 */
	public GrepConsoleView()
	{
		Activator.getDefault().addListener(this);
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose()
	{
		Activator activator = Activator.getDefault();
		
		if(participant != null)
		{
			participant.removeListener(this);
		}
		
		activator.getPreferenceStore().removePropertyChangeListener(this);
		activator.removeListener(this);
		
		ScopedPreferenceStore debugUiPrefs = GrepConsoleUtil.getDebugUiPreferences();
		
		if(debugUiPrefs != null)
		{
			debugUiPrefs.removePropertyChangeListener(this);
		}

		/*
		if(colorRegistry != null)
		{
			colorRegistry.disposeColors();
			colorRegistry = null;
		}
		*/
		
		super.dispose();
	}

	/**
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);

		IToolBarManager toolbar = site.getActionBars().getToolBarManager();
		
		linkedToConsoleAction = new LinkedToConsoleAction(this);
		toolbar.add(linkedToConsoleAction);

		selectConsoleAction = new SelectConsoleAction(this);
		toolbar.add(selectConsoleAction);
	}

	/**
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		super.init(site, memento);
		
		linkedToConsole = getMementoBoolean(memento, KEY_LINK_TO_CONSOLE, linkedToConsole);
		linkedToConsoleAction.setChecked(linkedToConsole);
	}
	
	/**
	 * Reads a boolean value from a memento.
	 * 
	 * @param memento Memento.
	 * @param key Key of the value to read.
	 * @param defaultValue Value to use if key is not found in memento.
	 *  
	 * @return Boolean value.
	 */
	protected boolean getMementoBoolean(IMemento memento, String key, boolean defaultValue)
	{
		if(memento == null)
		{
			return defaultValue;
		}
		
		Boolean b = memento.getBoolean(key);
		
		if(b == null)
		{
			return defaultValue;
		}
		else
		{
			return b;
		}
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#participantAdded(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	public void participantAdded(GrepPageParticipant participant)
	{
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#participantRemoved(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	public void participantRemoved(GrepPageParticipant participant)
	{
		if(this.participant == participant)
		{
			setConsole(null);
		}
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#participantActivated(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	public void participantActivated(GrepPageParticipant participant)
	{
		if(linkedToConsole || console == null)
		{
			if(participant == null)
			{
				setConsole(null);
			}
			else
			{
				setConsole(participant.getConsole());
			}
		}
	}
	
	/**
	 * Sets the current console. As a result, also updates the participant,
	 * content and line style listener.
	 *  
	 * @param console Console.
	 */
	public void setConsole(IConsole console)
	{
		this.console = console;
		setParticipant(console == null ? null : Activator.getDefault().getParticipant(console));
	}
	
	protected void setParticipant(GrepPageParticipant participant)
	{
		this.participant = participant;
	}
	
	/**
	 * Returns the current console.
	 * 
	 * @return Console.
	 */
	public IConsole getConsole()
	{
		return console;
	}
	
	/**
	 * Sets the linked to console flag.
	 * 
	 * @param Whether the currently displayed console content should be linked to
	 * 		the currently active console.
	 */
	public void setLinkedToConsole(boolean linkedToConsole)
	{
		this.linkedToConsole = linkedToConsole;
	}
	
	/**
	 * Returns the linked to console flag.
	 * 
	 * @return Whether the currently displayed console content is linked to
	 * 		the currently active console.
	 */
	public boolean isLinkedToConsole()
	{
		return linkedToConsole;
	}

	/**
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento)
	{
		super.saveState(memento);
		
		memento.putBoolean(KEY_LINK_TO_CONSOLE, linkedToConsole);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
	}

	/* (non-Javadoc)
	 * @see name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant.IGrepPageParticipantListener#participantChanged(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	public void participantChanged(GrepPageParticipant grepPageParticipant)
	{
	}

	/* (non-Javadoc)
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#settingsChanged()
	 */
	@Override
	public void settingsChanged()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent)
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
	}
	
	/**
	 * @return the participant
	 */
	public GrepPageParticipant getParticipant()
	{
		return participant;
	}
}
