package in.hariharan.Resumebuilder.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final ApplicationContext applicationContext;

    public HealthController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();

        String status = "UP";
        String actuatorHealth = null;

        try {
            Class<?> healthEndpointClass = Class.forName("org.springframework.boot.actuate.health.HealthEndpoint");
            Object healthEndpoint = applicationContext.getBean(healthEndpointClass);
            java.lang.reflect.Method healthMethod = healthEndpointClass.getMethod("health");
            Object healthObj = healthMethod.invoke(healthEndpoint);
            if (healthObj != null) {
                // try to get a status string via getStatus() if present
                try {
                    java.lang.reflect.Method getStatus = healthObj.getClass().getMethod("getStatus");
                    Object statusObj = getStatus.invoke(healthObj);
                    if (statusObj != null) status = statusObj.toString();
                } catch (Exception ignored) {
                }
                actuatorHealth = healthObj.toString();
            }
        } catch (ClassNotFoundException | org.springframework.beans.BeansException e) {
            // Actuator not present or bean not available; ignore
        } catch (Exception e) {
            // reflection errors; ignore and continue
        }

        body.put("status", status);
        body.put("application", "Resumebuilder");
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            body.put("ip", ip);
        } catch (Exception e) {
            body.put("ip", "unknown");
        }

        if (actuatorHealth != null) {
            body.put("actuator", actuatorHealth);
        }

        return ResponseEntity.ok(body);
    }
}
