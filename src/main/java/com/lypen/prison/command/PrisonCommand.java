package com.lypen.prison.command;

import com.lypen.prison.LypenPrison;
import com.lypen.prison.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrisonCommand implements CommandExecutor {

    private final LypenPrison plugin;

    public PrisonCommand(LypenPrison plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lypenprison.admin")) {
            sender.sendMessage(ColorUtils.format("&cYetkiniz yok."));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            String result = plugin.reloadAll();
            sender.sendMessage(ColorUtils.format(result));
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("info")) {
            String targetName = args[1];
            int page = 1;
            if (args.length == 3) {
                try {
                    page = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {}
            }
            java.util.List<String> logs = plugin.getLogManager().getLogs(targetName);
            if (logs == null || logs.isEmpty()) {
                sender.sendMessage(ColorUtils.format("&cBu oyuncunun hapishane kaydı bulunmuyor."));
                return true;
            }
            int totalPages = (int) Math.ceil(logs.size() / 3.0);
            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;
            
            sender.sendMessage(ColorUtils.format("&6--- " + targetName + " Hapishane Geçmişi (Sayfa " + page + "/" + totalPages + ") ---"));
            int start = (page - 1) * 3;
            int end = Math.min(start + 3, logs.size());
            for (int i = start; i < end; i++) {
                sender.sendMessage(ColorUtils.format("&7" + (i + 1) + ". " + logs.get(i)));
            }
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.format("&cKullanım: /prison <tür> <oyuncu> <süre> [mesaj] | /prison info <oyuncu> [sayfa] | /prison reload"));
            return true;
        }

        String type = args[0];
        
        if (!plugin.getConfig().contains("prison_types." + type)) {
            sender.sendMessage(ColorUtils.format("&cGeçersiz prison türü: " + type));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.format("&cOyuncu bulunamadı."));
            return true;
        }

        int duration = 0;
        String durationStr = args[2].toLowerCase();
        try {
            if (durationStr.endsWith("s")) {
                duration = Integer.parseInt(durationStr.substring(0, durationStr.length() - 1));
            } else if (durationStr.endsWith("m")) {
                duration = Integer.parseInt(durationStr.substring(0, durationStr.length() - 1)) * 60;
            } else if (durationStr.endsWith("h")) {
                duration = Integer.parseInt(durationStr.substring(0, durationStr.length() - 1)) * 3600;
            } else if (durationStr.endsWith("d")) {
                duration = Integer.parseInt(durationStr.substring(0, durationStr.length() - 1)) * 86400;
            } else {
                duration = Integer.parseInt(durationStr);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.format("&cGeçersiz süre formatı! Örnek: 60, 60s, 30m, 2h"));
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        if (args.length > 3) {
            for (int i = 3; i < args.length; i++) {
                messageBuilder.append(args[i]).append(" ");
            }
        }
        String adminMessage = messageBuilder.toString().trim();
        if (adminMessage.isEmpty()) {
            adminMessage = plugin.getConfig().getString("no_admin_message", "&cSebep belirtilmedi.");
        }

        Player admin = sender instanceof Player ? (Player) sender : null;

        // Send admin message to chat
        Bukkit.broadcastMessage(ColorUtils.format("&eYetkili Mesajı: &f" + adminMessage));

        plugin.getPrisonManager().putInPrison(target, admin, type, duration);
        sender.sendMessage(ColorUtils.format("&a" + target.getName() + " başarıyla " + type + " hücresine alındı."));

        return true;
    }
}
