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

import java.text.MessageFormat;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.links.JavaLink;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Adapter for Java links.
 * 
 * @author msched
 */
public class JavaLinkAdapter extends GrepLinkAdapter
{
	/** Resolved type name. */
	private String typeName;
	
	/** Resolved line number (if any). */
	private Integer lineNumber;
	
	/** Eclipse project. */
	private IProject project;
	
	/** Resolved offset (if any). */
	private Integer offset;

	/** Tooltip text. */
	private String toolTipText;

	/**
	 * Creates a new instance.
	 * 
	 * @param match Link match. Must have a CommandLink assigned.
	 * @param shell Shell.
	 * @param project Eclipse project from which the Java type will be opened.
	 */
	public JavaLinkAdapter(LinkMatch match, Shell shell, IProject project)
	{
		super(match, shell);
		
		this.project = project;
	}

	/**
	 * Calculates the adapter's fields (if it hasn't been initialized before).
	 */
	private void init()
	{
		if(typeName != null || toolTipText != null)
		{
			return;
		}
	
		JavaLink link = getLink();
		
		typeName = replaceParams(link.getTypePattern());
		
		lineNumber = readOptionalIntPattern(link.getLineNumberPattern());
		offset = readOptionalIntPattern(link.getOffsetPattern());
		
		toolTipText = typeName + (lineNumber == null ? "" : " (" + lineNumber + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#getLink()
	 */
	@Override
	public JavaLink getLink()
	{
		return (JavaLink) super.getLink();
	}
	
	/**
	 * @see name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter#openLink()
	 */
	@Override
	public void openLink()
	{
		init();
		
		if(project == null)
		{
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, Messages.JavaLinkAdapter_no_project_selected);
			return;
		}
		
		IJavaProject javaProject;
		
		try
		{
			javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		}
		catch(CoreException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, MessageFormat.format(Messages.JavaLinkAdapter_not_a_java_project_name, project.getName()), ex);
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.JavaLinkAdapter_not_a_java_project_name_message, project.getName(), ex.getMessage()));
			return;
		}
		
		if(javaProject == null)
		{
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.JavaLinkAdapter_not_a_java_project_name, project.getName()));
			return;
		}
		
		IType type;
		
		try
		{
			type = javaProject.findType(typeName);
		}
		catch(JavaModelException ex)
		{
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.JavaLinkAdapter_could_not_find_type_in_project_name_message, typeName, ex.getLocalizedMessage()));
			return;
		}
		
		if(type == null)
		{
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.JavaLinkAdapter_could_not_find_type_in_project_name, typeName));
			return;
		}
		
		try
		{
			IEditorPart editor = JavaUI.openInEditor(type);

			if(editor instanceof ITextEditor && lineNumber != null)
			{
				GrepConsoleUtil.jumpToEditorLine((ITextEditor) editor, lineNumber, offset);
			}
			
			getShell().forceActive();
		}
		catch(PartInitException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.JavaLinkAdapter_could_not_open_editor, ex);
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.JavaLinkAdapter_could_not_open_editor_message, ex.getLocalizedMessage()));
		}
		catch(JavaModelException ex)
		{
			Activator.getDefault().log(IStatus.ERROR, Messages.JavaLinkAdapter_could_not_open_editor, ex);
			MessageDialog.openError(getShell(), Messages.LinkAdapter_link_error, MessageFormat.format(Messages.JavaLinkAdapter_could_not_open_editor_message, ex.getLocalizedMessage()));
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
