package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
    public Response predict(PredictionRequest request) {
        try {
            PredictionResponse response = modelService.predict(request);
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(new PredictionResponse(null, "error: " + e.getMessage(), 0))
                    .build();
        }
    }

    @GET
    @Path("/model/validate")
    public Response validateModel() {
        ModelValidationResponse validation = modelService.validateModel();
        if (validation.isValid()) {
            return Response.ok(validation).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(validation)
                    .build();
        }
    }

    @GET
    @Path("/model/info")
    public Response getModelInfo() {
        ModelValidationResponse validation = modelService.validateModel();
        return Response.ok(validation).build();
    }
}
