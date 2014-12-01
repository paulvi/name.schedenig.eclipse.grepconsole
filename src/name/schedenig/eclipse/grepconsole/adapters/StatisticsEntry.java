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
public class StatisticsEntry
{
	public static enum Type { COUNT, LABEL };
	
	private Type type;
	private String label;
	private Object value;

	/**
	 * @param label
	 * @param value
	 */
	public StatisticsEntry(Type type, String label, Object value)
	{
		this.type = type;
		this.label = label;
		this.value = value;
	}
	
	/**
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * @return the value
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * @param value
	 */
	public void setValue(Object value)
	{
		this.value = value;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return label.hashCode();
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof StatisticsEntry)
		{
			return ((StatisticsEntry) o).label.equals(label);
		}
		else
		{
			return super.equals(o);
		}
	}
}
