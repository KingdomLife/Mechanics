package com.patrickzhong.mechanics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/*
 * 
 * An object which represents an interactive chatbot, which generates messages based on input.
 * @author Patrick Zhong
 *
 */
public class ChatBot {

	String personality;
	Mechanics plugin;
	
	String[] greeting = {"Hey!", "Hello!", "Herro!", "Hullo!", "Hey!", "Hi!", "That's me!"};
	String[] greetingSec = {"", "", "What's up?", "How're you doing?", "Do you need something?"};
	
	public ChatBot(String personality, Mechanics plugin){
		this.personality = personality;
		this.plugin = plugin;
	}
	
	public String generateMessage(String message, Player sender){
		message = message.toLowerCase();
		
		OfflinePlayer target = target(message, sender);
		
		if(message.contains("what")){
			
			// Question looking for an OBJECT
			
			if(message.contains("name")){
				return target.getName();
			}
			else if(message.contains("level")){
				String uuid = target.getUniqueId().toString();
				return "I believe it's "+plugin.kLifeAPI.level(uuid, plugin.kLifeAPI.type(uuid));
			}
			else if(contOr(message, "class", "type")){
				String uuid = target.getUniqueId().toString();
				return "It's "+plugin.kLifeAPI.type(uuid);
			}
			else if(message.contains("date")){
				String[] date = (new Date()).toString().split(" ");
				return "The date is " + date[1]+" "+date[2]+", "+date[date.length-1];
			}
			else if(message.contains("time")){
				Date date = new Date();;
				int hours = date.getHours();
				String zone = "EST";
				
				if(contOr(message, "gmt", "greenwich")){
					hours += 5;
					zone = "GMT";
				}
				else if(contOr(message, "pst", "pacific")){
					hours -= 3;
					zone = "PST";
				}
				else if(contOr(message, "cet", "europe")){
					hours += 6;
					zone = "CET";
				}
				else if(contOr(message, "cct", "china")){
					hours += 13;
					zone = "CCT";
				}
				else if(contOr(message, "jst", "japan")){
					hours += 14;
					zone = "JST";
				}
				else if(contOr(message, "cst", "central standard")){
					hours -= 1;
					zone = "CST";
				}
				else if(contOr(message, "hst", "hawaii")){
					hours -= 5;
					zone = "HST";
				}
				
				return "It's " + hours%24 + ":" + date.getMinutes() + " "+zone;
			}	
			else if(contOr(message, "+", "-", "/", "*")){
				return parseSentenceForMath(message);
			}
			else if(contAnd(message, "0", "divided")){
				return "0 divided by 0 is... umm... let me get my calculator real quick.";
			}
			
		}
		else if(message.contains("calc")){
			return parseSentenceForMath(message);
		}
		else if(message.contains("thank")){
			return "No problem!";
		}
		else if(message.contains("mettaton")){
			return "He's a robot with a soul, and the sole television star of the Underground. He might also be the demon inquisitor Maltheus";
		}
		else if(contAnd(message, "0", "divided")){
			return "0 divided by 0 is... umm... let me get my calculator real quick.";
		}
		else if(contOr(message, "?", "why", "what", "where", "when")){
			return "Inquisitive, aren't you?";
		}
		else if(contOr(message, "!", "yay", "hooray", "yass")){
			return "Excited, ay?";
		}
		
		return greeting[(int)(Math.random()*greeting.length)] + " " + greetingSec[(int)(Math.random()*greetingSec.length)];
		
	}
	
	private OfflinePlayer target(String str, Player player){
		String[] arr = str.split(" ");
		for(String s : arr){
			if(s.equalsIgnoreCase("my") || s.equalsIgnoreCase("i")){
				return player;
			}
			else {
				s = s.replace("'s", "");
				
				Player p = plugin.getServer().getPlayer(s);
				if(p != null)
					return p;
				OfflinePlayer eP = plugin.getServer().getOfflinePlayer(s);
				if(eP != null && eP.hasPlayedBefore())
					return eP;
			}
		}
		
		return player;
	}
	
	private boolean contOr(String str, String... checks){
		for(String check : checks){
			if(str.contains(check))
				return true;
		}
		
		return false;
	}
	
	private boolean contAnd(String str, String... checks){
		for(String check : checks){
			if(!str.contains(check))
				return false;
		}
		
		return true;
	}
	
	private String parseSentenceForMath(String str){
		String expression = "";
		
		for(int i = 0; i < str.length(); i ++){
			String s = str.substring(i, i+1);
			if(contOr(s, "+", "-", "*", "/", ".") || isDigit(s))
				expression += s;
		}
		
		return expression + " = " + evaluateExpression(expression);
	}
	
	private double evaluateExpression(String str){
		String currNum = "";
		List<Object> xps = new ArrayList<Object>();
		for(int i = 0; i < str.length(); i++){
			String s = str.substring(i, i+1);
			if(isDigit(s) || s.equals("."))
				currNum += s;
			else {
				xps.add(Double.parseDouble(currNum));
				xps.add(s);
				currNum = "";
			}
		}
		
		if(currNum.length() != 0)
			xps.add(Double.parseDouble(currNum));
		
		while(xps.size() > 2){
			double n1 = (Double)xps.get(0);
			String action = (String)xps.get(1);
			double n2 = (Double)xps.get(2);
			
			if(action.equals("*"))
				xps.set(0, n1 * n2);
			else if(action.equals("+"))
				xps.set(0, n1 + n2);
			else if(action.equals("-"))
				xps.set(0, n1 - n2);
			else if(action.equals("/"))
				xps.set(0, n1 / n2);
			
			xps.remove(2);
			xps.remove(1);
		}
		return (Double)xps.get(0);
	}
	
	private boolean isDigit(String str){
		return (str.compareTo("0") >= 0 && str.compareTo("9") <= 0);
	}
	
}
