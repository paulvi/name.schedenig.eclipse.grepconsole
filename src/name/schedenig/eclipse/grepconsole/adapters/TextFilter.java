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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener;
import name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter;
import name.schedenig.eclipse.grepconsole.adapters.links.LinkMatch;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.notifications.GrepConsoleNotification;
import name.schedenig.eclipse.popupnotifications.INotificationListener;
import name.schedenig.eclipse.popupnotifications.Notification;
import name.schedenig.eclipse.popupnotifications.PopupNotificationManager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * A text change listener that presents a subset of the original content's lines
 * depending on a list of filter expressions.
 *  
 * @author msched
 */
public class TextFilter implements TextChangeListener, StyledTextContent, IGrepConsoleListener, INotificationListener
{
	/**
	 * Holds a line's text and styles.
	 * 
	 * @author msched
	 */
	public static class Line
	{
		/** Line text. */
		private String text;
		
		/** Whether the line has been rewritten for output. */
		private boolean rewritten;
		
		/** Grep style ranges for the line. */
		private GrepStyleRange[] ranges;

		/**
		 * Creates a new instance.
		 * 
		 * @param text Line text.
		 * @param rewritten Whether the line has been rewritten for output.
		 * @param ranges Grep style ranges for the line.
		 */
		public Line(String text, boolean rewritten, GrepStyleRange[] ranges)
		{
			this.text = text;
			this.rewritten = rewritten;
			this.ranges = ranges;
		}
		
		/**
		 * Returns the line text.
		 * 
		 * @return Line text.
		 */
		public String getText()
		{
			return text;
		}
		
		/**
		 * Returns the grep style ranges for the line.
		 * 
		 * @return Grep style ranges.
		 */
		public GrepStyleRange[] getRanges()
		{
			return ranges;
		}
		
		/**
		 * Returns whether the line has been rewritten for output.
		 * 
		 * @return Whether the line has been rewritten.
		 */
		public boolean isRewritten()
		{
			return rewritten;
		}
	}

	/** Original content. */
	private StyledTextContent originalContent;
	
	/** List of filter expressions. */
	private LinkedList<GrepExpressionItem> filterExpressions;
	
	/** List of statistics expressions. */
	private LinkedHashSet<GrepExpressionItem> statisticsExpressions;
	
	/** Filtered lines. */
	private LinkedList<Line> lines = new LinkedList<Line>();
	
	/** Character offsets of the filtered lines. */
	private LinkedList<Integer> lineOffsets = new LinkedList<Integer>();
	
	/** Character offsets, in the original text content, of the filtered lines. */
	private LinkedList<Integer> originalOffsets = new LinkedList<Integer>();
	
	/** Number of characters in the filtered text. */
	private int charCount;
	
	/** Line delimiter. */
	private String delimiter;
	
	/** Text that has been added to the original content but could not yet be
	 *  filtered because the line has not yet been completed. */
	private StringBuilder bufferedText;
	
	/** Index in the parsing of the line delimiter string of the buffered text.
	 *  If this is greater than 0, the beginning of the delimiter string has been
	 *  found at the end of the buffered text. */
	private int bufferedDelimiterIndex;
	
	/** Whether a textChanging event was sent after the last call to
	 *  textChanged(). */
	private boolean sentChanging;
	
	/** Set of listeners. */
	private LinkedHashSet<TextChangeListener> listeners = new LinkedHashSet<TextChangeListener>();

	/** Maximum number of characters to match per line. */
	private int matchLength;

	/** Keeps track of the current offset in the original text content. */
	private int originalOffset;
	
	/** Keeps track of deleted characters that have to be subtracted from the
	 *  original offset when the length of the original content exceeds the
	 *  console buffer size. */
	private int originalOffsetDelta;

	/** Highest line offset of already processed lines. Prevents autostart links
	 *  from earlier lines to be re-executed when refreshing the content. */
	private int maxProcessedLineOffset = -1;

	/** Shell for executing links. */
	private Shell shell;

	/** Optional project for executing links. */
	private IProject project;

	/** Set of expression items which have autostart links. Created on demand. */
	private Set<GrepExpressionItem> actionItems;

