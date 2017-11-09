package com.solinia.solinia.Interfaces;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import com.solinia.solinia.Models.PlayerFactionEntry;
import com.solinia.solinia.Models.PlayerQuest;
import com.solinia.solinia.Models.SoliniaAARankEffect;
import com.solinia.solinia.Models.SoliniaPlayerSkill;
import com.solinia.solinia.Models.SpellEffectType;
import com.solinia.solinia.Models.SpellResistType;

public interface ISoliniaPlayer extends Serializable {
	public UUID getUUID();

	void setUUID(UUID uuid);

	public String getForename();

	void setForename(String forename);

	public String getLastname();

	void setLastname(String lastname);

	public void updateDisplayName();
	
	public void updateMaxHp();

	public String getFullName();

	public int getMana();

	void setMana(int mana);

	public Double getAAExperience();

	void setAAExperience(Double aaexperience);

	public Double getExperience();

	void setExperience(Double experience);

	public int getLevel();

	public int getRaceId();

	public boolean hasChosenRace();

	void setChosenRace(boolean chosen);

	void setRaceId(int raceid);

	public ISoliniaRace getRace();

	public boolean hasChosenClass();

	public int getClassId();

	public ISoliniaClass getClassObj();
	
	void setClassId(int classid);

	void setChosenClass(boolean haschosenclass);

	Player getBukkitPlayer();

	void increasePlayerExperience(Double experience);

	void increasePlayerNormalExperience(Double experience);
	
	void increasePlayerAAExperience(Double experience);

	public void giveMoney(int i);

	int getAAPoints();

	void setAAPoints(int aapoints);

	public int getSkillCap(String skillName);

	public List<SoliniaPlayerSkill> getSkills();

	String getLanguage();

	void setLanguage(String language);

	String getGender();

	void setGender(String gender);

	public void say(String string);

	public SoliniaPlayerSkill getSkill(String skillname);

	public void tryIncreaseSkill(String skillname, int xp);
	
	public void setSkill(String skillname, int value);

	void reducePlayerMana(int mana);

	void increasePlayerMana(int mana);

	void emote(String string);

	UUID getInteraction();

	public void ooc(String message);

	String getCurrentChannel();

	void setCurrentChannel(String currentChannel);

	public boolean understandsLanguage(String language);

	public void tryImproveLanguage(String language);

	public ISoliniaGroup getGroup();

	public int getResist(SpellResistType type);

	int getTotalResist(SpellResistType type);

	int getAapct();

	void setAapct(int aapct);

	public List<ISoliniaAARank> getBuyableAARanks();

	boolean canPurchaseAlternateAdvancementRank(ISoliniaAAAbility ability, ISoliniaAARank rank);

	boolean hasRank(ISoliniaAARank rank);

	boolean hasPreviousRanks(ISoliniaAAAbility ability, ISoliniaAARank rank);

	boolean canUseAlternateAdvancementRank(ISoliniaAAAbility ability, ISoliniaAARank rank);

	void purchaseAlternateAdvancementRank(ISoliniaAAAbility ability, ISoliniaAARank rank);

	boolean hasPrerequisites(ISoliniaAAAbility ability, ISoliniaAARank rank);

	boolean hasAAAbility(int abilityid);

	void interact(Plugin plugin, PlayerInteractEvent event);

	void setInteraction(UUID interaction, ISoliniaNPC npc);

	public boolean canDodge();

	public boolean canRiposte();

	public boolean canDoubleAttack();

	boolean getDoubleAttackCheck();

	boolean getDodgeCheck();

	boolean getRiposteCheck();

	public boolean getSafefallCheck();

	List<PlayerFactionEntry> getFactionEntries();

	void setFactionEntries(List<PlayerFactionEntry> factionEntries);

	public void decreaseFactionStanding(int factionid, int i);

	PlayerFactionEntry getFactionEntry(int factionId);

	PlayerFactionEntry createPlayerFactionEntry(int factionId);

	void ignorePlayer(Player player);

	List<UUID> getIgnoredPlayers();

	void setIgnoredPlayers(List<UUID> ignoredPlayers);

	boolean hasIgnored(UUID uuid);

	List<String> getAvailableTitles();

	void setAvailableTitles(List<String> availableTitles);

	String getTitle();

	void setTitle(String title);

	String getFullNameWithTitle();

	boolean grantTitle(String title);

	void increaseFactionStanding(int factionId, int amount);

	boolean isMezzed();

	List<PlayerQuest> getPlayerQuests();

	void setPlayerQuests(List<PlayerQuest> playerQuests);

	public void addPlayerQuest(int questId);

	public List<String> getPlayerQuestFlags();

	public void addPlayerQuestFlag(String questFlag);

	void setPlayerQuestFlags(List<String> playerQuestFlags);
	
	List<SoliniaAARankEffect> getRanksEffectsOfEffectType(int effectId);

	public boolean isMeditating();

	public void setSkills(List<SoliniaPlayerSkill> skillCache);

	void doCastSpellItem(Plugin plugin, ISoliniaSpell spell, Player player, ISoliniaItem spellSourceItem);

	boolean checkFizzle(ISoliniaSpell spell);

	void dropResurrectionItem(int experienceamount);

	void reducePlayerNormalExperience(Double experience);

	public void setFealty(UUID uniqueId);

	UUID getFealty();

	boolean isRacialKing();
}
