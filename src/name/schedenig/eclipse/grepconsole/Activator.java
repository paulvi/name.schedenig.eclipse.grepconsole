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

package name.schedenig.eclipse.grepconsole;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.model.xml.LegacyXmlReader;
import name.schedenig.eclipse.grepconsole.model.xml.XmlHandler;
import name.schedenig.eclipse.grepconsole.model.xml.XmlHandlerException;
import name.schedenig.eclipse.grepconsole.util.SoundManager;
import name.schedenig.eclipse.grepconsole.view.colors.OwnerColorRegistry;
import name.schedenig.eclipse.grepconsole.view.notifications.GrepConsoleNotificationStyle;
import name.schedenig.eclipse.popupnotifications.INotificationStyle;
import name.schedenig.eclipse.popupnotifications.PopupNotificationManager;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{
	/**
	 * Interface for plug-in event listeners.
	 * 
	 * @author msched
	 */
	public static interface IGrepConsoleListener
	{
		/**
		 * Called when a new participant is registered.
		 * 
		 * @param participant New participant.
		 */
		public void participantAdded(GrepPageParticipant participant);
		
		/**
		 * Called when a participant is unregistered.
		 * 
		 * @param participant Participant.
		 */
		public void participantRemoved(GrepPageParticipant participant);
		
		/**
		 * Called when a participant is activated.
		 * 
		 * @param participant Participant.
		 */
		public void participantActivated(GrepPageParticipant participant);
		
		/**
		 * Called when the plug-in settings have changed.
		 */
		public void settingsChanged();
	}

	/** Plug-in ID. */
	public static final String PLUGIN_ID = "name.schedenig.eclipse.grepconsole"; //$NON-NLS-1$

	/** Legacy plug-in ID, used to load legacy settings. */
	public static final String LEGACY_PLUGIN_ID = "com.musgit.eclipse.grepconsole"; //$NON-NLS-1$

	/** Version number for What's New. */
	public static final int CURRENT_VERSION_NUMBER = 360;	

	/** Keys for plug-in preferences. */
	public static final String PREFS_EXPRESSIONS = "expressions"; //$NON-NLS-1$
	public static final String PREFS_SHOW_WHATS_NEW = "showWhatsNew"; //$NON-NLS-1$
	public static final String PREFS_PREVIEW = "preview"; //$NON-NLS-1$
	public static final String PREFS_STYLE_MATCH_LENGTH = "styleMatchLength"; //$NON-NLS-1$
	public static final String PREFS_FILTER_MATCH_LENGTH = "filterMatchLength"; //$NON-NLS-1$
	public static final String PREFS_GREP_VIEW_FOREGROUND_COLOR = "grepViewForegroundColor"; //$NON-NLS-1$
	public static final String PREFS_GREP_VIEW_BACKGROUND_COLOR = "grepViewBackgroundColor"; //$NON-NLS-1$
	public static final String PREFS_LINK_MODIFIER_KEY = "linkModifierKey"; //$NON-NLS-1$
	public static final String PREFS_LEGACY_SETTINGS = "settings"; //$NON-NLS-1$
	public static final String PREFS_NOTIFICATION_FOREGROUND_COLOR = "notificationForegroundColor"; //$NON-NLS-1$
	public static final String PREFS_NOTIFICATION_BACKGROUND_COLOR = "notificationBackgroundColor"; //$NON-NLS-1$
	public static final String PREFS_NOTIFICATION_TITLE_FONT = "notificationTitleFont"; //$NON-NLS-1$
	public static final String PREFS_NOTIFICATION_MESSAGE_FONT = "notificationMessageFont"; //$NON-NLS-1$
	
	/** Image ID constants. */
	public static final String IMG_LOGO_SMALL = "logo_small"; //$NON-NLS-1$
	public static final String IMG_LOGO_LARGE = "logo_large"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_ON = "checkbox_on"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_OFF = "checkbox_off"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_ON_GREYED = "checkbox_on_greyed"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_OFF_GREYED = "checkbox_off_greyed"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_ON_INHERITED = "checkbox_on_inherited"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_OFF_INHERITED = "checkbox_off_inherited"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_ON_INHERITED_GREYED = "checkbox_on_inherited_greyed"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_OFF_INHERITED_GREYED = "checkbox_off_inherited_greyed"; //$NON-NLS-1$
	public static final String IMG_FILTER_ON = "filter_on"; //$NON-NLS-1$
	public static final String IMG_FILTER_OFF = "filter_off"; //$NON-NLS-1$
	public static final String IMG_FILTER_ON_GREYED = "filter_on_greyed"; //$NON-NLS-1$
	public static final String IMG_FILTER_OFF_GREYED = "filter_off_greyed"; //$NON-NLS-1$
	public static final String IMG_FILTER_ON_INHERITED = "filter_on_inherited"; //$NON-NLS-1$
	public static final String IMG_FILTER_OFF_INHERITED = "filter_off_inherited"; //$NON-NLS-1$
	public static final String IMG_FILTER_ON_INHERITED_GREYED = "filter_on_inherited_greyed"; //$NON-NLS-1$
	public static final String IMG_FILTER_OFF_INHERITED_GREYED = "filter_off_inherited_greyed"; //$NON-NLS-1$
	public static final String IMG_STATISTICS_ON = "statistics_on"; //$NON-NLS-1$
	public static final String IMG_STATISTICS_OFF = "statistics_off"; //$NON-NLS-1$
	public static final String IMG_STATISTICS_ON_GREYED = "statistics_on_greyed"; //$NON-NLS-1$
	public static final String IMG_STATISTICS_OFF_GREYED = "statistics_off_greyed"; //$NON-NLS-1$
	public static final String IMG_STATISTICS_ON_INHERITED = "statistics_on_inherited"; //$NON-NLS-1$
	public static final String IMG_STATISTICS_OFF_INHERITED = "statistics_off_inherited"; //$NON-NLS-1$
	public static final String IMG_STATISTICS_ON_INHERITED_GREYED = "statistics_on_inherited_greyed"; //$NON-NLS-1$
	public static final String IMG_STATISTICS_OFF_INHERITED_GREYED = "statistics_off_inherited_greyed"; //$NON-NLS-1$
	public static final String IMG_NOTIFICATION_ON = "notification_on"; //$NON-NLS-1$
	public static final String IMG_NOTIFICATION_OFF = "notification_off"; //$NON-NLS-1$
	public static final String IMG_NOTIFICATION_ON_GREYED = "notification_on_greyed"; //$NON-NLS-1$
	public static final String IMG_NOTIFICATION_OFF_GREYED = "notification_off_greyed"; //$NON-NLS-1$
	public static final String IMG_NOTIFICATION_ON_INHERITED = "notification_on_inherited"; //$NON-NLS-1$
	public static final String IMG_NOTIFICATION_OFF_INHERITED = "notification_off_inherited"; //$NON-NLS-1$
	public static final String IMG_NOTIFICATION_ON_INHERITED_GREYED = "notification_on_inherited_greyed"; //$NON-NLS-1$
	public static final String IMG_NOTIFICATION_OFF_INHERITED_GREYED = "notification_off_inherited_greyed"; //$NON-NLS-1$
	public static final String IMG_ACTION_SELECT_CONSOLE = "action.select_console"; //$NON-NLS-1$
	public static final String IMG_ACTION_SCROLL_LOCK = "action.scroll_lock"; //$NON-NLS-1$
	public static final String IMG_ACTION_LINKED_TO_CONSOLE = "action.linked_to_console"; //$NON-NLS-1$
	public static final String IMG_PLAY = "play"; //$NON-NLS-1$

	/** Default text for preview panel. */
	private static final String DEFAULT_PREVIEW_TEXT = Messages.Activator_default_preview_text;

	/** Default style match length. */
	public static final int DEFAULT_STYLE_MATCH_LENGTH = 150;
	
	/** Default filter match length. */
	public static final int DEFAULT_FILTER_MATCH_LENGTH = 100;

	/** Number of sound samples to keep cached in memory. */
	private static final int SOUND_CACHE_SIZE = 3;
	
	/** Activator instance. */
	private static Activator plugin;

	/** Global colour registry. */
	private OwnerColorRegistry colorRegistry = new OwnerColorRegistry(null);
	
	/** Expressions used by the plug-in.*/
	private GrepExpressionRootFolder expressions;

	/** Keeps track of curently active participants (by console) */
	private Map<IConsole, GrepPageParticipant> participants = new LinkedHashMap<IConsole, GrepPageParticipant>();

	/** Listeners. Iterated in insertion order. */
	private Set<IGrepConsoleListener> listeners = new LinkedHashSet<Activator.IGrepConsoleListener>();

	/** Currently active participant. */
	private GrepPageParticipant activeParticipant;

	/** Keeps track of whether "show what's new" has already been called (and
	 *  therefore displayed as a dialog) during this life cycle of the plug-in. */
	private boolean showWhatsNewCalled = false;

	/** Whether a legacy warning should be displayed the next time Grep Console
	 *  opens a window. */
	private boolean displayLegacyWarning = false;

	/** Link mouse cursor. Created on demand. */
	private Cursor linkCursor;

//	/** Popup notification manager. Created on demand. */
//	private PopupNotificationManager popupNotificationManager;

	/** Sound manager. Created on demand. */
	private SoundManager soundManager;

	private GrepConsoleNotificationStyle notificationStyle;

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		displayLegacyWarning = Platform.getBundle(LEGACY_PLUGIN_ID) != null;
		
		try
		{
			loadSettings();
		}
		catch(XmlHandlerException ex)
		{
			log(IStatus.ERROR, "Could not read settings. Using defaults.", ex); //$NON-NLS-1$
		}

		if(expressions == null)
		{
			expressions = loadLegacySettings();
			
			if(expressions != null)
			{
				setShowWhatsNew(true);
			}
		}
		
		if(expressions == null)
		{
			expressions = loadDefaults();
		}

		plugin = this;
	}

	/**
	 * Displays a legacy warning, if necessary, and asks the user whether the
	 * legacy version of Grep Console should be uninstalled automatically.
	 * 
	 * @param shell Parent shell.
	 */
	public void displayLegacyWarningIfEnabled(Shell shell)
	{
		if(!displayLegacyWarning)
		{
			return;
		}
		
		displayLegacyWarning = false;
		
		Bundle bundle = Platform.getBundle(LEGACY_PLUGIN_ID);
		
		if(bundle == null)
		{
			return;
		}
		
		String msg = MessageFormat.format(Messages.Activator_legacy_version_message, bundle.getVersion(), getBundle().getVersion(), LEGACY_PLUGIN_ID);
		MessageDialog.openWarning(shell, Messages.Activator_legacy_version_title, msg);
	}
	
	/**
	 * Returns whether the "what's new" dialog may be displayed. The dialog may
	 * be displayed only if it has not been disabled in the preferences and if the
	 * method has not already been called before during the plug-in's life cycle.
	 * 
	 * @return <code>true</code> iff the dialog may be displayed.
	 */
	public boolean isShowWhatsNew()
	{
		if(showWhatsNewCalled)
		{
			return false;
		}
		
		showWhatsNewCalled = true;
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		
		return scope.getInt(PREFS_SHOW_WHATS_NEW, 0) < CURRENT_VERSION_NUMBER;
	}
	
	/**
	 * Stores whether the "what's new" dialog may be displayed again in the
	 * preferences. This has no influence on the fact that the dialog may only
	 * be displayed once per plug-in life cycle.
	 * 
	 * @param showWhatsNew Whether the dialog may be displayed again.
	 */
	public void setShowWhatsNew(boolean showWhatsNew)
	{
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		
		if(showWhatsNew)
		{
			scope.remove(PREFS_SHOW_WHATS_NEW);
		}
		else
		{
			scope.putInt(PREFS_SHOW_WHATS_NEW, CURRENT_VERSION_NUMBER);
		}
	}

	/**
	 * Loads plug-in settings from the preference store.
	 * 
	 * @throws XmlHandlerException
	 */
	private void loadSettings() throws XmlHandlerException
	{
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		String xmlString = scope.get(PREFS_EXPRESSIONS, null);

		if(xmlString == null || xmlString.length() == 0)
		{
			return;
		}

		XmlHandler handler = new XmlHandler();
		expressions = handler.readExpressions(xmlString);
	}

	/**
	 * Reads legacy (Grep Console 2.x) settings from the perference store.
	 * 
	 * @return Root folder constructed from legacy settings, if found.
	 * 		<code>null</code> otherwise.
	 *  
	 * @throws XmlHandlerException 
	 */
	private GrepExpressionRootFolder loadLegacySettings() throws XmlHandlerException
	{
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(LEGACY_PLUGIN_ID);
		String xmlString = scope.get(PREFS_LEGACY_SETTINGS, null);

		if(xmlString == null || xmlString.length() == 0)
		{
			return null;
		}

		LegacyXmlReader reader = new LegacyXmlReader();
		return reader.xmlStringToExpressions(xmlString);
	}

	/**
	 * Saves plug-in settings to the preference store.
	 * 
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws BackingStoreException
	 */
	public void saveSettings() throws ParserConfigurationException, TransformerException, BackingStoreException
	{
		XmlHandler handler = new XmlHandler();
		String xml = handler.createXmlString(expressions);

		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		scope.put(PREFS_EXPRESSIONS, xml);
		scope.flush();
	}

	/**
	 * Loads styles and elements from defaults.
	 * 
	 * @return Default root folder.
	 */
	public GrepExpressionRootFolder loadDefaults()
	{
		XmlHandler handler = new XmlHandler();

		try
		{
			return handler.readExpressions(Activator.class.getResourceAsStream("resources/defaultExpressions.xml")); //$NON-NLS-1$
		}
		catch(XmlHandlerException ex)
		{
			log(IStatus.ERROR, "Could not load defaults.", ex); //$NON-NLS-1$

			return new GrepExpressionRootFolder();
		}
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public synchronized void stop(BundleContext context) throws Exception
	{
		if(soundManager != null)
		{
			soundManager.dispose();
			soundManager = null;
		}
		
//		if(popupNotificationManager != null)
//		{
//			popupNotificationManager.dispose();
//			popupNotificationManager = null;
//		}
		
		if(linkCursor != null)
		{
			linkCursor.dispose();
			linkCursor = null;
		}
		
		colorRegistry.disposeColors();
		plugin = null;

		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return Activator instance.
	 */
	public static Activator getDefault()
	{
		return plugin;
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry registry)
	{
		super.initializeImageRegistry(registry);

		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		
		registry.put(IMG_LOGO_SMALL, createImageDescriptor(bundle, "logo_16.png")); //$NON-NLS-1$
		registry.put(IMG_LOGO_LARGE, createImageDescriptor(bundle, "logo_64.png")); //$NON-NLS-1$
		registry.put(IMG_CHECKBOX_ON, createImageDescriptor(bundle, "checkbox_on.png")); //$NON-NLS-1$
		registry.put(IMG_CHECKBOX_OFF, createImageDescriptor(bundle, "checkbox_off.png")); //$NON-NLS-1$
		registry.put(IMG_CHECKBOX_ON_GREYED, createImageDescriptor(bundle, "checkbox_on_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_CHECKBOX_OFF_GREYED, createImageDescriptor(bundle, "checkbox_off_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_CHECKBOX_ON_INHERITED, createImageDescriptor(bundle, "checkbox_on_inherited.png")); //$NON-NLS-1$
		registry.put(IMG_CHECKBOX_OFF_INHERITED, createImageDescriptor(bundle, "checkbox_off_inherited.png")); //$NON-NLS-1$
		registry.put(IMG_CHECKBOX_ON_INHERITED_GREYED, createImageDescriptor(bundle, "checkbox_on_inherited_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_CHECKBOX_OFF_INHERITED_GREYED, createImageDescriptor(bundle, "checkbox_off_inherited_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_FILTER_ON, createImageDescriptor(bundle, "filter_on.png")); //$NON-NLS-1$
		registry.put(IMG_FILTER_OFF, createImageDescriptor(bundle, "filter_off.png")); //$NON-NLS-1$
		registry.put(IMG_FILTER_ON_GREYED, createImageDescriptor(bundle, "filter_on_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_FILTER_OFF_GREYED, createImageDescriptor(bundle, "filter_off_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_FILTER_ON_INHERITED, createImageDescriptor(bundle, "filter_on_inherited.png")); //$NON-NLS-1$
		registry.put(IMG_FILTER_OFF_INHERITED, createImageDescriptor(bundle, "filter_off_inherited.png")); //$NON-NLS-1$
		registry.put(IMG_FILTER_ON_INHERITED_GREYED, createImageDescriptor(bundle, "filter_on_inherited_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_FILTER_OFF_INHERITED_GREYED, createImageDescriptor(bundle, "filter_off_inherited_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_STATISTICS_ON, createImageDescriptor(bundle, "statistics_on.png")); //$NON-NLS-1$
		registry.put(IMG_STATISTICS_OFF, createImageDescriptor(bundle, "statistics_off.png")); //$NON-NLS-1$
		registry.put(IMG_STATISTICS_ON_GREYED, createImageDescriptor(bundle, "statistics_on_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_STATISTICS_OFF_GREYED, createImageDescriptor(bundle, "statistics_off_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_STATISTICS_ON_INHERITED, createImageDescriptor(bundle, "statistics_on_inherited.png")); //$NON-NLS-1$
		registry.put(IMG_STATISTICS_OFF_INHERITED, createImageDescriptor(bundle, "statistics_off_inherited.png")); //$NON-NLS-1$
		registry.put(IMG_STATISTICS_ON_INHERITED_GREYED, createImageDescriptor(bundle, "statistics_on_inherited_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_STATISTICS_OFF_INHERITED_GREYED, createImageDescriptor(bundle, "statistics_off_inherited_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_ACTION_SELECT_CONSOLE, createImageDescriptor(bundle, "eclipse/console_view.gif")); //$NON-NLS-1$
		registry.put(IMG_ACTION_SCROLL_LOCK, createImageDescriptor(bundle, "eclipse/lock_co.gif")); //$NON-NLS-1$
		registry.put(IMG_ACTION_LINKED_TO_CONSOLE, createImageDescriptor(bundle, "eclipse/writeout_co.gif")); //$NON-NLS-1$
		registry.put(IMG_NOTIFICATION_ON, createImageDescriptor(bundle, "notification_on.png")); //$NON-NLS-1$
		registry.put(IMG_NOTIFICATION_OFF, createImageDescriptor(bundle, "notification_off.png")); //$NON-NLS-1$
		registry.put(IMG_NOTIFICATION_ON_GREYED, createImageDescriptor(bundle, "notification_on_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_NOTIFICATION_OFF_GREYED, createImageDescriptor(bundle, "notification_off_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_NOTIFICATION_ON_INHERITED, createImageDescriptor(bundle, "notification_on_inherited.png")); //$NON-NLS-1$
		registry.put(IMG_NOTIFICATION_OFF_INHERITED, createImageDescriptor(bundle, "notification_off_inherited.png")); //$NON-NLS-1$
		registry.put(IMG_NOTIFICATION_ON_INHERITED_GREYED, createImageDescriptor(bundle, "notification_on_inherited_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_NOTIFICATION_OFF_INHERITED_GREYED, createImageDescriptor(bundle, "notification_off_inherited_greyed.png")); //$NON-NLS-1$
		registry.put(IMG_ACTION_SELECT_CONSOLE, createImageDescriptor(bundle, "eclipse/console_view.gif")); //$NON-NLS-1$
		registry.put(IMG_ACTION_SCROLL_LOCK, createImageDescriptor(bundle, "eclipse/lock_co.gif")); //$NON-NLS-1$
		registry.put(IMG_ACTION_LINKED_TO_CONSOLE, createImageDescriptor(bundle, "eclipse/writeout_co.gif")); //$NON-NLS-1$
		registry.put(IMG_PLAY, createImageDescriptor(bundle, "eclipse/start_task.gif")); //$NON-NLS-1$
	}

	/**
	 * Creates an image descriptor for the specified bundle, using the icons base
	 * path and the specified relative path.
	 * 
	 * @param bundle Bundle.
	 * @param path Image path, relative to icons base path.
	 * 
	 * @return Image descriptor.
	 */
	private ImageDescriptor createImageDescriptor(Bundle bundle, String path)
	{
		URL url = FileLocator.find(bundle, new Path("icons/" + path), null); //$NON-NLS-1$
		return ImageDescriptor.createFromURL(url);
	}

	/**
	 * Returns the model tree root folder.
	 * 
	 * @return Model root.
	 */
	public GrepExpressionRootFolder getExpressions()
	{
		return expressions;
	}

	/**
	 * Returns the plug-in's colour registry.
	 * 
	 * @return Colour registry.
	 */
	public OwnerColorRegistry getColorRegistry()
	{
		return colorRegistry;
	}

	/**
	 * Registers a new grep page participant for a console.
	 * 
	 * @param console Console.
	 * @param participant Participant bound to the console.
	 */
	public synchronized void setParticipant(IConsole console, GrepPageParticipant participant)
	{
		GrepPageParticipant oldParticipant = participants.get(console);
		
		if(participant == null)
		{
			participants.remove(console);
			
			if(oldParticipant != null)
			{
				doParticipantRemoved(oldParticipant);
			}
		}
		else
		{
			participants.put(console, participant);
			
			if(oldParticipant != participant)
			{
				if(oldParticipant != null)
				{
					doParticipantRemoved(oldParticipant);
				}
				
				doParticipantAdded(participant);
			}
			
			setActiveParticipant(participant);
		}
	}

	/**
	 * Adds a listener.
	 * 
	 * @param listener Listener.
	 */
	public void addListener(IGrepConsoleListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener Listener.
	 */
	public void removeListener(IGrepConsoleListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Informs listeners that a participant has been added.
	 * 
	 * @param participant Added participant.
	 */
	private void doParticipantAdded(GrepPageParticipant participant)
	{
		for(IGrepConsoleListener listener: listeners)
		{
			listener.participantAdded(participant);
		}
	}

	/**
	 * Informs listeners that a participant has been removed.
	 * 
	 * @param participant Removed participant.
	 */
	private void doParticipantRemoved(GrepPageParticipant participant)
	{
		for(IGrepConsoleListener listener: listeners)
		{
			listener.participantRemoved(participant);
		}
	}

	/**
	 * Sets the active participant. Informs listeners if the active participant
	 * has changed.
	 * 
	 * @param activeParticipant New active participant.
	 */
	public void setActiveParticipant(GrepPageParticipant activeParticipant)
	{
		if(this.activeParticipant == activeParticipant)
		{
			return;
		}
		
		this.activeParticipant = activeParticipant;

		for(IGrepConsoleListener listener: new ArrayList<IGrepConsoleListener>(listeners))
		{
			listener.participantActivated(activeParticipant);
		}
	}

	/**
	 * Returns the active participant.
	 * 
	 * @return Participant.
	 */
	public GrepPageParticipant getActiveParticipant()
	{
		return activeParticipant;
	}

	/**
	 * Returns the participant bound to a specific console.
	 * 
	 * @param console Console.
	 * 
	 * @return Participant, or <code.null</code>
	 */
	public synchronized GrepPageParticipant getParticipant(IConsole console)
	{
		return participants.get(console);
	}

	/**
	 * Returns an unmodifiable view of the map of console/participant pairings.
	 * 
	 * @return Participant map.
	 */
	public Map<IConsole, GrepPageParticipant> getParticipants()
	{
		return Collections.unmodifiableMap(participants);
	}
	
	/**
	 * Logs an exception without a custom message.
	 * 
	 * @param severity Severity.
	 * @param exception Exception.
	 */
	public void log(int severity, Exception exception)
	{
		log(severity, exception.getMessage(), exception);
	}

	/**
	 * Logs an exception with a custom message.
	 * 
	 * @param severity Severity.
	 * @param message Custom message.
	 * @param exception Exception.
	 */
	public void log(int severity, String message, Throwable exception)
	{
		IStatus status = new Status(severity, PLUGIN_ID, message, exception);
		getLog().log(status);
	}

	/**
	 * Informs listeners that the plug-in's settings have changed. 
	 */
	public void doSettingsChanged()
	{
		for(IGrepConsoleListener listener: new ArrayList<IGrepConsoleListener>(listeners))
		{
			listener.settingsChanged();
		}
	}
	
	/**
	 * Returns the configured default preview text.
	 * 
	 * @return Preview text.
	 */
	public String getPreviewText()
	{
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		return scope.get(PREFS_PREVIEW, DEFAULT_PREVIEW_TEXT);
	}

	/**
	 * Sets the configured default preview text.
	 * 
	 * @param text Preview text.
	 */
	public void setPreviewText(String text)
	{
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		scope.put(PREFS_PREVIEW, text);
	}

	/**
	 * Returns the number of characters to match with a regular expression when
	 * styling a line. If a given line is longer than this value, only the first n
	 * characters are matched.
	 * 
	 * @return Style match length;
	 */
	public int getStyleMatchLength()
	{
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		return scope.getInt(PREFS_STYLE_MATCH_LENGTH, DEFAULT_STYLE_MATCH_LENGTH);
	}
	
	/**
	 * Returns the number of characters to match with a regular expression when
	 * filtering a line. If a given line is longer than this value, only the first n
	 * characters are matched.
	 * 
	 * @return Filter match length;
	 */
	public int getFilterMatchLength()
	{
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		return scope.getInt(PREFS_FILTER_MATCH_LENGTH, DEFAULT_FILTER_MATCH_LENGTH);
	}

	/**
	 * Returns the link mouse cursor (creates it if necessary).
	 * 
	 * @return Link mouse cursor.
	 */
	public synchronized Cursor getLinkCursor()
	{
		if(linkCursor == null)
		{
			linkCursor = new Cursor(getWorkbench().getDisplay(), SWT.CURSOR_HAND);
			
		}

		return linkCursor;
	}

	/**
	 * Returns the key code of the modifier key for console links.
	 * 
	 * @return Modifier key code.
	 */
	public int getLinkModifierKey()
	{
		IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		String s = scope.get(PREFS_LINK_MODIFIER_KEY, null);
		
		if(s != null)
		{
			try
			{
				return Integer.parseInt(s);
			}
			catch(NumberFormatException ex)
			{
			}
		}

		return SWT.MOD1;
	}
	
//	/**
//	 * Returns the popup notification manager. Creates it if necessary.
//	 * 
//	 * @return Popup notification manager.
//	 */
//	public synchronized PopupNotificationManager getPopupNotificationManager()
//	{
//		if(popupNotificationManager == null)
//		{
//			Display display = getWorkbench().getDisplay();
//			popupNotificationManager = new PopupNotificationManager(display);
//			
//			GrepConsoleNotificationStyle defaultStyle = new GrepConsoleNotificationStyle(popupNotificationManager.getDefaultNotificationStyle(), this, popupNotificationManager);
//			popupNotificationManager.setDefaultNotificationStyle(defaultStyle);
//			
//			defaultStyle.setImage(getImageRegistry().get(IMG_LOGO_SMALL));
//		}
//		
//		return popupNotificationManager;
//	}
	
	/**
	 * Returns the sound manager. Creates it if necessary.
	 * 
	 * @return Sound manager.
	 */
	public synchronized SoundManager getSoundManager()
	{
		if(soundManager == null)
		{
			soundManager = new SoundManager(SOUND_CACHE_SIZE);
		}
		
		return soundManager;
	}

	/**
	 * @return
	 */
  public synchronized INotificationStyle getNotificationStyle()
  {
  	if(notificationStyle == null)
  	{
  		PopupNotificationManager man = name.schedenig.eclipse.popupnotifications.Activator.getDefault().getManager();
  		notificationStyle = new GrepConsoleNotificationStyle(man.getDefaultNotificationStyle(), this, man);
		//popupNotificationManager.setDefaultNotificationStyle(defaultStyle);
  		notificationStyle.setImage(getImageRegistry().get(IMG_LOGO_SMALL));
  	}
  	
  	return notificationStyle;
  }
}
