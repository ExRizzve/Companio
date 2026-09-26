package out.rizzve.companio.client.config;

import java.util.regex.Pattern;

public record CompanioConfig(
        String lastPlayerName,
        double hoverHeight,
        double wanderRadius,
        double maxDistance,
        double maxSpeed,
        double acceleration,
        double followSpeed,
        float turnSpeed
) {
    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    public static final CompanioConfig DEFAULT = new CompanioConfig("", 2.6, 3.5, 15.0, 0.09, 0.012, 0.42, 11.0F);

    public CompanioConfig validated() {
        return new CompanioConfig(
                validName(lastPlayerName) ? lastPlayerName : "",
                clampFinite(hoverHeight, 1.5, 6.0, DEFAULT.hoverHeight),
                clampFinite(wanderRadius, 1.0, 12.0, DEFAULT.wanderRadius),
                clampFinite(maxDistance, 3.0, 30.0, DEFAULT.maxDistance),
                clampFinite(maxSpeed, 0.04, 0.2, DEFAULT.maxSpeed),
                clampFinite(acceleration, 0.004, 0.03, DEFAULT.acceleration),
                clampFinite(followSpeed, 0.25, 0.8, DEFAULT.followSpeed),
                (float) clampFinite(turnSpeed, 3.0, 24.0, DEFAULT.turnSpeed)
        );
    }

    public CompanioConfig withLastPlayerName(String playerName) {
        return copy(playerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed);
    }

    public CompanioConfig withHoverHeight(double value) {
        return copy(lastPlayerName, value, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed);
    }

    public CompanioConfig withWanderRadius(double value) {
        return copy(lastPlayerName, hoverHeight, value, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed);
    }

    public CompanioConfig withMaxSpeed(double value) {
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, value, acceleration, followSpeed, turnSpeed);
    }

    public CompanioConfig withSmoothness(int level) {
        double value = 0.032 - Math.clamp(level, 1, 10) * 0.0026;
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, value, followSpeed, turnSpeed);
    }

    public CompanioConfig withFollowSpeed(double value) {
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, value, turnSpeed);
    }

    public CompanioConfig withTurnSharpness(int level) {
        float value = 2.0F + Math.clamp(level, 1, 10) * 2.2F;
        return copy(lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, value);
    }

    private static CompanioConfig copy(
            String lastPlayerName,
            double hoverHeight,
            double wanderRadius,
            double maxDistance,
            double maxSpeed,
            double acceleration,
            double followSpeed,
            float turnSpeed
    ) {
        return new CompanioConfig(
                lastPlayerName, hoverHeight, wanderRadius, maxDistance, maxSpeed, acceleration, followSpeed, turnSpeed
        ).validated();
    }

    private static boolean validName(String value) {
        return value != null && (value.isEmpty() || PLAYER_NAME.matcher(value).matches());
    }

    private static double clampFinite(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }
}
