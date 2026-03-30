package com.sklearn.resource;

import com.sklearn.dto.ModelValidationResponse;
import com.sklearn.dto.PredictRequest;
import com.sklearn.dto.PredictResponse;
import com.sklearn.service.ModelService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

    @Inject
    ModelService modelService;

    @POST
    @Path("/predict")
    public Response predict(PredictRequest request) {
        if (request == null || request.getFeatures() == null || request.getFeatures().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(PredictResponse.error("Invalid request: features are required"))
                    .build();
        }

        PredictResponse response = modelService.predict(request.getFeaturesAsFloatArray());
        
        if (response.isSuccess()) {
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(response)
                    .build();
        }
    }

    @GET
    @Path("/model/validate")
    public Response validateModel() {
        ModelValidationResponse response = modelService.validate();
        
        if (response.isValid()) {
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(response)
                    .build();
        }
    }

    @GET
    @Path("/model/status")
    public Response modelStatus() {
        return Response.ok()
                .entity(new ModelStatusResponse(modelService.isModelLoaded()))
                .build();
    }

    @GET
    @Path("/model/info")
    public Response modelInfo() {
        return Response.ok()
                .entity(new ModelInfoResponse(
                    "iris_classifier",
                    "RandomForestClassifier",
                    4,
                    3,
                    "Iris flower classification model"
                ))
                .build();
    }

    public static class ModelStatusResponse {
        public boolean modelLoaded;

        public ModelStatusResponse(boolean modelLoaded) {
            this.modelLoaded = modelLoaded;
        }
    }

    public static class ModelInfoResponse {
        public String name;
        public String type;
        public int inputFeatures;
        public int outputClasses;
        public String description;

        public ModelInfoResponse(String name, String type, int inputFeatures, 
                                  int outputClasses, String description) {
            this.name = name;
            this.type = type;
            this.inputFeatures = inputFeatures;
            this.outputClasses = outputClasses;
            this.description = description;
        }
    }
}
