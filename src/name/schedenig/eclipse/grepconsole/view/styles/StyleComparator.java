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

import name.schedenig.eclipse.grepconsole.model.GrepStyle;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Comapres two styles by name. Can handle the style content provider's null
 * element style.
 * 
 * @author msched
 */
public class StyleComparator extends ViewerComparator
{
	/**
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if(e1 == StyleContentProvider.NULL_ELEMENT && e2 == StyleContentProvider.NULL_ELEMENT)
		{
			return 0;
		}
		else if(e1 == StyleContentProvider.NULL_ELEMENT)
		{
			return -1;
		}
		else if(e2 == StyleContentProvider.NULL_ELEMENT)
		{
			return +1;
		}
		
		GrepStyle s1 = (GrepStyle) e1;
		GrepStyle s2 = (GrepStyle) e2;
		
		String n1 = s1.getName();
		String n2 = s2.getName();
		
		if(n1 == n2)
		{
			return 0;
		}
		else if(n1 == null)
		{
			return -1;
		}
		else if(n2 == null)
		{
			return +1;
		}
		else
		{
			return n1.compareTo(n2);
		}
	}
}
