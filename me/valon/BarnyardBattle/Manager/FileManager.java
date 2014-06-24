package me.valon.BarnyardBattle.Manager;

import java.io.File;
import java.io.IOException;

import me.valon.BarnyardBattle.Main.BarnyardBattle;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class FileManager {

	public BarnyardBattle plugin;
	public FileManager (BarnyardBattle instance){
		plugin = instance;
	}

	public void createGameFile(Player player, int playerRequirement, int scoreRequirement){

		if (playerRequirement > 1){

			File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
			FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

			plugin.gameActive = false;
			plugin.playerRequirement = playerRequirement;
			plugin.scoreRequirement = scoreRequirement;
			gameFileConfig.set("Scores", "");

			try {
				gameFileConfig.save(gameFile);
			} catch (IOException e){
				return;
			}
		} else {
			player.sendMessage("A game must have a player requirement of at least 2");
		}
	}	





}


