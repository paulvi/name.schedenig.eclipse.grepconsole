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

import name.schedenig.eclipse.grepconsole.Activator;
import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.util.GridLayoutBuilder;
import name.schedenig.eclipse.grepconsole.view.colors.ColorRegistry;
import name.schedenig.eclipse.grepconsole.view.items.PreviewColorHandler;
import name.schedenig.eclipse.popupnotifications.gui.ColorPickerLine;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel for editing a single style. The style instance being edited will not
 * be changed until <code>updateGrepStyle()</code> is called.
 * 
 * @author msched
 */
public class StylePanel extends Composite
{
	/** The style being edited. */
	private GrepStyle grepStyle;
	
	/** Colour registry used for preview. */
	private ColorRegistry colorRegistry;
	
	/** GUI variables. */
	private Composite panelCheckboxes;
	private Button cbBold;
	private Button cbItalic;
	private Label labelName;
	private Text textName;
	private StyledText stPreview;
	private ColorPickerLine cpForeground;
	private ColorPickerLine cpBackground;
	private ColorPickerLine cpUnderline;
	private ColorPickerLine cpStrikethrough;
	private ColorPickerLine cpBorder;

	/** A selection listener which updates the preview when its component's
	 *  content changes. */
	private SelectionListener selectionListener = new SelectionAdapter()
	{
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
		{
			updatePreview();
		};
	};
	
	/** A color change listener whcih updates the preview when its component's
	 *  color changes. */
	private ColorPickerLine.IColorChangeListener colorChangeListener = new ColorPickerLine.IColorChangeListener()
	{
		@Override
		public void colorChanged(ColorPickerLine source, RGB color)
		{
			updatePreview();
		}
	};

	private Label labelPreview;

	private Font headerFont;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent Parent component.
	 * @param style Style flags.
	 */
	public StylePanel(Composite parent, int style)
	{
		super(parent, style);

		init();
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose()
	{
		colorRegistry.disposeColors();
		
		super.dispose();
	}
	
	/**
	 * Initialises the GUI.
	 */
	private void init()
	{
		headerFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
		colorRegistry = new ColorRegistry(Activator.getDefault().getColorRegistry());
		
		new GridLayoutBuilder(this, 3, false).apply();
		
		labelName = new Label(this, SWT.NONE);
		labelName.setText(Messages.StylePanel_name);
		labelName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		textName = new Text(this, SWT.BORDER);
		textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		panelCheckboxes  = new Composite(this, SWT.NONE);
		RowLayout panelCheckboxesLayout = new RowLayout();
		panelCheckboxesLayout.marginWidth = panelCheckboxesLayout.marginHeight = 0; 
		panelCheckboxesLayout.marginLeft = panelCheckboxesLayout.marginRight = 0; 
		panelCheckboxesLayout.marginTop = panelCheckboxesLayout.marginBottom = 0;
		
		panelCheckboxes.setLayout(panelCheckboxesLayout);
		panelCheckboxes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3 ,1));

		cbBold = new Button(panelCheckboxes, SWT.CHECK);
		cbBold.setText(Messages.StylePanel_bold);
		cbBold.addSelectionListener(selectionListener);
		
		cbItalic = new Button(panelCheckboxes, SWT.CHECK);
		cbItalic.setText(Messages.StylePanel_italic);
		cbItalic.addSelectionListener(selectionListener);

		cpForeground = new ColorPickerLine(this);
		cpForeground.setText(Messages.StylePanel_foreground);
		cpForeground.setDialogTitle(Messages.StylePanel_title_foreground);
		cpForeground.addListener(colorChangeListener);
		
		cpBackground = new ColorPickerLine(this);
		cpBackground.setText(Messages.StylePanel_background);
		cpBackground.setDialogTitle(Messages.StylePanel_title_background);
		cpBackground.addListener(colorChangeListener);
		
		cpUnderline = new ColorPickerLine(this);
		cpUnderline.setText(Messages.StylePanel_underline);
		cpUnderline.setDialogTitle(Messages.StylePanel_title_underline);
		cpUnderline.setColorMandatory(false);
		cpUnderline.addListener(colorChangeListener);

		cpStrikethrough = new ColorPickerLine(this);
		cpStrikethrough.setText(Messages.StylePanel_strikethrough);
		cpStrikethrough.setDialogTitle(Messages.StylePanel_title_strikethrough);
		cpStrikethrough.setColorMandatory(false);
		cpStrikethrough.addListener(colorChangeListener);

		cpBorder = new ColorPickerLine(this);
		cpBorder.setText(Messages.StylePanel_border);
		cpBorder.setDialogTitle(Messages.StylePanel_title_border);
		cpBorder.setColorMandatory(false);
		cpBorder.addListener(colorChangeListener);

		labelPreview = new Label(this, SWT.NONE);
		labelPreview.setFont(headerFont);
		labelPreview.setText(Messages.StylePanel_preview);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gridData.verticalIndent = 10;
		labelPreview.setLayoutData(gridData);

		final String unstyledText = Messages.StylePanel_unstyled_preview;
		final String styledText = Messages.StylePanel_styled_preview;
		
