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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.Activator.IGrepConsoleListener;
import name.schedenig.eclipse.grepconsole.adapters.links.CommandLinkAdapter;
import name.schedenig.eclipse.grepconsole.adapters.links.FileLinkAdapter;
import name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter;
import name.schedenig.eclipse.grepconsole.adapters.links.JavaLinkAdapter;
import name.schedenig.eclipse.grepconsole.adapters.links.LinkMatch;
import name.schedenig.eclipse.grepconsole.adapters.links.ScriptLinkAdapter;
import name.schedenig.eclipse.grepconsole.adapters.links.UrlLinkAdapter;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.model.links.CommandLink;
import name.schedenig.eclipse.grepconsole.model.links.FileLink;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;
import name.schedenig.eclipse.grepconsole.model.links.JavaLink;
import name.schedenig.eclipse.grepconsole.model.links.ScriptLink;
import name.schedenig.eclipse.grepconsole.model.links.UrlLink;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;

/**
 * A line style listener for styling a StyledText's lines according to a set
 * of grep expression items.
 * 
 * @author msched
 */
public class GrepLineStyleListener implements LineStyleListener, IGrepConsoleListener
{
	/**
	 * Sorts grep style ranges in the order in which they should be evaluated.
	 * Also sorts them by index, so that traversing the full list of style ranges
	 * can be stopped when the first style range with a higher index than
	 * queried is found. 
	 */
	private static final Comparator<GrepStyleRange> STYLE_RANGE_COMPARATOR = new Comparator<GrepStyleRange>()
	{
		@Override
		public int compare(GrepStyleRange a, GrepStyleRange b)
		{
			if(a.getFirstIndex() != b.getFirstIndex())
			{
				return a.getFirstIndex() < b.getFirstIndex() ? -1 : +1;
			}
			
			if(a.getStyleRange() != b.getStyleRange())
			{
				if(a.getStyleRange() == null)
				{
					return -1;
				}
				else if(b.getStyleRange() == null)
				{
					return +1;
				}
			}
			
			if(a.isWholeLine() != b.isWholeLine())
			{
				return a.isWholeLine() ? +1 : -1;
			}
			
			if(a.getExpressionIndex() != b.getExpressionIndex())
			{
				return a.getExpressionIndex() < b.getExpressionIndex() ? -1 : +1;
			}
			
			return 0;
		}
	};

	/**
	 * Sorts grep style ranges only by putting whole line styles after sub group
	 * styles. This is used to re-sort the list of currently active styles at
	 * a certain index in a line so that sub group styles always take precedence
	 * over whole line styles.
	 */
	private static final Comparator<GrepStyleRange> STYLE_RANGE_COMPARATOR_SUB_BEFORE_WHOLE = new Comparator<GrepStyleRange>()
	{
		@Override
		public int compare(GrepStyleRange a, GrepStyleRange b)
		{
			if(a.isWholeLine() != b.isWholeLine())
			{
				return a.isWholeLine() ? +1 : -1;
			}
			
			return 0;
		}
	};
	
	/** Collection of grep expression items to be used for determining the line
	 *  styles. Expressions are evaluated in the order provided. */
	private Collection<GrepExpressionItem> items;
	
	/** Colour registry for creating line style colours. */
	private ColorRegistry colorRegistry;

	/** Maximum number of characters to match per line. */
	private int matchLength;

	/** Shell (used when opening dialogs). */
	private Shell shell;

	/** Optional project associated with the originating console. */
	private IProject project;
	
	/**
	 * Creates a new instance.
	 */
	public GrepLineStyleListener(Shell shell, IProject project)
	{
		Activator activator = Activator.getDefault();
		activator.addListener(this);
		this.matchLength = activator.getStyleMatchLength();
		this.shell = shell;
		this.project = project;
	}
	
	/**
	 * Returns the colour registry.
	 * 
	 * @return Colour registry.
	 */
	public ColorRegistry getColorRegistry()
	{
		return colorRegistry;
	}

	/**
	 * Sets the colour registry.
	 * 
	 * @param colorRegistry Colour registry.
	 */
	public void setColorRegistry(ColorRegistry colorRegistry)
	{
		this.colorRegistry = colorRegistry;
	}

	/**
	 * Returns the collection of grep expression items used for determining
	 * the line styles.
	 * 
	 * @return Items.
	 */
	public Collection<GrepExpressionItem> getItems()
	{
		return items;
	}

	/**
	 * Sets the collection of grep expression items to be used for determining the
	 * line styles. Expressions are evaluated in the order provided.
	 *  
	 * @param items Items.
	 */
	public void setItems(Collection<GrepExpressionItem> items)
	{
		this.items = items;
	}

