package com.github.zathrus_writer.commandsex.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.github.zathrus_writer.commandsex.CommandsEX;
import com.github.zathrus_writer.commandsex.Vault;

/***
 * Contains set of functions to be used for player promotion functions.
 * @author zathrus-writer
 *
 */
public class Promotions {
	
	/***
	 * The actual delayed task to check for promotions and promote as needed.
	 * Used in 2 places - delayed task and Quit event.
	 */
	public static void checkTimedPromotions(Player...players) {
		// load up settings from config file
		FileConfiguration f = CommandsEX.getConf();
		ConfigurationSection configGroups = f.getConfigurationSection("timedPromote");
		Map<String, Integer> settings = new HashMap<String, Integer>();
		List<?> exclusions = CommandsEX.getConf().getList("timedPromoteExclude", new ArrayList<String>());
		for (String s : configGroups.getKeys(true)) {
			// ignore default group with time 0, since that one is an example record
			if (s.equals("default") && (f.getInt("timedPromote." + s) == 0)) continue;
			settings.put(s, f.getInt("timedPromote." + s));
		}
		
		// run through all online players and check their playtime
		Iterator<Entry<String, Integer>> it = CommandsEX.playTimes.entrySet().iterator();
		while (it.hasNext()) {
			Player p;
			Integer playerValue;
			if (players.length > 0) {
				p = players[0];
				playerValue = CommandsEX.playTimes.get(p.getName());
			} else {
				Map.Entry<String, Integer> playTimePairs = (Map.Entry<String, Integer>)it.next();
				p = Bukkit.getServer().getPlayer(playTimePairs.getKey());
				playerValue = playTimePairs.getValue();
			}
			String promotedTo = "";
			
			String[] tPlayerGroups = Vault.perms.getPlayerGroups(p);
			List<String> playerGroups = new ArrayList<String>();

			// if the player belongs to at least one excluded group, stop here
			for (String s : tPlayerGroups) {
				if (exclusions.contains(s)) {
					return;
				} else {
					playerGroups.add(s);
				}
			}
			// check player's playtime against config settings
			Iterator<Entry<String, Integer>> it2 = settings.entrySet().iterator();
			while (it2.hasNext()) {
				Map.Entry<String, Integer> configPairs = (Map.Entry<String, Integer>)it2.next();
				if ((playerValue >= configPairs.getValue()) && !playerGroups.contains(configPairs.getKey())) {
					promotedTo = configPairs.getKey();
				}
			}
			
			// if we have a promotion to deliver, do it here
			if (!promotedTo.equals("")) {
				Vault.perms.playerAddGroup(p, promotedTo);
				LogHelper.showInfo("timedPromoteMessage1", p, ChatColor.GREEN);
				LogHelper.showInfo("timedPromoteMessage2#####[" + promotedTo, p, ChatColor.GREEN);
			}
			
			// exit here if we requested a single player
			if (players.length > 0) {
				return;
			}
		}
	}
	
	/***
	 * TIME2RANK - shows how much time has a player left until he'll be auto-promoted to a higher rank
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean time2rank(CommandSender sender, String[] args, String command, String alias) {
		if (!CommandsEX.sqlEnabled) {
			LogHelper.showInfo("playTimeNoSQL", sender, ChatColor.YELLOW);
			return true;
		}
		
		if (!CommandsEX.vaultPresent) {
			// don't show anything when Vault is not present, except for a debug message in console
			LogHelper.logDebug("[CommandsEX] time2rank could not be invoked because Vault is not present");
			return true;
		}
		
		// load up settings from config file
		FileConfiguration f = CommandsEX.getConf();
		ConfigurationSection configGroups = f.getConfigurationSection("timedPromote");
		Player p = (Player)sender;
		Long currentGroupTime = f.getLong("timedPromote." + Vault.perms.getPrimaryGroup(p));
		Long nextGroupTime = 9223372036854775807L; // maximum value of LONG in Java
		String nextRankName = "?";
		for (String s : configGroups.getKeys(true)) {
			// ignore default group with time 0, since that one is an example record
			Long t = f.getLong("timedPromote." + s);
			if (s.equals("default") && (t == 0)) continue;
			
			// check if this group has more time set than current one and set it as the next group's time
			if (t > currentGroupTime) {
				// if our current time for next group is higher than the one we found now, use the one we found,
				// otherwise leave the previous one, since it's closer to our current rank
				if (nextGroupTime > t) {
					nextGroupTime = t;
					nextRankName = s;
				}
			}
		}
		
		if (nextGroupTime == 9223372036854775807L) {
			// there are no higher ranks
			LogHelper.showInfo("timedPromoteHighestRank", sender, ChatColor.GREEN);
		} else {
			// calculate how much time we have left until the next rank
			Long remain = nextGroupTime - CommandsEX.playTimes.get(p.getName());
			Map<String, Integer> m = Utils.parseTimeStamp(remain);
			LogHelper.showInfo("timedPromoteTime2RankLeft#####[" + (m.get("days") + " #####days#####[, ") + (m.get("hours") + " #####hours#####[, ") + (m.get("minutes") + " #####minutes#####[, ") + (m.get("seconds") + " #####seconds"), sender);
			LogHelper.showInfo("timedPromoteTime2RankNextRank#####[" + nextRankName, sender);
		}

		return true;
	}
}