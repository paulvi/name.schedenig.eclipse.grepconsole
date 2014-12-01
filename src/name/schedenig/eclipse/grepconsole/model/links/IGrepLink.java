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

package name.schedenig.eclipse.grepconsole.model.links;

import name.schedenig.eclipse.grepconsole.adapters.links.GrepLinkAdapter;

/**
 * Base interface for all types of links model items.
 * 
 * Links typically use "patterns" as their fields, i.e. strings where certain
 * parameters are inserted from the matched text line. See
 * {@link GrepLinkAdapter} for available pattern parameters.
 * 
 * @author msched
 */
public interface IGrepLink
{
	/**
	 * Creates a copy of the link.
	 * 
	 * @return Copy.
	 */
	public IGrepLink copy();
}
