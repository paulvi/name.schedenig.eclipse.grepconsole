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

package name.schedenig.eclipse.grepconsole.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.MatchResult;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter;
import name.schedenig.eclipse.grepconsole.adapters.links.LinkMatch;
import name.schedenig.eclipse.grepconsole.i18n.Messages;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Static utility methods.
 * 
 * @author msched
 */
public class GrepConsoleUtil
{
	/** Configuration attribute names. */ 
	public static final String ATTRIBUTE_PREFIX = "name.schedenig.eclipse.grepconsole."; //$NON-NLS-1$
	public static final String ATTRIBUTE_ENABLED_IDS = ATTRIBUTE_PREFIX + "EnabledIds"; //$NON-NLS-1$
	public static final String ATTRIBUTE_DISABLED_IDS = ATTRIBUTE_PREFIX + "DisabledIds"; //$NON-NLS-1$
	public static final String ATTRIBUTE_FILTER_ENABLED_IDS = ATTRIBUTE_PREFIX + "FilterEnabledIds"; //$NON-NLS-1$
	public static final String ATTRIBUTE_FILTER_DISABLED_IDS = ATTRIBUTE_PREFIX + "FilterDisabledIds"; //$NON-NLS-1$
	public static final String ATTRIBUTE_STATISTICS_ENABLED_IDS = ATTRIBUTE_PREFIX + "StatisticsEnabledIds"; //$NON-NLS-1$
	public static final String ATTRIBUTE_STATISTICS_DISABLED_IDS = ATTRIBUTE_PREFIX + "StatisticsDisabledIds"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NOTIFICATIONS_ENABLED_IDS = ATTRIBUTE_PREFIX + "NotificationsEnabledIds"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NOTIFICATIONS_DISABLED_IDS = ATTRIBUTE_PREFIX + "NotificationsDisabledIds"; //$NON-NLS-1$

	/** Preference keys. */
	public static final String PREF_DEBUG_CONSOLE_BACKGROUND_COLOR = IDebugUIConstants.PLUGIN_ID + ".consoleBackground";  //$NON-NLS-1$
	public static final String PREF_DEBUG_CONSOLE_SYS_OUT_COLOR = IDebugUIConstants.PLUGIN_ID + ".outColor"; //$NON-NLS-1$
	
	/** Used for random numbers. */
	private static Random random = new Random();
	
	/**
	 * Converts an integer value to a hex string.
	 * 
	 * @param value Integer value.
	 * @param length Number of digits for the hex string. Shorter strings will be
	 *     padded with zeroes.
	 *     
	 * @return Hex string representation.
	 */
	public static String intHexString(int value, int length)
	{
		String hex = Integer.toHexString(value);
		
		if(hex.length() < length)
		{
			StringBuffer sb = new StringBuffer("0"); //$NON-NLS-1$
			
			for(int i = sb.length() + hex.length(); i < length; i++)
			{
				sb.append('0');
			}
			
			sb.append(hex);
			hex = sb.toString();
		}
		
		return hex;
	}

	/**
	 * Creates a random RGB value.
	 * 
	 * @return Random RGB value.
	 */
	public static RGB getRandomRgb()
	{
		return new RGB(128 + random.nextInt(128),
				128 + random.nextInt(128),
				128 + random.nextInt(128));
	}

	/**
	 * Converts an RGB instance to a 6-digit hex string.
	 * 
	 * @param color RGB instance to convert.
	 * 
	 * @return String representation of the RGB instance, or an empty string if
	 * 		color was <code>null</code>.
	 */
	public static String rgbToString(RGB color)
	{
		if(color == null)
		{
			return ""; //$NON-NLS-1$
		}
		else
		{
			return GrepConsoleUtil.intHexString(color.red, 2) + 
				GrepConsoleUtil.intHexString(color.green, 2) +
				GrepConsoleUtil.intHexString(color.blue, 2);
		}
	}

	/**
	 * Parses a string into an RGB instance. The string should be a 6-digit hex
	 * representation of the RGB value. Longer strings are truncated, shorter
	 * strings are padded with zeroes from the left.
	 * 
	 * @param s String representation.
	 * 
	 * @return RGB instance, or <code>null</code> if the provided string was empty
	 *     or <code>null</code>.
	 *     
	 * @throws NumberFormatException If the provided string could not be parsed.
	 */
	public static RGB stringToRgb(String s) throws NumberFormatException
	{
		if(s == null || s.length() == 0)
		{
			return null;
		}
		
		if(s.length() != 6)
		{
			s = ("000000" + s); //$NON-NLS-1$
			s = s.substring(s.length() - 6);
		}
		
		return new RGB(Integer.parseInt(s.substring(0, 2), 16),
				Integer.parseInt(s.substring(2, 4), 16),
				Integer.parseInt(s.substring(4, 6), 16));
	}

