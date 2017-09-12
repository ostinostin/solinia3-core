package com.solinia.solinia.Managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import com.solinia.solinia.Adapters.SoliniaLivingEntityAdapter;
import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Interfaces.IEntityManager;
import com.solinia.solinia.Interfaces.INPCEntityProvider;
import com.solinia.solinia.Interfaces.ISoliniaLivingEntity;
import com.solinia.solinia.Models.SoliniaEntitySpellEffects;
import com.solinia.solinia.Models.SoliniaLivingEntity;
import com.solinia.solinia.Models.SoliniaSpell;
import com.solinia.solinia.Utils.Utils;

public class EntityManager implements IEntityManager {
	INPCEntityProvider npcEntityProvider;
	private ConcurrentHashMap<UUID, SoliniaEntitySpellEffects> entitySpellEffects = new ConcurrentHashMap<UUID, SoliniaEntitySpellEffects>();

	public EntityManager(INPCEntityProvider npcEntityProvider) {
		this.npcEntityProvider = npcEntityProvider;
	}

	@Override
	public ISoliniaLivingEntity getLivingEntity(LivingEntity livingentity)
	{
		return new SoliniaLivingEntity(livingentity);
	}
	
	@Override
	public INPCEntityProvider getNPCEntityProvider()
	{
		return npcEntityProvider;
	}

	@Override
	public boolean addActiveEntityEffect(LivingEntity targetentity, SoliniaSpell soliniaSpell, Player player) {
		
		if (entitySpellEffects.get(targetentity.getUniqueId()) == null)
			entitySpellEffects.put(targetentity.getUniqueId(), new SoliniaEntitySpellEffects(targetentity));
		
		int duration = Utils.getDurationFromSpell(soliniaSpell);
		return entitySpellEffects.get(targetentity.getUniqueId()).addSpellEffect(soliniaSpell, player, duration);
	}
	
	@Override
	public SoliniaEntitySpellEffects getActiveEntityEffects(LivingEntity entity) {
		return entitySpellEffects.get(entity.getUniqueId());
	}

	@Override
	public void spellTick() {
		List<UUID> uuidRemoval = new ArrayList<UUID>();
		for (SoliniaEntitySpellEffects entityEffects : entitySpellEffects.values())
		{
			Entity entity = Bukkit.getEntity(entityEffects.getLivingEntityUUID());
			if (entity == null)
			{
				uuidRemoval.add(entityEffects.getLivingEntityUUID());
			}
		}
		
		for(UUID uuid : uuidRemoval)
		{
			System.out.println("Cleared Entity Effects for invalid UUID: " + uuid);
			entitySpellEffects.remove(uuid);
		}
		
		
		for(SoliniaEntitySpellEffects entityEffects : entitySpellEffects.values())
		{
			entityEffects.run();
		}
	}

	@Override
	public void doNPCRandomChat() {
		System.out.println("NPC Random Chat Tick");
		List<Integer> completedNpcsIds = new ArrayList<Integer>();
		for(Player player : Bukkit.getOnlinePlayers())
		{
			for(Entity entity : player.getNearbyEntities(50, 50, 50))
			{
				if (entity instanceof Player)
					continue;
				
				if (!(entity instanceof LivingEntity))
					continue;
				
				LivingEntity le = (LivingEntity)entity;
				if (!Utils.isLivingEntityNPC(le))
					continue;
				
				try {
					ISoliniaLivingEntity solle = SoliniaLivingEntityAdapter.Adapt(le);
					if (completedNpcsIds.contains(solle.getNpcid()))
						continue;
					
					completedNpcsIds.add(solle.getNpcid());
					solle.doRandomChat();
					
				} catch (CoreStateInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("NPC Random Chat Tick completed");
	}

}
