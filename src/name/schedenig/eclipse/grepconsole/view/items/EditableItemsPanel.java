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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.adapters.GrepLineStyleListener;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;
import name.schedenig.eclipse.grepconsole.model.xml.XmlHandler;
import name.schedenig.eclipse.grepconsole.model.xml.XmlHandlerException;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;
import name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Wraps an items tree panel into a panel with edit controls.
 * 
 * @author msched
 */
public class EditableItemsPanel extends Composite implements IItemTreeListener
{
	/** Current position of the sash separating the tree from the preview
	 *  panel. */
	private Integer sashPosition;
	
	/** Line style listener used for the preview. */
	private GrepLineStyleListener grepLineStyleListener;
	
	/** Color registry used for the preview. */
	private ColorRegistry colorRegistry;
	
	/** Preview panel text. */
	private String previewText;
	
	/** GUI variables. */
	private Button btnAddFolder;
	private Button btnAddExpression;
	private Button btnEdit;
	private Button btnRemove;
	private ItemsTreePanel treePanel;
	private Clipboard clipboard;
	private Button btnSaveSelected;
	private Button btnSaveAll;
	private Button btnLoad;
	private StyledText stPreview;
	private Menu treeMenu;
	private MenuItem miCut;
	private MenuItem miCopy;
	private MenuItem miPaste;
	private Composite panelTreeAndButtons;
	private Sash sash;
	private Menu menuPreview;
	private MenuItem miAddExpressionFromPreview;
	private MenuItem miLoadDefault;
	private MenuItem miSaveDefault;
	private Label labelPreview;
	private Font headerFont;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent control.
	 * @param style Style flags.
	 */
	public EditableItemsPanel(Composite parent, int style)
	{
		super(parent, style);

		init();
	}

