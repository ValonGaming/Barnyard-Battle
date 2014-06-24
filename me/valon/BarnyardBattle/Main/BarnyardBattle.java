package me.valon.BarnyardBattle.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.valon.BarnyardBattle.Manager.EggManager;
import me.valon.BarnyardBattle.Manager.FileManager;
import me.valon.BarnyardBattle.Manager.GameManager;
import me.valon.BarnyardBattle.Manager.Lobby;
import me.valon.BarnyardBattle.Manager.ScoreManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BarnyardBattle extends JavaPlugin {

	public FileManager fileManager = new FileManager(this);
	public EggManager eggManager = new EggManager(this);
	public GameManager gameManager = new GameManager(this);
	public ScoreManager scoreManager = new ScoreManager(this);
	public Lobby lobbyManager = new Lobby (this);

	public String pluginTag = "[BarnyardBattle] ";
	
	public File BBPlayerConfig = new File (getDataFolder(), "Player Configuration");
	public File BBGameConfig = new File (getDataFolder() + File.separator + "Game Configuration");
	FileConfiguration BBGameConfiguration = YamlConfiguration.loadConfiguration(BBGameConfig);
	
	public boolean gameActive;
	public ArrayList<String> inGame = new ArrayList<String>();
	public ArrayList<String> inLobby = new ArrayList<String>();
	public ArrayList<String> canThrow = new ArrayList<String>();

	public int playerRequirement;
	public int scoreRequirement;
	
	public void onEnable(){

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new GameManager(this), this);

		if (!BBPlayerConfig.exists()){
			try {
				BBPlayerConfig.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!BBGameConfig.exists()){
			try {
				BBGameConfig.createNewFile();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// OnCommand
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("bb")){

			if (sender instanceof Player){
				Player player = (Player)sender;
				if (args.length == 1){
					if (args[0].equalsIgnoreCase("join")){
						lobbyManager.addPlayer(player);
					} else if (args[0].equalsIgnoreCase("leave")){
						lobbyManager.removePlayer(player);
						gameManager.removePlayer(player);
						scoreManager.clearScoreboard(player);
						lobbyManager.sendToMainSpawnPoint(player);
					} else if (args[0].equalsIgnoreCase("start")){
						if (player.hasPermission("barnyardbattle.command")){
							gameManager.startPreGame();
						}
					} 
				} else if (args.length == 2){
					if (args[0].equalsIgnoreCase("set")){
						if (args[1].equalsIgnoreCase("lobby")){
							lobbyManager.setGameLobbyPoint(player);
						} else if (args[1].equalsIgnoreCase("spawn")){
							lobbyManager.setMainSpawnPoint(player);
						}
					}
				} else if (args.length == 3){
					if (args[0].equalsIgnoreCase("set")){
						if (args[1].equalsIgnoreCase("spawnpoint")){
							gameManager.setSpawnpoint(player, Integer.valueOf(args[2]));
						}
					} else if (args[0].equalsIgnoreCase("create")){
						fileManager.createGameFile(player, Integer.valueOf(args[1]), Integer.valueOf(args[2]));
					}
				}
			}
		}
		return true;
	}







}