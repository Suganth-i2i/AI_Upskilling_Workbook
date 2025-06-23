# GildedRose Refactoring Report

## 1. Original Code (Before Refactor)
```kotlin
class GildedRose(val items: List<Item>) {
    fun updateQuality() {
        for (i in items.indices) {
            if (items[i].name != "Aged Brie" && items[i].name != "Backstage passes to a TAFKAL80ETC concert") {
                if (items[i].quality > 0) {
                    if (items[i].name != "Sulfuras, Hand of Ragnaros") {
                        items[i].quality = items[i].quality - 1
                    }
                }
            } else {
                if (items[i].quality < 50) {
                    items[i].quality = items[i].quality + 1
                    if (items[i].name == "Backstage passes to a TAFKAL80ETC concert") {
                        if (items[i].sellIn < 11) {
                            if (items[i].quality < 50) {
                                items[i].quality = items[i].quality + 1
                            }
                        }
                        if (items[i].sellIn < 6) {
                            if (items[i].quality < 50) {
                                items[i].quality = items[i].quality + 1
                            }
                        }
                    }
                }
            }
            if (items[i].name != "Sulfuras, Hand of Ragnaros") {
                items[i].sellIn = items[i].sellIn - 1
            }
            if (items[i].sellIn < 0) {
                if (items[i].name != "Aged Brie") {
                    if (items[i].name != "Backstage passes to a TAFKAL80ETC concert") {
                        if (items[i].quality > 0) {
                            if (items[i].name != "Sulfuras, Hand of Ragnaros") {
                                items[i].quality = items[i].quality - 1
                            }
                        }
                    } else {
                        items[i].quality = items[i].quality - items[i].quality
                    }
                } else {
                    if (items[i].quality < 50) {
                        items[i].quality = items[i].quality + 1
                    }
                }
            }
        }
    }
}
```

### Issues in the Original Code
- Long, complex, and deeply nested function
- Magic strings for item types
- Duplicated logic for quality and sellIn updates
- No use of polymorphism or abstraction
- Hardcoded quality bounds

## 2. Refactored Code (After Refactor)
```kotlin
interface ItemUpdater {
    fun update(item: Item)
}

class AgedBrieUpdater : ItemUpdater {
    override fun update(item: Item) {
        if (item.quality < 50) item.quality++
        item.sellIn--
        if (item.sellIn < 0 && item.quality < 50) item.quality++
    }
}

class BackstagePassUpdater : ItemUpdater {
    override fun update(item: Item) {
        if (item.quality < 50) item.quality++
        if (item.sellIn < 11 && item.quality < 50) item.quality++
        if (item.sellIn < 6 && item.quality < 50) item.quality++
        item.sellIn--
        if (item.sellIn < 0) item.quality = 0
    }
}

class SulfurasUpdater : ItemUpdater {
    override fun update(item: Item) {
        // Legendary item, does not change
    }
}

class DefaultUpdater : ItemUpdater {
    override fun update(item: Item) {
        if (item.quality > 0) item.quality--
        item.sellIn--
        if (item.sellIn < 0 && item.quality > 0) item.quality--
    }
}

class GildedRose(val items: List<Item>) {
    private val updaters = mapOf(
        "Aged Brie" to AgedBrieUpdater(),
        "Backstage passes to a TAFKAL80ETC concert" to BackstagePassUpdater(),
        "Sulfuras, Hand of Ragnaros" to SulfurasUpdater()
    )
    private val defaultUpdater = DefaultUpdater()
    fun updateQuality() {
        for (item in items) {
            val updater = updaters[item.name] ?: defaultUpdater
            updater.update(item)
        }
    }
}
```

### Improvements in the Refactored Code
- Uses the Strategy pattern for item updates
- Removes magic strings from main logic
- Each item type's logic is encapsulated in its own class
- Reduces code duplication and complexity
- Easier to extend and maintain

## 3. Summary Table
| Aspect                | Before Refactor | After Refactor         |
|-----------------------|-----------------|------------------------|
| Readability           | Low             | High                   |
| Extensibility         | Poor            | Excellent              |
| Duplication           | High            | Low                    |
| Testability           | Poor            | Good                   |
| SOLID Principles      | Violated        | Followed (OCP, SRP)    |

---
For more details, see the README. 