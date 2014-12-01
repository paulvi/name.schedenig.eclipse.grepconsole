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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.UrlLink;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

/**
 * Adapter for URL links.
 * 
 * @author msched
 */
public class UrlLinkAdapter extends GrepLinkAdapter
{
	/** Resolved URL. */
	private URL url;
	
	/** Tooltip text. */
	private String toolTipText;

	/**
	 * Creates a new instance.
	 * 
	 * @param match Link match. Must have a UrlLink assigned.
	 * @param shell Shell.
	 */
	public UrlLinkAdapter(LinkMatch match, Shell shell)
	{
		super(match, shell);
	}
	
	/**
	 * Calculates the adapter's fields (if it hasn't been initialised before).
	 */
	private void init()
	{
		if(url != null || toolTipText != null)
		{
			return;
		}
		
		String s = replaceParams(getLink().getUrlPattern());
		
		try
		{
			URI uri = new URI(s);
			
			if(!uri.isAbsolute())
			{
				uri = new URI("http://" + s); //$NON-NLS-1$
			}
			
			this.url = uri.toURL();
			toolTipText = this.url.toString() + (getLink().isExternal() ? Messages.UrlLinkAdapter_external : ""); //$NON-NLS-1$
		}
		catch(MalformedURLException ex)
		{
			this.url = null;
			toolTipText = Messages.UrlLinkAdapter_invalid_url + s;
		}
		catch(URISyntaxException ex)
		{
			this.url = null;
			toolTipText = Messages.UrlLinkAdapter_invalid_url + s;
		}
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#getLink()
	 */
	@Override
	public UrlLink getLink()
	{
		return (UrlLink) super.getLink();
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#openLink()
	 */
	@Override
	public void openLink()
	{
		init();
		
		if(getLink().isExternal())
		{
			try
			{
				Program.launch(url.toString());
			}
			catch(UnsatisfiedLinkError ex)
			{
				Activator.getDefault().log(IStatus.ERROR, Messages.UrlLinkAdapter_could_not_open_link, ex);
				MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.UrlLinkAdapter_could_not_open_link_message, ex.getLocalizedMessage()));
			}
		}
		else
		{
			try
			{
				IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null);
				browser.openURL(url);
				getShell().forceActive();
			}
			catch(PartInitException ex)
			{
				Activator.getDefault().log(IStatus.ERROR, Messages.UrlLinkAdapter_could_not_open_link, ex);
				MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.UrlLinkAdapter_could_not_open_link_message, ex.getLocalizedMessage()));
			}
		}
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#getToolTipText()
	 */
	@Override
	public String getToolTipText()
	{
		init();
		return toolTipText;
	}
}
