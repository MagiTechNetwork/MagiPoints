package com.magitechserver.magipoints;

import me.lyras.api.gui.action.Action;
import me.lyras.api.gui.action.BlockAction;
import me.lyras.api.gui.action.CloseAction;
import me.lyras.api.gui.link.Link;
import me.lyras.api.gui.permission.Permission;
import me.lyras.api.gui.permission.PermissionedPlayer;
import me.lyras.api.gui.ui.Listing;
import me.lyras.api.gui.ui.ListingManager;
import me.lyras.api.gui.utilities.EventStatus;
import me.lyras.api.gui.utilities.HandleStatus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by Frani on 11/12/2017.
 */
public class GUIManager {

    public static Listing listing;

    public static void handle(Player player) {
        Listing listing = new Listing(36, "MagiPoints > GUI") {
            @Override
            public void load() {
                GUIManager.listing = this;
                GUIManager.setItem(GUIManager.getItem(Material.STAINED_GLASS_PANE, 15, " ", null),
                        new BlockAction(),
                        false,
                        0, 1, 2, 3, 4, 5, 6, 7, 8,
                        9, 17, 18, 26, 27, 28, 29, 30, 32, 33, 34);

                GUIManager.setItem(GUIManager.getItem(Material.COMPASS,
                        14,
                        "&6Seus Pontos",
                        "&7Você tem &b" + MagiPointsSpigot.getAPI().getPoints(player.getUniqueId().toString()) + " &7pontos"),
                        new BlockAction(),
                        false, 31);

                GUIManager.setItem(GUIManager.getItem(Material.WOOL,
                        14,
                        "&cFechar",
                        "&cClique para fechar essa GUI"),
                        new CloseAction(),
                        false, 35);

                // 20
                GUIManager.setBuyableItem(10, Material.DOUBLE_PLANT,
                        "&eLimpador de Tempo",
                        "Limpa o tempo (chuva) do servidor",
                        500,
                        player,
                        "toggledownfall");

                GUIManager.setBuyableItem(11, Material.EMERALD,
                        "&eSacola de Moedas",
                        "Troca pontos por $1000 de money in-game",
                        1000,
                        player,
                        "eco give " + player.getName() + " 1000");

                GUIManager.setBuyableItem(12, Material.IRON_BLOCK,
                        "&eChunk Loader (comum)",
                        "Troca pontos por 2x Chunks para o Chunk Loader do bloco de ferro",
                        8000,
                        player,
                        "bcl chunks add " + player.getName() + " onlineonly 2 force");

                GUIManager.setBuyableItem(13, Material.DIAMOND_BLOCK,
                        "&eChunk Loader (24h)",
                        "Troca pontos por 2x Chunks para o Chunk Loader 24h",
                        15000,
                        player,
                        "bcl chunks add " + player.getName() + " alwayson 2 force");

                GUIManager.setBuyableItem(14, Material.WATCH,
                        "&eSuper Relógio (noite)",
                        "Muda o horário do mundo em que você está (para meia noite)",
                        5000,
                        player,
                        "time set 16000");

                GUIManager.setBuyableItem(15, Material.WATCH,
                        "&eSuper Relógio (dia)",
                        "Muda o horário do mundo em que você está (para meio dia)",
                        5000,
                        player,
                        "time set 6000");

                GUIManager.setBuyableItem(16, Material.NAME_TAG,
                        "&eCupom de 15% de Desconto",
                        "Troca pontos por 15% de desconto na Loja",
                        10000,
                        player,
                        "msg " + player.getName() + " Parabéns " + player.getName() + ", você acaba de ganhar 15% de desconto na Loja! Envie uma mensagem privada para Eufranio no nosso Discord com uma screenshot desta mensagem para conseguir seu cupom!");

                GUIManager.setBuyableItem(19, Material.FEATHER,
                        "&eVôo Temporário",
                        "Te dá 30 minutos de acesso ao /fly",
                        2000,
                        player,
                        "lp user " + player.getName() + " perm settemp essentials.fly true 30m");

            }
        };
        PermissionedPlayer pPlayer = new PermissionedPlayer(player, Permission.CLICK, Permission.CLOSE, Permission.OPEN);
        ListingManager.bind(listing, pPlayer);
        listing.getOptions().setClosing(true);
        listing.open();
    }

    private static ItemStack getItem(Material material, int meta, String displayName, String... lore) {
        ItemStack stack = new ItemStack(material, 1, (byte) meta);
        ItemMeta itemMeta = stack.getItemMeta();
        if (displayName != null) itemMeta.setDisplayName(displayName.replaceAll("&", "§"));
        if (lore != null) itemMeta.setLore(Arrays.stream(lore).map(s -> s.replace("&", "§")).collect(Collectors.toList()));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    private static void setItem(ItemStack item, Action action, boolean customAction, int... slots) {
        for (int slot : slots) {
            Action a;
            if (customAction) {
                a = action;
            } else {
                try {
                    a = action.getClass().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            listing.set(slot, new Link(item, a));
        }
    }

    private static void setBuyableItem(int slot, Material material, String name, String description, int price, Player player, String... commands) {
        GUIManager.setItem(GUIManager.getItem(material, 0, name,
                        " ",
                        " &a" + description,
                        " &7Preço: &b" + price + " Pontos &7(você tem &b" + MagiPointsSpigot.getAPI().getPoints(player.getUniqueId().toString()) + " pontos&7)",
                        " "),
                new Action() {
                    @Override
                    public void execute() {
                        this.setEventStatus(EventStatus.CANCEL);
                        this.setHandleStatus(HandleStatus.HANDLE);
                        boolean success = MagiPointsSpigot.getAPI().takePoints(player.getUniqueId().toString(), price);
                        if (success) {
                            for (String command : commands) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                            player.closeInventory();
                            player.sendMessage("§a Você usou o(a) " + name.replace("&", "§") + "§a com sucesso!");
                        } else {
                            player.closeInventory();
                            player.sendMessage("§4§l Erro: §cVocê não tem pontos suficientes para comprar isso!");
                        }
                    }
                },
        true, slot);
    }

}
