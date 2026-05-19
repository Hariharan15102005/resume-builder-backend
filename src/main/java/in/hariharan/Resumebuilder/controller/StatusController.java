package in.hariharan.Resumebuilder.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator/health")
public class StatusController {

    private final ApplicationContext applicationContext;

    public StatusController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        String status = "UP";

        try {
            Class<?> healthEndpointClass = Class.forName("org.springframework.boot.actuate.health.HealthEndpoint");
            Object healthEndpoint = applicationContext.getBean(healthEndpointClass);
            java.lang.reflect.Method healthMethod = healthEndpointClass.getMethod("health");
            Object healthObj = healthMethod.invoke(healthEndpoint);
            if (healthObj != null) {
                try {
                    java.lang.reflect.Method getStatus = healthObj.getClass().getMethod("getStatus");
                    Object statusObj = getStatus.invoke(healthObj);
                    if (statusObj != null) status = statusObj.toString();
                } catch (Exception ignored) {
                }
            }
        } catch (ClassNotFoundException | org.springframework.beans.BeansException e) {
            // Actuator not present or bean not available; keep default
        } catch (Exception e) {
            // reflection errors; keep default
        }

        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        return ResponseEntity.ok(body);
    }
}
