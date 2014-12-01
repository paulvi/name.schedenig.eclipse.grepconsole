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

package name.schedenig.eclipse.grepconsole.adapters;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener;
import name.schedenig.eclipse.grepconsole.adapters.links.LinkListener;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console page participant that attaches grep line style listeners to a new
 * console.
 * 
 * @author msched
 */
public class GrepPageParticipant implements IConsolePageParticipant, IGrepConsoleListener
{
	/**
	 * An interface for listeners to participant changes.
	 * 
	 * @author msched
	 */
	public static interface IGrepPageParticipantListener
	{
		/**
		 * Called when the current participant has changed.
		 * 
		 * @param grepPageParticipant New participant.
		 */
		public void participantChanged(GrepPageParticipant grepPageParticipant);
	}
	
	/** Console. */
	private IConsole console;
	
	/** Launch configuration associated with the console. */
	private ILaunchConfiguration launchConfig;
	
	/** The console's styled text. */
	private StyledText styledText;

	/** Line style listener assigned to the console's styled text.*/
	private GrepLineStyleListener grepLineStyleListener;

	/** Colour registry used for the line style listener. */
	private ColorRegistry colorRegistry;
	
	/** Map of item enablement states. */
	private Map<String, Boolean> enablementMap;
	
	/** Map of item filter states. */
	private Map<String, Boolean> filterMap;
	
	/** Map of item statistics states. */
	private Map<String, Boolean> statisticsMap;
	
	/** Map of item notifications states. */
	private Map<String, Boolean> notificationsMap;
	
	/** Set of listeners. */
	private Set<IGrepPageParticipantListener> listeners = new LinkedHashSet<GrepPageParticipant.IGrepPageParticipantListener>();

	/** Console link adapter. */
	private LinkListener linkAdapter;

	/** Text filter used for Grep View output and notifications. An instance is
	 *  always created, but only active (hooked as a listener) when Grep View or
	 *  notifications are enabled. */
	private TextFilter textFilter;

	private boolean used;

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
	{
		return null;
	}

