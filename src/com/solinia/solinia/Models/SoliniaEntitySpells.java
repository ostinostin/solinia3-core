package com.solinia.solinia.Models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
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
	private ConcurrentHashMap<Integer, SoliniaActiveSpell> activeSpells = new ConcurrentHashMap<Integer, SoliniaActiveSpell>();
	private boolean isPlayer;

	public SoliniaEntitySpells(LivingEntity livingEntity) {
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

	public LivingEntity getLivingEntity() {
		return (LivingEntity) Bukkit.getEntity(this.getLivingEntityUUID());
	}

	public Collection<SoliniaActiveSpell> getActiveSpells() {
		return activeSpells.values();
	}

	public boolean addSpell(Plugin plugin, SoliniaSpell soliniaSpell, LivingEntity sourceEntity, int duration) {
		// This spell ID is already active
		if (activeSpells.get(soliniaSpell.getId()) != null)
			return false;

		if (this.getLivingEntity() == null)
			return false;

		if (sourceEntity == null)
			return false;

		// System.out.println("Adding spell: " + soliniaSpell.getName() + " to " +
		// this.getLivingEntity().getName() + " from " + sourceEntity.getName());

		try {
			if (!SoliniaSpell.isValidEffectForEntity(getLivingEntity(), sourceEntity, soliniaSpell)) {
				// System.out.println("Spell: " + soliniaSpell.getName() + "[" +
				// soliniaSpell.getId() + "] found to have invalid target (" +
				// getLivingEntity().getName() + ")");
				return false;
			}
		} catch (CoreStateInitException e) {
			return false;
		}

		// Resist spells!
		if (soliniaSpell.isResistable() && !soliniaSpell.isBeneficial()) {
			float effectiveness = 100;

			// If state is active, try to use spell effectiveness (resists)
			try {
				effectiveness = soliniaSpell.getSpellEffectiveness(sourceEntity, getLivingEntity());
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
		if (duration > 0)
			activeSpells.put(soliniaSpell.getId(), activeSpell);

		// System.out.println("Successfully queued spell: "+ soliniaSpell.getName());

		if (activeSpell.getSpell().isBardSong()) {
			if (getLivingEntityUUID().equals(sourceEntity.getUniqueId())) {
				try {
					ISoliniaLivingEntity solEntity = SoliniaLivingEntityAdapter.Adapt(sourceEntity);
					solEntity.emote(sourceEntity.getCustomName() + " starts to sing " + soliniaSpell.getName() + " /hidesongs", true);
					StateManager.getInstance().getEntityManager().setEntitySinging(sourceEntity.getUniqueId(), soliniaSpell.getId());
				} catch (CoreStateInitException e) {
					// ignore
				}
			}
		}

		// Initial run
		activeSpell.apply(plugin);
		Utils.playSpecialEffect(getLivingEntity(), activeSpell);
		getLivingEntity().getWorld().playSound(getLivingEntity().getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 0);

		if (duration > 0) {
			if (activeSpells.get(soliniaSpell.getId()) != null)
				activeSpells.get(soliniaSpell.getId()).setFirstRun(false);
		}
		return true;
	}

	@SuppressWarnings("incomplete-switch")
	public void removeSpell(Plugin plugin, Integer spellId, boolean forceDoNotLoopBardSpell) {
		// Effect has worn off
		SoliniaActiveSpell activeSpell = activeSpells.get(spellId);

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

		activeSpells.remove(spellId);

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
		
		if (removeCharm == true)
		{
			try {
				ISoliniaLivingEntity solLivingEntity = SoliniaLivingEntityAdapter.Adapt(getLivingEntity());
				System.out.println("End of charm, attempting removal of pet");
				if (solLivingEntity != null && solLivingEntity.getActiveMob() != null && solLivingEntity.getActiveMob().getOwner() != null && solLivingEntity.getActiveMob().getOwner().isPresent())
				{
					UUID ownerUuid = solLivingEntity.getActiveMob().getOwner().get();
					StateManager.getInstance().getEntityManager().removePet((Player)Bukkit.getEntity(ownerUuid), false);
					System.out.println("Pet being removed");
				} else {
					System.out.println("Could not remove pet");
				}
			} catch (CoreStateInitException e) {

			}
		}
		
		// Check if bard song, may need to keep singing
		if (forceDoNotLoopBardSpell == false)
		if (activeSpell.getSpell().isBardSong()) {
			if (getLivingEntityUUID().equals(activeSpell.getSourceUuid())) {
				try {
					if (getLivingEntity() != null) {
						if (StateManager.getInstance().getEntityManager()
								.getEntitySinging(getLivingEntity().getUniqueId()) != null) 
						{
							Integer singingId = StateManager.getInstance().getEntityManager()
									.getEntitySinging(getLivingEntity().getUniqueId());
							if (singingId != activeSpell.getSpellId() || activeSpell.getSpell().getRecastTime() > 0) {
								ISoliniaLivingEntity solEntity = SoliniaLivingEntityAdapter.Adapt(getLivingEntity());
								solEntity.emote(getLivingEntity().getCustomName() + "'s song comes to a close [" + activeSpell.getSpell().getName() + "]", true);
							} else {
								// Continue singing!
								if (Bukkit.getEntity(activeSpell.getOwnerUuid()) instanceof LivingEntity
										&& Bukkit.getEntity(activeSpell.getSourceUuid()) instanceof LivingEntity) {
									boolean itemUseSuccess = activeSpell.getSpell().tryApplyOnEntity(
											(LivingEntity) Bukkit.getEntity(activeSpell.getSourceUuid()),
											(LivingEntity) Bukkit.getEntity(activeSpell.getOwnerUuid()));
									return;
								}
							}
						} else {
							// skip
						}
					}
				} catch (CoreStateInitException e) {
					// ignore
				}
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
		List<SoliniaActiveSpell> updateSpells = new ArrayList<SoliniaActiveSpell>();

		if (!getLivingEntity().isDead())
			for (SoliniaActiveSpell activeSpell : getActiveSpells()) {
				if (activeSpell.getTicksLeft() == 0) {
					removeSpells.add(activeSpell.getSpellId());
				} else {
					activeSpell.apply(plugin);
					activeSpell.setTicksLeft(activeSpell.getTicksLeft() - 1);
					updateSpells.add(activeSpell);
				}
			}

		for (Integer spellId : removeSpells) {
			removeSpell(plugin, spellId, false);
		}

		for (SoliniaActiveSpell activeSpell : updateSpells) {
			activeSpells.put(activeSpell.getSpellId(), activeSpell);
		}
	}

	// Mainly used for cures
	public void removeFirstSpellOfEffectType(Plugin plugin, SpellEffectType type, boolean forceDoNotLoopBardSpell) {
		List<Integer> removeSpells = new ArrayList<Integer>();
		List<SoliniaActiveSpell> updateSpells = new ArrayList<SoliniaActiveSpell>();

		boolean foundToRemove = false;
		for (SoliniaActiveSpell activeSpell : getActiveSpells()) {
			if (foundToRemove == false && activeSpell.getSpell().isEffectInSpell(type)) {
				removeSpells.add(activeSpell.getSpellId());
				foundToRemove = true;
			} else {
				updateSpells.add(activeSpell);
			}
		}

		for (Integer spellId : removeSpells) {
			removeSpell(plugin, spellId, forceDoNotLoopBardSpell);
		}

		for (SoliniaActiveSpell activeSpell : updateSpells) {
			activeSpells.put(activeSpell.getSpellId(), activeSpell);
		}

	}

	public void removeAllSpellsOfId(Plugin plugin, int spellId, boolean forceDoNotLoopBardSpell) {
		removeSpell(plugin, spellId, forceDoNotLoopBardSpell);
	}

	public void removeFirstSpell(Plugin plugin, boolean forceDoNotLoopBardSpell) {
		List<Integer> removeSpells = new ArrayList<Integer>();
		List<SoliniaActiveSpell> updateSpells = new ArrayList<SoliniaActiveSpell>();

		boolean foundToRemove = false;
		for (SoliniaActiveSpell activeSpell : getActiveSpells()) {
			if (foundToRemove == false) {
				removeSpells.add(activeSpell.getSpellId());
				foundToRemove = true;
			} else {
				updateSpells.add(activeSpell);
			}
		}

		for (Integer spellId : removeSpells) {
			removeSpell(plugin, spellId, forceDoNotLoopBardSpell);
		}

		for (SoliniaActiveSpell activeSpell : updateSpells) {
			activeSpells.put(activeSpell.getSpellId(), activeSpell);
		}
	}
}
