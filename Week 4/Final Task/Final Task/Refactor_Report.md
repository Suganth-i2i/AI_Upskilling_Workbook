# GildedRose `updateQuality` Refactoring Report

## Original Issues
- Deeply nested conditionals made the code hard to read and maintain.
- Magic strings and numbers (item names, quality limits) were hardcoded throughout.
- All item update logic was in a single function, violating the Single Responsibility Principle.
- Code duplication for quality and sellIn updates.
- No use of object-oriented principles; missed opportunity for polymorphism.
- Index-based iteration was less idiomatic in Kotlin.
- Lack of comments and clear naming.

## Refactoring Approach
- Introduced an `ItemUpdater` interface and specific updater classes for each item type (`Aged Brie`, `Backstage passes`, `Sulfuras`, and a default updater).
- Used a map to delegate update logic to the appropriate updater based on item name.
- Replaced magic strings and numbers with constants.
- Used idiomatic Kotlin iteration (`for (item in items)`).
- Reduced code duplication by encapsulating logic in updater classes.
- Improved readability and maintainability by separating concerns.

## Benefits of the New Design
- **Readability:** Logic for each item type is clearly separated and easy to follow.
- **Maintainability:** Adding new item types or changing rules is straightforward.
- **Extensibility:** New updaters can be added without modifying the core loop.
- **Testability:** Each updater can be tested independently.
- **Idiomatic Kotlin:** Uses modern Kotlin practices for iteration and constants.

---

*This refactoring brings the code closer to SOLID principles and prepares it for future requirements or extensions.* 