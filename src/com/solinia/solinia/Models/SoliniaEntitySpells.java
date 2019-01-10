package com.solinia.solinia.Models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.solinia.solinia.Adapters.SoliniaLivingEntityAdapter;
import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Interfaces.ISoliniaLivingEntity;
import com.solinia.solinia.Managers.StateManager;
import com.solinia.solinia.Utils.Utils;

import me.libraryaddict.disguise.DisguiseAPI;
import net.md_5.bungee.api.ChatColor;

public class SoliniaEntitySpells {

	private UUID livingEntityUUID;
	private ConcurrentHashMap<Short, SoliniaActiveSpell> slots = new ConcurrentHashMap<Short, SoliniaActiveSpell>();
	private boolean isPlayer;

	public SoliniaEntitySpells(LivingEntity livingEntity) {
		setLivingEntityUUID(livingEntity.getUniqueId());
		if (livingEntity instanceof Player)
			isPlayer = true;
	}
	
	private LivingEntity getBukkitLivingEntity()
	{
		Entity entity = Bukkit.getEntity(livingEntityUUID);
		if (!(entity instanceof LivingEntity))
			return null;
		
		return (LivingEntity)entity;		
	}
	
	private ISoliniaLivingEntity getSoliniaLivingEntity()
	{
		if (getBukkitLivingEntity() == null)
			return null;
		
		try
		{
			ISoliniaLivingEntity solLivingEntity = SoliniaLivingEntityAdapter.Adapt(getBukkitLivingEntity());
			return solLivingEntity;
		} catch (CoreStateInitException e)
		{
			
		}
		
		return null;
	}
	
	private int getMaxTotalSlots()
	{
		ISoliniaLivingEntity solLivingEntity = getSoliniaLivingEntity();
		if (solLivingEntity == null)
			return 0;
		
		return solLivingEntity.getMaxTotalSlots();
	}

	public UUID getLivingEntityUUID() {
		return livingEntityUUID;
	}

	public void setLivingEntityUUID(UUID livingEntityUUID) {
		this.livingEntityUUID = livingEntityUUID;
	}

	public LivingEntity getLivingEntity() {
		return (LivingEntity) Bukkit.getEntity(this.getLivingEntityUUID());
	}

	public Collection<SoliniaActiveSpell> getActiveSpells() {
		return slots.values();
	}
	
	private Boolean containsSpellId(int spellId)
	{
		for (SoliniaActiveSpell activeSpell : getActiveSpells()) {
			if (activeSpell.getSpellId() == spellId)
				return true;
		}
		
		return false;
	}

