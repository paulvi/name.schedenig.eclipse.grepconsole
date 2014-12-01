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

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;

/**
 * A grep expression item element. Expression items are leaf elements.
 * 
 * @author msched
 */
public class GrepExpressionItem extends AbstractGrepModelElement
{
	/** Regular expression string. */
	private String grepExpression;
	
	/** Regular expression string for the quick expression. */
	private String quickGrepExpression;
	
	/** Regular expression string for the "unless" expression. */
	private String unlessGrepExpression;

	/** An array of groups. The first group matches is used for the entire matched
	 *  line, the remaining groups are used for the corresponding capture 
	 *  groups. */
	private GrepGroup groups[];
	
	/** Compiled pattern. Set on demand. */
	private Pattern pattern;

	/** Compiled unless pattern. Set on demand. */
	private Pattern unlessPattern;

	/** Compiled quick pattern. Set on demand. */
	private Pattern quickPattern;
	
	/** Whether the patterns should be case insensitive. */
	private boolean caseInsensitive;
	
	/** Whether to remove any original style the matched line may have. */
	private boolean removeOriginalStyle;

	/** Optional rewrite expression. Replaces the original line in the grep view
	 *  if set. */
	private String rewriteExpression;

	/** Similar to the groups array, but applies to the rewritten line and groups
	 *  defined by the rewrite expression. */
	private GrepGroup[] rewriteGroups;
	
	/** Optional autostart link. */
	private IGrepLink autostartLink;

	/** Whether to use notifications. */
	private boolean popupNotification;
	
	/** Notification title. */
	private String notificationTitle;
	
	/** Notification message. */
	private String notificationMessage;

	/** Optional notification link. */
	private IGrepLink notificationLink;

	/** Optional path to a notification sound file. */
	private String soundNotificationPath;
	
	/** Label for an optional statistics count entry. */
	private String statisticsCountLabel;
	
	/** Label for an optional statistics label entry. */
	private String statisticsValueLabel;
	
	/** Optional pattern for the statistics label entry. */
	private String statisticsValuePattern;
	
