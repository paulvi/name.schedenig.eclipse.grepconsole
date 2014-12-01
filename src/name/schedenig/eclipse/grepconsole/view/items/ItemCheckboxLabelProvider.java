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
import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;
import name.schedenig.eclipse.grepconsole.model.InheritedFlagResolver;
import name.schedenig.eclipse.grepconsole.model.InheritedFlagResolver.InheritedFlag;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Abstract base class for image based checkbox (two state or tristate) label
 * providers.
 * 
 * Implementations must provide methods to determine the state of an item and
 * the image key (in the Activator's image registry) for each state.
 * 
 * @author msched
 */
public abstract class ItemCheckboxLabelProvider extends ColumnLabelProvider
{
	/**
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element)
	{
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element)
	{
		AbstractGrepModelElement modelElement = (AbstractGrepModelElement) element;
		InheritedFlag flag = getFlagResolver().getFlag(modelElement, getInheritanceMap());
		String key = getImageKey(flag.isSet(), flag.isInherited(), flag.isGreyed());
		
		return Activator.getDefault().getImageRegistry().get(key);
	}

	/**
	 * Returns the inheritance map, if one is available. The map is used to
	 * determine element state that overrides the element's default state.
	 * 
	 * @return Inheritance map, or <code>null</code>.
	 */
	protected abstract Map<String, Boolean> getInheritanceMap();

	/**
	 * Returns the key (for lookup in the Activator's image registry) of the
	 * specified image.
	 * 
	 * @param checked Checked or unchecked.
	 * @param inherited Inherited or not.
	 * @param greyed Greyed out or not.
	 * 
	 * @return Image key.
	 */
	public abstract String getImageKey(boolean checked, boolean inherited, boolean greyed);

	/**
	 * Gets the flag resolver used to determine the checkbox state.
	 * 
	 * @return Flag resolver.
	 */
	public abstract InheritedFlagResolver getFlagResolver();
}
