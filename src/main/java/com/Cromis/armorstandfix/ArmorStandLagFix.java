
package com.Cromis.armorstandfix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class ArmorStandLagFix extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> lastHitTick = new HashMap<>();
    private BukkitTask cleanupTask;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ArmorStandLagFix enabled.");
    }

    @Override
    public void onDisable() {
        stopCleanupTask();
        lastHitTick.clear();
    }

    private void startCleanupTask() {
        // Only one cleanup task can run at a time.
        if (cleanupTask != null || lastHitTick.isEmpty()) {
            return;
        }

        cleanupTask = Bukkit.getScheduler().runTaskTimer(
            this,
            this::cleanupOldEntries,
            20L,
            20L
        );
    }

    private void stopCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
    }

    private void cleanupOldEntries() {
        int currentTick = Bukkit.getCurrentTick();

        Iterator<Map.Entry<UUID, Integer>> iterator =
            lastHitTick.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();

            if (entry.getValue() != currentTick) {
                iterator.remove();
            }
        }

        // Stop checking when there is nothing left to clean.
        if (lastHitTick.isEmpty()) {
            stopCleanupTask();
        }
    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        int currentTick = Bukkit.getCurrentTick();
        UUID armorStandId = armorStand.getUniqueId();

        Integer previousTick = lastHitTick.put(
            armorStandId,
            currentTick
        );

        if (previousTick == null) {
            startCleanupTask();
        }

        // Cancel additional player hits on this stand during this tick.
        if (previousTick != null && previousTick == currentTick) {
            event.setCancelled(true);
        }
    }
}
