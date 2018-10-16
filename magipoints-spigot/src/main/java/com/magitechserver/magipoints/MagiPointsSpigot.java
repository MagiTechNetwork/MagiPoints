package com.magitechserver.magipoints;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.magitechserver.magipoints.api.MagiPointsAPI;
import com.magitechserver.magipoints.datastore.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.UUID;
import java.util.logging.Level;

public final class MagiPointsSpigot extends JavaPlugin implements PluginMessageListener {

    public static MagiPointsSpigot instance;
    public MagiPointsPlaceholders placeholders;
    public DataStore dataStore;
    private String randomUuid = UUID.randomUUID().toString();

    @Override
    public void onEnable() {
        instance = this;
        this.dataStore = new DataStore(getLogger());
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "MagiPoints");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "MagiPoints", this);

        this.getCommand("mp").setExecutor((sender, command, label, args) -> {
            String uuid, name;
            if (args.length == 1 && args[0].equals("gui")) {
                GUIManager.handle((Player) sender);
                return true;
            }
            if (sender.hasPermission("magipoints.admin") && args.length >= 1) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
                    name = p.getName();
                    uuid = p.getUniqueId().toString();
                } else {
                    name = player.getName();
                    uuid = player.getUniqueId().toString();
                }
            } else {
                Player p = (Player) sender;
                name = p.getName();
                uuid = p.getUniqueId().toString();
            }

            int result = MagiPointsSpigot.instance.dataStore.getPoints(uuid);
            sender.sendMessage("§a§l Magi§e§lPoints §7> " + (args.length >= 1 && sender.hasPermission("magipoints.admin") ?
                    "§6" + name + "§7 tem §6" + result + "§7 pontos" :
                    "Você tem §6" + result + "§7 pontos"));
            return true;
        });

        this.getCommand("mpa").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("magipoints.admin")) return false;
            String uuid = "";
            boolean all = args[1].equals("@");
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                if (!all) uuid = p.getUniqueId().toString();
            } else {
                uuid = player.getUniqueId().toString();
            }
            String action = args[0];
            switch (action) {
                case "add":
                    if (all) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            dataStore.addPoints(p.getUniqueId().toString(), Integer.valueOf(args[2]));
                        }
                        sender.sendMessage(" §a§lMagi§e§lPoints §7> Pontos adicionados com sucesso!");
                        return true;
                    } else {
                        dataStore.addPoints(uuid, Integer.valueOf(args[2]));
                        sender.sendMessage(" §a§lMagi§e§lPoints §7> Pontos adicionados com sucesso!");
                        return true;
                    }
                case "set":
                    dataStore.setPoints(uuid, Integer.valueOf(args[2]));
                    sender.sendMessage(" §a§lMagi§e§lPoints §7> Pontos adicionados com sucesso!");
                    return true;
                case "take":
                    boolean couldTake = dataStore.takePoints(uuid, Integer.valueOf(args[2]));
                    if (!couldTake) {
                        String msg = ChatColor.translateAlternateColorCodes('&', "&4&l Erro: &c" + player.getName() + " não tem pontos suficientes!");
                        sender.sendMessage(msg);
                    } else {
                        String msg = ChatColor.translateAlternateColorCodes('&', "&a Pontos removidos com sucesso!");
                        sender.sendMessage(msg);
                    }
                    return true;
                case "get":
                    int result = dataStore.getPoints(uuid);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l Magi&e&lPoints &7> &aVocê tem &6" + result + " pontos&a!"));
                    return true;
                default:
                    sender.sendMessage("§4§l Erro: §cComando inválido!");
            }
            return true;
        });

        this.placeholders = new MagiPointsPlaceholders(this);
        this.placeholders.hook();
    }

    @Override
    public void onDisable() {
        this.dataStore.shutdown();
        this.dataStore = null;
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "MagiPoints");
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "MagiPoints", this);
    }

    public static void update(String uuid) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("update," + uuid + "," + instance.randomUuid);
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(instance, "MagiPoints", out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals("MagiPoints")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String[] msg = in.readUTF().split(",");
        if (msg[0].equals("update")) {
            if (msg[2].equals(this.randomUuid)) return;
            getLogger().log(Level.INFO, "Received update request, updating database...");
            this.placeholders.update(msg[1]);
        }
    }

    public static MagiPointsAPI getAPI() {
        return instance.dataStore;
    }
}
