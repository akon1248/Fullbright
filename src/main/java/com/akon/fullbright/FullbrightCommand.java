package com.akon.fullbright;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FullbrightCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player) || args.length != 0) {
			return false;
		}
		Player player = (Player)sender;
		FullbrightAPI.setFullbright(player, !FullbrightAPI.hasFullbright(player));
		sender.sendMessage(ChatColor.GRAY + "Fullbright: " + (FullbrightAPI.hasFullbright(player) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
 		return true;
	}

}
