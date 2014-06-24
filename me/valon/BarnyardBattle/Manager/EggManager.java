package me.valon.BarnyardBattle.Manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.valon.BarnyardBattle.Main.BarnyardBattle;

public class EggManager {

	public BarnyardBattle plugin;
	public EggManager (BarnyardBattle instance){
		plugin = instance;
	}
	
	public boolean canThrowEgg (Player player){
		if (plugin.canThrow.contains(player.getName())){
			if (player.getExp() == 1.0000001f){
				return true;
			}
		}
		return false;
	}
	
	public void increaseXpBar (final Player player){
		if (player.getExp() < 1.0f){
			player.setExp(player.getExp() + 0.1f);
			
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run(){
					increaseXpBar(player);
				}
			}, 2L);
		}
	}
	
}
