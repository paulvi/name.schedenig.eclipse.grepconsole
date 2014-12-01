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

package name.schedenig.eclipse.grepconsole.preferences;

import name.schedenig.eclipse.grepconsole.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Preferences initializer for Grep Console settings.
 * 
 * @author msched
 */
public class GrepConsolePreferenceInitializer extends AbstractPreferenceInitializer
{
	/**
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(Activator.PREFS_STYLE_MATCH_LENGTH, Activator.DEFAULT_STYLE_MATCH_LENGTH);
		store.setDefault(Activator.PREFS_FILTER_MATCH_LENGTH, Activator.DEFAULT_FILTER_MATCH_LENGTH);
	}
}
