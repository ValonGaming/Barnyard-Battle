package me.valon.BarnyardBattle.Manager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import me.valon.BarnyardBattle.Main.BarnyardBattle;
import me.valon.BarnyardBattle.Particles.ParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameManager implements Listener {

	public BarnyardBattle plugin;
	public GameManager (BarnyardBattle instance){
		plugin = instance;
	}

	int remainingTime = 0;

	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// Lobby ///////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	// Add Player to the lobby
	public void addPlayer (Player player){
		plugin.inGame.add(player.getName());
	}

	// Remove player from the game
	public void removePlayer (Player player){
		if (plugin.inGame.contains(player.getName())){
			plugin.inGame.remove(player.getName());

			player.setExp(0.0f);
			player.getInventory().clear();

			if (getGameSize() == 0){
				plugin.gameActive = false;
			}	
		} 
	}

	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// Game ////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	// Get size of the game
	public int getGameSize(){
		return plugin.inGame.size();
	}

	// Check if game is active
	public boolean isGameActive(){
		return plugin.gameActive;
	}

	// set whether the game is active
	public void setGameActive (String active){
		if (active.equalsIgnoreCase("false")){
			plugin.gameActive = false;
		} else if (active.equalsIgnoreCase("true")){
			plugin.gameActive = true;
		}
	}

	// Start the Pre Game
	@SuppressWarnings("deprecation")
	public void startPreGame (){

		setGameActive("true");

		for (String s : plugin.inLobby){
			Player player = plugin.getServer().getPlayer(s);
			plugin.inGame.add(player.getName());

			int spawnPoint = plugin.inLobby.indexOf(player.getName()) + 1;
			spawnPlayer(player, spawnPoint);

		}
		remainingTime = 10;
		runGameCountdown();
	}

	// Start the actual game
	@SuppressWarnings("deprecation")
	public void startGame(){
		for (String s : plugin.inGame){
			Player player = plugin.getServer().getPlayer(s);
			player.sendMessage("GO!");
			setupPlayerInv();
			plugin.canThrow.add(player.getName());
		}
	}

	// Run the game countdown
	@SuppressWarnings("deprecation")
	public void runGameCountdown(){
		String message = remainingTime + " seconds remaining until start!";

		for (String s : plugin.inGame){

			Player player = plugin.getServer().getPlayer(s);

			if (remainingTime != 0){
				if (remainingTime == 10){
					player.sendMessage(message);
				} else if (remainingTime < 6 && remainingTime > 0){
					player.sendMessage(message);
				}
			} else {
				startGame();
			}
		}		

		remainingTime--;
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run(){
				runGameCountdown();
			}
		}, 20L);

	}

	public void setSpawnpoint (Player player, int spawnpoint){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		String world = player.getWorld().getName();
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();
		float pitch = player.getLocation().getPitch();
		float yaw = player.getLocation().getYaw();

		gameFileConfig.set("Spawnpoint." + spawnpoint + ".world", world);
		gameFileConfig.set("Spawnpoint." + spawnpoint + ".x", x);
		gameFileConfig.set("Spawnpoint." + spawnpoint + ".y", y);
		gameFileConfig.set("Spawnpoint." + spawnpoint + ".z", z);
		gameFileConfig.set("Spawnpoint." + spawnpoint + ".pitch", pitch);
		gameFileConfig.set("Spawnpoint." + spawnpoint + ".yaw", yaw);

		player.sendMessage(plugin.pluginTag + "Spawnpoint " +  spawnpoint + " has been set.");

		try {
			gameFileConfig.save(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Check if game is missing information
	public boolean gameHasAllInfo(){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		if (gameFileConfig.get("Player-Requirement") != null &&
				gameFileConfig.get("Score-Requirement") != null &&
				gameFileConfig.get("Lobby") != null &&
				gameFileConfig.get("Spawnpoint") != null){
			return true;
		}
		return false;
	}

	// Clear game information for new game
	public void resetGame(){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		plugin.inGame.clear();
		plugin.gameActive = false;
		gameFileConfig.set("Scores", "");

		try {
			gameFileConfig.save(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void gameOver (Player winningPlayer){
		for (String s : plugin.inGame){
			final Player player = plugin.getServer().getPlayer(s);
			player.getInventory().clear();
			player.setExp(0.0f);

			player.sendMessage(plugin.pluginTag + winningPlayer.getName() + " wins!");

			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run(){
					removePlayer(player);
					plugin.lobbyManager.removePlayer(player);

					plugin.scoreManager.clearScoreboard(player);
					plugin.lobbyManager.sendToMainSpawnPoint(player);
				}
			}, 100L);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Spawning Mechanics /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	// Spawn player
	public void spawnPlayer (Player player, Integer spawnPoint){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		String world = gameFileConfig.getString("Spawnpoint." + spawnPoint + ".world");
		double x = gameFileConfig.getInt("Spawnpoint." + spawnPoint + ".x");
		double y = gameFileConfig.getInt("Spawnpoint." + spawnPoint + ".y");
		double z = gameFileConfig.getInt("Spawnpoint." + spawnPoint + ".z");
		float pitch = gameFileConfig.getInt("Spawnpoint." + spawnPoint + ".pitch");
		float yaw = gameFileConfig.getInt("Spawnpoint." + spawnPoint + ".yaw");

		player.teleport(new Location(plugin.getServer().getWorld(world), x + 0.5, y, z + 0.5, yaw, pitch));

		plugin.scoreManager.setupScoreboard();
	}

	public void respawn (Player player){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		Random random = new Random();
		int high = gameFileConfig.getConfigurationSection("Spawnpoint").getKeys(false).size();
		int n = random.nextInt(high) + 1;
		spawnPlayer(player, n);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Egg Mechanics ////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	// When player throws an egg
	@EventHandler
	public void onEggThrow(ProjectileLaunchEvent event){
		if (event.getEntity() instanceof Egg){
			if (event.getEntity().getShooter() instanceof Player){
				Player player = (Player) event.getEntity().getShooter();
				if (plugin.gameActive == true && plugin.eggManager.canThrowEgg(player)){
					player.setExp(0.0f);
					plugin.eggManager.increaseXpBar(player);
				} else {
					event.setCancelled(true);
				}
				player.getItemInHand().setAmount(player.getItemInHand().getAmount() + 1);
			}
		}	
	}

	@EventHandler
	public void onPlayerEggThrow(PlayerEggThrowEvent event){
		event.setHatching(false);
	}

	// When an egg hits anything other than player
	@EventHandler
	public void onEggHit (ProjectileHitEvent event) {
		if (event.getEntity() instanceof Egg){
			if (event.getEntity().getShooter() instanceof Player){
				Player player = (Player) event.getEntity().getShooter();
				if (plugin.inGame.contains(player.getName()) && plugin.gameActive == true){
					Location loc = event.getEntity().getLocation();
					ParticleEffect.CRIT.display(loc, 0, 0, 0, 1, 30);
				}
			}
		}
	}

	// When an egg hits a player
	@EventHandler
	public void onPlayerHit (EntityDamageByEntityEvent event){
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile){
			Projectile projectile = (Projectile) event.getDamager();
			if (projectile.getShooter() instanceof Player){
				Player player = (Player) event.getEntity();
				Player shooter = (Player) projectile.getShooter();

				if (isPlayerInGame(player) && isPlayerInGame(shooter)){

					playerHit(player, shooter);
					respawn(player);
					event.setCancelled(true);
				}
			}				
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event){
		event.setCancelled(true);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Player ///////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	public void playerHit (Player playerHit, Player playerHitBy){
		Location loc = playerHit.getLocation();

		playerHit.getWorld().playSound(loc, Sound.CHICKEN_HURT, 1, 1);
		ParticleEffect.LARGE_SMOKE.display(loc, 0.1f, 0.1f, 0.1f, 1, 30);

		plugin.scoreManager.increaseScore(playerHitBy, 1);
	}

	public void setPosition (Player player){

		File playerFile = new File (plugin.getDataFolder(), "Player Configuration");
		FileConfiguration playerFileConfig = YamlConfiguration.loadConfiguration(playerFile);

		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);
		List<String> lobby = gameFileConfig.getStringList("InLobby");

		playerFileConfig.set(player.getName() + ".position", lobby.size());

		try {
			playerFileConfig.save(playerFile);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	public int getPosition (Player player){
		File playerFile = new File (plugin.getDataFolder(), "Player Configuration");
		FileConfiguration playerFileConfig = YamlConfiguration.loadConfiguration(playerFile);

		return playerFileConfig.getInt(player.getName() + ".position"); 
	}

	public boolean isPlayerInGame (Player player){
		if (plugin.gameActive == true && plugin.inGame.contains(player.getName())){
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void setupPlayerInv(){
		ItemStack egg = new ItemStack (Material.EGG, 64);
		ItemMeta eggMeta = egg.getItemMeta();
		eggMeta.setDisplayName(ChatColor.RESET + "Eggsplosive");
		egg.setItemMeta(eggMeta);

		for (String s : plugin.inGame){
			Player player = plugin.getServer().getPlayer(s);
			player.getInventory().setItem(4, egg);
			player.getInventory().setHeldItemSlot(4);
		}
	}

	// Player Listeners
	@EventHandler @SuppressWarnings("deprecation")
	public void onPlayerLeave(PlayerQuitEvent event){
		Player player = event.getPlayer();
		plugin.lobbyManager.removePlayer(player);
		removePlayer(player);

		if (getGameSize() == 1){
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run(){
					Player winningPlayer = plugin.getServer().getPlayer(plugin.inGame.get(0));
					plugin.gameManager.gameOver(winningPlayer);
				}
			}, 10L);
		}
	}
}