	/**
	 * Creates the GUI content.
	 */
	private void init()
	{
		addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				colorRegistry.disposeColors();
			}
		});
		
		headerFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);// JFaceResources.getHeaderFont();
		
		colorRegistry = new ColorRegistry(Activator.getDefault().getColorRegistry());
		clipboard = new Clipboard(getDisplay());

		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		FormLayout layout = new FormLayout();
		setLayout(layout);
		
		panelTreeAndButtons = new Composite(this, SWT.NONE);
		new GridLayoutBuilder(panelTreeAndButtons, 2, false).setMargins(0).apply();
		
		treePanel = new ItemsTreePanel(panelTreeAndButtons, SWT.BORDER);
		treePanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 10));
		treePanel.addListener(this);

		hookTreeDragAndDrop();

		Tree tree = treePanel.getTree();
		
		treeMenu = new Menu(tree);
		treeMenu.addMenuListener(new MenuAdapter()
		{
			@Override
			public void menuShown(MenuEvent e)
			{
				refreshMenuItems();
			}
		});
		
		miCut = new MenuItem(treeMenu, SWT.PUSH);
		miCut.setText(Messages.EditableItemsPanel_cut);
		
		miCut.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doCopy(true);
			}
		});
		
		miCopy = new MenuItem(treeMenu, SWT.PUSH);
		miCopy.setText(Messages.EditableItemsPanel_copy);
		
		miCopy.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doCopy(false);
			}
		});

		miPaste = new MenuItem(treeMenu, SWT.PUSH);
		miPaste.setText(Messages.EditableItemsPanel_paste);
		
		miPaste.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doPaste();
			}
		});
			
		tree.setMenu(treeMenu);

		tree.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if(e.character == SWT.DEL)
				{
					doRemove();
					e.doit = false;
				}
				else if(e.keyCode == SWT.F2)
				{
					doEdit();
					e.doit = false;
				}
				else if(e.keyCode == 'c' && (e.stateMask & SWT.MOD1) != 0)
				{
					doCopy(false);
					e.doit = false;
				}
				else if(e.keyCode == 'x' && (e.stateMask & SWT.MOD1) != 0)
				{
					doCopy(true);
					e.doit = false;
				}
				else if(e.keyCode == 'v' && (e.stateMask & SWT.MOD1) != 0)
				{
					doPaste();
				}
			}
		});

		btnAddFolder = new Button(panelTreeAndButtons, SWT.PUSH);
		btnAddFolder.setText(Messages.EditableItemsPanel_add_folder);
		btnAddFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnAddFolder.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doAddFolder();
			}
		});

		btnAddExpression = new Button(panelTreeAndButtons, SWT.PUSH);
		btnAddExpression.setText(Messages.EditableItemsPanel_add_expression);
		btnAddExpression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnAddExpression.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doAddExpression(null);
			}
		});

		btnEdit = new Button(panelTreeAndButtons, SWT.PUSH);
		btnEdit.setText(Messages.EditableItemsPanel_edit);
		btnEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnEdit.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doEdit();
			}
		});

		btnRemove = new Button(panelTreeAndButtons, SWT.PUSH);
		btnRemove.setText(Messages.EditableItemsPanel_remove);
		btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnRemove.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doRemove();
			}
		});

		btnLoad = new Button(panelTreeAndButtons, SWT.PUSH);
		btnLoad.setText(Messages.EditableItemsPanel_load);
		btnLoad.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnLoad.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doLoad();
			}
		});

		btnSaveSelected = new Button(panelTreeAndButtons, SWT.PUSH);
		btnSaveSelected.setText(Messages.EditableItemsPanel_save_selected);
		btnSaveSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnSaveSelected.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doSave(false);
			}
		});

		btnSaveAll = new Button(panelTreeAndButtons, SWT.PUSH);
		btnSaveAll.setText(Messages.EditableItemsPanel_save_all);
		btnSaveAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnSaveAll.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doSave(true);
			}
		});

		sash = new Sash(this, SWT.HORIZONTAL | SWT.SMOOTH);
		
		labelPreview = new Label(this, SWT.NONE);
		labelPreview.setFont(headerFont);
		labelPreview.setText(Messages.EditableItemsPanel_preview);
		
		grepLineStyleListener = new GrepLineStyleListener(getShell(), null);
		grepLineStyleListener.setColorRegistry(colorRegistry);

		stPreview = new StyledText(this, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		stPreview.addLineStyleListener(grepLineStyleListener);
		stPreview.setFont(JFaceResources.getTextFont());

		menuPreview = new Menu(stPreview);
		miAddExpressionFromPreview = new MenuItem(menuPreview, SWT.PUSH);
		miAddExpressionFromPreview.setText(Messages.EditableItemsPanel_add_expression);
		miAddExpressionFromPreview.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doAddExpression(stPreview.getSelectionText());
			}
		});
		
		miLoadDefault = new MenuItem(menuPreview, SWT.PUSH);
		miLoadDefault.setText(Messages.EditableItemsPanel_load_default);
		miLoadDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doLoadDefaultPreview();
			}
		});
		
		miSaveDefault = new MenuItem(menuPreview, SWT.PUSH);
		miSaveDefault.setText(Messages.EditableItemsPanel_save_default);
		miSaveDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doSaveDefaultPreview();
			}
		});
		
		new MenuItem(menuPreview, SWT.SEPARATOR);
		new PreviewColorHandler(stPreview, menuPreview);
		
		menuPreview.addMenuListener(new MenuAdapter()
		{
			@Override
			public void menuShown(MenuEvent e)
			{
				Set<AbstractGrepModelElement> selection = treePanel.getSelectedElements();
				boolean hasSelection = selection != null && !selection.isEmpty();

				String text = stPreview.getSelectionText();
				miAddExpressionFromPreview.setEnabled(hasSelection && text != null && text.length() > 0);
				
				super.menuShown(e);
			}
		});
		
		stPreview.setMenu(menuPreview);
		
		int preferredPreviewHeight = ((getDisplay().getDPI().y * JFaceResources.getTextFont().getFontData()[0].getHeight()) / 72) * 4;
		
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.bottom = new FormAttachment(sash, 0);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		panelTreeAndButtons.setLayoutData(fd);

		fd = new FormData();
		fd.bottom = new FormAttachment(100, -sash.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - preferredPreviewHeight);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		sash.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(sash, 5);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		//fd.bottom = new FormAttachment(100, 0);
		labelPreview.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(labelPreview, 0);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		fd.bottom = new FormAttachment(100, 0);
		stPreview.setLayoutData(fd);
		
		sash.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Rectangle sashBounds = sash.getBounds();
				Rectangle clientArea = getClientArea();
				
				setSashPosition(-(clientArea.height - sashBounds.height - e.y));
			}
		});
		
		addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				int height = getSize().y;
				FormData fd = (FormData) sash.getLayoutData();
				
				int min = -height + 50;
				
				if(min > 0)
				{
					min = 0;
				}
				
				if(fd.bottom.offset <= min)
				{
					setSashPosition(min);
				}
			}
		});
		
		if(sashPosition != null)
		{
			setSashPosition(sashPosition);
		}
		
		refreshButtons();

		if(previewText == null || previewText.trim().length() == 0)
		{
			stPreview.setText(Activator.getDefault().getPreviewText());
		}
		else
		{
			stPreview.setText(previewText);
		}
	}

	/**
	 * Loads the default preview text.
	 */
	private void doLoadDefaultPreview()
	{
		stPreview.setText(Activator.getDefault().getPreviewText());		
	}

	/**
	 * Saves the default preview text after the user has confirmed via a dialog.
	 */
	private void doSaveDefaultPreview()
	{
		if(MessageDialog.openConfirm(getShell(), Messages.Preview_save_default_preview_title, Messages.Preview_save_default_preview_confirm))
		{
			Activator.getDefault().setPreviewText(stPreview.getText());
		}
	}

	/**
	 * Sets the position of the sash separating the tree from the preview panel.
	 * 
	 * @param sashPosition New sash position.
	 */
	protected void setSashPosition(Integer sashPosition)
	{
		this.sashPosition = sashPosition; 

		if(sash != null && sashPosition != null)
		{
			FormData fd = (FormData) sash.getLayoutData();
			fd.bottom = new FormAttachment(100, sashPosition);
			layout();
		}
	}

	/**
	 * Returns the sash position.
	 * 
	 * @return Sash position in pixels.
	 */
	public Integer getSashPosition()
	{
		return sashPosition;
	}
	
	/**
	 * Hooks drag and drop listeners to the tree.
	 */
	private void hookTreeDragAndDrop()
	{
		final Tree tree = treePanel.getTree();
		Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };

		DragSource source = new DragSource(tree, DND.DROP_MOVE);
		source.setTransfer(transferTypes);

		source.addDragListener(new DragSourceListener()
		{
			@Override
			public void dragStart(DragSourceEvent event)
			{
			}

			@Override
			public void dragSetData(DragSourceEvent event)
			{
				event.data = buildXml(false, null, true);
			}

			@Override
			public void dragFinished(DragSourceEvent event)
			{
			}
		});

		DropTarget target = new DropTarget(tree, DND.DROP_MOVE);
		target.setTransfer(transferTypes);

		target.addDropListener(new DropTargetListener()
		{
			@Override
			public void dropAccept(DropTargetEvent event)
			{
			}

			private GrepExpressionRootFolder getDropRoot(String xml)
			{
				XmlHandler handler = new XmlHandler();

				try
				{
					return handler.readExpressions(xml);
				}
				catch(XmlHandlerException ex)
				{
					Activator.getDefault().log(IStatus.ERROR, Messages.EditableItemsPanel_paste_failed, ex);
					return null;
				}
			}
			
			@Override
			public void drop(DropTargetEvent event)
			{
				if(event.data == null)
				{
					event.detail = DND.DROP_NONE;
					return;
				}
				
				event.detail = DND.DROP_NONE;
				GrepExpressionRootFolder dropRoot = getDropRoot((String) event.data);
				
				if(dropRoot == null)
				{
					return;
				}

				Set<GrepExpressionFolder> folders = new HashSet<GrepExpressionFolder>();
				Set<GrepExpressionItem> items = new HashSet<GrepExpressionItem>();

				for(AbstractGrepModelElement element: dropRoot.getChildren())
				{
					if(element instanceof GrepExpressionFolder)
					{
						folders.add((GrepExpressionFolder) element);
					}
					else
					{
						items.add((GrepExpressionItem) element);
					}
				}

				if(!folders.isEmpty() && !items.isEmpty())
				{
					event.detail = DND.DROP_NONE;
					return;
				}
				
				boolean droppingFolders = !folders.isEmpty();
				TreeItem item = (TreeItem) event.item;
				
				if(item == null)
				{
					event.detail = DND.DROP_NONE;
					return;
				}
				
				AbstractGrepModelElement targetElement = (AbstractGrepModelElement) item.getData();
				Point pt = tree.getDisplay().map(null, tree, event.x, event.y);
				Rectangle bounds = item.getBounds();

				GrepExpressionFolder root = getRootFolder();
				GrepExpressionFolder targetFolder;
				int offset;
				boolean isFolder = item.getData() instanceof GrepExpressionFolder;
				
				if(pt.y < bounds.y + (isFolder ? bounds.height / 3 : bounds.height / 2))
				{
					targetFolder = targetElement.getParent();
					offset = targetFolder.getChildren().indexOf(targetElement);
				}
				else if(!isFolder || pt.y > bounds.y + 2 * bounds.height / 3)
				{
					targetFolder = targetElement.getParent();
					offset = targetFolder.getChildren().indexOf(targetElement) + 1;
				}
				else
				{
					targetFolder = (GrepExpressionFolder) (isFolder ? targetElement : targetElement.getParent());
					offset = isFolder ? -1 : targetFolder.getChildren().indexOf(targetElement);
				}

				if(droppingFolders && targetFolder != root)
				{
					offset = root.getChildren().indexOf(targetFolder) + 1;
					targetFolder = root;
				}
				else if(!droppingFolders && targetFolder == root)
				{
					if(offset <= 0 || offset > root.getChildren().size())
					{
						event.detail = DND.DROP_NONE;
						return;
					}
					
					targetFolder = (GrepExpressionFolder) root.getChildren().get(offset - 1);
					offset = -1;
				}
				
				LinkedList<AbstractGrepModelElement> remove = new LinkedList<AbstractGrepModelElement>();
				List<AbstractGrepModelElement> dropElements = new LinkedList<AbstractGrepModelElement>(dropRoot.getChildren());
				
				for(AbstractGrepModelElement element: dropElements)
				{
					remove.add(root.findById(element.getId()));
					targetFolder.add(element, offset);
					
					if(offset >= 0)
					{
						offset++;
					}
				}
				
				for(AbstractGrepModelElement element: remove)
				{
					element.getParent().remove(element);
				}
				
				treePanel.refresh();
			}

			@Override
			public void dragOver(DropTargetEvent event)
			{
				TextTransfer textTransfer = TextTransfer.getInstance();
				
				if(!textTransfer.isSupportedType(event.currentDataType))
				{
					event.operations = DND.DROP_NONE;
					return;
				}
				
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;

				if(event.item == null)
				{
					return;
				}

				boolean droppingFolders = false;
				Object o = textTransfer.nativeToJava(event.currentDataType);
				String xml = (String) o;
				
				if(xml != null)
				{
					GrepExpressionRootFolder dropRoot = getDropRoot(xml);
					
					for(AbstractGrepModelElement element: dropRoot.getChildren())
					{
						if(element instanceof GrepExpressionFolder)
						{
							droppingFolders = true;
							break;
						}
					}
				}						
				
				TreeItem item = (TreeItem) event.item;
				Point pt = tree.getDisplay().map(null, tree, event.x, event.y);
				Rectangle bounds = item.getBounds();

				boolean isFolder = item.getData() instanceof GrepExpressionFolder;
				
				if(pt.y < bounds.y + (isFolder ? bounds.height / 3 : bounds.height / 2))
				{
					event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
				}
				else if(!isFolder || pt.y > bounds.y + 2 * bounds.height / 3)
				{
					event.feedback |= DND.FEEDBACK_INSERT_AFTER;
				}
				else
				{
					if(droppingFolders)
					{
						event.feedback = DND.FEEDBACK_NONE;
					}
					else
					{
						event.feedback |= DND.FEEDBACK_SELECT;
					}
				}
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event)
			{
			}

			@Override
			public void dragLeave(DropTargetEvent event)
			{
			}

			@Override
			public void dragEnter(DropTargetEvent event)
			{
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
		treePanel.setExpressions(expressions);
		refreshLineStyleListener();
		resizeColumns();
	}

	/**
	 * Recalculates the tree columns' sizes.
	 */
	public void resizeColumns()
	{
		treePanel.resizeColumns();		
	}

	/**
	 * Updates the line style listener used for the preview. 
	 */
	private void refreshLineStyleListener()
	{
		LinkedList<GrepExpressionItem> items = new LinkedList<GrepExpressionItem>();
		GrepExpressionsWithSelection expressions = getExpressions();
		GrepExpressionRootFolder root = expressions.getRootFolder();
		addAllEnabledItems(items, root, expressions.getEnablementMap());

		grepLineStyleListener.setItems(items);
		stPreview.redraw();
	}

	/**
	 * Recursively collects all enabled items in a list.
	 * 
	 * @param items List of items. Found items are added to this list.
	 * @param element Element to search.
	 * @param enablementMap Map of enablement states, or <code>null</code>.
	 */
	private void addAllEnabledItems(LinkedList<GrepExpressionItem> items, 
			AbstractGrepModelElement element, Map<String, Boolean> enablementMap)
	{
		Boolean enablement = enablementMap == null ? null : enablementMap.get(element.getId());

		if(enablement == null)
		{
			enablement = element.isDefaultEnabled();
		}
		
		if(!(element instanceof GrepExpressionRootFolder) && !enablement)
		{
			return;
		}

		if(element instanceof GrepExpressionItem)
		{
			items.add((GrepExpressionItem) element);
		}
		else if(element instanceof GrepExpressionFolder)
		{
			for(AbstractGrepModelElement child: ((GrepExpressionFolder) element).getChildren())
			{
				addAllEnabledItems(items, child, enablementMap);
			}
		}
	}

	/**
	 * Returns the model root being edited.
	 * 
	 * @return Model root.
	 */
	public GrepExpressionsWithSelection getExpressions()
	{
		return treePanel.getExpressions();
	}

	/**
	 * Adds a new folder.
	 */
	private void doAddFolder()
	{
		GrepExpressionFolder folder = new GrepExpressionFolder();

		if(editFolder(folder, true))
		{
			getRootFolder().add(folder);
			treePanel.refresh();
		}
	}

	/**
	 * Returns the model root folder.
	 * 
	 * @return Root folder.
	 */
	private GrepExpressionFolder getRootFolder()
	{
		return getExpressions().getRootFolder();
	}

	/**
	 * Adds a new expression.
	 * 
	 * @param text Optional default text. If specified, the text is quoted and
	 * 		escaped as a regular expression.
	 */
	private void doAddExpression(String text)
	{
		GrepExpressionItem item = new GrepExpressionItem();
		GrepExpressionFolder folder = treePanel.getSelectedFolder();
		folder.add(item);
		
		if(text != null)
		{
			item.setGrepExpression("(" + Pattern.quote(text) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
				
		if(editExpression(item, true))
		{
			treePanel.refresh();
			refreshLineStyleListener();
		}
		else
		{
			folder.remove(item);
		}
	}

	/**
	 * Edits the currently selected model element.
	 * 
	 * Calls <code>editFolder()</code> or <code>editExpression()</code> depending
	 * on the item type. 
	 */
	private void doEdit()
	{
		AbstractGrepModelElement element = treePanel.getSelectedElement();
		boolean refresh;

		if(element instanceof GrepExpressionFolder)
		{
			refresh = editFolder((GrepExpressionFolder) element, false);
		}
		else
		{
			refresh = editExpression((GrepExpressionItem) element, false);
		}

		if(refresh)
		{
			treePanel.refresh();
			refreshLineStyleListener();
		}
	}

	/**
	 * Edits the specified folder.
	 * 
	 * @param folder Folder.
	 */
	public boolean editFolder(GrepExpressionFolder folder, boolean add)
	{
		FolderDialog dlg = new FolderDialog(getShell(), folder, add);

		return dlg.open() == InputDialog.OK;
	}

	/**
	 * Edits the specified item.
	 * 
	 * @param item Item.
	 */
	public boolean editExpression(GrepExpressionItem item, boolean add)
	{
		ExpressionDialog dlg = new ExpressionDialog(getShell(), add);
		dlg.setPreviewText(stPreview.getText());
		dlg.setItem(item);

		return dlg.open() == ExpressionDialog.OK;
	}

	/**
	 * Removes the selected item.
	 */
	private void doRemove()
	{
		Set<AbstractGrepModelElement> elements = treePanel.getSelectedElements();
		
		if(elements == null)
		{
			return;
		}
		
		if(!MessageDialog.openConfirm(getShell(), Messages.EditableItemsPanel_delete_items, Messages.EditableItemsPanel_really_delete_items))
		{
			return;
		}
		
		Set<AbstractGrepModelElement> deleted = new HashSet<AbstractGrepModelElement>();
		
		for(AbstractGrepModelElement element: elements)
		{
			GrepExpressionFolder parent = element.getParent();
			
			if(deleted.contains(parent))
			{
				continue;
			}
			
			parent.remove(element);
			deleted.add(element);
		}

		treePanel.refresh();
		// TODO: select next element
	}

	/**
	 * Saves an element tree to a file.
	 * 
	 * @param all Whether to save the entire model tree or only the selected
	 * 		elements.
	 */
	protected void doSave(boolean all)
	{
		String xml = buildXml(all, null, true);

		FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
		String fileName = dlg.open();

		if(fileName != null)
		{
			File file = new File(fileName);
			BufferedWriter writer = null;

			try
			{
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"))); //$NON-NLS-1$
				writer.write(xml);
			}
			catch(IOException ex)
			{
				Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_save_settings, ex);
			}
			finally
			{
				if(writer != null)
				{
					try
					{
						writer.close();
					}
					catch(IOException ex)
					{
						Activator.getDefault().log(IStatus.WARNING, ex);
					}
				}
			}
		}
	}

	/**
	 * Serialises an element tree to an XML string.
	 * 
	 * @param all Whether to serialise the entire model tree or only the selected
	 * 		elements.
	 * @param cutElements If provided, this list will be filled with all elements
	 * 		that should be deleted as part of a cut operation. May be
	 * 		<code>null</code>.
	 * @param identityCopy Whether the XML string should use the original element
	 * 		IDs or newly generated ones.
	 * 
	 * @return
	 */
	private String buildXml(boolean all,
			LinkedList<AbstractGrepModelElement> cutElements, boolean identityCopy)
	{
		Set<AbstractGrepModelElement> allElements;
		GrepExpressionRootFolder copyRoot;

		if(all)
		{
			copyRoot = getExpressions().getRootFolder();
			allElements = new LinkedHashSet<AbstractGrepModelElement>();
			allElements.addAll(copyRoot.getChildren());
		}
		else
		{
			allElements = treePanel.getSelectedElements();
			copyRoot = new GrepExpressionRootFolder();

			for(AbstractGrepModelElement element: allElements)
			{
				AbstractGrepModelElement parent = element.getParent();
				boolean skip = false;

				while(parent != null)
				{
					if(allElements.contains(parent))
					{
						skip = true;
						break;
					}

					parent = parent.getParent();
				}

				if(!skip)
				{
					AbstractGrepModelElement newElement = element.copy(identityCopy);
					copyRoot.add(newElement);

					if(cutElements != null)
					{
						cutElements.add(element);
					}
				}
			}
		}

		XmlHandler handler = new XmlHandler();
		handler.recalculateStyleSet(copyRoot);

		try
		{
			return handler.createXmlString(copyRoot);
		}
		catch(ParserConfigurationException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, ex);
		}
		catch(TransformerException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, ex);
		}

		return null;
	}

	/**
	 * Performs a copy or cut operation.
	 * 
	 * @param cut <code>true</code> for cut, <code>false</code> for copy. 
	 */
	private void doCopy(boolean cut)
	{
		LinkedList<AbstractGrepModelElement> cutElements = cut ?
				new LinkedList<AbstractGrepModelElement>() : null;
		String xml = buildXml(false, cutElements, cut);
		TextTransfer textTransfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[] { xml }, new Transfer[] { textTransfer });

		if(cut)
		{
			for(AbstractGrepModelElement element: cutElements)
			{
				element.getParent().remove(element);
			}

			treePanel.refresh();
		}
	}

	/**
	 * Loads an element tree from a file.
	 */
	protected void doLoad()
	{
		FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
		String fileName = dlg.open();

		if(fileName == null)
		{
			return;
		}

		File file = new File(fileName);
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();

		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			for(;;)
			{
				String s = reader.readLine();

				if(s == null)
				{
					break;
				}

				if(sb.length() > 0)
				{
					sb.append("\n"); //$NON-NLS-1$
				}

				sb.append(s);
			}
		}
		catch(IOException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.Error_could_not_load_settings, ex);
		}
		finally
		{
			if(reader != null)
			{
				try
				{
					reader.close();
				}
				catch(IOException ex)
				{
					Activator.getDefault().log(IStatus.WARNING, ex);
				}
			}
		}

		String xml = sb.toString();
		paste(getExpressions().getRootFolder(), xml);
	}

	/**
	 * Performs a paste operation.
	 */
	private void doPaste()
	{
		TextTransfer transfer = TextTransfer.getInstance();
		paste(treePanel.getSelectedElement(), (String) clipboard.getContents(transfer));
	}

	/**
	 * Pastes the elements serialised in the provided XML string into the
	 * specified target element.
	 * 
	 * @param selectedElement Target element. If this is <code>null</code>, the
	 * 		root folder is used as the target. If an expression item is specified,
	 * 		its parent folder is used as the target.
	 * @param xml XML string containing a serialised element tree.
	 */
	private void paste(AbstractGrepModelElement selectedElement, String xml)
	{
		GrepExpressionFolder parent = selectedElement == null ? treePanel.getExpressions().getRootFolder() : selectedElement instanceof GrepExpressionFolder ? (GrepExpressionFolder) selectedElement : selectedElement.getParent();
		GrepExpressionFolder root = parent.getParent() == null ? parent : parent.getParent();

		XmlHandler handler = new XmlHandler();
		GrepExpressionRootFolder copyRoot;

		try
		{
			copyRoot = handler.readExpressions(xml);
		}
		catch(XmlHandlerException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.EditableItemsPanel_paste_failed, ex);
			return;
		}

		copyRoot.rewriteDuplicateIds(root.getAllIds(new HashSet<String>()));

		GrepExpressionFolder newFolder = null;

		for(AbstractGrepModelElement element: copyRoot.getChildren())
		{
			boolean isItem = element instanceof GrepExpressionItem;

			if(isItem)
			{
				if(parent == root)
				{
					if(newFolder == null)
					{
						newFolder = new GrepExpressionFolder();
						newFolder.setName(Messages.EditableItemsPanel_new_items_folder_title);
						parent.add(newFolder);
					}

					newFolder.add(element.copy(true));
				}
				else
				{
					parent.add(element.copy(true));
				}
			}
			else
			{
				root.add(element.copy(true));
			}
		}

		root.getRoot().addMissingStyles();
		treePanel.refresh();
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.
	 * IEnabledExpressionsListener#enabledExpressionsChanged()
	 */
	@Override
	public void enabledExpressionsChanged()
	{
		refreshLineStyleListener();
	}
	
	/**
	 * Returns the items tree panel.
	 * 
	 * @return Panel.
	 */
	public ItemsTreePanel getTreePanel()
	{
		return treePanel;
	}
	
	/**
	 * Refreshes the popup menu.
	 */
	private void refreshMenuItems()
	{
		Set<AbstractGrepModelElement> selection = treePanel.getSelectedElements();
		boolean noSelection = selection == null || selection.isEmpty();
		
		boolean canPaste = false;
		TextTransfer transfer = TextTransfer.getInstance();
		String xml = (String) clipboard.getContents(transfer);
		
		if(xml != null)
		{
			try
			{
				new XmlHandler().readExpressions(xml);
				canPaste = true;
			}
			catch(XmlHandlerException ex)
			{
			}
		}

		miCut.setEnabled(!noSelection);
		miCopy.setEnabled(!noSelection);
		miPaste.setEnabled(canPaste);
	}

	/**
	 * Triggers an edit operation when an element is double clicked.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#elementDoubleClicked(name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement)
	 */
	@Override
	public void elementDoubleClicked(AbstractGrepModelElement element)
	{
		doEdit();
	}

	/**
	 * Refreshes the buttons when the element selection changes.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#elementSelectionChanged(java.util.Set)
	 */
	@Override
	public void elementSelectionChanged(Set<AbstractGrepModelElement> elements)
	{
		refreshButtons();
	}

	/**
	 * Refrehes the buttons according to the currently selected elements. 
	 */
	private void refreshButtons()
	{
		Set<AbstractGrepModelElement> selection = treePanel.getSelectedElements();
		boolean hasSelection = selection != null && !selection.isEmpty();
		
		btnAddExpression.setEnabled(hasSelection);
		btnEdit.setEnabled(hasSelection);
		btnRemove.setEnabled(hasSelection);
		btnSaveSelected.setEnabled(hasSelection);
	}

	/**
	 * Sets the preview text.
	 * 
	 * @param previewText Preview text.
	 */
	public void setPreviewText(String previewText)
	{
		this.previewText = previewText;
		
		if(stPreview != null && previewText != null)
		{
			stPreview.setText(previewText);
		}
	}
	
	/**
	 * Returns the preview text.
	 * 
	 * @return Preview text
	 */
	public String getPreviewText()
	{
		return previewText;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#filterExpressionsChanged()
	 */
	@Override
	public void filterExpressionsChanged()
	{
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#statisticsExpressionsChanged()
	 */
	@Override
	public void statisticsExpressionsChanged()
	{
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.view.items.ItemsTreePanel.IItemTreeListener#notificationsExpressionsChanged()
	 */
	@Override
	public void notificationsExpressionsChanged()
	{
	}
	
	/**
	 * Refreshes the tree.
	 */
	public void refresh()
	{
		treePanel.refresh();
	}
}
