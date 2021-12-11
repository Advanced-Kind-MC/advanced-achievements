package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.iConomy.iConomy;
import com.iConomy.events.AccountSetEvent;
import com.iConomy.events.AccountUpdateEvent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener class to deal with Eggs achievements.
 *
 * @author Pyves
 *
 */
@Singleton
public class BalanceListener extends AbstractListener {

	@Inject
	public BalanceListener(@Named("main") YamlConfiguration mainConfig,
			AchievementMap achievementMap, CacheManager cacheManager) {
		super(NormalAchievements.BALANCE, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEggThrow(AccountUpdateEvent event) {
		if (event.getAccount().isBank())
			return;
		Player player = Bukkit.getPlayer(event.getAccountName());
		if(player == null)
			return;
		increaseStatisticAndAwardAchievementsIfAvailable(player, (int) Math.floor(event.getBalance()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEggThrow(AccountSetEvent event) {
		if (event.getAccount().isBank())
			return;
		Player player = Bukkit.getPlayer(event.getAccountName());
		if(player == null)
			return;
		increaseStatisticAndAwardAchievementsIfAvailable(player, (int) Math.floor(event.getBalance()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEggThrow(PlayerJoinEvent event) {
		increaseStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), (int) Math.floor(iConomy.getAccount(event.getPlayer().getName()).getHoldings().balance()));
	}
}
