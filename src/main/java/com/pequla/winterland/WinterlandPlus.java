package com.pequla.winterland;

import com.pequla.winterland.listener.LoginListener;
import com.pequla.winterland.model.DataModel;
import com.pequla.winterland.service.DatabaseService;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public final class WinterlandPlus extends JavaPlugin {

    private final Map<UUID, DataModel> playerData = new HashMap<>();
    private DatabaseService db;
    private JDA jda;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        PluginManager pm = getServer().getPluginManager();
        ServicesManager sm = getServer().getServicesManager();

        try {
            // Connect to MySQL
            getLogger().info("Connecting to database");
            this.db = new DatabaseService(
                    getConfig().getString("mysql.host"),
                    getConfig().getString("mysql.database"),
                    getConfig().getString("mysql.user"),
                    getConfig().getString("mysql.password")
            );

            getLogger().info("Connecting to Discord API");
            this.jda = JDABuilder.createDefault(db.getConfigByKey("bot.token"))
                    .setActivity(Activity.playing(db.getConfigByKey("bot.playing")))
                    .setStatus(OnlineStatus.IDLE)
                    .build();

            // Retrieving LuckPerms instance
            RegisteredServiceProvider<LuckPerms> provider = sm.getRegistration(LuckPerms.class);
            if (pm.getPlugin("LuckPerms") == null || provider == null) {
                getLogger().severe("LuckPerms not found");
                pm.disablePlugin(this);
                return;
            }

            // Registering listener
            getLogger().info("Registering event listeners");
            pm.registerEvents(new LoginListener(this, provider.getProvider()), this);

        } catch (SQLException | ClassNotFoundException e) {
            getLogger().severe("Couldn't connect to database");
            e.printStackTrace();
            pm.disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (db != null) {
            try {
                getLogger().info("Disconnecting from database");
                db.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (jda !=null) {

        }
    }
}