	/** Set of expression items which have active notifications. */
	private LinkedHashSet<GrepExpressionItem> notificationExpressions;

	private Map<String, StatisticsEntry> statisticEntries;

	private LinkedHashSet<IStatisticsListener> statisticsListeners = new LinkedHashSet<IStatisticsListener>();

	/**
	 * Creates a new instance.
	 * 
	 * @param originalContent Original text content.
	 * @param filterExpressions List of expressions to use for filtering.
	 * @param notificationExpressions 
	 */
	public TextFilter(StyledTextContent originalContent, LinkedList<GrepExpressionItem> filterExpressions,
			LinkedHashSet<GrepExpressionItem> statisticsExpressions, 
			LinkedHashSet<GrepExpressionItem> notificationExpressions, 
			Shell shell, IProject project)
	{
		this.originalContent = originalContent;
		this.filterExpressions = filterExpressions;
		this.statisticsExpressions = statisticsExpressions;
		this.notificationExpressions = notificationExpressions;
		this.shell = shell;
		this.project = project;
		
		Activator activator = Activator.getDefault();
		activator.addListener(this);
		this.matchLength = activator.getFilterMatchLength();
		statisticEntries = new HashMap<String, StatisticsEntry>();
	}

	/**
	 * Disposes the text filter.
	 */
	public void dispose()
	{
		unhookListener();
		
		Activator.getDefault().removeListener(this);
	}
	
	/**
	 * Returns the original, unfiltered text content.
	 * 
	 * @return Original content.
	 */
	public StyledTextContent getOriginalContent()
	{
		return originalContent;
	}
	
	/**
	 * Recalculates the filtered text.
	 */
	public void refresh()
	{
//		statisticCountEntries = new HashMap<String, StatisticsEntry>();
//		statisticValueEntries = new HashMap<String, StatisticsEntry>();
		clearStatistics();
		delimiter = originalContent.getLineDelimiter();
		
		String text = filterExpressions.isEmpty() ? "" : originalContent.getTextRange(0, originalContent.getCharCount()); //$NON-NLS-1$
		setText(text);
		
		if(filterExpressions.isEmpty() && statisticsExpressions.isEmpty() && notificationExpressions.isEmpty()) // experimental
		{
			unhookListener();
		}
		else
		{
			hookListener();
		}
	}
	
	/**
	 * Clears the previous text and filters the new text when new text content
	 * is set.
	 * 
	 * @see org.eclipse.swt.custom.StyledTextContent#setText(java.lang.String)
	 */
	@Override
	public void setText(String s)
	{
		clear();
		bufferedText = new StringBuilder();
		bufferedDelimiterIndex = 0;

		if(s.length() > 0)
		{
			handleNewText(s, false);
		}
		
		TextChangedEvent event = new TextChangedEvent(this);
		
		for(TextChangeListener listener: listeners)
		{
			listener.textSet(event);
		}
	}

	/**
	 * Clears the existing filtered text.
	 */
	private void clear()
	{
		lines.clear();
		lineOffsets.clear();
		originalOffsets.clear();
		charCount = 0;
		originalOffset = 0;
		originalOffsetDelta = 0;
		
		clearStatistics();
	}
	
	private void clearStatistics()
	{
		for(StatisticsEntry entry: statisticEntries.values())
		{
			fireStatisticsRemoved(entry);
		}
		
		statisticEntries.clear();
	}

	/**
	 * Hooks the change listener on the original content.
	 */
	public void hookListener()
	{
		if(!filterExpressions.isEmpty() || !statisticsExpressions.isEmpty() || !notificationExpressions.isEmpty())
		{
			originalContent.addTextChangeListener(this);
		}
	}

	public void unhookListener()
	{
		originalContent.removeTextChangeListener(this);
	}
	
