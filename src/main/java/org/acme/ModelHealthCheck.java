package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

@Liveness
@Readiness
@ApplicationScoped
public class ModelHealthCheck implements HealthCheck {

    @Inject
    ModelService modelService;

    @Override
    public HealthCheckResponse call() {
        ModelValidationResponse validation = modelService.validateModel();
        return HealthCheckResponse.named("model-service")
                .status(validation.isValid())
                .withData("modelType", validation.getModelType())
                .withData("modelVersion", validation.getVersion())
                .withData("message", validation.getMessage())
                .build();
    }
}
