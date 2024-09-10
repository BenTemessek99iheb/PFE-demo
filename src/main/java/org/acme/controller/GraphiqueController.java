package org.acme.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.GraphiqueRequest;
import org.acme.model.charts.Graphique;
import org.acme.service.GraphiqueService;

import java.util.List;

@Path("/api/graphique")
@RequestScoped
@Consumes("application/json")
@Produces("application/json")
public class GraphiqueController {

    @Inject
    GraphiqueService graphiqueService;
    //basic bar + column chart

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveGraphique(GraphiqueRequest request) {
        try {
            graphiqueService.saveGraphique(request.getGraphique(), request.getDeviceIds());
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error saving graphique: " + e.getMessage())
                    .build();
        }
    }

    //get graphique by User Id
    @GET
    @Path("/GraphiqueByClient/{userId}")
    public Response getGraphiqueByUser(@PathParam("userId") Long userId) {
        try {
            List<Graphique> graphiques = graphiqueService.getGraphiqueByClientId(userId);
            return Response.ok(graphiques).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


    //get graphiques per device
    @GET
    @Path("/GraphiquesByDevice/{deviceId}")
    public Response getGraphiquesByDevice(@PathParam("deviceId") Long deviceId) {
        try {
            List<Graphique> graphiques = graphiqueService.getGraphiquesByDevice(deviceId);
            return Response.ok(graphiques).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/GraphiquesByUser/{userId}")
    public Response getGraphiquesByUser(@PathParam("userId") Long userId) {
        try {
            List<Graphique> graphiques = graphiqueService.getGraphiquesByUserId(userId);
            return Response.ok(graphiques).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/GraphiqueTypesByDevice/{deviceId}")
    public Response getGraphiqueTypesByDevice(@PathParam("deviceId") Long deviceId) {
        try {
            return Response.ok(graphiqueService.getGraphiqueTypesByDeviceId(deviceId)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    //GraphiquesAndTypesByUserId
    @GET
    @Path("/GraphiquesAndTypesByUser/{userId}")
    public Response getGraphiquesAndTypesByUser(@PathParam("userId") Long userId) {
        try {
            return Response.ok(graphiqueService.GraphiquesAndTypesByUserId(userId)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/GraphiquesByUser2/{userId}")
    public Response getGraphiquesByUser2(@PathParam("userId") Long userId) {
        try {
            List<Graphique> graphiques = graphiqueService.getGraphiquesByUserId2(userId);
            return Response.ok(graphiques).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/delete/{graphiqueId}")
    public Response deleteGraphique(@PathParam("graphiqueId") Long graphiqueId) {
        try {
            graphiqueService.deleteGraphiqueById(graphiqueId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


}