	/**
	 * Handles new text. Checks every complete line of text against the filter
	 * expressions and updates the filtered text accordingly. Keeps unfinished
	 * lines in the buffer for later completion.
	 * 
	 * Returns a text changing event if the filtered text has changed. The caller
	 * is responsible for sending the event to listeners.
	 * 
	 * @param s Newly added text. All text is assumed to be added at the end of
	 * 		the existing text.
	 * @param notifications Whether to use create notifications for items that
	 * 		have notifications configured.
	 * 
	 * @return Change event, or <code>null</code>.
	 */
	protected TextChangingEvent handleNewText(String s, boolean notifications)
	{
		TextChangingEvent event = new TextChangingEvent(this);
		StringBuilder newText = new StringBuilder();
		
		event.start = charCount;
		int startIndex = 0;
		
		for(int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			
			if(delimiter.charAt(bufferedDelimiterIndex) == c)
			{
				if(bufferedDelimiterIndex < delimiter.length() - 1)
				{
					bufferedDelimiterIndex++;
				}
				else
				{
					String line = bufferedText.toString() + s.substring(startIndex, i + 1);
					line = line.substring(0, line.length() - delimiter.length());
					
					if(handleNewLine(line, notifications))
					{
						event.newCharCount += line.length() + delimiter.length();
						event.newLineCount++;
						newText.append(line);
					}
					
					originalOffset += line.length() + delimiter.length();
					bufferedText = new StringBuilder();
					bufferedDelimiterIndex = 0;
					startIndex = i + 1;
				}
			}
			else
			{
				bufferedDelimiterIndex = 0;
			}
		}
		
		if(startIndex < s.length())
		{
			bufferedText.append(s.substring(startIndex));
		}
		
		if(event.newCharCount == 0)
		{
			return null;
		}
		
		event.newText = newText.toString();
		return event;
	}

