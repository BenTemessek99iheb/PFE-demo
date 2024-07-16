package org.acme.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.acme.dto.DeviceConsumption_cout_carbone;
import org.acme.dto.GreenTotalConsumptionWmEmCo2Sp;
import org.acme.model.Alert;
import org.acme.model.MetaData.EnergyMeterMetaData;
import org.acme.model.MetaData.THLMetaData;
import org.acme.model.MetaData.WaterMeterMetaData;
import org.acme.service.MetaDataService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Path("/api/meta-data")
@RequestScoped
@Produces("application/json")
public class MetaDataController {
    @Inject
    MetaDataService metaDataService;

    @POST
    @Path("/SetEmReadings/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addElectricityMeterReading(@PathParam("deviceId") Long deviceId, EnergyMeterMetaData reading) {
        try {
            metaDataService.addReading(deviceId, reading);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
    @POST
    @Path("/SetWmReadings/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addWaterMeterReading(@PathParam("deviceId") Long deviceId, WaterMeterMetaData reading) {
        try {
            metaDataService.addWmReading(deviceId, reading);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
    @POST
    @Path("/SetThlReadings/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addThlReading(@PathParam("deviceId") Long deviceId, THLMetaData reading) {
        try {
            metaDataService.addTHlReading(deviceId, reading);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/getReadings/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getElectricityMeterReadings(@PathParam("deviceId") Long deviceId) {
        List<EnergyMeterMetaData> readings = metaDataService.getEmReadings(deviceId);
        return Response.ok(readings).build();
    }

    //calculateConsumption_cout_carbone_em
    @GET
    @Path("/consumption-cout-carbone-em/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculateConsumption_cout_carbone_em(@PathParam("deviceId") Long deviceId) {
        DeviceConsumption_cout_carbone deviceConsumption_cout_carbone = metaDataService.calculateConsumption_cout_carbone_em(deviceId);
        return Response.ok(deviceConsumption_cout_carbone).build();
    }
    //calculateConsumption_cout_carbone_ByUserId
    @GET
    @Path("/consumption-cout-carbone-ByUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculateConsumption_cout_carbone_ByUserId(@PathParam("userId") Long userId) {
        List<DeviceConsumption_cout_carbone> deviceConsumption_cout_carbone = metaDataService.calculateConsumption_cout_carbone_ByUserId(userId);
        return Response.ok(deviceConsumption_cout_carbone).build();
    }
    //    public TotalConsumptionWmEmCo2SolarPanel totalWaterAndElectricityConsumptionAndCarbonEviteByUserId(Long userId) {
    @GET
    @Path("/eco-friendly-kpis/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGreenTotalConsumption(@PathParam("userId") Long userId) {
        GreenTotalConsumptionWmEmCo2Sp greenTotalConsumptionWmEmCo2Sp = metaDataService.totalWaterAndElectricityConsumptionAndCarbonEviteByUserId(userId);
        return Response.ok(greenTotalConsumptionWmEmCo2Sp).build();
    }


    // Energy meter Kpis

    @GET
    @Path("/getEnergyPhasesByDate/{deviceId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnergyPhasesByDate(@PathParam("deviceId") Long deviceId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        return Response.ok(metaDataService.getEnergyPhasesByDate(deviceId, startDate, endDate)).build();
    }
    @GET
    @Path("/getActivePowerPhasesByDate/{deviceId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActivePowerPhasesByDate(@PathParam("deviceId") Long deviceId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {

        return Response.ok(metaDataService.getActivePowerPhasesByDate(deviceId, startDate, endDate)).build();
    }

    @GET
    @Path("/getHarmonicDistortionPhasesByDate/{deviceId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHarmonicDistortionPhasesByDate(@PathParam("deviceId") Long deviceId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        return Response.ok(metaDataService.getHarmonicDistortionPhasesByDate(deviceId, startDate, endDate)).build();
    }

    @GET
    @Path("/getPowerFactorPhasesByDate/{deviceId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPowerFactorPhasesByDate(@PathParam("deviceId") Long deviceId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) {

        return Response.ok(metaDataService.getPowerFactorPhasesByDate(deviceId, startDate, endDate)).build();
    }
    //powerFactorPhasesForUser
    @GET
    @Path("/powerFactorPhasesForUser/{userId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPowerFactorPhasesByUserIdAndPeriod(@PathParam("userId") Long userId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        return Response.ok(metaDataService.powerFactorPhasesForUser(userId, startDate, endDate)).build();
    }
    @GET
    @Path("/harmonicDistortionPhasesForUser/{userId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response harmonicDistortionPhasesForUser(@PathParam("userId") Long userId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        return Response.ok(metaDataService.harmonicDistortionPhasesForUser(userId, startDate, endDate)).build();
    }
    @GET
    @Path("/activePowerPhasesPhasesForUser/{userId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activePowerPhasesPhasesForUser(@PathParam("userId") Long userId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        return Response.ok(metaDataService.activePowerPhasesForUser(userId, startDate, endDate)).build();
    }

    @GET
    @Path("/energyPhasesPhasesPhasesForUser/{userId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response energyPhasesForUser(@PathParam("userId") Long userId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        return Response.ok(metaDataService.energyPhasesForUser(userId, startDate, endDate)).build();
    }
//countEnergyMetersByUserId
    @GET
    @Path("/countEnergyMetersByUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countEnergyMetersByUserId(@PathParam("userId") Long userId) {
        return Response.ok(metaDataService.countEnergyMetersByUserId(userId)).build();
    }

    @GET
    @Path("/EnergyMeterMetaDataByUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response EnergyMeterMetaDataByUserId(@PathParam("userId") Long userId) {
        return Response.ok(metaDataService.getEnergyMeterMetaDataByUserId(userId)).build();
    }
//save alert
    @POST
    @Path("/saveAlert/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveAlert(Alert alert,@PathParam("deviceId")  Long deviceId) {
        try {
            metaDataService.addAlert(alert, deviceId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }


    @PUT
    @Path("/updateAlert")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAlert(Alert alert) {
        try {
            metaDataService.updateAlert(alert);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
    @GET
    @Path("/getAlerts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlerts() {
        try {

            return Response.ok(metaDataService.getAlerts()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
