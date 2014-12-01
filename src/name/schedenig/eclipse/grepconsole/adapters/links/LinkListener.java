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

package name.schedenig.eclipse.grepconsole.adapters.links;

import java.util.LinkedList;
import java.util.List;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.adapters.GrepLineStyleListener;
import name.schedenig.eclipse.grepconsole.adapters.GrepStyleRange;
import name.schedenig.eclipse.grepconsole.adapters.TextFilter;
import name.schedenig.eclipse.grepconsole.adapters.TextFilter.Line;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Assigns listeners to a StyledText instance to handle user/link interaction.
 * 
 * @author msched
 */
public class LinkListener implements KeyListener, MouseListener,
		MouseMoveListener, MouseTrackListener, IPropertyChangeListener
{
	/** Styled text to which listeners are assigned. */
	private StyledText styledText;
	
	/** Line style listener used for link queries. */
	private GrepLineStyleListener lineStyleListener;

	/** Last mouse position reported by a mouse move event (X coordinate). */
	private int lastX;

	/** Last mouse position reported by a mouse move event (Y coordinate). */
	private int lastY;

	/** Whether the mouse cursor is currently inside the text control. */
	private boolean mouseInside;

	/** Modifier keycode. */
	private int modifier;

	/**
	 * Creates a new instance.
	 * 
	 * @param styledText Styled text to which listeners are assigned.
	 * @param lineStyleListener Line style listener used for link queries.
	 */
	public LinkListener(StyledText styledText, GrepLineStyleListener lineStyleListener)
	{
		this.styledText = styledText;
		this.lineStyleListener = lineStyleListener;
		
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		init();
	}
	
	/**
	 * Disposes the listener.
	 */
	public void dispose()
	{
		ScopedPreferenceStore debugUiPrefs = GrepConsoleUtil.getDebugUiPreferences();
		
		if(debugUiPrefs != null)
		{
			debugUiPrefs.removePropertyChangeListener(this);
		}
	}
	
	/**
	 * Reads preferences.
	 */
	private void init()
	{
		modifier = Activator.getDefault().getLinkModifierKey();
	}

	/**
	 * Sets the line style listener.
	 * 
	 * @param lineStyleListener Line style listener.
	 */
	public void setLineStyleListener(GrepLineStyleListener lineStyleListener)
	{
		this.lineStyleListener = lineStyleListener;
	}
	
	/**
	 * Returns the line style listener.
	 * 
	 * @return the lineStyleListener Line style listener.
	 */
	public GrepLineStyleListener getLineStyleListener()
	{
		return lineStyleListener;
	}

	/**
	 * Attaches the listeners.
	 */
	public void attach()
	{
		styledText.addKeyListener(this);
		styledText.addMouseListener(this);
		styledText.addMouseMoveListener(this);
		styledText.addMouseTrackListener(this);
		
		styledText.getShell().getDisplay().addFilter(SWT.KeyDown, new Listener()
		{
			public void handleEvent(final Event event)
			{
				if(mouseInside && !styledText.isDisposed() && event.keyCode == modifier)
				{
					updateLinkCursor();
				}
			}
		});
		
		styledText.getShell().getDisplay().addFilter(SWT.KeyUp, new Listener()
		{
			public void handleEvent(final Event event)
			{
				if(!styledText.isDisposed() && event.keyCode == modifier)
				{
					styledText.setCursor(null);
					styledText.setToolTipText(null);
				}
			}
		});
	}

	/**
	 * Calculates the style at the specified mouse location.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * 
	 * @return Style, possibly <code>null</code>.
	 */
	private StyleRange getStyleAt(int x, int y)
	{
		try
		{
			int offset = styledText.getOffsetAtLocation(new Point(x, y));
			int lineIndex = styledText.getLineAtOffset(offset);
			int lineOffset = styledText.getOffsetAtLine(lineIndex);

			if(styledText.getContent() instanceof TextFilter)
			{
				TextFilter textFilter = (TextFilter) styledText.getContent();
				Line line = textFilter.getLineElement(lineIndex);
				
				if(line != null && line.isRewritten())
				{
					int offsetInLine = offset - lineOffset;
					GrepStyleRange[] styles = textFilter.getLineStyles(lineIndex);
					List<GrepStyleRange> foundStyles = new LinkedList<GrepStyleRange>();
					
					for(GrepStyleRange style: styles)
					{
						if(style.getFirstIndex() > offsetInLine)
						{
							break;
						}
						
						if(style.getLastIndex() >= offsetInLine)
						{
							foundStyles.add(style);
						}
					}
					
					if(foundStyles.isEmpty())
					{
						return null;
					}
					else
					{
						StyleRange styleRange = new StyleRange(); // we don't need the indexes
						lineStyleListener.collapseStyles(foundStyles, styleRange);
						return styleRange;
					}
				}
			}
			

			String lineText = styledText.getLine(lineIndex);

			return lineStyleListener.lineGetStyleAt(lineOffset, lineText, offset, false, true);
		}
		catch(IllegalArgumentException ex)
		{
			return null;
		}
	}
	
	/**
	 * Queries links when the mouse is moved and updates the mouse cursor if
	 * necessary.
	 * 
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseMove(MouseEvent e)
	{
		lastX = e.x;
		lastY = e.y;

		if(styledText.isDisposed() || lineStyleListener == null)
		{
			return;
		}
		
		if((e.stateMask & modifier) == 0)
		{
			return;
		}
		
		updateLinkCursor();
	}
	
	/**
	 * Assumes that the link modifier is held down and updates the mouse cursor
	 * depending on the last mouse location reported by a mouse move event.
	 */
	public void updateLinkCursor()
	{
		StyleRange style = getStyleAt(lastX, lastY);
		
		if(style != null && style.data instanceof GrepLinkAdapter)
		{
			styledText.setCursor(Activator.getDefault().getLinkCursor());
			styledText.setToolTipText(((GrepLinkAdapter) style.data).getToolTipText());
		}
		else
		{
			styledText.setCursor(null);
			styledText.setToolTipText(null);
		}
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e)
	{
	}

	/**
	 * Executes links when the user clicks on them.
	 * 
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e)
	{
		if(styledText.isDisposed() || lineStyleListener == null)
		{
			return;
		}
		
		if((e.stateMask & modifier) == 0)
		{
			return;
		}
		
		StyleRange style = getStyleAt(e.x, e.y);

		if(style != null && style.data instanceof GrepLinkAdapter)
		{
			GrepLinkAdapter link = ((GrepLinkAdapter) style.data);
			link.openLink();
		}
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent e)
	{
	}

	/**
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.keyCode == modifier)
		{
			updateLinkCursor();
		}
	}

	/**
	 * Resets the mouse cursor and tooltip when the user releases the link
	 * modifier key.
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e)
	{
		if(e.keyCode == modifier)
		{
			styledText.setCursor(null);
			styledText.setToolTipText(null);
		}
	}

	/**
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseEnter(MouseEvent e)
	{
		mouseInside = true;
	}

	/**
	 * Resets the mouse cursor and tooltip when the user leaves the control.
	 * 
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseExit(MouseEvent e)
	{
		mouseInside = false;
		styledText.setCursor(null);
		styledText.setToolTipText(null);
	}

	/**
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseHover(MouseEvent e)
	{
	}

	/**
	 * Reinitialises the listener when the modifier key preference changes.
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String prop = event.getProperty();
		
		if(Activator.PREFS_LINK_MODIFIER_KEY.equals(prop))
		{
			init();
		}
	}
}
