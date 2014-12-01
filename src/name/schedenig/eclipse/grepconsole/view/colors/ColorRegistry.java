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

package name.schedenig.eclipse.grepconsole.view.colors;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * A per-owner proxy to OwnerColorRegistry. Clients may instantiate this class
 * to access a color registry without having to specify an owner instance for
 * each request.
 * 
 * @author msched
 */
public class ColorRegistry
{
	/** Colour registry to which this proxy instance is bound. */
	private OwnerColorRegistry mainRegistry;

	/**
	 * Creates a new instance.
	 * 
	 * @param mainRegistry Colour registry to which the new instance shall be
	 * 		bound.
	 */
	public ColorRegistry(OwnerColorRegistry mainRegistry)
	{
		this.mainRegistry = mainRegistry;
	}
	
	/**
	 * Disposes all colours held by this proxy.
	 */
	public synchronized void disposeColors()
	{
		mainRegistry.disposeColors(this);
	}
	
	/**
	 * Returns the specified colour. If necessary, the colour is instantiated on
	 * demand.
	 * 
	 * Once an owner calls this method, it is responsible for calling
	 * disposeColors() at a later point.
	 * 
	 * @param rgb RGB value of the requested colour. <code>null</code> values
	 * 		are permitted and return <code>null</code> instead of a Color instance.
	 * 
	 * @return Colour instance.
	 */
	public synchronized Color get(RGB rgb)
	{
		return mainRegistry.get(this, rgb);
	}
}
