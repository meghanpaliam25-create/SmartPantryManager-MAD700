# Smart Pantry Manager

## Project Overview

Smart Pantry Manager is an Android application developed using Java and SQLite. The application helps users manage ingredients stored in their pantry, monitor expiry dates, and discover recipes they can prepare using ingredients they already have.

The main objective of the application is to improve household food management, reduce unnecessary food waste, and make meal planning more convenient.

## Main Features

### 1. Pantry Management
- Add new ingredients with their names, quantities, measurement units, and expiry dates.
- View saved pantry ingredients.
- Edit existing ingredient information.
- Delete ingredients that are no longer available.
- Store ingredient information locally using SQLite.

### 2. Recipe Suggestions
- Access a collection of predefined recipes.
- Match available pantry ingredients against recipe requirements.
- Suggest recipes when the required ingredients and quantities are available.
- Support different measurement units, including grams, kilograms, millilitres, litres, pieces, and slices.

### 3. Recipe Details
- Select a suggested recipe to open its details.
- View the required ingredients and quantities.
- Read the preparation instructions.

### 4. Expiry Date Reminders
- Store expiry dates for pantry ingredients.
- Schedule background checks using Android WorkManager.
- Display notifications for ingredients approaching their expiry dates.
- Configure expiry reminder preferences through the Settings screen.

### 5. User Interface and Navigation
- Custom ingredient cards for displaying pantry information.
- Navigation between the pantry, suggested recipes, and settings.
- Three-dot navigation menu.
- Ingredient input validation and calendar-based expiry date selection.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java | Application logic and functionality |
| Android Studio | Development environment |
| Android SDK | Android application development |
| SQLite | Local database storage |
| XML | User interface layouts |
| WorkManager | Background expiry reminder scheduling |
| Git and GitHub | Version control and project hosting |

## Database Design

The application uses a local SQLite database named `SmartPantry.db`.

The main database tables are:

**pantry_items:** Stores pantry ingredients, including their names, quantities, units, and expiry dates.

**recipes:** Stores predefined recipes and their preparation instructions.

**recipe_ingredients:** Stores the ingredients, quantities, and units required for each recipe.

These tables support pantry management and recipe matching.

## Installation and Setup

1. Download or clone the project repository from GitHub.
2. Open Android Studio.
3. Select **Open** and choose the SmartPantryManager project folder.
4. Allow Gradle to synchronize the project dependencies.
5. Start an Android emulator or connect a compatible Android device.
6. Select the device in Android Studio.
7. Click **Run** to build and launch the application.

The SQLite database is created automatically when the application runs.

## How to Use the Application

1. Launch Smart Pantry Manager.
2. Select **Add Ingredient** to enter pantry items.
3. Provide the ingredient name, quantity, unit, and optional expiry date.
4. Save the ingredient.
5. Tap an existing ingredient to edit or delete it.
6. Select **Suggested Recipes** to view recipes that match the available ingredients.
7. Tap a recipe to view its ingredients and preparation instructions.
8. Open **Settings** to manage expiry reminder preferences.

## Testing

The application has been manually tested using an Android emulator.

The following functionality was tested successfully:

- Adding pantry ingredients.
- Editing saved ingredients.
- Deleting ingredients.
- Rejecting empty ingredient names.
- Rejecting invalid quantities.
- Saving ingredients without expiry dates.
- Displaying matching recipe suggestions.
- Opening recipe details.
- Navigating between application screens.
- Saving reminder preferences.
- Displaying expiry reminder notifications.

## Screenshots

Screenshots of the application can be added to demonstrate:

- Main pantry screen
- Add/Edit Ingredient screen
- Suggested Recipes screen
- Recipe Details screen
- Settings screen

## Future Improvements

Potential improvements include:

- Barcode scanning for adding grocery items.
- Shopping list generation.
- Cloud backup and synchronization.
- Additional recipes and meal-planning options.
- Improved ingredient unit conversions.

## Conclusion

Smart Pantry Manager demonstrates Android application development using Java, XML, and SQLite. It integrates local data storage, CRUD operations, recipe matching, notifications, and user-friendly navigation into a practical food management application.