	public boolean addSpell(Plugin plugin, SoliniaSpell soliniaSpell, LivingEntity sourceEntity, int duration) {
		// This spell ID is already active
		if (containsSpellId(soliniaSpell.getId()))
			return false;
		
		if ((slots.size() + 1) > getMaxTotalSlots())
			return false;

		if (this.getLivingEntity() == null)
			return false;

		if (sourceEntity == null)
			return false;

		// System.out.println("Adding spell: " + soliniaSpell.getName() + " to " +
		// this.getLivingEntity().getName() + " from " + sourceEntity.getName());

		try {
			if (!SoliniaSpell.isValidEffectForEntity(getLivingEntity(), sourceEntity, soliniaSpell)) {
				//System.out.println("Spell: " + soliniaSpell.getName() + "[" + soliniaSpell.getId() + "] found to have invalid target (" + getLivingEntity().getName() + ")");
				return false;
			}
		} catch (CoreStateInitException e) {
			return false;
		}
		
		if(!canStack(soliniaSpell))
			return false;
		
		// Resist spells!
		if (soliniaSpell.isResistable() && !soliniaSpell.isBeneficial()) {
			float effectiveness = 100;

			// If state is active, try to use spell effectiveness (resists)
			try {
				ISoliniaLivingEntity solVictimEntity = SoliniaLivingEntityAdapter.Adapt(getLivingEntity());
				effectiveness = solVictimEntity.getResistSpell(soliniaSpell, sourceEntity);
			} catch (CoreStateInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (effectiveness < 100) {
				if (this.getLivingEntity() instanceof Creature && sourceEntity instanceof LivingEntity) {
					// aggro if resisted
					Creature creature = (Creature) this.getLivingEntity();
					if (creature.getTarget() == null) {
						try {
							StateManager.getInstance().getEntityManager().setEntityTarget((LivingEntity) creature,
									(LivingEntity) sourceEntity);
							SoliniaLivingEntityAdapter.Adapt(creature).addToHateList(sourceEntity.getUniqueId(), 1);
						} catch (CoreStateInitException e) {

						}
					}
				}

				// Check resistances
				if (this.getLivingEntity() instanceof Player) {
					Player player = (Player) this.getLivingEntity();
					player.sendMessage(ChatColor.GRAY + "* You resist " + soliniaSpell.getName());
				}

				if (sourceEntity instanceof Player) {
					Player player = (Player) sourceEntity;
					player.sendMessage(ChatColor.GRAY + "* " + soliniaSpell.getName() + " was completely resisted");
				}

				return true;
			}
		}
		
		SoliniaActiveSpell activeSpell = new SoliniaActiveSpell(getLivingEntityUUID(), soliniaSpell.getId(), isPlayer,
				sourceEntity.getUniqueId(), true, duration, soliniaSpell.getNumhits());
		
		Short slot = getNextAvailableSlot();
		if (slot == null)
			return false;
		
		if (duration > 0)
			putSlot(slot, activeSpell, true);

		// System.out.println("Successfully queued spell: "+ soliniaSpell.getName());

		if (activeSpell.getSpell().isBardSong() && sourceEntity != null && !sourceEntity.isDead()) {
			try {
				ISoliniaLivingEntity solEntity = SoliniaLivingEntityAdapter.Adapt(sourceEntity);
				if (solEntity != null)
				{
					solEntity.emote(sourceEntity.getCustomName() + " starts to sing " + soliniaSpell.getName() + " /hidesongs", true);
					System.out.println(sourceEntity.getCustomName() + " starts to sing " + soliniaSpell.getName() + " /hidesongs");
					StateManager.getInstance().getEntityManager().setEntitySinging(sourceEntity.getUniqueId(), soliniaSpell.getId());
				}
			} catch (CoreStateInitException e) {
				// ignore
			}
		}

		// Initial run
		activeSpell.apply(plugin);
		Utils.playSpecialEffect(getLivingEntity(), activeSpell);
		getLivingEntity().getWorld().playSound(getLivingEntity().getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 0);

		if (duration > 0) {
			if (slots.get(slot) != null)
				slots.get(slot).setFirstRun(false);
		}
		return true;
	}

	private boolean canStack(SoliniaSpell soliniaSpell) {
		for(SoliniaActiveSpell activeSpell : getActiveSpells())
		for(ActiveSpellEffect activeEffect : activeSpell.getActiveSpellEffects())
		{
			for(SpellEffect newEffect : soliniaSpell.getBaseSpellEffects())
			{
				// if effects are not same SPA (same effect) they stack 
				if (!newEffect.getSpellEffectType().equals(activeEffect.getSpellEffectType()))
					continue;
				
				// if effects are not in the same position they stack
				if (newEffect.getSpellEffectNo() != activeEffect.getSpellEffectNo())
					continue;
				
				// Does the SPA allow stacking
				if (!newEffect.allowsStacking(activeEffect))
					return false;
			}
		}
			
		return true;
	}

	@SuppressWarnings("incomplete-switch")
	public void removeSpell(Plugin plugin, Integer spellId, boolean forceDoNotLoopBardSpell) {
		// Effect has worn off
		
		SoliniaActiveSpell activeSpell = null;
		for (SoliniaActiveSpell curSpell : getActiveSpells())
		{
			if (curSpell.getSpellId() != spellId)
				continue;
			
			activeSpell = curSpell;
			break;
		}
		
		if (activeSpell == null)
			return;

		boolean updateMaxHp = false;
		boolean updateDisguise = false;
		boolean removeCharm = true;

		// Handle any effect removals needed
		for (ActiveSpellEffect effect : activeSpell.getActiveSpellEffects()) {
			switch (effect.getSpellEffectType()) {
			case TotalHP:
				updateMaxHp = true;
				break;
			case STA:
				updateMaxHp = true;
				break;
			case Illusion:
			case IllusionCopy:
			case IllusionOther:
			case IllusionPersistence:
			case IllusionaryTarget:
				updateDisguise = true;
				break;
			case Charm:
				removeCharm = true;
				break;
			}
		}

		for (Entry<Short, SoliniaActiveSpell> slot : slots.entrySet())
		{
			if (slot.getValue().getSpellId() == spellId)
				slots.remove(slot.getKey());
		}

		if (updateMaxHp == true) {
			if (getLivingEntity() != null)
				if (getLivingEntity() instanceof LivingEntity)
					try {
						ISoliniaLivingEntity solLivingEntity = SoliniaLivingEntityAdapter.Adapt(getLivingEntity());
						if (solLivingEntity != null)
							solLivingEntity.updateMaxHp();
					} catch (CoreStateInitException e) {
	
					}
		}

		if (updateDisguise == true) {
			if (getLivingEntity() != null)
				DisguiseAPI.undisguiseToAll(getLivingEntity());
		}
		
		// Check if bard song, may need to keep singing
		if (forceDoNotLoopBardSpell == false)
		if (activeSpell.getSpell().isBardSong()) {
			try
			{
				if (StateManager.getInstance().getEntityManager().getEntitySinging(activeSpell.getSourceUuid()) != null) 
				{
					Integer singingId = StateManager.getInstance().getEntityManager().getEntitySinging(activeSpell.getSourceUuid());
					if (singingId != activeSpell.getSpellId() || activeSpell.getSpell().getRecastTime() > 0 && Bukkit.getEntity(activeSpell.getSourceUuid()) != null && Bukkit.getEntity(activeSpell.getSourceUuid()) instanceof LivingEntity) {
						ISoliniaLivingEntity solEntity = SoliniaLivingEntityAdapter.Adapt((LivingEntity)Bukkit.getEntity(activeSpell.getSourceUuid()));
						solEntity.emote(solEntity.getName() + "'s song comes to a close [" + activeSpell.getSpell().getName() + "]", true);
						
						if (solEntity.getBukkitLivingEntity().isOp())
							System.out.println("Debug: " + solEntity.getName() + "'s song comes to a close [" + activeSpell.getSpell().getName() + "]");
					} else {
						// Continue singing!
						if (Bukkit.getEntity(activeSpell.getOwnerUuid()) instanceof LivingEntity && Bukkit.getEntity(activeSpell.getSourceUuid()) instanceof LivingEntity && !Bukkit.getEntity(activeSpell.getOwnerUuid()).isDead() && !Bukkit.getEntity(activeSpell.getSourceUuid()).isDead()) {
							boolean itemUseSuccess = activeSpell.getSpell().tryApplyOnEntity(
									(LivingEntity) Bukkit.getEntity(activeSpell.getSourceUuid()),
									(LivingEntity) Bukkit.getEntity(activeSpell.getOwnerUuid()));
							if (!itemUseSuccess)
								Bukkit.getEntity(activeSpell.getSourceUuid()).sendMessage(ChatColor.GRAY + "* Your song failed to apply to the entity!");
							else
								return;
						}
					}
				} else {
					// skip
				}
			} catch (CoreStateInitException e)
			{
				
			}
		}
		
		if (removeCharm == true)
		{
			try {
				ISoliniaLivingEntity solLivingEntity = SoliniaLivingEntityAdapter.Adapt(getLivingEntity());
				//System.out.println("End of charm, attempting removal of pet");
				if (solLivingEntity != null && solLivingEntity.getActiveMob() != null)
				{
					StateManager.getInstance().getEntityManager().removePet(activeSpell.getSourceUuid(), false);
					//System.out.println("Pet being removed");
				} else {
					System.out.println("Could not remove pet");
				}
			} catch (CoreStateInitException e) {

			}
		}
	}

	public void removeAllSpells(Plugin plugin, boolean forceDoNotLoopBardSpell) {
		List<Integer> removeSpells = new ArrayList<Integer>();
		for (SoliniaActiveSpell activeSpell : getActiveSpells()) {
			removeSpells.add(activeSpell.getSpellId());
		}

		for (Integer spellId : removeSpells) {
			try
			{
				removeSpell(plugin, spellId, forceDoNotLoopBardSpell);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void run(Plugin plugin) {
		List<Integer> removeSpells = new ArrayList<Integer>();
		HashMap<Short, SoliniaActiveSpell> updateSpellsSlots = new HashMap<Short, SoliniaActiveSpell>();

		if (!getLivingEntity().isDead())
			for (Entry<Short, SoliniaActiveSpell> slot : this.slots.entrySet()) {
				SoliniaActiveSpell activeSpell = slot.getValue();
				if (activeSpell.getTicksLeft() == 0) {
					removeSpells.add(activeSpell.getSpellId());
				} else {
					activeSpell.buffTick();
					activeSpell.apply(plugin);
					activeSpell.setTicksLeft(activeSpell.getTicksLeft() - 1);
					updateSpellsSlots.put(slot.getKey(), activeSpell);
				}
			}

		for (Integer spellId : removeSpells) {
			removeSpell(plugin, spellId, false);
		}

		for (Entry<Short, SoliniaActiveSpell> activeSpellSlot : updateSpellsSlots.entrySet()) {
			putSlot(activeSpellSlot.getKey(), activeSpellSlot.getValue(),false);
		}
	}
	
	private void putSlot(Short slot, SoliniaActiveSpell activeSpell, boolean checkExists)
	{
		if (checkExists == true && containsSpellId(activeSpell.getSpellId()))
		{
			System.out.println("Error: tried to put a spell [" + activeSpell.getSpellId() + "] in a slot [" + slot + "] that we already have! Cancelling...");
			return;
		}
		
		slots.put(slot, activeSpell);
	}

	private Short getNextAvailableSlot() {
		for (Short slot = 0; slot < getMaxTotalSlots(); slot++)
		{
			if (slots.containsKey(slot))
				continue;
			
			return slot;
		}
		
		return null;
	}

	// Mainly used for cures
	public void removeFirstSpellOfEffectType(Plugin plugin, SpellEffectType type, boolean forceDoNotLoopBardSpell) {
		List<Integer> removeSpells = new ArrayList<Integer>();
		HashMap<Short, SoliniaActiveSpell> updateSpells = new HashMap<Short, SoliniaActiveSpell>();

		boolean foundToRemove = false;
		for (Entry<Short, SoliniaActiveSpell> activeSpellSlot : slots.entrySet()) {
			if (foundToRemove == false && activeSpellSlot.getValue().getSpell().isEffectInSpell(type)) {
				removeSpells.add(activeSpellSlot.getValue().getSpellId());
				foundToRemove = true;
			} else {
				updateSpells.put(activeSpellSlot.getKey(), activeSpellSlot.getValue());
			}
		}

		for (Integer spellId : removeSpells) {
			removeSpell(plugin, spellId, forceDoNotLoopBardSpell);
		}

		for (Entry<Short, SoliniaActiveSpell> activeSpellSlot : updateSpells.entrySet()) {
			putSlot(activeSpellSlot.getKey(), activeSpellSlot.getValue(), false);
		}

	}

	public void removeAllSpellsOfId(Plugin plugin, int spellId, boolean forceDoNotLoopBardSpell) {
		removeSpell(plugin, spellId, forceDoNotLoopBardSpell);
	}

	public void removeFirstSpell(Plugin plugin, boolean forceDoNotLoopBardSpell) {
		List<Integer> removeSpells = new ArrayList<Integer>();
		HashMap<Short, SoliniaActiveSpell> updateSpells = new HashMap<Short, SoliniaActiveSpell>();

		boolean foundToRemove = false;
		for (Entry<Short,SoliniaActiveSpell> activeSpellSlot : slots.entrySet()) {
			if (foundToRemove == false) {
				removeSpells.add(activeSpellSlot.getValue().getSpellId());
				foundToRemove = true;
			} else {
				updateSpells.put(activeSpellSlot.getKey(), activeSpellSlot.getValue());
			}
		}

		for (Integer spellId : removeSpells) {
			removeSpell(plugin, spellId, forceDoNotLoopBardSpell);
		}

		for (Entry<Short, SoliniaActiveSpell> activeSpellSlot : updateSpells.entrySet()) {
			putSlot(activeSpellSlot.getKey(), activeSpellSlot.getValue(), false);
		}
	}
}
