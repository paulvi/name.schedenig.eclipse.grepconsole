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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.adapters.GrepLineStyleListener;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;
import name.schedenig.eclipse.grepconsole.model.xml.XmlHandler;
import name.schedenig.eclipse.grepconsole.model.xml.XmlHandlerException;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;
import name.schedenig.eclipse.grepconsole.view.DefaultSizeDialog;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;
import name.schedenig.eclipse.grepconsole.view.items.links.LinkDialog;
import name.schedenig.eclipse.grepconsole.view.styles.StylesPanel;
import name.schedenig.eclipse.grepconsole.view.styles.StylesPanel.IStylesListener;
import name.schedenig.eclipse.grepconsole.view.whatsnew.WhatsNewDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * A modal dialog for editing a grep expression.
 * 
 * @author msched
 */
public class ExpressionDialog extends DefaultSizeDialog implements IStylesListener
{
	/** Dialog settings section key. */
	public static final String DIALOG_SETTINGS_SECTION = "expressionDialog"; //$NON-NLS-1$

	/** URL to be shown when the "pattern help" link is clicked. */
	protected static final String URL_PATTERN_HELP = "http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html"; //$NON-NLS-1$

	/** Whether the dialogue is used to add a new item or edit an existing one. */
	private boolean add;

	/** The item being edited. Update only when the user confirms the changes. */
	private GrepExpressionItem item;
	
	/** A working copy of the item being edited. Updated continuously. */
	private GrepExpressionItem workItem;
	
	/** Preview text. */
	private String previewText;
	
	/** Color registry used for the preview. */
	private ColorRegistry colorRegistry;
	
	/** Line style listener used for the preview. */
	private GrepLineStyleListener grepLineStyleListener;

	/** Stores an array of all used capture groups. This way they can be restored
	 *  if the user edits the expression and temporarily reduces the number of
	 *  capture groups. */
	private GrepGroup[] groupArray;
	
	/** Stores an array of all used rewerite groups. This way they can be restored
	 *  if the user edits the expression and temporarily reduces the number of
	 *  rewrite groups. */
	private GrepGroup[] rewriteGroupArray;
	
	/** Keeps a copy of the model tree's styles so they can be restored if the
	 *  user cancels the dialog. */
	private HashSet<GrepStyle> backupStyles;

	/** Keeps track of whether two or more groups have the same name assigned. */
	private boolean duplicateGroupNames = false;

	/** GUI variables. */
	private Label labelName;
	private Text textName;
	private Label labelExpression;
	private Text textExpression;
	private Label labelUnlessExpression;
	private Text textUnlessExpression;
	private Composite panelHeader;
	private StyledText stPreview;
	private Menu menuPreview;
	private MenuItem miAddExpressionFromPreview;
	private MenuItem miAddUnlessExpressionFromPreview;
	private TableViewerColumn colGroup;
	private TableViewerColumn colStyle;
	private Composite panelStyleAssignments;
	private Table tableStyleAssignments;
	private TableViewer viewerStyleAssignments;
	private StylesPanel panelAvailableStyles;
	private Button btnAssignStyle;
	private SashForm panelStyleTables;
	private Button cbActiveByDefault;
	private Button cbFilterDefault;
	private Button cbCaseSensitive;
	private MenuItem miLoadDefault;
	private MenuItem miSaveDefault;
	private TableViewerColumn colLink;
	private Menu menuGroups;
	private MenuItem miCopyLink;
	private MenuItem miPasteLink;
	private Clipboard clipboard;
	private Link labelPatternHelp;
	private Button cbRemoveOriginalStyle;
	private Label labelRewrite;
	private Text textRewrite;
	private StyleAssignmentsGroupLabelProvider groupLabelProvider;
	private Label labelQuickExpression;
	private Text textQuickExpression;
	private TabFolder tabs;
	private TabItem tiStyles;
	private TabItem tiGeneral;
	private Composite panelGeneral;
	private TabItem tiNotifications;
	private Composite panelNotifications;
	private Button cbPopupNotification;
	private Text textNotificationTitle;
	private Text textNotificationMessage;
	private Text textSoundNotification;
	private Button cbSoundNotification;
	private Button btnSoundNotification;
	private Button btnPlaySoundNotification;
	private Button cbNotificationsDefault;
	private Label labelPopupNotifications;
	private Label labelSoundNotifications;
	private Font headerFont;
	private Label labelPreview;
	private LinkPickerLine pickerNotificationLink;
	private LinkPickerLine pickerAutostartLink;
	private Label labelAutostartLink;
	private Composite compositePopupNotification;
	private Composite compositeOtherNotifications;
	private TabItem tiGrepView;
	private Composite panelGrepView;
	private TabItem tiStatistics;
	private Label labelStatisticsCountLabel;
	private Text textStatisticsCountLabel;
	private Label labelStatisticsValueLabel;
	private Text textStatisticsValueLabel;
	private Composite panelStatistics;
	private Label labelStatisticsValuePattern;
	private Text textStatisticsValuePattern;
	private Button cbStatisticsDefault;

	private Label labelStatisticsCountHeader;

	private Label labelStatisticsValueHeader;