	/**
	 * Creates a link adapter from a link match, which can then be stored in a
	 * StyleRange's data setting.
	 * 
	 * @param match Link match.
	 * 
	 * @return Link adapter.
	 */
	public static GrepLinkAdapter createLinkAdapter(LinkMatch match, Shell shell,
			IProject project)
	{
		IGrepLink link = match.getLink();
		
		if(link instanceof UrlLink)
		{
			return new UrlLinkAdapter(match, shell);
		}
		else if(link instanceof ScriptLink)
		{
			return new ScriptLinkAdapter(match, shell);
		}
		else if(link instanceof FileLink)
		{
			return new FileLinkAdapter(match, shell);
		}
		else if(link instanceof CommandLink)
		{
			return new CommandLinkAdapter(match, shell);
		}
		else if(link instanceof JavaLink)
		{
			return new JavaLinkAdapter(match, shell, project);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Tests all available grep expressions against a line of text and calculates
	 * the line's styles.
	 * 
	 * @see org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt.custom.LineStyleEvent)
	 */
	public void lineGetStyle(LineStyleEvent event)
	{
		if(event.lineText.length() == 0 || items == null || items.isEmpty())
		{
			return;
		}

		LinkedList<GrepStyleRange> grepStyles = null;
		
		if(event.getSource() instanceof StyledText && ((StyledText) event.getSource()).getContent() instanceof TextFilter)
		{
			TextFilter textFilter = ((TextFilter) ((StyledText) event.getSource()).getContent());
			GrepStyleRange[] styleArray = textFilter.getLineStyles(textFilter.getLineAtOffset(event.lineOffset));
			
			if(styleArray != null)
			{
				grepStyles = new LinkedList<GrepStyleRange>();
				
				for(GrepStyleRange style: styleArray)
				{
					grepStyles.add(style);
				}
			}
		}
		
		if(grepStyles == null)
		{
			grepStyles = lineGetStyle(event.lineOffset, event.lineText, true, false,
					event.styles == null || event.styles.length == 0 ? null : event.styles);
		}
		
		StyleRange[] styles = convertStyles(event.lineOffset, event.lineText,
				event.styles == null || event.styles.length == 0 ? null : event.styles, grepStyles);
		
		if(styles != null)
		{
			event.styles = styles;
		}
	}

	/**
	 * Returns the style range at a specific character offset.
	 * 
	 * @param lineOffset Offset of the first character in the line.
	 * @param lineText Full line text.
	 * @param charOffset Character offset.
	 * @param styles Whether to assign matched styles.
	 * @param links Whether to assign matched links.
	 * 
	 * @return Style range. <code>null</code> if none is found.
	 */
	public StyleRange lineGetStyleAt(int lineOffset, String lineText, 
			int charOffset, boolean styles, boolean links)
	{
		LinkedList<GrepStyleRange> grepStyleRanges = lineGetStyle(lineOffset, lineText, styles, links, null);
		StyleRange[] ranges = convertStyles(lineOffset, lineText, null, grepStyleRanges);
		
		if(ranges == null)
		{
			return null;
		}
		
		for(StyleRange range: ranges)
		{
			if(range.start > charOffset)
			{
				return null;
			}
			else if(range.start + range.length >= charOffset)
			{
				return range;
			}
		}
		
		return null;
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
		this.matchLength = Activator.getDefault().getStyleMatchLength();
	}

	/**
	 * Gets the style ranges for a specific line.
	 * 
	 * @param lineOffset Offset of the first character in the line.
	 * @param lineText Full line text.
	 * @param styles Whether to assign styles.
	 * @param links Whether to assign links.
	 * @param originalStyleRanges Optional style ranges already present on the
	 * 		line.
	 * 
	 * @return Array of style ranges. Possibly empty, never <code>null</code>.
	 */
	public LinkedList<GrepStyleRange> lineGetStyle(int lineOffset, String lineText,
			boolean styles, boolean links, StyleRange[] originalStyleRanges)
	{
		String matchText = (matchLength <= 0 || lineText.length() <= matchLength) ? lineText : lineText.substring(0, matchLength);
		LinkedList<GrepStyleRange> grepStyleRanges = new LinkedList<GrepStyleRange>();
		
		int index = 0;
		
		for(GrepExpressionItem item: items)
		{
			calculateStyleRanges(index, lineText, matchText, lineOffset, item, grepStyleRanges, styles, links);
			index++;
		}

		if(grepStyleRanges.isEmpty())
		{
			return null;
		}
		
		return grepStyleRanges;
	}
	
	/**
	 * Converts a list of grep style ranges into a StyleRange array.
	 * 
	 * @param lineOffset Offset of the line's first character.
	 * @param lineText Full text of the line.
	 * @param originalStyleRanges Optional array of StyleRanges already set on
	 * 		the line.
	 * @param grepStyleRanges Grep style ranges for the line.
	 * 
	 * @return StyleRange array.
	 */
	public StyleRange[] convertStyles(int lineOffset, String lineText,
			StyleRange[] originalStyleRanges, LinkedList<GrepStyleRange> grepStyleRanges)
	{
		if(grepStyleRanges == null || grepStyleRanges.isEmpty())
		{
			return null;
		}
		
		if(originalStyleRanges != null)
		{
			boolean useExistingStyleRanges = true;
			
			for(GrepStyleRange range: grepStyleRanges)
			{
				if(range.getExpressionItem() != null 
						&& range.getExpressionItem().isRemoveOriginalStyle())
				{
					useExistingStyleRanges = false;
					break;
				}
			}
			
			if(useExistingStyleRanges)
			{
				int lineLength = lineText.length();
				
				for(StyleRange range: originalStyleRanges)
				{
					if(range.start + range.length <= lineOffset
							|| range.start >= lineOffset + lineLength)
					{
						continue;
					}
					
					int rangeStart = Math.max(range.start - lineOffset, 0);
					int rangeEnd = Math.min(range.start + range.length - lineOffset, lineLength);
					grepStyleRanges.add(new GrepStyleRange(rangeStart, rangeEnd, range));
				}
			}
		}
		
		Collections.sort(grepStyleRanges, STYLE_RANGE_COMPARATOR);

		TreeSet<Integer> indexes = new TreeSet<Integer>();

		for(GrepStyleRange range: grepStyleRanges)
		{
			indexes.add(range.getFirstIndex());
			indexes.add(range.getLastIndex() + 1);
		}

		List<StyleRange> styleRanges = new LinkedList<StyleRange>();
		List<GrepStyleRange> activeGrepStyleRanges = new LinkedList<GrepStyleRange>();
		
		int currentIndex = indexes.pollFirst();
		
		while(!indexes.isEmpty())
		{
			boolean activeStylesChanged = false;
			
			Integer nextIndex = indexes.pollFirst();
			LinkedList<GrepStyleRange> removeActive = new LinkedList<GrepStyleRange>();
			
			for(GrepStyleRange range: activeGrepStyleRanges)
			{
				if(range.getLastIndex() < currentIndex)
				{
					removeActive.add(range);
					activeStylesChanged = true;
				}
			}
			
			activeGrepStyleRanges.removeAll(removeActive);
			
			while(!grepStyleRanges.isEmpty() && grepStyleRanges.getFirst().getFirstIndex() == currentIndex)
			{
				activeGrepStyleRanges.add(grepStyleRanges.removeFirst());
				activeStylesChanged = true;
			}

			if(activeStylesChanged)
			{
				Collections.sort(activeGrepStyleRanges, STYLE_RANGE_COMPARATOR_SUB_BEFORE_WHOLE);
			}
			
			StyleRange styleRange = new StyleRange();
			styleRange.start = lineOffset + currentIndex;
			styleRange.length = nextIndex - currentIndex;
			
			if(!activeGrepStyleRanges.isEmpty())
			{
				collapseStyles(activeGrepStyleRanges, styleRange);
				styleRanges.add(styleRange);
			}
			
			currentIndex = nextIndex;
		}
		
		return styleRanges.isEmpty() ? null : styleRanges.toArray(new StyleRange[]{});
	}
	
	/**
	 * Collapses an ordered list of grep style ranges into a single Eclipse
	 * style range. Earlier grep style ranges take priority, with later items only
	 * settings those style range fields that have not been assigned earlier.
	 * 
	 * @param grepStyleRanges Ordered list of grep style ranges.
	 * @param styleRange Collapsed Eclipse style range.
	 */
	public void collapseStyles(List<GrepStyleRange> grepStyleRanges, StyleRange styleRange)
	{
		StyleRange originalRange = null;
		
		for(GrepStyleRange grepStyleRange: grepStyleRanges)
		{
			if(originalRange == null)
			{
				originalRange = grepStyleRange.getStyleRange();
			}
			
			GrepStyle style = grepStyleRange.getStyle();
		
			if(style != null)
			{
				if(styleRange.foreground == null && style.getForeground() != null)
				{
					styleRange.foreground = colorRegistry.get(style.getForeground());
				}
				
				if(styleRange.background == null && style.getBackground() != null)
				{
					styleRange.background = colorRegistry.get(style.getBackground());
				}
				
				styleRange.fontStyle |= (style.isBold() ? SWT.BOLD : 0) | (style.isItalic() ? SWT.ITALIC : 0);
				styleRange.underline |= style.isUnderline();
				
				if(styleRange.underlineColor == null && style.getUnderlineColor() != null)
				{
					styleRange.underlineColor = colorRegistry.get(style.getUnderlineColor());
				}
				
				styleRange.strikeout |= style.isStrikeout();
				
				if(styleRange.strikeoutColor == null && style.getStrikeoutColor() != null)
				{
					styleRange.strikeoutColor = colorRegistry.get(style.getStrikeoutColor());
				}
				
				styleRange.borderStyle |= style.isBorder() ? SWT.BORDER_SOLID : SWT.NONE;
				
				if(styleRange.borderColor == null && style.getBorderColor() != null)
				{
					styleRange.borderColor = colorRegistry.get(style.getBorderColor());
				}
			}
			
			if(styleRange.data == null && grepStyleRange.getLinkMatch() != null)
			{
				styleRange.data = createLinkAdapter(grepStyleRange.getLinkMatch(), 
						shell, project);
			}
		}
		
		if(originalRange != null)
		{
			copyStyleRangeFields(originalRange, styleRange);
		}
	}

	/**
	 * Copies fields from one style range to another. Only those fields not
	 * already set in the target range are copied.
	 * 
	 * @param src Source range.
	 * @param dest Target range.
	 */
	private void copyStyleRangeFields(StyleRange src, StyleRange dest)
	{
		if(dest.background == null)
		{
			dest.background = src.background;
		}
		
		if(dest.borderColor == null)
		{
			dest.borderColor = src.borderColor;
		}
		
		dest.borderStyle |= src.borderStyle;
		
		if(dest.data == null)
		{
			dest.data = src.data;
		}
		
		if(dest.font == null)
		{
			dest.font = src.font;
		}
		
		dest.fontStyle |= src.fontStyle;
		
		if(dest.foreground == null)
		{
			dest.foreground = src.foreground;
		}
		
		if(dest.metrics == null)
		{
			dest.metrics = src.metrics;
		}
		
		if(dest.rise == 0)
		{
			dest.rise = src.rise;
		}
		
		dest.strikeout |= src.strikeout;
		
		if(dest.strikeoutColor == null)
		{
			dest.strikeoutColor = src.strikeoutColor;
		}
		
		dest.underline |= src.underline;
		
		if(dest.underlineColor == null)
		{
			dest.underlineColor = src.underlineColor;
		}
		
		dest.underlineStyle |= src.underlineStyle;
	}

	/**
	 * Tests the specified grep expression item against a line of text and
	 * creates the corresponding grep style ranges, if necessary.
	 *  
	 * @param expressionIndex Grep expression index. Used to keep the order of
	 * 		matched expressions when sorting the matches later.
	 * @param text Full line text.
	 * @param matchText Text for grep testing. May be shortened from the full
	 * 		line for performance reasons. 
	 * @param lineOffset Offset of the first character in the line.
	 * @param item Grep expression item to test.
	 * @param styleRanges List of style ranges to which any new style ranges will
	 * 		be added.
	 * @param styles Whether to assign styles.
	 * @param links Whether to assign links.
	 */
	protected void calculateStyleRanges(int expressionIndex, String text, 
			String matchText, int lineOffset, GrepExpressionItem item, 
			List<GrepStyleRange> styleRanges, boolean styles, boolean links)
	{
		Pattern pattern = item.getPattern();
		
		if(pattern == null)
		{
			return;
		}
		
		Pattern quickPattern = item.getQuickPattern();
		
		if(quickPattern != null 
				&& !quickPattern.matcher(matchText).find())
		{
			return;
		}
		
		Matcher matcher = pattern.matcher(quickPattern == null ? matchText : text);
		boolean first = true;
		
		while(matcher.find())
		{
			if(first)
			{
				first = false;
				Pattern unlessPattern = item.getUnlessPattern();
				
				if(unlessPattern != null)
				{
					Matcher unlessMatcher = unlessPattern.matcher(quickPattern == null ? matchText : text);
					
					if(unlessMatcher.find())
					{
						return;
					}
				}
				
				GrepStyle style = styles ? item.getGroups()[0].getStyle() : null;
				IGrepLink link = links ? item.getGroups()[0].getLink() : null;
				
				if(style != null || link != null)
				{
					styleRanges.add(new GrepStyleRange(item, expressionIndex, true, 0, text.length(), style, link, matcher, text, 0, project));
				}
			}

			GrepGroup[] groups = item.getGroups();
			
			for(int i = 1; i <= matcher.groupCount(); i++)
			{
				GrepGroup group = i < groups.length ? groups[i] : null;
				
				if(group == null)
				{
					continue;
				}

				int start = matcher.start(i);
				
				if(start < 0)
				{
					continue;
				}
				
				int length = matcher.group(i).length();
				
				GrepStyle groupStyle = styles ? group.getStyle() : null;
				IGrepLink groupLink = links ? group.getLink() : null;
			
				styleRanges.add(new GrepStyleRange(item, expressionIndex, false, start, start + length - 1, groupStyle, groupLink, matcher, text, i, project));
			}
		}
	}
}
