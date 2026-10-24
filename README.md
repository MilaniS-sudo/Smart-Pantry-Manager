# Smart Pantry Manager

A Java Android app that helps reduce food waste. You record the ingredients you
already have at home, and the app suggests recipes you can cook using **only**
those ingredients, with no shopping trip needed.

## Features
- **Pantry management:** add, edit and delete ingredients (name, quantity, unit), with input validation
- **Pantry list:** RecyclerView with a custom adapter (`IngredientAdapter`)
- **Recipe collection:** 16 recipes seeded into the database on first run
- **Suggested Recipes:** strict matching, so a recipe appears only if every ingredient is in the
    pantry in at least the required quantity
- **Almost there (bonus):** a separate list of recipes missing exactly one ingredient
- **Recipe detail:** full ingredient checklist (✓ have / ✗ missing) and method
- **Settings:** saved preferences (show "Almost there" list, confirm before deleting)
- **Navigation:** toolbar options menu and home screen buttons

## Screens
| Screen | Class |
|---|---|
| Home | `MainActivity` |
| Pantry List | `PantryActivity` |
| Add/Edit Ingredient | `AddEditIngredientActivity` |
| Suggested Recipes | `RecipesActivity` |
| Recipe Detail | `RecipeDetailActivity` |
| Settings | `SettingsActivity` |

## Strict-matching rule
`IngredientMatcher` decides whether a recipe qualifies:
1. Ingredient names are normalised: lower case, plurals made singular ("tomatoes" → "tomato"),
      and common synonyms mapped ("mealie meal" → "maize meal").
2. Units are converted to base units: kg → g, l → ml, cup/tbsp/tsp → ml.
3. Duplicate pantry entries are added together.
4. A recipe is suggested only if **none** of its ingredients are missing or short.

## Database choice: SQLite
I used **SQLite** through `SQLiteOpenHelper` (`PantryDatabase`) because:
- the app works fully **offline** and needs no server, account or internet;
- the pantry is **personal data for one user on one device**, so cloud sync isn't needed;
- SQLite is **built into Android**, so no extra setup or cost is involved;
- it was covered in the module's persistent data chapter.

Tables: `ingredients`, `recipes`, and `recipe_ingredients` (foreign key to `recipes`).

## Setup and run
1. Clone the repository:
      `git clone https://github.com/MilaniS-sudo/Smart-Pantry-Manager.git`
2. Open the project folder in **Android Studio** (latest stable version).
3. Wait for Gradle to sync.
4. Run on an emulator or device with **Android 7.0 (API 24)** or higher.
5. Add a few ingredients (e.g. eggs 3 pcs, milk 1 l, butter 250 g), then open
      **Suggested Recipes** to see matches.

## Out of scope
No maps, GPS/location or payment features, as required by the brief.