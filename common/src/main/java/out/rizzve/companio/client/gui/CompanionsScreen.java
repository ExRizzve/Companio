package out.rizzve.companio.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.CompanionSlot;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.gui.cloth.ClothConfigScreenFactory;
import out.rizzve.companio.client.skin.MojangProfileService;

import java.util.ArrayList;
import java.util.List;

public final class CompanionsScreen extends Screen {
    public static final Component TITLE = Component.translatable("companio.screen.title");

    private static final int ROW_HEIGHT = 20;
    private static final int MAX_SPACING = 23;
    private static final int MIN_SPACING = 21;
    private static final int GAP = 4;
    private static final int NUMBER_WIDTH = 14;
    private static final int NAME_WIDTH = 96;
    private static final int HAT_WIDTH = 104;
    private static final int DELETE_WIDTH = 14;
    private static final int ROW_WIDTH =
            NUMBER_WIDTH + NAME_WIDTH * 2 + HAT_WIDTH + DELETE_WIDTH + GAP * 4;
    private static final int HEADER_BOTTOM = 52;
    private static final int FOOTER_HEIGHT = 34;
    private static final int SKIN_DELAY_TICKS = 20;

    private final Screen parent;
    private final CompanionController controller;
    private final ConfigManager configManager;
    private final MojangProfileService profileService;

    private CompanioConfig config;
    private int pendingSkinTicks = -1;

    public CompanionsScreen(
            Screen parent,
            CompanionController controller,
            ConfigManager configManager,
            MojangProfileService profileService
    ) {
        super(TITLE);
        this.parent = parent;
        this.controller = controller;
        this.configManager = configManager;
        this.profileService = profileService;
        this.config = configManager.load();
    }

    @Override
    protected void init() {
        List<CompanionSlot> slots = config.companions();
        int left = (width - ROW_WIDTH) / 2;
        int spacing = spacing(slots.size());

        addRenderableWidget(new StringWidget(left, 16, ROW_WIDTH, ROW_HEIGHT,
                Component.translatable("companio.menu.header", slots.size(), CompanionController.MAX_COMPANIONS),
                font));

        if (slots.isEmpty()) {
            addRenderableWidget(new StringWidget(left, HEADER_BOTTOM, ROW_WIDTH, ROW_HEIGHT,
                    Component.translatable("companio.menu.empty").withStyle(ChatFormatting.GRAY), font));
        } else {
            addColumnHeaders(left);
        }

        for (int index = 0; index < slots.size(); index++) {
            addRow(left, HEADER_BOTTOM + index * spacing, index, slots.get(index));
        }

        addFooter(left, footerTop(slots.size(), spacing), slots.size());
    }

    private void addColumnHeaders(int left) {
        int x = left + NUMBER_WIDTH + GAP;
        addRenderableWidget(header(x, NAME_WIDTH, "companio.menu.player_name"));
        x += NAME_WIDTH + GAP;
        addRenderableWidget(header(x, NAME_WIDTH, "companio.menu.name"));
        x += NAME_WIDTH + GAP;
        addRenderableWidget(header(x, HAT_WIDTH, "companio.menu.hat_label"));
    }

    private StringWidget header(int x, int columnWidth, String translationKey) {
        return new StringWidget(x, HEADER_BOTTOM - 14, columnWidth, 10,
                Component.translatable(translationKey).withStyle(ChatFormatting.GRAY), font);
    }

