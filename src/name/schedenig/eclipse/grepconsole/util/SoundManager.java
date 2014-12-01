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

package name.schedenig.eclipse.grepconsole.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import name.schedenig.eclipse.grepconsole.i18n.Messages;

/**
 * Plays sampled audio clips via javax.sound. Samples are specified via file
 * system paths and cached by the manager for quick reuse. Audio lines are
 * closed automatically after playback to prevent the system from running out
 * of free lines.
 * 
 * @author msched
 */
public class SoundManager
{
	/** Buffer size for reading sample files. */
	private static final int BUFFER_SIZE = 4096;
	
	/** Maps paths to loaded sample byte arrays. */
	private LinkedHashMap<String, byte[]> samples;
	
	/** Synchronises access to the samples map. */
	private ReentrantLock samplesLock = new ReentrantLock();
	
	/**
	 * Creates a new instance.
	 * 
	 * @param cacheSize Maximum number of samples that should be cached. 
	 */
	@SuppressWarnings("serial")
	public SoundManager(final int cacheSize)
	{
		samples = new LinkedHashMap<String, byte[]>(cacheSize)
		{
		  public boolean removeEldestEntry(Map.Entry<String, byte[]> eldest)
		  {
        return size() > cacheSize;
		  }
		};
	}
	
	/**
	 * Clears sample cache when the manager is disposed.
	 */
	public synchronized void dispose()
	{
		samplesLock.lock();
		
		try
		{
			samples.clear();
		}
		finally
		{
			samplesLock.unlock();
		}
	}
	
	/**
	 * Gets an audio input stream for the specified sample path. The sample data
	 * is taken from the cache, if available, or loaded from the disk.
	 * 
	 * @param path Sample path.
	 * 
	 * @return Audio input stream.
	 * 
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws LineUnavailableException
	 */
	private synchronized AudioInputStream getAudioInputStream(String path) throws IOException, UnsupportedAudioFileException, LineUnavailableException
	{
		byte[] bytes;
		
		samplesLock.lock();
		
		try
		{
			bytes = samples.remove(path);

			if(bytes == null)
			{
				File file = new File(path);
				
				if(!file.exists())
				{
					throw new IOException(MessageFormat.format(Messages.SoundManager_file_not_found_file, path));
				}
				
				FileInputStream in = new FileInputStream(file);
				
				try
				{
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					byte[] buffer = new byte[BUFFER_SIZE];
					
					for(;;)
					{
						int count = in.read(buffer);
						
						if(count < 0)
						{
							break;
						}
						
						bout.write(buffer, 0, count);
					}
					
					bout.close();
					bytes = bout.toByteArray();
				}
				finally
				{
					in.close();
				}
			}
			
			samples.put(path, bytes);
		}
		finally
		{
			samplesLock.unlock();
		}
		
		AudioInputStream in = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes));
		
		return in;
	}
	
	/**
	 * Plays a sample. Returns immediately, while the sound is still playing.
	 * 
	 * @param soundPath Sample path.
	 * 
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws LineUnavailableException
	 */
	public synchronized void playSound(String soundPath) throws UnsupportedAudioFileException, IOException, LineUnavailableException
	{
		AudioInputStream in = getAudioInputStream(soundPath);

		if(in != null)
		{
			AudioFormat format = in.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);

			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(in);
			
			startClip(clip);
		}
	}

	/**
	 * Starts audio clip playback in a separate thread.
	 * 
	 * @param clip Audio clip.
	 */
	private synchronized void startClip(Clip clip)
	{
		new Thread(new PlayClipRunnable(clip)).start();
	}

	/**
	 * Plays back an audio clip and frees the line after playback has finished.
	 * 
	 * @author msched
	 */
	private class PlayClipRunnable implements Runnable, LineListener
	{
		/** Audio clip. */
		private Clip clip;
		
		/** Latch for waiting for end of playback. */
		private CountDownLatch latch = new CountDownLatch(1);

		/**
		 * Creates a new instance.
		 * 
		 * @param clip Audio clip for playback.
		 */
		public PlayClipRunnable(Clip clip)
		{
			this.clip = clip;
		}
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			clip.addLineListener(this);
			clip.start();

			try
			{
				latch.await();
			}
			catch(InterruptedException ex)
			{
				Thread.currentThread().interrupt();
			}
			
			clip.close();
		}

		/**
		 * If playback has stopped, counts down the latch to let the main thread
		 * close the clip.
		 * 
		 * @see javax.sound.sampled.LineListener#update(javax.sound.sampled.LineEvent)
		 */
		@Override
		public void update(LineEvent event)
		{
			if(event.getType() == LineEvent.Type.STOP)
			{
				latch.countDown();
			}
		}
	}
}
