/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.action.squeezebox.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.openhab.core.scriptengine.action.ActionDoc;
import org.openhab.core.scriptengine.action.ParamDoc;
import org.openhab.io.squeezeserver.SqueezePlayer;
import org.openhab.io.squeezeserver.SqueezeServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the methods that are made available in scripts and rules
 * for Squeezebox integration.
 * 
 * @author Ben Jones
 * @since 1.4.0
 */
public class Squeezebox {

	private static final Logger logger = 
		LoggerFactory.getLogger(Squeezebox.class);

	// handle to the Squeeze Server connection
	public static SqueezeServer squeezeServer;

	// TODO: could make these properties configurable to support other translation services
	private final static String GOOGLE_TRANSLATE_URL = "http://translate.google.com/translate_tts?tl=en&q=";
	private final static int MAX_SENTENCE_LENGTH = 100;

	@ActionDoc(text = "Turn one of your Squeezebox devices on/off", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxPower(
			@ParamDoc(name = "playerId", text = "The Squeezebox to turn on/off") String playerId,
			@ParamDoc(name = "power", text = "True to turn on, False to turn off") boolean power) {		
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;
		
		if (power) {
			squeezeServer.powerOn(playerId);
		} else {
			squeezeServer.powerOff(playerId);
		}
		return true;
	}
	
	@ActionDoc(text = "Mute/unmute one of your Squeezebox devices", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxMute(
			@ParamDoc(name = "playerId", text = "The Squeezebox to turn on/off") String playerId,
			@ParamDoc(name = "mute", text = "True to mute, False to un-mute") boolean mute) {		
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;

		if (mute) {
			squeezeServer.mute(playerId);
		} else {
			squeezeServer.unMute(playerId);
		}
		return true;
	}
	
	@ActionDoc(text = "Set the volume on one of your Squeezebox devices", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxVolume(
			@ParamDoc(name = "playerId", text = "The Squeezebox to turn on/off") String playerId,
			@ParamDoc(name = "volume", text = "The volume between 0-100") int volume) {		
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;

		squeezeServer.setVolume(playerId, volume);
		return true;
	}
	
	@ActionDoc(text = "Send the 'play' command to one of your Squeezebox devices", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxPlay(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the command to") String playerId) {
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;
		
		squeezeServer.play(playerId);
		return true;
	}
	
	@ActionDoc(text = "Send the 'pause' command to one of your Squeezebox devices", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxPause(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the command to") String playerId) {
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;

		squeezeServer.pause(playerId);
		return true;
	}
	
	@ActionDoc(text = "Send the 'stop' command to one of your Squeezebox devices", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxStop(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the command to") String playerId) {
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;

		squeezeServer.stop(playerId);
		return true;
	}
	
	@ActionDoc(text = "Send the 'next' command to one of your Squeezebox devices", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxNext(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the command to") String playerId) {
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;

		squeezeServer.next(playerId);
		return true;
	}
	
	@ActionDoc(text = "Send the 'prev' command to one of your Squeezebox devices", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxPrev(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the command to") String playerId) {
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;

		squeezeServer.prev(playerId);
		return true;
	}
	
	@ActionDoc(text = "Play a URL on one of your Squeezebox devices using the current volume for that device", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxPlayUrl(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the URL to") String playerId,
			@ParamDoc(name = "url", text = "The URL to play (if empty will clear the playlist)") String url) {
		return squeezeboxPlayUrl(playerId, url, -1);
	}	
	
	@ActionDoc(text = "Play a URL on one of your Squeezebox devices using the specified volume", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxPlayUrl(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the URL to") String playerId,
			@ParamDoc(name = "url", text = "The URL to play (if empty will clear the playlist)") String url,
			@ParamDoc(name = "volume", text = "The volume to set the device when playing this URL (between 1-100)") int volume) {
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;

		// set the player ready to play this URL
   		if (volume != -1) {
			logger.trace("Setting player state: volume {}", volume);
			squeezeServer.setVolume(playerId, volume);
		}

		// play the url
   		if (StringUtils.isEmpty(url)) {
   			squeezeServer.clearPlaylist(playerId);
   		} else {
   			squeezeServer.playUrl(playerId, url);
   		}
		return true;
	}
	
	@ActionDoc(text = "Speak a message via one of your Squeezebox devices using the current volume for that device", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxSpeak(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the message to") String playerId,
			@ParamDoc(name = "message", text = "The message to say") String message) {
		return squeezeboxSpeak(playerId, message, -1);
	}	
	
	@ActionDoc(text = "Speak a message via one of your Squeezebox devices using the specified volume", returns = "<code>true</code>, if successful and <code>false</code> otherwise.")
	public static boolean squeezeboxSpeak(
			@ParamDoc(name = "playerId", text = "The Squeezebox to send the message to") String playerId,
			@ParamDoc(name = "message", text = "The message to say") String message,
			@ParamDoc(name = "volume", text = "The volume to set the device when speaking this message (between 1-100)") int volume) {
		SqueezePlayer player = getPlayer(playerId);
		if (player == null) return false;

		// get the current player state
		int playerVolume = player.getUnmuteVolume();
		boolean playerPowered = player.isPowered();
		boolean playerMuted = player.isMuted();
		
		// set the player ready to play this announcement
		if (playerMuted) {
			logger.trace("Setting player state: unmuted");
			squeezeServer.unMute(playerId);
		}
		if (volume != -1) {
			logger.trace("Setting player state: volume {}", volume);
			squeezeServer.setVolume(playerId, volume);
		}

		// can only 'say' 100 chars at a time
		List<String> sentences = getSentences(message, MAX_SENTENCE_LENGTH);

		// send each sentence in turn
		for (String sentence : sentences) {
			logger.debug("Sending sentence to " + playerId + " (" + sentence + ")");
			
			String encodedSentence;
			try {
				encodedSentence = URLEncoder.encode(sentence, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.warn("Failed to encode sentence '" + sentence + "'. Skipping sentence.", e);
				continue;
			}
			encodedSentence = encodedSentence.replace("+", "%20");
			logger.trace("Encoded sentence " + encodedSentence);
			
			// build the URL to send to the Squeezebox to play
			String url = GOOGLE_TRANSLATE_URL + encodedSentence;
			
			// create an instance of our special listener so we can detect when the sentence is complete
			SqueezeboxListener listener = new SqueezeboxListener(playerId, url);
			player.addPlayerEventListener(listener);
			
			// send the URL (this will power up the player and un-mute if necessary)
			logger.trace("Sending URL '{}' to device to play", url);
			squeezeServer.playUrl(playerId, url);
			
			// wait for this message to complete (timing out after 10s)
			int timeoutCount = 0;
			while (!listener.isFinished() && timeoutCount < 100) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
				timeoutCount++;
			}
			
			// clean up the listener
			player.removePlayerEventListener(listener);
			listener = null;
		}
		
		// clear the player playlist
		squeezeServer.clearPlaylist(playerId);
		
		// restore the player state
		if (volume != -1) {
			logger.trace("Restoring player to previous state: volume {}", playerVolume);
			squeezeServer.setVolume(playerId, playerVolume);
		}
		if (playerMuted) {
			logger.trace("Restoring player to previous state: muted");
			squeezeServer.mute(playerId);
		}
		if (!playerPowered) {
			logger.trace("Restoring player to previous state: off");
			squeezeServer.powerOff(playerId);
		}
		
		return true;
	}
	
	private static SqueezePlayer getPlayer(String playerId) {
		if (StringUtils.isEmpty(playerId))
			throw new NullArgumentException("playerId");

		// check the Squeeze Server has been initialised
		if (squeezeServer == null) {
			logger.warn("Squeeze Server yet to be initialised. Ignoring action.");
			return null;
		}
		
		// check we are connected to the Squeeze Server
		if (!squeezeServer.isConnected()) {
			logger.warn("Not connected to the Squeeze Server. Please check your config and consult the openHAB WIKI for instructions on how to configure. Ignoring action.");
			return null;
		}

		SqueezePlayer player = squeezeServer.getPlayer(playerId);
		if (player == null) {
			logger.warn("No Squeezebox player exists with name '{}'. Ignoring action.", playerId);
			return null;
		}
		
		return player;
	}

	private static List<String> getSentences(String message, int maxSentenceLength) {
		if (StringUtils.isEmpty(message))
			return new ArrayList<String>();
		
		// can only 'say' 100 chars at a time so split the message into words
		String[] words = StringUtils.split(message, ' ');

		List<String> sentences = new ArrayList<String>();
		String sentence = "";
		
		for (String word : words) {
			// ignore double spaces
			if (word.length() == 0) {
				continue;
			}
			
			// check this word isn't too long by itself
			if (word.length() > maxSentenceLength) {
				logger.warn("Unable to say '{}' as this word is longer than the maximum sentence allowed ({})", word, maxSentenceLength);
				continue;
			}
			
			// if this word makes our sentence too long start a new sentence
			if (sentence.length() + word.length() > maxSentenceLength) {
				sentences.add(sentence.substring(0, sentence.length() - 1));
				sentence = "";
			} 
			
			// add this word to the current sentence
			sentence += word + " ";
		}
		
		// add the final sentence
		if (sentence.length() > 0)
			sentences.add(sentence.substring(0, sentence.length() - 1));
		
		return sentences;
	}	
}
