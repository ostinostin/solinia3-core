package com.solinia.solinia.Models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SoliniaEntitySpellEffects {


	private UUID livingEntityUUID;
	private ConcurrentHashMap<Integer, SoliniaActiveSpellEffect> activeSpells = new ConcurrentHashMap<Integer,SoliniaActiveSpellEffect>();
	private boolean isPlayer;
	
	public SoliniaEntitySpellEffects(LivingEntity livingEntity) {
		setLivingEntityUUID(livingEntity.getUniqueId());
		if (livingEntity instanceof Player)
			isPlayer = true;
	}

	public UUID getLivingEntityUUID() {
		return livingEntityUUID;
	}

	public void setLivingEntityUUID(UUID livingEntityUUID) {
		this.livingEntityUUID = livingEntityUUID;
	}
	
	public LivingEntity getLivingEntity()
	{
		return (LivingEntity)Bukkit.getEntity(this.getLivingEntityUUID());
	}

	public Collection<SoliniaActiveSpellEffect> getActiveSpell() {
		return activeSpells.values();
	}

	public boolean addSpellEffect(SoliniaSpell soliniaSpell, Player player, int duration) {
		// This spell ID is already active
		if (activeSpells.get(soliniaSpell.getId()) != null)
			return false;
		
		if(!SoliniaSpell.isValidEffectForEntity(getLivingEntity(),player,soliniaSpell))
			return false;
		
		SoliniaActiveSpellEffect activeEffect = new SoliniaActiveSpellEffect(getLivingEntityUUID(), soliniaSpell.getId(), isPlayer, player.getUniqueId(), true, duration);
		if (duration > 0)
			activeSpells.put(soliniaSpell.getId(),activeEffect);
		
		// Initial run
		activeEffect.apply();
		getLivingEntity().getLocation().getWorld().playEffect(getLivingEntity().getLocation().add(0.5,0.5,0.5), Effect.POTION_BREAK, 5);
		getLivingEntity().getWorld().playSound(getLivingEntity().getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT,1, 0);
		
		if (duration > 0)
			activeSpells.get(soliniaSpell.getId()).setFirstRun(false);
		return true;
	}

	public void run() {
		List<Integer> removeSpells = new ArrayList<Integer>();
		List<SoliniaActiveSpellEffect> updateSpells = new ArrayList<SoliniaActiveSpellEffect>();
		
		for(SoliniaActiveSpellEffect activeSpellEffect : getActiveSpell())
		{
			if (activeSpellEffect.getTicksLeft() == 0)
			{
				removeSpells.add(activeSpellEffect.getSpellId());
			}
			else
			{
				activeSpellEffect.apply();
				activeSpellEffect.setTicksLeft(activeSpellEffect.getTicksLeft() - 1);
				updateSpells.add(activeSpellEffect);
			}
		}
		
		for(Integer spellId : removeSpells)
		{
			activeSpells.remove(spellId);
		}
		
		for(SoliniaActiveSpellEffect effect : updateSpells)
		{
			activeSpells.put(effect.getSpellId(), effect);
		}
	}
}
