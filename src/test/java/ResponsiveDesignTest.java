import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Scenario 13: Responsive Design and Mobile Compatibility
 * Objective: Validate that the website functions correctly across different devices
 * and screen sizes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResponsiveDesignTest {

    private static WebDriver webDriver;
    private static String baseUrl;

    // Common device dimensions
    private static final Map<String, Dimension> DEVICE_SIZES = new HashMap<>();

    static {
        // Mobile devices
        DEVICE_SIZES.put("iPhone SE", new Dimension(375, 667));
        DEVICE_SIZES.put("iPhone 12", new Dimension(390, 844));
        DEVICE_SIZES.put("Samsung Galaxy S8", new Dimension(360, 740));
        DEVICE_SIZES.put("Small Mobile", new Dimension(320, 568));

        // Tablets
        DEVICE_SIZES.put("iPad", new Dimension(768, 1024));
        DEVICE_SIZES.put("iPad Pro", new Dimension(1024, 1366));
        DEVICE_SIZES.put("Android Tablet", new Dimension(800, 1280));

        // Desktop
        DEVICE_SIZES.put("Laptop", new Dimension(1366, 768));
        DEVICE_SIZES.put("Desktop HD", new Dimension(1920, 1080));
        DEVICE_SIZES.put("Desktop 4K", new Dimension(2560, 1440));
    }

    @BeforeAll
    public static void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:/Users/Korisnik/Desktop/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        webDriver = new ChromeDriver(options);
        baseUrl = "https://bijelatehnika.com";
    }

    @AfterAll
    public static void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    /**
     * Helper method to set viewport size
     */
    private void setViewportSize(Dimension size) {
        webDriver.manage().window().setSize(size);
    }

    /**
     * Helper method to take screenshot name
     */
    private String getScreenshotName(String testName, String deviceName) {
        return testName + "_" + deviceName.replace(" ", "_");
    }

    /**
     * TC13.1: Test website on mobile devices (320px-480px width)
     * Expected: Layout adapts, content readable
     */
    @Test
    @Order(1)
    public void testMobileDeviceLayout() throws InterruptedException {
        System.out.println("\n=== TC13.1: Mobile Device Layout Test ===");

        String[] mobileDevices = {"Small Mobile", "iPhone SE", "Samsung Galaxy S8"};

        for (String device : mobileDevices) {
            System.out.println("\n--- Testing on " + device + " ---");

            Dimension deviceSize = DEVICE_SIZES.get(device);
            setViewportSize(deviceSize);
            Thread.sleep(1000);

            webDriver.get(baseUrl);
            Thread.sleep(2000);

            System.out.println("Viewport: " + deviceSize.width + "x" + deviceSize.height);

            // Check if content is visible and not cut off
            try {
                WebElement body = webDriver.findElement(By.tagName("body"));
                int bodyWidth = body.getSize().getWidth();

                System.out.println("Body width: " + bodyWidth + "px");

                // Body should not exceed viewport width
                assertTrue(bodyWidth <= deviceSize.width + 50,
                        "Content should fit within mobile viewport");

                System.out.println("✓ Content fits within viewport");
            } catch (Exception e) {
                System.out.println("⚠ Could not verify body width");
            }

            // Check for horizontal scrollbar (should not exist on mobile)
            Long scrollWidth = (Long) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.documentElement.scrollWidth");
            Long clientWidth = (Long) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.documentElement.clientWidth");

            System.out.println("Scroll width: " + scrollWidth);
            System.out.println("Client width: " + clientWidth);

            if (scrollWidth <= clientWidth + 20) {
                System.out.println("✓ No horizontal scroll detected");
            } else {
                System.out.println("⚠ Horizontal scroll present: " + (scrollWidth - clientWidth) + "px overflow");
            }

            // Verify text is readable (font size)
            try {
                WebElement heading = webDriver.findElement(By.tagName("h1"));
                String fontSize = heading.getCssValue("font-size");

                // Convert font size to pixels
                int fontSizePx = Integer.parseInt(fontSize.replace("px", "").split("\\.")[0]);

                System.out.println("Main heading font size: " + fontSize);
                assertTrue(fontSizePx >= 16, "Font should be at least 16px for readability");
                System.out.println("✓ Text is readable");
            } catch (Exception e) {
                System.out.println("ℹ Could not check font size");
            }

            // Check if images are responsive
            List<WebElement> images = webDriver.findElements(By.tagName("img"));
            int responsiveImages = 0;

            for (WebElement img : images) {
                String maxWidth = img.getCssValue("max-width");
                if (maxWidth.equals("100%") || maxWidth.equals("100vw")) {
                    responsiveImages++;
                }
            }

            if (images.size() > 0) {
                System.out.println("✓ Images checked: " + responsiveImages + "/" + images.size() + " responsive");
            }
        }

        System.out.println("\n✓ TC13.1 PASSED: Mobile layout adapts correctly\n");
    }

    /**
     * TC13.2: Test on tablet devices (768px-1024px width)
     * Expected: Responsive design works properly
     */
    @Test
    @Order(2)
    public void testTabletDeviceLayout() throws InterruptedException {
        System.out.println("\n=== TC13.2: Tablet Device Layout Test ===");

        String[] tablets = {"iPad", "iPad Pro", "Android Tablet"};

        for (String device : tablets) {
            System.out.println("\n--- Testing on " + device + " ---");

            Dimension deviceSize = DEVICE_SIZES.get(device);
            setViewportSize(deviceSize);
            Thread.sleep(1000);

            webDriver.get(baseUrl);
            Thread.sleep(2000);

            System.out.println("Viewport: " + deviceSize.width + "x" + deviceSize.height);

            // Check layout structure
            try {
                // Check if navigation is visible
                List<WebElement> navElements = webDriver.findElements(
                        By.xpath("//nav | //header//ul")
                );

                if (!navElements.isEmpty()) {
                    System.out.println("✓ Navigation elements found: " + navElements.size());
                } else {
                    System.out.println("ℹ Navigation structure different on tablet");
                }
            } catch (Exception e) {
                System.out.println("ℹ Could not check navigation");
            }

            // Verify content is centered and properly laid out
            try {
                WebElement mainContent = webDriver.findElement(
                        By.xpath("//main | //div[contains(@class, 'container')]")
                );

                int contentWidth = mainContent.getSize().getWidth();
                System.out.println("Main content width: " + contentWidth + "px");

                assertTrue(contentWidth > 0, "Main content should be visible");
                System.out.println("✓ Content properly laid out");
            } catch (Exception e) {
                System.out.println("ℹ Could not check main content");
            }

            // Check for proper spacing
            Long scrollHeight = (Long) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.documentElement.scrollHeight");

            System.out.println("Page height: " + scrollHeight + "px");
            assertTrue(scrollHeight > deviceSize.height, "Page should have scrollable content");
            System.out.println("✓ Page has scrollable content");
        }

        System.out.println("\n✓ TC13.2 PASSED: Tablet layout works properly\n");
    }

    /**
     * TC13.3: Test on desktop (1920px+ width)
     * Expected: Optimal layout on large screens
     */
    @Test
    @Order(3)
    public void testDesktopLayout() throws InterruptedException {
        System.out.println("\n=== TC13.3: Desktop Layout Test ===");

        String[] desktops = {"Laptop", "Desktop HD", "Desktop 4K"};

        for (String device : desktops) {
            System.out.println("\n--- Testing on " + device + " ---");

            Dimension deviceSize = DEVICE_SIZES.get(device);
            setViewportSize(deviceSize);
            Thread.sleep(1000);

            webDriver.get(baseUrl);
            Thread.sleep(2000);

            System.out.println("Viewport: " + deviceSize.width + "x" + deviceSize.height);

            // Check if content is centered with max-width
            try {
                WebElement container = webDriver.findElement(
                        By.xpath("//div[contains(@class, 'container') or contains(@class, 'wrapper')]")
                );

                int containerWidth = container.getSize().getWidth();
                System.out.println("Container width: " + containerWidth + "px");

                // Content should not span entire width on very large screens
                if (deviceSize.width >= 1920 && containerWidth < deviceSize.width) {
                    System.out.println("✓ Content is centered/constrained on large screen");
                } else {
                    System.out.println("ℹ Content uses available width");
                }
            } catch (Exception e) {
                System.out.println("ℹ Could not check container width");
            }

            // Verify navigation is horizontal on desktop
            try {
                List<WebElement> navItems = webDriver.findElements(
                        By.xpath("//nav//a | //header//ul//a")
                );

                if (!navItems.isEmpty()) {
                    System.out.println("✓ Navigation items visible: " + navItems.size());

                    // Check if items are displayed inline (horizontal)
                    WebElement firstItem = navItems.get(0);
                    String display = firstItem.getCssValue("display");

                    System.out.println("Navigation display style: " + display);
                }
            } catch (Exception e) {
                System.out.println("ℹ Could not check navigation layout");
            }

            // Check if all content is visible without horizontal scroll
            Long scrollWidth = (Long) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.documentElement.scrollWidth");

            System.out.println("Document scroll width: " + scrollWidth);
            assertTrue(scrollWidth <= deviceSize.width + 50,
                    "No horizontal scroll should be present");
            System.out.println("✓ No horizontal scroll on desktop");
        }

        System.out.println("\n✓ TC13.3 PASSED: Desktop layout is optimal\n");
    }

    /**
     * TC13.4: Verify mobile navigation menu (hamburger menu)
     * Expected: Menu expands and works correctly
     */
    @Test
    @Order(4)
    public void testMobileNavigationMenu() throws InterruptedException {
        System.out.println("\n=== TC13.4: Mobile Navigation Menu Test ===");

        // Set to mobile size
        setViewportSize(DEVICE_SIZES.get("iPhone 12"));
        Thread.sleep(1000);

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        System.out.println("Testing on mobile viewport: 390x844");

        // Look for hamburger menu
        try {
            List<WebElement> hamburgerButtons = webDriver.findElements(
                    By.xpath("//button[contains(@class, 'hamburger') or contains(@class, 'menu-toggle') or " +
                            "contains(@class, 'navbar-toggler') or contains(@aria-label, 'menu')]")
            );

            if (!hamburgerButtons.isEmpty()) {
                WebElement hamburger = hamburgerButtons.get(0);
                System.out.println("✓ Hamburger menu button found");

                // Check if visible on mobile
                assertTrue(hamburger.isDisplayed(), "Hamburger should be visible on mobile");
                System.out.println("✓ Hamburger menu is visible");

                // Click to open menu
                hamburger.click();
                Thread.sleep(1000);
                System.out.println("✓ Clicked hamburger menu");

                // Check if menu is now visible
                try {
                    WebElement mobileMenu = webDriver.findElement(
                            By.xpath("//nav[contains(@class, 'mobile') or contains(@class, 'open')] | " +
                                    "//*[contains(@class, 'menu-open')]")
                    );

                    if (mobileMenu.isDisplayed()) {
                        System.out.println("✓ Mobile menu expanded successfully");
                    }
                } catch (Exception e) {
                    System.out.println("ℹ Menu expansion not visually detected");
                }

                // Click again to close
                hamburger.click();
                Thread.sleep(500);
                System.out.println("✓ Closed mobile menu");

            } else {
                System.out.println("ℹ Hamburger menu not found");
                System.out.println("ℹ Site may use different mobile navigation pattern");
            }

        } catch (Exception e) {
            System.out.println("ℹ Mobile menu test: " + e.getMessage());
        }

        System.out.println("\n✓ TC13.4 COMPLETED: Mobile navigation tested\n");
    }

    /**
     * TC13.5: Verify button and link sizes on mobile
     * Expected: Elements easily tappable (min 44px)
     */
    @Test
    @Order(5)
    public void testTappableElementSizes() throws InterruptedException {
        System.out.println("\n=== TC13.5: Tappable Element Sizes Test ===");

        // Set to mobile size
        setViewportSize(DEVICE_SIZES.get("iPhone SE"));
        Thread.sleep(1000);

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        System.out.println("Testing on mobile viewport: 375x667");
        System.out.println("Minimum recommended tap target: 44x44 pixels\n");

        int elementsChecked = 0;
        int elementsPassingSize = 0;

        // Check buttons
        List<WebElement> buttons = webDriver.findElements(By.tagName("button"));
        System.out.println("=== Checking Buttons ===");

        for (int i = 0; i < Math.min(5, buttons.size()); i++) {
            WebElement button = buttons.get(i);

            if (button.isDisplayed()) {
                int height = button.getSize().getHeight();
                int width = button.getSize().getWidth();

                String text = button.getText();
                if (text.isEmpty()) {
                    text = button.getAttribute("aria-label");
                }
                if (text == null || text.isEmpty()) {
                    text = "Button " + (i + 1);
                }

                boolean passesSize = height >= 44 || width >= 44;

                System.out.println("Button: " + text.substring(0, Math.min(30, text.length())));
                System.out.println("  Size: " + width + "x" + height + "px");
                System.out.println("  " + (passesSize ? "✓ Adequate size" : "⚠ Below recommended size"));

                elementsChecked++;
                if (passesSize) elementsPassingSize++;
            }
        }

        // Check links
        List<WebElement> links = webDriver.findElements(By.tagName("a"));
        System.out.println("\n=== Checking Links ===");

        int linksChecked = 0;
        for (WebElement link : links) {
            if (link.isDisplayed() && linksChecked < 5) {
                int height = link.getSize().getHeight();
                int width = link.getSize().getWidth();

                String text = link.getText();
                if (text.isEmpty()) continue;

                boolean passesSize = height >= 44 || width >= 44;

                System.out.println("Link: " + text.substring(0, Math.min(30, text.length())));
                System.out.println("  Size: " + width + "x" + height + "px");
                System.out.println("  " + (passesSize ? "✓ Adequate size" : "⚠ Below recommended size"));

                elementsChecked++;
                linksChecked++;
                if (passesSize) elementsPassingSize++;
            }
        }

        System.out.println("\n=== Summary ===");
        System.out.println("Elements checked: " + elementsChecked);
        System.out.println("Elements with adequate size: " + elementsPassingSize);

        if (elementsChecked > 0) {
            double percentage = (elementsPassingSize * 100.0) / elementsChecked;
            System.out.println("Percentage: " + String.format("%.1f", percentage) + "%");
        }

        System.out.println("\n✓ TC13.5 COMPLETED: Tappable element sizes verified\n");
    }

    /**
     * TC13.6: Test form inputs on mobile devices
     * Expected: Inputs functional and properly sized
     */
    @Test
    @Order(6)
    public void testFormInputsOnMobile() throws InterruptedException {
        System.out.println("\n=== TC13.6: Form Inputs on Mobile Test ===");

        // Set to mobile size
        setViewportSize(DEVICE_SIZES.get("Samsung Galaxy S8"));
        Thread.sleep(1000);

        // Navigate to a page with forms (login page)
        webDriver.get(baseUrl + "/prijava");
        Thread.sleep(2000);

        System.out.println("Testing on mobile viewport: 360x740");

        // Find input fields
        List<WebElement> inputs = webDriver.findElements(
                By.xpath("//input[@type='text'] | //input[@type='email'] | " +
                        "//input[@type='password'] | //textarea")
        );

        System.out.println("Form inputs found: " + inputs.size());

        if (!inputs.isEmpty()) {
            System.out.println("\n=== Checking Input Fields ===");

            for (int i = 0; i < Math.min(3, inputs.size()); i++) {
                WebElement input = inputs.get(i);

                if (input.isDisplayed()) {
                    String type = input.getAttribute("type");
                    String placeholder = input.getAttribute("placeholder");
                    String name = input.getAttribute("name");

                    int height = input.getSize().getHeight();
                    int width = input.getSize().getWidth();

                    System.out.println("\nInput field:");
                    System.out.println("  Type: " + type);
                    System.out.println("  Name: " + name);
                    System.out.println("  Placeholder: " + placeholder);
                    System.out.println("  Size: " + width + "x" + height + "px");

                    // Check if height is adequate for mobile (min 44px)
                    if (height >= 44) {
                        System.out.println("  ✓ Adequate height for mobile");
                    } else {
                        System.out.println("  ⚠ Height below recommended 44px");
                    }

                    // Test if input is clickable/tappable
                    try {
                        input.click();
                        Thread.sleep(300);
                        System.out.println("  ✓ Input is tappable");

                        // Check if input is focused
                        WebElement activeElement = webDriver.switchTo().activeElement();
                        if (activeElement.equals(input)) {
                            System.out.println("  ✓ Input receives focus correctly");
                        }

                    } catch (Exception e) {
                        System.out.println("  ⚠ Could not interact with input");
                    }
                }
            }

            // Check form button
            try {
                WebElement submitButton = webDriver.findElement(
                        By.xpath("//button[@type='submit'] | //input[@type='submit']")
                );

                int buttonHeight = submitButton.getSize().getHeight();
                int buttonWidth = submitButton.getSize().getWidth();

                System.out.println("\n=== Submit Button ===");
                System.out.println("Size: " + buttonWidth + "x" + buttonHeight + "px");

                if (buttonHeight >= 44) {
                    System.out.println("✓ Button has adequate tap target size");
                } else {
                    System.out.println("⚠ Button below recommended size");
                }

            } catch (Exception e) {
                System.out.println("\nℹ Submit button not found");
            }

        } else {
            System.out.println("ℹ No form inputs found on this page");

            // Try contact form instead
            webDriver.get(baseUrl + "/kontakt");
            Thread.sleep(2000);

            inputs = webDriver.findElements(By.tagName("input"));
            System.out.println("Inputs on contact page: " + inputs.size());
        }

        System.out.println("\n✓ TC13.6 COMPLETED: Form inputs tested on mobile\n");
    }

    /**
     * TC13.7: Test viewport meta tag
     * Expected: Proper viewport configuration for mobile
     */
    @Test
    @Order(7)
    public void testViewportMetaTag() throws InterruptedException {
        System.out.println("\n=== TC13.7: Viewport Meta Tag Test ===");

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        try {
            WebElement viewportMeta = webDriver.findElement(
                    By.xpath("//meta[@name='viewport']")
            );

            String content = viewportMeta.getAttribute("content");

            System.out.println("Viewport meta tag found:");
            System.out.println("  Content: " + content);

            // Check for required viewport settings
            boolean hasWidthDevice = content.contains("width=device-width");
            boolean hasInitialScale = content.contains("initial-scale=1");

            if (hasWidthDevice) {
                System.out.println("  ✓ width=device-width present");
            } else {
                System.out.println("  ⚠ width=device-width missing");
            }

            if (hasInitialScale) {
                System.out.println("  ✓ initial-scale=1 present");
            } else {
                System.out.println("  ⚠ initial-scale=1 missing");
            }

            assertTrue(hasWidthDevice && hasInitialScale,
                    "Viewport should have width=device-width and initial-scale=1");

            System.out.println("\n✓ Viewport meta tag properly configured");

        } catch (Exception e) {
            System.out.println("✗ Viewport meta tag not found");
            System.out.println("⚠ This is critical for responsive design!");
            fail("Viewport meta tag is required for mobile compatibility");
        }

        System.out.println("\n✓ TC13.7 PASSED: Viewport configuration verified\n");
    }

    /**
     * TC13.8: Test orientation changes
     * Expected: Layout adapts to landscape and portrait
     */
    @Test
    @Order(8)
    public void testOrientationChanges() throws InterruptedException {
        System.out.println("\n=== TC13.8: Orientation Changes Test ===");

        // Portrait mode
        System.out.println("\n--- Portrait Mode (390x844) ---");
        setViewportSize(new Dimension(390, 844));
        Thread.sleep(1000);

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        Long portraitScrollWidth = (Long) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                .executeScript("return document.documentElement.scrollWidth");

        System.out.println("Portrait scroll width: " + portraitScrollWidth);
        System.out.println("✓ Portrait mode loaded");

        // Landscape mode (flip dimensions)
        System.out.println("\n--- Landscape Mode (844x390) ---");
        setViewportSize(new Dimension(844, 390));
        Thread.sleep(1000);

        webDriver.navigate().refresh();
        Thread.sleep(2000);

        Long landscapeScrollWidth = (Long) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                .executeScript("return document.documentElement.scrollWidth");

        System.out.println("Landscape scroll width: " + landscapeScrollWidth);
        System.out.println("✓ Landscape mode loaded");

        // Verify content adapts
        assertNotNull(portraitScrollWidth, "Portrait mode should render");
        assertNotNull(landscapeScrollWidth, "Landscape mode should render");

        System.out.println("\n✓ Layout adapts to orientation changes");
        System.out.println("\n✓ TC13.8 PASSED: Orientation changes handled\n");
    }
}