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

package name.schedenig.eclipse.grepconsole.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A record class that holds an expression item tree and ID/boolean maps for
 * enabled and filtered items.
 * 
 * @author msched
 */
public class GrepExpressionsWithSelection
{
	/** Root of expression item tree. */
	private GrepExpressionRootFolder rootGroup;
	
	/** ID/Boolean map of enabled items. Items not included in the map use their
	 *  default enablement setting. */
	private Map<String, Boolean> enablementMap;
	
	/** ID/Boolean map of filtered items. Items not included in the map use their
	 *  default filter setting. */
	private Map<String, Boolean> filterMap;
	
	/** ID/Boolean map of statistics items. Items not included in the map use
	 *  their default filter setting. */
	private Map<String, Boolean> statisticsMap;
	
	/** ID/Boolean map of notification items. Items not included in the map use
	 *  their default notification setting. */
	private Map<String, Boolean> notificationsMap;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param rootGroup Root of expression item tree.
	 * @param enablementMap ID/Boolean map of enabled items.
	 * @param filterMap ID/Boolean map of filtered items.
	 */
	public GrepExpressionsWithSelection(GrepExpressionRootFolder rootGroup,
			Map<String, Boolean> enablementMap, Map<String, Boolean> filterMap,
			Map<String, Boolean> statisticsMap, Map<String, Boolean> notificationsMap)
	{
		this.rootGroup = rootGroup;
		this.enablementMap = enablementMap;
		this.filterMap = filterMap;
		this.statisticsMap = statisticsMap;
		this.notificationsMap = notificationsMap;
	}

	/**
	 * Returns the root of the expression item tree.
	 * 
	 * @return Root.
	 */
	public GrepExpressionRootFolder getRootFolder()
	{
		return rootGroup;
	}
	
	/**
	 * Sets the root of the expression item tree.
	 * 
	 * @param rootGroup Root.
	 */
	public void setRootGroup(GrepExpressionRootFolder rootGroup)
	{
		this.rootGroup = rootGroup;
	}
	
	/**
	 * Returns the ID/Boolean map of enabled items.
	 * 
	 * @return Map of enabled items.
	 */
	public Map<String, Boolean> getEnablementMap()
	{
		return enablementMap;
	}
	
	/**
	 * Sets the ID/Boolean map of enabled items.
	 * 
	 * @param enablementMap Map of enabled items.
	 */
	public void setEnablementMap(Map<String, Boolean> enablementMap)
	{
		this.enablementMap = enablementMap;
	}

	/**
	 * Returns the ID/Boolean map of filtered items.
	 * 
	 * @return Map of filtered items.
	 */
	public Map<String, Boolean> getFilterMap()
	{
		return filterMap;
	}

	/**
	 * Sets the ID/Boolean map of filtered items.
	 * 
	 * @param filterMap Map of filtered items.
	 */
	public void setFilterMap(Map<String, Boolean> filterMap)
	{
		this.filterMap = filterMap;
	}

	/**
	 * Returns the ID/Boolean map of statistics items.
	 * 
	 * @return Map of statistics items.
	 */
	public Map<String, Boolean> getStatisticsMap()
	{
		return statisticsMap;
	}
	
	/**
	 * Sets the ID/Boolean map of statistics items.
	 * 
	 * @param filterMap Map of statistics items.
	 */
	public void setStatisticsMap(Map<String, Boolean> statisticsMap)
	{
		this.statisticsMap = statisticsMap;
	}
	
	/**
	 * Returns the ID/Boolean map of notification items.
	 * 
	 * @return Map of notification items.
	 */
	public Map<String, Boolean> getNotificationsMap()
	{
		return notificationsMap;
	}
	
	/**
	 * Sets the ID/Boolean map of notification items.
	 * 
	 * @param filterMap Map of notification items.
	 */
	public void setNotificationsMap(Map<String, Boolean> notificationsMap)
	{
		this.notificationsMap = notificationsMap;
	}
	
	/**
	 * Creates a copy of this instance.
	 * 
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 * 
	 * @return Copy.
	 */
	public GrepExpressionsWithSelection copy(boolean identityCopy)
	{
		return new GrepExpressionsWithSelection(rootGroup.copy(identityCopy), 
				enablementMap == null ? null : new HashMap<String, Boolean>(enablementMap),
				filterMap == null ? null : new HashMap<String, Boolean>(filterMap),
				statisticsMap == null ? null : new HashMap<String, Boolean>(statisticsMap),
				notificationsMap == null ? null : new HashMap<String, Boolean>(notificationsMap));
	}

	/**
	 * Copies the contents of this instance from the specified source.
	 * 
	 * @param src Source.
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 */
	public void copyFrom(GrepExpressionsWithSelection src, boolean identityCopy)
	{
		enablementMap = src.enablementMap == null ? null : new HashMap<String, Boolean>(src.enablementMap);
		filterMap = src.filterMap == null ? null : new HashMap<String, Boolean>(src.filterMap);
		notificationsMap = src.notificationsMap == null ? null : new HashMap<String, Boolean>(src.notificationsMap);
		rootGroup.copyFrom(src.rootGroup, identityCopy);
	}
}
