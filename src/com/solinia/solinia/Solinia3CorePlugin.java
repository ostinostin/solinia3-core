package com.solinia.solinia;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.solinia.solinia.Commands.*;
import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Listeners.DiscordListener;
import com.solinia.solinia.Listeners.Solinia3CoreBlockListener;
import com.solinia.solinia.Listeners.Solinia3CoreEntityListener;
import com.solinia.solinia.Listeners.Solinia3CoreItemPickupListener;
import com.solinia.solinia.Listeners.Solinia3CoreNPCUpdatedListener;
import com.solinia.solinia.Listeners.Solinia3CorePlayerChatListener;
import com.solinia.solinia.Listeners.Solinia3CorePlayerListener;
import com.solinia.solinia.Listeners.Solinia3CoreSpawnGroupUpdatedListener;
import com.solinia.solinia.Listeners.Solinia3CoreVehicleListener;
import com.solinia.solinia.Listeners.Solinia3CoreVoteListener;
import com.solinia.solinia.Managers.ChannelManager;
import com.solinia.solinia.Managers.ConfigurationManager;
import com.solinia.solinia.Managers.EntityManager;
import com.solinia.solinia.Managers.PlayerManager;
import com.solinia.solinia.Managers.StateManager;
import com.solinia.solinia.Providers.MythicMobsNPCEntityProvider;
import com.solinia.solinia.Repositories.JsonAAAbilityRepository;
import com.solinia.solinia.Repositories.JsonAccountClaimRepository;
import com.solinia.solinia.Repositories.JsonAlignmentRepository;
import com.solinia.solinia.Repositories.JsonCharacterListRepository;
import com.solinia.solinia.Repositories.JsonClassRepository;
import com.solinia.solinia.Repositories.JsonCraftRepository;
import com.solinia.solinia.Repositories.JsonFactionRepository;
import com.solinia.solinia.Repositories.JsonZoneRepository;
import com.solinia.solinia.Repositories.JsonItemRepository;
import com.solinia.solinia.Repositories.JsonLootDropRepository;
import com.solinia.solinia.Repositories.JsonLootTableRepository;
import com.solinia.solinia.Repositories.JsonNPCMerchantRepository;
import com.solinia.solinia.Repositories.JsonNPCRepository;
import com.solinia.solinia.Repositories.JsonNPCSpellListRepository;
import com.solinia.solinia.Repositories.JsonPatchRepository;
import com.solinia.solinia.Repositories.JsonPlayerRepository;
import com.solinia.solinia.Repositories.JsonQuestRepository;
import com.solinia.solinia.Repositories.JsonRaceRepository;
import com.solinia.solinia.Repositories.JsonSpawnGroupRepository;
import com.solinia.solinia.Repositories.JsonSpellRepository;
import com.solinia.solinia.Repositories.JsonWorldRepository;
import com.solinia.solinia.Repositories.JsonWorldWidePerkRepository;
import com.solinia.solinia.Timers.AttendenceXpBonusTimer;
import com.solinia.solinia.Timers.CSVGenerationTimer;
import com.solinia.solinia.Timers.CastingTimer;
import com.solinia.solinia.Timers.DiscordMessageTimer;
import com.solinia.solinia.Timers.EntityAutoAttackTimer;
import com.solinia.solinia.Timers.HintTimer;
import com.solinia.solinia.Timers.InvalidItemCheckerTimer;
import com.solinia.solinia.Timers.NPCCheckForEnemiesTimer;
import com.solinia.solinia.Timers.NPCRandomChatTimer;
import com.solinia.solinia.Timers.NPCSpellCastTimer;
import com.solinia.solinia.Timers.NPCSummonAndTeleportCastTimer;
import com.solinia.solinia.Timers.PerkLoadTimer;
import com.solinia.solinia.Timers.PetCheckTickTimer;
import com.solinia.solinia.Timers.PetFastCheckTimer;
import com.solinia.solinia.Timers.PlayerInteractionTimer;
import com.solinia.solinia.Timers.PlayerInventoryValidatorTimer;
import com.solinia.solinia.Timers.PlayerRegenTickTimer;
import com.solinia.solinia.Timers.SpellTickTimer;
import com.solinia.solinia.Timers.StateCommitTimer;
import com.solinia.solinia.Timers.UnsetPersonalityTimer;

