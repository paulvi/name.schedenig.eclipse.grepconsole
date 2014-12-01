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

import java.util.Map;

/**
 * Determines whether a flag is set on a specific model element. Includes
 * information about whether the flag's setting is inherited from the element's
 * default settings and whether the setting is "greyed out", i.e. disabled
 * because a parent element has the same flag set to <code>false</code>.
 * 
 * @author msched
 */
public abstract class InheritedFlagResolver
{
	/**
	 * Resolver implementation for the "enabled" flag.
	 */
	public static final InheritedFlagResolver ENABLED_RESOLVER = new InheritedFlagResolver()
	{
		@Override
		protected boolean getDefaultValue(AbstractGrepModelElement element)
		{
			return element.isDefaultEnabled();
		}
	};
	
	/**
	 * Resolver implementation for the "filter" flag.
	 */
	public static final InheritedFlagResolver FILTER_RESOLVER = new InheritedFlagResolver()
	{
		@Override
		protected boolean getDefaultValue(AbstractGrepModelElement element)
		{
			return element.isDefaultFilter();
		}
	};
	
	/**
	 * Resolver implementation for the "statistics" flag.
	 */
	public static final InheritedFlagResolver STATISTICS_RESOLVER = new InheritedFlagResolver()
	{
		@Override
		protected boolean getDefaultValue(AbstractGrepModelElement element)
		{
			return element.isDefaultStatistics();
		}
	};
	
	/**
	 * Resolver implementation for the "notifications" flag.
	 */
	public static final InheritedFlagResolver NOTIFICATIONS_RESOLVER = new InheritedFlagResolver()
	{
		@Override
		protected boolean getDefaultValue(AbstractGrepModelElement element)
		{
			return element.isDefaultNotifications();
		}
	};
	
	/**
	 * A flag state.
	 * 
	 * @author msched
	 */
	public static class InheritedFlag
	{
		/** Whether the flag is set. */
		private boolean set;
		
		/** Whether the flag's setting is inherited. */
		private boolean inherited;
		
		/** Whether the flag is "greyed out". */
		private boolean greyed;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param set Whether the flag is set.
		 * @param inherited Whether the flag's setting is inherited.
		 * @param greyed Whether the flag is "greyed out".
		 */
		private InheritedFlag(boolean set, boolean inherited, boolean greyed)
		{
			this.set = set;
			this.inherited = inherited;
			this.greyed = greyed;
		}
		
		/**
		 * Returns whether the flag is set.
		 * 
		 * @return Whether the flag is set.
		 */
		public boolean isSet()
		{
			return set;
		}
		
		/**
		 * Returns whether the flag's setting is inherited.
		 * 
		 * @return Whether the flag's setting is inherited.
		 */
		public boolean isInherited()
		{
			return inherited;
		}
		
		/**
		 * Returns whether the flag is "greyed out".
		 * 
		 * @return the greyed Returns whether the flag is "greyed out".
		 */
		public boolean isGreyed()
		{
			return greyed;
		}
	}

	/**
	 * Gets the flag state for a specific element.
	 * 
	 * @param element Model element.
	 * @param inheritanceMap Inheritance map. If this is provided, the flag's
	 * 		setting is read from this map and if the flag is not found there, the
	 * 		element's default value is used as the flag's inherited value. If this
	 * 		is <code>null</code>, the flag's setting is read directly from the
	 * 		model element's default value, but not marked as inherited.
	 * 
	 * @return Flag state.
	 */
	public InheritedFlag getFlag(AbstractGrepModelElement element, Map<String, Boolean> inheritanceMap)
	{
		Boolean checked;
		
		if(inheritanceMap == null)
		{
			checked = getDefaultValue(element);
		}
		else
		{
			checked = inheritanceMap.get(element.getId());
		}
			
		boolean greyed = !isElementSet(((AbstractGrepModelElement) element).getParent(), inheritanceMap);
		
		if(checked == null)
		{
			Boolean inheritedChecked = getDefaultValue(element);
			
			if(inheritedChecked == null)
			{
				inheritedChecked = false;
			}
			
			return new InheritedFlag(inheritedChecked, true, greyed);
		}
		else
		{
			return new InheritedFlag(checked, false, greyed);
		}
	}

	/**
	 * Tests whether the flag is set on the specified element.
	 * 
	 * @param element Model element.
	 * @param inheritanceMap Inheritance map.
	 * 
	 * @return Whether the flag is set.
	 */
	private boolean isElementSet(AbstractGrepModelElement element, Map<String, Boolean> inheritanceMap)
	{
		GrepExpressionFolder parent = element.getParent();
		
		if(parent != null)
		{
			if(!isElementSet(element.getParent(), inheritanceMap))
			{
				return false;
			}
		}
		
		Boolean enabled;
		
		if(inheritanceMap == null)
		{
			enabled = null;
		}
		else
		{
			enabled = inheritanceMap.get(element.getId());
		}
		
		if(enabled == null)
		{
			enabled = getDefaultValue(element);
		}
		
		return enabled;
	}

	/**
	 * Reads the flag's default value from the specified element.
	 * 
	 * @param element Model element.
	 * 
	 * @return Default value.
	 */
	protected abstract boolean getDefaultValue(AbstractGrepModelElement element);
}
