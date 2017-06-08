package me.minidigger.voxelgameslib.world;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;
import me.minidigger.voxelgameslib.game.GameHandler;
import me.minidigger.voxelgameslib.game.GameMode;
import me.minidigger.voxelgameslib.lang.Lang;
import me.minidigger.voxelgameslib.lang.LangKey;
import me.minidigger.voxelgameslib.map.Map;
import me.minidigger.voxelgameslib.map.MapInfo;
import me.minidigger.voxelgameslib.map.MapScanner;
import me.minidigger.voxelgameslib.map.Vector3D;
import me.minidigger.voxelgameslib.user.User;
import me.minidigger.voxelgameslib.utils.ZipUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;

/**
 * Handles creation of new worlds/maps
 */
@Log
@Singleton
@SuppressWarnings("JavaDoc") // commands don't need javadoc, go read the command's descriptions
public class WorldCreator extends BaseCommand {

  //TODO world creator completer

  @Inject
  private WorldHandler worldHandler;

  @Inject
  private GameHandler gameHandler;

  @Inject
  private MapScanner mapScanner;

  @Inject
  private WorldConfig config;

  @Inject
  private Gson gson;

  private User editor;

  private int step = 0;

  private String worldName;
  private Vector3D center;
  private int radius;
  private String displayName;
  private String author;
  private List<String> gameModes;

  private Map map;

  @CommandAlias("worldcreator|wc")
  @CommandPermission("%admin")
  public void worldcreator(User sender) {
    Lang.msg(sender, LangKey.WORLD_CREATOR_INFO);
  }

