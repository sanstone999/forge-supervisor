package com.example.supervisor;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod("worldsupervisor")
public class SupervisorMod {

    private static final Logger LOGGER = LogManager.getLogger();

    public SupervisorMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        try {
            File serverDir = new File("").getAbsoluteFile();
            File modsDir = new File(serverDir, "mods");
            File bootstrapScript = new File(modsDir, "world-supervisor_bootstrap.sh");

            if (!bootstrapScript.exists()) {
                LOGGER.warn("[supervisor] bootstrap script not found: {}", bootstrapScript.getAbsolutePath());
                return;
            }

            bootstrapScript.setExecutable(true);

            File pluginDir = new File(modsDir, "ws");
            pluginDir.mkdirs();
            File configPath = new File(pluginDir, "supervisor.yml");

            ProcessBuilder pb = new ProcessBuilder(
                "bash", bootstrapScript.getAbsolutePath()
            );
            pb.environment().put("WS_PROCESS_CWD", serverDir.getAbsolutePath());
            pb.environment().put("WS_PLUGIN_DIR", pluginDir.getAbsolutePath());
            pb.environment().put("WS_CONFIG_PATH", configPath.getAbsolutePath());
            pb.redirectErrorStream(true);
            pb.redirectOutput(new File(pluginDir, "bootstrap.log"));
            pb.start();

            LOGGER.info("[supervisor] bootstrap started, log: {}", new File(pluginDir, "bootstrap.log"));

        } catch (Exception e) {
            LOGGER.error("[supervisor] failed to start bootstrap", e);
        }
    }
}
