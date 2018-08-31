package com.voxelgameslib.voxelgameslib.feature.features;

import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.voxelgameslib.voxelgameslib.event.GameEvent;
import com.voxelgameslib.voxelgameslib.feature.AbstractFeature;
import com.voxelgameslib.voxelgameslib.feature.FeatureInfo;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

@FeatureInfo(name = "JumpPad", author = "MiniDigger", version = "1.0", description = "Lets players use pressure plates to get launched up in the sky")
public class JumpPadFeature extends AbstractFeature {

    private static final Logger log = Logger.getLogger(JumpPadFeature.class.getName());

    @GameEvent
    public void onStep(@Nonnull PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {

            if (!Tag.WOODEN_PRESSURE_PLATES.isTagged(event.getClickedBlock().getType()) &&
                    event.getClickedBlock().getType() != Material.STONE_PRESSURE_PLATE) {
                return;
            }
            if (event.isCancelled()) {
                return;
            }
            double strength = 1.5;
            double up = 1;
            if (event.getClickedBlock().getRelative(BlockFace.DOWN, 2).getState() instanceof Sign) {
                Sign sign = (Sign) event.getClickedBlock().getRelative(BlockFace.DOWN, 2).getState();
                if (sign.getLine(0).contains("[Boom]")) {
                    try {
                        strength = Double.parseDouble(sign.getLine(1));
                        up = Double.parseDouble(sign.getLine(2));
                    } catch (final Exception ex) {
                        log.warning("Invalid boom sign at " + sign.getLocation());
                    }
                }
            }

            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 10.0F, 1.0F);
            event.getPlayer().playEffect(event.getPlayer().getLocation(), Effect.SMOKE, 10);
            Vector v = event.getPlayer().getLocation().getDirection().multiply(strength / 2).setY(up / 2);
            event.getPlayer().setVelocity(v);
            event.setCancelled(true);
        }
    }
}
