package com.hm.achievement.listener.statistics;

import java.util.Collections;
import java.util.HashSet;
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
	public JobsRebornListener(@Named("main") YamlConfiguration mainConfig, int serverVersion,
			AchievementMap achievementMap, CacheManager cacheManager) {
		super(MultipleAchievements.JOBSREBORN, mainConfig, serverVersion, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJobLevelUp(JobsLevelUpEvent event) {
		// Get the Player from the JobsPlayer.
		Player player = event.getPlayer().getPlayer();
		if (player == null) {
			return;
		}
		updateJob(player, event.getJobName(), event.getLevel());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event) {
		if (event.getPlayer() == null) {
			return;
		}

		// Grab the player from the JobsPlayer
		Player player = event.getPlayer().getPlayer();
		if (player == null) {
			return;
		}

		for (JobProgression progression : Jobs.getPlayerManager().getPlayerInfo(event.getPlayer().getUniqueId())
				.getJobsPlayer().getJobProgression()) {
			updateJob(player, progression.getJob().getName(), progression.getLevel());
		}
	}

	private void updateJob(Player player, String jobName, int level) {
		jobName = jobName.toLowerCase();

		if (!player.hasPermission(category.toChildPermName(jobName))) {
			return;
		}

		Set<String> subcategories = new HashSet<>();
		addMatchingSubcategories(subcategories, jobName);
		subcategories.forEach(key -> increaseStatisticAndAwardAchievementsIfAvailable(player, subcategories, level));
	}
}
