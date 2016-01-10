package com.patrickzhong.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Mechanics extends JavaPlugin implements Listener{
	Plugin plugin;
	BukkitTask timer;
	
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		plugin = this;
		
		new BukkitRunnable(){
			public void run(){
				heal();
			}
		}.runTaskTimer(this, 100, 100);
		
		getLogger().info("Mechanics enabled successfully.");
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev){
		Entity ent = ev.getEntity();
		if(ent instanceof Arrow)
			ent.remove();
	}
	
	@EventHandler
	public void onPlayerAutoHeal(EntityRegainHealthEvent ev){
		if(ev.getEntity() instanceof Player && ev.getRegainReason().equals(RegainReason.SATIATED))
			ev.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerHurt(EntityDamageEvent ev){
		if(ev.getEntity() instanceof Player){
			final Player player = (Player) ev.getEntity();
			player.setMetadata("healCool", new FixedMetadataValue(this, false));
			
			new BukkitRunnable(){
				public void run(){
					player.setMetadata("healCool", new FixedMetadataValue(plugin, true));
				}
			}.runTaskLater(this, 100);
		}
	}
	
	@EventHandler
	public void onStickClick(PlayerInteractEvent ev){
		if(ev.getAction().equals(Action.LEFT_CLICK_AIR)){
			final Player player = ev.getPlayer();
			ItemStack hand = player.getInventory().getItemInHand();
			if(hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName())
				return;
			if(hand.getItemMeta().getDisplayName().contains("MAGE STICKEY TEST")){
				if(!player.hasMetadata("mageOn") || !player.getMetadata("mageOn").get(0).asBoolean()){
					final Double[] time = new Double[1];
					final Location[] loc = new Location[1];
					time[0] = 0.0;
					loc[0] = player.getLocation();
					final double yaw = loc[0].getYaw();
					final double pitch = loc[0].getPitch();
					
					final double velocity = 1;
					timer = new BukkitRunnable(){
						public void run(){
							double x = xPos(velocity, yaw, pitch, time[0], loc[0].getX());
							double y = yPos(velocity, yaw, pitch, time[0], loc[0].getY(), 2);
							double z = xPos(velocity, yaw, pitch, time[0], loc[0].getZ());
							Location newLoc = new Location(player.getWorld(), x, y, z);
							player.getWorld().playEffect(newLoc, Effect.MOBSPAWNER_FLAMES, 0);
							time[0] = time[0] + 1;
							loc[0] = newLoc;
						}
					}.runTaskTimer(this, 0, 10);
				}else {
					timer.cancel();
				}
			}
		}
	}
	
	private void heal(){
		Player[] playerList = Bukkit.getServer().getOnlinePlayers();
		for(Player player: playerList){
			if(!player.hasMetadata("healCool") || player.getMetadata("healCool").get(0).asBoolean()){
				if(player.getHealth() >= 0.9*player.getMaxHealth())
					player.setHealth(player.getMaxHealth());
				else
					player.setHealth(player.getHealth()+0.1*player.getMaxHealth());
			}
		}
	}
	
	private double xPos(double velocity, double yaw, double pitch, double time, double initialX){
		return -1 * velocity * Math.cos(pitch) * time * Math.sin(yaw) + initialX;
	}
	
	private double yPos(double velocity, double yaw, double pitch, double time, double initialY, double gravity){
		return 1/2 * gravity * Math.pow(time, 2) - velocity * Math.sin(pitch) * time + initialY;
	}
	
	private double zPos(double velocity, double yaw, double pitch, double time, double initialZ){
		return -1 * velocity * Math.cos(pitch) * time * Math.cos(yaw) + initialZ;
	}
}
