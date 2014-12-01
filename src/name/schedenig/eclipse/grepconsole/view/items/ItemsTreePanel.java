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

package name.schedenig.eclipse.grepconsole.view.items;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;
import name.schedenig.eclipse.grepconsole.model.InheritedFlagResolver;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * A panel displaying a tree of model items. No editing functionality is
 * provided, but item enabled and filter state can be toggled.
 * 
 * @author msched
 */
public class ItemsTreePanel extends Composite
{
	/**
	 * Interface for listeners to selection events.
	 * 
	 * @author msched
	 */
	public static interface IItemTreeListener
	{
		/**
		 * Called when an item's enablement state has changed.
		 */
		public void enabledExpressionsChanged();
		
		/**
		 * Called when an item's filter state has changed.
		 */
		public void filterExpressionsChanged();
		
		/**
		 * Called when an item's statistics state has changed.
		 */
		public void statisticsExpressionsChanged();
		
		/**
		 * Called when an item's notifications state has changed.
		 */
		public void notificationsExpressionsChanged();
		
		/**
		 * Called when element selection has changed.
		 * 
		 * @param elements Currently selected elements.
		 */
		public void elementSelectionChanged(Set<AbstractGrepModelElement> elements);
		
		/**
		 * Called when an element has been double clicked.
		 * 
		 * @param element Element.
		 */
		public void elementDoubleClicked(AbstractGrepModelElement element);
	}

	/** Set of listeners. */
	private LinkedHashSet<IItemTreeListener> listeners = new LinkedHashSet<ItemsTreePanel.IItemTreeListener>();
	
	/** Style flags for the tree. */
	private int treeStyle;

	/** GUI variables. */
	private Tree tree;
	private TreeViewer viewer;
	private TreeViewerColumn colCheck;
	private TreeViewerColumn colLabel;
	private ItemCheckboxLabelProvider enablementLabelProvider;
	private TreeViewerColumn colFilter;
	private ItemCheckboxLabelProvider filterLabelProvider;
	private TreeViewerColumn colNotifications;
	private ItemCheckboxLabelProvider notificationsLabelProvider;

	private TreeViewerColumn colStatistics;

	private ItemCheckboxLabelProvider statisticsLabelProvider;

	/**
	 * Creates a new instance.
	 * 
	 * If the flags include the <code>SWT.BORDER</code> flag, it is applied to
	 * the tree.
	 * 
	 * @param parent Parent control.
	 * @param style Style flags.
	 */
	public ItemsTreePanel(Composite parent, int style)
	{
		super(parent, style & ~SWT.BORDER);

		treeStyle = style & SWT.BORDER;
		
		init();
	}

