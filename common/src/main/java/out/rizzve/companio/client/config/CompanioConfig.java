package out.rizzve.companio.client.config;

import out.rizzve.companio.client.companion.CompanionController;

import java.util.List;
import java.util.regex.Pattern;

public record CompanioConfig(
        String lastPlayerName,
        double hoverHeight,
        double wanderRadius,
        double maxDistance,
        double maxSpeed,
        double acceleration,
        double followSpeed,
        float turnSpeed,
        HatPlacement hat,
        List<CompanionSlot> companions
) {
    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    public static final CompanioConfig DEFAULT = new CompanioConfig("", 2.6, 3.5, 15.0, 0.09, 0.012, 0.42, 11.0F, HatPlacement.DEFAULT, List.of());

    public CompanioConfig validated() {
        return new CompanioConfig(
                validName(lastPlayerName) ? lastPlayerName : "",
                clampFinite(hoverHeight, 1.5, 6.0, DEFAULT.hoverHeight),
                clampFinite(wanderRadius, 1.0, 12.0, DEFAULT.wanderRadius),
                clampFinite(maxDistance, 3.0, 30.0, DEFAULT.maxDistance),
                clampFinite(maxSpeed, 0.04, 0.2, DEFAULT.maxSpeed),
                clampFinite(acceleration, 0.004, 0.03, DEFAULT.acceleration),
                clampFinite(followSpeed, 0.25, 0.8, DEFAULT.followSpeed),
                (float) clampFinite(turnSpeed, 3.0, 24.0, DEFAULT.turnSpeed),
                hat == null ? HatPlacement.DEFAULT : hat.validated(),
                validatedSlots(companions)
        );
    }

    private static List<CompanionSlot> validatedSlots(List<CompanionSlot> slots) {
        if (slots == null) {
            return List.of();
        }
        return slots.stream()
                .filter(java.util.Objects::nonNull)
                .limit(CompanionController.MAX_COMPANIONS)
                .map(CompanionSlot::validated)
                .toList();
    }

    public CompanioConfig withCompanionAdded(CompanionSlot slot) {
        List<CompanionSlot> updated = new java.util.ArrayList<>(companions);
        updated.add(slot);
        return withCompanions(updated);
    }

    public CompanioConfig withCompanionRemoved(int index) {
        List<CompanionSlot> updated = new java.util.ArrayList<>(companions);
        updated.remove(index);
        return withCompanions(updated);
    }

    public CompanioConfig withCompanions(List<CompanionSlot> value) {
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed, hat, value);
    }

    public CompanioConfig withCompanion(int index, CompanionSlot slot) {
        List<CompanionSlot> updated = new java.util.ArrayList<>(companions);
        updated.set(index, slot);
        return withCompanions(updated);
    }

    public CompanioConfig withHat(HatPlacement value) {
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed, value, companions);
    }

    public CompanioConfig withLastPlayerName(String playerName) {
        return copy(playerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed, hat, companions);
    }

    public CompanioConfig withHoverHeight(double value) {
        return copy(lastPlayerName, value, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed, hat, companions);
    }

    public CompanioConfig withWanderRadius(double value) {
        return copy(lastPlayerName, hoverHeight, value, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed, hat, companions);
    }

    public CompanioConfig withMaxSpeed(double value) {
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, value, acceleration, followSpeed, turnSpeed, hat, companions);
    }

    public CompanioConfig withSmoothness(int level) {
        double value = 0.032 - Math.clamp(level, 1, 10) * 0.0026;
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, value, followSpeed, turnSpeed, hat, companions);
    }

    public int smoothnessLevel() {
        return (int) Math.clamp(Math.round((0.032 - acceleration) / 0.0026), 1, 10);
    }

    public int turnSharpnessLevel() {
        return (int) Math.clamp(Math.round((turnSpeed - 2.0) / 2.2), 1, 10);
    }

    public CompanioConfig withFollowSpeed(double value) {
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, value, turnSpeed, hat, companions);
    }

    public CompanioConfig withTurnSharpness(int level) {
        float value = 2.0F + Math.clamp(level, 1, 10) * 2.2F;
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, value, hat, companions);
    }

    private static CompanioConfig copy(
            String lastPlayerName,
            double hoverHeight,
            double wanderRadius,
            double maxDistance,
            double maxSpeed,
            double acceleration,
            double followSpeed,
            float turnSpeed,
            HatPlacement hat,
            List<CompanionSlot> companions
    ) {
        return new CompanioConfig(
                lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed, hat, companions
        ).validated();
    }

    private static boolean validName(String value) {
        return value != null && (value.isEmpty() || PLAYER_NAME.matcher(value).matches());
    }

    private static double clampFinite(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }
}
