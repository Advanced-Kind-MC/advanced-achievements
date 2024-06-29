package com.hm.achievement.runnable;

import com.earth2me.essentials.Essentials;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.advancedafk.AdvancedAFK;
import solutions.nuhvel.spigot.advancedafk.utils.AFKHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Class used to monitor players' played times.
 *
 * @author Pyves
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
    public AchievePlayTimeRunnable(@Named("main") final YamlConfiguration mainConfig, final AchievementMap achievementMap,
                                   final CacheManager cacheManager) {
        super(mainConfig, achievementMap, cacheManager);

        if (Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		if (Bukkit.getPluginManager().isPluginEnabled("AdvancedAFK")) {
			advancedAFK = (AdvancedAFK) Bukkit.getPluginManager().getPlugin("AdvancedAFK");
		}

        this.previousTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

		configIgnoreAFKPlayedTimeEssentials = (essentials != null || Bukkit.getPluginManager().isPluginEnabled("CMI")) && mainConfig.getBoolean("IgnoreAFKPlayedTime");
		configIgnoreAFKPlayedTimeAdvancedAFK = advancedAFK != null && mainConfig.getBoolean("IgnoreAFKPlayedTime");
    }

    @Override
    public void run() {
        final long currentTimeMillis = System.currentTimeMillis();
        final int millisSincePreviousRun = (int) (currentTimeMillis - this.previousTimeMillis);
        Bukkit.getOnlinePlayers().forEach(p -> this.updateTime(p, millisSincePreviousRun));
        this.previousTimeMillis = currentTimeMillis;
    }

    /**
     * Updates play time if all conditions are met and awards achievements if necessary.
     *
     * @param player
     * @param millisSincePreviousRun
     */
    private void updateTime(final Player player, final int millisSincePreviousRun) {
        if (!this.shouldIncreaseBeTakenIntoAccount(player, NormalAchievements.PLAYEDTIME))
            return;

		// If player is AFK, don't update played time.
		if (this.configIgnoreAFKPlayedTimeEssentials && this.essentials.getUser(player).isAfk()) {
			return;
		}
		// AdvancedAFK variant
		if (this.configIgnoreAFKPlayedTimeAdvancedAFK && AFKHandler.getAFKPlayer(this.advancedAFK, player).isAFK()) {
			return;
		}

        final long totalMillis = this.cacheManager.getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME, player.getUniqueId(),
                millisSincePreviousRun);
        // Thresholds in the configuration are in hours.
        this.checkThresholdsAndAchievements(player, NormalAchievements.PLAYEDTIME, totalMillis / MILLIS_PER_HOUR);
    }
}
