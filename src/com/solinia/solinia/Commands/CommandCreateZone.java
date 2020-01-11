package com.solinia.solinia.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Exceptions.SoliniaZoneCreationException;
import com.solinia.solinia.Factories.SoliniaZoneFactory;
import com.solinia.solinia.Models.SoliniaZone;

public class CommandCreateZone implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender instanceof Player) && !(sender instanceof CommandSender))
			return false;
		
		if (!sender.isOp() && !sender.hasPermission("solinia.createzone"))
		{
			sender.sendMessage("You do not have permission to access this command");
			return false;
		}
		
		// Args
		// NameNoSpaces
		// X
		// Y
		// Z
		// SuccorX
		// SuccorY
		// SuccorZ
		// worldname
		
		if (args.length < 8)
		{
			sender.sendMessage("Insufficient arguments: namenospaces x y z succorx succory succorz worldname");
			return false;
		}
		
		String world = Bukkit.getWorld(args[7]).getName();
		
		String zonename = args[0].toUpperCase();
		int x = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int z = Integer.parseInt(args[3]);

		int succorx = Integer.parseInt(args[4]);
		int succory = Integer.parseInt(args[5]);
		int succorz = Integer.parseInt(args[6]);

		if (zonename.equals(""))
		{
			sender.sendMessage("Name of Zone cannot be null");
			return false;
		}
		
		try {
			SoliniaZone zone = SoliniaZoneFactory.Create(zonename,world, x, y, z, succorx, succory, succorz);
			
			sender.sendMessage("Created Zone: " + zone.getId());
		} catch (CoreStateInitException | SoliniaZoneCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sender.sendMessage(e.getMessage());
		}
		return true;
	}
}
