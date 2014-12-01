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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;

/**
 * Maintains a set of colours. Each colour is bound to one or more owners.
 * When an owner disposes its colours, all colours that are not bound to any
 * more owners are disposed.
 * 
 * @author msched
 */
public class OwnerColorRegistry
{
	/** Device for instantiating new colours. */
	private Device device;
	
	/** Maps RGB values to colour instances. */
	private Map<RGB, Color> colors = new HashMap<RGB, Color>();
	
	/** Maps owners to sets of RGB values. */
	private Map<Object, Set<RGB>> rgbByOwner = new HashMap<Object, Set<RGB>>();
	
	/** Maps RGB values to sets of owners. */
	private Map<RGB, Set<Object>> ownersByRgb = new HashMap<RGB, Set<Object>>();
	
	/**
	 * Creates a new instance.
	 * 
	 * @param device Device for instantiating new colours.
	 */
	public OwnerColorRegistry(Device device)
	{
		this.device = device;
	}

	/**
	 * Disposes all colours by all owners.
	 */
	public synchronized void disposeColors()
	{
		for(Color color: colors.values())
		{
			color.dispose();
		}
		
		colors.clear();
		rgbByOwner.clear();
		ownersByRgb.clear();
	}

	/**
	 * Disposes all colours by the specified owner.
	 * 
	 * @param owner Owner.
	 */
	public synchronized void disposeColors(Object owner)
	{
		Set<RGB> owned = rgbByOwner.get(owner);
		
		if(owned == null)
		{
			return;
		}
		
		for(RGB rgb: owned)
		{
			Set<Object> owners = ownersByRgb.get(rgb);
			owners.remove(owner);
			
			if(owners.isEmpty())
			{
				ownersByRgb.remove(rgb);
				Color color = colors.remove(rgb);
				color.dispose();
			}
		}
		
		rgbByOwner.remove(owner);
	}
	
	/**
	 * Returns the specified colour for an owner. If necessary, the colour is
	 * instantiated on demand.
	 * 
	 * Once an owner calls this method, it is responsible for calling
	 * disposeColors() at a later point.
	 * 
	 * @param owner Owner.
	 * @param rgb RGB value of the requested colour. <code>null</code> values
	 * 		are permitted and return <code>null</code> instead of a Color instance.
	 * 
	 * @return Colour instance.
	 */
	public synchronized Color get(Object owner, RGB rgb)
	{
		if(rgb == null)
		{
			return null;
		}
		
		Color color = colors.get(rgb);
		
		if(color == null)
		{
			color = new Color(device, rgb);
			colors.put(rgb, color);
		}

		Set<RGB> owned = rgbByOwner.get(owner);
		
		if(owned == null)
		{
			owned = new HashSet<RGB>();
			rgbByOwner.put(owner, owned);
		}

		owned.add(rgb);
		
		Set<Object> owners = ownersByRgb.get(rgb);
		
		if(owners == null)
		{
			owners = new HashSet<Object>();
			ownersByRgb.put(rgb, owners);
		}

		owners.add(owner);
		
		return color;
	}
}
