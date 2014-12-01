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

package name.schedenig.eclipse.grepconsole.view.styles;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;
import name.schedenig.eclipse.grepconsole.view.ReadOnlyListDialog;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;
import name.schedenig.eclipse.grepconsole.view.items.ItemLabelProvider;
import name.schedenig.eclipse.grepconsole.view.items.PreviewColorHandler;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * A panel that lists all configured styles and lets the user manage them.
 * 
 * @author msched
 */
public class StylesPanel extends Composite
{
	/**
	 * Interface for listeners to style changes.
	 * 
	 * @author msched
	 */
	public static interface IStylesListener
	{
		/**
		 * Called when the user created a new style.
		 * 
		 * @param panel Source panel.
		 * @param style The new style.
		 */
		void onNewStyle(StylesPanel panel, GrepStyle style);
		
		/**
		 * Called when the user deleted a style.
		 * 
		 * @param panel Source panel.
		 * @param style The deleted style.
		 */
		void onStyleDeleted(StylesPanel panel, GrepStyle style);
		
		/**
		 * Called when the user changed a style.
		 * 
		 * @param panel Source panel.
		 * @param style The changed style.
		 */
		void onStyleChanged(StylesPanel panel, GrepStyle style);
		
		/**
		 * Called when the user selected a style.
		 * 
		 * @param panel Source panel.
		 * @param style Selected style.
		 */
		void onStyleSelected(StylesPanel panel, GrepStyle style);
		
		/**
		 * Called when the user double clicked a style.
		 * 
		 * @param panel Source panel.
		 * @param style Double clicked style.
		 * 
		 * @return <code>true</code> if processing of the event should stop after
		 * 		this listener. 
		 */
		boolean onStyleDoubleClicked(StylesPanel panel, GrepStyle style);
	}
	
	/** Set of listeners. */
	private LinkedHashSet<IStylesListener> listeners = new LinkedHashSet<IStylesListener>();
	
	/** Model tree root. Source of the managed styles. */
	private GrepExpressionRootFolder root;
	
	/** Colour registry used for preview. */
	private ColorRegistry colorRegistry;
	
	/** GUI variables. */
	private Table table;
	private TableViewer viewer;
	private StyledText stPreview;
	@SuppressWarnings("unused")
	private Button btnNew;
	private Button btnEdit;
	private Button btnDelete;
	private Composite panelButtons;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent component.
	 * @param style Style flags.
	 */
	public StylesPanel(Composite parent, int style)
	{
		super(parent, style);
		
		init();
	}

	/**
	 * Called when the panel is disposed.
	 */
	protected void onDispose()
	{
		colorRegistry.disposeColors();
	}
	
