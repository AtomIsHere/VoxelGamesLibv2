package com.voxelgameslib.voxelgameslib.api.feature.features;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import com.voxelgameslib.voxelgameslib.api.event.GameEvent;
import com.voxelgameslib.voxelgameslib.api.feature.AbstractFeature;
import com.voxelgameslib.voxelgameslib.api.feature.Feature;
import com.voxelgameslib.voxelgameslib.api.feature.FeatureInfo;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@FeatureInfo(name = "VoidTeleportFeature", author = "aphel", version = "1.0",
        description = "Teleports player to spawn if they fall into the void")
public class VoidTeleportFeature extends AbstractFeature {

    @Override
    @Nonnull
    public List<Class<? extends Feature>> getDependencies() {
        return Collections.singletonList(SpawnFeature.class);
    }

    @GameEvent
    public void onVoidDamage(@Nonnull EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            Player player = (Player) event.getEntity();

            player.teleportAsync(getPhase().getFeature(SpawnFeature.class).getSpawn(player.getUniqueId()));

            event.setCancelled(true);
        }
    }

    @GameEvent
    public void onFellOutOfWorld(@Nonnull PlayerMoveEvent event) {
        // just in case damage is disabled
        if (event.getTo().getY() < 0) {
            event.getPlayer().teleportAsync(getPhase().getFeature(SpawnFeature.class).getSpawn(event.getPlayer().getUniqueId()));
        }
    }
}