import de.slikey.effectlib.EffectManager;
import me.dadus33.chatitem.api.ChatItemAPI;
import net.milkbowl.vault.economy.Economy;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

public class Solinia3CorePlugin extends JavaPlugin {

	private CSVGenerationTimer csvGenerationTimer;
	private CastingTimer castingTimer;
	private StateCommitTimer commitTimer;
	private PlayerRegenTickTimer playerRegenTimer;
	private SpellTickTimer spellTickTimer;
	private NPCSpellCastTimer npcSpellCastTimer;
	private NPCSummonAndTeleportCastTimer npcSummonCastTimer;
	private PlayerInteractionTimer playerInteractionTimer;
	private PlayerInventoryValidatorTimer playerInventoryValidatorTimer;
	private NPCRandomChatTimer npcRandomChatTimer;
	private NPCCheckForEnemiesTimer npcCheckForEnemiesTimer;
	private PetCheckTickTimer petCheckTickTimer;
	private PetFastCheckTimer petFastCheckTickTimer;
	private DiscordMessageTimer discordMessageTimer;
	private InvalidItemCheckerTimer invalidItemCheckerTimer;
	private EntityAutoAttackTimer entityAutoAttackTimer;
	FileConfiguration config = getConfig();
	private EffectManager effectManager;
	private AttendenceXpBonusTimer attendenceXpBonusTimer;
	private UnsetPersonalityTimer unsetPersonalityTimer;
	private HintTimer hintTimer;
	
	private Economy economy;
	private ChatItemAPI ciApi;
	private PerkLoadTimer perkLoadTimer;
	private IDiscordClient discordClient;
	
	String discordbottoken = "";
	String discordmainchannelid;
	String discordcontentteamchannelid;
	String discordadminchannelid;
	String discordincharacterchannelid;

	@Override
	public void onEnable() {
		System.out.println("[Solinia3Core] Plugin Enabled");
		createConfigDir();
		
		config.addDefault("discordbottoken", "");
		config.addDefault("discordmainchannelid", "");
		config.addDefault("discordadminchannelid", "");
		config.addDefault("discordcontentteamchannelid", "");
		config.addDefault("discordincharacterchannelid", "");
		config.options().copyDefaults(true);
		saveConfig();
		
		if (
				!config.getString("discordbottoken").equals("") && 
				!config.getString("discordmainchannelid").equals("") && 
				!config.getString("discordadminchannelid").equals("") &&  
				!config.getString("discordcontentteamchannelid").equals("") &&  
				!config.getString("discordincharacterchannelid").equals(""))
		{
			discordbottoken = config.getString("discordbottoken");
			discordmainchannelid = config.getString("discordmainchannelid");
			discordadminchannelid = config.getString("discordadminchannelid");
			discordcontentteamchannelid = config.getString("discordcontentteamchannelid");
			discordincharacterchannelid = config.getString("discordincharacterchannelid");
			setupDiscordClient();
		}
		
		System.out.println("All local dates stored in format: " + Locale.getDefault().toLanguageTag());
		
		// For debugging
		//new RuntimeTransformer( EntityDamageEventTransformer.class );
		
		effectManager = new EffectManager(this);

		initialise();
		registerEvents();

		setupEconomy();
		setupChatItem();

		StateManager.getInstance().setEconomy(this.economy);
		StateManager.getInstance().setChatItem(this.ciApi);
		StateManager.getInstance().setDiscordClient(this.discordClient);
		
		RegisterEntities();
	}
	
	private void RegisterEntities() {
		//NMSUtils.registerEntity("SoliniaMob", NMSUtils.Type.SKELETON, MythicEntitySoliniaMob.class, true);
		
		
	}
	
	private void UnregisterEntities() {
		
	}

