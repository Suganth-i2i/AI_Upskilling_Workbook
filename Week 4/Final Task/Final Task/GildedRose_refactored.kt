package com.gildedrose

class GildedRose(val items: List<Item>) {

    private val updaters = mapOf(
        "Aged Brie" to AgedBrieUpdater(),
        "Backstage passes to a TAFKAL80ETC concert" to BackstagePassUpdater(),
        "Sulfuras, Hand of Ragnaros" to SulfurasUpdater()
    )

    fun updateQuality() {
        for (item in items) {
            val updater = updaters[item.name] ?: DefaultUpdater()
            updater.update(item)
        }
    }
}

private const val MAX_QUALITY = 50
private const val MIN_QUALITY = 0

interface ItemUpdater {
    fun update(item: Item)
}

class DefaultUpdater : ItemUpdater {
    override fun update(item: Item) {
        if (item.quality > MIN_QUALITY) {
            item.quality--
        }
        item.sellIn--
        if (item.sellIn < 0 && item.quality > MIN_QUALITY) {
            item.quality--
        }
    }
}

class AgedBrieUpdater : ItemUpdater {
    override fun update(item: Item) {
        if (item.quality < MAX_QUALITY) {
            item.quality++
        }
        item.sellIn--
        if (item.sellIn < 0 && item.quality < MAX_QUALITY) {
            item.quality++
        }
    }
}

class BackstagePassUpdater : ItemUpdater {
    override fun update(item: Item) {
        if (item.quality < MAX_QUALITY) {
            item.quality++
            if (item.sellIn < 11 && item.quality < MAX_QUALITY) {
                item.quality++
            }
            if (item.sellIn < 6 && item.quality < MAX_QUALITY) {
                item.quality++
            }
        }
        item.sellIn--
        if (item.sellIn < 0) {
            item.quality = MIN_QUALITY
        }
    }
}

class SulfurasUpdater : ItemUpdater {
    override fun update(item: Item) {
        // Legendary item, does not change
    }
}

