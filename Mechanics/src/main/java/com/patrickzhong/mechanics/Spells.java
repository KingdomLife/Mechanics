package com.patrickzhong.mechanics;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Spells {
	Plugin plugin;
	
	public Spells(Plugin plugin){
		this.plugin = plugin;
	}
	public void launchSpell(final Player player, final String name, final Location loc, final Vector direction){
		final double speed; // speed * numPartsPerTick * 20 = blocks/second.
		final int numPartsPerTick; // Number of particles displayed per tick.
		if(name.contains("Wild Banshee")){
			numPartsPerTick = 40;
			speed = 0.025;
		}
		else if(name.contains("Narrowing Corkscrew")){
			numPartsPerTick = 40;
			speed = 0.2;
		}
		else {
			numPartsPerTick = 20;
			speed = 0.1;
		}
		
		if(name.equals("Machination")){
			direction.setX(Math.random()/4.0-1.0/8.0 + direction.getX());
			direction.setY(Math.random()/4.0-1.0/8.0 + direction.getY());
			direction.setZ(Math.random()/4.0-1.0/8.0 + direction.getZ());
			direction.normalize();
			loc.add(0, 0.5, 0);
		}
		
	    final double radius = 0.35;
	    final Integer[] durability = {1};
	    final boolean goesThroughWalls = false;
	    final int range = 28;
	    final Double[] time = {0.0};
	    final Double[] sec = {0.0};
	    new BukkitRunnable(){
	    	public void run(){
		    	for(int i = 0; i < numPartsPerTick; i ++){
		    		Location center = new Location(loc.getWorld(), loc.getX() + direction.getX() * time[0], loc.getY() + direction.getY() * time[0], loc.getZ() + direction.getZ() * time[0]);
		    		
		    		
		    		double x = xPos(name, time[0], range);
		    		double y = yPos(name, time[0], range);
			        double z = x * -Math.sin(Math.PI/180 * loc.getYaw());
			        x *= -Math.cos(Math.PI/180 * loc.getYaw());
			        
			        
			        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.FIREWORKS_SPARK, true, (float) (center.getX() + x), (float) (center.getY() + y), (float) (center.getZ() + z), 0f, 0f, 0f, 0f, 1);
			        Location partLoc = new Location(center.getWorld(), center.getX()+x, center.getY()+y, center.getZ()+z);
			        
			        for(Player online : loc.getWorld().getPlayers()) {
			            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
			        }
			        
			        if(!goesThroughWalls && !partLoc.getWorld().getBlockAt(partLoc).getType().equals(Material.AIR))
						this.cancel();
					
					for(Entity ent : partLoc.getChunk().getEntities()){
						if(!ent.equals(player)){
							if(ent instanceof LivingEntity && isColliding(partLoc, ent)){
								EntityDamageByEntityEvent damEv = new EntityDamageByEntityEvent(player, ent, DamageCause.MAGIC, 25);
								Bukkit.getServer().getPluginManager().callEvent(damEv);
								if(!damEv.isCancelled())
									((Damageable)ent).damage(25);
								durability[0] --;
								if(durability[0] <= 0)
									this.cancel();
							}
						}
					}
					
					if(name.equals("Double Helix")){
						double sx = xPos(name, time[0]+Math.PI, range);
			    		double sy = yPos(name, time[0]+Math.PI, range);
				        double sz = sx * -Math.sin(Math.PI/180 * loc.getYaw());
				        sx *= -Math.cos(Math.PI/180 * loc.getYaw());
				        
				        PacketPlayOutWorldParticles spacket = new PacketPlayOutWorldParticles(EnumParticle.FIREWORKS_SPARK, true, (float) (center.getX() + sx), (float) (center.getY() + sy), (float) (center.getZ() + sz), 0f, 0f, 0f, 0f, 1);
				        Location spartLoc = new Location(center.getWorld(), center.getX()+sx, center.getY()+sy, center.getZ()+sz);
				        
				        for(Player online : loc.getWorld().getPlayers()) {
				            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(spacket);
				        }
				        
				        if(!goesThroughWalls && !spartLoc.getWorld().getBlockAt(spartLoc).getType().equals(Material.AIR))
							this.cancel();
						
						for(Entity ent : spartLoc.getChunk().getEntities()){
							if(!ent.equals(player)){
								if(ent instanceof LivingEntity && isColliding(spartLoc, ent)){
									EntityDamageByEntityEvent damEv = new EntityDamageByEntityEvent(player, ent, DamageCause.MAGIC, 25);
									Bukkit.getServer().getPluginManager().callEvent(damEv);
									if(!damEv.isCancelled())
										((Damageable)ent).damage(25);
									durability[0] --;
									if(durability[0] <= 0)
										this.cancel();
								}
							}
						}
					}
					
			        time[0] += speed;
			        //player.sendMessage(ChatColor.GRAY+""+time[0]);
			        if(Math.sqrt(Math.pow(direction.getX() * time[0], 2) + Math.pow(direction.getY() * time[0], 2) + Math.pow(direction.getZ() * time[0], 2)) >= range)
			        	this.cancel();
		    	}
		    	
		    	//sec[0]+=0.05;
		    	//player.sendMessage(ChatColor.GRAY+""+sec[0]+","+time[0]+","+Math.sqrt(Math.pow(direction.getX() * time[0], 2) + Math.pow(direction.getY() * time[0], 2) + Math.pow(direction.getZ() * time[0], 2)));
	    	}
	    }.runTaskTimer(plugin, 0, 1);
	    
	    
	}
	
	private boolean isColliding(Location loc, Entity ent){
		Location entLoc = ent.getLocation();
		boolean x = Math.abs(loc.getX()-entLoc.getX()) <= ((CraftLivingEntity)ent).getHandle().width/2*Math.sqrt(2);
		boolean y = loc.getY() >= entLoc.getY() && loc.getY()-entLoc.getY() <= ((LivingEntity)ent).getEyeHeight()*5/4;
		boolean z = Math.abs(loc.getZ()-entLoc.getZ()) <= ((CraftLivingEntity)ent).getHandle().width/2*Math.sqrt(2);
		
		return x && y && z;
	}
	
	private double xPos(String name, double t, double range){
		double x = 0;
		
		if(name.equals("Narrowing Corkscrew")){
			x = (1-t/range)*1 * Math.sin(t);
		}
		else if(name.contains("Helix")){
			x = 0.35 * Math.sin(t);
		}
		else if(name.equals("Bouncing Arc")){
			x = 0.75 * Math.sin(t);
		}
		else if(name.equals("Lemniscate")){
			double temp = Math.cos(t)/2;
			x = Math.sqrt(Math.abs(temp)) * Math.cos(t);
		}
		else if(name.equals("Wild Banshee")){
			x = (0.5+1*Math.cos(4*t)) * Math.cos(t);
		}
		else if(name.equals("Mist")){
			x = Math.random()*2-1;
		}
		
		return x;
	}
	
	private double yPos(String name, double t, double range){
		double y = 0;
		
		if(name.equals("Narrowing Corkscrew")){
			y= (1-t/range)*1 * Math.cos(t);
		}
		else if(name.contains("Helix")){
			y= 0.35 * Math.cos(t);
		}
		else if(name.equals("Bouncing Arc")){
			y= Math.abs(0.75 * Math.cos(t));
		}
		else if(name.equals("Lemniscate")){
			double temp = Math.cos(t)/2;
			y = Math.signum(temp) * Math.sqrt(Math.abs(temp)) * Math.sin(t);
		}
		else if(name.equals("Wild Banshee")){
			y = (0.5+1*Math.cos(4*t)) * Math.sin(t);
		}
		else if(name.equals("Mist")){
			y = Math.random()*2-1;
		}
		return y;
	}
}
