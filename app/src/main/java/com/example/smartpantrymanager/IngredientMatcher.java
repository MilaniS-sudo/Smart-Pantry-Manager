package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CORE LOGIC - the strict-matching rule.
 *
 * A recipe is suggested ONLY if EVERY ingredient it needs is in the pantry
 * in AT LEAST the required quantity.
 *
 * Before comparing, names and units are normalised so trivial differences
 * do not break the match:
 *   "Tomatoes" / "tomato " / "TOMATO"  -> "tomato"
 *   "scallions"                        -> "spring onion" (synonym)
 *   1 kg -> 1000 g,  1 l -> 1000 ml,  1 cup -> 250 ml
 */
public final class IngredientMatcher {

    private static final String MASS = "mass";
    private static final String VOLUME = "volume";
    private static final String COUNT = "count";

    // unit -> factor to convert to the base unit (g, ml or pcs)
    private static final Map<String, Double> UNIT_FACTORS = new HashMap<>();
    // unit -> what kind of amount it measures
    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    // other spellings of units -> standard unit
    private static final Map<String, String> UNIT_ALIASES = new HashMap<>();
    // other names for the same ingredient -> standard name
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        addUnit("g", 1, MASS);
        addUnit("kg", 1000, MASS);
        addUnit("ml", 1, VOLUME);
        addUnit("l", 1000, VOLUME);
        addUnit("tsp", 5, VOLUME);
        addUnit("tbsp", 15, VOLUME);
        addUnit("cup", 250, VOLUME);
        addUnit("pcs", 1, COUNT);

        UNIT_ALIASES.put("gram", "g");
        UNIT_ALIASES.put("grams", "g");
        UNIT_ALIASES.put("kilogram", "kg");
        UNIT_ALIASES.put("kilograms", "kg");
        UNIT_ALIASES.put("litre", "l");
        UNIT_ALIASES.put("liter", "l");
        UNIT_ALIASES.put("litres", "l");
        UNIT_ALIASES.put("millilitre", "ml");
        UNIT_ALIASES.put("cups", "cup");
        UNIT_ALIASES.put("piece", "pcs");
        UNIT_ALIASES.put("pieces", "pcs");
        UNIT_ALIASES.put("pc", "pcs");
        UNIT_ALIASES.put("", "pcs");

        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("macaroni", "pasta");
        SYNONYMS.put("cheddar", "cheese");
        SYNONYMS.put("cheddar cheese", "cheese");
        SYNONYMS.put("cooking oil", "oil");
        SYNONYMS.put("sunflower oil", "oil");
        SYNONYMS.put("vegetable oil", "oil");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("chicken fillet", "chicken breast");
        SYNONYMS.put("garlic clove", "garlic");
        SYNONYMS.put("rolled oat", "oat");
    }

    private IngredientMatcher() { }

    private static void addUnit(String unit, double factor, String type) {
        UNIT_FACTORS.put(unit, factor);
        UNIT_TYPES.put(unit, type);
    }

    // ---------------- Normalisation ----------------

    /** "  Cherry Tomatoes! " -> "cherry tomato" */
    public static String normaliseName(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z ]", " ")   // remove digits and punctuation
                .replaceAll("\\s+", " ")      // collapse double spaces
                .trim();

        StringBuilder result = new StringBuilder();
        for (String word : text.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(singularise(word));
        }

        String name = result.toString();
        String synonym = SYNONYMS.get(name);
        return synonym != null ? synonym : name;
    }

    /** Simple plural -> singular rules. */
    static String singularise(String word) {
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("ies")) {                        // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes")) {                        // tomatoes -> tomato
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);    // peaches -> peach
        }
        if (word.endsWith("s") && !word.endsWith("ss")
                && !word.endsWith("us") && !word.endsWith("is")) {
            return word.substring(0, word.length() - 1);    // eggs -> egg
        }
        return word;
    }

    private static String normaliseUnit(String raw) {
        String unit = raw == null ? "" : raw.toLowerCase(Locale.ROOT).trim();
        String alias = UNIT_ALIASES.get(unit);
        return alias != null ? alias : unit;
    }

    private static String unitType(String unit) {
        String type = UNIT_TYPES.get(normaliseUnit(unit));
        // unknown units only match exactly the same unit
        return type != null ? type : "other:" + normaliseUnit(unit);
    }

    private static double toBaseAmount(double quantity, String unit) {
        Double factor = UNIT_FACTORS.get(normaliseUnit(unit));
        return quantity * (factor != null ? factor : 1.0);
    }

    /** e.g. "tomato|count" or "rice|mass" */
    private static String key(String name, String unit) {
        return normaliseName(name) + "|" + unitType(unit);
    }

    // ---------------- Matching ----------------

    /**
     * Lookup table of what the user has: key -> total amount in base units.
     * Duplicate pantry rows are added together (2 eggs + 1 egg = 3 eggs).
     */
    public static Map<String, Double> buildPantryIndex(List<Ingredient> pantry) {
        Map<String, Double> index = new HashMap<>();
        for (Ingredient item : pantry) {
            String k = key(item.getName(), item.getUnit());
            double amount = toBaseAmount(item.getQuantity(), item.getUnit());
            Double existing = index.get(k);
            index.put(k, existing == null ? amount : existing + amount);
        }
        return index;
    }

    /** True if the pantry has at least the amount this ingredient needs. */
    public static boolean hasEnough(Map<String, Double> pantryIndex, RecipeIngredient needed) {
        Double have = pantryIndex.get(key(needed.getName(), needed.getUnit()));
        double need = toBaseAmount(needed.getQuantity(), needed.getUnit());
        return have != null && have + 0.0001 >= need; // tiny tolerance for decimals
    }

    /** Names of the ingredients that are missing or not enough. */
    public static List<String> getMissingIngredients(Recipe recipe, Map<String, Double> pantryIndex) {
        List<String> missing = new ArrayList<>();
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasEnough(pantryIndex, needed)) {
                missing.add(needed.getName());
            }
        }
        return missing;
    }

    /** STRICT: only recipes with ZERO missing ingredients. */
    public static List<Recipe> findStrictMatches(List<Recipe> recipes, List<Ingredient> pantry) {
        Map<String, Double> index = buildPantryIndex(pantry);
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (!recipe.getIngredients().isEmpty()
                    && getMissingIngredients(recipe, index).isEmpty()) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /** BONUS: recipes missing EXACTLY one ingredient (shown in a separate list). */
    public static List<Recipe> findAlmostThere(List<Recipe> recipes, List<Ingredient> pantry) {
        Map<String, Double> index = buildPantryIndex(pantry);
        List<Recipe> almost = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (getMissingIngredients(recipe, index).size() == 1) {
                almost.add(recipe);
            }
        }
        return almost;
    }

}