package org.acme.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.acme.model.Client;
import org.acme.model.devices.ElectricityMeter;
import org.acme.model.devices.SolarPanel;
import org.acme.model.devices.THL;
import org.acme.model.devices.WaterMeter;
import org.acme.service.ClientService;

import java.util.logging.Logger;

@AllArgsConstructor
@Path("/api/user")
@RequestScoped
@Produces("application/json")
public class ClientController {
    private static final Logger LOGGER = Logger.getLogger(ClientController.class.getName());

    @Inject
    ClientService clientService;
    //add user controller methods here
    @POST
    @Path("/assignEM/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEmToUser(@PathParam("clientId") Long clientId,

                                ElectricityMeter device) {
        try {
            Client client = clientService.findUserById(clientId);
            if (client == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
            clientService.assignEmToUser(client, device);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/assignWM/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addWMToUser(@PathParam("clientId") Long clientId,

                                WaterMeter device) {
        try {
            Client client = clientService.findUserById(clientId);
            if (client == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
            clientService.assignWmToUser(client, device);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/assignTHL/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTHLToUser(@PathParam("clientId") Long clientId,
                                 THL device) {
        try {
            Client client = clientService.findUserById(clientId);
            if (client == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
            clientService.assignTHLToUser(client, device);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    @POST
    @Path("/assignSolarPanel/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSolarPanelToUser(@PathParam("clientId") Long clientId,
                                 SolarPanel device) {
        try {
            Client client = clientService.findUserById(clientId);
            if (client == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
            clientService.assignSolarPanelToUser(client, device);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(Client client) {
        try {
            clientService.save(client);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    //findUserById
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findUserById(@PathParam("userId") Long userId) {
        try {
            Client client = clientService.findUserById(userId);
            if (client == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
            return Response.ok(client).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    //getAllClients
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllClients() {
        try {
            return Response.ok(clientService.getAllClients()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    //getClientsWithDevices
    @GET
    @Path("/clientsWithDevices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientsWithDevices() {
        try {
            return Response.ok(clientService.getClientsWithDevices()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    //deleteClientAndDevice
    @DELETE
    @Path("/{clientId}/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteClientAndDevice(@PathParam("clientId") Long clientId,
                                          @PathParam("deviceId") Long deviceId) {
        try {
            clientService.deleteClientAndDevice(clientId, deviceId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    @DELETE
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("userId") Long userId) {
        try {
            clientService.deleteUser(userId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

}
