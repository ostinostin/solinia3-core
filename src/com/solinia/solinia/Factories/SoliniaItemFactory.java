package com.solinia.solinia.Factories;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Exceptions.SoliniaItemException;
import com.solinia.solinia.Interfaces.ISoliniaClass;
import com.solinia.solinia.Interfaces.ISoliniaItem;
import com.solinia.solinia.Managers.ConfigurationManager;
import com.solinia.solinia.Managers.StateManager;
import com.solinia.solinia.Models.EquipmentSlot;
import com.solinia.solinia.Models.ItemGenBonusStatType;
import com.solinia.solinia.Models.ItemType;
import com.solinia.solinia.Models.SoliniaItem;
import com.solinia.solinia.Utils.ItemStackUtils;
import com.solinia.solinia.Utils.MathUtils;
import com.solinia.solinia.Utils.TextUtils;

public class SoliniaItemFactory {
	public static ISoliniaItem CreateItem(ItemStack itemStack, boolean commit) throws SoliniaItemException, CoreStateInitException {
		SoliniaItem item = new SoliniaItem();
		if (commit)
		item.setId(StateManager.getInstance().getConfigurationManager().getNextItemId());
		item.setBasename(itemStack.getType().name());
		item.setDisplayname(itemStack.getType().name());
		item.setLastUpdatedTimeNow();
		item.setPlaceable(false);
		item.setItemType(ItemType.None);
		
		// ItemType auto configuration
		if (ConfigurationManager.HandMaterials.contains(item.getBasename().toUpperCase()))
		{
			switch (ItemStackUtils.getDefaultSkillForMaterial(itemStack.getType()))
			{
				case "SLASHING":
					item.setItemType(ItemType.OneHandSlashing);
					break;
				case "PIERCING":
					item.setItemType(ItemType.OneHandPiercing);
					break;
				case "CRUSHING":
					item.setItemType(ItemType.OneHandBlunt);
					break;
				case "ARCHERY":
					item.setItemType(ItemType.BowArchery);
					break;
				default:
					item.setItemType(ItemType.OneHandBlunt);
					break;
			}
		}
		
		if (ConfigurationManager.ArmourMaterials.contains(item.getBasename().toUpperCase()))
		{
			item.setItemType(ItemType.Clothing);
		}
		
		try
		{
			if (itemStack.getData() != null)
			{
				try
				{
					item.setColor(itemStack.getData().getData());
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			if (ItemStackUtils.isSkullItem(itemStack))
				item.setTexturebase64(ItemStackUtils.getSkullTexture(itemStack));
		} catch (NullPointerException e)
		{
			// For the cases where bukkit isnt initalised
		}
		
		
		
		if (itemStack.getType().name().equals("WRITTEN_BOOK"))
	    {
			BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
			item.setBookAuthor(bookMeta.getAuthor());
			item.setBookPages(bookMeta.getPages());
			item.setDisplayname(bookMeta.getTitle());
	    }
		
		if (commit)
		StateManager.getInstance().getConfigurationManager().addItem(item);
		if (commit)
		StateManager.getInstance().getConfigurationManager().setItemsChanged(true);
		return item;
	}
	
	public static ISoliniaItem CreateItemCopy(ISoliniaItem originalItem) throws SoliniaItemException, CoreStateInitException {
		StateManager.getInstance().getConfigurationManager().setItemsChanged(true);
		ItemStack itemStack = originalItem.asItemStack();		
		
		SoliniaItem item = new SoliniaItem();
		item.setId(StateManager.getInstance().getConfigurationManager().getNextItemId());
		item.setBasename(itemStack.getType().name());
		item.setDisplayname(originalItem.getDisplayname());
		item.setEquipmentSlot(originalItem.getEquipmentSlot());
		item.setDefinedItemDamage(originalItem.getDefinedItemDamage());
		item.setAC(originalItem.getAC());
		item.setLastUpdatedTimeNow();
		item.setPlaceable(false);
		item.setItemType(originalItem.getItemType());
		item.setWeaponDelay(originalItem.getWeaponDelay());
		item.setAugmentation(originalItem.isAugmentation());
		item.setAugmentationFitsSlotType(originalItem.getAugmentationFitsSlotType());
		item.setAllowedClassNames(originalItem.getAllowedClassNamesUpper());
		item.setAllowedRaceNames(originalItem.getAllowedRaceNamesUpper());
		
		if (itemStack.getData() != null)
		{
			try
			{
				item.setColor(itemStack.getData().getData());
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		if (ItemStackUtils.isSkullItem(itemStack))
			item.setTexturebase64(ItemStackUtils.getSkullTexture(itemStack));
		
		if (itemStack.getType().name().equals("WRITTEN_BOOK"))
	    {
			BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
			item.setBookAuthor(bookMeta.getAuthor());
			item.setBookPages(bookMeta.getPages());
			item.setDisplayname(bookMeta.getTitle());
	    }
		
		StateManager.getInstance().getConfigurationManager().addItem(item);
		StateManager.getInstance().getConfigurationManager().setItemsChanged(true);
		return item;
	}

	public static List<Integer> CreateClassItemSet(ISoliniaClass classtype, int armourtier, String partialname, boolean prefixClassName, String discoverer, boolean partialNameIsPrefix) throws SoliniaItemException {
		if (classtype == null)
			return new ArrayList<Integer>();
		
		List<Integer> items = new ArrayList<Integer>();
		
		try
		{
			StateManager.getInstance().getConfigurationManager().setItemsChanged(true);
			// Get the appropriate material for the class and generate the base item
			ISoliniaItem headItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.valueOf(classtype.getDefaultHeadMaterial().toUpperCase())), true);
			headItem.setDiscoverer(discoverer);
			headItem.setAppearanceId(classtype.getAppearanceId());
			if (classtype.getDefaultHeadMaterial().toUpperCase().equals("LEATHER_HELMET") && classtype.getLeatherRgbDecimal() > 0)
				headItem.setLeatherRgbDecimal(classtype.getLeatherRgbDecimal());
			
			ISoliniaItem chestItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.valueOf(classtype.getDefaultChestMaterial().toUpperCase())), true);
			chestItem.setDiscoverer(discoverer);
			chestItem.setAppearanceId(classtype.getAppearanceId());
			if (classtype.getDefaultChestMaterial().toUpperCase().equals("LEATHER_CHESTPLATE") && classtype.getLeatherRgbDecimal() > 0)
				chestItem.setLeatherRgbDecimal(classtype.getLeatherRgbDecimal());

			ISoliniaItem legsItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.valueOf(classtype.getDefaultLegsMaterial().toUpperCase())), true);
			legsItem.setDiscoverer(discoverer);
			legsItem.setAppearanceId(classtype.getAppearanceId());
			if (classtype.getDefaultLegsMaterial().toUpperCase().equals("LEATHER_LEGGINGS") && classtype.getLeatherRgbDecimal() > 0)
				legsItem.setLeatherRgbDecimal(classtype.getLeatherRgbDecimal());
			