	/**
	 * Creates a new instance.
	 * 
	 * @param parentShell Parent shell.
	 * @param add Whether the dialogue is used to add a new item or edit an
	 * 		existing one.
	 */
	public ExpressionDialog(Shell parentShell, boolean add)
	{
		super(parentShell);
		
		this.add = add;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create()
	{
		super.create();
		
		// We do this here after the bounds have been initialised
		refresh();
	}
	
	/**
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	protected int getShellStyle()
	{
		return super.getShellStyle() | SWT.RESIZE;
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		
		newShell.setText(add ? Messages.ExpressionDialog_title_add_expression : Messages.ExpressionDialog_title_edit_expression);
		newShell.addShellListener(new WhatsNewDialog.ShellListener());
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		headerFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
		
		parent.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				if(colorRegistry != null)
				{
					colorRegistry.disposeColors();
				}
			}
		});
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, Activator.PLUGIN_ID + ".edit_expression"); //$NON-NLS-1$

		colorRegistry = new ColorRegistry(Activator.getDefault().getColorRegistry());
		clipboard = new Clipboard(parent.getDisplay());
		
		Composite composite = parent;
		new GridLayoutBuilder(composite, 1, false).apply();

		panelHeader = createHeaderPanel(composite);
		panelHeader.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		tabs = new TabFolder(composite, SWT.TOP);
		tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tiGeneral = new TabItem(tabs, SWT.NONE);
		tiGeneral.setText(Messages.ExpressionDialog_general);
		
		panelGeneral = createGeneralPanel(tabs);
		tiGeneral.setControl(panelGeneral);
		
		tiStyles = new TabItem(tabs, SWT.NONE);
		tiStyles.setText(Messages.ExpressionDialog_styles_links);
		
		tiGrepView = new TabItem(tabs, SWT.NONE);
		tiGrepView.setText(Messages.ExpressionDialog_grep_view);

		panelGrepView = createGrepViewPanel(tabs);
		tiGrepView.setControl(panelGrepView);

		tiNotifications = new TabItem(tabs, SWT.NONE);
		tiNotifications.setText(Messages.ExpressionDialog_notifications_tab);

		panelNotifications = createNotificationsPanel(tabs);
		tiNotifications.setControl(panelNotifications);

		tiStatistics = new TabItem(tabs, SWT.NONE);
		tiStatistics.setText(Messages.ExpressionDialog_statistics_tab);

		panelStatistics = createStatisticsPanel(tabs);
		tiStatistics.setControl(panelStatistics);

		panelStyleTables = new SashForm(tabs, SWT.HORIZONTAL);
		panelStyleTables.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tiStyles.setControl(panelStyleTables);
		
		panelStyleAssignments = new Composite(panelStyleTables, SWT.NONE);
		panelStyleAssignments.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayoutBuilder builder = new GridLayoutBuilder(panelStyleAssignments, 3, false).setMarginRight(0);
		builder.apply();
		GridLayout panelStyleAssignmentsLayout = builder.getLayout();
		
		panelStyleTables.setSashWidth(((GridLayout) panelStyleAssignments.getLayout()).marginLeft);
		
		tableStyleAssignments = new Table(panelStyleAssignments, SWT.BORDER | SWT.FULL_SELECTION);
		tableStyleAssignments.setHeaderVisible(true);
		tableStyleAssignments.setLinesVisible(false);
		tableStyleAssignments.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		viewerStyleAssignments = new TableViewer(tableStyleAssignments);
		viewerStyleAssignments.setContentProvider(new StyleAssignmentsContentProvider());

		groupLabelProvider = new StyleAssignmentsGroupLabelProvider(viewerStyleAssignments);
		
		colGroup = new TableViewerColumn(viewerStyleAssignments, SWT.LEFT);
		colGroup.getColumn().setText(Messages.ExpressionDialog_column_group);
		colGroup.getColumn().setWidth(130);
		colGroup.setLabelProvider(groupLabelProvider);

		colStyle = new TableViewerColumn(viewerStyleAssignments, SWT.LEFT);
		colStyle.getColumn().setText(Messages.ExpressionDialog_column_style);
		colStyle.getColumn().setWidth(130);
		colStyle.setLabelProvider(new StyleAssignmentsStyleLabelProvider(viewerStyleAssignments));

		colLink = new TableViewerColumn(viewerStyleAssignments, SWT.RIGHT);
		colLink.getColumn().setText(Messages.ExpressionDialog_link);
		colLink.getColumn().setWidth(70);
		colLink.setLabelProvider(new StyleAssignmentsLinkLabelProvider(viewerStyleAssignments));
		
		ColumnViewerToolTipSupport.enableFor(viewerStyleAssignments);
		
		menuGroups = new Menu(tableStyleAssignments);
		
		miCopyLink = new MenuItem(menuGroups, SWT.PUSH);
		miCopyLink.setText(Messages.ExpressionDialog_copy_link);
		miCopyLink.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				copyLink();
			}
		});
		
		miPasteLink = new MenuItem(menuGroups, SWT.PUSH);
		miPasteLink.setText(Messages.ExpressionDialog_paste_link);
		miPasteLink.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				pasteLink();
			}
		});
		
		menuGroups.addMenuListener(new MenuAdapter()
		{
			@Override
			public void menuShown(MenuEvent e)
			{
				Integer group = getSelectedGroup((IStructuredSelection) viewerStyleAssignments.getSelection());
				
				miCopyLink.setEnabled(group != null && getGroup(group).getLink() != null);
				miPasteLink.setEnabled(group != null);
			}
		});
		
		tableStyleAssignments.setMenu(menuGroups);
		
		viewerStyleAssignments.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				groupSelected(getSelectedGroup((IStructuredSelection) event.getSelection()));
			}
		});
		
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewerStyleAssignments, new FocusCellOwnerDrawHighlighter(viewerStyleAssignments));

		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(viewerStyleAssignments)
		{
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event)
			{
				// Enable editor only with mouse double click
				if(event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION)
				{
					EventObject source = event.sourceEvent;
					
					if(source instanceof MouseEvent && ((MouseEvent) source).button == 3)
					{
						return false;
					}
					
					return true;
				}

				return false;
			}
		};

		TableViewerEditor.create(viewerStyleAssignments, focusCellManager, activationSupport, 
				ColumnViewerEditor.TABBING_HORIZONTAL | 
		    ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | 
		    ColumnViewerEditor.TABBING_VERTICAL |
		    ColumnViewerEditor.KEYBOARD_ACTIVATION);

		EditingSupport nameEditingSupport = new EditingSupport(viewerStyleAssignments)
		{
			@Override
			protected void setValue(Object element, Object value)
			{
				String s = ((String) value).trim();
				
				if(s.length() == 0)
				{
					s = null;
				}
				
				getGroup((Integer) element).setName(s);
				viewerStyleAssignments.refresh(element);
				updateDuplicateGroupNames();
			}
			
			@Override
			protected Object getValue(Object element)
			{
				GrepGroup group = getGroup((Integer) element);
				return group.getName() == null ? "" : group.getName(); //$NON-NLS-1$
			}
			
			@Override
			protected CellEditor getCellEditor(Object element)
			{
				return new TextCellEditor(viewerStyleAssignments.getTable());
			}
			
			@Override
			protected boolean canEdit(Object element)
			{
				return true;
			}
		};

		colGroup.setEditingSupport(nameEditingSupport);

		DoubleClickEditingSupport styleEditingSupport = new DoubleClickEditingSupport(viewerStyleAssignments)
		{
			@Override
			protected void doEdit(Object value)
			{
				Integer group = getSelectedGroup((IStructuredSelection) viewerStyleAssignments.getSelection());
				final GrepStyle style = getGroup(group).getStyle();
				
				getShell().getDisplay().asyncExec(new Runnable()
				{
					public void run()
					{
						if(style == null)
						{
							panelAvailableStyles.doNewStyle();
						}
						else
						{
							panelAvailableStyles.doEditStyle(style);
						}
					}
				});				
			}
		};
		
		colStyle.setEditingSupport(styleEditingSupport);
				
		colLink.setEditingSupport(new DoubleClickEditingSupport(viewerStyleAssignments)
		{
			@Override
			protected void doEdit(Object value)
			{
				final Integer groupIndex = getSelectedGroup((IStructuredSelection) viewerStyleAssignments.getSelection());
				final GrepGroup group = getGroup(groupIndex);

				getShell().getDisplay().asyncExec(new Runnable()
				{
					public void run()
					{
						LinkDialog dlg = new LinkDialog(getShell(), groupIndex < workItem.getGroups().length);
						dlg.setLink(group.getLink());
						
						if(dlg.open() == LinkDialog.OK)
						{
							group.setLink(dlg.getLink());
							viewerStyleAssignments.refresh();
						}
					}
				});				
			}
		});

		cbRemoveOriginalStyle = new Button(panelStyleAssignments, SWT.CHECK);
		cbRemoveOriginalStyle.setText(Messages.ExpressionDialog_remove_original_style);
		cbRemoveOriginalStyle.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		
		cbRemoveOriginalStyle.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				workItem.setRemoveOriginalStyle(cbRemoveOriginalStyle.getSelection());
				refreshPreview();
			}
		});

		grepLineStyleListener = new GrepLineStyleListener(getShell(), null);
		grepLineStyleListener.setColorRegistry(colorRegistry);

		labelPreview = new Label(panelStyleAssignments, SWT.NONE);
		labelPreview.setFont(headerFont);
		labelPreview.setText(Messages.ExpressionDialog_preview);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gridData.verticalIndent = 10;
		labelPreview.setLayoutData(gridData);
		
		stPreview = new StyledText(panelStyleAssignments, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd.heightHint = ((parent.getDisplay().getDPI().y * JFaceResources.getTextFont().getFontData()[0].getHeight()) / 72) * 4;
		stPreview.setLayoutData(gd);
		stPreview.addLineStyleListener(grepLineStyleListener);
		stPreview.setFont(JFaceResources.getTextFont());
		
		menuPreview = new Menu(stPreview);
		miAddExpressionFromPreview = new MenuItem(menuPreview, SWT.PUSH);
		miAddExpressionFromPreview.setText(Messages.ExpressionDialog_use_as_expression);
		miAddExpressionFromPreview.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setExpression(stPreview.getSelectionText());
			}
		});
		
		miAddUnlessExpressionFromPreview = new MenuItem(menuPreview, SWT.PUSH);
		miAddUnlessExpressionFromPreview.setText(Messages.ExpressionDialog_use_as_unless_expression);
		miAddUnlessExpressionFromPreview.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setUnlessExpression(stPreview.getSelectionText());
			}
		});
		
		miLoadDefault = new MenuItem(menuPreview, SWT.PUSH);
		miLoadDefault.setText(Messages.ExpressionDialog_load_default);
		miLoadDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doLoadDefaultPreview();
			}
		});
		
		miSaveDefault = new MenuItem(menuPreview, SWT.PUSH);
		miSaveDefault.setText(Messages.ExpressionDialog_save_default);
		miSaveDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doSaveDefaultPreview();
			}
		});

		menuPreview.addMenuListener(new MenuAdapter()
		{
			@Override
			public void menuShown(MenuEvent e)
			{
				String text = stPreview.getSelectionText();
				miAddExpressionFromPreview.setEnabled(text != null && text.length() > 0);
				
				super.menuShown(e);
			}
		});

		new MenuItem(menuPreview, SWT.SEPARATOR);
		new PreviewColorHandler(stPreview, menuPreview);
		
		stPreview.setMenu(menuPreview);
		
		panelAvailableStyles = new StylesPanel(panelStyleTables, SWT.NONE)
		{
			@Override
			protected void doStylesSelected(Collection<GrepStyle> styles)
			{
				btnAssignStyle.setEnabled(styles.size() == 1);

				super.doStylesSelected(styles);
			}
			
			protected void createButtons(Composite parent)
			{
				btnAssignStyle = new Button(parent, SWT.PUSH);
				btnAssignStyle.setText(Messages.ExpressionDialog_assign);
				btnAssignStyle.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						doAssignStyle(panelAvailableStyles.getSelectedStyle());
					}
				});

				super.createButtons(parent);
			};
		};
		
		GridLayout panelAvailableStylesLayout = (GridLayout) panelAvailableStyles.getLayout();
		panelAvailableStylesLayout.marginLeft = panelStyleAssignmentsLayout.marginRight; 
		panelAvailableStylesLayout.marginRight = panelStyleAssignmentsLayout.marginLeft; 
		panelAvailableStylesLayout.marginWidth = panelStyleAssignmentsLayout.marginWidth; 
		panelAvailableStylesLayout.marginHeight = panelStyleAssignmentsLayout.marginHeight; 
		panelAvailableStylesLayout.marginTop = panelStyleAssignmentsLayout.marginTop; 
		panelAvailableStylesLayout.marginBottom = panelStyleAssignmentsLayout.marginBottom; 

		panelAvailableStyles.setIncludeNullStyle(true);
		panelAvailableStyles.addListener(this);
		
		if(workItem != null)
		{
			panelAvailableStyles.setRoot(workItem.getRoot());
		}
		
		textExpression.setFocus();
		
		return composite;
	}

	/**
	 * Pastes a link from the clipboard to the selected group.
	 */
	protected void pasteLink()
	{
		Integer group = getSelectedGroup((IStructuredSelection) viewerStyleAssignments.getSelection());
		
		if(group == null)
		{
			return;
		}
		
		IGrepLink link = getLinkFromClipboard();
		
		if(link != null)
		{
			getGroup(group).setLink(link);
			viewerStyleAssignments.refresh();
		}
	}

	/**
	 * Reads a grep link from the clipboard.
	 * 
	 * @return Grep link, or <code>null</code> if the clipboard did not contain
	 * 		one.
	 */
	private IGrepLink getLinkFromClipboard()
	{
		TextTransfer transfer = TextTransfer.getInstance();
		String xml = (String) clipboard.getContents(transfer);
		
		XmlHandler handler = new XmlHandler();

		try
		{
			return handler.readLink(xml);
		}
		catch(XmlHandlerException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.EditableItemsPanel_paste_failed, ex);
			return null;
		}
	}

