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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;
import name.schedenig.eclipse.grepconsole.model.xml.XmlHandler;
import name.schedenig.eclipse.grepconsole.model.xml.XmlHandlerException;
import name.schedenig.eclipse.grepconsole.view.items.links.LinkDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

/**
 * Combines a label, a read only text field displaying action details, and an
 * edit button into an action picker line widget.
 * 
 * @author msched
 */
public class LinkPickerLine
{
	/**
	 * Interface for action change listeners.
	 * 
	 * @author msched
	 */
	public static interface ILinkChangeListener
	{
		/**
		 * Called when the selected action has changed.
		 * 
		 * @param source Source action picker.
		 * @param color Selected action.
		 */
		public void linkChanged(LinkPickerLine source, IGrepLink link);
	}

	/** Set of listeners. */
	private LinkedHashSet<ILinkChangeListener> listeners = new LinkedHashSet<LinkPickerLine.ILinkChangeListener>();

	/** Current action. */
	private IGrepLink link;
	
	/** GUI variables. */
	private Label label;
	private Text text;
	private Menu menu;
	private MenuItem miCopy;
	private MenuItem miPaste;
	private Clipboard clipboard;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent control.
	 */
	public LinkPickerLine(Composite parent)
	{
		init(parent);
	}

	/**
	 * Creates the GUI.
	 * 
	 * @param parent Parent control. 
	 */
	private void init(final Composite parent)
	{
		parent.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				dispose();
			}
		});
		
		clipboard = new Clipboard(parent.getDisplay());
		
		label = new Label(parent, SWT.NONE);
		label.setText(""); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		text = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		text.setText(""); //$NON-NLS-1$
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Button btnNotificationLink = new Button(parent, SWT.PUSH);
		btnNotificationLink.setText(Messages.LinkPickerLine_edit);
		btnNotificationLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		btnNotificationLink.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				LinkDialog dlg = new LinkDialog(parent.getShell(), false);
				dlg.setLink(link);
				
				if(dlg.open() == LinkDialog.OK)
				{
					link = dlg.getLink();
					refreshLinkText();
					fireLinkChanged(link);
				}
			}
		});
		
		menu = new Menu(text);
		
		miCopy = new MenuItem(menu, SWT.PUSH);
		miCopy.setText(Messages.ExpressionDialog_copy_link);
		miCopy.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				copyLink(link);
			}
		});
		
		miPaste = new MenuItem(menu, SWT.PUSH);
		miPaste.setText(Messages.ExpressionDialog_paste_link);
		miPaste.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IGrepLink newLink = getLinkFromClipboard();
				
				if(newLink != null)
				{
					link = newLink;
					refreshLinkText();
					fireLinkChanged(link);
				}
			}
		});
		
		menu.addMenuListener(new MenuAdapter()
		{
			@Override
			public void menuShown(MenuEvent e)
			{
				miCopy.setEnabled(link != null);
			}
		});
		
		text.setMenu(menu);
	}

	/**
	 * Disposes resources.
	 */
	protected void dispose()
	{
		if(clipboard != null)
		{
			clipboard.dispose();
			clipboard = null;
		}
	}

	/**
	 * Copies an action to the clipboard.
	 * 
	 * @param link Action. 
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
	 * Reads an action from the clipboard.
	 * 
	 * @return Action, or <code>null</code> if none could be read from the
	 * clipboard.
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
	 * Updates the text element displaying the action's details.
	 */
	private void refreshLinkText()
	{
		text.setText(StyleAssignmentsLinkLabelProvider.getLinkDisplayText(link, true));
	}

	/**
	 * Fires the action changed event.
	 * 
	 * @param link Current action.
	 */
	private void fireLinkChanged(IGrepLink link)
	{
		for(ILinkChangeListener listener: listeners)
		{
			listener.linkChanged(this, link);
		}
	}

	/**
	 * Adds a listener.
	 * 
	 * @param listener Listener.
	 */
	public void addListener(ILinkChangeListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes a listener.
	 * 
	 * @param listener Listener.
	 */
	public void removeListener(ILinkChangeListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Sets the label text.
	 * 
	 * @param text New label text.
	 */
	public void setText(String text)
	{
		label.setText(text);
	}
	
	/**
	 * Returns the label text.
	 * 
	 * @return Label text.
	 */
	public String getText()
	{
		return label.getText();
	}

	/**
	 * Returns the current action.
	 * 
	 * @return Action.
	 */
	public IGrepLink getLink()
	{
		return link;
	}

	/**
	 * Sets the current action.
	 * 
	 * @param link Action.
	 */
	public void setLink(IGrepLink link)
	{
		this.link = link;
		refreshLinkText();
	}
}
