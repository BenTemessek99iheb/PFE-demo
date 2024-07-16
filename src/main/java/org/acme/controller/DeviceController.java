package org.acme.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.acme.dto.*;
import org.acme.model.ClientDevice;
import org.acme.model.devices.Device;
import org.acme.model.devices.ElectricityMeter;
import org.acme.model.devices.THL;
import org.acme.model.devices.WaterMeter;
import org.acme.service.ClientService;
import org.acme.service.DeviceService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@AllArgsConstructor
@Path("/api/device")
@RequestScoped
@Produces("application/json")
public class DeviceController {

    @Inject
    DeviceService deviceService;
    @Inject
    ClientService clientService;

    @Transactional
    @GET
    public Response getDevices() {
        List<Device> devices = deviceService.getDevices();
        return Response.ok(devices).build();
    }


    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        Device device = deviceService.getById(id);
        if (device != null) {
            return Response.ok(device).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Transactional
    public Response addDevice(Device deviceDto) throws URISyntaxException {
        try {
            Response response = deviceService.create(deviceDto);
            return Response.created(new URI("/api/device/" + deviceDto.getId())).entity(response.getEntity()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/type/{deviceType}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Device> getDevicesByType(@PathParam("deviceType") String deviceType) {
        return deviceService.getDevicesByType(deviceType);
    }

    @POST
    @Path("/thl")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public THL assignTHLDevice(THL thl) {
        return deviceService.assignTHLDevice(thl);
    }

    @POST
    @Path("/watermeter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WaterMeter assignWMDevice(WaterMeter wm) {
        return deviceService.assignWMDevice(wm);
    }

    @POST
    @Path("/electricitymeter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ElectricityMeter assignEMDevice(ElectricityMeter em) {
        return deviceService.assignEMDevice(em);
    }


    @GET
    @Path("/totalEnergyConsumption/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TotalEnergyConsumptionDto> getTotalEnergyConsumption(Long userId) {
        return deviceService.getTotalEnergyConsumption(userId);
    }

    @GET
    @Path("/powerFactor/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPowerFactorByUserIdAndPeriod(@PathParam("userId") Long userId,
                                                    @QueryParam("startDate") Date startDate,
                                                    @QueryParam("endDate") Date endDate) {
        try {
            List<PowerFactorDto> powerFactors = deviceService.getPowerFactorByUserIdAndPeriod(userId, startDate, endDate);
            getPowerFactorByUserIdAndPeriod(userId, startDate, endDate);
            return Response.ok(powerFactors).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{deviceType}/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Device> getDevicesByTypeAndUserId(@PathParam("deviceType") String deviceType, @PathParam("userId") Long userId) {
        return deviceService.getDevicesByTypeAndUserId(deviceType, userId);
    }

    @GET
    @Path("/types/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getDeviceTypesByUserId(@PathParam("userId") Long userId) {
        return deviceService.getDeviceTypesByUserId(userId);
    }

    @GET
    @Path("/deviceByUser/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Device> devicesByUserId(@PathParam("userId") Long userId) {
        return deviceService.devicesByUserId(userId);
    }


    @GET
    @Path("/deviceByUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientDevice> getDevicesByUserId(@PathParam("userId") Long userId) {
        return deviceService.getDevicesByUserId(userId);
    }

    @GET
    @Path("/attributes/{deviceType}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getDeviceAttributesByDeviceType(@PathParam("deviceType") String deviceType) {
        return deviceService.getDeviceAttributesByDeviceType(deviceType);
    }

    @GET
    @Path("/data/{userId}/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDataByDeviceTypeAttribute(@PathParam("userId") Long userId,
                                                 @PathParam("deviceId") Long deviceId,
                                                 @QueryParam("attributes") String attributes) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.info("Received request with attributes: " + attributes + ", userId: " + userId + ", deviceId: " + deviceId);

        if (attributes == null || attributes.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Attribute parameter cannot be null or empty").build();
        }

        try {
            List<String> attributeList = Arrays.asList(attributes.split(","));
            logger.info("Parsed attributes: " + attributeList);

            List<Map<String, Object>> result = deviceService.getDataByUserIdAndDeviceIdAndAttributes(userId, deviceId, attributeList);
            return Response.ok(result).build();
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @GET
    @Path("/carbon-emissions/{userId}")
    public Response getCarbonEmissions(@PathParam("userId") Long userId) {
        try {
            List<DeviceEmissionDto> carbonEmissions = deviceService.calculateCarbonEmissions(userId);
            return Response.ok(carbonEmissions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error calculating carbon emissions: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/wizard/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDataByUserIdAndDeviceIds(@PathParam("userId") Long userId,
                                                @QueryParam("deviceAttributes") String deviceAttributes) {
        try {
            Map<Long, List<String>> deviceAttributesMap = parseDeviceAttributes(deviceAttributes);
            Map<Long, List<Map<String, Object>>> result = deviceService.dataByUserIdAndDevicesIds(userId, deviceAttributesMap);
            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private Map<Long, List<String>> parseDeviceAttributes(String deviceAttributes) {
        Map<Long, List<String>> deviceAttributesMap = new HashMap<>();
        String[] devices = deviceAttributes.split(";");
        for (String device : devices) {
            String[] parts = device.split(":");
            Long deviceId = Long.parseLong(parts[0]);
            List<String> attributes = Arrays.stream(parts[1].split(","))
                    .filter(attr -> !attr.trim().isEmpty())
                    .collect(Collectors.toList());
            deviceAttributesMap.put(deviceId, attributes);
        }
        return deviceAttributesMap;
    }

    /**********************************************************/
    @GET
    @Path("/wizard2/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDataByUserIdAndDeviceIds2(@PathParam("userId") Long userId,
                                                 @QueryParam("deviceAttributes") String deviceAttributes) {
        try {
            Map<Long, List<String>> deviceAttributesMap = parseDeviceAttributes(deviceAttributes);
            System.out.println("Parsed Device Attributes Map: " + deviceAttributesMap);

            UserData result = deviceService.dataByUserIdAndDevicesIds2(userId, deviceAttributesMap);
            System.out.println("Resulting UserData: " + result);

            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /***********************************************/

    @GET
    @Path("/wizard3/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDataByUserIdAndDeviceIds3(@PathParam("userId") Long userId,
                                                 @QueryParam("deviceAttributes") String deviceAttributes) {
        try {
            List<DeviceAttributeRequest> deviceAttributesList = parseDeviceAttributes2(deviceAttributes);

            System.out.println("Received Device Attributes List: " + deviceAttributesList);

            UserData result = deviceService.dataByUserIdAndDevicesIds3(userId, deviceAttributesList);
            System.out.println("Resulting UserData: " + result);

            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private List<DeviceAttributeRequest> parseDeviceAttributes2(String deviceAttributes) {
        List<DeviceAttributeRequest> deviceAttributesList = new ArrayList<>();
        String[] devices = deviceAttributes.split(";");
        for (String device : devices) {
            String[] parts = device.split(":");
            Long deviceId = Long.parseLong(parts[0]);
            List<String> attributes = Arrays.stream(parts[1].split(","))
                    .filter(attr -> !attr.trim().isEmpty())
                    .collect(Collectors.toList());
            deviceAttributesList.add(new DeviceAttributeRequest(deviceId, attributes));
        }
        return deviceAttributesList;
    }


    /*********************************************/
    @GET
    @Path("/total-energy-consumption/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTotalWaterConsumption(@PathParam("userId") Long userId) {
        try {
            double totalConsumption = deviceService.getTotalEnergyConsumptionForElectricityMeter(userId);
            return Response.ok(totalConsumption).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error calculating total water consumption for WaterMeter with userId: " + userId)
                    .build();
        }
    }

}
