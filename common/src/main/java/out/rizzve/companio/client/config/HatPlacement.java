package out.rizzve.companio.client.config;

public record HatPlacement(double offsetX, double offsetY, double offsetZ, double scale) {
    public static final HatPlacement DEFAULT = new HatPlacement(0.0, 0.0, 0.0, 1.05);

    public static final double MIN_OFFSET = -0.5;
    public static final double MAX_OFFSET = 0.5;
    public static final double MIN_SCALE = 0.4;
    public static final double MAX_SCALE = 2.0;

    public HatPlacement validated() {
        return new HatPlacement(
                clampFinite(offsetX, MIN_OFFSET, MAX_OFFSET, DEFAULT.offsetX),
                clampFinite(offsetY, MIN_OFFSET, MAX_OFFSET, DEFAULT.offsetY),
                clampFinite(offsetZ, MIN_OFFSET, MAX_OFFSET, DEFAULT.offsetZ),
                clampFinite(scale, MIN_SCALE, MAX_SCALE, DEFAULT.scale)
        );
    }

    public HatPlacement withOffsetX(double value) {
        return new HatPlacement(value, offsetY, offsetZ, scale).validated();
    }

    public HatPlacement withOffsetY(double value) {
        return new HatPlacement(offsetX, value, offsetZ, scale).validated();
    }

    public HatPlacement withOffsetZ(double value) {
        return new HatPlacement(offsetX, offsetY, value, scale).validated();
    }

    public HatPlacement withScale(double value) {
        return new HatPlacement(offsetX, offsetY, offsetZ, value).validated();
    }

    private static double clampFinite(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }
}