			ISoliniaItem feetItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.valueOf(classtype.getDefaultFeetMaterial().toUpperCase())), true);
			feetItem.setDiscoverer(discoverer);
			feetItem.setAppearanceId(classtype.getAppearanceId());
			if (classtype.getDefaultFeetMaterial().toUpperCase().equals("LEATHER_BOOTS") && classtype.getLeatherRgbDecimal() > 0)
				feetItem.setLeatherRgbDecimal(classtype.getLeatherRgbDecimal());
			
			ISoliniaItem handItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.valueOf(classtype.getDefaulthandMaterial().toUpperCase())), true);
			handItem.setDiscoverer(discoverer);
			handItem.setItemType(classtype.getDefaultHandItemType());
			handItem.setAppearanceId(classtype.getAppearanceId());
			ISoliniaItem offhandItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.valueOf(classtype.getDefaultoffHandMaterial().toUpperCase())), true);
			offhandItem.setDiscoverer(discoverer);
			offhandItem.setItemType(classtype.getDefaultOffHandItemType());
			offhandItem.setAppearanceId(classtype.getAppearanceId());
			
			ISoliniaItem alternateHandItem = null;
			if (!classtype.getDefaultAlternateHandMaterial().toUpperCase().equals("NONE"))
			{
				alternateHandItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.valueOf(classtype.getDefaultAlternateHandMaterial().toUpperCase())), true);
				alternateHandItem.setDiscoverer(discoverer);
				alternateHandItem.setItemType(classtype.getDefaultAlternateHandItemType());
				alternateHandItem.setAppearanceId(classtype.getAppearanceId());
			}

			ISoliniaItem neckItemTemplate = null;
			if (classtype.getNeckItemIconItemId() > 0)
				neckItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getNeckItemIconItemId());
			
			ISoliniaItem shouldersItemTemplate = null;
			if (classtype.getShouldersItemIconItemId() > 0)
				shouldersItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getShouldersItemIconItemId());
			
			ISoliniaItem fingersItemTemplate = null;
			if (classtype.getFingersItemIconItemId() > 0)
				fingersItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getFingersItemIconItemId());
			
			ISoliniaItem earsItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getEarsItemIconItemId());
			if (classtype.getEarsItemIconItemId() > 0)
				earsItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getEarsItemIconItemId());
			
			ISoliniaItem forearmsItemTemplate = null;
			if (classtype.getForearmsItemIconItemId() > 0)
				forearmsItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getForearmsItemIconItemId());
			
			ISoliniaItem armsItemTemplate = null;
			if (classtype.getArmsItemIconItemId() > 0)
				armsItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getArmsItemIconItemId());
			
			ISoliniaItem handsItemTemplate = null;
			if (classtype.getHandsItemIconItemId() > 0)
				handsItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getHandsItemIconItemId());

			ISoliniaItem waistItemTemplate = null;
			if (classtype.getWaistItemIconItemId() > 0)
				waistItemTemplate = StateManager.getInstance().getConfigurationManager().getItem(classtype.getWaistItemIconItemId());
			
			
			// Jewelry!
			ISoliniaItem neckItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.PLAYER_HEAD), true);
			neckItem.setEquipmentSlot(EquipmentSlot.Neck);
			neckItem.setDiscoverer(discoverer);
			if (neckItemTemplate != null)
				neckItem.setTexturebase64(neckItemTemplate.getTexturebase64());
			else
				neckItem.setTexturebase64(ItemStackUtils.Neck);
			
			ISoliniaItem shouldersItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.PLAYER_HEAD), true);
			shouldersItem.setEquipmentSlot(EquipmentSlot.Shoulders);
			shouldersItem.setDiscoverer(discoverer);
			if (shouldersItemTemplate != null)
				shouldersItem.setTexturebase64(shouldersItemTemplate.getTexturebase64());
			else
				shouldersItem.setTexturebase64(ItemStackUtils.Shoulders);
			
			ISoliniaItem fingersItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.PLAYER_HEAD), true);
			fingersItem.setEquipmentSlot(EquipmentSlot.Fingers);
			fingersItem.setDiscoverer(discoverer);
			if (fingersItemTemplate != null)
				fingersItem.setTexturebase64(fingersItemTemplate.getTexturebase64());
			else
				fingersItem.setTexturebase64(ItemStackUtils.Fingers);
			
			ISoliniaItem earsItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.PLAYER_HEAD), true);
			earsItem.setEquipmentSlot(EquipmentSlot.Ears);
			earsItem.setDiscoverer(discoverer);
			if (earsItemTemplate != null)
				earsItem.setTexturebase64(earsItemTemplate.getTexturebase64());
			else
				earsItem.setTexturebase64(ItemStackUtils.Ears);

			// Additional Armour!
			ISoliniaItem forearmsItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.PLAYER_HEAD), true);
			forearmsItem.setEquipmentSlot(EquipmentSlot.Forearms);
			forearmsItem.setDiscoverer(discoverer);
			if (forearmsItemTemplate != null)
				forearmsItem.setTexturebase64(forearmsItemTemplate.getTexturebase64());
			else
				forearmsItem.setTexturebase64(ItemStackUtils.Forearms);
			
			ISoliniaItem armsItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.PLAYER_HEAD), true);
			armsItem.setEquipmentSlot(EquipmentSlot.Arms);
			armsItem.setDiscoverer(discoverer);
			if (armsItemTemplate != null)
				armsItem.setTexturebase64(armsItemTemplate.getTexturebase64());
			else
				armsItem.setTexturebase64(ItemStackUtils.Arms);
			
			ISoliniaItem handsItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.PLAYER_HEAD), true);
			handsItem.setEquipmentSlot(EquipmentSlot.Hands);
			handsItem.setDiscoverer(discoverer);
			if (handsItemTemplate != null)
				handsItem.setTexturebase64(handsItemTemplate.getTexturebase64());
			else
				handsItem.setTexturebase64(ItemStackUtils.Hands);

			ISoliniaItem waistItem = SoliniaItemFactory.CreateItem(new ItemStack(Material.PLAYER_HEAD), true);
			waistItem.setEquipmentSlot(EquipmentSlot.Waist);
			waistItem.setDiscoverer(discoverer);
			if (waistItemTemplate != null)
				waistItem.setTexturebase64(waistItemTemplate.getTexturebase64());
			else
				waistItem.setTexturebase64(ItemStackUtils.Waist);
			
			items.add(headItem.getId());
			items.add(chestItem.getId());
			items.add(legsItem.getId());
			items.add(feetItem.getId());
			items.add(handItem.getId());
			items.add(offhandItem.getId());
			if (alternateHandItem != null)
			items.add(alternateHandItem.getId());
			items.add(neckItem.getId());
			items.add(shouldersItem.getId());
			items.add(fingersItem.getId());
			items.add(earsItem.getId());
			items.add(forearmsItem.getId());
			items.add(armsItem.getId());
			items.add(handsItem.getId());
			items.add(waistItem.getId());
			
			for(Integer i : items)
			{
				ISoliniaItem item = StateManager.getInstance().getConfigurationManager().getItem(i);
				item.setPlaceable(false);
				List<String> classNames = new ArrayList<String>();
				List<String> raceNames = new ArrayList<String>();
				classNames.add(classtype.getName().toUpperCase());
				item.setAllowedClassNames(classNames);
				item.setAllowedRaceNames(raceNames);
				
				item.setWorth(armourtier*15);
				// Randomise the stats of the class armour so we get more unique content in each dungeon
				int rarityChance = MathUtils.RandomBetween(1, 100);
				int rarityBonus = 0;
				String rarityName = "";

				if (rarityChance > 80) {
					rarityName = "Uncommon ";
					rarityBonus = 1;
				}

				if (rarityChance > 85) {
					rarityName = "Rare ";
					rarityBonus = 2;
				}

				if (rarityChance > 92) {
					rarityName = "Legendary ";
					rarityBonus = 3;
				}
				
				if (rarityChance > 96) {
					rarityName = "Mythical ";
					rarityBonus = 4;
				}

				if (rarityChance > 98) {
					rarityName = "Ancient ";
					rarityBonus = 5;
				}

				
				String className = "";
				if (prefixClassName == true)
				{
					className = classtype.getClassItemPrefix().toLowerCase();
					className = TextUtils.FormatAsName(className);
					className += " ";
				}
				
				if (!item.isJewelry() && !item.isAdditionalArmour())
				{
					if (partialNameIsPrefix)
						item.setDisplayname(partialname + " " + rarityName + className + classtype.getItemArmorTypeName(item.getBasename().toUpperCase()));
					else
						item.setDisplayname(rarityName + className + classtype.getItemArmorTypeName(item.getBasename().toUpperCase()) + " " + partialname);
				} else {
					if (item.isJewelry())
					{
						String jewelryTypeName = "Jewelry";
						
						if (item.getEquipmentSlot().equals(EquipmentSlot.Ears))
							jewelryTypeName = "Earrings";
	
						if (item.getEquipmentSlot().equals(EquipmentSlot.Neck))
							jewelryTypeName = "Necklace";
						
						if (item.getEquipmentSlot().equals(EquipmentSlot.Fingers))
							jewelryTypeName = "Rings";
	
						if (item.getEquipmentSlot().equals(EquipmentSlot.Shoulders))
							jewelryTypeName = "Cloak";
						
						if (partialNameIsPrefix)
							item.setDisplayname(partialname + " " + rarityName + className + jewelryTypeName);
						else
							item.setDisplayname(rarityName + className + jewelryTypeName + " " + partialname);
					}
					
					if (item.isAdditionalArmour())
					{
						String jewelryTypeName = "Clothing";
						
						if (item.getEquipmentSlot().equals(EquipmentSlot.Forearms))
							jewelryTypeName = "Bracers";
	
						if (item.getEquipmentSlot().equals(EquipmentSlot.Arms))
							jewelryTypeName = "Sleeves";
						
						if (item.getEquipmentSlot().equals(EquipmentSlot.Hands))
							jewelryTypeName = "Gloves";
	
						if (item.getEquipmentSlot().equals(EquipmentSlot.Waist))
							jewelryTypeName = "Belt";
						
						if (partialNameIsPrefix)
							item.setDisplayname(partialname + " " + rarityName + className + jewelryTypeName);
						else
						item.setDisplayname(rarityName + className + jewelryTypeName + " " + partialname);
					}
				}
				
				int baseAmount = getBaseAmount(item);
				
				int tierMin = getTierMin(item, armourtier, baseAmount);
				int tierMax = getTierMax(item, armourtier, baseAmount);
				
				setMinLevel(item, armourtier);
				
				int classStrBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.Strength);
				int classStaBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.Stamina);
				int classAgiBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.Agility);
				int classDexBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.Dexterity);
				int classIntBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.Intelligence);
				int classWisBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.Wisdom);
				int classChaBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.Charisma);
				int classAcBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.AC);
				int classDelayBonus = classtype.getItemGenerationBonus(ItemGenBonusStatType.WeaponDelay);
				

				// Unless there is a bonus defined, the class doesnt seem to use that statistic
				
				if (classStrBonus > 0)
					item.setStrength(MathUtils.RandomBetween(tierMin, tierMax) + rarityBonus + classStrBonus);
				if (classStaBonus > 0)
					item.setStamina(MathUtils.RandomBetween(tierMin, tierMax) + rarityBonus + classStaBonus);
				if (classAgiBonus > 0)
					item.setAgility(MathUtils.RandomBetween(tierMin, tierMax) + rarityBonus + classAgiBonus);
				if (classDexBonus > 0)
					item.setDexterity(MathUtils.RandomBetween(tierMin, tierMax) + rarityBonus + classDexBonus);
				if (classIntBonus > 0)
					item.setIntelligence(MathUtils.RandomBetween(tierMin, tierMax) + rarityBonus+classIntBonus);
				if (classWisBonus > 0)
					item.setWisdom(MathUtils.RandomBetween(tierMin, tierMax) + rarityBonus+classWisBonus);
				if (classChaBonus > 0)
					item.setCharisma(MathUtils.RandomBetween(tierMin, tierMax) + rarityBonus+classChaBonus);
				
				
				
				setItemDamageAndAcAndDelay(item, armourtier, tierMin, tierMax, classAcBonus, rarityBonus, classStrBonus, classDelayBonus, item.getItemType());
				
				// mana
				item.setMana(MathUtils.RandomBetween(0,armourtier * 20)+rarityBonus);
								
				// hp
				item.setHp(MathUtils.RandomBetween(0,armourtier * 20)+rarityBonus);
				
				// resists
				item.setColdResist(MathUtils.RandomBetween(0,(int)Math.floor(armourtier * 0.7) + rarityBonus));
				item.setFireResist(MathUtils.RandomBetween(0,(int)Math.floor(armourtier * 0.7) + rarityBonus));
				item.setMagicResist(MathUtils.RandomBetween(0,(int)Math.floor(armourtier * 0.7) + rarityBonus));
				item.setPoisonResist(MathUtils.RandomBetween(0,(int)Math.floor(armourtier * 0.7) + rarityBonus));
				item.setDiseaseResist(MathUtils.RandomBetween(0,(int)Math.floor(armourtier * 0.7) + rarityBonus));
				
				// TODO class procs?
				StateManager.getInstance().getConfigurationManager().setItemsChanged(true);
			}
		
		} catch (CoreStateInitException e)
		{
			return new ArrayList<Integer>();
		}
		
		return items;
	}
	
	public static void setMinLevel(ISoliniaItem item, int tier) {
		int lvlMin = 1;
		if (tier > 1)
		{
			lvlMin = (tier-1) * 10;
		}
		
		item.setMinLevel(lvlMin);
	}

	public static int getBaseAmount(ISoliniaItem item) {
		int baseAmount = 2;
		
		if (item.isJewelry())
		{
			baseAmount = 1;
		}
		return baseAmount;
	}

	public static int getTierMin(ISoliniaItem item, int tier, int baseAmount)
	{
		int tierMin = 0;
		if (tier > 1)
			tierMin =+ (baseAmount * tier) - baseAmount;
		
		return tierMin;
	}
	
	public static int getTierMax(ISoliniaItem item, int tier, int baseAmount)
	{
		int tierMax = tier * baseAmount;
		return tierMax;
	}

	public static void setItemDamageAndAcAndDelay(ISoliniaItem item, int tier, int tierMin, int tierMax, int acBonus, int rarityBonus, int damageBonus, int delayBonus, ItemType itemType) {
		// Damage
		if (ConfigurationManager.HandMaterials.contains(item.getBasename().toUpperCase()))
		{
			if (!item.getBasename().toUpperCase().equals("SHIELD"))
			{
				int dmgMin = tierMin;
				int dmgMax = tierMax;
				if (dmgMin < 6)
				{
					dmgMin = 6;
				}
				
				if (dmgMax < 7)
					dmgMax = 7;
				
				int damage = MathUtils.RandomBetween(dmgMin, dmgMax) + rarityBonus + damageBonus;
				if (itemType.equals(ItemType.TwoHandBlunt) || itemType.equals(ItemType.TwoHandPiercing) || itemType.equals(ItemType.TwoHandSlashing) )
					damage = damage*2;
				
				item.setDefinedItemDamage(damage);
				
				int delay = (item.getWeaponDelay() - delayBonus);
				if (itemType.equals(ItemType.TwoHandBlunt) || itemType.equals(ItemType.TwoHandPiercing) || itemType.equals(ItemType.TwoHandSlashing) )
					delay = delay+((delay/5)*3);
				
				item.setWeaponDelay(delay);
				
			} else {
				item.setAC(SoliniaItemFactory.generateArmourClass(acBonus, tier, rarityBonus));
			}
		}
		if (ConfigurationManager.ArmourMaterials.contains(item.getBasename().toUpperCase()) || item.isAdditionalArmour())
		{
			item.setAC(SoliniaItemFactory.generateArmourClass(acBonus, tier, rarityBonus));
		}
		if (item.isJewelry())
		{
			int ac = SoliniaItemFactory.generateArmourClass(acBonus, tier, rarityBonus);
			if (ac > 0)
			{
				ac = (int)Math.floor(ac/2);
				item.setAC(ac);
			}
		}
	}

	public static int generateArmourClass(int classAcBonus, int armourTier, int rarityBonus) {
		// AC
		int acMultiplier = classAcBonus;
		if (acMultiplier < 1)
			acMultiplier = 1;
		
		int acMin = 0;
		int acMax = armourTier * acMultiplier;
		if (armourTier > 1)
			acMin =+ (acMultiplier * armourTier) - acMultiplier;

		int armourClass = MathUtils.RandomBetween(acMin,acMax)  + rarityBonus;
		return armourClass;
	}
}
