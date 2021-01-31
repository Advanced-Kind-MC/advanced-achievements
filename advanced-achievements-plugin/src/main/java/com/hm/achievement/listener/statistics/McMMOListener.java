package com.hm.achievement.listener.statistics;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Jobs Reborn achievements.
 */
@Singleton
public class McMMOListener extends AbstractListener {

	@Inject
	public McMMOListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(MultipleAchievements.MCMMO, mainConfig, serverVersion, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJob(McMMOPlayerLevelUpEvent event) {
		Player player = event.getPlayer().getPlayer();
		if (player == null) {
			return;
		}

		String jobName = event.getSkill().getName().toLowerCase();
		if (!player.hasPermission(category.toChildPermName(jobName))) {
			return;
		}

		Set<String> foundAchievements = findAchievementsByCategoryAndName(jobName);
		updateStatisticAndAwardAchievementsIfAvailable(player, foundAchievements, event.getLevelsGained());
	}
}
