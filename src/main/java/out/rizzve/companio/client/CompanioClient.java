package out.rizzve.companio.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import out.rizzve.companio.client.command.CompanioCommand;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.skin.MojangProfileService;

public class CompanioClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager configManager = new ConfigManager();
        CompanioConfig config = configManager.load();
        CompanionController controller = new CompanionController(config);
        MojangProfileService profileService = new MojangProfileService();

        CompanioCommand.register(controller, profileService, configManager);
        ClientTickEvents.END_CLIENT_TICK.register(controller::tick);
    }
}