		stPreview = new StyledText(this, SWT.BORDER);
		stPreview.setText(unstyledText + "\n" + styledText + "\n" + unstyledText); //$NON-NLS-1$ //$NON-NLS-2$
		stPreview.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 3, 1));
		stPreview.setFont(JFaceResources.getTextFont());
		stPreview.setEditable(false);
		
		new PreviewColorHandler(stPreview);

		stPreview.addLineStyleListener(new LineStyleListener()
		{
			@Override
			public void lineGetStyle(LineStyleEvent event)
			{
				if(event.lineOffset == unstyledText.length() + 1)
				{
					StyleRange style = new StyleRange();
					style.start = event.lineOffset;
					style.length = event.lineText.length();
					fillStyleRange(style);
					event.styles = new StyleRange[]{style};
				}
			}
		});
		
		refresh();
	}

	/**
	 * Refreshes the panel's contents based on the current style.
	 */
	public void refresh()
	{
		if(grepStyle == null)
		{
			textName.setText(""); //$NON-NLS-1$
			
			cbBold.setSelection(false);
			cbItalic.setSelection(false);
			
			cpForeground.setColor(null);
			cpBackground.setColor(null);
			cpUnderline.setColor(null);
			cpUnderline.setChecked(false);
			cpStrikethrough.setColor(null);
			cpStrikethrough.setChecked(false);
			cpBorder.setColor(null);
			cpBorder.setChecked(false);
		}
		else
		{
			String name = grepStyle.getName();
			textName.setText(name == null ? StyleLabelProvider.LABEL_UNNAMED : name);

			cbBold.setSelection(grepStyle.isBold());
			cbItalic.setSelection(grepStyle.isItalic());
			
			cpForeground.setColor(grepStyle.getForeground());
			cpBackground.setColor(grepStyle.getBackground());
			cpUnderline.setColor(grepStyle.getUnderlineColor());
			cpUnderline.setChecked(grepStyle.isUnderline());
			cpStrikethrough.setColor(grepStyle.getStrikeoutColor());
			cpStrikethrough.setChecked(grepStyle.isStrikeout());
			cpBorder.setColor(grepStyle.getBorderColor());
			cpBorder.setChecked(grepStyle.isBorder());
		}
		
		textName.selectAll();
		updatePreview();
	}

	/**
	 * Fills a style range with the style settings currently set in the panel.
	 * 
	 * @param style Style range.
	 */
	protected void fillStyleRange(StyleRange style)
	{
		RGB rgbForeground = cpForeground.getEffectiveColor();
		RGB rgbBackground = cpBackground.getEffectiveColor();
		RGB rgbUnderline = cpUnderline.getEffectiveColor();
		RGB rgbStrikethrough = cpStrikethrough.getEffectiveColor();
		RGB rgbBorder = cpBorder.getEffectiveColor();

		if(rgbBackground == null)
		{
			rgbBackground = new RGB(255, 255, 255);
		}

		style.foreground = getColor(rgbForeground);
		style.background = getColor(rgbBackground);
		
		style.fontStyle = 0
				| (cbBold.getSelection() ? SWT.BOLD : 0)
				| (cbItalic.getSelection() ? SWT.ITALIC : 0);
		
		style.underline = cpUnderline.isChecked();
		style.underlineColor = getColor(rgbUnderline);
		
		style.strikeout = cpStrikethrough.isChecked();
		style.strikeoutColor = getColor(rgbStrikethrough);
		
		style.borderStyle = cpBorder.isChecked() ? SWT.BORDER_SOLID : SWT.NONE;
		style.borderColor = getColor(rgbBorder);
	}

	/**
	 * Reads a colour from the colour registry.
	 * 
	 * @param rgb RGB value. May be <code>null</code>.
	 * 
	 * @return Color instance. May be <code>null</code>.
	 */
	private Color getColor(RGB rgb)
	{
		return rgb == null ? null : colorRegistry.get(rgb);
	}

	/**
	 * Updates the preview.
	 */
	private void updatePreview()
	{
		colorRegistry.disposeColors();
		stPreview.redraw();
	}

	/**
	 * Sets the style being edited.
	 * 
	 * @param style Style.
	 */
	public void setGrepStyle(GrepStyle grepStyle)
	{
		this.grepStyle = grepStyle;
		
		if(textName != null)
		{
			refresh();
		}
	}
	
	/**
	 * Returns the style being edited.
	 * 
	 * @return Style.
	 */
	public GrepStyle getGrepStyle()
	{
		return grepStyle;
	}
	
	/**
	 * Updates the style's properties according to the values set in the panel.
	 */
	public void updateGrepStyle()
	{
		grepStyle.setName(textName.getText());
		grepStyle.setBold(cbBold.getSelection());
		grepStyle.setItalic(cbItalic.getSelection());
		grepStyle.setForeground(cpForeground.getEffectiveColor());
		grepStyle.setBackground(cpBackground.getEffectiveColor());
		grepStyle.setUnderline(cpUnderline.isChecked());
		grepStyle.setUnderlineColor(cpUnderline.getColor()); 
		grepStyle.setStrikeout(cpStrikethrough.isChecked());
		grepStyle.setStrikeoutColor(cpStrikethrough.getColor());
		grepStyle.setBorder(cpBorder.isChecked());
		grepStyle.setBorderColor(cpBorder.getColor());
	}
}
