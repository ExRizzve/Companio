package out.rizzve.companio.client.companion.movement;

public final class SmoothRotation {
    private SmoothRotation() {
    }

    public static float approach(float current, float target, float maximumStep) {
        return current + Math.clamp(wrapDegrees(target - current), -maximumStep, maximumStep);
    }

    public static float fromDirection(double x, double z, float fallback) {
        if (x * x + z * z < 1.0E-6) {
            return fallback;
        }
        return (float) Math.toDegrees(Math.atan2(-x, z));
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) wrapped -= 360.0F;
        else if (wrapped < -180.0F) wrapped += 360.0F;
        return wrapped;
    }
}