	/**
	 * Creates a new instance, generating a new ID.
	 */
	public GrepExpressionItem()
	{
		this((String) null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id ID. It is the caller's responsibility to ensure the ID is unique.
	 */
	public GrepExpressionItem(String id)
	{
		super(id);
		
		groups = new GrepGroup[]{};
		rewriteGroups = new GrepGroup[]{};
	}

	/**
	 * Creates a new instance by copying the specified source item.
	 * 
	 * @param src Source item.
	 * @param identityCopy If this is <code>true</code>, all IDs will be copied as
	 * 		well. Otherwise, copied elements will be assigned new IDs.
	 */
	public GrepExpressionItem(GrepExpressionItem src, boolean identityCopy)
	{
		super(src, identityCopy);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#copy(boolean)
	 */
	@Override
	public GrepExpressionItem copy(boolean identityCopy)
	{
		return new GrepExpressionItem(this, identityCopy);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#copyFrom(name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement, boolean)
	 */
	@Override
	public void copyFrom(AbstractGrepModelElement src, boolean identityCopy)
	{
		super.copyFrom(src, identityCopy);
		
		GrepExpressionItem item = (GrepExpressionItem) src; 
		grepExpression = item.grepExpression;
		quickGrepExpression = item.quickGrepExpression;
		unlessGrepExpression = item.unlessGrepExpression;
		rewriteExpression = item.getRewriteExpression();
		
		groups = new GrepGroup[item.groups.length];
		
		for(int i = 0; i < groups.length; i++)
		{
			groups[i] = new GrepGroup(item.groups[i], true);
		}
		
		rewriteGroups = item.rewriteGroups == null ? null : new GrepGroup[item.rewriteGroups.length];
		
		if(rewriteGroups != null)
		{
			for(int i = 0; i < rewriteGroups.length; i++)
			{
				rewriteGroups[i] = new GrepGroup(item.rewriteGroups[i], true);
			}
		}
		
		caseInsensitive = item.caseInsensitive;
		removeOriginalStyle = item.removeOriginalStyle;
		autostartLink = item.autostartLink == null ? null : item.autostartLink.copy();
		
		popupNotification = item.popupNotification;
		notificationTitle = item.notificationTitle;
		notificationMessage = item.notificationMessage;
		notificationLink = item.notificationLink == null ? null : item.notificationLink.copy();
		
		statisticsCountLabel = item.statisticsCountLabel;
		statisticsValueLabel = item.statisticsValueLabel;
		statisticsValuePattern = item.statisticsValuePattern;
		
		soundNotificationPath = item.soundNotificationPath;
	}

	/**
	 * Returns the regular expression string.
	 * 
	 * @return Regular expression string.
	 */
	public String getGrepExpression()
	{
		return grepExpression;
	}

	/**
	 * Sets the regular expression string.
	 * 
	 * @param grepExpression Regular expression string.
	 */
	public void setGrepExpression(String grepExpression)
	{
		if(this.grepExpression == null || grepExpression == null 
				|| !this.grepExpression.equals(grepExpression))
		{
			this.grepExpression = grepExpression;
			this.pattern = null;
		}
	}

	/**
	 * Returns the regular expression string for the "unless" expression.
	 * 
	 * @return Regular "unless" expression string.
	 */
	public String getUnlessGrepExpression()
	{
		return unlessGrepExpression;
	}
	
	/**
	 * Sets the regular expression string for the "unless" expression.
	 * 
	 * @param grepExpression Regular "unless" expression string.
	 */
	public void setUnlessGrepExpression(String unlessGrepExpression)
	{
		if(this.unlessGrepExpression == null || unlessGrepExpression == null 
				|| !this.unlessGrepExpression.equals(unlessGrepExpression))
		{
			this.unlessGrepExpression = unlessGrepExpression;
			this.unlessPattern = null;
		}
	}
	
	/**
	 * Returns the groups.
	 * 
	 * @return Groups.
	 */
	public GrepGroup[] getGroups()
	{
		return groups;
	}

	/**
	 * Sets the groups.
	 * 
	 * @param groups Groups.
	 */
	public void setGroups(GrepGroup[] groups)
	{
		this.groups = groups;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#findStyleUses(name.schedenig.eclipse.grepconsole.model.GrepStyle, java.util.Set)
	 */
	@Override
	public void findStyleUses(GrepStyle style, Set<GrepExpressionItem> items)
	{
		for(GrepGroup group: groups)
		{
			if(group.getStyle() == style)
			{
				items.add(this);
				break;
			}
		}
	}

	/**
	 * Returns the compiled pattern.
	 * 
	 * @return Pattern. <code>null</code> if the grep expression could not be
	 * 		compiled.
	 */
	public Pattern getPattern()
	{
		if(pattern == null && grepExpression != null)
		{
			pattern = compilePattern(grepExpression);
		}
		
		return pattern;
	}

	/**
	 * Returns the compiled pattern for the "unless" expression.
	 * 
	 * @return Pattern. <code>null</code> if the "unless" grep expression is
	 * 		<code>null</code> or could not be compiled.
	 */
	public Pattern getUnlessPattern()
	{
		if(unlessPattern == null && unlessGrepExpression != null)
		{
			unlessPattern = compilePattern(unlessGrepExpression);
		}
		
		return unlessPattern;
	}
	
	/**
	 * Compiles the specified grep expression. Swallows exceptions caused by
	 * invalid expressions.
	 * 
	 * @param Expression.
	 * 
	 * @return The compiled pattern, or <code>null</code> if an error occurs.
	 */
	private Pattern compilePattern(String expression)
	{
		Pattern pattern;
		
		try
		{
			pattern = Pattern.compile(expression, computeFlags()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(PatternSyntaxException ex)
		{
			pattern = null;
		}
		
		return pattern;
	}

	/**
	 * Computes flags for the regular expression pattern.
	 * 
	 * @return Flags.
	 */
	private int computeFlags()
	{
		return caseInsensitive ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement#refreshStyles()
	 */
	@Override
	protected void refreshStyles()
	{
		for(int i = 0; i < groups.length; i++)
		{
			if(groups[i].getStyle() != null)
			{
				groups[i].setStyle(getRoot().getStyle(groups[i].getStyle().getId()));
			}
		}
	}

	/**
	 * Returns whether the regular expressions for this item should be case
	 * insensitive.
	 * 
	 * @return <code>true</code> for case insensitivity.
	 */
	public boolean isCaseInsensitive()
	{
		return caseInsensitive;
	}

	/**
	 * Sets whether the regular expressions for this item should be case
	 * insensitive.
	 * 
	 * @param caseInsensitive <code>true</code> for case insensitivity.
	 */
	public void setCaseInsensitive(boolean caseInsensitive)
	{
		if(caseInsensitive != this.caseInsensitive)
		{
			this.caseInsensitive = caseInsensitive;
			this.pattern = null;
			this.unlessPattern = null;
		}
	}
	
	/**
	 * Returns whether to remove any original style the matched line may have.
	 * 
	 * @return Whether to remove any original style the matched line may have.
	 */
	public boolean isRemoveOriginalStyle()
	{
		return removeOriginalStyle;
	}
	
	/**
	 * Sets whether to remove any original style the matched line may have.
	 * 
	 * @param removeOriginalStyle Whether to remove any original style the matched
	 * 		line may have.
	 */
	public void setRemoveOriginalStyle(boolean removeOriginalStyle)
	{
		this.removeOriginalStyle = removeOriginalStyle;
	}

	/**
	 * Returns the rewrite expression.
	 * 
	 * @return Rewrite expression. May be <code>null</code>.
	 */
	public String getRewriteExpression()
	{
		return rewriteExpression;
	}
	
	/**
	 * Sets the rewrite expression.
	 * 
	 * @param rewriteExpression Rewrite expression. May be <code>null</code>.
	 */
	public void setRewriteExpression(String rewriteExpression)
	{
		this.rewriteExpression = rewriteExpression;
	}

	/**
	 * Returns the rewrite groups.
	 * 
	 * @return Rewrite groups.
	 */
	public GrepGroup[] getRewriteGroups()
	{
		return rewriteGroups;
	}
	
	/**
	 * Sets the rewrite groups.
	 * 
	 * @param rewriteGroups Rewrite groups.
	 */
	public void setRewriteGroups(GrepGroup[] rewriteGroups)
	{
		this.rewriteGroups = rewriteGroups;
	}

	/**
	 * Returns the autostart link.
	 * 
	 * @return Autostart link.
	 */
	public IGrepLink getAutostartLink()
	{
		return autostartLink;
	}

	/**
	 * Sets the autostart link.
	 * 
	 * @param autostartLink Autostart link.
	 */
	public void setAutostartLink(IGrepLink autostartLink)
	{
		this.autostartLink = autostartLink;
	}

	/**
	 * Returns the quick expression string.
	 * 
	 * @return Quick expression string. May be <code>null</code>.
	 */
	public String getQuickGrepExpression()
	{
		return quickGrepExpression;
	}

	/**
	 * Sets the qucik expression string.
	 * 
	 * @param quickGrepExpression Quick expression string. May be
	 * 		<code>null</code>. 
	 */
	public void setQuickGrepExpression(String quickGrepExpression)
	{
		if(this.quickGrepExpression == null || quickGrepExpression == null 
				|| !this.quickGrepExpression.equals(quickGrepExpression))
		{
			this.quickGrepExpression = quickGrepExpression;
			this.quickPattern = null;
		}
	}

	/**
	 * Returns the compiled quick expression pattern.
	 * 
	 * @return Quick expression pattern. <code>null</code> if the quick expression
	 * 		could not be compiled.
	 */
	public Pattern getQuickPattern()
	{
		if(quickPattern == null && quickGrepExpression != null)
		{
			quickPattern = compilePattern(quickGrepExpression);
		}
		
		return quickPattern;
	}

	/**
	 * Returns whether or not to use notifications.
	 * 
	 * @return Whether or not to use notifications.
	 */
	public boolean isPopupNotification()
	{
		return popupNotification;
	}

	/**
	 * Sets whether or not to use notifications.
	 * 
	 * @param popupNotification Whether or not to use notifications.
	 */
	public void setPopupNotification(boolean popupNotification)
	{
		this.popupNotification = popupNotification;
	}

	/**
	 * Returns the notification title pattern.
	 * 
	 * @return Notification title.
	 */
	public String getNotificationTitle()
	{
		return notificationTitle;
	}

	/**
	 * Sets the notification title pattern.
	 * 
	 * @param notificationTitle Notification title.
	 */
	public void setNotificationTitle(String notificationTitle)
	{
		this.notificationTitle = notificationTitle;
	}

	/**
	 * Returns the notification title pattern.
	 * 
	 * @return Notification title pattern.
	 */
	public String getNotificationMessage()
	{
		return notificationMessage;
	}

	/**
	 * Sets the notification title pattern.
	 * 
	 * @param notificationMessage Notification title pattern.
	 */
	public void setNotificationMessage(String notificationMessage)
	{
		this.notificationMessage = notificationMessage;
	}

	/**
	 * Returns the notification link action.
	 * 
	 * @return Notification link action.
	 */
	public IGrepLink getNotificationLink()
	{
		return notificationLink;
	}

	/**
	 * Sets the notification link action.
	 * 
	 * @param notificationLink Notification link action.
	 */
	public void setNotificationLink(IGrepLink notificationLink)
	{
		this.notificationLink = notificationLink;
	}

	/**
	 * Returns the notification sound path.
	 * 
	 * @return Notification sound path.
	 */
	public String getSoundNotificationPath()
	{
		return soundNotificationPath;
	}

	/**
	 * Sets the notification sound path.
	 * 
	 * @param soundNotificationPath Notification sound path.
	 */
	public void setSoundNotificationPath(String soundNotificationPath)
	{
		this.soundNotificationPath = soundNotificationPath;
	}

	public String getStatisticsCountLabel()
	{
		return statisticsCountLabel;
	}

	public void setStatisticsCountLabel(String statisticsCountLabel)
	{
		this.statisticsCountLabel = statisticsCountLabel;
	}

	public String getStatisticsValueLabel()
	{
		return statisticsValueLabel;
	}

	public void setStatisticsValueLabel(String statisticsValueLabel)
	{
		this.statisticsValueLabel = statisticsValueLabel;
	}

	public String getStatisticsValuePattern()
	{
		return statisticsValuePattern;
	}

	public void setStatisticsValuePattern(String statisticsValuePattern)
	{
		this.statisticsValuePattern = statisticsValuePattern;
	}
}	