	/**
	 * Creates the GUI content.
	 */
	private void init()
	{
		setLayout(new FillLayout());
		
		tree = new Tree(this, SWT.MULTI | SWT.H_SCROLL |
				SWT.V_SCROLL | SWT.FULL_SELECTION | treeStyle);
		tree.setLinesVisible(false);
		viewer = new TreeViewer(tree);
		viewer.setContentProvider(new ItemContentProvider());

		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				if(!(event.getSelection() instanceof IStructuredSelection))
				{
					return;
				}
				
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				AbstractGrepModelElement element = (AbstractGrepModelElement) selection.getFirstElement();
				
				for(IItemTreeListener listener: listeners)
				{
					listener.elementDoubleClicked(element);
				}
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				Set<AbstractGrepModelElement> elements = getSelectedElements();
				
				for(IItemTreeListener listener: listeners)
				{
					listener.elementSelectionChanged(elements);
				}
			}
		});
		
		ColumnViewerToolTipSupport.enableFor(viewer); 
		
		colLabel = new TreeViewerColumn(viewer, SWT.NONE);
		colLabel.setLabelProvider(new ItemLabelProvider());
		
		colCheck = new TreeViewerColumn(viewer, SWT.RIGHT);

		enablementLabelProvider = new ItemCheckboxLabelProvider()
		{
			@Override
			public InheritedFlagResolver getFlagResolver()
			{
				return InheritedFlagResolver.ENABLED_RESOLVER;
			}
			
			@Override
			protected Map<String, Boolean> getInheritanceMap()
			{
				return getExpressions().getEnablementMap();
			}
			
			@Override
			public String getImageKey(boolean checked, boolean inherited, boolean greyed)
			{
				if(inherited)
				{
					if(greyed)
					{
						return checked ? Activator.IMG_CHECKBOX_ON_INHERITED_GREYED : Activator.IMG_CHECKBOX_OFF_INHERITED_GREYED;
					}
					else
					{
						return checked ? Activator.IMG_CHECKBOX_ON_INHERITED : Activator.IMG_CHECKBOX_OFF_INHERITED;
					}
				}
				else
				{
					if(greyed)
					{
						return checked ? Activator.IMG_CHECKBOX_ON_GREYED : Activator.IMG_CHECKBOX_OFF;
					}
					else
					{
						return checked ? Activator.IMG_CHECKBOX_ON : Activator.IMG_CHECKBOX_OFF;
					}
				}
			}
		};
		
		colCheck.setLabelProvider(enablementLabelProvider);
		
		colFilter = new TreeViewerColumn(viewer, SWT.RIGHT);

		filterLabelProvider = new ItemCheckboxLabelProvider()
		{
			@Override
			public InheritedFlagResolver getFlagResolver()
			{
				return InheritedFlagResolver.FILTER_RESOLVER;
			}
			
			@Override
			protected Map<String, Boolean> getInheritanceMap()
			{
				return getExpressions().getFilterMap();
			}

			@Override
			public String getImageKey(boolean checked, boolean inherited, boolean greyed)
			{
				if(inherited)
				{
					if(greyed)
					{
						return checked ? Activator.IMG_FILTER_ON_INHERITED_GREYED : Activator.IMG_FILTER_OFF_INHERITED_GREYED;
					}
					else
					{
						return checked ? Activator.IMG_FILTER_ON_INHERITED : Activator.IMG_FILTER_OFF_INHERITED;
					}
				}
				else
				{
					if(greyed)
					{
						return checked ? Activator.IMG_FILTER_ON_GREYED : Activator.IMG_FILTER_OFF_GREYED;
					}
					else
					{
						return checked ? Activator.IMG_FILTER_ON : Activator.IMG_FILTER_OFF;
					}
				}
			}
		};
		
		colFilter.setLabelProvider(filterLabelProvider);
		
		colStatistics = new TreeViewerColumn(viewer, SWT.RIGHT);

		statisticsLabelProvider = new ItemCheckboxLabelProvider()
		{
			@Override
			public InheritedFlagResolver getFlagResolver()
			{
				return InheritedFlagResolver.STATISTICS_RESOLVER;
			}
			
			@Override
			protected Map<String, Boolean> getInheritanceMap()
			{
				return getExpressions().getStatisticsMap();
			}

			@Override
			public String getImageKey(boolean checked, boolean inherited, boolean greyed)
			{
				if(inherited)
				{
					if(greyed)
					{
						return checked ? Activator.IMG_STATISTICS_ON_INHERITED_GREYED : Activator.IMG_STATISTICS_OFF_INHERITED_GREYED;
					}
					else
					{
						return checked ? Activator.IMG_STATISTICS_ON_INHERITED : Activator.IMG_STATISTICS_OFF_INHERITED;
					}
				}
				else
				{
					if(greyed)
					{
						return checked ? Activator.IMG_STATISTICS_ON_GREYED : Activator.IMG_STATISTICS_OFF_GREYED;
					}
					else
					{
						return checked ? Activator.IMG_STATISTICS_ON : Activator.IMG_STATISTICS_OFF;
					}
				}
			}
		};
		
		colStatistics.setLabelProvider(statisticsLabelProvider);

		colNotifications = new TreeViewerColumn(viewer, SWT.RIGHT);

		notificationsLabelProvider = new ItemCheckboxLabelProvider()
		{
			@Override
			public InheritedFlagResolver getFlagResolver()
			{
				return InheritedFlagResolver.NOTIFICATIONS_RESOLVER;
			}
			
			@Override
			protected Map<String, Boolean> getInheritanceMap()
			{
				return getExpressions().getNotificationsMap();
			}

			@Override
			public String getImageKey(boolean checked, boolean inherited, boolean greyed)
			{
				if(inherited)
				{
					if(greyed)
					{
						return checked ? Activator.IMG_NOTIFICATION_ON_INHERITED_GREYED : Activator.IMG_NOTIFICATION_OFF_INHERITED_GREYED;
					}
					else
					{
						return checked ? Activator.IMG_NOTIFICATION_ON_INHERITED : Activator.IMG_NOTIFICATION_OFF_INHERITED;
					}
				}
				else
				{
					if(greyed)
					{
						return checked ? Activator.IMG_NOTIFICATION_ON_GREYED : Activator.IMG_NOTIFICATION_OFF_GREYED;
					}
					else
					{
						return checked ? Activator.IMG_NOTIFICATION_ON : Activator.IMG_NOTIFICATION_OFF;
					}
				}
			}
		};
		
		colNotifications.setLabelProvider(notificationsLabelProvider);
		
		tree.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				resizeColumns();
			}
		});
		
		colCheck.setEditingSupport(new TristateEditingSupport(viewer)
		{
			@Override
			protected void toggleElementValue(AbstractGrepModelElement element)
			{
				element.setDefaultEnabled(!element.isDefaultEnabled());
			}
			
			@Override
			protected Map<String, Boolean> getInheritanceMap()
			{
				return getExpressions().getEnablementMap();
			}
			
			@Override
			protected boolean getElementValue(AbstractGrepModelElement element)
			{
				return element.isDefaultEnabled();
			}

			@Override
			protected void setValue(Object element, Object value)
			{
				super.setValue(element, value);
				
				fireEnabledExpressionsChanged();
			}
		});

		colFilter.setEditingSupport(new TristateEditingSupport(viewer)
		{
			@Override
			protected void toggleElementValue(AbstractGrepModelElement element)
			{
				element.setDefaultFilter(!element.isDefaultFilter());
			}
			
			@Override
			protected Map<String, Boolean> getInheritanceMap()
			{
				return getExpressions().getFilterMap();
			}
			
			@Override
			protected boolean getElementValue(AbstractGrepModelElement element)
			{
				return element.isDefaultFilter();
			}

			@Override
			protected void setValue(Object element, Object value)
			{
				super.setValue(element, value);
				
				fireFilterExpressionsChanged();
			}
		});
		
		colStatistics.setEditingSupport(new TristateEditingSupport(viewer)
		{
			@Override
			protected void toggleElementValue(AbstractGrepModelElement element)
			{
				element.setDefaultStatistics(!element.isDefaultStatistics());
			}
			
			@Override
			protected Map<String, Boolean> getInheritanceMap()
			{
				return getExpressions().getStatisticsMap();
			}
			
			@Override
			protected boolean getElementValue(AbstractGrepModelElement element)
			{
				return element.isDefaultStatistics();
			}
			
			@Override
			protected void setValue(Object element, Object value)
			{
				super.setValue(element, value);
				
				fireStatisticsExpressionsChanged();
			}
		});
		
		colNotifications.setEditingSupport(new TristateEditingSupport(viewer)
		{
			@Override
			protected void toggleElementValue(AbstractGrepModelElement element)
			{
				element.setDefaultNotifications(!element.isDefaultNotifications());
			}
			
			@Override
			protected Map<String, Boolean> getInheritanceMap()
			{
				return getExpressions().getNotificationsMap();
			}
			
			@Override
			protected boolean getElementValue(AbstractGrepModelElement element)
			{
				return element.isDefaultNotifications();
			}
			
			@Override
			protected void setValue(Object element, Object value)
			{
				super.setValue(element, value);
				
				fireNotificationsExpressionsChanged();
			}
		});
	}

	/**
	 * Automatically resizes the columns when the tree width has changed.
	 */
	public synchronized void resizeColumns()
	{
		if(colCheck.getColumn() == null)
		{
			return;
		}
		
		getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if(tree.isDisposed())
				{
					return;
				}
				
				int availableWidth = tree.getClientArea().width - 2 * tree.getBorderWidth();
				
				colCheck.getColumn().pack();
				colFilter.getColumn().pack();
				colStatistics.getColumn().pack();
				colNotifications.getColumn().pack();
				colLabel.getColumn().setWidth(availableWidth 
						- colCheck.getColumn().getWidth() 
						- colFilter.getColumn().getWidth() 
						- colStatistics.getColumn().getWidth() 
						- colNotifications.getColumn().getWidth());
			}
		});
	}

	/**
	 * Sets the model root being edited.
	 * 
	 * @param expressions Model root.
	 */
	public void setExpressions(GrepExpressionsWithSelection expressions)
	{
		colCheck.getColumn().setWidth(0);
		colFilter.getColumn().setWidth(0);
		colStatistics.getColumn().setWidth(0);
		colNotifications.getColumn().setWidth(0);
		viewer.setInput(expressions);
		viewer.expandAll();
		resizeColumns();
	}

	/**
	 * Returns the model root being edited.
	 * 
	 * @return Model root.
	 */
	public GrepExpressionsWithSelection getExpressions()
	{
		return (GrepExpressionsWithSelection) viewer.getInput();
	}

	/**
	 * Returns the current root folder.
	 * 
	 * @return Root folder.
	 */
	public GrepExpressionFolder getRootFolder()
	{
		return getExpressions().getRootFolder();
	}

	/**
	 * Returns the currently selected element. If multiple elements are selected,
	 * the first one is returned.
	 * 
	 * @return Element. May be <code>null</code>.
	 */
	public AbstractGrepModelElement getSelectedElement()
	{
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();

		if(selection.isEmpty())
		{
			return null;
		}

		return (AbstractGrepModelElement) selection.getFirstElement();
	}

	/**
	 * Returns the set of currently selected element. If no element is selected,
	 * <code>null</code> is returned.
	 * 
	 * @return Selected elements, or <code>null</code>.
	 */
	public Set<AbstractGrepModelElement> getSelectedElements()
	{
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();

		if(selection.isEmpty())
		{
			return null;
		}

		@SuppressWarnings("unchecked")
		LinkedHashSet<AbstractGrepModelElement> elements = new LinkedHashSet<AbstractGrepModelElement>(selection.toList());
		
		return elements;
	}
	
	/**
	 * Returns the currently selected as a folder. If the selected element is not
	 * a folder, its parent is returned. If multiple elements are selected, the
	 * first one is used.
	 * 
	 * @return Folder. May be <code>null</code>.
	 */
	public GrepExpressionFolder getSelectedFolder()
	{
		AbstractGrepModelElement element = getSelectedElement();
		return element instanceof GrepExpressionFolder ? (GrepExpressionFolder) element : element.getParent();
	}

	/**
	 * Refreshes the tree.
	 */
	public void refresh()
	{
		viewer.refresh();
	}

	/**
	 * Refreshes a specific element in the tree.
	 * 
	 * @param element Element.
	 */
	public void refresh(AbstractGrepModelElement element)
	{
		viewer.refresh(element);
	}

	/**
	 * Adds a listener.
	 * 
	 * @param listener Listener.
	 */
	public void addListener(IItemTreeListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener Listener.
	 */
	public void removeListener(IItemTreeListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Fires the enabledExpressionsChaned event.
	 */
	private void fireEnabledExpressionsChanged()
	{
		for(IItemTreeListener listener: listeners)
		{
			listener.enabledExpressionsChanged();
		}
	}

	/**
	 * Fires the filterExpressionsChaned event.
	 */
	private void fireFilterExpressionsChanged()
	{
		for(IItemTreeListener listener: listeners)
		{
			listener.filterExpressionsChanged();
		}
	}
	
	/**
	 * Fires the statisticsExpressionsChaned event.
	 */
	private void fireStatisticsExpressionsChanged()
	{
		for(IItemTreeListener listener: listeners)
		{
			listener.statisticsExpressionsChanged();
		}
	}
	
	/**
	 * Fires the notificationsExpressionsChaned event.
	 */
	private void fireNotificationsExpressionsChanged()
	{
		for(IItemTreeListener listener: listeners)
		{
			listener.notificationsExpressionsChanged();
		}
	}
	
	/**
	 * Returns the tree control.
	 *  
	 * @return Tree.
	 */
	public Tree getTree()
	{
		return tree;
	}
}
