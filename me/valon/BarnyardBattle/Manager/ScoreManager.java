package me.valon.BarnyardBattle.Manager;

import java.io.File;
import java.io.IOException;

import me.valon.BarnyardBattle.Main.BarnyardBattle;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class ScoreManager {

	public BarnyardBattle plugin;
	public ScoreManager (BarnyardBattle instance){
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	public void setupScoreboard (){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
		Team team = scoreboard.registerNewTeam("team");

		Objective objective = scoreboard.registerNewObjective("test", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Barnyard Battle");

		for (String s : plugin.inGame){
			Player player = plugin.getServer().getPlayer(s);
			team.addPlayer(player);

			Score score = objective.getScore(player);
			score.setScore(gameFileConfig.getInt("Scores." + player.getName()));
		}	

		for (String s : plugin.inGame){
			Player player = plugin.getServer().getPlayer(s);
			player.setScoreboard(scoreboard);
		}
	}

	public void increaseScore (Player player, int scoreIncrement){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		int currentScore = gameFileConfig.getInt("Scores." + player.getName());
		gameFileConfig.set("Scores." + player.getName(), (currentScore + 1));

		try {
			gameFileConfig.save(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setupScoreboard();

		checkScore(player);
	}

	public void checkScore (Player player){
		File gameFile = new File (plugin.getDataFolder(), "Game Configuration");
		FileConfiguration gameFileConfig = YamlConfiguration.loadConfiguration(gameFile);

		int currentScore = gameFileConfig.getInt("Scores." + player.getName());

		if (currentScore >= plugin.scoreRequirement){
			plugin.gameManager.gameOver(player);
		}
	}

	public void clearScoreboard (Player player){
		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		player.setScoreboard(scoreboardManager.getNewScoreboard());

		plugin.gameManager.resetGame();
	}

}
