# Foodish Project Performance Optimization Report

## 1. Identify Performance Bottlenecks

### Observed Bottlenecks
- **Repeated List Traversals:** Methods like `grandTotal(cartFoods)` traverse the entire cart list every time the total is needed.
- **Adapter Recreation:** Adapters are recreated instead of updating their data and calling `notifyDataSetChanged()`.
- **Static Shared State:** Use of static fields for data and UI elements can cause memory leaks and unnecessary reprocessing.
- **No Caching/Memoization:** Calculated values (like cart total) are not cached, leading to redundant computation.

---

## 2. Implementing Proper Memoization

### Before
```java
public static int grandTotal(List<GeneralFood> cartFoods){
    int totalPrice = 0;
    for(int i = 0 ; i < cartFoods.size(); i++) {
        totalPrice += cartFoods.get(i).getPrice();
    }
    return totalPrice;
}
```

### After (with Memoization)
```java
private Integer cachedTotal = null;
private int lastCartSize = -1;

private int grandTotal(List<GeneralFood> cartFoods){
    if (cachedTotal != null && lastCartSize == cartFoods.size()) {
        return cachedTotal;
    }
    int totalPrice = 0;
    for (GeneralFood food : cartFoods) {
        totalPrice += food.getPrice();
    }
    cachedTotal = totalPrice;
    lastCartSize = cartFoods.size();
    return totalPrice;
}

// Invalidate cache when cartFoods changes (e.g., item added/removed)
private void invalidateCartTotalCache() {
    cachedTotal = null;
    lastCartSize = -1;
}
```
**Performance Metric:**  
- **Before:** O(n) every call  
- **After:** O(1) if cart size unchanged, O(n) only when cart changes

---

## 3. Optimizing Filter Logic

### Before
```java
List<GeneralFood> filtered = new ArrayList<>();
for (GeneralFood food : allFoods) {
    if (food.getCategory().equals("Vegetarian")) {
        filtered.add(food);
    }
}
```

### After (using Java Streams for clarity and potential parallelism)
```java
List<GeneralFood> filtered = allFoods.stream()
    .filter(food -> "Vegetarian".equals(food.getCategory()))
    .collect(Collectors.toList());
```
**Performance Metric:**  
- **Before:** O(n), manual loop  
- **After:** O(n), but more readable and can be parallelized with `.parallelStream()`

---

## 4. Adding Performance Monitoring

### Example: Timing Expensive Operations
```java
long start = System.nanoTime();
int total = grandTotal(cartFoods);
long duration = System.nanoTime() - start;
Log.d("Performance", "grandTotal took " + duration + " ns");
```

### Example: Using Android Profiler
- Use Android Studio's built-in profiler to monitor memory, CPU, and UI thread usage.
- Add custom logs to track slow operations.

---

# Summary Table

| Area                | Before (Example)                | After (Optimized)                | Metric/Benefit                |
|---------------------|---------------------------------|----------------------------------|-------------------------------|
| List Traversal      | O(n) every call                 | O(1) with memoization            | Fewer redundant computations  |
| Filter Logic        | Manual for-loop                 | Stream API (or parallelStream)   | Readability, parallelism      |
| Adapter Updates     | Recreate adapter                | Update data + notifyDataSetChanged | Less UI overhead           |
| Performance Logs    | None                            | Log timing for key functions     | Identify real bottlenecks     |

---

# Recommendations

- **Memoize** expensive calculations that depend on rarely-changing data.
- **Use Streams** for filtering, mapping, and sorting for cleaner and potentially faster code.
- **Update Adapters Efficiently:** Only update data and call `notifyDataSetChanged()` instead of recreating adapters.
- **Add Logging:** Use `Log.d` or a monitoring library to track performance of key operations.
- **Profile Regularly:** Use Android Studio Profiler to catch UI jank, memory leaks, and slow operations.

---

# Example: Full Before/After for Cart Total Calculation

**Before:**
```java
public static int grandTotal(List<GeneralFood> cartFoods){
    int totalPrice = 0;
    for(int i = 0 ; i < cartFoods.size(); i++) {
        totalPrice += cartFoods.get(i).getPrice();
    }
    return totalPrice;
}
```

**After:**
```java
private Integer cachedTotal = null;
private int lastCartSize = -1;

private int grandTotal(List<GeneralFood> cartFoods){
    if (cachedTotal != null && lastCartSize == cartFoods.size()) {
        return cachedTotal;
    }
    int totalPrice = 0;
    for (GeneralFood food : cartFoods) {
        totalPrice += food.getPrice();
    }
    cachedTotal = totalPrice;
    lastCartSize = cartFoods.size();
    return totalPrice;
}

private void invalidateCartTotalCache() {
    cachedTotal = null;
    lastCartSize = -1;
}
```
**With Performance Logging:**
```java
long start = System.nanoTime();
int total = grandTotal(cartFoods);
long duration = System.nanoTime() - start;
Log.d("Performance", "grandTotal took " + duration + " ns");
``` 