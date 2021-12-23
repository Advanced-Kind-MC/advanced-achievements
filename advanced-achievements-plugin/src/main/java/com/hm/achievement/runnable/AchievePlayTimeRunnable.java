package com.hm.achievement.runnable;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import solutions.nuhvel.spigot.advancedafk.AdvancedAFK;
import solutions.nuhvel.spigot.advancedafk.utils.AFKHandler;

/**
 * Class used to monitor players' played times.
 * 
 * @author Pyves
 *
 */
@Singleton
public class AchievePlayTimeRunnable extends StatisticIncreaseHandler implements Runnable {

	private static final long MILLIS_PER_HOUR = TimeUnit.HOURS.toMillis(1);

	private Essentials essentials;
	private AdvancedAFK advancedAFK;
	private long previousTimeMillis;

	private boolean configIgnoreAFKPlayedTimeEssentials;
	private boolean configIgnoreAFKPlayedTimeAdvancedAFK;

	@Inject
	public AchievePlayTimeRunnable(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(mainConfig, achievementMap, cacheManager);

		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		}
		if (Bukkit.getPluginManager().isPluginEnabled("AdvancedAFK")) {
			advancedAFK = (AdvancedAFK) Bukkit.getPluginManager().getPlugin("AdvancedAFK");
		}

		previousTimeMillis = System.currentTimeMillis();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configIgnoreAFKPlayedTimeEssentials = essentials != null && mainConfig.getBoolean("IgnoreAFKPlayedTime");
		configIgnoreAFKPlayedTimeAdvancedAFK = advancedAFK != null && mainConfig.getBoolean("IgnoreAFKPlayedTime");
	}

	@Override
	public void run() {
		long currentTimeMillis = System.currentTimeMillis();
		int millisSincePreviousRun = (int) (currentTimeMillis - previousTimeMillis);
		Bukkit.getOnlinePlayers().forEach(p -> updateTime(p, millisSincePreviousRun));
		previousTimeMillis = currentTimeMillis;
	}

	/**
	 * Updates play time if all conditions are met and awards achievements if necessary.
	 * 
	 * @param player
	 * @param millisSincePreviousRun
	 */
	private void updateTime(Player player, int millisSincePreviousRun) {
		if (!shouldIncreaseBeTakenIntoAccount(player, NormalAchievements.PLAYEDTIME)) {
			return;
		}

		// If player is AFK, don't update played time.
		if (configIgnoreAFKPlayedTimeEssentials && essentials.getUser(player).isAfk()) {
			return;
		}
		// AdvancedAFK variant
		if (configIgnoreAFKPlayedTimeAdvancedAFK && AFKHandler.getAFKPlayer(advancedAFK, player).isAFK()) {
			return;
		}

		long totalMillis = cacheManager.getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME, player.getUniqueId(),
				millisSincePreviousRun);
		// Thresholds in the configuration are in hours.
		checkThresholdsAndAchievements(player, NormalAchievements.PLAYEDTIME, totalMillis / MILLIS_PER_HOUR);
	}
}