    private void addRow(int left, int y, int index, CompanionSlot slot) {
        int x = left;

        addRenderableWidget(new StringWidget(x, y + 6, NUMBER_WIDTH, 10,
                Component.literal(String.valueOf(index + 1)).withStyle(ChatFormatting.DARK_GRAY), font));
        x += NUMBER_WIDTH + GAP;

        EditBox playerName = new EditBox(font, x, y, NAME_WIDTH, ROW_HEIGHT,
                Component.translatable("companio.menu.player_name"));
        playerName.setMaxLength(16);
        playerName.setHint(Component.translatable("companio.menu.steve").withStyle(ChatFormatting.DARK_GRAY));
        playerName.setValue(slot.playerName());
        playerName.setTooltip(Tooltip.create(Component.translatable("companio.menu.player_name_tooltip")));
        playerName.setResponder(value -> {
            edit(index, current -> current.withPlayerName(value.trim()));
            pendingSkinTicks = SKIN_DELAY_TICKS;
        });
        addRenderableWidget(playerName);
        x += NAME_WIDTH + GAP;

        EditBox displayName = new EditBox(font, x, y, NAME_WIDTH, ROW_HEIGHT,
                Component.translatable("companio.menu.name"));
        displayName.setMaxLength(CompanionSlot.MAX_NAME_LENGTH);
        displayName.setHint(Component.translatable("companio.menu.no_name").withStyle(ChatFormatting.DARK_GRAY));
        displayName.setValue(slot.name());
        displayName.setTooltip(Tooltip.create(Component.translatable("companio.menu.name_tooltip")));
        displayName.setResponder(value -> {
            edit(index, current -> current.withName(value));
            apply();
        });
        addRenderableWidget(displayName);
        x += NAME_WIDTH + GAP;

        addRenderableWidget(Button.builder(
                        Component.translatable(slot.hat().translationKey()),
                        button -> {
                            edit(index, current -> current.withHat(current.hat().next()));
                            apply();
                            rebuildWidgets();
                        })
                .tooltip(Tooltip.create(Component.translatable("companio.menu.hat_tooltip")))
                .bounds(x, y, HAT_WIDTH, ROW_HEIGHT).build());
        x += HAT_WIDTH + GAP;

        addRenderableWidget(Button.builder(
                        Component.literal("×").withStyle(ChatFormatting.GRAY),
                        button -> {
                            config = config.withCompanionRemoved(index);
                            apply();
                            rebuildWidgets();
                        })
                .tooltip(Tooltip.create(Component.translatable("companio.menu.remove")))
                .bounds(x, y + 2, DELETE_WIDTH, ROW_HEIGHT - 4).build());
    }

    private void addFooter(int left, int y, int count) {
        int third = (ROW_WIDTH - GAP * 2) / 3;

        Button add = Button.builder(Component.translatable("companio.menu.add"), button -> {
                    config = config.withCompanionAdded(CompanionSlot.EMPTY);
                    apply();
                    rebuildWidgets();
                })
                .tooltip(Tooltip.create(Component.translatable("companio.menu.add_tooltip")))
                .bounds(left, y, third, ROW_HEIGHT).build();
        add.active = count < CompanionController.MAX_COMPANIONS;
        addRenderableWidget(add);

        addRenderableWidget(Button.builder(Component.translatable("companio.menu.settings"),
                        button -> ScreenCompat.setScreen(minecraft, ClothConfigScreenFactory.create(
                                this, controller, configManager, profileService)))
                .tooltip(Tooltip.create(Component.translatable("companio.menu.settings_tooltip")))
                .bounds(left + third + GAP, y, third, ROW_HEIGHT).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(left + (third + GAP) * 2, y, third, ROW_HEIGHT).build());
    }

    private int spacing(int count) {
        if (count <= 1) {
            return MAX_SPACING;
        }
        int available = height - HEADER_BOTTOM - FOOTER_HEIGHT;
        return Math.clamp(available / count, MIN_SPACING, MAX_SPACING);
    }

    private int footerTop(int count, int spacing) {
        int afterRows = HEADER_BOTTOM + Math.max(count, 1) * spacing + 6;
        return Math.min(height - FOOTER_HEIGHT + 6, afterRows);
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingSkinTicks > 0 && --pendingSkinTicks == 0) {
            apply();
        }
    }

    @Override
    public void onClose() {
        if (pendingSkinTicks > 0) {
            apply();
        }
        ScreenCompat.setScreen(minecraft, parent);
    }

    private void edit(int index, java.util.function.UnaryOperator<CompanionSlot> change) {
        List<CompanionSlot> updated = new ArrayList<>(config.companions());
        if (index >= updated.size()) {
            return;
        }
        updated.set(index, change.apply(updated.get(index)));
        config = config.withCompanions(updated);
    }

    private void apply() {
        pendingSkinTicks = -1;
        config = configManager.load().withCompanions(config.companions());
        configManager.save(config);
        controller.sync(config, minecraft, profileService, configManager);
    }
}
