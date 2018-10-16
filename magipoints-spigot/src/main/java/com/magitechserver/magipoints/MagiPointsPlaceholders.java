package com.magitechserver.magipoints;

import com.google.common.collect.Maps;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Created by Frani on 02/12/2017.
 */
public class MagiPointsPlaceholders extends EZPlaceholderHook {

    private Map<String, Integer> cache = Maps.newHashMap();

    public MagiPointsPlaceholders(MagiPointsSpigot plugin) {
        super(plugin, "magipoints");
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (identifier.equals("points") && !cache.containsKey(p.getUniqueId().toString())) {
            cache.put(p.getUniqueId().toString(), MagiPointsSpigot.instance.dataStore.getPoints(p.getUniqueId().toString()));
        }
        return String.valueOf(cache.get(p.getUniqueId().toString()));
    }

    public void update(String uuid) {
        if (cache.containsKey(uuid)) {
            cache.remove(uuid);
        }
    }

}
