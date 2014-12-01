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

import java.io.File;
import java.text.MessageFormat;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.FileLink;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Adapter for file links.
 * 
 * @author msched
 */
public class FileLinkAdapter extends GrepLinkAdapter
{
	/** Default base directory pattern. */
	public static final String DEFAULT_BASE_DIR = "{eclipse:PROJECT_LOC}"; //$NON-NLS-1$
	
	/** Resolved file. */
	private File file;
	
	/** Resolved line number (if any). */
	private Integer lineNumber;
	
	/** Resolved offset (if any). */
	private Integer offset;

	/** Tooltip text. */
	private String toolTipText;

	/**
	 * Creates a new instance.
	 * 
	 * @param match Link match. Must have a FileLink assigned.
	 * @param shell Shell.
	 */
	public FileLinkAdapter(LinkMatch match, Shell shell)
	{
		super(match, shell);
	}

	/**
	 * Calculates the adapter's fields (if it hasn't been initialized before).
	 */
	private void init()
	{
		if(file != null || toolTipText != null)
		{
			return;
		}
		
		FileLink link = getLink();
		
		String s = replaceParams(link.getFilePattern());
		s = stripUrl(s);
		file = new File(s);
		
		if(!file.isAbsolute() && !s.startsWith("file:/")) //$NON-NLS-1$
		{
			s = link.getBaseDirPattern();
			
			if(s == null || s.trim().length() == 0)
			{
				s = DEFAULT_BASE_DIR;
			}
			else
			{
				s = s.trim();
			}
			
			s = replaceParams(s);
			s = stripUrl(s);
			
			File baseDir = new File(s);
			file = new File(baseDir, file.getPath());
		}
		
		lineNumber = readOptionalIntPattern(link.getLineNumberPattern());
		offset = readOptionalIntPattern(link.getOffsetPattern());
		
		toolTipText = this.file.toString() + (lineNumber == null ? "" : " (" + lineNumber + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Strips the scheme and potential superfluous opening slashes from the string
	 * if it is a URI string.
	 * 
	 * @param s Path or URI string.
	 * 
	 * @return Path string.
	 */
	private String stripUrl(String s)
	{
		if(s == null)
		{
			return null;
		}
		
		if(s.startsWith("file:")) //$NON-NLS-1$
		{
			s = s.substring(5);
			
			if(s.startsWith("///")) //$NON-NLS-1$
			{
				s = s.substring(2);
			}
		}
		
		return s;
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#getLink()
	 */
	@Override
	public FileLink getLink()
	{
		return (FileLink) super.getLink();
	}

	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#openLink()
	 */
	@Override
	public void openLink()
	{
		init();

		if(file.exists() && file.isFile())
		{
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			try
			{
				IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);

				if(editor instanceof ITextEditor && lineNumber != null)
				{
					GrepConsoleUtil.jumpToEditorLine((ITextEditor) editor, lineNumber, offset);
				}
				
				getShell().forceActive();
			}
			catch(PartInitException ex)
			{
				Activator.getDefault().log(IStatus.ERROR, Messages.FileLinkAdapter_could_not_open_editor, ex);
				MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, 
						MessageFormat.format(Messages.FileLinkAdapter_could_not_open_editor_with_msg, ex.getLocalizedMessage()));
			}
		}
		else
		{
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error,
					MessageFormat.format(Messages.FileLinkAdapter_file_does_not_exist, file));
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
