package com.github.zathrus_writer.commandsex.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.github.zathrus_writer.commandsex.CommandsEX;
import com.github.zathrus_writer.commandsex.helpers.LogHelper;
import com.github.zathrus_writer.commandsex.helpers.Utils;

/***
 * Chatted Link Shorten - automatically shortens chatted links
 * @author Kezz101
 * @return
 */

public class Handler_chattedlinkshorten implements Listener {
	
	public Handler_chattedlinkshorten() {
		Bukkit.getServer().getPluginManager().registerEvents(this, CommandsEX.plugin);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		if (!player.hasPermission("cex.shorten.auto")){
			return;
		}
		
		//Split the string into words
		String[] message = e.getMessage().split("\\s");
		
		//Create HashSet of URLs that need shortening
		HashMap<URL, Integer> urlToShorten = new HashMap<URL, Integer>();
		
		//Check for URLs and attempt to define and add them to the above HashMap
		for (int i = 0; i < message.length; i++) {
		    try {
				URL url = new URL(message[i]);
				urlToShorten.put(url, i);
			} catch (MalformedURLException e1) {
				//Do nothing!
			}
		}
		
		if(!urlToShorten.isEmpty()) {
			e.setMessage(composeNewMessage(message, urlToShorten));
		}
	}
	
	String composeNewMessage(String[] message, HashMap<URL, Integer> urlToShorten) {
		Iterator<Entry<URL, Integer>> it = urlToShorten.entrySet().iterator();
		while (it.hasNext()) {
			Entry<URL, Integer> set = (Entry<URL, Integer>)it.next();
		    try {
				message[set.getValue()] = Utils.shortenURL(set.getKey()).getPath();
			} catch (IOException e) {
				LogHelper.logWarning("chatLinkShortenError#####" + set.getKey().getPath() + "#####:#####" + e.getMessage());
			}
		    it.remove();
		}
		return Utils.implode(message, " ");
	}

}
