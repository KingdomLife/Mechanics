package com.patrickzhong.mechanics;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.patrickzhong.kingdomlifeapi.KingdomLifeAPI;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class Mechanics extends JavaPlugin implements Listener{
	Plugin plugin;
	BukkitTask timer;
	BukkitTask healCoolTimer;
	//private KingdomLifeAPI kLifeAPI;
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		
		plugin = this;
		
		new BukkitRunnable(){
			public void run(){
				heal();
			}
		}.runTaskTimer(this, 100, 100);
		
		/*new BukkitRunnable(){
			public void run(){
				if (!setUpKingdomLifeAPI() ) {
		            getLogger().severe(String.format("[%s] - Disabled due to no KingdomLifeAPI found!", getDescription().getName()));
		            getServer().getPluginManager().disablePlugin(plugin);
		            return;
		        }
				getLogger().info("Mechanics enabled successfully.");
			}
		}.runTaskLater(plugin, 1);
		
		
		new BukkitRunnable(){
			public void run(){
				if(!getServer().getPluginManager().isPluginEnabled("KingdomLifeAPI")){
					kLifeAPI = null;
				}else if(kLifeAPI == null){
					kLifeAPI = (KingdomLifeAPI)getServer().getPluginManager().getPlugin("KingdomLifeAPI");
				}
			}
		}.runTaskTimer(plugin, 0, 1);
		*/
	}
	/*
	private boolean setUpKingdomLifeAPI(){
		if (getServer().getPluginManager().getPlugin("KingdomLifeAPI") == null) {
            return false;
        }
		RegisteredServiceProvider<KingdomLifeAPI> rsp = getServer().getServicesManager().getRegistration(KingdomLifeAPI.class);
        if (rsp == null) {
            return false;
        }
        kLifeAPI = rsp.getProvider();
        return kLifeAPI != null;
        
		kLifeAPI = (KingdomLifeAPI)getServer().getPluginManager().getPlugin("KingdomLifeAPI");
		return kLifeAPI != null;
    }
    */
	
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
			
			if(healCoolTimer == null)
				player.setMetadata("healCool", new FixedMetadataValue(this, false));
			else
				healCoolTimer.cancel();
			
			healCoolTimer = new BukkitRunnable(){
				public void run(){
					player.setMetadata("healCool", new FixedMetadataValue(plugin, true));
					healCoolTimer = null;
					this.cancel();
				}
			}.runTaskLater(this, 100);
			
		}
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent ev){
		Inventory inv = ev.getInventory();
		if(inv.getTitle().equalsIgnoreCase("Character Selection") && KingdomLifeAPI.type(ev.getPlayer().getUniqueId().toString()).equals(""))
			ev.getPlayer().openInventory(inv);
	}
	
	@EventHandler
	public void onStickClick(PlayerInteractEvent ev){
		final Player player = ev.getPlayer();
		ItemStack hand = player.getInventory().getItemInHand();
		if(hand == null || !hand.getType().equals(Material.STICK) || !hand.hasItemMeta() || !hand.getItemMeta().hasLore())
			return;
		List<String> lores = hand.getItemMeta().getLore();
		int attack = 0;
		for(int a = 0; a < lores.size(); a++){
			String loreLine = ChatColor.stripColor(lores.get(a));
			if(loreLine.contains("Attack")){
				String attStr = loreLine.substring(loreLine.indexOf(":")+2);
				if(!attStr.contains("-"))
					attack = Integer.parseInt(attStr);
				else {
					int first = Integer.parseInt(attStr.split("-")[0]);
					int last = Integer.parseInt(attStr.split("-")[1]);
					attack = (int)Math.floor(Math.random()*(last-first+1)+first);
				}
					
			}else if(loreLine.contains("Min. Level")){
				int minLevel = Integer.parseInt(loreLine.substring(loreLine.indexOf(":")+2));
				if(KingdomLifeAPI.level(player.getUniqueId().toString(), KingdomLifeAPI.type(player.getUniqueId().toString())) >= minLevel){
					ev.setCancelled(true);
					EnumParticle[] particles = {EnumParticle.CLOUD, EnumParticle.CRIT_MAGIC, EnumParticle.SPELL_WITCH, EnumParticle.VILLAGER_HAPPY};
					createHelix(player, particles[(int)Math.floor(Math.random()*particles.length)], attack);
					return;
				}else {
					player.sendMessage(ChatColor.RED+"You must be of level "+minLevel+" to use this weapon!");
					return;
				}
			}
		}
	
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent ev){
		final Location loc = ev.getTo();
		final double x = loc.getX();
		final double y = loc.getY();
		final double z = loc.getZ();
		final double maxRadius = 4.0;
		final Double[] time = {0.0};
		
		new BukkitRunnable(){
			public void run(){
				double radius = radius(time[0], maxRadius);
				
				for(double i = 0; i < Math.PI*2; i+=Math.PI/20){
					float newX = (float)(xLoc(i, radius) + x);
					float newY = (float)(y+maxRadius-time[0]);
					float newZ = (float)(zLoc(i, radius) + z);
					
					PacketPlayOutWorldParticles packet= new PacketPlayOutWorldParticles(EnumParticle.CRIT_MAGIC, true, newX, newY, newZ, 0f, 0f, 0f, 0f, 1);
					for(Player player : Bukkit.getServer().getOnlinePlayers()){
						((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
					}
				}
				
				time[0] += 0.2;
				if(radius >= maxRadius)
					this.cancel();
			}
		}.runTaskTimer(plugin, 0, 2);
	}
	
	private double xLoc(double time, double radius){
		return Math.sin(time) * radius;
	}
	
	private double zLoc(double time, double radius){
		return Math.cos(time) * radius;
	}
	
	private double radius(double time, double maxRadius){
		return Math.sqrt(Math.pow(maxRadius,2) - Math.pow(time-maxRadius, 2));
	}
	
	private void heal(){
		Player[] playerList = Bukkit.getServer().getOnlinePlayers();
		for(Player player: playerList){
			if(!player.hasMetadata("healCool") || player.getMetadata("healCool").size() == 0 || player.getMetadata("healCool").get(0).asBoolean()){
				if(player.getHealth() >= 0.9*player.getMaxHealth())
					player.setHealth(player.getMaxHealth());
				else
					player.setHealth(player.getHealth()+0.1*player.getMaxHealth());
			}
		}
	}
	
	private double xPos(double velocity, double yaw, double pitch, double time, double initialX){
		return -1 * velocity * Math.cos(Math.PI/180*pitch) * time * Math.sin(Math.PI/180*yaw) + initialX;
	}
	
	private double yPos(double velocity, double yaw, double pitch, double time, double initialY, double gravity){
		return 0.5 * gravity * Math.pow(time, 2) - velocity * Math.sin(Math.PI/180*pitch) * time + initialY;
	}
	
	private double zPos(double velocity, double yaw, double pitch, double time, double initialZ){
		return velocity * Math.cos(Math.PI/180*pitch) * time * Math.cos(Math.PI/180*yaw) + initialZ;
	}
	
	
	private void createHelix(final Player player, final EnumParticle particle, final int damage) {
		final Location loc = player.getLocation();
		loc.add(0, 0.5, 0);
		final Vector direction = player.getLocation().getDirection();
	    final double radius = 0.25;
	    final int range = 7;
	    final Double[] time = {0.0};
	    new BukkitRunnable(){
	    	public void run(){
		    	for(int i = 0; i < 20; i ++){
		    		Location center = new Location(loc.getWorld(), loc.getX() + direction.getX() * time[0], loc.getY() + direction.getY() * time[0], loc.getZ() + direction.getZ() * time[0]);
		    		double x = radius * Math.sin(time[0]) * Math.cos(Math.PI/180 * loc.getYaw());
		    		double y = radius * Math.cos(time[0]);
			        //double z = time[0];
			        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, true, (float) (center.getX() + x), (float) (center.getY() + y), (float) (center.getZ()), 0f, 0f, 0f, 0f, 1);
			        for(Player online : Bukkit.getOnlinePlayers()) {
			            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
			        }
			        
			        if(!center.getWorld().getBlockAt(center).getType().equals(Material.AIR))
						this.cancel();
					
					for(Entity ent : center.getChunk().getEntities()){
						if(!ent.equals(player)){
							Location entLoc = ent.getLocation();
							boolean closeX = ((center.getX() + x) <= entLoc.getX()+radius*3 && (center.getX() + x) >= entLoc.getX()-radius*3);
							boolean closeY = ((center.getY() + y) <= entLoc.getY()+radius*3*2 && (center.getY() + y) >= entLoc.getY()-radius*3/2);
							boolean closeZ = (center.getZ() <= entLoc.getZ()+radius*3 && center.getZ() >= entLoc.getZ()-radius*3);
							if(closeX && closeY && closeZ){
								((Damageable)ent).damage(damage);
								this.cancel();
							}
						}
					}
					
			        time[0] += 0.1;
			        if(Math.sqrt(Math.pow(center.getX(), 2) + Math.pow(center.getY(), 2) + Math.pow(center.getZ(), 2)) >= range)
			        	this.cancel();
		    	}
	    	}
	    }.runTaskTimer(this, 0, 1);
	}
	
	private void straightSpell(final Player player){
		final Double[] time = new Double[1];
		final Location[] loc = new Location[1];
		time[0] = 0.0;
		final Location location = player.getLocation();
		loc[0] = new Location(location.getWorld(), location.getX(), location.getY()+0.75, location.getZ());
		
		final double yaw = location.getYaw();
		final double pitch = location.getPitch();
		final long period = 1;
		final double range = 20;
		final double splash = 1;
		final double velocity = 20; // Blocks per two seconds
		timer = new BukkitRunnable(){
			public void run(){
				double x = xPos(velocity, yaw, pitch, time[0], loc[0].getX());
				double y = yPos(velocity, yaw, pitch, time[0], loc[0].getY(), 0);
				double z = zPos(velocity, yaw, pitch, time[0], loc[0].getZ());
				Location newLoc = new Location(player.getWorld(), x, y, z);
				
				if(!newLoc.getWorld().getBlockAt(newLoc).getType().equals(Material.AIR))
					this.cancel();
				
				for(Entity ent : newLoc.getChunk().getEntities()){
					if(!ent.equals(player)){
						Location entLoc = ent.getLocation();
						boolean closeX = (x <= entLoc.getX()+splash && x >= entLoc.getX()-splash);
						boolean closeY = (y <= entLoc.getY()+splash*2 && y >= entLoc.getY()-splash/2);
						boolean closeZ = (z <= entLoc.getZ()+splash && z >= entLoc.getZ()-splash);
						if(closeX && closeY && closeZ){
							((Damageable)ent).damage(2);
							this.cancel();
						}
					}
				}
				newLoc.getWorld().playEffect(newLoc, Effect.POTION_BREAK, 8235);
				time[0] = time[0] + period/20.0;
				if(Math.sqrt(Math.pow(location.getX()-x, 2)+Math.pow(location.getZ()-z, 2)) >= range)
					this.cancel();
			}
		}.runTaskTimer(this, 0, period);
		
	}
}