	/**
	 * Copies the selected group's link to the clipboard.
	 */
	protected void copyLink()
	{
		Integer group = getSelectedGroup((IStructuredSelection) viewerStyleAssignments.getSelection());
		
		if(group == null)
		{
			return;
		}
		
		copyLink(getGroup(group).getLink());
	}
	
	/**
	 * Copies the specified link to the clipboard.
	 * 
	 * @param link Link.
	 */
	protected void copyLink(IGrepLink link)
	{
		XmlHandler xmlHandler = new XmlHandler();
		
		try
		{
			String xml = xmlHandler.createLinkXmlString(link);
			TextTransfer textTransfer = TextTransfer.getInstance();
			clipboard.setContents(new Object[] { xml }, new Transfer[] { textTransfer });
		}
		catch(ParserConfigurationException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, "Could not copy link to clipboard.", ex); //$NON-NLS-1$
		}
		catch(TransformerException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, "Could not copy link to clipboard.", ex); //$NON-NLS-1$
		}
	}

	/**
	 * Creates the header panel and its content.
	 * 
	 * @param parent Parent control.
	 * 
	 * @return Header panel.
	 */
	private Composite createHeaderPanel(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new GridLayoutBuilder(composite, 2, false).setMargins(0).apply();
		
		labelExpression = new Label(composite, SWT.NONE);
		labelExpression.setText(Messages.ExpressionDialog_expression);
		labelExpression.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textExpression = new Text(composite, SWT.BORDER);
		textExpression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		textExpression.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				expressionChanged(textExpression.getText());
			}
		});
		
		return composite;
	}

	/**
	 * Creates the contents panel for the general tab.
	 * 
	 * @param parent Parent control.
	 * 
	 * @return General contents panel.
	 */
	private Composite createGeneralPanel(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new GridLayoutBuilder(composite, 2, false).apply();
		
		labelName = new Label(composite, SWT.NONE);
		labelName.setText(Messages.ExpressionDialog_name);
		labelName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textName = new Text(composite, SWT.BORDER);
		textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		textName.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				String text = textName.getText();
				workItem.setName(text.length() > 0 ? text : null);
			}
		});

		labelQuickExpression = new Label(composite, SWT.NONE);
		labelQuickExpression.setText(Messages.ExpressionDialog_quick_expression);
		labelQuickExpression.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textQuickExpression = new Text(composite, SWT.BORDER);
		textQuickExpression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		textQuickExpression.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				quickExpressionChanged(textQuickExpression.getText());
			}
		});
		
		labelUnlessExpression = new Label(composite, SWT.NONE);
		labelUnlessExpression.setText(Messages.ExpressionDialog_unless_expression);
		labelUnlessExpression.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textUnlessExpression = new Text(composite, SWT.BORDER);
		textUnlessExpression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		textUnlessExpression.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				unlessExpressionChanged(textUnlessExpression.getText());
			}
		});

		cbCaseSensitive = new Button(composite, SWT.CHECK);
		cbCaseSensitive.setText(Messages.ExpressionDialog_case_sensitive);
		cbCaseSensitive.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		cbCaseSensitive.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				workItem.setCaseInsensitive(!cbCaseSensitive.getSelection());
				refreshPreview();
			}
		});
		
		labelPatternHelp = new Link(composite, SWT.NONE);
		labelPatternHelp.setText("<a>" + Messages.ExpressionDialog_pattern_help + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		labelPatternHelp.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 2, 1));
		
		labelPatternHelp.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Program.launch(URL_PATTERN_HELP);
			}
		});

		Label labelDefaults = new Label(composite, SWT.NONE);
		labelDefaults.setText(Messages.ExpressionDialog_defaults);
		labelDefaults.setFont(headerFont);
		labelDefaults.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		Composite panelDefaults = new Composite(composite, SWT.NONE);
		new GridLayoutBuilder(panelDefaults, 4, false).setMargins(0).apply();
		panelDefaults.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		cbActiveByDefault = new Button(panelDefaults, SWT.CHECK);
		cbActiveByDefault.setText(Messages.ExpressionDialog_active_by_default);
		cbActiveByDefault.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		
		cbActiveByDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				workItem.setDefaultEnabled(cbActiveByDefault.getSelection());
			}
		});
		
		cbFilterDefault = new Button(panelDefaults, SWT.CHECK);
		cbFilterDefault.setText(Messages.ExpressionDialog_filter_default);
		cbFilterDefault.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		
		cbFilterDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				workItem.setDefaultFilter(cbFilterDefault.getSelection());
			}
		});
		
		cbStatisticsDefault = new Button(panelDefaults, SWT.CHECK);
		cbStatisticsDefault.setText(Messages.ExpressionDialog_statistics_default);
		cbStatisticsDefault.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		
		cbStatisticsDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				workItem.setDefaultStatistics(cbStatisticsDefault.getSelection());
			}
		});
		
		cbNotificationsDefault = new Button(panelDefaults, SWT.CHECK);
		cbNotificationsDefault.setText(Messages.ExpressionDialog_notifications);
		cbNotificationsDefault.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		
		cbNotificationsDefault.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				workItem.setDefaultNotifications(cbNotificationsDefault.getSelection());
			}
		});
		
		return composite;
	}

	/**
	 * Creates the contents panel for the Grep View tab.
	 * 
	 * @param parent Parent control.
	 * 
	 * @return Grep View contents panel.
	 */
	private Composite createGrepViewPanel(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		new GridLayoutBuilder(composite, 2, false).apply();
		
		labelRewrite = new Label(composite, SWT.NONE);
		labelRewrite.setText(Messages.ExpressionDialog_rewrite);
		labelRewrite.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textRewrite = new Text(composite, SWT.BORDER);
		textRewrite.setToolTipText(Messages.LinkPanel_pattern_tooltip_without_capture_group);
		textRewrite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		textRewrite.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				String text = textRewrite.getText();
				workItem.setRewriteExpression(text.trim().length() > 0 ? text : null);
				refreshGroups();
			}
		});
		
		return composite;
	}
	
	/**
	 * Creates the contents panel for the notifications tab.
	 * 
	 * @param parent Parent control.
	 * 
	 * @return Notifications contents panel.
	 */
	private Composite createNotificationsPanel(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		composite.setLayout(layout);
		
		compositePopupNotification = new Composite(composite, SWT.NONE);
		new GridLayoutBuilder(compositePopupNotification, 4, false).apply();
		
		compositeOtherNotifications = new Composite(composite, SWT.NONE);
		new GridLayoutBuilder(compositeOtherNotifications, 4, false).apply();
		
		labelPopupNotifications = new Label(compositePopupNotification, SWT.NONE);
		labelPopupNotifications.setText(Messages.ExpressionDialog_popup_notification);
		labelPopupNotifications.setFont(headerFont);
		labelPopupNotifications.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		cbPopupNotification = new Button(compositePopupNotification, SWT.CHECK);
		cbPopupNotification.setText(Messages.ExpressionDialog_enabled);
		cbPopupNotification.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		cbPopupNotification.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				workItem.setPopupNotification(cbPopupNotification.getSelection());
				refreshPreview();
			}
		});

		Label labelNotificationTitle = new Label(compositePopupNotification, SWT.NONE);
		labelNotificationTitle.setText(Messages.ExpressionDialog_title);
		labelNotificationTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		textNotificationTitle = new Text(compositePopupNotification, SWT.BORDER);
		textNotificationTitle.setToolTipText(Messages.LinkPanel_pattern_tooltip_with_capture_group);
		textNotificationTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		textNotificationTitle.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				String text = textNotificationTitle.getText().trim();
				workItem.setNotificationTitle(text.length() > 0 ? text : null);
			}
		});

		Label labelNotificationMessage = new Label(compositePopupNotification, SWT.NONE);
		labelNotificationMessage.setText(Messages.ExpressionDialog_message);
		labelNotificationMessage.setLayoutData(new GridData(SWT.FILL, SWT.END, false, false, 4, 1));
		
		textNotificationMessage = new Text(compositePopupNotification, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		textNotificationMessage.setToolTipText(Messages.LinkPanel_pattern_tooltip_with_capture_group);
		textNotificationMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		textNotificationMessage.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				String text = textNotificationMessage.getText().trim();
				workItem.setNotificationMessage(text.length() > 0 ? text : null);
			}
		});

		pickerNotificationLink = new LinkPickerLine(compositePopupNotification);
		pickerNotificationLink.setText(Messages.ExpressionDialog_link_label);
		
		pickerNotificationLink.addListener(new LinkPickerLine.ILinkChangeListener()
		{
			@Override
			public void linkChanged(LinkPickerLine source, IGrepLink link)
			{
				workItem.setNotificationLink(link);
			}
		});
		
		labelSoundNotifications = new Label(compositeOtherNotifications, SWT.NONE);
		labelSoundNotifications.setFont(headerFont);
		labelSoundNotifications.setText(Messages.ExpressionDialog_sound_notification);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		labelSoundNotifications.setLayoutData(gridData);
		
		cbSoundNotification = new Button(compositeOtherNotifications, SWT.CHECK);
		cbSoundNotification.setText(Messages.ExpressionDialog_sound);
		cbSoundNotification.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		cbSoundNotification.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				textSoundNotification.setEnabled(cbSoundNotification.getSelection());
				btnPlaySoundNotification.setEnabled(cbSoundNotification.getSelection());
				
				if(cbSoundNotification.getSelection())
				{
					if(workItem.getSoundNotificationPath() != null)
					{
						textSoundNotification.setText(workItem.getSoundNotificationPath());
					}
				}
				else
				{
					workItem.setSoundNotificationPath(null);
				}
			}
		});
		
		textSoundNotification = new Text(compositeOtherNotifications, SWT.BORDER);
		textSoundNotification.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		textSoundNotification.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				String text = textSoundNotification.getText().trim();
				workItem.setSoundNotificationPath(text.length() > 0 ? text : null);
			}
		});
		
		btnPlaySoundNotification = new Button(compositeOtherNotifications, SWT.PUSH);
		btnPlaySoundNotification.setToolTipText(Messages.ExpressionDialog_play);
		btnPlaySoundNotification.setImage(Activator.getDefault().getImageRegistry().get(Activator.IMG_PLAY));
		btnPlaySoundNotification.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		btnPlaySoundNotification.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String text = textSoundNotification.getText().trim();
				
				if(text.length() > 0)
				{
					playSound(text);
				}
			}
		});
		
		btnSoundNotification = new Button(compositeOtherNotifications, SWT.PUSH);
		btnSoundNotification.setText(Messages.ExpressionDialog_browse);
		btnSoundNotification.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		btnSoundNotification.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				selectSoundNotification();
			}
		});
		
		labelAutostartLink = new Label(compositeOtherNotifications, SWT.NONE);
		labelAutostartLink.setFont(headerFont);
		labelAutostartLink.setText(Messages.ExpressionDialog_autostart_action);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gridData.verticalIndent = 10;
		labelAutostartLink.setLayoutData(gridData);
		
		pickerAutostartLink = new LinkPickerLine(compositeOtherNotifications);
		pickerAutostartLink.setText(Messages.ExpressionDialog_action);
		
		pickerAutostartLink.addListener(new LinkPickerLine.ILinkChangeListener()
		{
			@Override
			public void linkChanged(LinkPickerLine source, IGrepLink link)
			{
				workItem.setAutostartLink(link);
			}
		});

		return composite;
	}

	/**
	 * Creates the contents panel for the statistics tab.
	 * 
	 * @param parent Parent control.
	 * 
	 * @return Statistics contents panel.
	 */
	private Composite createStatisticsPanel(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new GridLayoutBuilder(composite, 2, false).apply();
		
		labelStatisticsCountHeader = new Label(composite, SWT.NONE);
		labelStatisticsCountHeader.setFont(headerFont);
		labelStatisticsCountHeader.setText(Messages.ExpressionDialog_statistics_count_header);
		labelStatisticsCountHeader.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		labelStatisticsCountLabel = new Label(composite, SWT.NONE);
		labelStatisticsCountLabel.setText(Messages.ExpressionDialog_statistics_count_label);
		labelStatisticsCountLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textStatisticsCountLabel = new Text(composite, SWT.BORDER);
		textStatisticsCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		textStatisticsCountLabel.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				String text = textStatisticsCountLabel.getText().trim();
				workItem.setStatisticsCountLabel(text.length() > 0 ? text : null);
			}
		});
		
		labelStatisticsValueHeader = new Label(composite, SWT.NONE);
		labelStatisticsValueHeader.setFont(headerFont);
		labelStatisticsValueHeader.setText(Messages.ExpressionDialog_statistics_value_header);
		GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1);
		gridData.verticalIndent = 10;
		labelStatisticsValueHeader.setLayoutData(gridData);
		
		labelStatisticsValueLabel = new Label(composite, SWT.NONE);
		labelStatisticsValueLabel.setText(Messages.ExpressionDialog_statistics_value_label);
		labelStatisticsValueLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textStatisticsValueLabel = new Text(composite, SWT.BORDER);
		textStatisticsValueLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		textStatisticsValueLabel.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				String text = textStatisticsValueLabel.getText().trim();
				workItem.setStatisticsValueLabel(text.length() > 0 ? text : null);
			}
		});
		
		labelStatisticsValuePattern = new Label(composite, SWT.NONE);
		labelStatisticsValuePattern.setText(Messages.ExpressionDialog_statistics_value_pattern);
		labelStatisticsValuePattern.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		textStatisticsValuePattern = new Text(composite, SWT.BORDER);
		textStatisticsValuePattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textStatisticsValuePattern.setToolTipText(Messages.LinkPanel_pattern_tooltip_without_capture_group);
		
		textStatisticsValuePattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				String text = textStatisticsValuePattern.getText().trim();
				workItem.setStatisticsValuePattern(text.length() > 0 ? text : null);
			}
		});
		
		return composite;
	}
	
	/**
	 * Plays a sound. Displays an error message to the user if the sound could not
	 * be played.
	 * 
	 * @param path Sound file path.
	 */
	protected void playSound(String path)
	{
		String error = null;
		
		try
		{
			Activator.getDefault().getSoundManager().playSound(path);
		}
		catch(UnsupportedAudioFileException ex)
		{
			error = ex.getLocalizedMessage();
		}
		catch(IOException ex)
		{
			error = ex.getLocalizedMessage();
		}
		catch(LineUnavailableException ex)
		{
			error = ex.getLocalizedMessage();
		}
		
		if(error != null)
		{
			MessageDialog.openError(getShell(), Messages.ExpressionDialog_sound_error, MessageFormat.format(Messages.ExpressionDialog_could_not_play_sound, error));
		}
	}

	/**
	 * Shows a file dialogue to let the user select a notification sound file.
	 */
	protected void selectSoundNotification()
	{
		FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
		dlg.setText(Messages.ExpressionDialog_select_notification_sound);
		dlg.setFilterNames(new String[]{Messages.ExpressionDialog_audio_files, Messages.ExpressionDialog_all_files});
		dlg.setFilterExtensions(new String[]{"*.wav;*.aif;*.au", "*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
		
		String path = dlg.open();
		
		if(path != null)
		{
			path = path.trim();
			textSoundNotification.setText(path);
			workItem.setSoundNotificationPath(path);
			cbSoundNotification.setSelection(true);
			textSoundNotification.setEnabled(true);
			btnPlaySoundNotification.setEnabled(true);
		}
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings()
	{
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_SECTION);
		
		if(settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS_SECTION);
		}
		
		return settings;
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.view.DefaultSizeTitleAreaDialog#getDefaultSize()
	 */
	protected Point getDefaultSize()
	{
		Shell shell = getShell();
		return GrepConsoleUtil.charsToPixelDimensions(shell, 120, 30);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
	 */
	@Override
	protected void initializeBounds()
	{
		super.initializeBounds();
		
		Shell shell = getShell();
		shell.setMinimumSize(GrepConsoleUtil.charsToPixelDimensions(shell, 70, 25));
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
	 * Fills a style range's properties with those taken from the specified grep
	 * style.
	 * 
	 * @param style Style range.
	 * @param grepStyle Grep style source.
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
	 * Returns the first style in the specified selection, if any.
	 * 
	 * @param selection Selection containing styles. May be <code>null</code> or
	 * 		empty.
	 * 
	 * @return Style or <code>null</code>.
	 */
	protected GrepStyle getSelectedStyle(IStructuredSelection selection)
	{
		Object o = selection == null || selection.isEmpty() ? null : selection.getFirstElement();
		return (GrepStyle) (o instanceof GrepStyle ? o : null);
	}

	/**
	 * Sets the expression text to an escaped and quoted pattern derived from
	 * the specified text.
	 * 
	 * @param text Text.
	 */
	protected void setExpression(String text)
	{
		String expression = "(" + Pattern.quote(text) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		textExpression.setText(expression);
		textExpression.selectAll();
		expressionChanged(expression);
	}

	/**
	 * Sets the unless expression text to an escaped and quoted pattern derived
	 * from the specified text.
	 * 
	 * @param text Text.
	 */
	protected void setUnlessExpression(String text)
	{
		String expression = Pattern.quote(text);
		textUnlessExpression.setText(expression);
		textUnlessExpression.selectAll();
		unlessExpressionChanged(expression);
	}
	
	/**
	 * Updates the group list and preview when the expression pattern changes.
	 *  
	 * @param expression New expression pattern text.
	 */
	protected void expressionChanged(String expression)
	{
		workItem.setGrepExpression(expression);
		
		refreshGroups();
		refreshPreview();
	}

	/**
	 * Updates the preview when the quick expression pattern changes.
	 *  
	 * @param expression New quick expression pattern text.
	 */
	protected void quickExpressionChanged(String quickExpression)
	{
		String s = quickExpression.trim();
		workItem.setQuickGrepExpression(s.length() == 0 ? null : s);
		
		refreshPreview();
	}
	
	/**
	 * Updates the preview when the unless expression pattern changes.
	 *  
	 * @param unlessExpression New unless expression pattern text.
	 */
	protected void unlessExpressionChanged(String unlessExpression)
	{
		String s = unlessExpression.trim();
		workItem.setUnlessGrepExpression(s.length() == 0 ? null : s);
		
		refreshPreview();
	}

	/**
	 * Returns the specified group from the work item. Depending on the index,
	 * this may be regular group or a rewrite group.
	 * 
	 * @param index Group index.
	 * 
	 * @return Group.
	 */
	protected GrepGroup getGroup(Integer index)
	{
		if(index == null)
		{
			return null;
		}

		int styleGroupCount = workItem.getGroups().length;

		if(index < styleGroupCount)
		{
			return workItem.getGroups()[index];
		}
		else
		{
			return workItem.getRewriteGroups()[index - styleGroupCount];
		}
	}
	
	/**
	 * Updates the style selection when a group is selected.
	 * 
	 * @param index Group index.
	 */
	protected void groupSelected(Integer index)
	{
		GrepGroup group = getGroup(index);
		GrepStyle style = group == null ? null : group.getStyle();
		panelAvailableStyles.setSelection(style);
	}

	/**
	 * Assigns a style to the currently selected group.
	 * 
	 * @param style Style.
	 */
	protected void doAssignStyle(GrepStyle style)
	{
		Integer groupIndex = getSelectedGroup((IStructuredSelection) viewerStyleAssignments.getSelection());
		
		if(groupIndex != null)
		{
			GrepGroup group = getGroup(groupIndex);
			group.setStyle(style);
			refreshPreview();
			viewerStyleAssignments.refresh();
			refreshPreview();
		}
	}

	/**
	 * Returns the index of the first group in the selection.
	 * 
	 * @return Selection containing group indexes, or <code>null</code>.
	 */
	private Integer getSelectedGroup(IStructuredSelection selection)
	{
		if(selection == null)
		{
			return null;
		}
		else
		{
			return selection.isEmpty() ? null : (Integer) selection.getFirstElement();
		}
	}

	/**
	 * Refreshes the groups list.
	 */
	protected void refreshGroups()
	{
		String regex = textExpression.getText().trim();
		Pattern pattern = null;
		
		try
		{
			pattern = Pattern.compile(regex);
			textExpression.setForeground(null);
			textExpression.setToolTipText(null);
		}
		catch(PatternSyntaxException ex)
		{
			textExpression.setToolTipText(ex.getLocalizedMessage());
			textExpression.setForeground(textExpression.getDisplay().getSystemColor(SWT.COLOR_RED));
		}

		int groups = pattern == null ? 0 : pattern.matcher("").groupCount(); //$NON-NLS-1$
		groupArray = Arrays.copyOf(groupArray == null ? workItem.getGroups() : 
			groupArray, Math.max(groups + 1, groupArray == null ? 0 : groupArray.length));
		
		for(int i = 0; i < groupArray.length; i++)
		{
			if(i < workItem.getGroups().length)
			{
				GrepGroup group = workItem.getGroups()[i];
				groupArray[i] = group == null ? null : group.copy(true);
			}
			else
			{
				if(groupArray[i] == null)
				{
					groupArray[i] = new GrepGroup();
				}
			}
		}
		
		workItem.setGroups(Arrays.copyOf(groupArray, groups + 1));
		
		int rewriteGroups = workItem.getRewriteExpression() == null ? 0 : 1 + GrepConsoleUtil.countGroups(workItem.getRewriteExpression());
		rewriteGroupArray = Arrays.copyOf(rewriteGroupArray == null ? workItem.getRewriteGroups() : 
			rewriteGroupArray, Math.max(rewriteGroups, rewriteGroupArray == null ? 0 : rewriteGroupArray.length));
		
		for(int i = 0; i < rewriteGroupArray.length; i++)
		{
			if(i < workItem.getRewriteGroups().length)
			{
				GrepGroup group = workItem.getRewriteGroups()[i];
				rewriteGroupArray[i] = group == null ? null : group.copy(true);
			}
			else
			{
				if(rewriteGroupArray[i] == null)
				{
					rewriteGroupArray[i] = new GrepGroup();
				}
			}
		}
		
		workItem.setRewriteGroups(Arrays.copyOf(rewriteGroupArray, rewriteGroups));
		
		GrepGroup[] displayedGroups = new GrepGroup[workItem.getGroups().length + workItem.getRewriteGroups().length]; 
		System.arraycopy(workItem.getGroups(), 0, displayedGroups, 0, workItem.getGroups().length);
		System.arraycopy(workItem.getRewriteGroups(), 0, displayedGroups, workItem.getGroups().length, workItem.getRewriteGroups().length);
		
		groupLabelProvider.setCaptureGroupCount(workItem.getGroups().length);
		viewerStyleAssignments.setInput(displayedGroups);
		viewerStyleAssignments.refresh();
		
		if(panelAvailableStyles.getSelectedStyle() == null)
		{
			viewerStyleAssignments.setSelection(new StructuredSelection(Integer.valueOf(0)));
		}
	}

	/**
	 * Returns the item being edited.
	 * 
	 * @return Item.
	 */
	public GrepExpressionItem getItem()
	{
		return item;
	}

	/**
	 * Sets the item being edited. The item will only be modified when the user
	 * confirms the changes.
	 * 
	 * @param item Item.
	 */
	public void setItem(GrepExpressionItem item)
	{
		this.item = item;
		
		workItem = item.copy(true);
		backupStyles = new HashSet<GrepStyle>();
		
		for(GrepStyle style: item.getRoot().getStyles())
		{
			backupStyles.add(style.copy(true));
		}
		
		if(textName != null)
		{
			refresh();
		}
		
		if(panelAvailableStyles != null)
		{
			panelAvailableStyles.setRoot(workItem.getRoot());
		}
	}
	
	/**
	 * Checks whether multiple groups have the same name and updates the OK button
	 * state if necessary.
	 */
	private void updateDuplicateGroupNames()
	{
		Set<String> names = new HashSet<String>();
		boolean duplicates = false;
		
		for(GrepGroup group: workItem.getGroups())
		{
			String name = group.getName();
			
			if(name == null)
			{
				continue;
			}
			
			name = name.toLowerCase();
			
			if(names.contains(name))
			{
				duplicates = true;
				break;
			}
			
			names.add(name);
		}
		
		if(duplicates != duplicateGroupNames)
		{
			duplicateGroupNames = duplicates;
			refresh();
		}
	}
	
	/**
	 * Refreshes GUI contents according to the current item.
	 */
	private void refresh()
	{
		textName.setText(item == null || item.getName() == null ? "" : item.getName()); //$NON-NLS-1$
		textQuickExpression.setText(item == null || item.getQuickGrepExpression() == null ? "" : item.getQuickGrepExpression()); //$NON-NLS-1$
		textExpression.setText(item == null || item.getGrepExpression() == null ? "" : item.getGrepExpression()); //$NON-NLS-1$
		textUnlessExpression.setText(item == null || item.getUnlessGrepExpression() == null ? "" : item.getUnlessGrepExpression()); //$NON-NLS-1$
		textRewrite.setText(item == null || item.getRewriteExpression() == null ? "" : item.getRewriteExpression()); //$NON-NLS-1$
		cbActiveByDefault.setSelection(workItem.isDefaultEnabled());
		cbFilterDefault.setSelection(workItem.isDefaultFilter());
		cbStatisticsDefault.setSelection(workItem.isDefaultStatistics());
		cbNotificationsDefault.setSelection(workItem.isDefaultNotifications());
		cbCaseSensitive.setSelection(!workItem.isCaseInsensitive());
		cbRemoveOriginalStyle.setSelection(workItem.isRemoveOriginalStyle());
		stPreview.setText(previewText == null ? "" : previewText); //$NON-NLS-1$
		
		cbPopupNotification.setSelection(workItem.isPopupNotification());
		textNotificationTitle.setText(workItem.getNotificationTitle() == null ? "" : workItem.getNotificationTitle()); //$NON-NLS-1$
		textNotificationMessage.setText(workItem.getNotificationMessage() == null ? "" : workItem.getNotificationMessage()); //$NON-NLS-1$
		
		cbSoundNotification.setSelection(workItem.getSoundNotificationPath() != null && workItem.getSoundNotificationPath().length() > 0);
		textSoundNotification.setText(workItem.getSoundNotificationPath() == null ? "" : workItem.getSoundNotificationPath()); //$NON-NLS-1$
		textSoundNotification.setEnabled(cbSoundNotification.getSelection());
		btnPlaySoundNotification.setEnabled(cbSoundNotification.getSelection());
		
		pickerNotificationLink.setLink(item == null ? null : item.getNotificationLink());
		pickerAutostartLink.setLink(item == null ? null : item.getAutostartLink());

		textStatisticsCountLabel.setText(item == null || item.getStatisticsCountLabel() == null ? "" : item.getStatisticsCountLabel()); //$NON-NLS-1$
		textStatisticsValueLabel.setText(item == null || item.getStatisticsValueLabel() == null ? "" : item.getStatisticsValueLabel()); //$NON-NLS-1$
		textStatisticsValuePattern.setText(item == null || item.getStatisticsValuePattern() == null ? "" : item.getStatisticsValuePattern()); //$NON-NLS-1$

		textExpression.selectAll();
		textUnlessExpression.selectAll();
		
		getButton(OK).setEnabled(!duplicateGroupNames);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close()
	{
		if(getReturnCode() != OK)
		{
			item.getRoot().setStyles(backupStyles);
		}
		
		return super.close();
	}
	
	/**
	 * Updates the current item when the user clicks the "ok" button.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
		Pattern pattern = Pattern.compile(workItem.getGrepExpression());
		int groups = pattern.matcher("").groupCount(); //$NON-NLS-1$
		workItem.setGroups(Arrays.copyOf(workItem.getGroups(), groups + 1));
		item.copyFrom(workItem, true);
		
		super.okPressed();
	}
	
	/**
	 * Sets the preview text.
	 * 
	 * @param text Preview text.
	 */
	public void setPreviewText(String text)
	{
		previewText = text;
		
		if(stPreview != null)
		{
			stPreview.setText(text);
		}
	}
	
	/**
	 * Returns the preview text.
	 * 
	 * @return Preview text.
	 */
	public String getPreviewText()
	{
		return previewText;
	}

	/**
	 * Refreshes the preview text and line style listener.
	 */
	protected void refreshPreview()
	{
		grepLineStyleListener.setItems(Collections.singleton(workItem));
		stPreview.redraw();
	}

	/**
	 * Assigns a newly created style to the currently selected group.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel.IStylesListener#onNewStyle(name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel, name.schedenig.eclipse.grepconsole.model.GrepStyle)
	 */
	@Override
	public void onNewStyle(StylesPanel panel, GrepStyle style)
	{
		doAssignStyle(style);
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel.IStylesListener#onStyleDeleted(name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel, name.schedenig.eclipse.grepconsole.model.GrepStyle)
	 */
	@Override
	public void onStyleDeleted(StylesPanel panel, GrepStyle style)
	{
	}

	/**
	 * Updates the style assignments table and preview panel when a style has
	 * been edited.
	 *  
	 * @see name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel.IStylesListener#onStyleChanged(name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel, name.schedenig.eclipse.grepconsole.model.GrepStyle)
	 */
	@Override
	public void onStyleChanged(StylesPanel panel, GrepStyle style)
	{
		viewerStyleAssignments.refresh();
		refreshPreview();
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel.IStylesListener#onStyleSelected(name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel, name.schedenig.eclipse.grepconsole.model.GrepStyle)
	 */
	@Override
	public void onStyleSelected(StylesPanel panel, GrepStyle style)
	{
	}

	/**
	 * Assigns the double clicked style to the selected group.
	 * 
	 * @see name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel.IStylesListener#onStyleDoubleClicked(name.schedenig.eclipse.grepconsole.view.styles.NewStylesPanel, name.schedenig.eclipse.grepconsole.model.GrepStyle)
	 */
	@Override
	public boolean onStyleDoubleClicked(StylesPanel panel, GrepStyle style)
	{
		doAssignStyle(style);
		return true;
	}
}
