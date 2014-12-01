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

package name.schedenig.eclipse.grepconsole.view.grepstatistics;

import name.schedenig.eclipse.grepconsole.adapters.StatisticsEntry;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * 
 * @author msched
 */
public class LabelLabelProvider extends CellLabelProvider
{
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
	 */
	@Override
	public void update(ViewerCell cell)
	{
//		String label = (String) cell.getElement();
//		cell.setText(label);
		StatisticsEntry entry = (StatisticsEntry) cell.getElement();
		cell.setText(entry.getLabel());
	}
}
