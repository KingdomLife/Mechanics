package com.patrickzhong.mechanics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.patrickzhong.kingdomlifeapi.KingdomLifeAPI;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.ChatHoverable;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class Mechanics extends JavaPlugin implements Listener{
	Plugin plugin;
	BukkitTask timer;
	BukkitTask healCoolTimer;
	KingdomLifeAPI kLifeAPI;
	List<Player> locked = new ArrayList<Player>();
	Spells spells;
	ChatBot jimmy;
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		
		plugin = this;
		spells = new Spells(this);
		jimmy = new ChatBot("Helpful", this);
		
		new BukkitRunnable(){
			public void run(){
				kLifeAPI = (KingdomLifeAPI)getServer().getPluginManager().getPlugin("KingdomLifeAPI");
				getLogger().info("Mechanics enabled successfully.");
			}
		}.runTaskLater(plugin, 1);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("johnCena")){
			Player player = (Player)sender;
			Location loc = player.getLocation();
			PacketPlayOutNamedSoundEffect packet= new PacketPlayOutNamedSoundEffect("random.bow", loc.getX(), loc.getY(), loc.getZ(), 1, 1);
			((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("class")){
			Player player = (Player)sender;
			
			openClassSelection(player);
			
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("lightshow")){
			if(args.length < 2)
				return false;
			
			final Player player = (Player)sender;
			final Location loc = player.getTargetBlock((HashSet<Byte>)null, 100).getLocation().add(0, 3, 0);
			
			final double[] time = {Double.parseDouble(args[1])};
			final double radius = Double.parseDouble(args[0]);
			final String[] names = {"Helix", "Narrowing Corkscrew", "Bouncing Arc", "Lemniscate", "Double Helix", "Wild Banshee", "Mist", "Mist", "Machination", "Machination", "Machination"};
			
			new BukkitRunnable(){
				public void run(){
					
					Location spawn = new Location(loc.getWorld(), loc.getX() + Math.random()*radius*2-radius, loc.getY(), loc.getZ() + Math.random()*radius*2-radius);
					Vector direction = new Vector(0,1,0);
					
					direction.setX(Math.random()/4.0-1.0/8.0 + direction.getX());
					direction.setY(Math.random()/4.0-1.0/8.0 + direction.getY());
					direction.setZ(Math.random()/4.0-1.0/8.0 + direction.getZ());
					//names[(int)(Math.random()*names.length)]
					spells.launchSpell(player, "Machination", spawn, direction);
					
					time[0] -= 0.1;
					
					if(time[0] <= 0)
						this.cancel();
				}
			}.runTaskTimer(this, 0, 2);
			
			return true;
		}
		return false;
	}
	
	private double round(double query){
		return Math.round(query*10.0)/10.0;
	}
	
	private ItemStack removeAttributes(ItemStack i){
        if(i == null) {
            return i;
        }
        if(i.getType() == Material.BOOK_AND_QUILL) {
            return i;
        }
        ItemStack item = i.clone();
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!nmsStack.hasTag()){
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }
        else {
            tag = nmsStack.getTag();
        }
        NBTTagList am = new NBTTagList();
        tag.set("AttributeModifiers", am);
        nmsStack.setTag(tag);
        return CraftItemStack.asCraftMirror(nmsStack);
    }
	
	/*
	 * 
	 * 
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * CLASS SELECTION ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * 
	 * 
	 */
	
	private void openClassSelection(Player player){
		Inventory inv = Bukkit.createInventory(player, 36, "Character Selection");
		
		ItemStack create = new ItemStack(Material.STAINED_CLAY, 1, (byte)14);
		ItemMeta im = create.getItemMeta();
		im.setDisplayName(ChatColor.RED+"Create Character");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY+"Click to create a new character!");
		im.setLore(lore);
		create.setItemMeta(im);
		
		String uuid = player.getUniqueId().toString();
		List<Object[]> classes = kLifeAPI.classInfo(uuid);
		
		int i = 0;
		for(i = 0; i < classes.size(); i++){
			Object[] info = classes.get(i);
			int row = i/5;
			int col = i - row*5;
			
			String className = (String)info[0];
			ItemStack c = create;
			
			if(className.contains("Mage"))
				c = new ItemStack(Material.STICK);
			else if(className.contains("Archer"))
				c = new ItemStack(Material.BOW);
			else if(className.contains("Rogue"))
				c = new ItemStack(Material.LEVER);
			else if(className.contains("Warrior"))
				c = new ItemStack(Material.WOOD_AXE);
			else if(className.contains("none")){
				inv.setItem(11+(row*9)+col, removeAttributes(c));
				break;
			}
				
			ItemMeta m = c.getItemMeta();
			m.setDisplayName(ChatColor.YELLOW+"Character "+(i+1)+" ("+className+")");
			List<String> l = new ArrayList<String>();
			l.add(ChatColor.YELLOW+"- "+ChatColor.GRAY+"Level: "+ChatColor.AQUA+kLifeAPI.level(uuid, className.toLowerCase()));
			l.add(ChatColor.YELLOW+"- "+ChatColor.GRAY+"Health: "+ChatColor.AQUA+((Double)info[1]).intValue()+"/"+((Double)info[2]).intValue());
			l.add(ChatColor.YELLOW+"- "+ChatColor.GRAY+"Mana: "+ChatColor.AQUA+((Double)info[3]).intValue()+"/20");
			l.add(ChatColor.YELLOW+"- "+ChatColor.GRAY+"Quest Progress: "+ChatColor.AQUA+"0%");
			m.setLore(l);
			c.setItemMeta(m);
			
			inv.setItem(11+(row*9)+col, removeAttributes(c));
		}
		
		i++;
		int row = i/5;
		int col = i - row*5;
		
		for(i = 11+(row*9)+col; i < 25; i++){
			inv.setItem(i, removeAttributes(create));
		}
		
		locked.add(player);
		player.openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent ev){
		Inventory inv = ev.getInventory();
		
		if(inv.getTitle().contains("Character Selection") || inv.getTitle().contains("Select a Class")){
			ItemStack clicked = ev.getCurrentItem();
			Player player = (Player)ev.getWhoClicked();
			
			if(clicked != null){
				ev.setCancelled(true);
				String disp = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
				
				locked.remove(player);
				
				if(disp.contains("(")){
					String type = disp.substring(disp.indexOf("(")+1, disp.indexOf(")"));
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chr " + player.getName() + " select " + type);
					player.closeInventory();
				}
				else if(disp.contains("Class")){
					String type = disp.substring(0, disp.indexOf(" Class")).toLowerCase();
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "chr " + ev.getWhoClicked().getName() + " create " + type);
					ev.getWhoClicked().closeInventory();
				}
				else if(disp.contains("Character")){
					player.closeInventory();
					createCharacter((Player)ev.getWhoClicked());
				}
			}
		}
	}
	
	private void createCharacter(Player player){
		locked.add(player);
		
		Inventory inv = Bukkit.createInventory(player, 36, "Select a Class");
		
		ItemStack mage = new ItemStack(Material.STICK);
		ItemStack archer = new ItemStack(Material.BOW);
		ItemStack rogue = new ItemStack(Material.LEVER);
		ItemStack warrior = new ItemStack(Material.WOOD_AXE);
		
		String A = ChatColor.RED+"\u25A0";
		String D = ChatColor.GREEN+"\u25A0";
		String R = ChatColor.AQUA+"\u25A0";
		String E = ChatColor.GRAY+"\u25A0";
		
		ItemMeta m = mage.getItemMeta();
		m.setDisplayName(ChatColor.YELLOW+""+ChatColor.BOLD+"Mage Class");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY+"Attack:  "+A+A+A+A+E+E+E+E+E+E);
		lore.add(ChatColor.GRAY+"Defence: "+D+D+D+D+D+D+E+E+E+E);
		lore.add(ChatColor.GRAY+"Range:   "+R+R+R+R+R+R+E+E+E+E);
		lore.add("");
		lore.add(ChatColor.GRAY+""+ChatColor.ITALIC+"Click to select this class");
		m.setLore(lore);
		mage.setItemMeta(m);
		
		ItemMeta a = archer.getItemMeta();
		a.setDisplayName(ChatColor.YELLOW+""+ChatColor.BOLD+"Archer Class");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY+"Attack:  "+A+A+A+A+A+A+E+E+E+E);
		lore.add(ChatColor.GRAY+"Defence: "+D+D+D+D+E+E+E+E+E+E);
		lore.add(ChatColor.GRAY+"Range:   "+R+R+R+R+R+R+R+R+R+R);
		lore.add("");
		lore.add(ChatColor.GRAY+""+ChatColor.ITALIC+"Click to select this class");
		a.setLore(lore);
		archer.setItemMeta(a);
		
		ItemMeta r = rogue.getItemMeta();
		r.setDisplayName(ChatColor.YELLOW+""+ChatColor.BOLD+"Rogue Class");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY+"Attack:  "+A+A+A+A+A+A+A+A+A+A);
		lore.add(ChatColor.GRAY+"Defence: "+D+D+D+D+D+D+E+E+E+E);
		lore.add(ChatColor.GRAY+"Range:   "+R+R+E+E+E+E+E+E+E+E);
		lore.add("");
		lore.add(ChatColor.GRAY+""+ChatColor.ITALIC+"Click to select this class");
		r.setLore(lore);
		rogue.setItemMeta(r);
		
		ItemMeta w = warrior.getItemMeta();
		w.setDisplayName(ChatColor.YELLOW+""+ChatColor.BOLD+"Warrior Class");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY+"Attack:  "+A+A+A+A+A+A+A+E+E+E);
		lore.add(ChatColor.GRAY+"Defence: "+D+D+D+D+D+D+D+D+D+D);
		lore.add(ChatColor.GRAY+"Range:   "+R+E+E+E+E+E+E+E+E+E);
		lore.add("");
		lore.add(ChatColor.GRAY+""+ChatColor.ITALIC+"Click to select this class");
		w.setLore(lore);
		warrior.setItemMeta(w);
		
		inv.setItem(10, removeAttributes(mage));
		inv.setItem(12, removeAttributes(archer));
		inv.setItem(14, removeAttributes(rogue));
		inv.setItem(16, removeAttributes(warrior));
		
		player.openInventory(inv);
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent ev){
		if(!(ev.getPlayer() instanceof Player))
			return;
		
		final Inventory inv = ev.getInventory();
		final Player p = (Player)ev.getPlayer();
		
		if((inv.getTitle().equalsIgnoreCase("Character Selection") || inv.getTitle().equalsIgnoreCase("Select a Class")) && locked.contains(p)){
			new BukkitRunnable(){
				public void run(){
					locked.remove(p);
					openClassSelection(p);
				}
			}.runTaskLater(plugin, 1);
			
			return;
		}
		
		if(ev.getInventory().getType() != InventoryType.CRAFTING)
			return;
		
		String uuid = p.getUniqueId().toString();
		String type = kLifeAPI.type(uuid);
		checkHealth(p, uuid, type);
	}
	
	/*
	 * 
	 * 
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ARROW REMOVER ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * 
	 * 
	 */
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev){
		Entity ent = ev.getEntity();
		if(ent instanceof Arrow)
			ent.remove();
	}
	
	/*
	 * 
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * CUSTOM ARMOR |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * 
	 */
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerAutoHeal(EntityRegainHealthEvent ev){
		if(ev.getEntity() instanceof Player && ev.getRegainReason() == RegainReason.SATIATED){
			final Player player = (Player)ev.getEntity();
			double cH = player.getHealth();
			double mH = player.getMaxHealth();
			if(cH >= 0.9*mH){
				ev.setAmount(mH-cH);
			}
			else {
				ev.setAmount(0.1*mH);
			}
			
			new BukkitRunnable(){
				public void run(){
					showHealth(player);
				}
			}.runTaskLater(this, 1);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerHurt(EntityDamageEvent ev){
		if(ev.getEntity() instanceof Player){
			final Player player = (Player) ev.getEntity();
			new BukkitRunnable(){
				public void run(){
					String uuid = player.getUniqueId().toString();
					String type = kLifeAPI.type(uuid);
					checkHealth(player, uuid, type);
				}
			}.runTaskLater(this, 1);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent ev){
		new BukkitRunnable(){
			public void run(){
				String uuid = ev.getPlayer().getUniqueId().toString();
				String type = kLifeAPI.type(uuid);
				checkHealth(ev.getPlayer(), uuid, type);
			}
		}.runTaskLater(this, 1);
	}
	
	private void checkHealth(Player p, String uuid, String type){
		ItemStack[] armor = p.getInventory().getArmorContents();
		double health = 20.0;
		for(ItemStack piece : armor)
			health += readHealth(piece, kLifeAPI.level(uuid, type)) * 2;
		p.setMaxHealth(health);
		p.setHealthScale(20);
		showHealth(p);
	}
	
	private void showHealth(Player p){
		sendActionBar(p, ChatColor.DARK_RED + "\u2764 "+ChatColor.RED+"Health: "+(int)p.getHealth()/2+"/"+(int)p.getMaxHealth()/2);
	}
	
	private void sendActionBar(Player player, String message){
        CraftPlayer p = (CraftPlayer) player;
        IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte)2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }
	
	private double readHealth(ItemStack armor, int level){
		List<String> lore = null;
		
		try {
			lore = armor.getItemMeta().getLore();
		}
		catch (NullPointerException e){
			return 0;
		}
		
		if(lore == null)
			return 0;
		
		double def = 0;
		for(String line : lore){
			if(line.contains("Defence")){
				def = Double.parseDouble(line.substring(line.lastIndexOf(":")+2));
			}
			else if(line.contains("Level")){
				if(level < Integer.parseInt(line.substring(line.lastIndexOf(":")+2)))
					return 0;
				else
					return def;
			}
		}
		return 0;
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onClickMonitor(PlayerInteractEvent ev){
		if(ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK){
			final Player player = ev.getPlayer();
			ItemStack hand = player.getInventory().getItemInHand();
			if(hand != null){
				String iT = hand.getType().toString();
				if(iT.contains("BOOTS") || iT.contains("LEGGINGS") || iT.contains("CHESTPLATE") || iT.contains("HELMET")){
					final String uuid = player.getUniqueId().toString();
					final String type = kLifeAPI.type(uuid);
					new BukkitRunnable(){
						public void run(){
							checkHealth(player, uuid, type);
						}
					}.runTaskLater(this, 1);
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent ev){
		Player player = ev.getEntity();
		String uuid = player.getUniqueId().toString();
		String type = kLifeAPI.type(uuid);
		checkHealth(player, uuid, type);
	}
	
	/*
	 * 
	 * 
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * SPELLS |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * 
	 * 
	 */
	
	@EventHandler
	public void onStickClick(PlayerInteractEvent ev){
		final Player player = ev.getPlayer();
		ItemStack hand = player.getInventory().getItemInHand();
		if(hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName() || !hand.getItemMeta().hasLore())
			return;
		
		if(ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK || hand.getItemMeta().getDisplayName().contains("Machination")){
			String type = kLifeAPI.type(player.getUniqueId().toString());
			//if(hand == null /*|| !type.split("-")[1].equals("mage")*/ || !hand.getType().equals(Material.STICK) || !hand.hasItemMeta() || !hand.getItemMeta().hasLore())
			//	return;
			List<String> lores = hand.getItemMeta().getLore();
			int attack = 0;
			for(int a = 0; a < lores.size(); a++){
				String loreLine = ChatColor.stripColor(lores.get(a));
				
				if(loreLine.contains("Ability"))
					return;
				
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
					if(kLifeAPI.level(player.getUniqueId().toString(), type) >= minLevel){
						ev.setCancelled(true);
						//EnumParticle[] particles = {EnumParticle.CLOUD, EnumParticle.CRIT_MAGIC, EnumParticle.SPELL_WITCH, EnumParticle.VILLAGER_HAPPY};
						//createHelix(player, particles[(int)Math.floor(Math.random()*particles.length)], attack);
						Location loc = player.getEyeLocation();
						spells.launchSpell(player, ChatColor.stripColor(hand.getItemMeta().getDisplayName()), loc, loc.getDirection());
						return;
					}
				}
			}
		}
	}
	
	/*
	 * 
	 * 
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * TELEPORTATION EFFECTS ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * 
	 * 
	 */
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent ev){
		final Location loc = ev.getTo();
		final Location from = ev.getFrom();
		
		if(loc.distance(from) < 1)
			return;
		
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
					for(Player player : loc.getWorld().getPlayers()){
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
	
	private double xPos(double velocity, double yaw, double pitch, double time, double initialX){
		return -1 * velocity * Math.cos(Math.PI/180*pitch) * time * Math.sin(Math.PI/180*yaw) + initialX;
	}
	
	private double yPos(double velocity, double yaw, double pitch, double time, double initialY, double gravity){
		return 0.5 * gravity * Math.pow(time, 2) - velocity * Math.sin(Math.PI/180*pitch) * time + initialY;
	}
	
	private double zPos(double velocity, double yaw, double pitch, double time, double initialZ){
		return velocity * Math.cos(Math.PI/180*pitch) * time * Math.cos(Math.PI/180*yaw) + initialZ;
	}
	
	/*
	 * 
	 * 
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * HELIX SPELL ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * 
	 * 
	 */
	
	private void createHelix(final Player player, final EnumParticle particle, final int damage) {
		final Location loc = player.getEyeLocation();
		//loc.add(0, 0.7, 0);
		final Vector direction = player.getLocation().getDirection();
	    final double radius = 0.35;
	    final int range = 7;
	    final Double[] time = {0.0};
	    new BukkitRunnable(){
	    	public void run(){
		    	for(int i = 0; i < 20; i ++){
		    		Location center = new Location(loc.getWorld(), loc.getX() + direction.getX() * time[0], loc.getY() + direction.getY() * time[0], loc.getZ() + direction.getZ() * time[0]);
		    		double x = radius * Math.sin(time[0]) * Math.cos(Math.PI/180 * loc.getYaw());
		    		double y = radius * Math.cos(time[0]);
			        double z = radius * Math.sin(time[0]) * Math.sin(Math.PI/180 * loc.getYaw());
			        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, true, (float) (center.getX() + x), (float) (center.getY() + y), (float) (center.getZ() + z), 0f, 0f, 0f, 0f, 1);
			        for(Player online : loc.getWorld().getPlayers()) {
			            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
			        }
			        
			        if(!center.getWorld().getBlockAt(center).getType().equals(Material.AIR))
						this.cancel();
					
					for(Entity ent : center.getChunk().getEntities()){
						if(!ent.equals(player)){
							Location entLoc = ent.getLocation();
							boolean closeX = ((center.getX() + x) <= entLoc.getX()+radius*3 && (center.getX() + x) >= entLoc.getX()-radius*3);
							boolean closeY = ((center.getY() + y) <= entLoc.getY()+radius*3*2 && (center.getY() + y) >= entLoc.getY()-radius*3/2);
							boolean closeZ = ((center.getZ() + z) <= entLoc.getZ()+radius*3 && (center.getZ() + z) >= entLoc.getZ()-radius*3);
							if(closeX && closeY && closeZ){
								EntityDamageByEntityEvent damEv = new EntityDamageByEntityEvent(player, ent, DamageCause.MAGIC, damage);
								Bukkit.getServer().getPluginManager().callEvent(damEv);
								if(!damEv.isCancelled())
									((Damageable)ent).damage(damage);
								this.cancel();
							}
						}
					}
					
			        time[0] += 0.1;
			        if(Math.sqrt(Math.pow(direction.getX() * time[0], 2) + Math.pow(direction.getY() * time[0], 2) + Math.pow(direction.getZ() * time[0], 2)) >= range)
			        	this.cancel();
		    	}
	    	}
	    }.runTaskTimer(this, 0, 1);
	}
	
	/*
	 * 
	 * 
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * STRAIGHT SPELL |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * 
	 * 
	 */
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
	
	/*
	 * 
	 * 
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * HOVERABLE WEAPON INFO  |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * CHAT BOT  |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 * 
	 * 
	 */
	

	@EventHandler
	public void onChatEvent(final AsyncPlayerChatEvent ev){
		final String message = ev.getMessage();
		final Player player = ev.getPlayer();
		
		if(message.contains(";weapon")){
			
			String uuid = player.getUniqueId().toString();

			ChatMessage nMsg = new ChatMessage(kLifeAPI.karmaTitleName(uuid, player.getName())+ChatColor.GRAY+" > ");
			String[] words = message.split(" ");
			for(String word : words){
				if(word.contains(";weapon"))
					nMsg.addSibling(hoverableWeapon(player));
				else
					nMsg.addSibling(new ChatMessage(word));
				nMsg.addSibling(new ChatMessage(" "));
			}
			
			ev.setCancelled(true);
			
			for(Player p : ev.getRecipients())
				((CraftPlayer)p).getHandle().sendMessage(nMsg);
		}
		
		if(message.toLowerCase().contains("thirsty")){
			new BukkitRunnable(){
				public void run(){
					player.sendMessage(ChatColor.AQUA+"Jimmy "+ChatColor.GRAY+"> "+ChatColor.WHITE + "Would you like something to drink?");
					
					new BukkitRunnable(){
						public void run(){
							player.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"Jimmy opens the fridge.");
							
							new BukkitRunnable(){
								public void run(){
									player.sendMessage(ChatColor.AQUA+"Jimmy "+ChatColor.GRAY+"> "+ChatColor.WHITE + "We have water, milk, juice, spiders, and soda.");
								}
							}.runTaskLater(plugin, 30);
						}
					}.runTaskLater(plugin, 30);
				}
			}.runTaskLater(this, 30);
			
			ev.setCancelled(true);
			
			player.sendMessage(kLifeAPI.karmaTitleName(player.getUniqueId().toString(), player.getName())+ChatColor.GRAY+" > " + ChatColor.WHITE + message);
		}
		else if(message.toLowerCase().equals("spiders?")){
			new BukkitRunnable(){
				public void run(){
					player.sendMessage(ChatColor.AQUA+"Jimmy "+ChatColor.GRAY+"> "+ChatColor.WHITE + "Spiders it is, then!");
					
					new BukkitRunnable(){
						public void run(){
							player.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"You tried to interject, but he was already pouring you a brimming glass of spiders.");
							
							new BukkitRunnable(){
								public void run(){
									player.sendMessage(ChatColor.AQUA+"Jimmy "+ChatColor.GRAY+"> "+ChatColor.WHITE + "Enjoy! :)");
								}
							}.runTaskLater(plugin, 30);
						}
					}.runTaskLater(plugin, 30);
				}
			}.runTaskLater(plugin, 30);
			
			ev.setCancelled(true);
			player.sendMessage(kLifeAPI.karmaTitleName(player.getUniqueId().toString(), player.getName())+ChatColor.GRAY+" > " + ChatColor.WHITE + message);
		}
		else if(message.toLowerCase().contains("jimmy")){
			if(message.toLowerCase().contains("0") && message.toLowerCase().contains("divided")){
				new BukkitRunnable(){
					public void run(){
						player.sendMessage(ChatColor.AQUA+"Jimmy "+ChatColor.GRAY+"> "+ChatColor.WHITE + "It's... uh... error. Yeah. It says error on my calculator.");
					}
				}.runTaskLater(plugin, 50);
			}
			
			new BukkitRunnable(){
				public void run(){
					player.sendMessage(ChatColor.AQUA+"Jimmy "+ChatColor.GRAY+"> "+ChatColor.WHITE + jimmy.generateMessage(message, ev.getPlayer()));
				}
			}.runTaskLater(this, 20);
			
			ev.setCancelled(true);
			player.sendMessage(kLifeAPI.karmaTitleName(player.getUniqueId().toString(), player.getName())+ChatColor.GRAY+" > " + ChatColor.WHITE + message);
		}
		
		
	}
		
	private IChatBaseComponent hoverableWeapon(Player player){
		net.minecraft.server.v1_8_R3.ItemStack inHand = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy((CraftItemStack)player.getItemInHand());
		return toText(inHand, player.getItemInHand());
	}
	
	private IChatBaseComponent toText(net.minecraft.server.v1_8_R3.ItemStack item, ItemStack origI){
		ChatComponentText var1 = new ChatComponentText(item.getName());
	      if(item.hasName()) {
	         var1.getChatModifier().setItalic(Boolean.valueOf(true));
	      }
	      
	      String color;
	      try {
	    	  color = ChatColor.getLastColors(origI.getItemMeta().getDisplayName());
	      }
	      catch (NullPointerException e){
	    	  color = ChatColor.WHITE+"";
	      }
	      
	      ChatComponentText b1 = new ChatComponentText(color+"[");
	      ChatComponentText b2 = new ChatComponentText(color+"]");
	      IChatBaseComponent var2 = b1.addSibling(var1).addSibling(b2);
	      
	      if(item != null) {
	         NBTTagCompound var3 = new NBTTagCompound();
	         item.save(var3);
	         var2.getChatModifier().setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ITEM, new ChatComponentText(var3.toString())));
	      }

	      return var2;
	}
}
