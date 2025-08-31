package com.jinzo.commands.use;

import com.jinzo.KillTracker;
import com.jinzo.utils.ConfigManager;
import com.jinzo.utils.WeaponUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class showKill implements CommandExecutor {

    private final ConfigManager config = KillTracker.getInstance().getConfiguration();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        final UUID id = player.getUniqueId();
        final long now = System.currentTimeMillis();
        final long cdMillis = config.showCooldown * 1000L;

        Long last = cooldowns.get(id);
        if (last != null && (now - last) < cdMillis) {
            long secs = (cdMillis - (now - last)) / 1000;
            player.sendMessage(Component.text("Please wait " + secs + "s before using this again.", NamedTextColor.RED));
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(Component.text("You must hold an item.", NamedTextColor.RED));
            return false;
        }
        if (config.onlyShowTrackedWeapons && !WeaponUtil.isTrackedWeapon(item)) {
            player.sendMessage(Component.text("You can only show tracked weapons.", NamedTextColor.RED));
            return false;
        }

        cooldowns.put(id, now);

        Component itemName = (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                ? item.displayName()
                : Component.translatable(item.translationKey());
        itemName = itemName.decoration(TextDecoration.ITALIC, false);
        Component shownItem = itemName.hoverEvent(item.asHoverEvent());

        Component senderName = buildStyledName(player);

        Component message = Component.text()
                .append(senderName)
                .append(Component.text(" " + config.showItemText + " ", NamedTextColor.GRAY))
                .append(shownItem)
                .build();

        Bukkit.broadcast(message);
        return true;
    }

    private Component buildStyledName(Player player) {
        Component base = player.displayName()
                .hoverEvent(Component.text(player.getName()))
                .decoration(TextDecoration.ITALIC, false);

        String prefixRaw = getLuckPermsMeta(player, "getPrefix");
        String suffixRaw = getLuckPermsMeta(player, "getSuffix");

        TextColor nameColor = extractColorFromPrefix(prefixRaw);
        if (nameColor == null) nameColor = base.color();
        if (nameColor == null) nameColor = NamedTextColor.GRAY;
        base = base.color(nameColor);

        Component out = Component.empty();
        if (prefixRaw != null && !prefixRaw.isBlank()) {
            out = out.append(deserializeLegacyOrHex(prefixRaw));
        }
        out = out.append(base);
        if (suffixRaw != null && !suffixRaw.isBlank()) {
            out = out.append(deserializeLegacyOrHex(suffixRaw));
        }
        return out;
    }

    private String getLuckPermsMeta(Player player, String which) {
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = provider.getMethod("get").invoke(null);

            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = userManager.getClass().getMethod("getUser", UUID.class)
                    .invoke(userManager, player.getUniqueId());
            if (user == null) return null;

            Object cached = user.getClass().getMethod("getCachedData").invoke(user);
            Object meta = cached.getClass().getMethod("getMetaData").invoke(cached);
            Object val = meta.getClass().getMethod(which).invoke(meta);
            return val != null ? val.toString() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Component deserializeLegacyOrHex(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    private TextColor extractColorFromPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return null;

        TextColor found = null;

        Matcher mHex = Pattern.compile("&#([A-Fa-f0-9]{6})").matcher(prefix);
        while (mHex.find()) {
            found = TextColor.fromHexString("#" + mHex.group(1));
        }

        Matcher mLegacy = Pattern.compile("&([0-9a-fA-F])").matcher(prefix);
        while (mLegacy.find()) {
            switch (Character.toLowerCase(mLegacy.group(1).charAt(0))) {
                case '0': found = NamedTextColor.BLACK; break;
                case '1': found = NamedTextColor.DARK_BLUE; break;
                case '2': found = NamedTextColor.DARK_GREEN; break;
                case '3': found = NamedTextColor.DARK_AQUA; break;
                case '4': found = NamedTextColor.DARK_RED; break;
                case '5': found = NamedTextColor.DARK_PURPLE; break;
                case '6': found = NamedTextColor.GOLD; break;
                case '7': found = NamedTextColor.GRAY; break;
                case '8': found = NamedTextColor.DARK_GRAY; break;
                case '9': found = NamedTextColor.BLUE; break;
                case 'a': found = NamedTextColor.GREEN; break;
                case 'b': found = NamedTextColor.AQUA; break;
                case 'c': found = NamedTextColor.RED; break;
                case 'd': found = NamedTextColor.LIGHT_PURPLE; break;
                case 'e': found = NamedTextColor.YELLOW; break;
                case 'f': found = NamedTextColor.WHITE; break;
                default: break;
            }
        }

        return found;
    }
}
