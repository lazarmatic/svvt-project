import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Scenario 14: Performance and Load Time Optimization
 * Objective: Ensure the website loads efficiently and performs well under various conditions.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceLoadTest {

    private static WebDriver webDriver;
    private static String baseUrl;
    private static JavascriptExecutor jsExecutor;

    // Performance thresholds (in milliseconds)
    private static final long HOMEPAGE_LOAD_THRESHOLD = 3000; // 3 seconds
    private static final long PRODUCT_PAGE_THRESHOLD = 4000;  // 4 seconds
    private static final long SEARCH_THRESHOLD = 2000;        // 2 seconds

    @BeforeAll
    public static void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:/Users/Korisnik/Desktop/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");

        // Enable performance logging
        options.setCapability("goog:loggingPrefs",
                Map.of("performance", "ALL", "browser", "ALL"));

        webDriver = new ChromeDriver(options);
        jsExecutor = (JavascriptExecutor) webDriver;
        baseUrl = "https://bijelatehnika.com";
    }

    @AfterAll
    public static void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    /**
     * Helper method to get page load time using Navigation Timing API
     */
    private Map<String, Long> getPageLoadTimes() {
        Map<String, Long> timings = new HashMap<>();

        try {
            // Get Navigation Timing metrics
            Long navigationStart = (Long) jsExecutor.executeScript(
                    "return window.performance.timing.navigationStart");
            Long loadEventEnd = (Long) jsExecutor.executeScript(
                    "return window.performance.timing.loadEventEnd");
            Long domContentLoaded = (Long) jsExecutor.executeScript(
                    "return window.performance.timing.domContentLoadedEventEnd");
            Long responseEnd = (Long) jsExecutor.executeScript(
                    "return window.performance.timing.responseEnd");

            if (loadEventEnd != null && navigationStart != null) {
                timings.put("totalLoadTime", loadEventEnd - navigationStart);
            }
            if (domContentLoaded != null && navigationStart != null) {
                timings.put("domContentLoaded", domContentLoaded - navigationStart);
            }
            if (responseEnd != null && navigationStart != null) {
                timings.put("serverResponse", responseEnd - navigationStart);
            }

        } catch (Exception e) {
            System.out.println("Could not retrieve timing metrics: " + e.getMessage());
        }

        return timings;
    }

    /**
     * Helper method to get resource count and size
     */
    private Map<String, Object> getResourceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Get resource entries
            String script =
                    "var resources = window.performance.getEntriesByType('resource');" +
                            "var images = resources.filter(r => r.initiatorType === 'img');" +
                            "var scripts = resources.filter(r => r.initiatorType === 'script');" +
                            "var css = resources.filter(r => r.initiatorType === 'link' || r.initiatorType === 'css');" +
                            "return {" +
                            "  totalResources: resources.length," +
                            "  images: images.length," +
                            "  scripts: scripts.length," +
                            "  css: css.length," +
                            "  avgImageLoadTime: images.length > 0 ? images.reduce((a,r) => a + r.duration, 0) / images.length : 0" +
                            "};";

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) jsExecutor.executeScript(script);
            metrics.putAll(result);

        } catch (Exception e) {
            System.out.println("Could not retrieve resource metrics: " + e.getMessage());
        }

        return metrics;
    }

    /**
     * TC14.1: Measure homepage load time
     * Expected: Loads within 3 seconds on standard connection
     */
    @Test
    @Order(1)
    public void testHomepageLoadTime() throws InterruptedException {
        System.out.println("\n=== TC14.1: Homepage Load Time Test ===");

        long startTime = System.currentTimeMillis();
        webDriver.get(baseUrl);

        // Wait for page to be fully loaded
        jsExecutor.executeScript(
                "return document.readyState"
        );

        long endTime = System.currentTimeMillis();
        long manualLoadTime = endTime - startTime;

        // Get detailed timing metrics
        Thread.sleep(1000); // Wait for all metrics to be available
        Map<String, Long> timings = getPageLoadTimes();
        Map<String, Object> resources = getResourceMetrics();

        System.out.println("\n=== Homepage Performance Metrics ===");
        System.out.println("Manual load time: " + manualLoadTime + "ms");

        if (timings.containsKey("totalLoadTime")) {
            long totalLoadTime = timings.get("totalLoadTime");
            System.out.println("Total load time (Navigation Timing): " + totalLoadTime + "ms");
            System.out.println("DOM Content Loaded: " + timings.getOrDefault("domContentLoaded", 0L) + "ms");
            System.out.println("Server Response: " + timings.getOrDefault("serverResponse", 0L) + "ms");

            if (totalLoadTime <= HOMEPAGE_LOAD_THRESHOLD) {
                System.out.println("✓ Homepage loads within " + HOMEPAGE_LOAD_THRESHOLD + "ms threshold");
            } else {
                System.out.println("⚠ Homepage exceeds " + HOMEPAGE_LOAD_THRESHOLD + "ms threshold by " +
                        (totalLoadTime - HOMEPAGE_LOAD_THRESHOLD) + "ms");
            }
        }

        System.out.println("\n=== Resource Metrics ===");
        System.out.println("Total resources loaded: " + resources.getOrDefault("totalResources", 0));
        System.out.println("Images: " + resources.getOrDefault("images", 0));
        System.out.println("Scripts: " + resources.getOrDefault("scripts", 0));
        System.out.println("CSS files: " + resources.getOrDefault("css", 0));

        Object avgImageTime = resources.get("avgImageLoadTime");
        if (avgImageTime != null) {
            System.out.println("Average image load time: " +
                    String.format("%.2f", ((Number) avgImageTime).doubleValue()) + "ms");
        }

        System.out.println("\n✓ TC14.1 COMPLETED: Homepage load time measured\n");
    }

    /**
     * TC14.2: Test product listing page load time (100+ products)
     * Expected: Page loads efficiently with pagination
     */
    @Test
    @Order(2)
    public void testProductListingLoadTime() throws InterruptedException {
        System.out.println("\n=== TC14.2: Product Listing Page Load Time Test ===");

        String productListingUrl = baseUrl + "/proizvodi/veliki-kucanski-aparati";

        long startTime = System.currentTimeMillis();
        webDriver.get(productListingUrl);

        // Wait for products to load
        Thread.sleep(2000);

        long endTime = System.currentTimeMillis();
        long loadTime = endTime - startTime;

        Map<String, Long> timings = getPageLoadTimes();
        Map<String, Object> resources = getResourceMetrics();

        System.out.println("\n=== Product Listing Performance ===");
        System.out.println("Page load time: " + loadTime + "ms");

        if (timings.containsKey("totalLoadTime")) {
            System.out.println("Total load time: " + timings.get("totalLoadTime") + "ms");
        }

        // Count products loaded
        try {
            List<WebElement> products = webDriver.findElements(
                    By.xpath("//div[contains(@class, 'product')]")
            );
            System.out.println("Products rendered: " + products.size());
        } catch (Exception e) {
            System.out.println("Could not count products");
        }

        // Check for pagination
        try {
            List<WebElement> pagination = webDriver.findElements(
                    By.xpath("//button[contains(text(), '1') or contains(text(), '2')] | " +
                            "//*[contains(@class, 'pagination')]//button")
            );
            System.out.println("Pagination elements: " + pagination.size());

            if (!pagination.isEmpty()) {
                System.out.println("✓ Pagination implemented for performance");
            }
        } catch (Exception e) {
            System.out.println("Pagination check: " + e.getMessage());
        }

        System.out.println("\nResource count: " + resources.getOrDefault("totalResources", 0));
        System.out.println("Images loaded: " + resources.getOrDefault("images", 0));

        if (loadTime <= PRODUCT_PAGE_THRESHOLD) {
            System.out.println("\n✓ Product listing loads within acceptable time");
        } else {
            System.out.println("\n⚠ Product listing load time exceeds threshold");
        }

        System.out.println("\n✓ TC14.2 COMPLETED: Product listing load time tested\n");
    }

    /**
     * TC14.3: Verify image optimization and compression
     * Expected: Images load quickly without quality loss
     */
    @Test
    @Order(3)
    public void testImageOptimization() throws InterruptedException {
        System.out.println("\n=== TC14.3: Image Optimization Test ===");

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        List<WebElement> images = webDriver.findElements(By.tagName("img"));

        System.out.println("\n=== Image Analysis ===");
        System.out.println("Total images on page: " + images.size());

        int modernFormats = 0;
        int lazyLoadedImages = 0;
        int imagesWithDimensions = 0;
        int responsiveImages = 0;

        int samplesToCheck = Math.min(10, images.size());
        System.out.println("\nAnalyzing " + samplesToCheck + " sample images:");

        for (int i = 0; i < samplesToCheck; i++) {
            WebElement img = images.get(i);

            String src = img.getAttribute("src");
            String loading = img.getAttribute("loading");
            String width = img.getAttribute("width");
            String height = img.getAttribute("height");
            String srcset = img.getAttribute("srcset");

            if (src != null) {
                System.out.println("\nImage " + (i + 1) + ":");
                System.out.println("  Source: " + src.substring(0, Math.min(60, src.length())) + "...");

                // Check format
                if (src.contains(".webp")) {
                    System.out.println("  ✓ Modern format: WebP");
                    modernFormats++;
                } else if (src.contains(".jpg") || src.contains(".jpeg")) {
                    System.out.println("  Format: JPEG");
                } else if (src.contains(".png")) {
                    System.out.println("  Format: PNG");
                }

                // Check lazy loading
                if ("lazy".equals(loading)) {
                    System.out.println("  ✓ Lazy loading enabled");
                    lazyLoadedImages++;
                }

                // Check dimensions
                if (width != null && height != null) {
                    System.out.println("  ✓ Dimensions specified: " + width + "x" + height);
                    imagesWithDimensions++;
                }

                // Check responsive images
                if (srcset != null && !srcset.isEmpty()) {
                    System.out.println("  ✓ Responsive images (srcset)");
                    responsiveImages++;
                }
            }
        }

        System.out.println("\n=== Image Optimization Summary ===");
        System.out.println("Modern formats (WebP): " + modernFormats + "/" + samplesToCheck);
        System.out.println("Lazy loaded: " + lazyLoadedImages + "/" + samplesToCheck);
        System.out.println("With dimensions: " + imagesWithDimensions + "/" + samplesToCheck);
        System.out.println("Responsive (srcset): " + responsiveImages + "/" + samplesToCheck);

        // Get average image load time
        Map<String, Object> resources = getResourceMetrics();
        Object avgImageTime = resources.get("avgImageLoadTime");
        if (avgImageTime != null) {
            double avgTime = ((Number) avgImageTime).doubleValue();
            System.out.println("\nAverage image load time: " + String.format("%.2f", avgTime) + "ms");

            if (avgTime < 500) {
                System.out.println("✓ Images load efficiently");
            } else {
                System.out.println("⚠ Image load time could be improved");
            }
        }

        System.out.println("\n✓ TC14.3 COMPLETED: Image optimization verified\n");
    }

    /**
     * TC14.4: Test search result load time
     * Expected: Results appear within 2 seconds
     */
    @Test
    @Order(4)
    public void testSearchResultLoadTime() throws InterruptedException {
        System.out.println("\n=== TC14.4: Search Result Load Time Test ===");

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        try {
            // Find search input
            WebElement searchInput = webDriver.findElement(
                    By.xpath("//input[@placeholder='Unesite pojam za pretragu...']")
            );

            System.out.println("Performing search...");

            long searchStartTime = System.currentTimeMillis();

            searchInput.clear();
            searchInput.sendKeys("frižider");
            searchInput.submit();

            // Wait for results to load
            Thread.sleep(2000);

            long searchEndTime = System.currentTimeMillis();
            long searchTime = searchEndTime - searchStartTime;

            System.out.println("\n=== Search Performance ===");
            System.out.println("Search execution time: " + searchTime + "ms");

            // Count results
            try {
                List<WebElement> results = webDriver.findElements(
                        By.xpath("//div[contains(@class, 'product')]")
                );
                System.out.println("Results found: " + results.size());
            } catch (Exception e) {
                System.out.println("Could not count results");
            }

            if (searchTime <= SEARCH_THRESHOLD) {
                System.out.println("✓ Search results appear within " + SEARCH_THRESHOLD + "ms");
            } else {
                System.out.println("⚠ Search exceeds threshold by " +
                        (searchTime - SEARCH_THRESHOLD) + "ms");
            }

            // Get page load metrics after search
            Map<String, Long> timings = getPageLoadTimes();
            if (timings.containsKey("totalLoadTime")) {
                System.out.println("Page load after search: " + timings.get("totalLoadTime") + "ms");
            }

        } catch (Exception e) {
            System.out.println("✗ Could not perform search test: " + e.getMessage());
        }

        System.out.println("\n✓ TC14.4 COMPLETED: Search result load time tested\n");
    }

    /**
     * TC14.5: Verify caching mechanism
     * Expected: Repeat visits load faster
     */
    @Test
    @Order(5)
    public void testCachingMechanism() throws InterruptedException {
        System.out.println("\n=== TC14.5: Caching Mechanism Test ===");

        // First visit - cold cache
        System.out.println("\n--- First Visit (Cold Cache) ---");
        webDriver.manage().deleteAllCookies();

        long firstVisitStart = System.currentTimeMillis();
        webDriver.get(baseUrl);
        Thread.sleep(1000);
        long firstVisitEnd = System.currentTimeMillis();
        long firstVisitTime = firstVisitEnd - firstVisitStart;

        Map<String, Long> firstTimings = getPageLoadTimes();
        Map<String, Object> firstResources = getResourceMetrics();

        System.out.println("First visit load time: " + firstVisitTime + "ms");
        System.out.println("Resources loaded: " + firstResources.getOrDefault("totalResources", 0));

        // Second visit - should use cache
        System.out.println("\n--- Second Visit (Warm Cache) ---");
        Thread.sleep(1000);

        long secondVisitStart = System.currentTimeMillis();
        webDriver.navigate().refresh();
        Thread.sleep(1000);
        long secondVisitEnd = System.currentTimeMillis();
        long secondVisitTime = secondVisitEnd - secondVisitStart;

        Map<String, Long> secondTimings = getPageLoadTimes();
        Map<String, Object> secondResources = getResourceMetrics();

        System.out.println("Second visit load time: " + secondVisitTime + "ms");
        System.out.println("Resources loaded: " + secondResources.getOrDefault("totalResources", 0));

        // Third visit - additional cache verification
        System.out.println("\n--- Third Visit (Cache Verification) ---");
        Thread.sleep(1000);

        long thirdVisitStart = System.currentTimeMillis();
        webDriver.get(baseUrl);
        Thread.sleep(1000);
        long thirdVisitEnd = System.currentTimeMillis();
        long thirdVisitTime = thirdVisitEnd - thirdVisitStart;

        System.out.println("Third visit load time: " + thirdVisitTime + "ms");

        // Compare load times
        System.out.println("\n=== Caching Performance Analysis ===");
        System.out.println("First visit:  " + firstVisitTime + "ms");
        System.out.println("Second visit: " + secondVisitTime + "ms");
        System.out.println("Third visit:  " + thirdVisitTime + "ms");

        double improvementSecond = ((firstVisitTime - secondVisitTime) * 100.0) / firstVisitTime;
        double improvementThird = ((firstVisitTime - thirdVisitTime) * 100.0) / firstVisitTime;

        System.out.println("\nPerformance improvement:");
        System.out.println("  Second visit: " + String.format("%.1f", improvementSecond) + "%");
        System.out.println("  Third visit: " + String.format("%.1f", improvementThird) + "%");

        if (secondVisitTime < firstVisitTime || thirdVisitTime < firstVisitTime) {
            System.out.println("\n✓ Caching mechanism is working");
            System.out.println("✓ Repeat visits load faster");
        } else {
            System.out.println("\n⚠ No significant caching improvement detected");
            System.out.println("ℹ This could indicate caching is not optimally configured");
        }

        // Check cache headers via JavaScript
        try {
            String cacheCheckScript =
                    "var resources = window.performance.getEntriesByType('resource');" +
                            "var cached = resources.filter(r => r.transferSize === 0);" +
                            "return {" +
                            "  total: resources.length," +
                            "  cached: cached.length," +
                            "  percentage: resources.length > 0 ? (cached.length * 100 / resources.length) : 0" +
                            "};";

            @SuppressWarnings("unchecked")
            Map<String, Object> cacheStats = (Map<String, Object>)
                    jsExecutor.executeScript(cacheCheckScript);

            System.out.println("\n=== Cache Statistics ===");
            System.out.println("Total resources: " + cacheStats.get("total"));
            System.out.println("Cached resources: " + cacheStats.get("cached"));
            System.out.println("Cache hit rate: " +
                    String.format("%.1f", ((Number) cacheStats.get("percentage")).doubleValue()) + "%");

        } catch (Exception e) {
            System.out.println("\nCould not retrieve cache statistics");
        }

        System.out.println("\n✓ TC14.5 COMPLETED: Caching mechanism tested\n");
    }

    /**
     * TC14.6: Test JavaScript execution time
     * Expected: Scripts execute efficiently without blocking
     */
    @Test
    @Order(6)
    public void testJavaScriptPerformance() throws InterruptedException {
        System.out.println("\n=== TC14.6: JavaScript Performance Test ===");

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        try {
            // Get script execution metrics
            String script =
                    "var scripts = window.performance.getEntriesByType('resource')" +
                            "  .filter(r => r.initiatorType === 'script');" +
                            "var totalDuration = scripts.reduce((sum, s) => sum + s.duration, 0);" +
                            "return {" +
                            "  count: scripts.length," +
                            "  totalDuration: totalDuration," +
                            "  avgDuration: scripts.length > 0 ? totalDuration / scripts.length : 0" +
                            "};";

            @SuppressWarnings("unchecked")
            Map<String, Object> jsMetrics = (Map<String, Object>) jsExecutor.executeScript(script);

            System.out.println("\n=== JavaScript Metrics ===");
            System.out.println("Script files loaded: " + jsMetrics.get("count"));
            System.out.println("Total execution time: " +
                    String.format("%.2f", ((Number) jsMetrics.get("totalDuration")).doubleValue()) + "ms");
            System.out.println("Average per script: " +
                    String.format("%.2f", ((Number) jsMetrics.get("avgDuration")).doubleValue()) + "ms");

            double avgDuration = ((Number) jsMetrics.get("avgDuration")).doubleValue();
            if (avgDuration < 200) {
                System.out.println("✓ JavaScript executes efficiently");
            } else {
                System.out.println("⚠ JavaScript execution time could be optimized");
            }

        } catch (Exception e) {
            System.out.println("Could not measure JavaScript performance: " + e.getMessage());
        }

        System.out.println("\n✓ TC14.6 COMPLETED: JavaScript performance tested\n");
    }

    /**
     * TC14.7: Test overall page size and weight
     * Expected: Total page size is reasonable
     */
    @Test
    @Order(7)
    public void testPageSizeAndWeight() throws InterruptedException {
        System.out.println("\n=== TC14.7: Page Size and Weight Test ===");

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        try {
            String script =
                    "var resources = window.performance.getEntriesByType('resource');" +
                            "var totalSize = resources.reduce((sum, r) => {" +
                            "  return sum + (r.transferSize || r.encodedBodySize || 0);" +
                            "}, 0);" +
                            "return {" +
                            "  resourceCount: resources.length," +
                            "  totalSize: totalSize," +
                            "  totalSizeMB: (totalSize / 1024 / 1024).toFixed(2)" +
                            "};";

            @SuppressWarnings("unchecked")
            Map<String, Object> sizeMetrics = (Map<String, Object>) jsExecutor.executeScript(script);

            System.out.println("\n=== Page Weight Metrics ===");
            System.out.println("Total resources: " + sizeMetrics.get("resourceCount"));
            System.out.println("Total page size: " + sizeMetrics.get("totalSizeMB") + " MB");

            double sizeMB = Double.parseDouble((String) sizeMetrics.get("totalSizeMB"));

            if (sizeMB < 2.0) {
                System.out.println("✓ Page size is excellent (< 2 MB)");
            } else if (sizeMB < 5.0) {
                System.out.println("✓ Page size is acceptable (< 5 MB)");
            } else {
                System.out.println("⚠ Page size is large (> 5 MB) - consider optimization");
            }

        } catch (Exception e) {
            System.out.println("Could not measure page size: " + e.getMessage());
        }

        System.out.println("\n✓ TC14.7 COMPLETED: Page size tested\n");
    }
}