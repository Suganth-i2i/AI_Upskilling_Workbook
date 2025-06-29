# CartActivity.java Best Practices Report

## Overview
This report analyzes `CartActivity.java` for Java best practices violations, focusing on:
- State mutation issues
- Props mutation problems
- Performance optimization opportunities
- Proper immutable update patterns

---

## 1. State Mutation Issues
- **Problem:** `cartFoods` is imported as a static field from `MainActivity`, leading to shared mutable state across activities. This is discouraged in Android and can cause memory leaks and unpredictable behavior.
- **Problem:** `cartPrice` is a static `TextView`. Making UI elements static can cause memory leaks and is not lifecycle-aware.

---

## 2. Props Mutation Problems
- The method `grandTotal(List<GeneralFood> cartFoods)` does not mutate the list, so it is safe.
- However, since `cartFoods` is static and shared, mutations elsewhere can cause unpredictable UI updates and bugs.

---

## 3. Performance Optimization Opportunities
- Repeatedly calling `cartPrice.setText(Double.toString(grandTotal(cartFoods)));` can be inefficient if `cartFoods` is large. Consider caching the total or using a more efficient data structure.
- The adapter is recreated every time the activity is created. If the data changes, update the adapter's data and call `notifyDataSetChanged()` instead of recreating the adapter.

---

## 4. Proper Immutable Update Patterns
- Avoid static mutable state for UI and data.
- Use instance variables and pass data via `Intent` or a ViewModel.
- Use `final` where possible for variables that shouldn't change.

---

# Corrected Code Example

```java
public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerviewCart;
    private TextView cartPrice;
    private List<GeneralFood> cartFoods; // Instance variable, not static

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        // ... toolbar setup ...
        cartPrice = findViewById(R.id.cart_price);
        // Get cartFoods from a singleton manager, not static field
        cartFoods = CartManager.getInstance().getCartFoods();
        updateCartPrice();
        recyclerviewCart = findViewById(R.id.cart_recyclerview);
        recyclerviewCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerviewCart.setNestedScrollingEnabled(false);
        recyclerviewCart.setAdapter(new CartAdapter(cartFoods, R.layout.recyclerview_cart, getApplicationContext()));
    }
    private int grandTotal(List<GeneralFood> cartFoods){
        int totalPrice = 0;
        for (GeneralFood food : cartFoods) {
            totalPrice += food.getPrice();
        }
        return totalPrice;
    }
    private void updateCartPrice() {
        cartPrice.setText(String.valueOf(grandTotal(cartFoods)));
    }
    // Call updateCartPrice() when cart changes, e.g., via a callback from CartAdapter
}
```

---

## Explanations of Corrections
- **No static UI or data fields:** `cartPrice` and `cartFoods` are now instance variables.
- **No static data sharing:** Use a singleton manager or pass data via `Intent`/`ViewModel` for cart data.
- **Immutable update patterns:** The cart list is not mutated in this class. If you need to update it, do so via a manager or adapter, and always update the UI via a method (`updateCartPrice()`).
- **Performance:** Use enhanced for-loop for clarity. Only update the adapter's data when necessary, not recreate it.
- **General best practices:** Use `String.valueOf()` for setting text. Use private methods for encapsulation.

---

## Recommendations
- Use a singleton or ViewModel for cart data management.
- Avoid static fields for UI elements and shared data.
- Update UI reactively when data changes.
- Use immutable patterns and encapsulate state changes. 