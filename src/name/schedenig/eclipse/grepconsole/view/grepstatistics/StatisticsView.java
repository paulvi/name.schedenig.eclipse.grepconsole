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

import java.util.LinkedHashMap;
import java.util.Map;

import name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant;
import name.schedenig.eclipse.grepconsole.adapters.IStatisticsListener;
import name.schedenig.eclipse.grepconsole.adapters.StatisticsEntry;
import name.schedenig.eclipse.grepconsole.adapters.TextFilter;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.view.common.GrepConsoleView;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * 
 * @author msched
 */
public class StatisticsView extends GrepConsoleView implements IStatisticsListener
{
	private Table table;
	private TableViewer viewer;
	private TableViewerColumn colLabel;
	private TableViewerColumn colValue;
	private TextFilter filter;

	private Map<String, StatisticsEntry> values = new LinkedHashMap<String, StatisticsEntry>();
	
	@Override
	public void createPartControl(Composite parent)
	{
		table = new Table(parent, SWT.NONE);
		table.setHeaderVisible(true);
		
		viewer = new TableViewer(table);
		
		colLabel = new TableViewerColumn(viewer, SWT.LEFT);
		colLabel.getColumn().setText(Messages.StatisticsView_label);
		colLabel.getColumn().setWidth(100);
		colLabel.setLabelProvider(new LabelLabelProvider());
		
		colValue = new TableViewerColumn(viewer, SWT.LEFT);
		colValue.getColumn().setText(Messages.StatisticsView_value);
		colValue.getColumn().setWidth(100);
		colValue.setLabelProvider(new ValueLabelProvider());//values));
		
		viewer.setContentProvider(new StatisticsContentProvider());
//		viewer.setInput(values);
	}
	
	/* (non-Javadoc)
	 * @see name.schedenig.eclipse.grepconsole.view.common.GrepConsoleView#setParticipant(name.schedenig.eclipse.grepconsole.adapters.GrepPageParticipant)
	 */
	@Override
	protected void setParticipant(GrepPageParticipant participant)
	{
		if(filter != null)
		{
			filter.removeStatisticsListener(this);
		}
		
		super.setParticipant(participant);
		
		if(participant == null)
		{
			filter = null;
		}
		else
		{
			filter = participant.getTextFilter();
			
			if(filter != null)
			{
				filter.addStatisticsListener(this);
//				viewer.setInput(filter.getStatisticCountEntries()); // FIXME: also values
				viewer.setInput(filter.getStatisticEntries()); // FIXME: also values
			}
		}
	}

	/* (non-Javadoc)
	 * @see name.schedenig.eclipse.grepconsole.adapters.IStatisticsListener#statisticsUpdated(name.schedenig.eclipse.grepconsole.adapters.TextFilter, java.lang.String, java.lang.Object)
	 */
	@Override
	public void statisticsUpdated(TextFilter src, StatisticsEntry entry, boolean isNew)
	{
		if(isNew)
		{
			StatisticsEntry oldValue = values.remove(entry.getLabel());
			
			if(oldValue != null)
			{
				viewer.remove(oldValue);
			}
		}
		
		values.put(entry.getLabel(), entry);
		
		if(isNew)
		{
			viewer.add(entry);
		}
		else
		{
			viewer.update(entry, null);
		}
	}
	
	/* (non-Javadoc)
	 * @see name.schedenig.eclipse.grepconsole.adapters.IStatisticsListener#statisticsRemoved(name.schedenig.eclipse.grepconsole.adapters.TextFilter, name.schedenig.eclipse.grepconsole.adapters.StatisticsEntry)
	 */
	@Override
	public void statisticsRemoved(TextFilter textFilter, StatisticsEntry entry)
	{
		values.remove(entry.getLabel());
		viewer.remove(entry);
	}
}
