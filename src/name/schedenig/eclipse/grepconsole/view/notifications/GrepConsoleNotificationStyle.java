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

package name.schedenig.eclipse.grepconsole.view.notifications;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener;
import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.popupnotifications.INotificationStyle;
import name.schedenig.eclipse.popupnotifications.IPopupNotificationManagerListener;
import name.schedenig.eclipse.popupnotifications.PopupNotificationManager;
import name.schedenig.eclipse.popupnotifications.ResourceManagingNotificationStyle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * A notification style tied to the Grep Console resource management.
 * 
 * @author msched
 */
public class GrepConsoleNotificationStyle extends ResourceManagingNotificationStyle implements IGrepConsoleListener, IPopupNotificationManagerListener
{
	/** Plug-in. */
	private Activator activator;
	
	/** Notification manager instance. */
	private PopupNotificationManager manager;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent style. Should be the manager's original default style.
	 * @param activator Plug-in instance.
	 * @param manager Notification manager.
	 */
	public GrepConsoleNotificationStyle(INotificationStyle parent, Activator activator, PopupNotificationManager manager)
	{
		super(parent);
		
		this.activator = activator;
		this.manager = manager;
		
		activator.addListener(this);
		manager.addListener(this);
		
		refresh();
	}

	/**
	 * Releases listeners on dispose.
	 */
	@Override
	public void dispose()
	{
		manager.removeListener(this);
		activator.removeListener(this);
		
		super.dispose();
	}
	
	/**
	 * Refreshes the style's properties based on the current preferences.
	 */
	public void refresh()
	{
		Display display = manager.getDisplay();
		RGB rgb = GrepConsoleUtil.getNotificationForegroundColor();
		
		if(rgb != null)
		{
			setForeground(new Color(display, rgb), true);
		}
		else
		{
			setForeground(null, false);
		}
		
		rgb = GrepConsoleUtil.getNotificationBackgroundColor();
		
		if(rgb != null)
		{
			setBackground(new Color(display, rgb), true);
		}
		else
		{
			setBackground(null, false);
		}
		
		IPreferenceStore prefs = activator.getPreferenceStore();
		
		if(prefs.getString(Activator.PREFS_NOTIFICATION_TITLE_FONT).length() == 0)
		{
			setTitleFont(JFaceResources.getBannerFont(), false);
		}
		else
		{
			Font titleFont = new Font(display, PreferenceConverter.getFontData(prefs, Activator.PREFS_NOTIFICATION_TITLE_FONT));
			setTitleFont(titleFont, true);
		}
		
		if(prefs.getString(Activator.PREFS_NOTIFICATION_MESSAGE_FONT).length() == 0)
		{
			setMessageFont(JFaceResources.getDialogFont(), false);
		}
		else
		{
			Font messageFont = new Font(display, PreferenceConverter.getFontData(prefs, Activator.PREFS_NOTIFICATION_MESSAGE_FONT));
			setMessageFont(messageFont, true);
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
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#participantActivated(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	public void participantActivated(GrepPageParticipant participant)
	{
	}

	/**
	 * Refreshes the style's settings when the preferences change.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#settingsChanged()
	 */
	@Override
	public void settingsChanged()
	{
		manager.getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if(!manager.isDisposed())
				{
					refresh();
				}
			}
		});
	}

	/**
	 * Disposes the style when the notification manager is disposed.
	 * 
	 * @see name.schedenig.eclipse.popupnotifications.IPopupNotificationManagerListener#disposed(name.schedenig.eclipse.popupnotifications.PopupNotificationManager)
	 */
	@Override
	public void disposed(PopupNotificationManager manager)
	{
		dispose();
	}
}