	/**
	 * Tests a newly added line against the filter expressions and adds it to the
	 * filtered text if at least one filter expression matches.
	 * 
	 * @param line New line of text.
	 * 
	 * @return <code>true</code> iff the line was added to the filtered text.
	 */
	private boolean handleNewLine(String line, boolean notifications)
	{
		boolean filter = !listeners.isEmpty();
		
		int cutoff = line.length();
		
		while(cutoff > 0)
		{
			char c = line.charAt(cutoff - 1);
			
			if(c == '\n' || c == '\r')
			{
				cutoff--;
			}
			else
			{
				break;
			}
		}
		
		boolean result = false;
		LinkedHashSet<GrepExpressionItem> remainingActionItems = null;
		
		boolean notYetProcessed = originalOffset >= maxProcessedLineOffset;
		
		if(!statisticsExpressions.isEmpty() || (notifications && notYetProcessed))
		{
			remainingActionItems = new LinkedHashSet<GrepExpressionItem>(getActionItems(notYetProcessed));
		}
		
		List<GrepExpressionItem> filterExpressions = filter ? this.filterExpressions : Collections.<GrepExpressionItem>emptyList();
		line = line.substring(0, cutoff);
		
		for(GrepExpressionItem item: filterExpressions)
		{
			if(remainingActionItems != null)
			{
				remainingActionItems.remove(item);
			}
			
			Pattern quickPattern = item.getQuickPattern();
			String matchLine = (matchLength <= 0 || line.length() <= matchLength) ? line : line.substring(0, matchLength);
			
			if(quickPattern != null)
			{
				if(!quickPattern.matcher(matchLine).find())
				{
					continue;
				}
			
				matchLine = line;
			}
			
			Pattern pattern = item.getPattern();
			Matcher matcher = pattern.matcher(matchLine);
			
			if(!matcher.find())
			{
				continue;
			}
			
			Pattern unlessPattern = item.getUnlessPattern();
			
			if(unlessPattern != null)
			{
				Matcher unlessMatcher = unlessPattern.matcher(matchLine);
				
				if(unlessMatcher.find())
				{
					continue;
				}
			}

			if(remainingActionItems != null)
			{
				if(statisticsExpressions.contains(item))
				{
					handleMatchedStatisticsItem(item, matchLine, matcher);
				}
				
				if(notYetProcessed && notificationExpressions.contains(item))
				{
					handleMatchedNotificationsItem(item, matchLine, matcher);
				}
				/*
				LinkMatch linkMatch = null;
				boolean isNotificationItem = notificationExpressions.contains(item);
				boolean autostart = isNotificationItem && item.getAutostartLink() != null;
				boolean popup = isNotificationItem && item.isPopupNotification();
				boolean sound = isNotificationItem && item.getSoundNotificationPath() != null;
				
				if(autostart || popup)
				{
					linkMatch = new LinkMatch(item, item.getAutostartLink(), matcher.toMatchResult(), matcher.start(), line, 0, project);
				}
				
				if(sound)
				{
					playSound(item.getSoundNotificationPath());
				}
				
				if(popup)
				{
					Notification notification = createNotification(linkMatch);
					Activator.getDefault().getPopupNotificationManager().addNotification(notification);
				}
				
				if(autostart)
				{
					executeLink(linkMatch);
				}
				*/
			}
			
			if(item.getRewriteExpression() == null)
			{
				addLine(line, false, null);
			}
			else
			{
				List<int[]> rangeIndices = new LinkedList<int[]>();
				LinkMatch linkMatch = new LinkMatch(item, item.getAutostartLink(), matcher.toMatchResult(), matcher.start(), line, 0, project);
				String rewritten = GrepConsoleUtil.replaceParams(item.getRewriteExpression(), matcher.toMatchResult(), line, -1, rangeIndices, project, new GroupParameterResolver(linkMatch));
				
				GrepGroup[] groups = item.getRewriteGroups();
				GrepStyleRange[] ranges;
				
				if(groups == null || groups.length == 0)
				{
					ranges = null;
				}
				else
				{
					LinkedList<GrepStyleRange> rangeList = new LinkedList<GrepStyleRange>();
					
					if(groups[0] != null && (groups[0].getStyle() != null || groups[0].getLink() != null))
					{
						rangeList.add(new GrepStyleRange(item, 0, true, 0, rewritten.length() - 1, groups[0].getStyle(), groups[0].getLink(), matcher, rewritten, 0, project));
					}
					
					int i = 0;
					
					for(int[] indices: rangeIndices)
					{
						i++;
						
						if(groups[i] != null && (groups[i].getStyle() != null || groups[i].getLink() != null))
						{
							GrepStyleRange range = new GrepStyleRange(item, 0, false, indices[0], indices[1], groups[i].getStyle(), groups[i].getLink(), matcher, rewritten, -1, project);
							rangeList.add(range);
						}
					}
					
					if(rangeList.isEmpty())
					{
						ranges = null;
					}
					else
					{
						ranges = rangeList.toArray(new GrepStyleRange[rangeList.size()]);
					}
				}
				
				addLine(rewritten, true, ranges);
			}
			
			result = true;
			break;
		}

		// Here we handle all items remaining after we have already decided that the
		// line should be shown in Grep View
		if(remainingActionItems != null && !remainingActionItems.isEmpty())
		{
			String matchLine = (matchLength <= 0 || line.length() <= matchLength) ? line : line.substring(0, matchLength);
			
			for(GrepExpressionItem item: remainingActionItems)
			{
				if(statisticsExpressions.contains(item) || notificationExpressions.contains(item))
				{
					Pattern quickPattern = item.getQuickPattern();
					
					if(quickPattern != null && !quickPattern.matcher(matchLine).find())
					{
						continue;
					}
					
					Pattern pattern = item.getPattern();
					Matcher matcher = pattern.matcher(matchLine);
					
					if(matcher.find())
					{
						if(statisticsExpressions.contains(item))
						{
							handleMatchedStatisticsItem(item, line, matcher);
						}
						
						if(notYetProcessed && notificationExpressions.contains(item))
						{
							handleMatchedNotificationsItem(item, line, matcher);
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * @param item
	 * @param line
	 * @param matcher
	 */
	private void handleMatchedNotificationsItem(GrepExpressionItem item,
			String line, Matcher matcher)
	{
		LinkMatch linkMatch;
		
		if(item.isPopupNotification() || item.getAutostartLink() != null)
		{
			linkMatch = new LinkMatch(item, item.getAutostartLink(), matcher.toMatchResult(), matcher.start(), line, 0, project);
		}
		else
		{
			linkMatch = null;
		}

		if(item.getSoundNotificationPath() != null)
		{
			playSound(item.getSoundNotificationPath());
		}
		
		if(item.isPopupNotification())
		{
			Notification notification = createNotification(linkMatch);
			//Activator.getDefault().getPopupNotificationManager().
			PopupNotificationManager man = name.schedenig.eclipse.popupnotifications.Activator.getDefault().getManager();
			man.addNotification(notification);
		}
		
		if(item.getAutostartLink() != null)
		{
			executeLink(linkMatch);
		}
	}

	/**
	 * @param item
	 * @param matcher 
	 * @param line 
	 */
	private void handleMatchedStatisticsItem(GrepExpressionItem item, String line, Matcher matcher)
	{
		if(item.getStatisticsCountLabel() != null)
		{
			String label = item.getStatisticsCountLabel();
			StatisticsEntry entry = statisticEntries.get(label);
			boolean isNew;
			
			if(entry == null)
			{
				entry = new StatisticsEntry(StatisticsEntry.Type.COUNT, label, 1);
				statisticEntries.put(label, entry);
				isNew = true;
			}
			else
			{
				int value = entry.getValue() instanceof Integer ? (Integer) entry.getValue() : 0;
				entry.setValue(value + 1);//(Integer) (entry.getValue()) + 1);
				isNew = false;
			}
			
			fireStatisticsUpdated(entry, isNew);
		}
		
		if(item.getStatisticsValueLabel() != null)
		{
			String value;

			if(item.getStatisticsValuePattern() == null)
			{
				value = matcher.group(0);
			}
			else
			{
				List<int[]> rangeIndices = new LinkedList<int[]>();
				LinkMatch linkMatch = new LinkMatch(item, item.getAutostartLink(), matcher.toMatchResult(), matcher.start(), line, 0, project);
				value = GrepConsoleUtil.replaceParams(item.getStatisticsValuePattern(), matcher.toMatchResult(), line, -1, rangeIndices, project, new GroupParameterResolver(linkMatch));
			}
			
			String label = item.getStatisticsValueLabel();
			StatisticsEntry entry = statisticEntries.get(label);
			boolean isNew;
			
			if(entry == null)
			{
				entry = new StatisticsEntry(StatisticsEntry.Type.LABEL, label, value);
				statisticEntries.put(label, entry);
				isNew = true;
			}
			else
			{
				entry.setValue(value);
				isNew = false;
			}
			
			fireStatisticsUpdated(entry, isNew);
		}
	}

	/**
	 * Plays a sound
	 * 
	 * @param path Sound file path.
	 */
	private void playSound(String path)
	{
		Activator activator = Activator.getDefault();
		
		try
		{
			activator.getSoundManager().playSound(path);
		}
		catch(UnsupportedAudioFileException ex)
		{
			activator.log(IStatus.ERROR, ex);
		}
		catch(IOException ex)
		{
			activator.log(IStatus.ERROR, ex);
		}
		catch(LineUnavailableException ex)
		{
			activator.log(IStatus.ERROR, ex);
		}
	}

	/**
	 * Creates a notification.
	 * 
	 * @param linkMatch Link match.
	 * 
	 * @return Notification.
	 */
	private Notification createNotification(LinkMatch linkMatch)
	{
		Notification notification = new GrepConsoleNotification(linkMatch);
		
		GrepExpressionItem item = linkMatch.getItem();
		GroupParameterResolver resolver = new GroupParameterResolver(linkMatch);
		notification.setTitle(GrepConsoleUtil.replaceParams(item.getNotificationTitle(), linkMatch, resolver));
		notification.setMessage(GrepConsoleUtil.replaceParams(item.getNotificationMessage(), linkMatch, resolver));
		notification.setStyle(Activator.getDefault().getNotificationStyle());
		
		notification.addListener(this);
		
		return notification;
	}
	
	/**
	 * Returns the set of expression items with notifications or autostart links. 
	 * Creates it if necessary.
	 * 
	 * @return Expression items with notifications or autostart links.
	 */
	private Set<GrepExpressionItem> getActionItems(boolean includeNotifications)
	{
		if(actionItems == null)
		{
			actionItems = new LinkedHashSet<GrepExpressionItem>();
			
			if(statisticsExpressions != null)
			{
				actionItems.addAll(statisticsExpressions);
			}
			
			if(includeNotifications && notificationExpressions != null)
			{
				actionItems.addAll(notificationExpressions);
			}
		}
		
		return actionItems;
	}

	/**
	 * Executes a link.
	 * 
	 * @param linkMatch Link match.
	 */
	private void executeLink(LinkMatch linkMatch)
	{
		GrepLinkAdapter adapter = GrepLineStyleListener.createLinkAdapter(linkMatch, shell, project);
		adapter.openLink();
	}

	/**
	 * Adds a line to the filtered text.
	 * 
	 * @param text New line of text.
	 * @param rewritten Whether the line has been rewritten.
	 * @param ranges Optional array of styles assigned to the line.
	 */
	private void addLine(String text, boolean rewritten, GrepStyleRange[] ranges)
	{
		Line line = new Line(text, rewritten, ranges);
		lines.add(line);
		lineOffsets.add(charCount);
		originalOffsets.add(originalOffset);
		charCount += text.length() + delimiter.length();
		
		maxProcessedLineOffset = Math.max(maxProcessedLineOffset, originalOffset);
	}

	/**
	 * Returns the filtered text as a list of lines.
	 * 
	 * The returned list is the original list used by the text filter instance.
	 * Clients should not modify it.
	 * 
	 * @return Filtered text lines.
	 */
	public LinkedList<Line> getLines()
	{
		return lines;
	}
	
	/**
	 * Returns the line delimiter.
	 * 
	 * @return Line delimiter.
	 */
	public String getDelimiter()
	{
		return delimiter;
	}

	/**
	 * Handles new text when the text of the original content changes. Deleted
	 * text is ignored, and all newly added text is assumed to be added at the
	 * end of the existing content.
	 * 
	 * If the new text causes changes to the filtered text, a matching text
	 * changing event is sent to the listeners.
	 * 
	 * @see org.eclipse.swt.custom.TextChangeListener#textChanging(org.eclipse.swt.custom.TextChangingEvent)
	 */
	@Override
	public void textChanging(TextChangingEvent event)
	{
		boolean clear = event.newCharCount == 0 && event.replaceCharCount == originalOffset;
//		int eventReplaceCharCount = 0;
//		int eventReplaceLineCount = 0;
		
		if(clear)
		{
//			eventReplaceCharCount = getCharCount();
//			eventReplaceLineCount = getLineCount() - 2; // FIXME
			
			// Clearing console
			clear();
		}
		
		TextChangingEvent newEvent = handleNewText(event.newText, true);
		
//		if(clear)
//		{
//			if(newEvent == null)
//			{
//				newEvent = new TextChangingEvent(this);//(StyledTextContent) event.getSource());
//			}
//			
//			newEvent.replaceCharCount = eventReplaceCharCount;
//			newEvent.replaceLineCount = eventReplaceLineCount;
//			newEvent.newText = "";
//		}

		sentChanging = newEvent != null;
		
		if(!clear)
		{
			originalOffsetDelta += event.replaceCharCount;
		}

		if(clear)
		{
			for(TextChangeListener listener: listeners)
			{
				listener.textSet(new TextChangedEvent(this));
			}
		}
		
		if(sentChanging)
		{
			for(TextChangeListener listener: listeners)
			{
				listener.textChanging(newEvent);
			}
		}
	}

	/**
	 * After a text change operation of the original content has completed, also
	 * sends a textChanged event to the listeners if the original text change
	 * caused the filtered text to be changed as well (in which case a
	 * textChanging event has already been sent).
	 *  
	 * @see org.eclipse.swt.custom.TextChangeListener#textChanged(org.eclipse.swt.custom.TextChangedEvent)
	 */
	@Override
	public void textChanged(TextChangedEvent event)
	{
		if(sentChanging)
		{
			sentChanging = false;
			TextChangedEvent newEvent = new TextChangedEvent(this);

			for(TextChangeListener listener: listeners)
			{
				listener.textChanged(newEvent);
			}
		}
	}

	/**
	 * Refreshes the filtered text when the entire original text is changed.
	 * 
	 * @see org.eclipse.swt.custom.TextChangeListener#textSet(org.eclipse.swt.custom.TextChangedEvent)
	 */
	@Override
	public void textSet(TextChangedEvent event)
	{
		refresh();
	}

	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#getCharCount()
	 */
	@Override
	public int getCharCount()
	{
		return charCount;
	}

	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#getLine(int)
	 */
	@Override
	public String getLine(int lineIndex)
	{
		if(lineIndex < 0 || lineIndex >= lines.size())
		{
			return ""; //$NON-NLS-1$
		}
		
		Line line = lines.get(lineIndex);
		return line == null ? null : line.getText();
	}

	/**
	 * Returns the Line instance at the specified line index.
	 * 
	 * @param lineIndex Line index.
	 * 
	 * @return Line element, or <code>null</code>.
	 */
	public Line getLineElement(int lineIndex)
	{
		if(lineIndex < 0 || lineIndex >= lines.size())
		{
			return null;
		}
		
		return lines.get(lineIndex);
	}
	
	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#getLineAtOffset(int)
	 */
	@Override
	public int getLineAtOffset(int offset)
	{
		if(offset >= charCount)
		{
			return lines.size();
		}
		
		int i = lineOffsets.size() - 1;
		Iterator<Integer> itr = lineOffsets.descendingIterator();
		
		while(itr.hasNext())
		{
			int lineOffset = itr.next();
			
			if(lineOffset <= offset)
			{
				break;
			}
			
			i--;
		}
		
		return i;
	}

	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#getLineCount()
	 */
	@Override
	public int getLineCount()
	{
		return lines.size() + 1;
	}

	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#getLineDelimiter()
	 */
	@Override
	public String getLineDelimiter()
	{
		return originalContent.getLineDelimiter();
	}

	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#getOffsetAtLine(int)
	 */
	@Override
	public int getOffsetAtLine(int lineIndex)
	{
		if(lineIndex < 0)
		{
			return 0;
		}
		else if(lineIndex >= lines.size())
		{
			return charCount;
		}
		
		return lineOffsets.get(lineIndex);
	}
	
	/**
	 * Return the character offset, in the original text content, of the first
	 * character of the given line.
	 * 
	 * @param lineIndex Index of the line.
	 * 
	 * @return offset Original offset of the first character of the line. 
	 */
	public int getOriginalOffsetAtLine(int lineIndex)
	{
		if(lines.isEmpty())
		{
			return -1;
		}
		
		if(lineIndex < 0)
		{
			return 0;
		}
		else if(lineIndex >= lines.size())
		{
			return originalOffsets.getLast() - originalOffsetDelta;
		}
		
		return originalOffsets.get(lineIndex) - originalOffsetDelta;
	}

	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#getTextRange(int, int)
	 */
	@Override
	public String getTextRange(int start, int length)
	{
		if(length == 0)
		{
			return ""; //$NON-NLS-1$
		}
		
		int lineIndex = getLineAtOffset(start);
		int startLineOffset = lineOffsets.get(lineIndex);
		int remaining = length;
		StringBuilder sb = new StringBuilder();

		String s = lines.get(lineIndex).getText();
		s = s.substring(start - startLineOffset);
		sb.append(s);
		sb.append(delimiter);
		remaining -= s.length() + delimiter.length();
		
		while(remaining > 0)
		{
			lineIndex++;
			
			if(lineIndex >= lines.size())
			{
				break;
			}
			
			s = lines.get(lineIndex).getText();
			sb.append(s);
			sb.append(delimiter);
			remaining -= s.length() + delimiter.length();
		}
		
		String range = sb.toString();
		
		if(range.length() > length)
		{
			range = range.substring(0, length);
		}
		
		return range;
	}

	/**
	 * Throws an exception if a caller tries to modify the filtered text.
	 * 
	 * @see org.eclipse.swt.custom.StyledTextContent#replaceTextRange(int, int, java.lang.String)
	 */
	@Override
	public void replaceTextRange(int start, int replaceLength, String text)
	{
		Activator.getDefault().log(IStatus.ERROR, "Trying to modify filtered results.", null); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#addTextChangeListener(org.eclipse.swt.custom.TextChangeListener)
	 */
	@Override
	public void addTextChangeListener(TextChangeListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * @see org.eclipse.swt.custom.StyledTextContent#removeTextChangeListener(org.eclipse.swt.custom.TextChangeListener)
	 */
	@Override
	public void removeTextChangeListener(TextChangeListener listener)
	{
		listeners.remove(listener);
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
	 * @see name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener#settingsChanged()
	 */
	@Override
	public void settingsChanged()
	{
		this.matchLength = Activator.getDefault().getFilterMatchLength();
	}

	/**
	 * Returns the grep styles associated with the specified line.
	 * 
	 * @param lineIndex Line index.
	 * 
	 * @return Grep styles or <code>null</code>.
	 */
	public GrepStyleRange[] getLineStyles(int lineIndex)
	{
		if(lineIndex < 0 || lineIndex >= lines.size())
		{
			return null;
		}
		
		Line line = lines.get(lineIndex);
		return line == null ? null : line.getRanges();
	}

	/**
	 * Sets the list of filter expressions to be used by the text filter. Does not
	 * affect already filtered lines. refresh() should be called if previously
	 * filtered content should be refreshed.
	 * 
	 * @param filterExpressions Filter expressions.
	 */
	public void setFilterExpressions(LinkedList<GrepExpressionItem> filterExpressions)
	{
		this.filterExpressions = filterExpressions;
		actionItems = null;
	}
	
	/**
	 * Sets the set of expressions which may contribute to the statistics view.
	 * Does not affect already filtered lines. refresh() should be called to
	 * update listeners if necessary.
	 * 
	 * @param statisticsExpressions Statistics expressions.
	 */
	public void setStatisticsExpressions(LinkedHashSet<GrepExpressionItem> statisticsExpressions)
	{
		this.statisticsExpressions = statisticsExpressions;
		actionItems = null;
	}
	
	/**
	 * Sets the set of expression which may create notifications. Does not affect
	 * already handled lines. refresh() should be called to update listeners if
	 * necessary.
	 * 
	 * @param notificationExpressions Notification expressions.
	 */
	public void setNotificationExpressions(
			LinkedHashSet<GrepExpressionItem> notificationExpressions)
	{
		this.notificationExpressions = notificationExpressions;
		actionItems = null;
	}

	/**
	 * Performs the associated action when a popup notification is clicked, or
	 * activates the Eclipse window if no action has been configured.
	 * 
	 * @see name.schedenig.eclipse.popupnotifications.INotificationListener#notificationClicked(name.schedenig.eclipse.popupnotifications.Notification)
	 */
	@Override
	public void notificationClicked(Notification notification)
	{
		LinkMatch linkMatch = ((GrepConsoleNotification) notification).getLinkMatch();
		GrepExpressionItem item = linkMatch.getItem();
		
		if(item.getNotificationLink() != null)
		{
			LinkMatch popupLinkMatch = new LinkMatch(item, item.getNotificationLink(), linkMatch.getMatchResult(), linkMatch.getMatchStart(), linkMatch.getWholeLine(), linkMatch.getGroup(), linkMatch.getProject());
			executeLink(popupLinkMatch);
		}
		else
		{
			Shell shell = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			shell.forceActive();
		}
	}
	
	public void addStatisticsListener(IStatisticsListener listener)
	{
		this.statisticsListeners.add(listener);
	}
	
	public void removeStatisticsListener(IStatisticsListener listener)
	{
		this.statisticsListeners.remove(listener);
	}
	
	public void fireStatisticsRemoved(StatisticsEntry entry)
	{
		for(IStatisticsListener listener: statisticsListeners)
		{
			listener.statisticsRemoved(this, entry);
		}
	}
	
	public void fireStatisticsUpdated(StatisticsEntry entry, boolean isNew)
	{
		for(IStatisticsListener listener: statisticsListeners)
		{
			listener.statisticsUpdated(this, entry, isNew);
		}
	}

	/**
	 * @return the statisticEntries
	 */
	public Collection<StatisticsEntry> getStatisticEntries()
	{
		return statisticEntries.values();
	}
}
