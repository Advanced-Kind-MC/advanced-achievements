package com.hm.achievement.listener.statistics;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import com.gamingmesh.jobs.container.JobProgression;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener class to deal with Jobs Reborn achievements.
 */
@Singleton
public class JobsRebornListener extends AbstractListener {

	@Inject
	public JobsRebornListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(MultipleAchievements.JOBSREBORN, mainConfig, serverVersion, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJob(JobsLevelUpEvent event) {
		// Get the Player from the JobsPlayer.
		Player player = event.getPlayer().getPlayer();
		if (player == null) {
			return;
		}

		String jobName = event.getJobName().toLowerCase();
		if (!player.hasPermission(category.toChildPermName(jobName))) {
			return;
		}

		Set<String> foundAchievements = findAchievementsByCategoryAndName(jobName);
		increaseStatisticAndAwardAchievementsIfAvailable(player, foundAchievements, event.getLevel());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJob(PlayerJoinEvent event) {
		if (event.getPlayer() == null) {
			return;
		}

		// Grab the player from the JobsPlayer
		Player player = event.getPlayer().getPlayer();
		if (player == null) {
			return;
		}

		for(JobProgression progression : Jobs.getPlayerManager().getPlayerInfo(event.getPlayer().getUniqueId()).getJobsPlayer().progression){
			String jobName = progression.getJob().getName().toLowerCase();

			if (!player.hasPermission(category.toChildPermName(jobName))) {
				return;
			}
			Set<String> foundAchievements = findAchievementsByCategoryAndName(jobName);
			increaseStatisticAndAwardAchievementsIfAvailable(player, foundAchievements, progression.getLevel());
		}
	}
}
