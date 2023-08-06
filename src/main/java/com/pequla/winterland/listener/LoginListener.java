package com.pequla.winterland.listener;

import com.pequla.winterland.WinterlandPlus;
import com.pequla.winterland.model.DataModel;
import com.pequla.winterland.model.RoleModel;
import com.pequla.winterland.service.WebService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class LoginListener implements Listener {

    private final WinterlandPlus plugin;
    private final LuckPerms perms;

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {

            Player player = event.getPlayer();
            Server server = player.getServer();
            if (server.getBannedPlayers().stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()))) {
                return;
            }

            String uuid = player.getUniqueId().toString();
            String guild = plugin.getDb().getConfigByKey("bot.guild");

            DataModel data = WebService.getInstance().getLinkDataForGuild(uuid, guild);
            plugin.getPlayerData().put(player.getUniqueId(), data);
            plugin.getLogger().info("Player " + player.getName() + " authenticated as " + data.getName());


            Guild guildObj = plugin.getJda().getGuildById(guild);
            if (guildObj == null) throw new RuntimeException("Guild doesn't exist");

            Member member = guildObj.retrieveMemberById(data.getId()).complete();
            List<RoleModel> allRoles = plugin.getDb().getAllRoles();
            member.getRoles().forEach(role -> allRoles.forEach(roleModel -> {
                if (!player.hasPermission(roleModel.getGroup()) && role.getId().equals(roleModel.getDiscordId())) {
                    // Roles match, player should get the role
                    addPermission(player, roleModel.getGroup());
                }
            }));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info("Removing cache for " + player.getName());

        // Removing player from cache
        plugin.getPlayerData().remove(player.getUniqueId());
    }

    private void addPermission(Player player, String group) {
        Logger logger = plugin.getLogger();
        UserManager manager = perms.getUserManager();
        CompletableFuture<User> future = manager.loadUser(player.getUniqueId());
        future.thenAcceptAsync(user -> {
            user.data().add(Node.builder(group).build());
            manager.saveUser(user);
        });
        logger.info("Group permission " + group + " was added to " + player.getName());
    }

    private void removePermission(Player player, String group) {
        Logger logger = plugin.getLogger();
        UserManager manager = perms.getUserManager();
        CompletableFuture<User> future = manager.loadUser(player.getUniqueId());
        future.thenAcceptAsync(user -> {
            user.data().remove(Node.builder(group).build());
            manager.saveUser(user);
        });
        logger.info("Group permission " + group + " was removed from " + player.getName());
    }
}