	/**
	 * Tests two object for equality. Can handle <code>null</code> values.
	 * 
	 * @param o1 First object. May be <code>null</code>.
	 * @param o2 Second object. May be <code>null</code>.
	 * 
	 * @return Whether the objects are equal.
	 */
	public static boolean equals(Object o1, Object o2)
	{
		if(o1 == o2)
		{
			return true;
		}
		else if(o1 == null || o2 == null)
		{
			return false;
		}
		else
		{
			return o1.equals(o2);
		}
	}
	
	/**
	 * Constructs two sets of IDs from the specified map, depending on which
	 * boolean value they are mapped to.
	 * 
	 * @param map Map of IDs to Boolean values.
	 * @param trueSet All IDs mapped to <code>true</code> will be added to this
	 * 		set.
	 * @param falseSet All IDs mapped to <code>false</code> will be added to this
	 * 		set.
	 */
	public static void convertIdBooleanToSets(Map<String, Boolean> map, Set<String> trueSet, Set<String> falseSet)
	{
		for(Entry<String, Boolean> entry: map.entrySet())
		{
			Boolean b = entry.getValue();
			
			if(b == null)
			{
				continue;
			}
			
			if(b)
			{
				trueSet.add(entry.getKey());
			}
			else
			{
				falseSet.add(entry.getKey());
			}
		}
	}

	/**
	 * Creates an ID/Boolean map from two sets of IDs.
	 * 
	 * @param trueSet Set of IDs which will be mapped to <code>true</code>.
	 * @param falseSet Set of IDs which will be mapped to <code>false</code>.
	 * 
	 * @return Generated map.
	 */
	public static Map<String, Boolean> convertIdBooleanMapFromSets(Set<String> trueSet, Set<String> falseSet)
	{
		Map<String, Boolean> enablementMap = new HashMap<String, Boolean>();
		
		for(String id: trueSet)
		{
			enablementMap.put(id,  true);
		}
		
		for(String id: falseSet)
		{
			enablementMap.put(id,  false);
		}
		
		return enablementMap;
	}
	
	/**
	 * Stores the specified ID/Boolean map in a launch configuration. Uses two
	 * sets for storage, one for all IDs mapped to <code>true</code> and one for
	 * all IDs mapped to <code>false</code>.
	 * 
	 * @param attributeNameTrue Name of the launch configuration attribute that
	 * 		will store the <code>true</code> set. 
	 * @param attributeNameFalse Name of the launch configuration attribute that
	 * 		will store the <code>false</code> set.
	 * @param map ID/Boolean map.
	 * @param configuration Writable launch configuration.
	 */
	public static void storeIdBooleanMap(String attributeNameTrue, String attributeNameFalse, Map<String, Boolean> map, ILaunchConfigurationWorkingCopy configuration)
	{
		Set<String> enabledIds = new HashSet<String>();
		Set<String> disabledIds = new HashSet<String>();
		GrepConsoleUtil.convertIdBooleanToSets(map, enabledIds, disabledIds);
		
		configuration.setAttribute(attributeNameTrue, enabledIds);
		configuration.setAttribute(attributeNameFalse, disabledIds);
	}

	/**
	 * Reads an ID/Boolean map from a launch configuration.
	 * 
	 * @param attributeNameTrue Name of the launch configuration attribute that
	 * 		stores the <code>true</code> set.
	 * @param attributeNameFalse Name of the launch configuration attribute that
	 * 		stores the <code>false</code> set.
	 * @param configuration Launch configuration.
	 * 
	 * @return ID/Boolean map.
	 * 
	 * @throws CoreException
	 */
	public static Map<String, Boolean> loadIdBooleanMap(String attributeNameTrue, String attributeNameFalse, ILaunchConfiguration configuration) throws CoreException
	{
		@SuppressWarnings("unchecked")
		Set<String> enabledIds = configuration.getAttribute(attributeNameTrue, new HashSet<String>());
		
		@SuppressWarnings("unchecked")
		Set<String> disabledIds = configuration.getAttribute(attributeNameFalse, new HashSet<String>());
		
		return GrepConsoleUtil.convertIdBooleanMapFromSets(enabledIds, disabledIds);
	}
	
