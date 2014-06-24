package me.valon.BarnyardBattle.Manager;

import java.io.File;
import java.io.IOException;

import me.valon.BarnyardBattle.Main.BarnyardBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Lobby {

	public BarnyardBattle plugin;
	public Lobby (BarnyardBattle instance){
		plugin = instance;
	}

	public boolean isGameActive(){
		return plugin.gameActive;
	}

	@SuppressWarnings("deprecation")
	public void addPlayer (Player player){
		if (!plugin.inLobby.contains(player.getName())){
			sendToLobby(player);
			plugin.inLobby.add(player.getName());

			player.getInventory().clear();
			player.getInventory().setArmorContents(null);

			for (String s : plugin.inLobby){
				Player lobbyPlayers = plugin.getServer().getPlayer(s);
				lobbyPlayers.sendMessage(player.getName() + " has joined. [" + getLobbySize() + "/" + getRequiredPlayers() + "]");
			}

			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run(){
					if (isLobbyFull()){
						plugin.gameManager.startPreGame();
					}
				}
			}, 20L);

		} else {
			player.sendMessage("You're already in the lobby.");
		}
	}

	@SuppressWarnings("deprecation")
	public void removePlayer (Player player){
		if (plugin.inLobby.contains(player.getName())){
			plugin.inLobby.remove(player.getName());
			player.setExp(0.0f);

			for (String s : plugin.inLobby){
				Player lobbyPlayers = plugin.getServer().getPlayer(s);
				lobbyPlayers.sendMessage(player.getName() + " has left. [" + getLobbySize() + "/" + getRequiredPlayers() + "]");
			}
		}
	}

	public int getRequiredPlayers(){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		return gameFileConfig.getInt("Player-Requirement");
	}

	// Set lobby point
	public void setGameLobbyPoint (Player player){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		String world = player.getWorld().getName();
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();
		float pitch = player.getLocation().getPitch();
		float yaw = player.getLocation().getYaw();

		gameFileConfig.set("Lobby.world", world);
		gameFileConfig.set("Lobby.x", x);
		gameFileConfig.set("Lobby.y", y);
		gameFileConfig.set("Lobby.z", z);
		gameFileConfig.set("Lobby.pitch", pitch);
		gameFileConfig.set("Lobby.yaw", yaw);

		player.sendMessage(plugin.pluginTag + "Lobby point has been set.");

		try {
			gameFileConfig.save(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Set lobby point
	public void setMainSpawnPoint (Player player){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		String world = player.getWorld().getName();
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();
		float pitch = player.getLocation().getPitch();
		float yaw = player.getLocation().getYaw();

		gameFileConfig.set("MainSpawn.world", world);
		gameFileConfig.set("MainSpawn.x", x);
		gameFileConfig.set("MainSpawn.y", y);
		gameFileConfig.set("MainSpawn.z", z);
		gameFileConfig.set("MainSpawn.pitch", pitch);
		gameFileConfig.set("MainSpawn.yaw", yaw);

		player.sendMessage(plugin.pluginTag + "Main spawn location has been set.");

		try {
			gameFileConfig.save(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendToMainSpawnPoint (Player player){

		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		String world = gameFileConfig.getString("MainSpawn.world");
		double x = gameFileConfig.getInt("MainSpawn.x");
		double y = gameFileConfig.getInt("MainSpawn.y");
		double z = gameFileConfig.getInt("MainSpawn.z");
		float pitch = gameFileConfig.getInt("MainSpawn.pitch");
		float yaw = gameFileConfig.getInt("MainSpawn.yaw");

		if (lobbyExists()){
			player.teleport(new Location(plugin.getServer().getWorld(world), x + 0.5, y, z + 0.5, yaw, pitch));
		} else {
			player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Unable to send to main spawn.");
			System.out.println("Error: Unable to send player to main spawn.");
		}
	}

	// Get size of the lobby
	public int getLobbySize(){
		return plugin.inLobby.size();
	}

	// Check if player is in lobby
	public boolean isPlayerInLobby(Player player){
		if (plugin.inLobby.contains(player.getName())){
			return true;
		}
		return false;
	}

	public boolean lobbyExists(){

		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		if (gameFileConfig.get("Lobby.world") != null && gameFileConfig.get("Lobby.x") != null && gameFileConfig.get("Lobby.y") != null 
				&& gameFileConfig.get("Lobby.z") != null && gameFileConfig.get("Lobby.pitch") != null && gameFileConfig.get("Lobby.yaw") != null

				&& gameFileConfig.get("MainSpawn.world") != null && gameFileConfig.get("MainSpawn.x") != null && gameFileConfig.get("MainSpawn.y") != null 
				&& gameFileConfig.get("MainSpawn.z") != null && gameFileConfig.get("MainSpawn.pitch") != null && gameFileConfig.get("MainSpawn.yaw") != null){
			return true;
		}
		return false;
	}

	public void sendToLobby (Player player){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		String world = gameFileConfig.getString("Lobby.world");
		double x = gameFileConfig.getInt("Lobby.x");
		double y = gameFileConfig.getInt("Lobby.y");
		double z = gameFileConfig.getInt("Lobby.z");
		float pitch = gameFileConfig.getInt("Lobby.pitch");
		float yaw = gameFileConfig.getInt("Lobby.yaw");

		player.teleport(new Location(plugin.getServer().getWorld(world), x + 0.5, y, z + 0.5, yaw, pitch));
	}

	// Check if lobby is full
	public boolean isLobbyFull(){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		if (getLobbySize() == gameFileConfig.getInt("Player-Requirement")){
			return true;
		}
		return false;
	}

}
