package out.rizzve.companio.client.gui.cloth;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.companion.CompanionHat;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.CompanionSlot;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.config.HatPlacement;
import out.rizzve.companio.client.gui.ScreenCompat;
import out.rizzve.companio.client.skin.MojangProfileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleFunction;

public final class ClothConfigScreenFactory {
    private ClothConfigScreenFactory() {
    }

    public static Screen create(
            Screen parent,
            CompanionController controller,
            ConfigManager configManager,
            MojangProfileService profileService
    ) {
        AtomicReference<CompanioConfig> state = new AtomicReference<>(configManager.load());

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("companio.screen.title"))
                .setSavingRunnable(() -> {
                    CompanioConfig saved = state.get();
                    configManager.save(saved);
                    controller.sync(saved, Minecraft.getInstance(), profileService, configManager);
                });

        ConfigEntryBuilder entries = builder.entryBuilder();
        addFlight(builder, entries, state);
        addHatPlacement(builder, entries, state);
        return builder.build();
    }


    private static void addFlight(
            ConfigBuilder builder,
            ConfigEntryBuilder entries,
            AtomicReference<CompanioConfig> state
    ) {
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("companio.menu.flight"));
        CompanioConfig config = state.get();

        category.addEntry(scaled(entries, "companio.option.distance",
                config.wanderRadius(), CompanioConfig.DEFAULT.wanderRadius(), 1.0, 12.0, 10, 1,
                value -> state.updateAndGet(current -> current.withWanderRadius(value))));
        category.addEntry(scaled(entries, "companio.option.height",
                config.hoverHeight(), CompanioConfig.DEFAULT.hoverHeight(), 1.5, 6.0, 10, 1,
                value -> state.updateAndGet(current -> current.withHoverHeight(value))));
        category.addEntry(scaled(entries, "companio.option.speed",
                config.maxSpeed(), CompanioConfig.DEFAULT.maxSpeed(), 0.04, 0.2, 100, 2,
                value -> state.updateAndGet(current -> current.withMaxSpeed(value))));
        category.addEntry(scaled(entries, "companio.option.follow_speed",
                config.followSpeed(), CompanioConfig.DEFAULT.followSpeed(), 0.25, 0.8, 100, 2,
                value -> state.updateAndGet(current -> current.withFollowSpeed(value))));

        category.addEntry(entries.startIntSlider(
                        Component.translatable("companio.option.smoothness"), config.smoothnessLevel(), 1, 10)
                .setDefaultValue(CompanioConfig.DEFAULT.smoothnessLevel())
                .setSaveConsumer(value -> state.updateAndGet(current -> current.withSmoothness(value)))
                .build());
        category.addEntry(entries.startIntSlider(
                        Component.translatable("companio.option.sharpness"), config.turnSharpnessLevel(), 1, 10)
                .setDefaultValue(CompanioConfig.DEFAULT.turnSharpnessLevel())
                .setSaveConsumer(value -> state.updateAndGet(current -> current.withTurnSharpness(value)))
                .build());
    }

    private static void addHatPlacement(
            ConfigBuilder builder,
            ConfigEntryBuilder entries,
            AtomicReference<CompanioConfig> state
    ) {
        ConfigCategory category = builder.getOrCreateCategory(
                Component.translatable("companio.menu.hat_placement"));
        HatPlacement hat = state.get().hat();

        category.addEntry(scaled(entries, "companio.option.hat_height",
                hat.offsetY(), HatPlacement.DEFAULT.offsetY(),
                HatPlacement.MIN_OFFSET, HatPlacement.MAX_OFFSET, 100, 2,
                value -> state.updateAndGet(current -> current.withHat(current.hat().withOffsetY(value)))));
        category.addEntry(scaled(entries, "companio.option.hat_forward",
                hat.offsetZ(), HatPlacement.DEFAULT.offsetZ(),
                HatPlacement.MIN_OFFSET, HatPlacement.MAX_OFFSET, 100, 2,
                value -> state.updateAndGet(current -> current.withHat(current.hat().withOffsetZ(value)))));
        category.addEntry(scaled(entries, "companio.option.hat_side",
                hat.offsetX(), HatPlacement.DEFAULT.offsetX(),
                HatPlacement.MIN_OFFSET, HatPlacement.MAX_OFFSET, 100, 2,
                value -> state.updateAndGet(current -> current.withHat(current.hat().withOffsetX(value)))));
        category.addEntry(scaled(entries, "companio.option.hat_scale",
                hat.scale(), HatPlacement.DEFAULT.scale(),
                HatPlacement.MIN_SCALE, HatPlacement.MAX_SCALE, 100, 2,
                value -> state.updateAndGet(current -> current.withHat(current.hat().withScale(value)))));
    }

    private static me.shedaniel.clothconfig2.api.AbstractConfigListEntry<Integer> scaled(
            ConfigEntryBuilder entries,
            String translationKey,
            double value,
            double defaultValue,
            double minimum,
            double maximum,
            int steps,
            int decimals,
            DoubleFunction<CompanioConfig> change
    ) {
        return entries.startIntSlider(
                        Component.translatable(translationKey),
                        (int) Math.round(value * steps),
                        (int) Math.round(minimum * steps),
                        (int) Math.round(maximum * steps))
                .setDefaultValue((int) Math.round(defaultValue * steps))
                .setTextGetter(raw -> Component.literal(
                        String.format(Locale.ROOT, "%." + decimals + "f", raw / (double) steps)))
                .setSaveConsumer(raw -> change.apply(raw / (double) steps))
                .build();
    }

    private static void update(
            AtomicReference<CompanioConfig> state,
            int index,
            java.util.function.UnaryOperator<CompanionSlot> change
    ) {
        state.updateAndGet(config -> index < config.companions().size()
                ? config.withCompanion(index, change.apply(config.companions().get(index)))
                : config);
    }
}
