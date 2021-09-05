package com.akon.fullbright;

import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "Fullbright", version = "1.1")
@Dependency("ProtocolLib")
@Author("akon")
@Commands(@Command(name = "fullbright", permission = "fullbright.command.fullbright", aliases = "fb"))
@Permissions(@Permission(name = "fullbright.command.fullbright", defaultValue = PermissionDefault.TRUE))
public class Fullbright extends JavaPlugin {

	@Getter
	private static Fullbright instance;

	@Override
	public void onEnable() {
		instance = this;
		this.saveDefaultConfig();
		this.getCommand("fullbright").setExecutor(new FullbrightCommand());
		ProtocolLibrary.getProtocolManager().addPacketListener(new FullbrightListener());
		Bukkit.getOnlinePlayers().forEach(FullbrightAPI::updateLighting);
	}

	@Override
	public void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
		Bukkit.getOnlinePlayers().forEach(FullbrightAPI::updateLighting);
	}
}
