package com.mlops.model.validator.resource;

import com.mlops.model.validator.entity.HealthStatus;
import com.mlops.model.validator.service.ModelService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    private static final Logger LOG = Logger.getLogger(HealthResource.class);
    private static final Instant START_TIME = Instant.now();

    @Inject
    ModelService modelService;

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "sklearn-model-validator")
    String serviceName;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
    String version;

    @GET
    @Path("/live")
    public Response livenessCheck() {
        LOG.debug("Liveness check");

        boolean alive = true;
        Map<String, Object> checks = new HashMap<>();
        checks.put("service", serviceName);
        checks.put("status", alive ? "UP" : "DOWN");

        if (alive) {
            return Response.ok(checks).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(checks)
                .build();
        }
    }

    @GET
    @Path("/ready")
    public Response readinessCheck() {
        LOG.debug("Readiness check");

        boolean ready = modelService.isHealthy();
        List<String> models = modelService.listLoadedModels();

        Map<String, Object> checks = new HashMap<>();
        checks.put("service", serviceName);
        checks.put("status", ready ? "UP" : "DOWN");
        checks.put("models_loaded", models.size());
        checks.put("models", models);

        if (ready) {
            return Response.ok(checks).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(checks)
                .build();
        }
    }

    @GET
    public Response healthCheck() {
        LOG.debug("Health check");

        boolean healthy = modelService.isHealthy();
        List<String> models = modelService.listLoadedModels();

        Map<String, Object> modelCheck = new HashMap<>();
        modelCheck.put("status", healthy ? "UP" : "DOWN");
        modelCheck.put("loaded_models", models.size());

        Map<String, Object> checks = new HashMap<>();
        checks.put("models", modelCheck);
        checks.put("service", Map.of("status", "UP", "name", serviceName));

        Map<String, Object> response = new HashMap<>();
        response.put("status", healthy ? "UP" : "DOWN");
        response.put("checks", checks);

        if (healthy) {
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(response)
                .build();
        }
    }

    @GET
    @Path("/status")
    public Response detailedStatus() {
        LOG.debug("Detailed status check");

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeSeconds = runtimeMXBean.getUptime() / 1000;

        boolean healthy = modelService.isHealthy();
        Map<String, Object> serviceStats = modelService.getServiceStats();

        Map<String, Object> checks = new HashMap<>();
        checks.put("model_service", serviceStats);
        checks.put("jvm", Map.of(
            "uptime_seconds", uptimeSeconds,
            "vm_name", runtimeMXBean.getVmName(),
            "vm_version", runtimeMXBean.getVmVersion()
        ));

        HealthStatus status = healthy
            ? HealthStatus.up(serviceName, version, uptimeSeconds, checks)
            : HealthStatus.down(serviceName, version, checks);

        if (healthy) {
            return Response.ok(status).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(status)
                .build();
        }
    }
}
