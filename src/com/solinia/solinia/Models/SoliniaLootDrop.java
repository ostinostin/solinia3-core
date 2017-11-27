package com.solinia.solinia.Models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.google.gson.JsonElement;
import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Exceptions.InvalidLootDropSettingException;
import com.solinia.solinia.Exceptions.InvalidSpawnGroupSettingException;
import com.solinia.solinia.Interfaces.ISoliniaItem;
import com.solinia.solinia.Interfaces.ISoliniaLootDrop;
import com.solinia.solinia.Interfaces.ISoliniaLootDropEntry;
import com.solinia.solinia.Interfaces.ISoliniaNPC;
import com.solinia.solinia.Managers.StateManager;

import net.md_5.bungee.api.ChatColor;

public class SoliniaLootDrop implements ISoliniaLootDrop {
	private int id;
	private String name;
	private List<ISoliniaLootDropEntry> entries = new ArrayList<ISoliniaLootDropEntry>();	

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<ISoliniaLootDropEntry> getEntries() {
		return this.entries;
	}

	@Override
	public void setEntries(List<ISoliniaLootDropEntry> entries) {
		this.entries = entries;
	}

	@Override
	public void sendLootDropSettingsToSender(CommandSender sender) {
		try
		{
			sender.sendMessage(ChatColor.GOLD + getName().toUpperCase() + ChatColor.RESET + "[" + getId() + "]:");
			for(ISoliniaLootDropEntry lde : getEntries())
			{
				ISoliniaItem i = StateManager.getInstance().getConfigurationManager().getItem(lde.getItemid());
				sender.sendMessage("- " + ChatColor.GOLD + i.getDisplayname() + ChatColor.RESET + "[" + i.getId() + "] - " + lde.getChance() + "% chance Count: " + lde.getCount() + " Always: " + lde.isAlways());
			}
		} catch (CoreStateInitException e)
		{
			sender.sendMessage(e.getMessage());
		}
	}
	
	@Override
	public void editSetting(String setting, String value)
			throws InvalidLootDropSettingException, NumberFormatException, CoreStateInitException {

		switch (setting.toLowerCase()) {
		case "name":
			if (value.equals(""))
				throw new InvalidLootDropSettingException("Name is empty");

			if (value.length() > 25)
				throw new InvalidLootDropSettingException("Name is longer than 25 characters");
			setName(value);
			break;
		case "remove":
			int itemIdToRemove = Integer.parseInt(value);
			if (itemIdToRemove < 1)
				throw new InvalidLootDropSettingException("Invalid item id to remove");
			for(int i = 0; i < getEntries().size(); i++)
			{
				if (getEntries().get(i).getLootdropid() == itemIdToRemove)
					getEntries().remove(i);
			}
			break;
		case "setallchance":
			int newChance = Integer.parseInt(value);
			for(int i = 0; i < getEntries().size(); i++)
			{
				getEntries().get(i).setChance(newChance);
			}
			break;
		default:
			throw new InvalidLootDropSettingException(
					"Invalid LootDrop setting. Valid Options are: name,remove,setallchance");
		}
	}
}
