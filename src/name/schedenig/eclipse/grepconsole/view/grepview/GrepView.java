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

package name.schedenig.eclipse.grepconsole.view.grepview;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.actions.ScrollLockAction;
import name.schedenig.eclipse.grepconsole.adapters.GrepLineStyleListener;
import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.adapters.TextFilter;
import name.schedenig.eclipse.grepconsole.adapters.links.LinkListener;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;
import name.schedenig.eclipse.grepconsole.view.common.GrepConsoleView;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * A view that displays a filtered subset of lines from a console.
 *
 * FIXME: Correct context sensitive help only shows up when refocusing view
 * 				after help view has been opened. Reason unknown.
 * 
 * @author msched
 */
public class GrepView extends GrepConsoleView implements TextChangeListener, IContextProvider
{
	// --- Memento keys --- 
	private static final String KEY_SCROLL_LOCK = "scrollLock"; //$NON-NLS-1$
	
	/** Line style listener used to highlight the displayed lines. */
	private GrepLineStyleListener lineStyleListener;
	
	/** Whether the displayed console is linked to the console view. */
	private boolean linkedToConsole = true;
	
	/** Whether the displayed console is scroll locked. */
	private boolean scrollLock = false;
	
	/** Scroll lock action instance. */
	private ScrollLockAction scrollLockAction;
	
	/** Text widget for displaying filtered console content. */
	private StyledText text;
	
	/** Menu item for the "jump to" action. */
	private MenuItem miJumpTo;
	
	/** Index (in filtered content) of the selected line. */
	private int selectedLineIndex;
	private LinkListener linkAdapter;
	private ColorRegistry colorRegistry;

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose()
	{
		if(linkAdapter != null)
		{
			linkAdapter.dispose();
			linkAdapter = null;
		}
		
		if(colorRegistry != null)
		{
			colorRegistry.disposeColors();
			colorRegistry = null;
		}
		
		super.dispose();
	}
	
	/**
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException
	{
		IToolBarManager toolbar = site.getActionBars().getToolBarManager();
		
		scrollLockAction = new ScrollLockAction(this);
		toolbar.add(scrollLockAction);
		
		super.init(site);
	}

	/**
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		super.init(site, memento);
		
		scrollLock = getMementoBoolean(memento, KEY_SCROLL_LOCK, scrollLock);
		scrollLockAction.setChecked(scrollLock);
	}
	
	/**
	 * Creates GUI content.
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		colorRegistry = new ColorRegistry(Activator.getDefault().getColorRegistry());
		
		text = new StyledText(parent, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setFont(JFaceResources.getTextFont());
		
		ScopedPreferenceStore debugUiPrefs = GrepConsoleUtil.getDebugUiPreferences();
		
		if(debugUiPrefs != null)
		{
			debugUiPrefs.addPropertyChangeListener(this);
			refreshColors();
		}
		
		text.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				text.getContent().removeTextChangeListener(GrepView.this);
			}
		});
		
		linkAdapter = new LinkListener(text, lineStyleListener);
		linkAdapter.attach();
		
		Menu menu = new Menu(text);
		miJumpTo = new MenuItem(menu, SWT.PUSH);
		miJumpTo.setText(Messages.GrepView_jump_to);
		
		miJumpTo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int offset = calculateOriginalOffset(selectedLineIndex);
				
				if(offset < 0)
				{
					return;
				}
				
				getParticipant().getStyledText().setSelection(offset);
			}
		});
		
		text.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				int y = e.y;
				selectedLineIndex = text.getLineIndex(y);
			}
		});
		
		menu.addMenuListener(new MenuAdapter()
		{
			@Override
			public void menuShown(MenuEvent e)
			{
				int offset = calculateOriginalOffset(selectedLineIndex);
				miJumpTo.setEnabled(offset >= 0);
			}
		});
		
		text.setMenu(menu);
		participantActivated(Activator.getDefault().getActiveParticipant());
		
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * Updates the colours when the preferences change.
	 * 
	 * @param event Property change event.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String prop = event.getProperty();
		
		if(GrepConsoleUtil.PREF_DEBUG_CONSOLE_SYS_OUT_COLOR.equals(prop)
				|| GrepConsoleUtil.PREF_DEBUG_CONSOLE_BACKGROUND_COLOR.equals(prop)
				|| Activator.PREFS_GREP_VIEW_BACKGROUND_COLOR.equals(prop)
				|| Activator.PREFS_GREP_VIEW_FOREGROUND_COLOR.equals(prop))
		{
			refreshColors();
		}
	}

	/**
	 * Sets the styled text control's colours to those from the preferences.
	 */
	private void refreshColors()
	{
		text.setForeground(colorRegistry.get(GrepConsoleUtil.getGrepViewTextColor()));
		text.setBackground(colorRegistry.get(GrepConsoleUtil.getGrepViewBackgroundColor()));
	}

