
package com.Cromis.armorstandfix;

import java.util.HashMap;
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

public final class ArmorStandLagFix extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> lastHitTick = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ArmorStandLagFix enabled.");
    }

    @Override
    public void onDisable() {
        lastHitTick.clear();
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

        Integer previousTick = lastHitTick.putIfAbsent(
            armorStandId,
            currentTick
        );

        if (previousTick != null && previousTick == currentTick) {
            event.setCancelled(true);
            return;
        }

        lastHitTick.put(armorStandId, currentTick);
    }
}
