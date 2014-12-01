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

import java.util.Map;

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionsWithSelection;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.model.InheritedFlagResolver;
import name.schedenig.eclipse.grepconsole.model.InheritedFlagResolver.InheritedFlag;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * A label provider for model trees. Displays style thumbnails and item labels.
 * 
 * @author msched
 */
public class ItemLabelProvider extends OwnerDrawLabelProvider implements ILabelProvider
{
	/** Space (in pixels) between thumbnail and label. */
	private static final int SPACE = 4;

	/** RGB value for disabled labels. */
	private static final RGB RGB_DISABLED = new RGB(160, 160, 160);
	
	/** Viewer to which the provider is bound. */
	private ColumnViewer viewer;
	
	/** Style image registry for thumbnail images. */
	private StyleImageRegistry styleImageRegistry = new StyleImageRegistry();
	
	/** Colour registry for label colours. */
	private ColorRegistry colorRegistry = new ColorRegistry(Activator.getDefault().getColorRegistry());
	
	/**
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	@Override
	public void dispose()
	{
		super.dispose();
		
		styleImageRegistry.dispose();
		colorRegistry.disposeColors();
	}

	/**
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
	 */
	@Override
	protected void initialize(ColumnViewer viewer, ViewerColumn column)
	{
		super.initialize(viewer, column);
		
		this.viewer = viewer;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element)
	{
		AbstractGrepModelElement grepElement = (AbstractGrepModelElement) element;
		
		String text = null;
		
		if((grepElement instanceof GrepExpressionItem) && (grepElement.getName() == null))
		{
			text = ((GrepExpressionItem) grepElement).getGrepExpression();
		}
		
		if(text == null)
		{
			text = grepElement.getName();
		}
		
		return text == null ? "" : text; //$NON-NLS-1$
	}

	/**
	 * Returns the font to use for a specific element.
	 * 
	 * For groups, a bold version of the default font is returned. For all other
	 * items, <code>null</code> is returned.
	 * 
	 * @param element Element.
	 * 
	 * @return Font, or <code>null</code>.
	 */
	public Font getFont(Object element)
	{
		if(element instanceof GrepExpressionFolder)
		{
			return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
		}
		else
		{
			return JFaceResources.getDialogFont();
		}
	}

	/**
	 * For groups, no image is returned. For items, style thumbnail images are
	 * created on demand.
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element)
	{
		if(!(element instanceof GrepExpressionItem))
		{
			return null;
		}

		GrepExpressionItem item = (GrepExpressionItem) element;

		if(item.getGroups() == null || item.getGroups().length == 0)
		{
			return null;
		}
		
		GrepStyle style = null;
		
		for(GrepGroup g: item.getGroups())
		{
			GrepStyle s = g.getStyle();
			
			if(s != null)
			{
				style = s;
				break;
			}
		}
		
		RGB foreground = style == null ? null : style.getForeground();
		RGB background = style == null ? null : style.getBackground();
		
		return styleImageRegistry.getColorImage(new StyleImageRegistry.RgbPair(foreground, background));
	}

	/**
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event, java.lang.Object)
	 */
	@Override
	protected void measure(Event event, Object element)
	{
		GC gc = event.gc;
		Image image = getImage(element);
		String text = getText(element);
		
		Font oldFont = gc.getFont();
		Font font = getFont(element);
		
		if(font == null)
		{
			font = JFaceResources.getFont(JFaceResources.DEFAULT_FONT);
		}

		gc.setFont(font);
		Point size = gc.textExtent(text);
		gc.setFont(oldFont);
		
		if(image != null)
		{
			Rectangle imageBounds = image.getBounds();
			
			size.x += imageBounds.width + SPACE;
			size.y = Math.max(size.y, imageBounds.height);
		}
		
		event.setBounds(new Rectangle(event.x, event.y, size.x, size.y));
	}

	/**
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#erase(org.eclipse.swt.widgets.Event, java.lang.Object)
	 */
	@Override
	protected void erase(Event event, Object element)
	{
	}
	
	/**
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#paint(org.eclipse.swt.widgets.Event, java.lang.Object)
	 */
	@Override
	protected void paint(Event event, Object element)
	{
		Image image = getImage(element);
		String text = getText(element);
		
		boolean enabled = isElementEnabled((AbstractGrepModelElement) element);
		boolean selected = (event.detail & SWT.SELECTED) != 0;
		Color color;
		
		if(enabled)
		{
			Display display = viewer.getControl().getDisplay();
			color = selected ? display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT) : display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		}
		else
		{
			color = colorRegistry.get(RGB_DISABLED);
		}
		
		int x = event.x;
		
		if(image != null)
		{
			Rectangle imageBounds = image.getBounds();
			event.gc.drawImage(image, event.x, event.y + (event.height - imageBounds.height) / 2);
			int imageWidth = imageBounds.width + SPACE;
			x += imageWidth;
		}
		
		Device device = event.gc.getDevice();
		TextLayout layout = new TextLayout(device);

