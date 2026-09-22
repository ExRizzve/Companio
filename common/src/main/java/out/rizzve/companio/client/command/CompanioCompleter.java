package out.rizzve.companio.client.command;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import out.rizzve.companio.client.companion.CompanionController;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public final class CompanioCompleter {
    private static final String[] DISTANCES = {"3", "5", "8", "12"};
    private static final String[] HEIGHTS = {"2", "3", "4", "5"};
    private static final String[] SPEEDS = {"0.06", "0.1", "0.15", "0.2"};
    private static final String[] FOLLOW_SPEEDS = {"0.35", "0.5", "0.65", "0.8"};
    private static final String[] LEVELS = {"1", "3", "5", "7", "10"};

    private CompanioCompleter() {
    }

    public static CompletableFuture<Suggestions> distances(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(DISTANCES, builder);
    }

    public static CompletableFuture<Suggestions> heights(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(HEIGHTS, builder);
    }

    public static CompletableFuture<Suggestions> speeds(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(SPEEDS, builder);
    }

    public static CompletableFuture<Suggestions> followSpeeds(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(FOLLOW_SPEEDS, builder);
    }

    public static CompletableFuture<Suggestions> levels(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(LEVELS, builder);
    }

    public static CompletableFuture<Suggestions> companions(
            CompanionController controller,
            SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(
                IntStream.rangeClosed(1, controller.size()).mapToObj(String::valueOf),
                builder
        );
    }
}
