package out.rizzve.companio.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import out.rizzve.companio.Companio;
import out.rizzve.companio.client.command.CompanioCommand;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;

@Mod(value = Companio.MOD_ID, dist = Dist.CLIENT)
public final class CompanioNeoForgeClient {
    public CompanioNeoForgeClient() {
        ConfigManager configManager = new ConfigManager();
        CompanioConfig config = configManager.load();
        CompanionController controller = new CompanionController(config);
        MojangProfileService profileService = new MojangProfileService();

        CompanioCommand.register(controller, profileService, configManager);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> controller.tick(Minecraft.getInstance()));
    }
}