		try
		{
			Font font = getFont(element);
			
			if(font == null)
			{
				font = JFaceResources.getFont(JFaceResources.DEFAULT_FONT);
			}
			
			TextStyle style = new TextStyle(font, color, null);
			
			layout.setText(text);
			layout.setStyle(style, 0, text.length());
			layout.draw(event.gc, x, event.y + (event.height - layout.getBounds().height) / 2);
		}
		finally
		{
			layout.dispose();
		}
	}
	
	/**
	 * Determines whether an element is enabled by reverse iterating through
	 * its parents in the model tree.
	 * 
	 * Uses the enablement map, if available, and the elements' default values.
	 * 
	 * @param element Element.
	 * 
	 * @return Whether the element is enabled.
	 */
	public boolean isElementEnabled(AbstractGrepModelElement element)
	{
		GrepExpressionFolder parent = element.getParent();
		
		if(parent != null)
		{
			if(!isElementEnabled(element.getParent()))
			{
				return false;
			}
		}
		
		Map<String, Boolean> enablementMap = getEnablementMap();
		Boolean enabled;
		
		if(enablementMap == null)
		{
			enabled = null;
		}
		else
		{
			enabled = enablementMap.get(element.getId());
		}
		
		if(enabled == null)
		{
			enabled = element.isDefaultEnabled();
		}
		
		return enabled;
	}
	
	/**
	 * Returns the enablement map, if the viewer's input is a
	 * GrepExpressionWithSelection instance.
	 * 
	 * @return Enablement map, or <code>null</code>.
	 */
	private Map<String, Boolean> getEnablementMap()
	{
		Object input = viewer.getInput();
		
		if(input instanceof GrepExpressionsWithSelection)
		{
			return ((GrepExpressionsWithSelection) viewer.getInput()).getEnablementMap();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the filter map, if the viewer's input is a
	 * GrepExpressionWithSelection instance.
	 * 
	 * @return Filter map, or <code>null</code>.
	 */
	private Map<String, Boolean> getFilterMap()
	{
		Object input = viewer.getInput();
		
		if(input instanceof GrepExpressionsWithSelection)
		{
			return ((GrepExpressionsWithSelection) viewer.getInput()).getFilterMap();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the statistics map, if the viewer's input is a
	 * GrepExpressionWithSelection instance.
	 * 
	 * @return Statistics map, or <code>null</code>.
	 */
	private Map<String, Boolean> getStatisticsMap()
	{
		Object input = viewer.getInput();
		
		if(input instanceof GrepExpressionsWithSelection)
		{
			return ((GrepExpressionsWithSelection) viewer.getInput()).getStatisticsMap();
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Returns the notifications map, if the viewer's input is a
	 * GrepExpressionWithSelection instance.
	 * 
	 * @return Filter map, or <code>null</code>.
	 */
	private Map<String, Boolean> getNotificationsMap()
	{
		Object input = viewer.getInput();
		
		if(input instanceof GrepExpressionsWithSelection)
		{
			return ((GrepExpressionsWithSelection) viewer.getInput()).getNotificationsMap();
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element)
	{
		String name = getText(element);
		AbstractGrepModelElement me = (AbstractGrepModelElement) element;
		InheritedFlag enabled = InheritedFlagResolver.ENABLED_RESOLVER.getFlag(me, getEnablementMap());
		InheritedFlag filtered = InheritedFlagResolver.FILTER_RESOLVER.getFlag(me, getFilterMap());
		InheritedFlag statistics = InheritedFlagResolver.STATISTICS_RESOLVER.getFlag(me, getStatisticsMap());
		InheritedFlag notifications = InheritedFlagResolver.NOTIFICATIONS_RESOLVER.getFlag(me, getNotificationsMap());
		
		String enabledText = (enabled.isSet() ? Messages.ItemLabelProvider_styled : Messages.ItemLabelProvider_unstyled) + (enabled.isInherited() ? " " + Messages.ItemLabelProvider_inherited : ""); //$NON-NLS-1$ //$NON-NLS-2$
		String filteredText = (filtered.isSet() ? Messages.ItemLabelProvider_filtered : Messages.ItemLabelProvider_unfiltered) + (filtered.isInherited() ? " " + Messages.ItemLabelProvider_inherited : ""); //$NON-NLS-1$ //$NON-NLS-2$
		String statisticsText = (statistics.isSet() ? Messages.ItemLabelProvider_statistics : Messages.ItemLabelProvider_no_statistics) + (filtered.isInherited() ? " " + Messages.ItemLabelProvider_inherited : ""); //$NON-NLS-1$ //$NON-NLS-2$
		String notificationsText = (notifications.isSet() ? Messages.ItemLabelProvider_notifications : Messages.ItemLabelProvider_no_notifications) + (notifications.isInherited() ? " " + Messages.ItemLabelProvider_inherited : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		String s = name + " - " + enabledText + ", " + filteredText + ", " + statisticsText + "," + notificationsText + "."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		return s;
	}
}
