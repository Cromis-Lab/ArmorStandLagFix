package com.Cromis.armorstandfix;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class ArmorStandLagFix extends JavaPlugin implements Listener {

    private static final String LAST_TICK_KEY = "as-last-hit-tick";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ArmorStandLagFix by Cromis (Per-Tick Mode) enabled!");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand) || !(event.getDamager() instanceof Player)) {
            return;
        }

        ArmorStand stand = (ArmorStand) event.getEntity();
        long currentTick = Bukkit.getCurrentTick();

        if (stand.hasMetadata(LAST_TICK_KEY)) {
            long lastHitTick = stand.getMetadata(LAST_TICK_KEY).get(0).asLong();
            if (lastHitTick == currentTick) {
                event.setCancelled(true);
                return;
            }
        }

        stand.setMetadata(LAST_TICK_KEY, new FixedMetadataValue(this, currentTick));
    }
}