	/**
	 * Calculates the current offset in the original content of the selected line.
	 * 
	 * @param selectedLineIndex Index (in filtered content) of the selected line.
	 * 
	 * @return Offset (in original content) of the selected line.
	 */
	protected int calculateOriginalOffset(int selectedLineIndex)
	{
		TextFilter filter = (TextFilter) getContent();
		return filter.getOriginalOffsetAtLine(selectedLineIndex);
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
	}
	
	/**
	 * Returns the text widget's content instance.
	 * 
	 * @return Content.
	 */
	public StyledTextContent getContent()
	{
		return text.getContent();
	}

	/**
	 * Sets the text widget's line style listener.
	 * 
	 * @param lineStyleListener New line style listener.
	 */
	public void setLineStyleListener(GrepLineStyleListener lineStyleListener)
	{
		if(this.lineStyleListener != null)
		{
			text.removeLineStyleListener(this.lineStyleListener);
		}
		
		this.lineStyleListener = lineStyleListener;
		
		if(lineStyleListener != null)
		{
			text.addLineStyleListener(lineStyleListener);
		}
		
		linkAdapter.setLineStyleListener(lineStyleListener);
		
		text.redraw();
	}

	/**
	 * Sets the current text filter.
	 * 
	 * @param textFilter New text filter.
	 */
	private void setContent(StyledTextContent content)
	{
		text.getContent().removeTextChangeListener(this);
		
		if(content == null)
		{
			text.setVisible(false);
		}
		else
		{
			text.setVisible(true);
			text.setContent(content);
			content.addTextChangeListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see name.schedenig.eclipse.grepconsole.view.common.GrepConsoleView#setParticipant(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	protected void setParticipant(GrepPageParticipant participant)
	{
		if(participant != null)
		{
			participant.removeListener(this);
		}
		
		super.setParticipant(participant);

		if(participant == null)
		{
			setContent(null);
			setLineStyleListener(null);
		}
		else
		{
			participant.addListener(this);
			resetTextFilter();
		}
	}
	
	/**
	 * Refreshes the text filter and re-sets the line style listener and content. 
	 */
	protected void resetTextFilter()
	{
		TextFilter textFilter = getParticipant().getTextFilter();
		
		setLineStyleListener(getParticipant().getLineStyleListener());
		setContent(textFilter);
	}
	
	/**
	 * @see org.eclipse.swt.custom.TextChangeListener#textChanging(org.eclipse.swt.custom.TextChangingEvent)
	 */
	@Override
	public void textChanging(TextChangingEvent event)
	{
	}

	/**
	 * @see org.eclipse.swt.custom.TextChangeListener#textChanged(org.eclipse.swt.custom.TextChangedEvent)
	 */
	@Override
	public void textChanged(TextChangedEvent event)
	{
		autoScroll();
	}

	/**
	 * @see org.eclipse.swt.custom.TextChangeListener#textSet(org.eclipse.swt.custom.TextChangedEvent)
	 */
	@Override
	public void textSet(TextChangedEvent event)
	{
		autoScroll();
	}
	
	/**
	 * If scroll lock is disabled, scrolls to the end of the displayed text.
	 */
	public void autoScroll()
	{
		if(!scrollLock)
		{
			text.setSelection(text.getCharCount());
		}
	}

	/**
	 * Returns the scroll lock flag.
	 *
	 * @return Whether scroll lock is enabled.
	 */
	public boolean isScrollLock()
	{
		return scrollLock;
	}

	/**
	 * Sets the scroll lock flag.
	 * 
	 * @param scrollLock Whether scroll lock should be enabled.
	 */
	public void setScrollLock(boolean scrollLock)
	{
		this.scrollLock = scrollLock;
	}

	/**
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento)
	{
		super.saveState(memento);
		
		memento.putBoolean(KEY_SCROLL_LOCK, scrollLock);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant.IGrepPageParticipantListener#lineStyleListenerChanged()
	 */
	@Override
	public void participantChanged(GrepPageParticipant grepPageParticipant)
	{
		resetTextFilter();
		text.redraw();
	}

	/**
	 * @see org.eclipse.help.IContextProvider#getContextChangeMask()
	 */
	@Override
	public int getContextChangeMask()
	{
		return NONE;
	}

	/**
	 * @see org.eclipse.help.IContextProvider#getContext(java.lang.Object)
	 */
	@Override
	public IContext getContext(Object target)
	{
		return HelpSystem.getContext(Activator.PLUGIN_ID + ".grep_view"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.help.IContextProvider#getSearchExpression(java.lang.Object)
	 */
	@Override
	public String getSearchExpression(Object target)
	{
		return null;
	}
	
	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
	{
		if(IContextProvider.class.equals(adapter))
		{
			return this;
		}
		else
		{
			return super.getAdapter(adapter);
		}
	}
}
