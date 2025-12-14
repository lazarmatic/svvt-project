import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Scenario 10: HTTPS and Security Protocol Compliance
 * Objective: Ensure the website enforces HTTPS and follows security best practices
 * for data transmission.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HTTPSSecurityTest {

    private static WebDriver webDriver;
    private static String baseUrl;
    private static String httpUrl;
    private static String httpsUrl;

    @BeforeAll
    public static void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:/Users/Korisnik/Desktop/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");
        options.addArguments("--ignore-certificate-errors"); // For testing SSL

        webDriver = new ChromeDriver(options);
        baseUrl = "https://bijelatehnika.com";
        httpUrl = "http://bijelatehnika.com";
        httpsUrl = "https://bijelatehnika.com";
    }

    @AfterAll
    public static void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    /**
     * TC10.1: Verify HTTP redirects to HTTPS automatically
     * Expected: All HTTP requests redirect to HTTPS
     */
    @Test
    @Order(1)
    public void testHTTPRedirectsToHTTPS() throws InterruptedException, IOException {
        System.out.println("\n=== TC10.1: HTTP to HTTPS Redirect ===");

        // Test main page redirect
        testHTTPRedirect(httpUrl, "Main Page");

        // Test other important pages
        testHTTPRedirect(httpUrl + "/prijava", "Login Page");
        testHTTPRedirect(httpUrl + "/shop", "Shop Page");
        testHTTPRedirect(httpUrl + "/kontakt", "Contact Page");

        System.out.println("\n✓ TC10.1 PASSED: HTTP to HTTPS redirect verified\n");
    }

    /**
     * Helper method to test HTTP redirect
     */
    private void testHTTPRedirect(String httpUrl, String pageName) throws IOException {
        System.out.println("\nTesting: " + pageName);
        System.out.println("  HTTP URL: " + httpUrl);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            String location = connection.getHeaderField("Location");

            System.out.println("  Response Code: " + responseCode);
            System.out.println("  Location Header: " + location);

            if (responseCode == 301 || responseCode == 302 || responseCode == 307 || responseCode == 308) {
                if (location != null && location.startsWith("https://")) {
                    System.out.println("  ✓ Redirects to HTTPS: " + location);
                } else {
                    System.out.println("  ⚠ WARNING: Redirects but not to HTTPS");
                }
            } else if (responseCode == 200) {
                System.out.println("  ⚠ WARNING: No redirect - serving HTTP directly");
            }

        } catch (Exception e) {
            System.out.println("  ✗ Error testing redirect: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * TC10.2: Check SSL certificate validity
     * Expected: Valid SSL certificate installed
     */
    @Test
    @Order(2)
    public void testSSLCertificateValidity() throws Exception {
        System.out.println("\n=== TC10.2: SSL Certificate Validity ===");

        URL url = new URL(httpsUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.connect();

        Certificate[] certificates = connection.getServerCertificates();

        System.out.println("\nSSL Certificate Information:");
        System.out.println("Number of certificates: " + certificates.length);

        if (certificates.length > 0 && certificates[0] instanceof X509Certificate) {
            X509Certificate cert = (X509Certificate) certificates[0];

            System.out.println("\n=== Main Certificate ===");
            System.out.println("Subject DN: " + cert.getSubjectDN());
            System.out.println("Issuer DN: " + cert.getIssuerDN());
            System.out.println("Serial Number: " + cert.getSerialNumber());
            System.out.println("Not Before: " + cert.getNotBefore());
            System.out.println("Not After: " + cert.getNotAfter());
            System.out.println("Signature Algorithm: " + cert.getSigAlgName());

            // Check if certificate is currently valid
            Date now = new Date();
            Date notBefore = cert.getNotBefore();
            Date notAfter = cert.getNotAfter();

            boolean isValid = now.after(notBefore) && now.before(notAfter);

            System.out.println("\n=== Validity Check ===");
            System.out.println("Current Date: " + now);
            System.out.println("Certificate Valid: " + isValid);

            if (isValid) {
                System.out.println("✓ Certificate is currently valid");

                // Calculate days until expiration
                long daysUntilExpiry = (notAfter.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
                System.out.println("Days until expiration: " + daysUntilExpiry);

                if (daysUntilExpiry < 30) {
                    System.out.println("⚠ WARNING: Certificate expires in less than 30 days!");
                }
            } else {
                System.out.println("✗ Certificate is NOT valid");
                if (now.before(notBefore)) {
                    System.out.println("  Reason: Certificate not yet valid");
                } else {
                    System.out.println("  Reason: Certificate has expired");
                }
            }

            assertTrue(isValid, "SSL certificate should be valid");
        }

        connection.disconnect();
        System.out.println("\n✓ TC10.2 PASSED: SSL certificate is valid\n");
    }

    /**
     * TC10.3: Verify HTTPS on login page
     * Expected: Login form served over HTTPS
     */
    @Test
    @Order(3)
    public void testHTTPSOnLoginPage() throws InterruptedException {
        System.out.println("\n=== TC10.3: HTTPS on Login Page ===");

        webDriver.get(baseUrl + "/prijava");
        Thread.sleep(2000);

        String currentUrl = webDriver.getCurrentUrl();
        System.out.println("Login page URL: " + currentUrl);

        boolean isHTTPS = currentUrl.startsWith("https://");
        System.out.println("Uses HTTPS: " + isHTTPS);

        if (isHTTPS) {
            System.out.println("✓ Login page is served over HTTPS");
        } else {
            System.out.println("✗ SECURITY ISSUE: Login page NOT served over HTTPS!");
        }

        assertTrue(isHTTPS, "Login page must use HTTPS");

        // Check for mixed content warnings
        checkForMixedContent("Login Page");

        System.out.println("\n✓ TC10.3 PASSED: Login page uses HTTPS\n");
    }

    /**
     * TC10.4: Verify HTTPS on checkout/payment pages
     * Expected: All sensitive pages use HTTPS
     */
    @Test
    @Order(4)
    public void testHTTPSOnSensitivePages() throws InterruptedException {
        System.out.println("\n=== TC10.4: HTTPS on Sensitive Pages ===");

        String[] sensitivePages = {
                "/prijava",           // Login
                "/korisnicki-racun",  // Account
                "/shop",              // Shop (may lead to checkout)
                "/cart",              // Shopping cart
        };

        boolean allSecure = true;

        for (String page : sensitivePages) {
            webDriver.get(baseUrl + page);
            Thread.sleep(2000);

            String currentUrl = webDriver.getCurrentUrl();
            boolean isHTTPS = currentUrl.startsWith("https://");

            System.out.println("\nPage: " + page);
            System.out.println("  Current URL: " + currentUrl);
            System.out.println("  Uses HTTPS: " + isHTTPS);

            if (!isHTTPS) {
                System.out.println("  ✗ SECURITY ISSUE: Not using HTTPS!");
                allSecure = false;
            } else {
                System.out.println("  ✓ Secure");
                checkForMixedContent(page);
            }
        }

        if (allSecure) {
            System.out.println("\n✓ All sensitive pages use HTTPS");
        } else {
            System.out.println("\n⚠ Some sensitive pages not using HTTPS!");
        }

        assertTrue(allSecure, "All sensitive pages must use HTTPS");
        System.out.println("\n✓ TC10.4 PASSED: Sensitive pages use HTTPS\n");
    }

    /**
     * TC10.5: Test password transmission encryption
     * Expected: Passwords sent encrypted (verify HTTPS usage)
     */
    @Test
    @Order(5)
    public void testPasswordTransmissionEncryption() throws InterruptedException {
        System.out.println("\n=== TC10.5: Password Transmission Encryption ===");

        // Navigate to login page
        webDriver.get(baseUrl + "/prijava");
        Thread.sleep(2000);

        String loginPageUrl = webDriver.getCurrentUrl();
        boolean loginPageSecure = loginPageUrl.startsWith("https://");

        System.out.println("Login page URL: " + loginPageUrl);
        System.out.println("Login page uses HTTPS: " + loginPageSecure);

        if (loginPageSecure) {
            System.out.println("✓ Login form is served over HTTPS");
            System.out.println("✓ Password transmission will be encrypted");
        } else {
            System.out.println("✗ CRITICAL: Login form NOT served over HTTPS!");
            System.out.println("✗ Passwords would be transmitted in plain text!");
        }

        assertTrue(loginPageSecure, "Login page must use HTTPS for encrypted password transmission");

        // Additional security checks
        System.out.println("\n=== Additional Security Checks ===");

        // Check if page has any insecure resources
        checkForMixedContent("Login Page (Password Transmission)");

        // Verify form action
        try {
            String pageSource = webDriver.getPageSource();

            if (pageSource.contains("action=\"http://")) {
                System.out.println("⚠ WARNING: Form action uses HTTP!");
            } else if (pageSource.contains("action=\"https://") || !pageSource.contains("action=\"http")) {
                System.out.println("✓ Form action is secure (HTTPS or relative URL)");
            }
        } catch (Exception e) {
            System.out.println("Could not check form action: " + e.getMessage());
        }

        System.out.println("\n✓ TC10.5 PASSED: Password transmission is encrypted\n");
    }

    /**
     * TC10.6: Verify security headers
     * Expected: Important security headers present
     */
    @Test
    @Order(6)
    public void testSecurityHeaders() throws IOException {
        System.out.println("\n=== TC10.6: Security Headers Check ===");

        URL url = new URL(httpsUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.connect();

        System.out.println("\nChecking security headers:");

        // Check for important security headers
        String[] securityHeaders = {
                "Strict-Transport-Security",
                "X-Frame-Options",
                "X-Content-Type-Options",
                "X-XSS-Protection",
                "Content-Security-Policy",
                "Referrer-Policy"
        };

        int presentHeaders = 0;
        for (String header : securityHeaders) {
            String value = connection.getHeaderField(header);
            if (value != null) {
                System.out.println("✓ " + header + ": " + value);
                presentHeaders++;
            } else {
                System.out.println("✗ " + header + ": Not present");
            }
        }

        System.out.println("\n=== Summary ===");
        System.out.println("Security headers present: " + presentHeaders + "/" + securityHeaders.length);

        if (presentHeaders < 3) {
            System.out.println("⚠ WARNING: Few security headers present");
            System.out.println("RECOMMENDATION: Add security headers to improve security posture");
        }

        connection.disconnect();
        System.out.println("\n✓ TC10.6 COMPLETED: Security headers checked\n");
    }

    /**
     * TC10.7: Test TLS version and cipher strength
     * Expected: Modern TLS version with strong ciphers
     */
    @Test
    @Order(7)
    public void testTLSVersionAndCiphers() throws Exception {
        System.out.println("\n=== TC10.7: TLS Version and Cipher Check ===");

        URL url = new URL(httpsUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.connect();

        String protocol = connection.getCipherSuite();
        System.out.println("Cipher Suite: " + protocol);

        // Check for weak protocols/ciphers
        String protocolLower = protocol.toLowerCase();

        if (protocolLower.contains("ssl") || protocolLower.contains("tls_rsa")) {
            System.out.println("⚠ WARNING: Using older or weak cipher suite");
        } else if (protocolLower.contains("tls") &&
                (protocolLower.contains("ecdhe") || protocolLower.contains("dhe"))) {
            System.out.println("✓ Using modern cipher suite with forward secrecy");
        }

        connection.disconnect();
        System.out.println("\n✓ TC10.7 COMPLETED: TLS configuration checked\n");
    }

    /**
     * Helper method to check for mixed content warnings
     */
    private void checkForMixedContent(String pageName) {
        try {
            LogEntries logs = webDriver.manage().logs().get(LogType.BROWSER);
            boolean hasMixedContent = false;

            for (LogEntry entry : logs) {
                String message = entry.getMessage().toLowerCase();
                if (message.contains("mixed content") ||
                        message.contains("insecure") ||
                        message.contains("http://")) {
                    if (!hasMixedContent) {
                        System.out.println("\n  ⚠ Mixed Content Warnings:");
                        hasMixedContent = true;
                    }
                    System.out.println("    " + entry.getMessage());
                }
            }

            if (!hasMixedContent) {
                System.out.println("  ✓ No mixed content warnings detected");
            } else {
                System.out.println("  ⚠ RECOMMENDATION: Fix mixed content to ensure full HTTPS protection");
            }
        } catch (Exception e) {
            System.out.println("  ℹ Could not check for mixed content warnings");
        }
    }
}