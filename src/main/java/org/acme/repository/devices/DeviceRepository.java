package org.acme.repository.devices;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.acme.dto.*;
import org.acme.model.Client;
import org.acme.model.MetaData.EnergyMeterMetaData;
import org.acme.model.devices.*;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;

@ApplicationScoped
public class DeviceRepository implements PanacheRepositoryBase<Device, Long> {
    @PersistenceContext
    EntityManager entityManager;

    //TODO ADD THE PERIOD OF CONSUMPTION
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRepository.class);

    private static final double ELECTRICITY_EMISSION_FACTOR = 0.58;
    private static final double WATER_EMISSION_FACTOR = 0.2;

    public List<Device> getDevicesByUserId(Long userId) {
        try {
            LOGGER.info("Fetching devices for userId: {}", userId);
            TypedQuery<Device> query = entityManager.createQuery(
                    "SELECT d FROM Device d JOIN ClientDevice cd ON d.id = cd.device.id WHERE cd.client.id = :userId",
                    Device.class
            );
            query.setParameter("userId", userId);
            List<Device> devices = query.getResultList();
            LOGGER.info("Found {} devices for userId: {}", devices.size(), userId);
            return devices;
        } catch (Exception e) {
            LOGGER.error("Error fetching devices for userId: {}", userId, e);
            throw e;
        }
    }


    // Calculate allowable consumption and emissions for each device
    @Transactional
    public List<DeviceEmissionDto> calculateCarbonEmissions(Long userId) {
        List<DeviceEmissionDto> deviceEmissions = new ArrayList<>();
        double totalAllowableWaterConsumption = 0.0115 / WATER_EMISSION_FACTOR;
        double totalAllowableElectricityConsumption = 0.0115 / ELECTRICITY_EMISSION_FACTOR;

        try {
            List<Device> devices = getDevicesByUserId(userId);
            LOGGER.info("Calculating carbon emissions for userId: {}", userId);

            for (Device device : devices) {
                if (device instanceof ElectricityMeter) {
                    double totalEnergyConsumption = getTotalEnergyConsumptionForElectricityMeter(device.getId());
                    double carbonEmission = totalEnergyConsumption * ELECTRICITY_EMISSION_FACTOR;
                    double allowableConsumption = totalAllowableElectricityConsumption;

                    deviceEmissions.add(new DeviceEmissionDto(
                            device.getName() + device.getId(),
                            allowableConsumption,
                            totalEnergyConsumption,
                            carbonEmission
                    ));

                    LOGGER.info("Device: {}, Allowable Electricity Consumption: {}, Total Consumption: {}, Carbon Emission: {}",
                            device.getName() + device.getId(), allowableConsumption, totalEnergyConsumption, carbonEmission);
                } else if (device instanceof WaterMeter) {
                    double totalWaterConsumption = getTotalWaterConsumptionForWaterMeter(device.getId());
                    double carbonEmission = totalWaterConsumption * WATER_EMISSION_FACTOR;
                    double allowableConsumption = totalAllowableWaterConsumption;

                    deviceEmissions.add(new DeviceEmissionDto(
                            device.getName() + device.getId(),
                            allowableConsumption,
                            totalWaterConsumption,
                            carbonEmission
                    ));

                    LOGGER.info("Device: {}, Allowable Water Consumption: {}, Total Consumption: {}, Carbon Emission: {}",
                            device.getName() + device.getId(), allowableConsumption, totalWaterConsumption, carbonEmission);
                }
            }

            LOGGER.info("Calculated carbon emissions for userId: {}", userId);
        } catch (Exception e) {
            LOGGER.error("Error calculating carbon emissions for userId: {}", userId, e);
            throw e;
        }

        return deviceEmissions;
    }

    // Retrieve devices by user ID

    // Retrieve total energy consumption for a given electricity meter
    @Transactional
    public double getTotalEnergyConsumptionForElectricityMeter(Long deviceId) {
        try {
            LOGGER.info("Calculating total energy consumption for ElectricityMeter with deviceId: {}", deviceId);
            String queryString = "SELECT SUM(em.energy_L1 + em.energy_L2 + em.energy_L3 + em.energy_L4 + em.energy_L5 + em.energy_L6) " +
                    "FROM EnergyMeterMetaData em WHERE em.electricityMeter.id = :deviceId";
            TypedQuery<Double> query = entityManager.createQuery(queryString, Double.class);
            query.setParameter("deviceId", deviceId);
            Double totalEnergy = query.getSingleResult();
            LOGGER.info("Total energy consumption for ElectricityMeter with deviceId: {} is {}", deviceId, totalEnergy);

            if (totalEnergy != null) {
                DecimalFormat df = new DecimalFormat("#.###");
                return Double.parseDouble(df.format(totalEnergy));
            } else {
                return 0.0;
            }
        } catch (Exception e) {
            LOGGER.error("Error calculating total energy consumption for ElectricityMeter with deviceId: {}", deviceId, e);
            throw e;
        }
    }

    // Retrieve total water consumption for a given water meter
    @Transactional
    public double getTotalWaterConsumptionForWaterMeter(Long deviceId) {
        try {
            LOGGER.info("Calculating total water consumption for WaterMeter with deviceId: {}", deviceId);
            String queryString = "SELECT SUM(wm.forwardFlow + wm.forwardFlowBack + " +
                    "wm.reverseFlow + wm.reverseFlowBack + " +
                    "wm.consoH + wm.consoJ + wm.consoM + " +
                    "wm.reverseConsoH + wm.reverseConsoJ + wm.reverseConsoM + " +
                    "wm.infractionConsoH + wm.infractionConsoJ + wm.infractionConsoM) " +
                    "FROM WaterMeterMetaData wm WHERE wm.waterMeter.id = :deviceId";
            TypedQuery<Double> query = entityManager.createQuery(queryString, Double.class);
            query.setParameter("deviceId", deviceId);
            Double totalWaterConsumption = query.getSingleResult();
            LOGGER.info("Total water consumption for WaterMeter with deviceId: {} is {}", deviceId, totalWaterConsumption);
            return totalWaterConsumption != null ? totalWaterConsumption : 0.0;
        } catch (Exception e) {
            LOGGER.error("Error calculating total water consumption for WaterMeter with deviceId: {}", deviceId, e);
            throw e;
        }
    }

    //FIXME THIS IS NOT GONNA WORK CHECK E JOINTURE BROOO
    @Transactional
    public List<TotalEnergyConsumptionDto> getTotalEnergyConsumption(Long userId) {
        String query = "SELECT new org.acme.dto.TotalEnergyConsumptionDto(e.energy_L1, e.energy_L2, e.energy_L3, e.energy_L4, e.energy_L5, e.energy_L6, e.energy_L1 + e.energy_L2 + e.energy_L3 + e.energy_L4 + e.energy_L5 + e.energy_L6) " +
                "FROM EnergyMeterMetaData e " +
                "JOIN ClientDevice cd ON e.id = cd.device.id " +
                "WHERE cd.client.id = :userId AND cd.deviceType = 3";
        TypedQuery<TotalEnergyConsumptionDto> typedQuery = entityManager.createQuery(query, TotalEnergyConsumptionDto.class);
        typedQuery.setParameter("userId", userId);
        return typedQuery.getResultList();
    }

    @Transactional
    public List<Device> getDevicesByType(String deviceType) {
        Map<String, Class<? extends Device>> deviceTypeMap = new HashMap<>();
        deviceTypeMap.put("watermeter", WaterMeter.class);
        deviceTypeMap.put("electricitymeter", ElectricityMeter.class);
        deviceTypeMap.put("thl", THL.class);

        Class<? extends Device> deviceClass = deviceTypeMap.get(deviceType.toLowerCase());
        if (deviceClass == null) {
            throw new IllegalArgumentException("Invalid device type: " + deviceType);
        }

        TypedQuery<Device> query = entityManager.createQuery("SELECT d FROM Device d WHERE TYPE(d) = :deviceType", Device.class);
        query.setParameter("deviceType", deviceClass);
        return query.getResultList();
    }

    // FIXME dATE INVALID
    @Transactional
    public List<Device> getDevicesByTypeAndUserId(String deviceType, Long userId) {
        Map<String, Class<? extends Device>> deviceTypeMap = new HashMap<>();
        deviceTypeMap.put("watermeter", WaterMeter.class);
        deviceTypeMap.put("electricitymeter", ElectricityMeter.class);
        deviceTypeMap.put("thl", THL.class);

        Class<? extends Device> deviceClass = deviceTypeMap.get(deviceType.toLowerCase());
        if (deviceClass == null) {
            throw new IllegalArgumentException("Invalid device type: " + deviceType);
        }

        TypedQuery<Device> query = entityManager.createQuery("SELECT d FROM Device d JOIN ClientDevice cd ON d.id = cd.device.id WHERE TYPE(d) = :deviceType AND cd.client.id = :userId", Device.class);
        query.setParameter("deviceType", deviceClass);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    //FIXME THIS IS NOT GONNA WORK ZEDAAA CHECK JOINTURE
    @Transactional
    public List<PowerFactorDto> getPowerFactorByUserIdAndPeriod(Long userId, Date startDate, Date endDate) {
        String query = "SELECT new org.acme.dto.PowerFactorByUserIdAndPeriodDto(:userId, :startDate, :endDate, AVG(e.active_power / (e.rms_voltage * e.rms_current))) " +
                "FROM ElectricityMeter e " +
                "JOIN ClientDevice cd ON e.id = cd.device.id " +
                "WHERE cd.client.id = :userId AND e.date >= :startDate AND e.date <= :endDate";
        TypedQuery<PowerFactorDto> typedQuery = entityManager.createQuery(query, PowerFactorDto.class);
        typedQuery.setParameter("userId", userId);
        typedQuery.setParameter("startDate", startDate);
        typedQuery.setParameter("endDate", endDate);
        return typedQuery.getResultList();
    }

    @Transactional
    public List<String> getDeviceTypesByUserId(Long userId) {
        TypedQuery<String> query = entityManager.createQuery("SELECT DISTINCT cd.device.deviceType FROM ClientDevice cd WHERE cd.client.id = :userId", String.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    //  public List<String> DeviceTypesByUserId(Long userId) {
    //        TypedQuery<String> query = entityManager.createQuery("SELECT d FROM Device d join ClientDevice cd ON d.id =cd.device.id and cd.client.id =:userId", Object.c);
    //        query.setParameter("userId", userId);
    //        return query.getResultList();
    //    }

    @Transactional
    public List<Device> DevicesByUserId(Long userId) {
        TypedQuery<Device> query = entityManager.createQuery(
                "SELECT cd.device FROM ClientDevice cd WHERE cd.client.id = :userId", Device.class);
        query.setParameter("userId", userId);
        List<Device> devices = query.getResultList();

        // Explicitly initialize the graphiques collection
        for (Device device : devices) {
            Hibernate.initialize(device.getGraphiques());
        }

        return devices;
    }

    @Transactional
    public Long getDeviceByUserId(Long userId) {
        try {
            LOGGER.info("Fetching device ID for user ID: {}", userId);
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT cd.device.id FROM ClientDevice cd WHERE cd.client.id = :userId", Long.class);
            query.setParameter("userId", userId);
            Long deviceId = query.getSingleResult();
            LOGGER.info("Device ID for user ID {} is {}", userId, deviceId);
            return deviceId;
        } catch (Exception e) {
            LOGGER.error("Error fetching device ID for user ID: {}", userId, e);
            throw e;
        }
    }


    @Transactional
    public Device findByUserIdAndDeviceId(Long userId, Long deviceId) {
        TypedQuery<Device> query = entityManager.createQuery(
                "SELECT d FROM Device d JOIN ClientDevice cd ON d.id = cd.device.id WHERE cd.client.id = :userId AND d.id = :deviceId",
                Device.class
        );
        query.setParameter("userId", userId);
        query.setParameter("deviceId", deviceId);
        return query.getSingleResult();
    }

    @Transactional
    public List<Map<String, Object>> getDataByUserIdAndDeviceId(Long userId, Long deviceId, List<String> attributes) {
        // Determine device type based on deviceId
        String deviceTypeTable;
        EdeviceType deviceType = getDeviceTypeByDeviceId(deviceId); // Implement this method to retrieve device type based on deviceId

        switch (deviceType) {
            case WaterMeter:
                deviceTypeTable = "watermeter";
                break;
            case ElectricityMeter:
                deviceTypeTable = "electricitymeter";
                break;
            case THL:
                deviceTypeTable = "thl";
                break;
            default:
                throw new IllegalArgumentException("Invalid device type: " + deviceType);
        }

        String attributesSelect = String.join(", d.", attributes);
        String queryString = "SELECT d.date, d." + attributesSelect + " FROM " + deviceTypeTable + " d " +
                "JOIN clientdevice cd ON cd.device_id = d.id " +
                "WHERE cd.client_id = :userId AND cd.device_id = :deviceId";

        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("userId", userId);
        query.setParameter("deviceId", deviceId);

        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> record = new HashMap<>();
            for (int i = 0; i < attributes.size(); i++) {
                record.put(attributes.get(i), result[i]);
            }
            data.add(record);
        }

        return data;
    }

    private EdeviceType getDeviceTypeByDeviceId(Long deviceId) {
        String queryString = "SELECT cd.devicetype FROM clientdevice cd WHERE cd.device_id = :deviceId";
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("deviceId", deviceId);

        // Cast the result to a Number and get its int value
        Number deviceTypeValue = (Number) query.getSingleResult();
        return EdeviceType.values()[deviceTypeValue.intValue()]; // Assuming your enum starts from 1
    }

    @Transactional
    public List<Map<String, Object>> loulaaadataByUserIdAndDeviceId(Long userId, Long deviceId, List<String> attributes) {
        // Determine device type based on deviceId
        EdeviceType deviceType = getDeviceTypeByDeviceId(deviceId);
        String deviceTypeLowercase = deviceType.name().toLowerCase();
        String metadataTable;
        switch (deviceType) {
            case WaterMeter:
                metadataTable = "watermetermetadata";
                break;
            case ElectricityMeter:
                metadataTable = "energymetermetadata";
                break;
            case THL:
                metadataTable = "thlmetadata";
                break;
            default:
                throw new IllegalArgumentException("Invalid device type: " + deviceType);
        }

        // Include date in the selection of attributes
        String attributesSelect = String.join(", m.", attributes);
        String queryString = "SELECT m.date, m." + attributesSelect + " FROM " + metadataTable + " m " +
                "JOIN " + deviceTypeLowercase + " em ON m." + deviceTypeLowercase + "_id = em.id " +
                "JOIN device d ON em.id = d.id " +
                "JOIN clientdevice cd ON cd.device_id = d.id " +
                "WHERE cd.client_id = :userId AND cd.device_id = :deviceId";

        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("userId", userId);
        query.setParameter("deviceId", deviceId);

        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> record = new HashMap<>();
            // The first column is always the date
            record.put("date", result[0]);
            // The remaining columns are the attributes
            for (int i = 0; i < attributes.size(); i++) {
                record.put(attributes.get(i), result[i + 1]);
            }
            data.add(record);
        }

        return data;
    }

    @Transactional
    public Map<Long, List<Map<String, Object>>> dataByUserIdAndDeviceIds(Long userId, Map<Long, List<String>> deviceAttributesMap) {
        // Initialize a map to hold the results for each device
        Map<Long, List<Map<String, Object>>> resultData = new HashMap<>();

        // Iterate over each entry in the deviceAttributesMap
        for (Map.Entry<Long, List<String>> entry : deviceAttributesMap.entrySet()) {
            Long deviceId = entry.getKey(); // Get the device ID
            List<String> attributes = entry.getValue(); // Get the list of attributes for the device

            // Determine the device type based on the device ID
            EdeviceType deviceType = getDeviceTypeByDeviceId(deviceId);
            String deviceTypeLowercase = deviceType.name().toLowerCase(); // Convert device type to lowercase for table naming
            String metadataTable;

            // Select the appropriate metadata table based on the device type
            switch (deviceType) {
                case WaterMeter:
                    metadataTable = "watermetermetadata";
                    break;
                case ElectricityMeter:
                    metadataTable = "energymetermetadata";
                    break;
                case THL:
                    metadataTable = "thlmetadata";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid device type: " + deviceType);
            }

            // Construct the SQL query to select the required attributes including the date
            String attributesSelect = String.join(", m.", attributes);
            String queryString = "SELECT m.date, m." + attributesSelect + " FROM " + metadataTable + " m " +
                    "JOIN " + deviceTypeLowercase + " em ON m." + deviceTypeLowercase + "_id = em.id " +
                    "JOIN device d ON em.id = d.id " +
                    "JOIN clientdevice cd ON cd.device_id = d.id " +
                    "WHERE cd.client_id = :userId AND cd.device_id = :deviceId";

            // Create a native query using the constructed SQL query string
            Query query = entityManager.createNativeQuery(queryString);
            query.setParameter("userId", userId); // Set the userId parameter in the query
            query.setParameter("deviceId", deviceId); // Set the deviceId parameter in the query

            // Execute the query and get the results
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> data = new ArrayList<>();

            // Process each result row
            for (Object[] result : results) {
                Map<String, Object> record = new HashMap<>();
                // The first column is always the date
                record.put("date", result[0]);
                // The remaining columns are the attributes
                for (int i = 0; i < attributes.size(); i++) {
                    record.put(attributes.get(i), result[i + 1]);
                }
                data.add(record);
            }

            // Add the data for the current device to the result map
            resultData.put(deviceId, data);
        }

        // Return the result map containing data for all devices
        return resultData;
    }

    /********************************************************/
    @Transactional
    public UserData dataByUserIdAndDeviceIds2(Long userId, Map<Long, List<String>> deviceAttributesMap) {
        UserData userData = new UserData();
        userData.setUserId(userId);
        List<DeviceData> devices = new ArrayList<>();

        for (Map.Entry<Long, List<String>> entry : deviceAttributesMap.entrySet()) {
            Long deviceId = entry.getKey();
            List<String> attributes = entry.getValue();
            EdeviceType deviceType = getDeviceTypeByDeviceId(deviceId);
            String deviceTypeLowercase = deviceType.name().toLowerCase();
            String metadataTable;

            switch (deviceType) {
                case WaterMeter:
                    metadataTable = "watermetermetadata";
                    break;
                case ElectricityMeter:
                    metadataTable = "energymetermetadata";
                    break;
                case THL:
                    metadataTable = "thlmetadata";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid device type: " + deviceType);
            }

            String attributesSelect = String.join(", m.", attributes);
            String queryString = "SELECT m.date, m." + attributesSelect + " FROM " + metadataTable + " m " +
                    "JOIN " + deviceTypeLowercase + " em ON m." + deviceTypeLowercase + "_id = em.id " +
                    "JOIN device d ON em.id = d.id " +
                    "JOIN clientdevice cd ON cd.device_id = d.id " +
                    "WHERE cd.client_id = :userId AND cd.device_id = :deviceId";

            Query query = entityManager.createNativeQuery(queryString);
            query.setParameter("userId", userId);
            query.setParameter("deviceId", deviceId);

            List<Object[]> results = query.getResultList();
            List<AttributeData> data = new ArrayList<>();

            for (Object[] result : results) {
                AttributeData record = new AttributeData();
                record.setDate((Timestamp) result[0]);

                for (int i = 0; i < attributes.size(); i++) {
                    record.addAttribute(attributes.get(i), result[i + 1]);
                }
                data.add(record);
            }

            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceId(deviceId);
            deviceData.setDeviceType(deviceType.name());
            deviceData.setAttributes(data);
            devices.add(deviceData);
        }

        userData.setDevices(devices);
        return userData;
    }

    /*******************************/
    @Transactional
    public UserData dataByUserIdAndDeviceIds3(Long userId, List<DeviceAttributeRequest> deviceAttributesList) {
        UserData userData = new UserData();
        userData.setUserId(userId);
        List<DeviceData> devices = new ArrayList<>();

        for (DeviceAttributeRequest deviceAttributeRequest : deviceAttributesList) {
            Long deviceId = deviceAttributeRequest.getDeviceId();
            List<String> attributes = deviceAttributeRequest.getAttributes();
            EdeviceType deviceType = getDeviceTypeByDeviceId(deviceId);
            String deviceTypeLowercase = deviceType.name().toLowerCase();
            String metadataTable;

            switch (deviceType) {
                case WaterMeter:
                    metadataTable = "watermetermetadata";
                    break;
                case ElectricityMeter:
                    metadataTable = "energymetermetadata";
                    break;
                case THL:
                    metadataTable = "thlmetadata";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid device type: " + deviceType);
            }

            String attributesSelect = String.join(", m.", attributes);
            String queryString = "SELECT m.date, m." + attributesSelect + " FROM " + metadataTable + " m " +
                    "JOIN " + deviceTypeLowercase + " em ON m." + deviceTypeLowercase + "_id = em.id " +
                    "JOIN device d ON em.id = d.id " +
                    "JOIN clientdevice cd ON cd.device_id = d.id " +
                    "WHERE cd.client_id = :userId AND cd.device_id = :deviceId";

            Query query = entityManager.createNativeQuery(queryString);
            query.setParameter("userId", userId);
            query.setParameter("deviceId", deviceId);

            List<Object[]> results = query.getResultList();
            List<AttributeData> data = new ArrayList<>();

            for (Object[] result : results) {
                AttributeData record = new AttributeData();
                record.setDate((Timestamp) result[0]);

                for (int i = 0; i < attributes.size(); i++) {
                    record.addAttribute(attributes.get(i), result[i + 1]);
                }
                data.add(record);
            }

            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceId(deviceId);
            deviceData.setDeviceType(deviceType.name());
            deviceData.setAttributes(data);
            devices.add(deviceData);
        }

        userData.setDevices(devices);
        return userData;
    }

    @Transactional
    public Map<Long, List<Map<String, Object>>> dataByUserIdAndDevicesIds(Long userId, Map<Long, List<String>> deviceAttributesMap) {
        Map<Long, List<Map<String, Object>>> resultData = new HashMap<>();

        for (Map.Entry<Long, List<String>> entry : deviceAttributesMap.entrySet()) {
            Long deviceId = entry.getKey();
            List<String> attributes = entry.getValue();
            List<Map<String, Object>> deviceData = dataByUserIdAndDeviceId(userId, deviceId, attributes);
            resultData.put(deviceId, deviceData);
        }

        return resultData;
    }

    public List<Map<String, Object>> dataByUserIdAndDeviceId(Long userId, Long deviceId, List<String> attributes) {
        EdeviceType deviceType = getDeviceTypeByDeviceId(deviceId);
        String deviceTypeLowercase = deviceType.name().toLowerCase();
        String metadataTable;

        switch (deviceType) {
            case WaterMeter:
                metadataTable = "watermetermetadata";
                break;
            case ElectricityMeter:
                metadataTable = "energymetermetadata";
                break;
            case THL:
                metadataTable = "thlmetadata";
                break;
            default:
                throw new IllegalArgumentException("Invalid device type: " + deviceType);
        }

        String attributesSelect = String.join(", m.", attributes);
        String queryString = "SELECT m.date, m." + attributesSelect + " FROM " + metadataTable + " m " +
                "JOIN " + deviceTypeLowercase + " em ON m." + deviceTypeLowercase + "_id = em.id " +
                "JOIN device d ON em.id = d.id " +
                "JOIN clientdevice cd ON cd.device_id = d.id " +
                "WHERE cd.client_id = :userId AND cd.device_id = :deviceId";

        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("userId", userId);
        query.setParameter("deviceId", deviceId);

        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> record = new HashMap<>();
            record.put("date", result[0]);
            for (int i = 0; i < attributes.size(); i++) {
                record.put(attributes.get(i), result[i + 1]);
            }
            data.add(record);
        }

        return data;
    }

    //FindAllById
    @Transactional
    public List<Device> findAllById(List<Long> deviceIds) {
        TypedQuery<Device> query = entityManager.createQuery("SELECT d FROM Device d WHERE d.id IN :deviceIds", Device.class);
        query.setParameter("deviceIds", deviceIds);
        return query.getResultList();
    }

    //getClientIdByDevicesIds
    @Transactional
    public Client getClientIdByDevicesIds(List<Long> deviceIds) {
        TypedQuery<Client> query = entityManager.createQuery("SELECT cd.client FROM ClientDevice cd WHERE cd.device.id IN :deviceIds", Client.class);
        query.setParameter("deviceIds", deviceIds);
        return query.getSingleResult();
    }

    //Calculate total energy consumption for all users

    /**********************Admin Dashboard *********************/
    @Transactional
    public double calculateTotalEnergyConsumptionForAllUsers() {
        try {
            LOGGER.info("Calculating total energy consumption for all users");

            // Récupérer tous les dispositifs d'électricité (ElectricityMeter)
            TypedQuery<ElectricityMeter> query = entityManager.createQuery(
                    "SELECT e FROM ElectricityMeter e", ElectricityMeter.class);
            List<ElectricityMeter> electricityMeters = query.getResultList();

            double totalConsumption = 0.0;

            for (ElectricityMeter meter : electricityMeters) {
                // Récupérer la dernière lecture pour ce dispositif
                TypedQuery<EnergyMeterMetaData> latestReadingQuery = entityManager.createQuery(
                        "SELECT em FROM EnergyMeterMetaData em WHERE em.electricityMeter.id = :deviceId ORDER BY em.date DESC",
                        EnergyMeterMetaData.class);
                latestReadingQuery.setParameter("deviceId", meter.getId());
                latestReadingQuery.setMaxResults(1);

                EnergyMeterMetaData latestReading = null;
                try {
                    latestReading = latestReadingQuery.getSingleResult();
                } catch (jakarta.persistence.NoResultException e) {
                    LOGGER.warn("No energy meter data found for device ID: {}", meter.getId());
                    continue; // Skip this meter and continue with the next one
                }

                // Calculer la consommation totale pour ce dispositif
                double estimatedEnergy = latestReading.getEnergy_L1() + latestReading.getEnergy_L2() +
                        latestReading.getEnergy_L3() + latestReading.getEnergy_L4() +
                        latestReading.getEnergy_L5() + latestReading.getEnergy_L6();

                // Ajouter à la consommation totale
                totalConsumption += estimatedEnergy;
            }

            LOGGER.info("Total energy consumption for all users is {}", totalConsumption);
            return totalConsumption;
        } catch (Exception e) {
            LOGGER.error("Error calculating total energy consumption for all users", e);
            throw e;
        }
    }

    @Transactional
    public double calculateCarbonFootprintForAllUsers() {
        try {
            LOGGER.info("Calculating carbon footprint for all users");

            // Calculate total energy consumption for all users
            double totalConsumption = calculateTotalEnergyConsumptionForAllUsers();

            // Calculate the carbon footprint
            double carbonFootprint = totalConsumption * ELECTRICITY_EMISSION_FACTOR;

            LOGGER.info("Total carbon footprint for all users is {}", carbonFootprint);
            return carbonFootprint;
        } catch (Exception e) {
            LOGGER.error("Error calculating carbon footprint for all users", e);
            throw e;
        }
    }


    public double calculateSolarEnergyProduced() {

        try {
            LOGGER.info("Calculating solar energy produced");
            TypedQuery<SolarPanel> query = entityManager.createQuery(
                    "SELECT s FROM SolarPanel s", SolarPanel.class);
            List<SolarPanel> solarPanels = query.getResultList();
            double totalEnergyProduced = 0.0;
            for (SolarPanel solar : solarPanels) {
                try {

                } catch (Exception e) {
                    LOGGER.error("Error calculating solar energy produced", e);
                    throw e;
                }
                // Calculate the total energy produced by this solar panel
                double energyProduced = solar.getEfficiency() * solar.getSurfaceArea() * solar.getSolarIntensity();
                totalEnergyProduced += energyProduced;
                LOGGER.info("Energy produced by solar panel {} is {}", solar.getReference(), energyProduced);
            }
            LOGGER.info("Total solar energy produced is {}", totalEnergyProduced);
            return totalEnergyProduced;

        } catch (Exception e) {
            LOGGER.error("Error calculating solar energy produced", e);
            throw e;
        }
    }

    public double SolarEnergyProduced() {

        try {
            LOGGER.info("Calculating solar energy produced");
            TypedQuery<SolarPanel> query = entityManager.createQuery(
                    "SELECT s FROM SolarPanel s", SolarPanel.class);
            List<SolarPanel> solarPanels = query.getResultList();
            double totalEnergyProduced = 0.0;
            for (SolarPanel solar : solarPanels) {
                try {
                    // Calculate the total energy produced by this solar panel
                    double energyProduced = solar.getEfficiency() * solar.getSurfaceArea() * solar.getSolarIntensity();
                    totalEnergyProduced += energyProduced;
                    LOGGER.info("Energy produced by solar panel {} is {}", solar.getReference(), energyProduced);
                } catch (Exception e) {
                    LOGGER.error("Error calculating solar energy produced", e);
                    throw e;
                }

            }
            LOGGER.info("Total solar energy produced is {}", totalEnergyProduced);
            return totalEnergyProduced;

        } catch (Exception e) {
            LOGGER.error("Error calculating solar energy produced", e);
            throw e;
        }
    }
}
