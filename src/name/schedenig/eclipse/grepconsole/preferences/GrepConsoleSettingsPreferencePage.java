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
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;
import name.schedenig.eclipse.popupnotifications.preferences.ColorPickerFieldEditor;
import name.schedenig.eclipse.popupnotifications.preferences.FontPickerFieldEditor;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A preference page for global Grep Console settings.
 * 
 * @author msched
 */
public class GrepConsoleSettingsPreferencePage extends FieldEditorPreferencePage 
		implements IWorkbenchPreferencePage
{
	/**
	 * Creates a new instance.
	 */
	public GrepConsoleSettingsPreferencePage()
	{
		super(GRID);
	}
	
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors()
	{
		IntegerFieldEditor feStyleMatchLength = new IntegerFieldEditor(
				Activator.PREFS_STYLE_MATCH_LENGTH, 
				Messages.GrepConsoleSettingsPreferencePage_style_match_length_label, 
				getFieldEditorParent());
		feStyleMatchLength.setValidRange(0, Integer.MAX_VALUE);
		addField(feStyleMatchLength);
		
		IntegerFieldEditor feFilterMatchLength = new IntegerFieldEditor(
				Activator.PREFS_FILTER_MATCH_LENGTH, 
				Messages.GrepConsoleSettingsPreferencePage_filter_match_length_label, 
				getFieldEditorParent());
		feFilterMatchLength.setValidRange(0, Integer.MAX_VALUE);
		addField(feFilterMatchLength);
		
		ComboFieldEditor feLinkModifierKey = new ComboFieldEditor(
				Activator.PREFS_LINK_MODIFIER_KEY,
				Messages.GrepConsoleSettingsPreferencePage_link_modifier_key_label,
				new String[][]
				{
					{ Messages.GrepConsoleSettingsPreferencePage_link_modifier_ctrl, SWT.CTRL == SWT.MOD1 ? "" : String.valueOf(SWT.CTRL) }, //$NON-NLS-1$
					{ Messages.GrepConsoleSettingsPreferencePage_link_modifier_alt, SWT.ALT == SWT.MOD1 ? "" : String.valueOf(SWT.ALT) }, //$NON-NLS-1$
					{ Messages.GrepConsoleSettingsPreferencePage_link_modifier_shift, SWT.SHIFT == SWT.MOD1 ? "" : String.valueOf(SWT.SHIFT) }, //$NON-NLS-1$
					{ Messages.GrepConsoleSettingsPreferencePage_link_modifier_command, SWT.COMMAND == SWT.MOD1 ? "" : String.valueOf(SWT.COMMAND) }, //$NON-NLS-1$
				},
				getFieldEditorParent());
		addField(feLinkModifierKey);

		GrepConsoleUtil.createPreferenceLabel(getFieldEditorParent(), 
				Messages.GrepConsoleSettingsPreferencePage_title_grep_view);
		
		ColorPickerFieldEditor feForegroundColor = new ColorPickerFieldEditor(
				Activator.PREFS_GREP_VIEW_FOREGROUND_COLOR, 
				Messages.GrepConsoleSettingsPreferencePage_foreground, 
				Messages.GrepConsoleSettingsPreferencePage_title_foreground, 
				getFieldEditorParent());
		addField(feForegroundColor);
		
		ColorPickerFieldEditor feBackgroundColor = new ColorPickerFieldEditor(
				Activator.PREFS_GREP_VIEW_BACKGROUND_COLOR, 
				Messages.GrepConsoleSettingsPreferencePage_background, 
				Messages.GrepConsoleSettingsPreferencePage_title_background, 
				getFieldEditorParent());
		addField(feBackgroundColor);
		
		GrepConsoleUtil.createPreferenceLabel(getFieldEditorParent(), 
				Messages.GrepConsoleSettingsPreferencePage_title_popup_notifications);
		
		ColorPickerFieldEditor feNotificationForegroundColor = new ColorPickerFieldEditor(
				Activator.PREFS_NOTIFICATION_FOREGROUND_COLOR, 
				Messages.GrepConsoleSettingsPreferencePage_popup_notification_foreground_color, 
				Messages.GrepConsoleSettingsPreferencePage_title_popup_notification_foreground_color, 
				getFieldEditorParent());
		addField(feNotificationForegroundColor);
		
		ColorPickerFieldEditor feNotificationBackgroundColor = new ColorPickerFieldEditor(
				Activator.PREFS_NOTIFICATION_BACKGROUND_COLOR, 
				Messages.GrepConsoleSettingsPreferencePage_popup_notification_background_color, 
				Messages.GrepConsoleSettingsPreferencePage_title_popup_notification_background_color, 
				getFieldEditorParent());
		addField(feNotificationBackgroundColor);
		
		FontPickerFieldEditor feNotificationTitleFont = new FontPickerFieldEditor(
				Activator.PREFS_NOTIFICATION_TITLE_FONT, 
				Messages.GrepConsoleSettingsPreferencePage_title_font, 
				Messages.GrepConsoleSettingsPreferencePage_title_title_font, 
				getFieldEditorParent());
		addField(feNotificationTitleFont);
		
		FontPickerFieldEditor feNotificationMessageFont = new FontPickerFieldEditor(
				Activator.PREFS_NOTIFICATION_MESSAGE_FONT, 
				Messages.GrepConsoleSettingsPreferencePage_message_font, 
				Messages.GrepConsoleSettingsPreferencePage_title_message_font, 
				getFieldEditorParent());
		addField(feNotificationMessageFont);
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench)
	{
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk()
	{
		boolean b = super.performOk();
		
		if(b)
		{
			Activator.getDefault().doSettingsChanged();
		}
		
		return b;
	}
}
