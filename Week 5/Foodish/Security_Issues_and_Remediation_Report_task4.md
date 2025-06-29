# Security Issues & Remediation Report

## 1. XSS Vulnerabilities & Unsafe HTML Rendering

**Findings:**  
- The app does not render raw HTML or use WebViews, so direct XSS via HTML/JS injection is not present.
- However, user-controlled data (e.g., food titles, descriptions) is displayed in `TextView`s. If these are ever rendered as HTML (e.g., with `Html.fromHtml()` or in a WebView), XSS could be a risk.

**Remediation:**  
- Never use `Html.fromHtml()` or WebView to render untrusted data.
- Always use plain `setText()` for user-controlled content.

---

## 2. Input Validation Issues

**Findings:**  
- Data is passed between activities via `Intent` extras (e.g., food title, price, image URL).
- No explicit input validation is performed on these extras before use.

**Risks:**  
- Malicious or malformed data could crash the app or be used for phishing/social engineering.

**Remediation:**  
- Validate all `Intent` extras before use (e.g., check for null, type, and reasonable value ranges).
- Example:

```java
String foodTitle = getIntent().getStringExtra("foodTitle");
if (foodTitle == null || foodTitle.length() > 100) {
    foodTitle = "Unknown";
}
foodTitleTextView.setText(foodTitle);
```

---

## 3. URL/Navigation Security

**Findings:**  
- Navigation is performed via explicit `Intent` targets (e.g., `new Intent(context, Details.class)`).
- No implicit intents or external URLs are launched, so risk is low.

**Remediation:**  
- Always use explicit intents for internal navigation.
- If opening external URLs, validate the URL and use `Intent.ACTION_VIEW` safely.

---

## 4. Image Source Validation

**Findings:**  
- Images are loaded using Picasso with URLs from `Intent` extras or model data.
- No validation is performed on the image URL.

**Risks:**  
- Malicious URLs could be used for phishing, tracking, or denial-of-service.

**Remediation:**  
- Validate image URLs before loading:
  - Only allow HTTP(S) URLs.
  - Optionally, check for known domains or patterns.

**Example:**

```java
String imageUrl = getIntent().getStringExtra("foodImage");
if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
    Picasso.get().load(imageUrl).fit().into(foodImage);
} else {
    // Load a placeholder or show an error
    foodImage.setImageResource(R.drawable.ic_launcher_background);
}
```

---

## 5. Secure Alternatives & Explanations

| Issue                | Risk/Explanation                                                                 | Secure Alternative Example |
|----------------------|----------------------------------------------------------------------------------|---------------------------|
| XSS/HTML rendering   | Rendering untrusted HTML/JS can allow XSS attacks.                               | Use `setText()` only.     |
| Input validation     | Unchecked extras can crash app or be abused.                                     | Check for null, length, type. |
| Navigation           | Implicit/external intents can be hijacked or abused.                             | Use explicit intents.     |
| Image source         | Malicious URLs can be used for phishing, tracking, or DoS.                       | Validate URL scheme/domain. |

---

## 6. Example: Before/After for Image Loading

**Before:**
```java
Picasso.get().load(getIntent().getStringExtra("foodImage")).fit().into(foodImage);
```

**After:**
```java
String imageUrl = getIntent().getStringExtra("foodImage");
if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
    Picasso.get().load(imageUrl).fit().into(foodImage);
} else {
    foodImage.setImageResource(R.drawable.ic_launcher_background);
}
```

---

## 7. Recommendations

- Always validate all user and external input, especially when passed between activities.
- Never render untrusted HTML or JavaScript.
- Validate image URLs and never load from untrusted schemes.
- Use explicit intents for navigation.
- Consider using a security library or static analysis tool for further hardening. 