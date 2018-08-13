package com.solinia.solinia.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Exceptions.InvalidNPCMerchantListSettingException;
import com.solinia.solinia.Interfaces.ISoliniaNPCMerchant;
import com.solinia.solinia.Managers.StateManager;

public class CommandEditMerchantList implements CommandExecutor 
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) && !(sender instanceof CommandSender))
			return false;
		
		if (sender instanceof Player)
		{

			Player player = (Player) sender;
			
			if (!player.isOp() && !player.hasPermission("solinia.editmerchantlist"))
			{
				player.sendMessage("You do not have permission to access this command");
				return false;
			}
		}
		
		if (args.length < 1)
		{
			sender.sendMessage("Insufficient arguments: merchantid");
			return false;
		}
		
		// Args
		// merchantid
		// Setting
		// NewValue
		
		if (args.length == 0)
		{
			return false;
		}

		int merchantid = Integer.parseInt(args[0]);
		
		if (args.length == 1)
		{
			try
			{
				ISoliniaNPCMerchant list = StateManager.getInstance().getConfigurationManager().getNPCMerchant(merchantid);
				if (list != null)
				{
					list.sendMerchantSettingsToSender(sender);
				} else {
					sender.sendMessage("ID doesnt exist");
				}
				return true;
			} catch (CoreStateInitException e)
			{
				sender.sendMessage(e.getMessage());
			}
		}

		
		if (args.length < 3)
		{
			sender.sendMessage("Insufficient arguments: id setting value");
			return false;
		}
		
		String setting = args[1];
		
		String value = args[2];
		
		if (merchantid < 1)
		{
			sender.sendMessage("Invalid id");
			return false;
		}
		
		try
		{

			if (StateManager.getInstance().getConfigurationManager().getNPCMerchant(merchantid) == null)
			{
				sender.sendMessage("Cannot locate id: " + merchantid);
				return false;
			}
			
			if (StateManager.getInstance().getConfigurationManager().getNPCMerchant(merchantid).isOperatorCreated() && !sender.isOp())
			{
				sender.sendMessage("This was op created and you are not an op. Only ops can edit op items");
				return false;
			}

			StateManager.getInstance().getConfigurationManager().editNPCMerchantList(merchantid,setting,value);
			sender.sendMessage("Updating setting on merchant");
		} catch (InvalidNPCMerchantListSettingException ne)
		{
			sender.sendMessage("Invalid merchant setting: " + ne.getMessage());
		} catch (CoreStateInitException e) {
			// TODO Auto-generated catch block
			sender.sendMessage(e.getMessage());
		}
		return true;
	}
}
