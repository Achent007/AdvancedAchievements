package com.hm.achievement.listener;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with the GUIs from the /aach list command.
 * 
 * @author Pyves
 *
 */
public class ListGUIListener extends AbstractListener {

	private String langListGUITitle;

	public ListGUIListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langListGUITitle = ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List"));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getInventory().getName().startsWith(langListGUITitle) || event.getRawSlot() < 0) {
			return;
		}

		// Prevent players from taking items out of the GUI.
		event.setCancelled(true);

		if (haveSameGUIPurpose(event.getCurrentItem(), plugin.getListCommand().getBackButton())) {
			// Clicked item seems to be the back button. But player could have clicked on item in his personal inventory
			// that matches the properties of the back button used by Advanced Achievements. The first item matching the
			// properties of the back button is the real one, check that this is indeed the clicked one.
			Map<Integer, ItemStack> backButtonCandidates = new TreeMap<>(
					event.getInventory().all(event.getCurrentItem().getType()));
			for (Entry<Integer, ItemStack> entry : backButtonCandidates.entrySet()) {
				if (entry.getValue().getDurability() == event.getCurrentItem().getDurability()) {
					// Found real back button. Did the player click on it?
					if (entry.getKey() == event.getRawSlot()) {
						plugin.getListCommand().executeCommand(event.getWhoClicked(), null, "list");
					}
					return;
				}
			}
		}

		ItemStack firstItem = event.getInventory().getItem(0);
		if (haveSameGUIPurpose(firstItem, plugin.getListCommand().getAchievementNotStarted())
				|| haveSameGUIPurpose(firstItem, plugin.getListCommand().getAchievementStarted())
				|| haveSameGUIPurpose(firstItem, plugin.getListCommand().getAchievementReceived())) {
			// GUI corresponding to the achievement listing of a given category. Do not let the player interact with it.
			return;
		}

		// Main GUI displaying all the categories. Do not let players interact with locked categories or slots not
		// corresponding to a category item.
		if (event.getCurrentItem().getType() == Material.BARRIER
				|| event.getRawSlot() > NormalAchievements.values().length + MultipleAchievements.values().length
						- plugin.getDisabledCategorySet().size()) {
			return;
		}

		plugin.getListCommand().createCategoryGUI(event.getCurrentItem(), (Player) event.getWhoClicked());
	}

	/**
	 * Determines whether two ItemStacks in the GUI have the same purpose, in other words whether they have been
	 * generated from the same element in gui.yml.
	 * 
	 * @param clicked
	 * @param reference
	 * @return
	 */
	private boolean haveSameGUIPurpose(ItemStack clicked, ItemStack reference) {
		return clicked.getDurability() == reference.getDurability() && clicked.getType() == reference.getType();
	}
}
