package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.acme.dto.*;
import org.acme.model.Client;
import org.acme.model.ClientDevice;
import org.acme.model.MetaData.EnergyMeterMetaData;
import org.acme.model.MetaData.THLMetaData;
import org.acme.model.MetaData.WaterMeterMetaData;
import org.acme.model.devices.Device;
import org.acme.model.devices.ElectricityMeter;
import org.acme.model.devices.THL;
import org.acme.model.devices.WaterMeter;
import org.acme.repository.ClientDeviceRepo;
import org.acme.repository.devices.DeviceRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Transactional
@ApplicationScoped
public class DeviceService {
    @Inject
    DeviceRepository deviceRepo;
    @Inject
    ClientDeviceRepo clientDeviceRepo;
    private static final Logger LOGGER = Logger.getLogger(DeviceService.class.getName());

    public List<Device> getDevices() {
        var devices = deviceRepo.listAll();
        return devices;
    }

    public Response create(Device device) {
        if (device.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        deviceRepo.persist(device);
        return Response.ok(device).status(201).build();
    }

    public Device update(Long id, Device device) {
        if (device.getName() == null) {
            throw new WebApplicationException("Device Name was not set on request.", 422);
        }

        Device entity = deviceRepo.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Device with id of " + id + " does not exist.", 404);
        }
        entity.setName(device.getName());
        entity.setDescription(device.getDescription());
        entity.setStatus(device.getStatus());
        entity.setEspace(device.getEspace());
        entity.setZone(device.getZone());
        entity.setGateway(device.getGateway());
        entity.setOwner(device.getOwner());
        entity.setInstallationDate(device.getInstallationDate());
        entity.setLastMaintenanceDate(device.getLastMaintenanceDate());
        return device;
    }

    public Response delete(Long id) {
        Device entity = deviceRepo.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Device with id of " + id + " does not exist.", 404);
        }
        deviceRepo.delete(entity);
        return Response.status(204).build();
    }


    public Device getById(Long Iddevice) {

        return deviceRepo.findById(Iddevice);
    }


    public List<Device> getDevicesByType(String deviceType) {

        return deviceRepo.getDevicesByType(deviceType);
    }

    @Transactional
    public THL assignTHLDevice(THL thl) {
        deviceRepo.persist(thl);
        return thl;
    }

    @Transactional
    public WaterMeter assignWMDevice(WaterMeter wm) {
        deviceRepo.persist(wm);
        return wm;
    }

    @Transactional
    public ElectricityMeter assignEMDevice(ElectricityMeter em) {
        deviceRepo.persist(em);
        return em;
    }


    //KPI
    @Transactional
    public List<TotalEnergyConsumptionDto> getTotalEnergyConsumption(Long userId) {
        return deviceRepo.getTotalEnergyConsumption(userId);
    }

    public List<PowerFactorDto> getPowerFactorByUserIdAndPeriod(Long userId, Date startDate, Date endDate) {
        return deviceRepo.getPowerFactorByUserIdAndPeriod(userId, startDate, endDate);
    }

    public List<Device> getDevicesByTypeAndUserId(String deviceType, Long userId) {
        return deviceRepo.getDevicesByTypeAndUserId(deviceType, userId);
    }

    public List<String> getDeviceTypesByUserId(Long userId) {

        return deviceRepo.getDeviceTypesByUserId(userId);
    }

    public List<ClientDevice> getDevicesByUserId(Long userId) {

        return clientDeviceRepo.getDevicesByUserId(userId);
    }

    public List<Device> devicesByUserId(Long userId) {
        return deviceRepo.DevicesByUserId(userId);
    }

    public List<String> getDeviceAttributesByDeviceType(String deviceType) {
        List<String> fieldNames = new ArrayList<>();
        try {
            Class<?> metadataClass;
            switch (deviceType.toLowerCase()) {
                case "thl":
                    metadataClass = THLMetaData.class;
                    break;
                case "watermeter":
                    metadataClass = WaterMeterMetaData.class;
                    break;
                case "electricitymeter":
                    metadataClass = EnergyMeterMetaData.class;
                    break;
                // Add other device types here
                default:
                    throw new IllegalArgumentException("Invalid device type: " + deviceType);
            }

            Field[] fields = metadataClass.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                if (!fieldName.equals("id") &&
                        !fieldName.startsWith("$$_hibernate") &&
                        !fieldType.isAnnotationPresent(Entity.class)) {
                    fieldNames.add(fieldName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fieldNames;
    }

    public List<Map<String, Object>> getDataByUserIdAndDeviceIdAndAttributes(Long userId, Long deviceId, List<String> attributes) {
        return deviceRepo.dataByUserIdAndDeviceId(userId, deviceId, attributes);
    }

    public Map<Long, List<Map<String, Object>>> dataByUserIdAndDevicesIds(Long userId, Map<Long, List<String>> deviceAttributesMap) {
        return deviceRepo.dataByUserIdAndDeviceIds(userId, deviceAttributesMap);
    }

    /**********************************************/
    public UserData dataByUserIdAndDevicesIds2(Long userId, Map<Long, List<String>> deviceAttributesMap) {
        return deviceRepo.dataByUserIdAndDeviceIds2(userId, deviceAttributesMap);
    }

    public UserData dataByUserIdAndDevicesIds3(Long userId, List<DeviceAttributeRequest> deviceAttributesList) {
        return deviceRepo.dataByUserIdAndDeviceIds3(userId, deviceAttributesList);
    }
    //TODO LOGGERS slf4j ye 7aaaaaaaaaaaaaj WEEEEEEEEEEEE

    public List<DeviceEmissionDto> calculateCarbonEmissions(Long userId) {
        return deviceRepo.calculateCarbonEmissions(userId);
    }

    public Double getTotalEnergyConsumptionForElectricityMeter(Long userId) {
        return deviceRepo.getTotalEnergyConsumptionForElectricityMeter(userId);
    }

    // getClientIdByDevicesIds(deviceIds)
    public Client getClientIdByDevicesIds(List<Long> deviceIds) {
        return deviceRepo.getClientIdByDevicesIds(deviceIds);
    }
    //calculateTotalEnergyConsumptionForAllUsers
    public double calculateTotalEnergyConsumptionForAll() {
        return deviceRepo.calculateTotalEnergyConsumptionForAllUsers();
    }
    //calculateCarbonFootprintForAllUsers
    public double calculateCarbonFootprintForAllUsers() {
        return deviceRepo.calculateCarbonFootprintForAllUsers();
    }
    //SolarEnergyProduced
    public double SolarEnergyProduced() {
        return deviceRepo.SolarEnergyProduced();
    }

}


