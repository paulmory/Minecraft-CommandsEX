package com.github.zathrus_writer.commandsex.commands;


import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.zathrus_writer.commandsex.helpers.Commands;
import com.github.zathrus_writer.commandsex.helpers.Common;
import com.github.zathrus_writer.commandsex.helpers.Permissions;

public class Command_cex_kick extends Common {
	/***
	 * KICK - kicks a player out from the server, optionally providing a custom reason
	 * @param sender
	 * @param args
	 * @return
	 */
	public static Boolean run(CommandSender sender, String alias, String[] args) {
		// check if we have any parameters
		if (args.length > 0) {
			// check permissions and roll it :)
			Boolean hasPerms = true;
			if (sender instanceof Player) {
				hasPerms = Permissions.checkPerms((Player)sender, "cex.kick");
			}
			
			// permissions ok, kick them out
			if (hasPerms) {
				kick(sender, args, "cex_kick", alias);
			}
		} else {
			// show usage
			Commands.showCommandHelpAndUsage(sender, "cex_kick", alias);
		}
        return true;
	}
}
