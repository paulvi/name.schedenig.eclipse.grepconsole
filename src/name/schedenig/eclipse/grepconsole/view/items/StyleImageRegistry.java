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

package name.schedenig.eclipse.grepconsole.view.items;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * Creates and caches thumbnail images for styles. Each image consists of two
 * filled rectangles, defined by a two element entry of RGB values. 
 *  
 * @author msched
 */
public class StyleImageRegistry
{
	/**
	 * A comparable pair of RGB values.
	 * 
	 * @author msched
	 */
	public static class RgbPair
	{
		/** First RGB value. */
		private RGB rgb1;
		
		/** Second RGB value. */
		private RGB rgb2;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param rgb1 First RGB value.
		 * @param rgb2 Second RGB value.
		 */
		public RgbPair(RGB rgb1, RGB rgb2)
		{
			this.rgb1 = rgb1;
			this.rgb2 = rgb2;
		}

		/**
		 * Returns the first RGB value.
		 * 
		 * @return First RGB value.
		 */
		public RGB getRgb1()
		{
			return rgb1;
		}

		/**
		 * Returns the second RGB value.
		 * 
		 * @return Second RGB value.
		 */
		public RGB getRgb2()
		{
			return rgb2;
		}
		
		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return (rgb1 == null ? 0 : rgb1.hashCode()) 
					| (rgb2 == null ? 0 : rgb2.hashCode());
		}
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof RgbPair))
			{
				return false;
			}
			
			RgbPair pair = (RgbPair) obj;
			
			if(rgb1 == null || pair.rgb1 == null)
			{
				if(rgb1 != pair.rgb1)
				{
					return false;
				}
			}
			else if(!rgb1.equals(pair.rgb1))
			{
				return false;
			}
			
			if(rgb2 == null || pair.rgb2 == null)
			{
				if(rgb2 != pair.rgb2)
				{
					return false;
				}
			}
			else if(!rgb2.equals(pair.rgb2))
			{
				return false;
			}
			
			return true;
		}
	}
	
	/** Cached colour images. */
	private Map<RgbPair, Image> colorImages = new HashMap<RgbPair, Image>();

	/**
	 * Clears the colour image cache, disposing all contained images.
	 */
	public void dispose()
	{
		for(Image image: colorImages.values())
		{
			image.dispose();
		}
		
		colorImages.clear();
	}

	/**
	 * Returns an image showing the specified colour. If no matching image is
	 * found in the cache, it is created and cached automatically.
	 * 
	 * @param color RGB colour.
	 * 
	 * @return Colour image.
	 */
	public Image getColorImage(RgbPair color)
	{
		Image image = colorImages.get(color);
		
		if(image == null)
		{
			image = createColorImage(color);
			colorImages.put(color, image);
		}
		
		return image;
	}

	/**
	 * Creates an image showing the specified colour.
	 * 
	 * @param color RGB colour.
	 * 
	 * @return Colour image.
	 */
	private Image createColorImage(RgbPair color)
	{
    int height = 16;
    int width = (int)(height * 1.5);

    RGB black = new RGB(0, 0, 0);
    RGB left = color.getRgb1() == null ? new RGB(255, 255, 255) : color.getRgb1();
    RGB right = color.getRgb2() == null ? new RGB(255, 255, 255) : color.getRgb2();
    
    PaletteData dataPalette = new PaletteData(new RGB[] { black, left, right });
    
    ImageData data = new ImageData(width, height, 2, dataPalette);
    data.transparentPixel = -1;

    for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (x == 0 || y == 0 || x == width-1 || y == height-1)
				{
					data.setPixel(x, y, 0);
				}
				else
				{
					data.setPixel(x, y, (x < width / 2) ? 1 : 2);
				}
			}
		}

		return new Image(null, data);
	}
}
