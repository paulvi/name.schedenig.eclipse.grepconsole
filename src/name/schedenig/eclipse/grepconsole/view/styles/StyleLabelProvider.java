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
import java.util.LinkedHashSet;
import java.util.Set;

import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.view.items.StyleImageRegistry;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

/**
 * Provides labels and thumbnail images for styles. Also provides tool tip texts
 * showing style usage. 
 * 
 * @author msched
 */
public class StyleLabelProvider extends ColumnLabelProvider
{
	/** Label to display for unnamed styles. */
	public static final String LABEL_UNNAMED = Messages.StyleLabelProvider_unnamed;
	
	/** Image registry catching thumbnail images. */
	private StyleImageRegistry styleImageRegistry = new StyleImageRegistry();

	/** Viewer for which this provider is bound. */
	private ColumnViewer viewer;

	/**
	 * Creats a new instance.
	 * 
	 * @param viewer Viewer to which the new instance will be bound.
	 */
	public StyleLabelProvider(ColumnViewer viewer)
	{
		this.viewer = viewer;
	}
	
	/**
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	@Override
	public void dispose()
	{
		super.dispose();
		
		styleImageRegistry.dispose();
	}
	
	/**
	 * Returns the view to which the provider is bound.
	 * 
	 * @return Viewer.
	 */
	public ColumnViewer getViewer()
	{
		return viewer;
	}
	
	/**
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element)
	{
		if(element == null)
		{
			return ""; //$NON-NLS-1$
		}
		else if(element == StyleContentProvider.NULL_ELEMENT)
		{
			return ""; //$NON-NLS-1$
		}
		else
		{
			String name = ((GrepStyle) element).getName();
			return name == null ? LABEL_UNNAMED : name;
		}
	}
	
	/**
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element)
	{
		if(element == null || element == StyleContentProvider.NULL_ELEMENT)
		{
			return null;
		}
		
		GrepStyle style = (GrepStyle) element;
		
		RGB foreground = style.getForeground();
		RGB background = style.getBackground();
		
		return styleImageRegistry.getColorImage(new StyleImageRegistry.RgbPair(foreground, background));
	}
	
	/**
	 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element)
	{
		if(!(element instanceof GrepStyle))
		{
			return null;
		}
		
		GrepExpressionRootFolder root = (GrepExpressionRootFolder) viewer.getInput();
		Set<GrepExpressionItem> items = new LinkedHashSet<GrepExpressionItem>();
		root.findStyleUses((GrepStyle) element, items);
		
		if(items.isEmpty())
		{
			return Messages.StyleLabelProvider_this_style_is_unused;
		}
		else
		{
			StringBuilder sb = null;
			
			for(GrepExpressionItem item: items)
			{
				if(sb == null)
				{
					sb = new StringBuilder();
				}
				else
				{
					sb.append(", "); //$NON-NLS-1$
				}
				
				String name = item.getName();
				
				if(name == null || name.length() == 0)
				{
					name = item.getGrepExpression();
				}
				
				sb.append(name);
			}
			return MessageFormat.format(Messages.StyleLabelProvider_used_by_0, sb);
		}
	}
}
