package com.mlops.model.validator.resource;

import com.mlops.model.validator.entity.ModelMetadata;
import com.mlops.model.validator.entity.PredictionRequest;
import com.mlops.model.validator.entity.PredictionResponse;
import com.mlops.model.validator.entity.ValidationResult;
import com.mlops.model.validator.service.ModelService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api/v1/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

    private static final Logger LOG = Logger.getLogger(ModelResource.class);

    @Inject
    ModelService modelService;

    @GET
    public Response listModels() {
        LOG.info("Listing all loaded models");
        List<String> models = modelService.listLoadedModels();

        Map<String, Object> response = new HashMap<>();
        response.put("models", models);
        response.put("count", models.size());

        return Response.ok(response).build();
    }

    @GET
    @Path("/{modelName}")
    public Response getModelMetadata(@PathParam("modelName") String modelName) {
        LOG.info("Getting metadata for model: " + modelName);

        return modelService.getModelMetadata(modelName)
            .map(metadata -> Response.ok(metadata).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Model not found: " + modelName))
                .build());
    }

    @POST
    @Path("/{modelName}/load")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response loadModel(@PathParam("modelName") String modelName, FileUpload fileUpload) {
        LOG.info("Loading model: " + modelName);

        try {
            if (fileUpload == null || fileUpload.filePath() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "No file uploaded"))
                    .build();
            }

            ModelMetadata metadata = modelService.loadModel(modelName, fileUpload.filePath().toString());
            return Response.ok(metadata).build();

        } catch (Exception e) {
            LOG.error("Failed to load model: " + modelName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to load model: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/{modelName}/unload")
    public Response unloadModel(@PathParam("modelName") String modelName) {
        LOG.info("Unloading model: " + modelName);

        boolean success = modelService.unloadModel(modelName);
        if (success) {
            return Response.ok(Map.of("message", "Model unloaded successfully", "modelName", modelName)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Model not found: " + modelName))
                .build();
        }
    }

    @POST
    @Path("/{modelName}/predict")
    public Response predict(@PathParam("modelName") String modelName, PredictionRequest request) {
        LOG.info("Prediction request for model: " + modelName);

        if (request == null || !request.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Invalid request: data is required"))
                .build();
        }

        PredictionResponse response = modelService.predict(modelName, request);

        if (response.isSuccess()) {
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(response)
                .build();
        }
    }

    @POST
    @Path("/{modelName}/validate")
    public Response validateModel(@PathParam("modelName") String modelName) {
        LOG.info("Validating model: " + modelName);

        ValidationResult result = modelService.validateModel(modelName);

        if (result.isValid()) {
            return Response.ok(result).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(result)
                .build();
        }
    }

    @POST
    @Path("/validate-all")
    public Response validateAllModels() {
        LOG.info("Validating all models");

        ValidationResult result = modelService.validateAllModels();

        if (result.isValid()) {
            return Response.ok(result).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(result)
                .build();
        }
    }

    @GET
    @Path("/stats")
    public Response getStats() {
        LOG.info("Getting service stats");
        return Response.ok(modelService.getServiceStats()).build();
    }
}
