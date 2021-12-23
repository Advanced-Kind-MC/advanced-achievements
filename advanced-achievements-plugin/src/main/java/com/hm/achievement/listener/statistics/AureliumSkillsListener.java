package com.hm.achievement.listener.statistics;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.api.event.SkillLevelUpEvent;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.skills.Skill;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Listener class to deal with Jobs Reborn achievements.
 */
@Singleton
public class AureliumSkillsListener extends AbstractListener {

	@Inject
	public AureliumSkillsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
                                  CacheManager cacheManager) {
		super(MultipleAchievements.AureliumSkills, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onAureliumSkillLevelUp(SkillLevelUpEvent event) {
		Player player = event.getPlayer().getPlayer();
		if (player == null) {
			return;
		}

		String skillName = event.getSkill().getDisplayName(Locale.ENGLISH).toLowerCase();
		if (!player.hasPermission(category.toChildPermName(skillName))) {
			return;
		}

		Set<String> subcategories = new HashSet<>();

		addMatchingSubcategories(subcategories, skillName);
		increaseStatisticAndAwardAchievementsIfAvailable(player, subcategories, event.getLevel());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerDataLoadEvent event) {
		Player player = event.getPlayerData().getPlayer();
		if (player == null) {
			return;
		}

		if (event.isAsynchronous()) {
			Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(AdvancedAchievements.class), () -> {
				for (Skill skill : AureliumAPI.getPlugin().getSkillRegistry().getSkills()) {
					String skillName = skill.getDisplayName(Locale.ENGLISH).toLowerCase();
					if (!player.hasPermission(category.toChildPermName(skillName))) {
						continue;
					}

					Set<String> subcategories = new HashSet<>();

					addMatchingSubcategories(subcategories, skillName);
					increaseStatisticAndAwardAchievementsIfAvailable(player, subcategories,
							event.getPlayerData().getSkillLevel(skill));
				}
			});
		} else {
			for (Skill skill : AureliumAPI.getPlugin().getSkillRegistry().getSkills()) {
				String skillName = skill.getDisplayName(Locale.ENGLISH).toLowerCase();
				if (!player.hasPermission(category.toChildPermName(skillName))) {
					continue;
				}

				Set<String> subcategories = new HashSet<>();

				addMatchingSubcategories(subcategories, skillName);
				increaseStatisticAndAwardAchievementsIfAvailable(player, subcategories,
						event.getPlayerData().getSkillLevel(skill));
			}
		}
	}
}