  @Subcommand("start")
  @CommandPermission("%admin")
  public void start(User sender) {
    if (editor != null) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_IN_USE,
          editor.getDisplayName());
      return;
    }

    editor = sender;
    gameModes = new ArrayList<>();

    sender.sendMessage(Lang.trans(LangKey.WORLD_CREATOR_ENTER_WORLD_NAME,
        sender.getLocale())
        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/worldcreator world ")).create());

    step = 1;
  }

  @Subcommand("world")
  @CommandPermission("%admin")
  public void world(User sender, String worldName) {
    if (step != 1) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_WRONG_STEP, step, 1);
      return;
    }

    this.worldName = worldName;

    worldHandler.loadLocalWorld(worldName);
    sender.getPlayer().teleport(Bukkit.getWorld(worldName).getSpawnLocation());

    sender.sendMessage(
        Lang.trans(LangKey.WORLD_CREATOR_ENTER_CENTER, sender.getLocale())
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/worldcreator center")).create());

    step = 2;
  }

  @Subcommand("center")
  @CommandPermission("%admin")
  public void center(User sender) {
    if (step != 2) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_WRONG_STEP, step, 2);
      return;
    }

    center = sender.getPlayer().getLocation();

    sender.sendMessage(
        Lang.trans(LangKey.WORLD_CREATOR_ENTER_RADIUS, sender.getLocale())
            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/worldcreator radius "))
            .create());

    step = 3;
  }

  @Subcommand("radius")
  @CommandPermission("%admin")
  public void radius(User sender, int radius) {
    if (step != 3) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_WRONG_STEP, step, 3);
      return;
    }

    this.radius = radius;

    sender.sendMessage(Lang.trans(LangKey.WORLD_CREATOR_ENTER_DISPLAY_NAME,
        sender.getLocale())
        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/worldcreator name ")).create());

    step = 4;
  }

  @Subcommand("name")
  @CommandPermission("%admin")
  public void name(User sender, String name) {
    if (step != 4) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_WRONG_STEP, step, 4);
      return;
    }

    displayName = name;

    sender.sendMessage(
        Lang.trans(LangKey.WORLD_CREATOR_ENTER_AUTHOR, sender.getLocale(),
            displayName)
            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/worldcreator author "))
            .create());

    step = 5;
  }

  @Subcommand("author")
  @CommandPermission("%admin")
  public void author(User sender, String author) {
    if (step != 5) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_WRONG_STEP, step, 5);
      return;
    }

    this.author = author;

    Lang.msg(sender, LangKey.WORLD_CREATOR_AUTHOR_SET, author);
    for (GameMode mode : gameHandler.getGameModes()) {
      sender
          .sendMessage(new ComponentBuilder(mode.getName() + " ").color(ChatColor.YELLOW)
              .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                  "/worldcreator gamemode " + mode.getName())).create());
    }

    sender.sendMessage(
        Lang.trans(LangKey.WORLD_CREATOR_DONE_QUERY, sender.getLocale())
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/worldcreator gamemode done"))
            .create());

    step = 6;
  }

  @Subcommand("gamemode")
  @CommandPermission("%admin")
  public void gamemode(User sender, String gamemode) {
    if (step != 6) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_WRONG_STEP, step, 6);
      return;
    }

    if (gamemode.equalsIgnoreCase("done")) {
      sender.sendMessage(Lang.trans(LangKey.WORLD_CREATOR_EDIT_MODE_ON,
          sender.getLocale())
          .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/worldcreator edit on")).create());
      sender.sendMessage(Lang.trans(LangKey.WORLD_CREATOR_EDIT_MODE_OFF,
          sender.getLocale())
          .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/worldcreator edit off")).create());
      step = 7;
    } else {
      gameModes.add(gamemode);
      Lang.msg(sender, LangKey.WORLD_CREATOR_GAMEMODE_ADDED);
    }
  }

  @Subcommand("edit")
  @CommandPermission("%admin")
  //TODO might want to replace onOff with boolean in the future
  public void edit(User sender, String onOff) {
    if (step != 7) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_WRONG_STEP, step, 7);
      return;
    }

    if (onOff.equalsIgnoreCase("on")) {
      sender.getPlayer().performCommand("editmode on");
    } else if (onOff.equalsIgnoreCase("off")) {
      sender.getPlayer().performCommand("editmode off");
      MapInfo info = new MapInfo(displayName, author, gameModes);
      map = new Map(info, worldName, center, radius);
      map.printSummery(sender);
      sender.sendMessage(
          Lang.trans(LangKey.WORLD_CREATOR_DONE_QUERY, sender.getLocale())
              .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/worldcreator done")).create());
      step = 8;
    } else {
      Lang.msg(sender, LangKey.GENERAL_INVALID_ARGUMENT, onOff);
    }
  }

  @Subcommand("done")
  @CommandPermission("%admin")
  public void done(User sender) {
    if (step != 8) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_WRONG_STEP, step, 8);
      return;
    }

    mapScanner.scan(map);

    File worldFolder = new File(worldHandler.getWorldContainer(), map.getWorldName());

    try {
      FileWriter fileWriter = new FileWriter(new File(worldFolder, "config.json"));
      gson.toJson(map, fileWriter);
      fileWriter.close();
    } catch (IOException e) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_SAVE_CONFIG_ERROR, e.getMessage(),
          e.getClass().getName());
      log.log(Level.WARNING, "Error while saving the world config", e);
      return;
    }

    ZipFile zip;
    try {
      zip = ZipUtil.createZip(worldFolder);
    } catch (ZipException e) {
      Lang.msg(sender, LangKey.WORLD_CREATOR_SAVE_ZIP_ERROR, e.getMessage(),
          e.getClass().getName());
      log.log(Level.WARNING, "Error while creating the zip", e);
      return;
    }

    try {
      Files.move(zip.getFile(), new File(worldHandler.getWorldsFolder(), zip.getFile().getName()));
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (!config.maps.contains(map.getInfo())) {
      config.maps.add(map.getInfo());
      worldHandler.saveConfig();
    }

    // TODO debug
//        editor = null;
//        step = 0;
//        worldName = null;
//        center = null;
//        radius = -1;
//        displayName = null;
//        author = null;
//        gameModes = new ArrayList<>();

    Lang.msg(sender, LangKey.WORLD_CREATOR_DONE);
  }
}
