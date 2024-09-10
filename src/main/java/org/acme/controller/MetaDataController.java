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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.List;

@AllArgsConstructor
@Path("/api/meta-data")
@RequestScoped
@Produces("application/json")
public class MetaDataController {
    @Inject
    MetaDataService metaDataService;
    private static final Logger logger = LoggerFactory.getLogger(MetaDataController.class);

    @POST
    @Path("/SetEmReadings/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addElectricityMeterReading(@PathParam("deviceId") Long deviceId, EnergyMeterMetaData reading) {
        logger.info("Received request to add electricity meter reading for device ID: {}", deviceId);
        try {
            metaDataService.addReading(deviceId, reading);
            logger.info("Successfully added electricity meter reading for device ID: {}", deviceId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error adding electricity meter reading for device ID: {}", deviceId, e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/SetWmReadings/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addWaterMeterReading(@PathParam("deviceId") Long deviceId, WaterMeterMetaData reading) {
        logger.info("Received request to add water meter reading for device ID: {}", deviceId);
        try {
            metaDataService.addWmReading(deviceId, reading);
            logger.info("Successfully added water meter reading for device ID: {}", deviceId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error adding water meter reading for device ID: {}", deviceId, e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/SetThlReadings/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addThlReading(@PathParam("deviceId") Long deviceId, THLMetaData reading) {
        logger.info("Received request to add THL reading for device ID: {}", deviceId);
        try {
            metaDataService.addTHlReading(deviceId, reading);
            logger.info("Successfully added THL reading for device ID: {}", deviceId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error adding THL reading for device ID: {}", deviceId, e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/getReadings/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getElectricityMeterReadings(@PathParam("deviceId") Long deviceId) {
        logger.info("Received request to get electricity meter readings for device ID: {}", deviceId);
        try {
            List<EnergyMeterMetaData> readings = metaDataService.getEmReadings(deviceId);
            logger.info("Successfully retrieved electricity meter readings for device ID: {}", deviceId);
            return Response.ok(readings).build();
        } catch (Exception e) {
            logger.error("Error retrieving electricity meter readings for device ID: {}", deviceId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/consumption-cout-carbone-em/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculateConsumption_cout_carbone_em(@PathParam("deviceId") Long deviceId) {
        logger.info("Received request to calculate consumption, cost, and carbon emissions for device ID: {}", deviceId);
        try {
            DeviceConsumption_cout_carbone deviceConsumption_cout_carbone = metaDataService.calculateConsumption_cout_carbone_em(deviceId);
            logger.info("Successfully calculated consumption, cost, and carbon emissions for device ID: {}", deviceId);
            return Response.ok(deviceConsumption_cout_carbone).build();
        } catch (Exception e) {
            logger.error("Error calculating consumption, cost, and carbon emissions for device ID: {}", deviceId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    //calculateConsumption_cout_carbone_ByUserId

    @GET
    @Path("/consumption-cout-carbone-ByUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculateConsumption_cout_carbone_ByUserId(@PathParam("userId") Long userId) {
        logger.info("Received request to calculate consumption, cost, and carbon emissions for user ID: {}", userId);
        List<DeviceConsumption_cout_carbone> deviceConsumption_cout_carbone = metaDataService.calculateConsumption_cout_carbone_ByUserId(userId);
        logger.info("Successfully calculated consumption, cost, and carbon emissions for user ID: {}", userId);
        return Response.ok(deviceConsumption_cout_carbone).build();
    }

    @GET
    @Path("/eco-friendly-kpis/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGreenTotalConsumption(@PathParam("userId") Long userId) {
        logger.info("Received request to get green total consumption for user ID: {}", userId);
        GreenTotalConsumptionWmEmCo2Sp greenTotalConsumptionWmEmCo2Sp = metaDataService.totalWaterAndElectricityConsumptionAndCarbonEviteByUserId(userId);
        logger.info("Successfully retrieved green total consumption for user ID: {}", userId);
        return Response.ok(greenTotalConsumptionWmEmCo2Sp).build();
    }

    // Energy meter Kpis

    @GET
    @Path("/getEnergyPhasesByDate/{deviceId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnergyPhasesByDate(@PathParam("deviceId") Long deviceId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        logger.info("Received request to get energy phases by date for device ID: {}, start date: {}, end date: {}", deviceId, startDate, endDate);
        return Response.ok(metaDataService.getEnergyPhasesByDate(deviceId, startDate, endDate)).build();
    }

    @GET
    @Path("/getActivePowerPhasesByDate/{deviceId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActivePowerPhasesByDate(@PathParam("deviceId") Long deviceId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        logger.info("Received request to get active power phases by date for device ID: {}, start date: {}, end date: {}", deviceId, startDate, endDate);
        return Response.ok(metaDataService.getActivePowerPhasesByDate(deviceId, startDate, endDate)).build();
    }

    @GET
    @Path("/getHarmonicDistortionPhasesByDate/{deviceId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHarmonicDistortionPhasesByDate(@PathParam("deviceId") Long deviceId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        logger.info("Received request to get harmonic distortion phases by date for device ID: {}, start date: {}, end date: {}", deviceId, startDate, endDate);
        return Response.ok(metaDataService.getHarmonicDistortionPhasesByDate(deviceId, startDate, endDate)).build();
    }

    @GET
    @Path("/getPowerFactorPhasesByDate/{deviceId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPowerFactorPhasesByDate(@PathParam("deviceId") Long deviceId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) {
        logger.info("Received request to get power factor phases by date for device ID: {}, start date: {}, end date: {}", deviceId, startDate, endDate);
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
        logger.info("Received request to get active power phases for user ID: {}, start date: {}, end date: {}", userId, startDate, endDate);
        return Response.ok(metaDataService.activePowerPhasesForUser(userId, startDate, endDate)).build();
    }

    @GET
    @Path("/energyPhasesPhasesPhasesForUser/{userId}/{startDate}/{endDate}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response energyPhasesForUser(@PathParam("userId") Long userId, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate) throws ParseException {
        logger.info("Received request to get energy phases for user ID: {}, start date: {}, end date: {}", userId, startDate, endDate);

        return Response.ok(metaDataService.energyPhasesForUser(userId, startDate, endDate)).build();
    }

    //countEnergyMetersByUserId
    @GET
    @Path("/countEnergyMetersByUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countEnergyMetersByUserId(@PathParam("userId") Long userId) {
        logger.info("Received request to count energy meters by user ID: {}", userId);
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
    public Response saveAlert(Alert alert, @PathParam("deviceId") Long deviceId) {
        logger.info("Received request to save alert for device ID: {}", deviceId);
        try {
            metaDataService.addAlert(alert, deviceId);
            logger.info("Successfully saved alert for device ID: {}", deviceId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error saving alert for device ID: {}", deviceId, e);

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
    @GET
    @Path("/device/{deviceId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDeviceReadings(@PathParam("deviceId") Long deviceId) {
        List<EnergyMeterMetaData> readings = metaDataService.findByDeviceId(deviceId);

        String csvContent = convertReadingsToCSV(readings);

        return Response.ok(csvContent)
                .header("Content-Disposition", "attachment; filename=\"readings.csv\"")
                .build();
    }

    private String convertReadingsToCSV(List<EnergyMeterMetaData> readings) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Date,CurrentReading,PreviousReading,Estimated_energy_L1,Estimated_energy_L2,Estimated_energy_L3,Estimated_energy_L4,Estimated_energy_L5,Energy_L2,Energy_L3,Energy_L4,Energy_L5,Energy_L6,Reactive_energy_day_L1,Reactive_energy_day_L2,Reactive_energy_day_L3,Active_power_L1,Active_power_L2,Active_power_L3,Rms_current_L1,Rms_current_L2,Rms_current_L3,Rms_voltage_L1,Rms_voltage_L2,Rms_voltage_L3,Power_factor_L1,Power_factor_L2,Power_factor_L3,Battery_level,Rssi,Snr,Ctr \n");

        for (EnergyMeterMetaData reading : readings) {
            csvBuilder.append(reading.getDate()).append(",");
            csvBuilder.append(reading.getCurrentReading()).append(",");
            csvBuilder.append(reading.getPreviousReading()).append(",");
            csvBuilder.append(reading.getEstimated_energy_L1()).append(",");
            csvBuilder.append(reading.getEstimated_energy_L2()).append(",");
            csvBuilder.append(reading.getEstimated_energy_L3()).append(",");
            csvBuilder.append(reading.getEstimated_energy_L4()).append(",");
            csvBuilder.append(reading.getEstimated_energy_L5()).append(",");
            csvBuilder.append(reading.getEnergy_L2()).append(",");
            csvBuilder.append(reading.getEnergy_L3()).append(",");
            csvBuilder.append(reading.getEnergy_L4()).append(",");
            csvBuilder.append(reading.getEnergy_L5()).append(",");
            csvBuilder.append(reading.getEnergy_L6()).append(",");
            csvBuilder.append(reading.getReactive_energy_day_L1()).append(",");
            csvBuilder.append(reading.getReactive_energy_day_L2()).append(",");
            csvBuilder.append(reading.getReactive_energy_day_L3()).append(",");

            csvBuilder.append(reading.getActive_power_L1()).append(",");
            csvBuilder.append(reading.getActive_power_L2()).append(",");
            csvBuilder.append(reading.getActive_power_L3()).append(",");
            csvBuilder.append(reading.getRms_current_L1()).append(",");
            csvBuilder.append(reading.getRms_current_L2()).append(",");
            csvBuilder.append(reading.getRms_current_L3()).append(",");
            csvBuilder.append(reading.getRms_voltage_L1()).append(",");
            csvBuilder.append(reading.getRms_voltage_L2()).append(",");
            csvBuilder.append(reading.getRms_voltage_L3()).append(",");
            csvBuilder.append(reading.getPower_factor_L1()).append(",");
            csvBuilder.append(reading.getPower_factor_L2()).append(",");
            csvBuilder.append(reading.getPower_factor_L3()).append(",");
            csvBuilder.append(reading.getBattery_level()).append(",");
            csvBuilder.append(reading.getRssi()).append(",");
            csvBuilder.append(reading.getSnr()).append(",");
            csvBuilder.append(reading.getCtr()).append(",");

            csvBuilder.append("\n");
        }

        return csvBuilder.toString();
    }
//carbonTracker
    @GET
    @Path("/carbonTracker/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response carbonTracker(@PathParam("userId") Long userId) {
        logger.info("Received request to calculate carbon tracker for user ID: {}", userId);
        return Response.ok(metaDataService.carbonTracker(userId)).build();
    }
    //getEnergyQuality
    @GET
    @Path("/energyQuality/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnergyQuality(@PathParam("userId") Long userId) {
        logger.info("Received request to calculate energy quality for user ID: {}", userId);
        return Response.ok(metaDataService.getEnergyQuality(userId)).build();
    }
    //getListOfthlReadings
    @GET
    @Path("/getListOfthlReadings/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfthlData(@PathParam("userId") Long userId) {
        logger.info("Received request to get list of THL readings for user ID: {}", userId);
        return Response.ok(metaDataService.getListOfthlReadings(userId)).build();
    }
    @GET
    @Path("/thlDataPercentage/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculateThlDataPercentage(@PathParam("userId") Long userId) {
        logger.info("Received request to get list of THL readings for user ID: {}", userId);
        return Response.ok(metaDataService.calculateThlDataPercentage(userId)).build();
    }
    @GET
    @Path("/getThlAvgByUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThlAvgByUserId(@PathParam("userId") Long userId) {
        logger.info("Received request to get THL average for user ID: {}", userId);
        return Response.ok(metaDataService.calculateAverageTHL(userId)).build();
    }

}
