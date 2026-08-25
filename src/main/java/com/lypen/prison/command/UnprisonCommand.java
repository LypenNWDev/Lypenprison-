package com.lypen.prison.command;

import com.lypen.prison.LypenPrison;
import com.lypen.prison.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnprisonCommand implements CommandExecutor {

    private final LypenPrison plugin;

    public UnprisonCommand(LypenPrison plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lypenprison.admin")) {
            sender.sendMessage(ColorUtils.format("&cYetkiniz yok."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtils.format("&cKullanım: /unprison <oyuncu>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtils.format("&cOyuncu bulunamadı veya çevrimdışı."));
            return true;
        }

        if (!plugin.getPrisonManager().isPlayerInPrison(target.getUniqueId())) {
            sender.sendMessage(ColorUtils.format("&cBu oyuncu zaten hapiste değil."));
            return true;
        }

        plugin.getPrisonManager().removeFromServer(target.getUniqueId(), false, "");
        sender.sendMessage(ColorUtils.format("&a" + target.getName() + " hapisten çıkarıldı."));
        target.sendMessage(ColorUtils.format("&aHapisten çıkarıldınız."));

        return true;
    }
}
