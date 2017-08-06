package com.voxelgameslib.voxelgameslib.command.commands;

import com.voxelgameslib.voxelgameslib.lang.Lang;
import com.voxelgameslib.voxelgameslib.lang.LangKey;
import com.voxelgameslib.voxelgameslib.texture.TextureHandler;
import com.voxelgameslib.voxelgameslib.user.User;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.inventory.ItemStack;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;

@Singleton
@SuppressWarnings("JavaDoc") // commands don't need javadoc, go read the command's descriptions
@CommandAlias("texture")
public class TextureCommands extends BaseCommand {

    @Inject
    private TextureHandler textureHandler;

    @Subcommand("get")
    public void getById(@Nonnull User user, @Nonnull Integer id) {
        Lang.msg(user, LangKey.TEXTURE_FETCHING_TEXTURE);
        textureHandler.fetchSkin(id, skin -> {
            ItemStack skull = textureHandler.getSkull(skin);
            user.getPlayer().getInventory().addItem(skull);
            Lang.msg(user, LangKey.TEXTURE_TEXTURE_APPLIED);
        });
    }
}