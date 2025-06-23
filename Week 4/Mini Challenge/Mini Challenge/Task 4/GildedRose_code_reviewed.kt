package com.gildedrose

class GildedRose(val items: List<Item>) {

    companion object {
        const val AGED_BRIE = "Aged Brie"
        const val BACKSTAGE = "Backstage passes to a TAFKAL80ETC concert"
        const val SULFURAS = "Sulfuras, Hand of Ragnaros"
        const val MAX_QUALITY = 50
        const val MIN_QUALITY = 0
    }

    fun updateQuality() {
        for (item in items) {
            when (item.name) {
                SULFURAS -> {
                    // Legendary item, do nothing
                }
                AGED_BRIE -> {
                    updateAgedBrie(item)
                }
                BACKSTAGE -> {
                    updateBackstagePass(item)
                }
                else -> {
                    updateNormalItem(item)
                }
            }
        }
    }

    private fun updateAgedBrie(item: Item) {
        if (item.quality < MAX_QUALITY) item.quality++
        item.sellIn--
        if (item.sellIn < 0 && item.quality < MAX_QUALITY) item.quality++
    }

    private fun updateBackstagePass(item: Item) {
        if (item.quality < MAX_QUALITY) item.quality++
        if (item.sellIn < 11 && item.quality < MAX_QUALITY) item.quality++
        if (item.sellIn < 6 && item.quality < MAX_QUALITY) item.quality++
        item.sellIn--
        if (item.sellIn < 0) item.quality = MIN_QUALITY
    }

    private fun updateNormalItem(item: Item) {
        if (item.quality > MIN_QUALITY) item.quality--
        item.sellIn--
        if (item.sellIn < 0 && item.quality > MIN_QUALITY) item.quality--
    }
}

