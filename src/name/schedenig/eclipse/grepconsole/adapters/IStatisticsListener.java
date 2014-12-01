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

package name.schedenig.eclipse.grepconsole.adapters;

/**
 * 
 * @author msched
 */
public interface IStatisticsListener
{
	public void statisticsUpdated(TextFilter src, StatisticsEntry entry, boolean isNew);

	/**
	 * @param textFilter
	 * @param entry
	 */
	public void statisticsRemoved(TextFilter textFilter, StatisticsEntry entry);
}
