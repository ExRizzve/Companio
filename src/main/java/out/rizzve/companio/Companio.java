package out.rizzve.companio;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Companio implements ModInitializer {
    public static final String MOD_ID = "companio";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Companio initialized");
    }
}