	private void setupDiscordClient() {
		this.discordClient = createClient(this.discordbottoken, true);
		EventDispatcher dispatcher = this.discordClient.getDispatcher(); 
        dispatcher.registerListener(new DiscordListener(this));

	}

	public static IDiscordClient createClient(String token, boolean login) 
	{ 
        ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(token);
        try {
            if (login) {
                return clientBuilder.login();
            } else {
                return clientBuilder.build();
            }
        } catch (DiscordException e) {
            e.printStackTrace();
            return null;
        }
    }

	@Override
	public void onDisable() {
		try {
			StateManager.getInstance().getEntityManager().killAllPets();
			StateManager.getInstance().Commit();
		} catch (CoreStateInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		effectManager.dispose();
		
		UnregisterEntities();
		
		System.out.println("[Solinia3Core] Plugin Disabled");
	}

	private void setupChatItem() {
		ciApi = Bukkit.getServicesManager().getRegistration(ChatItemAPI.class).getProvider();
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
			economy = economyProvider.getProvider();
		return (economy != null);
	}

	private void initialise() {
		// TODO Lets load all this from config settings at some point

		try {
			JsonPlayerRepository repo = new JsonPlayerRepository();
			repo.setJsonFile(getDataFolder() + "/" + "players.json");
			repo.reload();

			JsonRaceRepository racerepo = new JsonRaceRepository();
			racerepo.setJsonFile(getDataFolder() + "/" + "races.json");
			racerepo.reload();

			JsonClassRepository classrepo = new JsonClassRepository();
			classrepo.setJsonFile(getDataFolder() + "/" + "classes.json");
			classrepo.reload();

			JsonItemRepository itemrepo = new JsonItemRepository();
			itemrepo.setJsonFile(getDataFolder() + "/" + "items.json");
			itemrepo.reload();

			JsonSpellRepository spellrepo = new JsonSpellRepository();
			spellrepo.setJsonFile(getDataFolder() + "/" + "spells.json");
			spellrepo.reload();

			JsonFactionRepository factionrepo = new JsonFactionRepository();
			factionrepo.setJsonFile(getDataFolder() + "/" + "factions.json");
			factionrepo.reload();

			JsonNPCRepository npcrepo = new JsonNPCRepository();
			npcrepo.setJsonFile(getDataFolder() + "/" + "npcs.json");
			npcrepo.reload();

			JsonNPCMerchantRepository npcmerchantrepo = new JsonNPCMerchantRepository();
			npcmerchantrepo.setJsonFile(getDataFolder() + "/" + "npcmerchants.json");
			npcmerchantrepo.reload();

			JsonLootTableRepository loottablerepo = new JsonLootTableRepository();
			loottablerepo.setJsonFile(getDataFolder() + "/" + "loottables.json");
			loottablerepo.reload();

			JsonLootDropRepository lootdroprepo = new JsonLootDropRepository();
			lootdroprepo.setJsonFile(getDataFolder() + "/" + "lootdrops.json");
			lootdroprepo.reload();

			JsonSpawnGroupRepository spawngrouprepo = new JsonSpawnGroupRepository();
			spawngrouprepo.setJsonFile(getDataFolder() + "/" + "spawngroups.json");
			spawngrouprepo.reload();

			// May be being written
			JsonWorldWidePerkRepository perkrepo = null;
			try
			{
				perkrepo = new JsonWorldWidePerkRepository();
				perkrepo.setJsonFile(getDataFolder() + "/" + "worldwideperks.json");
				perkrepo.reload();
			} catch (Exception e)
			{
				perkrepo = new JsonWorldWidePerkRepository();
			}

			JsonAAAbilityRepository aaabilityrepo = new JsonAAAbilityRepository();
			aaabilityrepo.setJsonFile(getDataFolder() + "/" + "aaabilities.json");
			aaabilityrepo.reload();

			JsonPatchRepository patchesrepo = new JsonPatchRepository();
			patchesrepo.setJsonFile(getDataFolder() + "/" + "patches.json");
			patchesrepo.reload();

			JsonQuestRepository questsrepo = new JsonQuestRepository();
			questsrepo.setJsonFile(getDataFolder() + "/" + "quests.json");
			questsrepo.reload();

			JsonAlignmentRepository alignmentsrepo = new JsonAlignmentRepository();
			alignmentsrepo.setJsonFile(getDataFolder() + "/" + "alignments.json");
			alignmentsrepo.reload();

			JsonCharacterListRepository characterlistrepo = new JsonCharacterListRepository();
			characterlistrepo.setJsonFile(getDataFolder() + "/" + "characterlists.json");
			characterlistrepo.reload();
			
			JsonNPCSpellListRepository npcspelllistrepo = new JsonNPCSpellListRepository();
			npcspelllistrepo.setJsonFile(getDataFolder() + "/" + "npcspelllists.json");
			npcspelllistrepo.reload();

			JsonAccountClaimRepository accountclaimsrepo = new JsonAccountClaimRepository();
			accountclaimsrepo.setJsonFile(getDataFolder() + "/" + "accountclaimrepo.json");
			accountclaimsrepo.reload();

			JsonZoneRepository zonesrepo = new JsonZoneRepository();
			zonesrepo.setJsonFile(getDataFolder() + "/" + "zones.json");
			zonesrepo.reload();

			JsonCraftRepository craftrepo = new JsonCraftRepository();
			craftrepo.setJsonFile(getDataFolder() + "/" + "craft.json");
			craftrepo.reload();

			JsonWorldRepository worldrepo = new JsonWorldRepository();
			worldrepo.setJsonFile(getDataFolder() + "/" + "worlds.json");
			worldrepo.reload();
			
			PlayerManager playerManager = new PlayerManager(repo);
			EntityManager entityManager = new EntityManager(this, new MythicMobsNPCEntityProvider());

			ConfigurationManager configurationManager = new ConfigurationManager(racerepo, classrepo, itemrepo,
					spellrepo, factionrepo, npcrepo, npcmerchantrepo, loottablerepo, lootdroprepo, spawngrouprepo,
					perkrepo, aaabilityrepo, patchesrepo, questsrepo, alignmentsrepo, characterlistrepo, npcspelllistrepo,
					accountclaimsrepo, zonesrepo, craftrepo, worldrepo);

			ChannelManager channelManager = new ChannelManager();
			
			if (this.discordClient != null)
			{
				channelManager.setDiscordMainChannelId(discordmainchannelid);
				channelManager.setDiscordAdminChannelId(discordadminchannelid);
				channelManager.setDiscordContentTeamChannelId(discordcontentteamchannelid);
				channelManager.setDiscordInCharacterChannelId(discordincharacterchannelid);
			}

			StateManager.getInstance().Initialise(playerManager, entityManager, configurationManager, channelManager, effectManager);

			commitTimer = new StateCommitTimer();
			commitTimer.runTaskTimer(this, 300 * 20L, 300 * 20L);

			playerRegenTimer = new PlayerRegenTickTimer();
			playerRegenTimer.runTaskTimer(this, 6 * 20L, 6 * 20L);

			spellTickTimer = new SpellTickTimer(this);
			spellTickTimer.runTaskTimer(this, 6 * 20L, 6 * 20L);

			playerInteractionTimer = new PlayerInteractionTimer();
			playerInteractionTimer.runTaskTimer(this, 6 * 20L, 6 * 20L);

			perkLoadTimer = new PerkLoadTimer();
			perkLoadTimer.runTaskTimer(this, 6 * 20L, 120 * 20L);

			// Only validate these things every 2 minutes
			playerInventoryValidatorTimer = new PlayerInventoryValidatorTimer();
			playerInventoryValidatorTimer.runTaskTimer(this, 120 * 20L, 120 * 20L);

			npcRandomChatTimer = new NPCRandomChatTimer();
			npcRandomChatTimer.runTaskTimer(this, 6 * 20L, 60 * 20L);

			npcCheckForEnemiesTimer = new NPCCheckForEnemiesTimer();
			npcCheckForEnemiesTimer.runTaskTimer(this, 1 * 20L, 1 * 20L);
			
			npcSpellCastTimer = new NPCSpellCastTimer(this);
			npcSpellCastTimer.runTaskTimer(this, 3 * 20L, 3 * 20L);

			npcSummonCastTimer = new NPCSummonAndTeleportCastTimer(this);
			npcSummonCastTimer.runTaskTimer(this, 6 * 20L, 6 * 20L);
			
			petCheckTickTimer = new PetCheckTickTimer();
			petCheckTickTimer.runTaskTimer(this, 1 * 20L, 1 * 20L);

			petFastCheckTickTimer = new PetFastCheckTimer();
			petFastCheckTickTimer.runTaskTimer(this, 1 * 20L, 1 * 20L);
			
			attendenceXpBonusTimer = new AttendenceXpBonusTimer();
			attendenceXpBonusTimer.runTaskTimer(this, 60 * 20L, 60 * 20L);

			invalidItemCheckerTimer = new InvalidItemCheckerTimer();
			invalidItemCheckerTimer.runTaskTimer(this, 60 * 20L, 60 * 20L);
			
			castingTimer = new CastingTimer();
			// every 100 milliseconds
			castingTimer.runTaskTimer(this, 0L, 1 * 2L);
			
			entityAutoAttackTimer = new EntityAutoAttackTimer();
			entityAutoAttackTimer.runTaskTimer(this, 0L, 1L);
			
			hintTimer = new HintTimer();
			hintTimer.runTaskTimer(this, 1800 * 20L, 1800 * 20L);

			
			if (this.discordClient != null)
			{
				discordMessageTimer = new DiscordMessageTimer();
				discordMessageTimer.runTaskTimer(this, 20L, 20L);
			}
			
			csvGenerationTimer = new CSVGenerationTimer();
			csvGenerationTimer.runTaskTimer(this, 1 * 20L, 3600 * 20L);
			
			unsetPersonalityTimer = new UnsetPersonalityTimer(this);
			unsetPersonalityTimer.runTaskTimer(this, 300 * 20L, 300 * 20L);

		} catch (CoreStateInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new Solinia3CorePlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new Solinia3CoreEntityListener(this), this);
		getServer().getPluginManager().registerEvents(new Solinia3CorePlayerChatListener(this), this);
		getServer().getPluginManager().registerEvents(new Solinia3CoreNPCUpdatedListener(this), this);
		getServer().getPluginManager().registerEvents(new Solinia3CoreSpawnGroupUpdatedListener(this), this);
		getServer().getPluginManager().registerEvents(new Solinia3CoreItemPickupListener(this), this);
		getServer().getPluginManager().registerEvents(new Solinia3CoreVehicleListener(this), this);
		getServer().getPluginManager().registerEvents(new Solinia3CoreVoteListener(this), this);
		getServer().getPluginManager().registerEvents(new Solinia3CoreBlockListener(this), this);

		this.getCommand("editchunk").setExecutor(new CommandEditChunk());
		this.getCommand("solinia").setExecutor(new CommandSolinia());
		this.getCommand("commit").setExecutor(new CommandCommit());
		this.getCommand("forename").setExecutor(new CommandForename());
		this.getCommand("lastname").setExecutor(new CommandLastname());
		this.getCommand("mana").setExecutor(new CommandMana());
		this.getCommand("addrace").setExecutor(new CommandAddRace());
		this.getCommand("setrace").setExecutor(new CommandSetRace());
		this.getCommand("addclass").setExecutor(new CommandAddClass());
		this.getCommand("setclass").setExecutor(new CommandSetClass());
		this.getCommand("addraceclass").setExecutor(new CommandAddRaceClass());
		this.getCommand("stats").setExecutor(new CommandStats());
		this.getCommand("resetplayer").setExecutor(new CommandResetPlayer());
		this.getCommand("who").setExecutor(new CommandWho());
		this.getCommand("emote").setExecutor(new CommandEmote());
		this.getCommand("roll").setExecutor(new CommandRoll());
		this.getCommand("setgender").setExecutor(new CommandSetGender());
		this.getCommand("setlanguage").setExecutor(new CommandSetLanguage());
		this.getCommand("tarot").setExecutor(new CommandTarot());
		this.getCommand("skills").setExecutor(new CommandSkills());
		this.getCommand("createitem").setExecutor(new CommandCreateItem());
		this.getCommand("listitems").setExecutor(new CommandListItems());
		this.getCommand("spawnitem").setExecutor(new CommandSpawnItem());
		this.getCommand("raceinfo").setExecutor(new CommandRaceInfo());
		this.getCommand("rebuildspellitems").setExecutor(new CommandRebuildSpellItems());
		this.getCommand("createfaction").setExecutor(new CommandCreateFaction());
		this.getCommand("createnpc").setExecutor(new CommandCreateNpc());
		this.getCommand("listfactions").setExecutor(new CommandListFactions());
		this.getCommand("listnpcs").setExecutor(new CommandListNPCs());
		this.getCommand("editnpc").setExecutor(new CommandEditNpc());
		this.getCommand("createloottable").setExecutor(new CommandCreateLootTable());
		this.getCommand("createlootdrop").setExecutor(new CommandCreateLootDrop());
		this.getCommand("addlootdropitem").setExecutor(new CommandAddLootDropItem());
		this.getCommand("addloottablelootdrop").setExecutor(new CommandAddLootTableLootDrop());
		this.getCommand("editspell").setExecutor(new CommandEditSpell());
		this.getCommand("listlootdrops").setExecutor(new CommandListLootDrops());
		this.getCommand("listloottables").setExecutor(new CommandListLootTables());
		this.getCommand("local").setExecutor(new CommandLocal());
		this.getCommand("forcelevel").setExecutor(new CommandForceLevel());
		this.getCommand("createmerchantlist").setExecutor(new CommandCreateMerchantList());
		this.getCommand("addmerchantitem").setExecutor(new CommandAddMerchantItem());
		this.getCommand("listmerchantlists").setExecutor(new CommandListMerchantLists());
		this.getCommand("edititem").setExecutor(new CommandEditItem());
		this.getCommand("createspawngroup").setExecutor(new CommandCreateSpawnGroup());
		this.getCommand("listspawngroups").setExecutor(new CommandListSpawnGroups());
		this.getCommand("updatespawngrouploc").setExecutor(new CommandUpdateSpawnGroupLoc());
		this.getCommand("ooc").setExecutor(new CommandOoc());
		this.getCommand("setchannel").setExecutor(new CommandSetChannel());
		this.getCommand("group").setExecutor(new CommandGroup());
		this.getCommand("groupchat").setExecutor(new CommandGroupChat());
		this.getCommand("convertmerchanttolootdrop").setExecutor(new CommandConvertMerchantToLootDrop());
		this.getCommand("perks").setExecutor(new CommandPerks());
		this.getCommand("effects").setExecutor(new CommandEffects(this));
		this.getCommand("editclass").setExecutor(new CommandEditClass());
		this.getCommand("createarmorset").setExecutor(new CommandCreateArmourSet());
		this.getCommand("createnpccopy").setExecutor(new CommandCreateNpcCopy());
		this.getCommand("listspells").setExecutor(new CommandListSpells());
		this.getCommand("editloottable").setExecutor(new CommandEditLootTable());
		this.getCommand("editlootdrop").setExecutor(new CommandEditLootDrop());
		this.getCommand("pet").setExecutor(new CommandPet());
		this.getCommand("trance").setExecutor(new CommandTrance());
		this.getCommand("aa").setExecutor(new CommandAA());
		this.getCommand("toggleaa").setExecutor(new CommandToggleAA());
		this.getCommand("createnpcevent").setExecutor(new CommandCreateNPCEvent());
		this.getCommand("editrace").setExecutor(new CommandEditRace());
		this.getCommand("editspawngroup").setExecutor(new CommandEditSpawngroup());
		this.getCommand("faction").setExecutor(new CommandFaction());
		this.getCommand("solignore").setExecutor(new CommandIgnore());
		this.getCommand("editfaction").setExecutor(new CommandEditFaction());
		this.getCommand("settitle").setExecutor(new CommandSetTitle());
		this.getCommand("granttitle").setExecutor(new CommandGrantTitle());
		this.getCommand("editnpcevent").setExecutor(new CommandEditNpcEvent());
		this.getCommand("createquest").setExecutor(new CommandCreateQuest());
		this.getCommand("quests").setExecutor(new CommandQuests());
		this.getCommand("npcgive").setExecutor(new CommandNPCGive());		
		this.getCommand("today").setExecutor(new CommandToday());		
		this.getCommand("listaas").setExecutor(new CommandListAAs());		
		this.getCommand("editaa").setExecutor(new CommandEditAA());		
		this.getCommand("loot").setExecutor(new CommandLoot());		
		this.getCommand("createallarmoursets").setExecutor(new CommandCreateAllArmourSets());
		this.getCommand("createalignment").setExecutor(new CommandCreateAlignment());
		this.getCommand("specialise").setExecutor(new CommandSpecialise());
		this.getCommand("bite").setExecutor(new CommandBite());
		this.getCommand("character").setExecutor(new CommandCharacter(this));
		this.getCommand("inspiration").setExecutor(new CommandInspiration());
		this.getCommand("listnpcspells").setExecutor(new CommandListNpcSpells());
		this.getCommand("editnpcspelllist").setExecutor(new CommandEditNpcSpellList());
		this.getCommand("claim").setExecutor(new CommandClaim());
		this.getCommand("hideooc").setExecutor(new CommandHideOoc());
		this.getCommand("skillcheck").setExecutor(new CommandSkillCheck());
		this.getCommand("createzone").setExecutor(new CommandCreateZone());
		this.getCommand("editzone").setExecutor(new CommandEditZone());
		this.getCommand("listzones").setExecutor(new CommandListZones());
		this.getCommand("hotzones").setExecutor(new CommandHotzones());
		this.getCommand("listraces").setExecutor(new CommandListRaces());
		this.getCommand("listclasses").setExecutor(new CommandListClasses());
		this.getCommand("equip").setExecutor(new CommandEquip());
		this.getCommand("craft").setExecutor(new CommandCraft());
		this.getCommand("createcraft").setExecutor(new CommandCreateCraft());
		this.getCommand("listcrafts").setExecutor(new CommandListCrafts());
		this.getCommand("editcraft").setExecutor(new CommandEditCraft());
		this.getCommand("npcsay").setExecutor(new CommandNPCSay());
		this.getCommand("listworlds").setExecutor(new CommandListWorlds());
		this.getCommand("editworld").setExecutor(new CommandEditWorld());
		this.getCommand("reagent").setExecutor(new CommandReagent());
		this.getCommand("setspouse").setExecutor(new CommandSetSpouse());
		this.getCommand("setmother").setExecutor(new CommandSetMother());
		this.getCommand("setmain").setExecutor(new CommandSetMain());
		this.getCommand("spellbook").setExecutor(new CommandSpellBook());
		this.getCommand("groupselect").setExecutor(new CommandGroupSelect());
		this.getCommand("listquests").setExecutor(new CommandListQuests());
		this.getCommand("editquest").setExecutor(new CommandEditQuest());
		this.getCommand("spawnnpc").setExecutor(new CommandSpawnNpc());
		this.getCommand("toggleglow").setExecutor(new CommandToggleGlow());
		this.getCommand("bindwound").setExecutor(new CommandBindWound());
		this.getCommand("editmerchantlist").setExecutor(new CommandEditMerchantList());
		this.getCommand("claimxp").setExecutor(new CommandClaimXp());
		this.getCommand("showdiscord").setExecutor(new CommandShowDiscord());
		this.getCommand("publishbook").setExecutor(new CommandPublishBook());
		this.getCommand("editalignment").setExecutor(new CommandEditAlignment());
		this.getCommand("oath").setExecutor(new CommandOath());
		this.getCommand("personality").setExecutor(new CommandPersonality());
		this.getCommand("resetpersonality").setExecutor(new CommandResetPersonality());
	}

	private void createConfigDir() {
		try {
			if (!getDataFolder().exists()) {
				getDataFolder().mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
