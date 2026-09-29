package out.rizzve.companio.client.companion;

import java.util.Arrays;
import java.util.Optional;

public enum CompanionHat {
    NONE("none", null),
    STRAW("straw", "straw_hat"),
    CYLINDER("cylinder", "cylinder"),
    SCARECROW("scarecrow", "pugalo_hat"),
    WREATH("wreath", "red_and_green"),
    NIMBUS("nimbus", "nimbus"),
    DIVING_MASK("diving_mask", "water_mask_w");

    private final String id;
    private final String model;

    CompanionHat(String id, String model) {
        this.id = id;
        this.model = model;
    }

    public String id() {
        return id;
    }

    public String model() {
        return model;
    }

    public String translationKey() {
        return "companio.hat." + id;
    }

    public CompanionHat next() {
        CompanionHat[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static Optional<CompanionHat> byId(String id) {
        return Arrays.stream(values()).filter(hat -> hat.id.equals(id)).findFirst();
    }

    public static String[] ids() {
        return Arrays.stream(values()).map(CompanionHat::id).toArray(String[]::new);
    }
}
