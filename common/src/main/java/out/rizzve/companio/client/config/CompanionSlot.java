package out.rizzve.companio.client.config;

import out.rizzve.companio.client.companion.CompanionHat;

import java.util.regex.Pattern;

public record CompanionSlot(
        String playerName,
        CompanionHat hat,
        String name,
        String profileId,
        String textures,
        String texturesSignature
) {
    public static final CompanionSlot EMPTY = new CompanionSlot("", CompanionHat.NONE, "", "", "", "");

    public static final int MAX_NAME_LENGTH = 32;

    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{0,16}");

    public CompanionSlot validated() {
        return new CompanionSlot(
                playerName != null && PLAYER_NAME.matcher(playerName).matches() ? playerName : "",
                hat == null ? CompanionHat.NONE : hat,
                name == null ? "" : name.substring(0, Math.min(name.length(), MAX_NAME_LENGTH)),
                profileId == null ? "" : profileId,
                textures == null ? "" : textures,
                texturesSignature == null ? "" : texturesSignature
        );
    }

    public boolean hasCachedSkin() {
        return !profileId.isEmpty() && !textures.isEmpty();
    }

    public CompanionSlot withPlayerName(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.equalsIgnoreCase(playerName)
                ? this
                : new CompanionSlot(trimmed, hat, name, "", "", "").validated();
    }

    public CompanionSlot withHat(CompanionHat value) {
        return new CompanionSlot(playerName, value, name, profileId, textures, texturesSignature).validated();
    }

    public CompanionSlot withName(String value) {
        return new CompanionSlot(playerName, hat, value, profileId, textures, texturesSignature).validated();
    }

    public CompanionSlot withCachedSkin(String profileId, String textures, String texturesSignature) {
        return new CompanionSlot(playerName, hat, name, profileId, textures, texturesSignature).validated();
    }
}