	/**
	 * Adds an action to an SWT menu using an action contribution item.
	 * 
	 * @param menu Menu.
	 * @param action Action.
	 */
	public static void addActionToMenu(Menu menu, IAction action)
	{
  	ActionContributionItem aci = new ActionContributionItem(action);
  	aci.fill(menu, -1);
	}
	
	/**
	 * Reads text from a stream.
	 * 
	 * @param in Input stream.
	 * 
	 * @return Text.
	 * 
	 * @throws IOException 
	 */
	public static String readText(InputStream in) throws IOException
	{
		StringBuilder sb = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		for(;;)
		{
			String s = reader.readLine();
			
			if(s == null)
			{
				break;
			}
			
			if(sb == null)
			{
				sb = new StringBuilder();
			}
			else
			{
				sb.append("\n"); //$NON-NLS-1$
			}
			
			sb.append(s);
		}
		
		return sb == null ? null : sb.toString();
	}

	/**
	 * Converts character counts to pixels, using the average width and height of
	 * the specified device's current font.
	 * 
	 * @param device Device.
	 * @param charsWidth Width, in characters.
	 * @param charsHeight Height, in characters.
	 * 
	 * @return Width and height in pixels.
	 */
	public static Point charsToPixelDimensions(Drawable device, int charsWidth,
			int charsHeight)
	{
		GC gc = new GC(device);
		
		try
		{
			FontMetrics fm = gc.getFontMetrics();
			
			int ch = fm.getHeight();
			int cw = fm.getAverageCharWidth();
			
			return new Point(cw * charsWidth, ch * charsHeight);
		}
		finally
		{
			gc.dispose();
		}
	}

	/**
	 * Jumps to a specific line in a text editor.
	 * 
	 * @param editor Editor.
	 * @param lineNumber Line number.
	 * @param offset Optional offset.
	 */
	public static void jumpToEditorLine(ITextEditor editor, Integer lineNumber, Integer offset)
	{
		ITextEditor textEditor = (ITextEditor) editor;

		IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

		if(document != null)
		{
			IRegion lineInfo = null;
			
			try
			{
				lineInfo = document.getLineInformation(lineNumber - 1);
			}
			catch(BadLocationException e)
			{
			}
			
			if(lineInfo != null)
			{
				textEditor.selectAndReveal(lineInfo.getOffset() + (offset == null ? 0 : offset),
						offset == null ? lineInfo.getLength() : 0);
			}
		}
	}