	/**
	 * Initialises the GUI.
	 */
	protected void init()
	{
		addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				onDispose();
			}
		});
		
		colorRegistry = new ColorRegistry(Activator.getDefault().getColorRegistry());

		new GridLayoutBuilder(this, 1, true).setMargins(0).apply();
		
		table = new Table(this, SWT.BORDER | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		viewer = new TableViewer(table);
		viewer.setContentProvider(new StyleContentProvider(false));
		viewer.setComparator(new StyleComparator());

		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				doStylesSelected(getSelectedStyles());
			}
		});
		
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				doStyleDoubleClicked(getSelectedStyle((IStructuredSelection) event.getSelection()));
			}
		});
		
		TableViewerColumn colAvailableStyle = new TableViewerColumn(viewer, SWT.LEFT);
		colAvailableStyle.getColumn().setText(Messages.StylesPanel_available_styles);
		colAvailableStyle.getColumn().setWidth(150);
		colAvailableStyle.setLabelProvider(new StyleLabelProvider(viewer));

		table.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if(e.character == SWT.DEL)
				{
					doDeleteStyles();
					e.doit = false;
				}
				else if(e.keyCode == SWT.F2)
				{
					doEditStyle();
					e.doit = false;
				}
			}
		});

		stPreview = new StyledText(this, SWT.BORDER);
		stPreview.setText(Messages.StylesPanel_style_preview);
		stPreview.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		stPreview.setFont(JFaceResources.getTextFont());
		
		new PreviewColorHandler(stPreview);

		stPreview.addLineStyleListener(new LineStyleListener()
		{
			@Override
			public void lineGetStyle(LineStyleEvent event)
			{
				GrepStyle grepStyle = getSelectedStyle();
				
				if(grepStyle != null)
				{
					StyleRange style = new StyleRange();
					style.start = event.lineOffset;
					style.length = event.lineText.length();
					fillStyleRange(style, grepStyle);
					event.styles = new StyleRange[]{style};
				}
			}
		});

		ColumnViewerToolTipSupport.enableFor(viewer);
		
		panelButtons = new Composite(this, SWT.NONE);
		panelButtons.setLayout(new FillLayout(SWT.HORIZONTAL));
		panelButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		createButtons(panelButtons);

		if(root != null)
		{
			viewer.setInput(root);
		}
	}
	
	/**
	 * Handles style double clicks.
	 * 
	 * @param style Selected style.
	 */
	protected void doStyleDoubleClicked(GrepStyle style)
	{
		boolean done = false;
		
		for(IStylesListener listener: listeners)
		{
			done |= listener.onStyleDoubleClicked(this, style);
		}

		if(!done)
		{
			doEditStyle(style);
		}
	}

	/**
	 * Creates buttons for the button panel.
	 * 
	 * @param Button panel.
	 */
	protected void createButtons(Composite parent)
	{
		btnNew = createNewButton(parent);
		btnEdit = createEditButton(parent);
		btnDelete = createDeleteButton(parent);
	}
	
	/**
	 * Creates the "new" button.
	 * 
	 * @param parent Parent panel.
	 * 
	 * @return Button.
	 */
	protected Button createNewButton(Composite parent)
	{
		Button btn = new Button(parent, SWT.PUSH);
		btn.setText(Messages.StylesPanel_new);
		btn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doNewStyle();
			}
		});
		
		return btn;
	}
	
	/**
	 * Creates the "edit" button.
	 * 
	 * @param parent Parent panel.
	 * 
	 * @return Button.
	 */
	protected Button createEditButton(Composite parent)
	{
		Button btn = new Button(parent, SWT.PUSH);
		btn.setText(Messages.StylesPanel_edit);
		btn.setEnabled(false);
		btn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doEditStyle();
			}
		});
		
		return btn;
	}
		
	/**
	 * Creates the "delete" button.
	 * 
	 * @param parent Parent panel.
	 * 
	 * @return Button.
	 */
	protected Button createDeleteButton(Composite parent)
	{
		Button btn = new Button(parent, SWT.PUSH);
		btn.setText(Messages.StylesPanel_delete);
		btn.setEnabled(false);
		btn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doDeleteStyles();
			}
		});
		
		return btn;
	}

	/**
	 * Adds a listener to the panel.
	 * 
	 * @param listener Listener.
	 */
	public void addListener(IStylesListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes a listener from the panel.
	 * 
	 * @param listener Listener.
	 */
	public void removeListener(IStylesListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Sets the root of the model tree to be used.
	 * 
	 * @param root Model tree root.
	 */
	public void setRoot(GrepExpressionRootFolder root)
	{
		this.root = root;
		
		if(viewer != null)
		{
			viewer.setInput(root);
		}
	}
	
	/**
	 * Sets whether to show the null style in the list.
	 * 
	 * @param includeNull Whether to show the null style.
	 */
	public void setIncludeNullStyle(boolean includeNull)
	{
		((StyleContentProvider) viewer.getContentProvider()).setIncludeNull(includeNull);
		viewer.refresh();
	}
	
	/**
	 * Called when the "new" button is clicked.
	 */
	public void doNewStyle()
	{
		GrepStyle newStyle = new GrepStyle();
		newStyle.setName(Messages.StylesPanel_new_style);
		
		StyleDialog dlg = new StyleDialog(getShell(), true);
		dlg.setGrepStyle(newStyle);
		
		if(dlg.open() == StyleDialog.OK)
		{
			root.addStyle(newStyle);
			viewer.refresh();
			viewer.setSelection(new StructuredSelection(newStyle));
			
			for(IStylesListener listener: listeners)
			{
				listener.onNewStyle(this, newStyle);
			}
		}
	}

	/**
	 * Caled when the "edit" button is clicked. Edits the selected style.
	 */
	public void doEditStyle()
	{
		doEditStyle(getSelectedStyle());
	}
	
	/**
	 * Opens the edit dialog for the specified style.
	 */
	public void doEditStyle(GrepStyle style)
	{
		if(style == null)
		{
			return;
		}
		
		StyleDialog dlg = new StyleDialog(getShell(), false);
		dlg.setGrepStyle(style);
		
		if(dlg.open() == StyleDialog.OK)
		{
			viewer.refresh();
			stPreview.redraw();
			
			for(IStylesListener listener: listeners)
			{
				listener.onStyleChanged(this, style);
			}
		}
	}

	/**
	 * Called when the "delete" button is clicked.
	 * 
	 * @param style Style that should be deleted.
	 */
	public void doDeleteStyles()
	{
		Collection<GrepStyle> styles = getSelectedStyles();
		
		if(styles.isEmpty())
		{
			return;
		}

		Set<GrepExpressionItem> items = new LinkedHashSet<GrepExpressionItem>();
		
		for(GrepStyle style: styles)
		{
			root.findStyleUses(style, items);
		}
		
		GrepStyle singleStyle = styles.size() == 1 ? styles.iterator().next() : null;
		boolean doIt;
		
		if(items.isEmpty())
		{
			String msg;
			
			if(singleStyle != null)
			{
				String styleName = singleStyle.getName();
				msg = MessageFormat.format(Messages.StylesPanel_delete_style_confirm_single, styleName == null ? Messages.StylesPanel_unnamed : styleName);
			}
			else
			{
				msg = MessageFormat.format(Messages.StylesPanel_delete_style_confirm_multi, styles.size());
			}
			
			doIt = MessageDialog.openConfirm(getShell(), Messages.StylesPanel_delete_style_title, msg);
		}
		else
		{
			String msg;
			
			if(singleStyle != null)
			{
				String styleName = singleStyle.getName();
				msg = MessageFormat.format(Messages.StylesPanel_delete_style_still_used_confirm_single, styleName == null ? Messages.StylesPanel_unnamed : styleName);
			}
			else
			{
				msg = MessageFormat.format(Messages.StylesPanel_delete_style_still_used_confirm_multi, styles.size());
			}
			
			ReadOnlyListDialog dlg = new ReadOnlyListDialog(getShell());
			dlg.setTitle(Messages.StylesPanel_delete_style_title);
			dlg.setMessage(msg);
			dlg.setContentProvider(new IStructuredContentProvider()
			{
				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
				{
				}
				
				@Override
				public void dispose()
				{
				}
				
				@Override
				public Object[] getElements(Object inputElement)
				{
					return ((Collection<?>) inputElement).toArray();
				}
			});
			
			dlg.setLabelProvider(new ItemLabelProvider());
			dlg.setInput(items);
			
			doIt = dlg.open() == ListDialog.OK;
		}
		
		if(!doIt)
		{
			return;
		}

		int index = viewer.getTable().getSelectionIndex();
		
		for(GrepStyle style: styles)
		{
			root.removeStyle(style);
			
			for(GrepExpressionItem item: items)
			{
				GrepGroup[] groups = item.getGroups();
				
				for(int i = 0; i < groups.length; i++)
				{
					if(groups[i].getStyle() == style)
					{
						groups[i].setStyle(null);
					}
				}
			}
			
			for(IStylesListener listener: listeners)
			{
				listener.onStyleDeleted(this, style);
			}
		}

		viewer.refresh();
		viewer.getTable().select(Math.min(index, root.getStyles().size() - 1));
	}

	/**
	 * Called when a style is selected. Updates the preview and buttons and calls
	 * the listeners (only if exactly one style is selected).
	 * 
	 * @param styles Selected styles.
	 */
	protected void doStylesSelected(Collection<GrepStyle> styles)
	{
		stPreview.redraw();
		
		btnEdit.setEnabled(styles.size() == 1);
		btnDelete.setEnabled(!styles.isEmpty());
		
		GrepStyle style = styles.size() == 1 ? styles.iterator().next() : null;
			
		for(IStylesListener listener: listeners)
		{
			listener.onStyleSelected(this, style);
		}
	}

	/**
	 * Fills a style range's properties from a grep style.
	 * 
	 * @param style Style range.
	 * @param grepStyle Grep style.
	 */
	protected void fillStyleRange(StyleRange style, GrepStyle grepStyle)
	{
		RGB rgbForeground = grepStyle.getForeground();
		RGB rgbBackground = grepStyle.getBackground();
		RGB rgbUnderline = grepStyle.getUnderlineColor();
		RGB rgbStrikethrough = grepStyle.getStrikeoutColor();
		RGB rgbBorder = grepStyle.getBorderColor();

		if(rgbBackground == null)
		{
			rgbBackground = new RGB(255, 255, 255);
		}

		style.foreground = colorRegistry.get(rgbForeground);
		style.background = colorRegistry.get(rgbBackground);
		
		style.fontStyle = 0
				| (grepStyle.isBold() ? SWT.BOLD : 0)
				| (grepStyle.isItalic() ? SWT.ITALIC : 0);
		
		style.underline = grepStyle.isUnderline();
		style.underlineColor = colorRegistry.get(rgbUnderline);
		
		style.strikeout = grepStyle.isStrikeout();
		style.strikeoutColor = colorRegistry.get(rgbStrikethrough);
		
		style.borderStyle = grepStyle.isBorder() ? SWT.BORDER_SOLID : SWT.NONE;
		style.borderColor = colorRegistry.get(rgbBorder);
	}

	/**
	 * Returns the selected style.
	 * 
	 * @return Style. May be <code>null</code>.
	 */
	public GrepStyle getSelectedStyle()
	{
		return getSelectedStyle((IStructuredSelection) viewer.getSelection());
	}
	
	/**
	 * Returns the selected styles.
	 * 
	 * @return Styles. May be empty.
	 */
	public Collection<GrepStyle> getSelectedStyles()
	{
		List<GrepStyle> styles = new LinkedList<GrepStyle>();
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		
		if(selection != null)
		{
			for(Object o: selection.toList())
			{
				if(o instanceof GrepStyle)
				{
					styles.add((GrepStyle) o);
				}
			}
		}
		
		return styles;
	}
	
	/**
	 * Reads the selected style from a selection.
	 * 
	 * @param selection Selection.
	 * 
	 * @return Style. May be <code>null</code>.
	 */
	protected GrepStyle getSelectedStyle(IStructuredSelection selection)
	{
		Object o = selection == null || selection.isEmpty() ? null : selection.getFirstElement();
		return (GrepStyle) (o instanceof GrepStyle ? o : null);
	}

	/**
	 * Selects the specified style.
	 * 
	 * @param style Style.
	 */
	public void setSelection(GrepStyle style)
	{
		viewer.setSelection(style == null ? null : new StructuredSelection(style));
	}

	/**
	 * Refreshes the tree.
	 */
	public void refresh()
	{
		viewer.refresh();
	}
}