	/**
	 * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageBookViewPage, org.eclipse.ui.console.IConsole)
	 */
	@Override
	public void init(IPageBookViewPage page, IConsole console)
	{
		Activator activator = Activator.getDefault();
		GrepPageParticipant oldParticipant = activator.getParticipant(console);

		// FIXME: I don't know why two participants are created per console, but
		// here at least we make sure the second one is not used.
		if(oldParticipant != null)
		{
			return;
		}
		
		// Marks that we are not the second participant mentioned above
		used = true;
		
		page.getControl().addPaintListener(new PaintListener()
		{
			@Override
			public void paintControl(PaintEvent e)
			{
				Activator activator = Activator.getDefault();
				
				if(activator.getActiveParticipant() != GrepPageParticipant.this)
				{
					activator.setActiveParticipant(GrepPageParticipant.this);
				}
			}
		});
		
		this.console = console;

		colorRegistry = new ColorRegistry(Activator.getDefault().getColorRegistry());
		enablementMap = new HashMap<String, Boolean>();
		filterMap = new HashMap<String, Boolean>();
		statisticsMap = new HashMap<String, Boolean>();
		notificationsMap = new HashMap<String, Boolean>();
		
		if(console instanceof IAdaptable)
		{
			Object o = ((IAdaptable) console).getAdapter(ILaunchConfiguration.class);
			
			if(o instanceof ILaunchConfiguration)
			{
				launchConfig = (ILaunchConfiguration) o;
			}
		}
		
		if(launchConfig == null && console instanceof org.eclipse.debug.ui.console.IConsole)
		{
			ILaunch launch = ((org.eclipse.debug.ui.console.IConsole) console).getProcess().getLaunch();
			launchConfig = launch.getLaunchConfiguration();
		}
		
		if(launchConfig != null)
		{
			try
			{
				enablementMap.putAll(GrepConsoleUtil.loadIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_DISABLED_IDS, launchConfig));
				filterMap.putAll(GrepConsoleUtil.loadIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_FILTER_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_FILTER_DISABLED_IDS, launchConfig));
				statisticsMap.putAll(GrepConsoleUtil.loadIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_STATISTICS_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_STATISTICS_DISABLED_IDS, launchConfig));
				notificationsMap.putAll(GrepConsoleUtil.loadIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_NOTIFICATIONS_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_NOTIFICATIONS_DISABLED_IDS, launchConfig));
			}
			catch(CoreException ex)
			{
				Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_read_launch_configuration, ex);
			}
		}
		
		if(page.getControl() instanceof StyledText)
		{
			styledText = (StyledText)(page.getControl());
			
			grepLineStyleListener = new GrepLineStyleListener(styledText.getShell(), getProject());
			grepLineStyleListener.setColorRegistry(colorRegistry);
			
			refreshLineStyleListener();
			
			styledText.addLineStyleListener(grepLineStyleListener);
			
			linkAdapter = new LinkListener(styledText, grepLineStyleListener);
			linkAdapter.attach();
		}

		textFilter = createTextFilter();
		
		activator.setParticipant(console, this);
		activator.addListener(this);
	}

	/**
	 * Returns the map of item enablement states.
	 * 
	 * @return Map mapping item IDs to Boolean states.
	 */
	public Map<String, Boolean> getEnablementMap()
	{
		return enablementMap;
	}
	
	/**
	 * Sets the map of item enablement states.
	 * 
	 * @param enablementMap Map mapping item IDs to Boolean states.
	 */
	public void setEnablementMap(Map<String, Boolean> enablementMap)
	{
		this.enablementMap = enablementMap;
	}

	/**
	 * Returns the map of item filter states.
	 * 
	 * @return Map mapping item IDs to Boolean states.
	 */
	public Map<String, Boolean> getFilterMap()
	{
		return filterMap;
	}
	
	/**
	 * Sets the map of item filter states.
	 * 
	 * @param filterMap Map mapping item IDs to Boolean states.
	 */
	public void setFilterMap(Map<String, Boolean> filterMap)
	{
		this.filterMap = filterMap;
	}
	
	/**
	 * Returns the map of item statistics states.
	 * 
	 * @return Map mapping item IDs to Boolean states.
	 */
	public Map<String, Boolean> getStatisticsMap()
	{
		return statisticsMap;
	}
	
	/**
	 * Sets the map of item statistics states.
	 * 
	 * @param filterMap Map mapping item IDs to Boolean states.
	 */
	public void setStatisticsMap(Map<String, Boolean> statisticsMap)
	{
		this.statisticsMap = statisticsMap;
	}
	
	/**
	 * Returns the map of item notifications states.
	 * 
	 * @return Map mapping item IDs to Boolean states.
	 */
	public Map<String, Boolean> getNotificationsMap()
	{
		return notificationsMap;
	}
	
	/**
	 * Sets the map of item notifications states.
	 * 
	 * @param filterMap Map mapping item IDs to Boolean states.
	 */
	public void setNotificationsMap(Map<String, Boolean> notificationsMap)
	{
		this.notificationsMap = notificationsMap;
	}
	
	/**
	 * Updates the line style listener according to the current elements.
	 */
	public void refreshLineStyleListener()
	{
		if(grepLineStyleListener == null)
		{
			return;
		}
		
		Activator activator = Activator.getDefault();
		LinkedList<GrepExpressionItem> items = new LinkedList<GrepExpressionItem>();
		addAllEnabledItems(items, activator.getExpressions(), enablementMap);
		
		grepLineStyleListener.setItems(items);
		
		if(!styledText.isDisposed())
		{
			styledText.redraw();
		}
	}

	/**
	 * Recursively collects all enabled items in a list.
	 * 
	 * @param items List of items. Found items are added to this list.
	 * @param element Element to search.
	 * @param enablementMap Map of enablement states, or <code>null</code>.
	 */
	private void addAllEnabledItems(LinkedList<GrepExpressionItem> items, AbstractGrepModelElement element, Map<String, Boolean> enablementMap)
	{
		Boolean enablement = enablementMap.get(element.getId());
		
		if(enablement == null)
		{
			enablement = element.isDefaultEnabled();
		}
		
		if(!(element instanceof GrepExpressionRootFolder) && !enablement)
		{
			return;
		}
		
		if(element instanceof GrepExpressionItem)
		{
			items.add((GrepExpressionItem) element);
		}
		else if(element instanceof GrepExpressionFolder)
		{
			for(AbstractGrepModelElement child: ((GrepExpressionFolder) element).getChildren())
			{
				addAllEnabledItems(items, child, enablementMap);
			}
		}
	}

	/**
	 * Recursively collects all filtered items in a list.
	 * 
	 * @param items List of items. Found items are added to this list.
	 * @param element Element to search.
	 * @param filterMap Map of filter states, or <code>null</code>.
	 */
	public void addAllFilterItems(LinkedList<GrepExpressionItem> items, AbstractGrepModelElement element, Map<String, Boolean> filterMap)
	{
		Boolean filter = filterMap.get(element.getId());
		
		if(filter == null)
		{
			filter = element.isDefaultFilter();
		}
		
		if(!(element instanceof GrepExpressionRootFolder) && !filter)
		{
			return;
		}
		
		if(element instanceof GrepExpressionItem)
		{
			items.add((GrepExpressionItem) element);
		}
		else if(element instanceof GrepExpressionFolder)
		{
			for(AbstractGrepModelElement child: ((GrepExpressionFolder) element).getChildren())
			{
				addAllFilterItems(items, child, filterMap);
			}
		}
	}

	/**
	 * Recursively collects all statistics items in a list.
	 * 
	 * @param items List of items. Found items are added to this list.
	 * @param element Element to search.
	 * @param statisticsMap Map of statistics states, or <code>null</code>.
	 */
	public void addAllStatisticsItems(LinkedHashSet<GrepExpressionItem> items, AbstractGrepModelElement element, Map<String, Boolean> statisticsMap)
	{
		Boolean statistics = statisticsMap.get(element.getId());
		
		if(statistics == null)
		{
			statistics = element.isDefaultStatistics();
		}
		
		if(!(element instanceof GrepExpressionRootFolder) && !statistics)
		{
			return;
		}
		
		if(element instanceof GrepExpressionItem)
		{
			items.add((GrepExpressionItem) element);
		}
		else if(element instanceof GrepExpressionFolder)
		{
			for(AbstractGrepModelElement child: ((GrepExpressionFolder) element).getChildren())
			{
				addAllStatisticsItems(items, child, statisticsMap);
			}
		}
	}
	
	/**
	 * Recursively collects all notifications items in a list.
	 * 
	 * @param items List of items. Found items are added to this list.
	 * @param element Element to search.
	 * @param filterMap Map of notification states, or <code>null</code>.
	 */
	public void addAllNotificationsItems(Collection<GrepExpressionItem> items, AbstractGrepModelElement element, Map<String, Boolean> notificationsMap)
	{
		Boolean notifications = notificationsMap.get(element.getId());
		
		if(notifications == null)
		{
			notifications = element.isDefaultNotifications();
		}
		
		if(!(element instanceof GrepExpressionRootFolder) && !notifications)
		{
			return;
		}
		
		if(element instanceof GrepExpressionItem)
		{
			items.add((GrepExpressionItem) element);
		}
		else if(element instanceof GrepExpressionFolder)
		{
			for(AbstractGrepModelElement child: ((GrepExpressionFolder) element).getChildren())
			{
				addAllNotificationsItems(items, child, notificationsMap);
			}
		}
	}
	
	/**
	 * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
	 */
	@Override
	public void dispose()
	{
		Activator activator = Activator.getDefault();
		activator.removeListener(this);
		
		if(textFilter != null)
		{
			textFilter.dispose();
		}
		
		if(linkAdapter != null)
		{
			linkAdapter.dispose();
			linkAdapter = null;
		}
		
		if(colorRegistry != null)
		{
			colorRegistry.disposeColors();
		}
		
		if(used)
		{
			activator.setParticipant(console, null);
		}
	}

	/**
	 * @see org.eclipse.ui.console.IConsolePageParticipant#activated()
	 */
	@Override
	public void activated()
	{
		Activator.getDefault().setActiveParticipant(this);
	}

	/**
	 * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
	 */
	@Override
	public void deactivated()
	{
	}

	/**
	 * Stores the current enablement and filter maps in the launch configuration.
	 */
	public void saveLaunchConfig()
	{
		if(launchConfig == null)
		{
			return;
		}
		
		ILaunchConfigurationWorkingCopy wc;
		
		try
		{
			wc = launchConfig.getWorkingCopy();
			GrepConsoleUtil.storeIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_DISABLED_IDS, enablementMap, wc);
			GrepConsoleUtil.storeIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_FILTER_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_FILTER_DISABLED_IDS, filterMap, wc);
			GrepConsoleUtil.storeIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_STATISTICS_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_STATISTICS_DISABLED_IDS, statisticsMap, wc);
			GrepConsoleUtil.storeIdBooleanMap(GrepConsoleUtil.ATTRIBUTE_NOTIFICATIONS_ENABLED_IDS, GrepConsoleUtil.ATTRIBUTE_NOTIFICATIONS_DISABLED_IDS, notificationsMap, wc);
			wc.doSave();
		}
		catch(CoreException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_save_launch_configuration, ex);
			return;
		}
	}
	
	/**
	 * Returns the styled text control.
	 * 
	 * @return Styled text control.
	 */
	public StyledText getStyledText()
	{
		return styledText;
	}

	/**
	 * Returns the launch configuration.
	 *  
	 * @return Launch configuration. May be <code>null</code>.
	 */
	public ILaunchConfiguration getLaunchConfig()
	{
		return launchConfig;
	}

	/**
	 * Returns the text filter.
	 * 
	 * @return Text filter.
	 */
	public synchronized TextFilter getTextFilter()
	{
		return textFilter;
	}
	
	/**
	 * Creates a new text filter for the console content, depending on the current
	 * filter expressions.
	 * 
	 * @return
	 */
	private TextFilter createTextFilter()
	{
		Activator activator = Activator.getDefault();
		LinkedList<GrepExpressionItem> filterExpressions = new LinkedList<GrepExpressionItem>();
		addAllFilterItems(filterExpressions, activator.getExpressions(), filterMap);
		
		LinkedHashSet<GrepExpressionItem> statisticsExpressions = new LinkedHashSet<GrepExpressionItem>();
		addAllStatisticsItems(statisticsExpressions, activator.getExpressions(), statisticsMap);
		
		LinkedHashSet<GrepExpressionItem> notificationExpressions = new LinkedHashSet<GrepExpressionItem>();
		addAllNotificationsItems(notificationExpressions, activator.getExpressions(), notificationsMap);
		
		TextFilter textFilter = new TextFilter(styledText.getContent(), filterExpressions, statisticsExpressions, notificationExpressions, styledText.getShell(), getProject());
		textFilter.refresh();

		return textFilter;
	}
	
	/**
	 * Returns the line style listener.
	 * 
	 * @return Line style listener.
	 */
	public GrepLineStyleListener getLineStyleListener()
	{
		return grepLineStyleListener;
	}
	
	/**
	 * Returns the console.
	 * 
	 * @return Console.
	 */
	public IConsole getConsole()
	{
		return console;
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
	 * Refreshes the line style listener and informs listeners of participant
	 * changes when the plug-in's settings have changed.
	 *  
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#settingsChanged()
	 */
	@Override
	public void settingsChanged()
	{
		refreshLineStyleListener();

		// experimental...
		Activator activator = Activator.getDefault();
		LinkedList<GrepExpressionItem> filterExpressions = new LinkedList<GrepExpressionItem>();
		addAllFilterItems(filterExpressions, activator.getExpressions(), filterMap);
		textFilter.setFilterExpressions(filterExpressions);
		
		LinkedHashSet<GrepExpressionItem> statisticsExpressions = new LinkedHashSet<GrepExpressionItem>();
		addAllStatisticsItems(statisticsExpressions, activator.getExpressions(), statisticsMap);
		textFilter.setStatisticsExpressions(statisticsExpressions);
		
		LinkedHashSet<GrepExpressionItem> notificationExpressions = new LinkedHashSet<GrepExpressionItem>();
		addAllNotificationsItems(notificationExpressions, activator.getExpressions(), notificationsMap);
		textFilter.setNotificationExpressions(notificationExpressions);

	
		textFilter.refresh();

		for(IGrepPageParticipantListener listener: listeners)
		{
			listener.participantChanged(this);
		}
	}
	
	/**
	 * Shows the console.
	 */
	public void setFocus()
	{
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
	}
	
	/**
	 * Adds a listener.
	 * 
	 * @param listener Listener.
	 */
	public void addListener(IGrepPageParticipantListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes a listener.
	 * 
	 * @param listener Listener.
	 */
	public void removeListener(IGrepPageParticipantListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Returns the project associated with the launch configuration.
	 * 
	 * @return Project.
	 */
	public IProject getProject()
	{
		try
		{
			if(launchConfig == null || launchConfig.getMappedResources() == null)
			{
				return null;
			}
			
			for(IResource resource: launchConfig.getMappedResources())
			{
				if(resource instanceof IProject)
				{
					return (IProject) resource;
				}
				else if(resource.getProject() != null)
				{
					return resource.getProject();
				}
			}
		}
		catch(CoreException ex)
		{
			return null;
		}
		
		return null;
	}
}