	/**
	 * Replaces parameters in a pattern string.
	 * 
	 * Parameters are enclosed in curly braces. PARAM_* constants and group
	 * indexes are supported parameters. Also, parantheses may be used to mark
	 * groups in the pattern string. The group marks will be stripped out and the
	 * character indices of these groups returned to the caller. '\' is the escape
	 * character.
	 * 
	 * If a variable resolver is specified, it will be queried first, and default
	 * parameters will only be evaluated if it did not produce a value.
	 *  
	 * @param s Pattern string.
	 * @param matchResult Match result.
	 * @param wholeLine Whole line text.
	 * @param group Matched group. 
	 * @param groupRanges Optional list which will contain the group ranges
	 * 		(as integer tuples denoting the first and last index of the gruop's
	 * 		characters). May be <code>null</code>.
	 * @param project Project used to resolve Eclipse variables.
	 * @param variable Resolver Optional variable resolver.
	 * 
	 * @return Result string.
	 */
	public static String replaceParams(String s, MatchResult matchResult,
			String wholeLine, int group, List<int[]> groupRanges, IProject project,
			IVariableResolver variableResolver)
	{
		if(s == null)
		{
			return s;
		}
		
		StringBuilder sb = new StringBuilder();
		StringBuilder expression = null;
		boolean escape = false;
		Integer currentGroupStart = null;
		
		for(int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			
			if(expression != null)
			{
				if(c == '}')
				{
					String param = expression.toString();
					String replace = null;
		
					if(variableResolver != null)
					{
						replace = variableResolver.getValue(param);
					}
					
					if(replace == null)
					{
						if(param.startsWith(GrepLinkAdapter.PARAM_ECLIPSE_PREFIX))
						{
							URI uri = null;
							
							if(project != null)
							{
								IPathVariableManager pvm = project.getPathVariableManager();
		
								if(pvm != null)
								{
									String name = param.substring(GrepLinkAdapter.PARAM_ECLIPSE_PREFIX.length()); 
									uri = pvm.getURIValue(name);
								}
							}
							
							if(uri != null)
							{
								replace = uri.toString();
							}
						}
						else if(GrepLinkAdapter.PARAM_WHOLE_LINE.equals(param))
						{
							replace = wholeLine;
						}
						else if(GrepLinkAdapter.PARAM_WHOLE_MATCH.equals(param))
						{
							replace = matchResult.group();
						}
						else if(GrepLinkAdapter.PARAM_GROUP_MATCH.equals(param))
						{
							if(group >= 0)
							{
								replace = matchResult.group(group);
							}
						}
						else
						{
							try
							{
								replace = matchResult.group(Integer.parseInt(param));
							}
							catch(NumberFormatException ex)
							{
								Activator.getDefault().log(IStatus.WARNING, MessageFormat.format(Messages.GrepLinkAdapter_could_not_resolve_parameter, param), ex);
								replace = "{" + param + "}"; //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
					}

					if(replace == null)
					{
						replace = "{" + param + "}"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					
					sb.append(replace);
					expression = null;
				}
				else
				{
					expression.append(c);
				}
			}
			else if(escape)
			{
				switch(c)
				{
					case '(':
					case ')':
					case '{':
					case '}':
					case '\\':
						sb.append(c);
						break;
					
					default:
						sb.append("\\" + c); //$NON-NLS-1$
				}
				
				escape = false;
			}
			else if(c == '\\')
			{
				escape = true;
			}
			else if(groupRanges != null && c == '(' && currentGroupStart == null)
			{
				currentGroupStart = sb.length();
			}
			else if(groupRanges != null && c == ')' && currentGroupStart != null)
			{
				groupRanges.add(new int[]{currentGroupStart, sb.length() - 1});
				currentGroupStart = null;
			}
			else if(c == '{')
			{
				expression = new StringBuilder();
			}
			else
			{
				sb.append(c);
			}
		}
		
		if(escape)
		{
			if(expression != null)
			{
				expression.append('\\');
			}
			else
			{
				sb.append('\\');
			}
		}
		if(expression != null)
		{
			sb.append(expression.toString());
		}
		
		return sb.toString();
	}

	/**
	 * Variable resolver interface for parameter values.
	 */
	public static interface IVariableResolver
	{
		/**
		 * Returns the value for the specified name.
		 * 
		 * @param name Parameter name.
		 * 
		 * @return Value. <code>null</code> for unknown parameter names.
		 */
		public String getValue(String name);
	}
	
	/**
	 * Replaces pattern parameters in the specified pattern string.
	 * 
	 * @param s Pattern string.
	 * @param match Link match.
	 * @param variableResolver Optional variable resolver.
	 *
	 * @return Pattern string with resolved parameters.
	 */
	public static String replaceParams(String s, LinkMatch match, IVariableResolver variableResolver)
	{
		return replaceParams(s, match.getMatchResult(), match.getWholeLine(), match.getGroup(), null, match.getProject(), variableResolver);
	}

	/**
	 * Counts the number of groups in a pattern string. Similar to
	 * <code>replaceParams()</code>, but does not perform any actual string
	 * replacements.
	 * 
	 * @param s Pattern string.
	 * 
	 * @return Number of groups.
	 */
	public static int countGroups(String s)
	{
		if(s == null)
		{
			return 0;
		}
		
		boolean inExpression = false;
		boolean escape = false;
		boolean inGroup = false;
		int groupCount = 0;
		
		for(int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			
			if(inExpression)
			{
				if(c == '}')
				{
					inExpression = false;
				}
			}
			else if(escape)
			{
				escape = false;
			}
			else if(c == '\\')
			{
				escape = true;
			}
			else if(c == '(' && !inGroup)
			{
				inGroup = true;
			}
			else if(c == ')' && inGroup)
			{
				inGroup = false;
				groupCount++;
			}
			else if(c == '{')
			{
				inExpression = true;
			}
		}
		
		return groupCount;
	}

	/**
	 * Returns the preference store for the DebugUI plug-in.
	 * 
	 * @return Preference store.
	 */
	public static ScopedPreferenceStore getDebugUiPreferences()
	{
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, IDebugUIConstants.PLUGIN_ID);
	}
	
	/**
	 * Returns an RGB value from a preference store.
	 * 
	 * @param prefs Preference store.
	 * @param key Key.
	 * 
	 * @return RGB value. <code>null</code> if no value was found, or if the
	 * 		format was incorrect.
	 */
	public static RGB getPreferenceColor(IPreferenceStore prefs, String key)
	{
		if(prefs == null || !prefs.contains(key))
		{
			return null;
		}
		
		String s = prefs.getString(key).trim();
		
		if(s.length() == 0)
		{
			return null;
		}
		
		String[] values = s.split("\\,"); //$NON-NLS-1$
		
		if(values.length != 3)
		{
			Activator.getDefault().log(IStatus.WARNING, MessageFormat.format("Cannot parse preference value for {0}: {1}", key, s), null); //$NON-NLS-1$
			return null;
		}
		
		int r, g, b;
		
		try
		{
			r = Integer.parseInt(values[0]);
			g = Integer.parseInt(values[1]);
			b = Integer.parseInt(values[2]);
		}
		catch(NumberFormatException ex)
		{
			Activator.getDefault().log(IStatus.WARNING, MessageFormat.format("Cannot parse preference value for {0}: {1}", key, s), ex); //$NON-NLS-1$
			return null;
		}
		
		return new RGB(r, g, b);
	}
	
	/**
	 * Returns the console background colour from the DebugUI preferences.
	 * 
	 * @return Console background colour.
	 */
	public static final RGB getConsoleBackgroundColor()
	{
		return getPreferenceColor(getDebugUiPreferences(), PREF_DEBUG_CONSOLE_BACKGROUND_COLOR);
	}
	
	/**
	 * Returns the console text colour from the DebugUI preferences.
	 * 
	 * @return Console text colour.
	 */
	public static final RGB getConsoleTextColor()
	{
		return getPreferenceColor(getDebugUiPreferences(), PREF_DEBUG_CONSOLE_SYS_OUT_COLOR);
	}
	
	/**
	 * Returns the Grep View background colour. Uses the console background colour
	 * if no colour was specified in the settings.
	 * 
	 * @return Grep View background colour.
	 */
	public static final RGB getGrepViewBackgroundColor()
	{
		String s = Activator.getDefault().getPreferenceStore().getString(Activator.PREFS_GREP_VIEW_BACKGROUND_COLOR);
		
		if(s == null || s.length() == 0)
		{
			return getConsoleBackgroundColor();
		}
		else
		{
			return stringToRgb(s);
		}
	}
	
	/**
	 * Returns the Grep View text colour. Uses the console text colour
	 * if no colour was specified in the settings.
	 * 
	 * @return Grep View text colour.
	 */
	public static final RGB getGrepViewTextColor()
	{
		String s = Activator.getDefault().getPreferenceStore().getString(Activator.PREFS_GREP_VIEW_FOREGROUND_COLOR);
		
		if(s == null || s.length() == 0)
		{
			return getConsoleTextColor();
		}
		else
		{
			return stringToRgb(s);
		}
	}

	/**
	 * Returns the RGB value of the configured notification foreground colour.
	 * 
	 * @return Notification foreground colour RGB value.
	 */
	public static final RGB getNotificationForegroundColor()
	{
		String s = Activator.getDefault().getPreferenceStore().getString(Activator.PREFS_NOTIFICATION_FOREGROUND_COLOR);
		
		if(s == null || s.length() == 0)
		{
			return null;
		}
		else
		{
			return stringToRgb(s);
		}
	}
	
	/**
	 * Returns the RGB value of the configured notification background colour.
	 * 
	 * @return Notification background colour RGB value.
	 */
	public static final RGB getNotificationBackgroundColor()
	{
		String s = Activator.getDefault().getPreferenceStore().getString(Activator.PREFS_NOTIFICATION_BACKGROUND_COLOR);
		
		if(s == null || s.length() == 0)
		{
			return null;
		}
		else
		{
			return stringToRgb(s);
		}
	}

	/**
	 * Creates a label in a preference page.
	 * 
	 * @param parent Parent control.
	 * @param text Label text.
	 * 
	 * @return Label.
	 */
	public static Label createPreferenceLabel(Composite parent,
      String text)
  {
		Label label = new Label(parent, SWT.NONE);
		
		if(text != null)
		{
			label.setText(text);
		}
		
		GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
		data.verticalIndent = charsToPixelDimensions(parent.getDisplay(), 1, 1).y;
		label.setLayoutData(data);

		return label;
  }
}
