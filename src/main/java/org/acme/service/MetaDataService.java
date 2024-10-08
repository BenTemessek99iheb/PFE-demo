package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.*;
import org.acme.dto.kpis.ActivePowerPhases;
import org.acme.dto.kpis.EnergyPhases;
import org.acme.dto.kpis.HarmonicDistortionPhases;
import org.acme.dto.kpis.PowerFactorPhases;
import org.acme.model.Alert;
import org.acme.model.MetaData.EnergyMeterMetaData;
import org.acme.model.MetaData.THLMetaData;
import org.acme.model.MetaData.WaterMeterMetaData;
import org.acme.model.devices.*;
import org.acme.repository.AlertRepo;
import org.acme.repository.ClientDeviceRepo;
import org.acme.repository.MetaData.EnergyMeterMetaDataRepository;
import org.acme.repository.MetaData.ThlMetaDataRepository;
import org.acme.repository.MetaData.WaterMeterMetaDataRepository;
import org.acme.repository.devices.DeviceRepository;
import org.acme.util.tools.Exceptions.AlertNotFoundException;
import org.acme.util.tools.Exceptions.DeviceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
public class MetaDataService {
    @Inject
    EnergyMeterMetaDataRepository energyMeterMetaDataRepository;
    @Inject
    WaterMeterMetaDataRepository waterMeterMetaDataRepository;

    @Inject
    ThlMetaDataRepository thlMetaDataRepository;
    @Inject
    AlertRepo alertRepo;
    @Inject
    ClientDeviceRepo clientDeviceRepo;

    @Inject
    DeviceRepository deviceRepo;
    private static final double ELECTRICITY_EMISSION_FACTOR = 0.0115;
    private static final double ENERGY_COST_PER_KWH = 0.352;
    private static final double WATER_COST_PER_M3 = 0.5; // Example value, update accordingly
    private static final double WATER_EMISSION_FACTOR = 0.0003; // Example value, update accordingly
    private static final Logger logger = LoggerFactory.getLogger(MetaDataService.class);

    @Transactional
    public List<EnergyMeterMetaData> findByDeviceId(Long deviceId) {
        logger.info("Attempting to find energy meter metadata for device ID: {}", deviceId);
        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.findByDeviceId(deviceId);
        logger.info("Found {} energy meter readings for device ID: {}", readings.size(), deviceId);
        return readings;
    }

    @Transactional
    public List<EnergyMeterMetaData> getEmReadings(Long deviceId) {
        logger.info("Attempting to get energy meter readings for device ID: {}", deviceId);
        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.findByDeviceId(deviceId);
        logger.info("Retrieved {} energy meter readings for device ID: {}", readings.size(), deviceId);
        return readings;
    }

    @Transactional
    public List<WaterMeterMetaData> getWmReadings(Long deviceId) {
        logger.info("Attempting to get water meter readings for device ID: {}", deviceId);
        List<WaterMeterMetaData> readings = waterMeterMetaDataRepository.findByDeviceId(deviceId);
        logger.info("Retrieved {} water meter readings for device ID: {}", readings.size(), deviceId);
        return readings;
    }
    @Transactional
    public List<THLMetaData> getTHLReadings(Long deviceId) {
        logger.info("Attempting to get water meter readings for device ID: {}", deviceId);
        List<THLMetaData> readings = thlMetaDataRepository.findByDeviceId(deviceId);
        logger.info("Retrieved {} water meter readings for device ID: {}", readings.size(), deviceId);
        return readings;
    }

    @Transactional
    public void addReading(Long deviceId, EnergyMeterMetaData reading) {
        logger.info("Attempting to add energy meter reading for device ID: {}", deviceId);
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof ElectricityMeter)) {
            logger.error("Electricity meter not found with ID: {}", deviceId);
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }
        ElectricityMeter electricityMeter = (ElectricityMeter) device;
        reading.setElectricityMeter(electricityMeter);
        energyMeterMetaDataRepository.persist(reading);
        logger.info("Successfully added energy meter reading for device ID: {}", deviceId);
    }

    @Transactional
    public void addWmReading(Long deviceId, WaterMeterMetaData reading) {
        logger.info("Attempting to add water meter reading for device ID: {}", deviceId);
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof WaterMeter)) {
            logger.error("Water meter not found with ID: {}", deviceId);
            throw new DeviceNotFoundException("Water meter not found with ID: " + deviceId);
        }
        WaterMeter waterMeter = (WaterMeter) device;
        reading.setWaterMeter(waterMeter);
        waterMeterMetaDataRepository.persist(reading);
        logger.info("Successfully added water meter reading for device ID: {}", deviceId);
    }

    @Transactional
    public void addTHlReading(Long deviceId, THLMetaData reading) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof THL)) {
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }
        THL thl = (THL) device;
        reading.setThl(thl);
        thlMetaDataRepository.persist(reading);
    }

    @Transactional
    public DeviceConsumption_cout_carbone calculateConsumption_cout_carbone_em(Long deviceId) {
        logger.info("Calculating consumption, cost, and carbon footprint for device ID: {}", deviceId);
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof ElectricityMeter)) {
            logger.error("Electricity meter not found with ID: {}", deviceId);
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }
        ElectricityMeter electricityMeter = (ElectricityMeter) device;
        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.findByDeviceId(deviceId);

        double totalConsumption = calculateTotalConsumption(readings);
        logger.info("Total consumption for device ID {}: {}", deviceId, totalConsumption);

        double energyCost = totalConsumption * ENERGY_COST_PER_KWH;
        double carbonFootprint = totalConsumption * ELECTRICITY_EMISSION_FACTOR;
        logger.info("Energy cost for device ID {}: {}", deviceId, energyCost);
        logger.info("Carbon footprint for device ID {}: {}", deviceId, carbonFootprint);

        BigDecimal formattedTotalConsumption = BigDecimal.valueOf(totalConsumption).setScale(3, RoundingMode.HALF_UP);
        BigDecimal formattedEnergyCost = BigDecimal.valueOf(energyCost).setScale(3, RoundingMode.HALF_UP);
        BigDecimal formattedCarbonFootprint = BigDecimal.valueOf(carbonFootprint).setScale(3, RoundingMode.HALF_UP);

        DeviceConsumption_cout_carbone result = new DeviceConsumption_cout_carbone(
                electricityMeter.getName(),
                electricityMeter.getDeviceType(),
                electricityMeter.getZone(),
                electricityMeter.getEspace(),
                formattedTotalConsumption.doubleValue(),
                formattedEnergyCost.doubleValue(),
                formattedCarbonFootprint.doubleValue() // exprimée en kg CO2e
        );

        logger.info("Calculated DeviceConsumption_cout_carbone for device ID {}: {}", deviceId, result);
        return result;
    }

    private double calculateTotalConsumption(List<EnergyMeterMetaData> readings) {
        double totalConsumption = 0.0;
        for (EnergyMeterMetaData reading : readings) {
            logger.debug("Processing reading: {}", reading);

            // Calcul consumption based on current and previous readings
            if (reading.getCurrentReading() != null && reading.getPreviousReading() != null) {
                double currentConsumption = reading.getCurrentReading() - reading.getPreviousReading();
                totalConsumption += currentConsumption;
                logger.debug("Added consumption from current and previous readings: {}", currentConsumption);
            }

            if (reading.getEstimated_energy_L1() != null) {
                totalConsumption += reading.getEstimated_energy_L1();
                logger.debug("Added estimated energy L1: {}", reading.getEstimated_energy_L1());
            }
            if (reading.getEstimated_energy_L2() != null) {
                totalConsumption += reading.getEstimated_energy_L2();
                logger.debug("Added estimated energy L2: {}", reading.getEstimated_energy_L2());
            }
            if (reading.getEstimated_energy_L3() != null) {
                totalConsumption += reading.getEstimated_energy_L3();
                logger.debug("Added estimated energy L3: {}", reading.getEstimated_energy_L3());
            }
            if (reading.getEstimated_energy_L4() != null) {
                totalConsumption += reading.getEstimated_energy_L4();
                logger.debug("Added estimated energy L4: {}", reading.getEstimated_energy_L4());
            }
            if (reading.getEstimated_energy_L5() != null) {
                totalConsumption += reading.getEstimated_energy_L5();
                logger.debug("Added estimated energy L5: {}", reading.getEstimated_energy_L5());
            }
            if (reading.getEstimated_energy_L6() != null) {
                totalConsumption += reading.getEstimated_energy_L6();
                logger.debug("Added estimated energy L6: {}", reading.getEstimated_energy_L6());
            }
        }
        logger.info("Total calculated consumption: {}", totalConsumption);
        return totalConsumption;
    }

    //calculate consumption cout carbone by DEVOICE id
    @Transactional
    public List<DeviceConsumption_cout_carbone> calculateConsumption_cout_carbone_ByUserId(Long userId) {
        List<Device> devices = deviceRepo.getDevicesByUserId(userId);
        return devices.stream()
                .map(device -> calculateConsumption_cout_carbone_ByDeviceId(device.getId()))
                .collect(Collectors.toList());
    }

    public DeviceConsumption_cout_carbone calculateConsumption_cout_carbone_ByDeviceId(Long deviceId) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null) {
            throw new DeviceNotFoundException("Device not found with ID: " + deviceId);
        }
        if (device instanceof ElectricityMeter) {
            return calculateConsumption_cout_carbone_em(deviceId);
        } else if (device instanceof WaterMeter) {
            return calculateConsumption_cout_carbone_wm(deviceId);

        } else if (device instanceof SolarPanel) {
            return calculateConsumption_cout_carbone_sp(deviceId);

        } else {
            throw new DeviceNotFoundException("Device not found with ID: " + deviceId);
        }
    }

    @Transactional
    public DeviceConsumption_cout_carbone calculateConsumption_cout_carbone_wm(Long deviceId) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof WaterMeter)) {
            throw new DeviceNotFoundException("Water meter not found with ID: " + deviceId);
        }
        WaterMeter waterMeter = (WaterMeter) device;
        List<WaterMeterMetaData> readings = waterMeterMetaDataRepository.findByDeviceId(deviceId);

        double totalConsumption = calculateTotalWaterConsumption(readings);
        double waterCost = calculateWaterCost(totalConsumption);
        double carbonFootprint = totalConsumption * WATER_EMISSION_FACTOR;

        BigDecimal formattedTotalConsumption = BigDecimal.valueOf(totalConsumption).setScale(3, RoundingMode.HALF_UP);
        BigDecimal formattedWaterCost = BigDecimal.valueOf(waterCost).setScale(3, RoundingMode.HALF_UP);
        BigDecimal formattedCarbonFootprint = BigDecimal.valueOf(carbonFootprint).setScale(3, RoundingMode.HALF_UP);

        return new DeviceConsumption_cout_carbone(
                waterMeter.getName(),
                waterMeter.getDeviceType(),
                waterMeter.getZone(),
                waterMeter.getEspace(),
                formattedTotalConsumption.doubleValue(),
                formattedWaterCost.doubleValue(),
                formattedCarbonFootprint.doubleValue()
        );
    }

    private double calculateTotalWaterConsumption(List<WaterMeterMetaData> readings) {
        double totalConsumption = 0.0;
        for (WaterMeterMetaData reading : readings) {
            totalConsumption += reading.getForwardFlow();
            totalConsumption += reading.getConsoH();
            totalConsumption += reading.getConsoJ();
            totalConsumption += reading.getConsoM();
        }
        return totalConsumption;
    }

    private double calculateTotalWaterConsumptionFromDate(List<WaterMeterMetaData> readings, String startDate) {
        double totalConsumption = 0.0;
        for (WaterMeterMetaData reading : readings) {
            totalConsumption += reading.getForwardFlow();
            totalConsumption += reading.getConsoH();
            totalConsumption += reading.getConsoJ();
            totalConsumption += reading.getConsoM();
        }
        return totalConsumption;
    }

    private double calculateWaterCost(double totalConsumption) {
        double cost = 0.0;

        if (totalConsumption <= 20) {
            cost = totalConsumption * 0.740;
        } else if (totalConsumption <= 40) {
            cost = 20 * 0.740 + (totalConsumption - 20) * 1.040;
        } else if (totalConsumption <= 70) {
            cost = 20 * 0.740 + 20 * 1.040 + (totalConsumption - 40) * 1.490;
        } else {
            cost = 20 * 0.740 + 20 * 1.040 + 30 * 1.490 + (totalConsumption - 70) * 1.490;
        }

        return cost;
    }

    //total Water Consumption ByuserId
    public GreenTotalConsumptionWmEmCo2Sp totalWaterAndElectricityConsumptionAndCarbonEviteByUserId(Long userId) {
        GreenTotalConsumptionWmEmCo2Sp totals = new GreenTotalConsumptionWmEmCo2Sp();
        List<Device> devices = deviceRepo.getDevicesByUserId(userId);

        double totalEnergyConsumption = 0.0;
        double totalWaterConsumption = 0.0;
        double totalCO2Avoided = 0.0;
        double totalSolarEnergyProduced = 0.0;
        boolean hasSolarPanel = false;

        for (Device device : devices) {
            if (device instanceof SolarPanel) {
                SolarPanel solarPanel = (SolarPanel) device;
                double energyProducedBySolarPanel = calculateEnergyProducedBySolarPanel(solarPanel);
                totalSolarEnergyProduced += energyProducedBySolarPanel;
                totalCO2Avoided += energyProducedBySolarPanel * solarPanel.getCo2SavedPerKWh();
                hasSolarPanel = true;
            } else if (device instanceof ElectricityMeter) {
                double energyConsumption = calculateTotalEnergyConsumption(device.getId());
                totalEnergyConsumption += energyConsumption;
                totalCO2Avoided += calculateTotalCO2Avoided(device.getId());
            } else if (device instanceof WaterMeter) {
                double waterConsumption = calculateTotalWaterConsumption(device.getId());
                totalWaterConsumption += waterConsumption;
                totalCO2Avoided += calculateTotalCO2Avoided(device.getId());
            }
        }

        if (hasSolarPanel) {
            double energySavedBySolarPanels = totalSolarEnergyProduced;
            totalEnergyConsumption -= energySavedBySolarPanels;
        }

        // Calculate the solar energy percentage
        double solarEnergyPercentage = (totalSolarEnergyProduced / (totalEnergyConsumption + totalSolarEnergyProduced)) * 100;

        // Calculate total CO2 emission
        double totalCo2Emission = (totalEnergyConsumption * ELECTRICITY_EMISSION_FACTOR) + (totalWaterConsumption * WATER_EMISSION_FACTOR);

        // Round to three decimal places
        totals.setTotalenergyConsumtion(roundToThreeDecimalPlaces(totalEnergyConsumption));
        totals.setTotalwaterConsumtion(roundToThreeDecimalPlaces(totalWaterConsumption));
        totals.setTotalco2Emission(roundToThreeDecimalPlaces(totalCo2Emission));
        totals.setTotalCO2Avoided(roundToThreeDecimalPlaces(totalCO2Avoided));
        totals.setTotalSolarEnergyProduced(roundToThreeDecimalPlaces(totalSolarEnergyProduced));
        totals.setSolarEnergyPercentage(roundToThreeDecimalPlaces(solarEnergyPercentage));

        return totals;
    }

    private double roundToThreeDecimalPlaces(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    // Méthode pour calculer l'énergie produite par un panneau solaire
    private double calculateTotalEnergyConsumption(Long deviceId) {
        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.findByDeviceId(deviceId);
        return calculateTotalConsumption(readings);
    }

    private double calculateTotalWaterConsumption(Long deviceId) {
        List<WaterMeterMetaData> readings = waterMeterMetaDataRepository.findByDeviceId(deviceId);
        return calculateTotalWaterConsumption(readings);
    }

    private double calculateTotalCO2Avoided(Long deviceId) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null) {
            throw new DeviceNotFoundException("Device not found with ID: " + deviceId);
        }
        if (device instanceof ElectricityMeter) {
            return calculateTotalEnergyConsumption(deviceId) * ELECTRICITY_EMISSION_FACTOR;
        } else if (device instanceof WaterMeter) {
            return calculateTotalWaterConsumption(deviceId) * WATER_EMISSION_FACTOR;
        } else {
            throw new DeviceNotFoundException("Device not found with ID: " + deviceId);
        }
    }

    public double calculateEnergyProducedBySolarPanel(SolarPanel solarPanel) {
        // Convert efficiency from percentage to fraction
        double efficiencyFraction = solarPanel.getEfficiency() / 80.0;
        // Calculate energy produced
        return efficiencyFraction * solarPanel.getSurfaceArea() * solarPanel.getSolarIntensity();
    }

    @Transactional
    public double calculateEnergyProducedBySolarPanelByUserId(Long userId) {
        List<Device> devices = deviceRepo.getDevicesByUserId(userId);

        double totalEnergyProduced = 0.0;

        for (Device device : devices) {
            if (device instanceof SolarPanel) {
                SolarPanel solarPanel = (SolarPanel) device;
                double energyProducedBySolarPanel = calculateEnergyProducedBySolarPanel(solarPanel);
                totalEnergyProduced += energyProducedBySolarPanel;
            }
        }

        return totalEnergyProduced;
    }

    //
    @Transactional
    public DeviceConsumption_cout_carbone calculateConsumption_cout_carbone_sp(Long deviceId) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof SolarPanel)) {
            throw new DeviceNotFoundException("Solar panel not found with ID: " + deviceId);
        }
        SolarPanel solarPanel = (SolarPanel) device;

        double energyProduced = calculateEnergyProducedBySolarPanel(solarPanel);
        double co2Saved = energyProduced * solarPanel.getCo2SavedPerKWh();

        BigDecimal formattedEnergyProduced = BigDecimal.valueOf(energyProduced).setScale(3, RoundingMode.HALF_UP);
        BigDecimal formattedCo2Saved = BigDecimal.valueOf(co2Saved).setScale(3, RoundingMode.HALF_UP);

        return new DeviceConsumption_cout_carbone(
                solarPanel.getName(),
                solarPanel.getDeviceType(),
                solarPanel.getZone(),
                solarPanel.getEspace(),
                formattedEnergyProduced.doubleValue(),
                0.0,
                formattedCo2Saved.doubleValue() // exprimée en kg CO2e
        );
    }

    //*********************************** EnergyMeter Kpis*****************************************************

    public List<EnergyPhases> getEnergyPhasesByDate(Long deviceId, String startDate, String endDate) {
        // Check if the device exists
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof ElectricityMeter)) {
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }


        return energyMeterMetaDataRepository.getEnergyPhasesByDate(deviceId, startDate, endDate);
    }

    public List<ActivePowerPhases> getActivePowerPhasesByDate(Long deviceId, String startDate, String endDate) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof ElectricityMeter)) {
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }

        return energyMeterMetaDataRepository.getActivePowerPhasesByDate(deviceId, startDate, endDate);
    }

    public List<HarmonicDistortionPhases> getHarmonicDistortionPhasesByDate(Long deviceId, String startDate, String endDate) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof ElectricityMeter)) {
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }


        return energyMeterMetaDataRepository.getHarmonicDistortionPhasesByDate(deviceId, startDate, endDate);
    }

    //
    public List<PowerFactorPhases> getPowerFactorPhasesByDate(Long deviceId, String startDate, String endDate) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof ElectricityMeter)) {
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }

        return energyMeterMetaDataRepository.getPowerFactorPhasesByDate(deviceId, startDate, endDate);

    }

    /***************************Data By User Id********************************/

    public List<PowerFactorPhases> powerFactorPhasesForUser(Long userId, String startDate, String endDate) {


        return energyMeterMetaDataRepository.getPowerFactorPhasesForUser(userId, startDate, endDate);

    }

    public List<HarmonicDistortionPhases> harmonicDistortionPhasesForUser(Long userId, String startDate, String endDate) {


        return energyMeterMetaDataRepository.getHarmonicDistortionPhasesForUser(userId, startDate, endDate);

    }

    public List<ActivePowerPhases> activePowerPhasesForUser(Long userId, String startDate, String endDate) {


        return energyMeterMetaDataRepository.getActivePowerPhasesPhasesForUser(userId, startDate, endDate);

    }

    public List<EnergyPhases> energyPhasesForUser(Long userId, String startDate, String endDate) {


        return energyMeterMetaDataRepository.getEnergyPhasesForUser(userId, startDate, endDate);

    }

    //count Energy Meters by userid
    public Integer countEnergyMetersByUserId(Long userId) {
        return energyMeterMetaDataRepository.countEnergyMetersByUserId(userId);
    }

    //getEnergyMeterMetaDataByUserId
    public List<EnergyMeterMetaData> getEnergyMeterMetaDataByUserId(Long userId) {
        return energyMeterMetaDataRepository.getEnergyMeterMetaDataByUserId(userId);
    }

    /*************************************    Alert   **************************************************/
    //Add Alert value on total Consumption
    public List<Alert> getAlertsByDeviceId(Long deviceId) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null) {
            throw new DeviceNotFoundException("Device not found with ID: " + deviceId);
        }
        return device.getAlerts();
    }

    // getAlert ByUserId
    public List<Alert> getAlertsByUserId(Long userId) {
        List<Device> devices = deviceRepo.getDevicesByUserId(userId);
        return devices.stream()
                .map(Device::getAlerts)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }


    // Method to calculate total energy consumption from Alert.datedebut
    @Transactional
    public double calculateTotalEnergyConsumptionFromStartDate(Long deviceId, String startDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.findByDeviceId(deviceId);
        double totalConsumption = 0.0;
        for (EnergyMeterMetaData reading : readings) {
            // Calculate consumption based on current and previous readings
            if (reading.getCurrentReading() != null && reading.getPreviousReading() != null) {
                totalConsumption += reading.getCurrentReading() - reading.getPreviousReading();
            }
            if (reading.getEstimated_energy_L1() != null) totalConsumption += reading.getEstimated_energy_L1();
            if (reading.getEstimated_energy_L2() != null) totalConsumption += reading.getEstimated_energy_L2();
            if (reading.getEstimated_energy_L3() != null) totalConsumption += reading.getEstimated_energy_L3();
            if (reading.getEstimated_energy_L4() != null) totalConsumption += reading.getEstimated_energy_L4();
            if (reading.getEstimated_energy_L5() != null) totalConsumption += reading.getEstimated_energy_L5();
            if (reading.getEstimated_energy_L6() != null) totalConsumption += reading.getEstimated_energy_L6();
        }

        return totalConsumption;
    }

    public double calculateTotalActivePowerFromStartDate(Long deviceId, String startDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start;
        try {
            start = dateFormat.parse(startDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd.", e);
        }

        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.findByDeviceId(deviceId);
        double totalActivePower = 0.0;
        for (EnergyMeterMetaData reading : readings) {
            if (reading.getDate().after(start)) {
                totalActivePower += (reading.getActive_power_L1() != null ? reading.getActive_power_L1() : 0) +
                        (reading.getActive_power_L2() != null ? reading.getActive_power_L2() : 0) +
                        (reading.getActive_power_L3() != null ? reading.getActive_power_L3() : 0);
            }
        }
        return totalActivePower;
    }

    public double calculateTotalPowerFactorFromStartDate(Long deviceId, String startDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start;
        try {
            start = dateFormat.parse(startDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd.", e);
        }

        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.findByDeviceId(deviceId);
        double totalPowerFactor = 0.0;
        int count = 0;
        for (EnergyMeterMetaData reading : readings) {
            if (reading.getDate().after(start)) {
                totalPowerFactor += (reading.getPower_factor_L1() != null ? reading.getPower_factor_L1() : 0) +
                        (reading.getPower_factor_L2() != null ? reading.getPower_factor_L2() : 0) +
                        (reading.getPower_factor_L3() != null ? reading.getPower_factor_L3() : 0) +
                        (reading.getPower_factor_L4() != null ? reading.getPower_factor_L4() : 0) +
                        (reading.getPower_factor_L5() != null ? reading.getPower_factor_L5() : 0) +
                        (reading.getPower_factor_L6() != null ? reading.getPower_factor_L6() : 0);
                count += 6;
            }
        }
        return count > 0 ? totalPowerFactor / count : 0.0;
    }

    // add alert
    public Alert addAlert(Alert alert, Long deviceId) {
        logger.info("Attempting to add alert for device ID: {}", deviceId);
        Device device = deviceRepo.findById(deviceId);
        if (device == null) {
            logger.error("Device not found with ID: {}", deviceId);
            throw new DeviceNotFoundException("Device not found with ID: " + deviceId);
        }
        alert.setDevice(device);
        alertRepo.persist(alert);
        logger.info("Successfully added alert for device ID: {}", deviceId);
        return alert;
    }

    public Alert updateAlert(Alert alert) {
        logger.info("Attempting to update alert with ID: {}", alert.getId());
        Alert existingAlert = alertRepo.findById(alert.getId());
        if (existingAlert == null) {
            logger.error("Alert not found with ID: {}", alert.getId());
            throw new AlertNotFoundException("Alert not found with ID: " + alert.getId());
        }
        existingAlert.setName(alert.getName());
        existingAlert.setDescription(alert.getDescription());
        existingAlert.setTag(alert.getTag());
        existingAlert.setAttribute(alert.getAttribute());
        existingAlert.setDatedebut(alert.getDatedebut());
        existingAlert.setValue(alert.getValue());
        alertRepo.persist(existingAlert);
        logger.info("Successfully updated alert with ID: {}", alert.getId());
        return existingAlert;
    }


    public List<Alert> getAlerts() {
        logger.info("Attempting to retrieve all alerts");
        List<Alert> alerts = alertRepo.listAll();
        logger.info("Successfully retrieved {} alerts", alerts.size());
        return alerts;
    }


    @Transactional
    public double calculateTotalWaterConsumptionFromStartDate(Long deviceId, String startDate) {
        List<WaterMeterMetaData> readings = waterMeterMetaDataRepository.findByDeviceIdAndDateAfter(deviceId, startDate);
        double totalConsumption = 0.0;
        for (WaterMeterMetaData reading : readings) {
            totalConsumption += reading.getForwardFlow();
            totalConsumption += reading.getConsoH();
            totalConsumption += reading.getConsoJ();
            totalConsumption += reading.getConsoM();
        }
        return totalConsumption;
    }

    public double calculateTotalWaterFlowFromStartDate(Long deviceId, String startDate) {

        List<WaterMeterMetaData> readings = waterMeterMetaDataRepository.findByDeviceIdAndDateAfter(deviceId, startDate);

        double totalFlow = 0.0;
        for (WaterMeterMetaData reading : readings) {
            totalFlow += reading.getForwardFlow();
        }
        return totalFlow;
    }
/************************************/
// Calculate GreenTotalConsumptionWmEmCo2Sp foreach User's Device
public List<GreenTotalConsumptionWmEmCo2Sp> carbonTracker(Long userId) {
    List<GreenTotalConsumptionWmEmCo2Sp> totalsList = new ArrayList<>();
    List<Device> devices = deviceRepo.getDevicesByUserId(userId);

    double totalEnergyProducedBySolarPanels = 0.0;
    double totalEnergyConsumptionByElectricityMeters = 0.0;

    // First pass: Calculate total solar energy produced and total energy consumed
    for (Device device : devices) {
        if (device instanceof SolarPanel) {
            SolarPanel solarPanel = (SolarPanel) device;
            totalEnergyProducedBySolarPanels += calculateEnergyProducedBySolarPanel(solarPanel);
        } else if (device instanceof ElectricityMeter) {
            ElectricityMeter electricityMeter = (ElectricityMeter) device;
            totalEnergyConsumptionByElectricityMeters += calculateTotalEnergyConsumption(electricityMeter.getId());
        }
    }

    double totalEnergy = totalEnergyProducedBySolarPanels + totalEnergyConsumptionByElectricityMeters;

    // Second pass: Populate totals for each device
    for (Device device : devices) {
        GreenTotalConsumptionWmEmCo2Sp totals = new GreenTotalConsumptionWmEmCo2Sp();

        if (device instanceof SolarPanel) {
            SolarPanel solarPanel = (SolarPanel) device;
            double energyProducedBySolarPanel = calculateEnergyProducedBySolarPanel(solarPanel);
            double co2Avoided = energyProducedBySolarPanel * solarPanel.getCo2SavedPerKWh();

            totals.setDevicename(solarPanel.getName());
            totals.setTotalSolarEnergyProduced(roundToThreeDecimalPlaces(energyProducedBySolarPanel));
            totals.setTotalCO2Avoided(roundToThreeDecimalPlaces(co2Avoided));
            totals.setSolarEnergyPercentage(roundToThreeDecimalPlaces(totalEnergy > 0 ? (energyProducedBySolarPanel / totalEnergy) * 100 : 0.0));

            // Fields not applicable to SolarPanel
            totals.setTotalenergyConsumtion(0.0);
            totals.setTotalwaterConsumtion(0.0);
            totals.setTotalco2Emission(0.0);
        } else if (device instanceof ElectricityMeter) {
            ElectricityMeter electricityMeter = (ElectricityMeter) device;
            double energyConsumption = calculateTotalEnergyConsumption(electricityMeter.getId());
            double co2Emission = energyConsumption * ELECTRICITY_EMISSION_FACTOR;
            double co2Avoided = calculateTotalCO2Avoided(electricityMeter.getId());

            totals.setDevicename(electricityMeter.getName());
            totals.setTotalenergyConsumtion(roundToThreeDecimalPlaces(energyConsumption));
            totals.setTotalco2Emission(roundToThreeDecimalPlaces(co2Emission));
            totals.setTotalCO2Avoided(roundToThreeDecimalPlaces(co2Avoided));
            totals.setSolarEnergyPercentage(0.0); // Not applicable to ElectricityMeter

            // Fields not applicable to ElectricityMeter
            totals.setTotalwaterConsumtion(0.0);
            totals.setTotalSolarEnergyProduced(0.0);
        } else if (device instanceof WaterMeter) {
            WaterMeter waterMeter = (WaterMeter) device;
            double waterConsumption = calculateTotalWaterConsumption(waterMeter.getId());
            double co2Avoided = calculateTotalCO2Avoided(waterMeter.getId());

            totals.setDevicename(waterMeter.getName());
            totals.setTotalwaterConsumtion(roundToThreeDecimalPlaces(waterConsumption));
            totals.setTotalCO2Avoided(roundToThreeDecimalPlaces(co2Avoided));

            // Fields not applicable to WaterMeter
            totals.setTotalenergyConsumtion(0.0);
            totals.setTotalSolarEnergyProduced(0.0);
            totals.setSolarEnergyPercentage(0.0);
            totals.setTotalco2Emission(0.0);
        } else {
            logger.warn("Unknown device type for device ID: {}", device.getId());
            continue;
        }

        totalsList.add(totals);
    }

    return totalsList;
}

    @Transactional
    public List<EnergyQuality> getEnergyQuality(Long userId) {
        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.getEnergyMeterMetaDataByUserId(userId);
        logger.info("Attempting to get energy meter readings for user ID: {}", userId);

        Map<ElectricityMeter, List<EnergyMeterMetaData>> readingsByMeter = readings.stream()
                .collect(Collectors.groupingBy(EnergyMeterMetaData::getElectricityMeter));

        List<EnergyQuality> energyQualities = new ArrayList<>();

        for (Map.Entry<ElectricityMeter, List<EnergyMeterMetaData>> entry : readingsByMeter.entrySet()) {
            ElectricityMeter meter = entry.getKey();
            List<EnergyMeterMetaData> meterReadings = entry.getValue();

            EnergyQuality energyQuality = new EnergyQuality();
            List<Float> energyReadings = new ArrayList<>();
            double totalEnergy = 0;
            double totalPowerFactor = 0;
            double totalHarmonicDistortion = 0;
            double totalRms = 0;
            double totalReactivePower = 0;

            double minEnergy = Double.MAX_VALUE;
            double maxEnergy = Double.MIN_VALUE;
            double minPowerFactor = Double.MAX_VALUE;
            double maxPowerFactor = Double.MIN_VALUE;
            double minHarmonicDistortion = Double.MAX_VALUE;
            double maxHarmonicDistortion = Double.MIN_VALUE;
            double minRms = Double.MAX_VALUE;
            double maxRms = Double.MIN_VALUE;
            double minReactivePower = Double.MAX_VALUE;
            double maxReactivePower = Double.MIN_VALUE;

            for (EnergyMeterMetaData reading : meterReadings) {
                energyReadings.add(reading.getCurrentReading().floatValue());

                // Calculate energy for all phases
                double energy = reading.getEnergy_L1() + reading.getEnergy_L2() + reading.getEnergy_L3()
                        + reading.getEnergy_L4() + reading.getEnergy_L5() + reading.getEnergy_L6();
                totalEnergy += energy;

                minEnergy = Math.min(minEnergy, energy);
                maxEnergy = Math.max(maxEnergy, energy);

                // Calculate power factor for all phases
                double powerFactor = (reading.getPower_factor_L1() + reading.getPower_factor_L2() + reading.getPower_factor_L3()
                        + reading.getPower_factor_L4() + reading.getPower_factor_L5() + reading.getPower_factor_L6()) / 6;
                totalPowerFactor += powerFactor;

                minPowerFactor = Math.min(minPowerFactor, powerFactor);
                maxPowerFactor = Math.max(maxPowerFactor, powerFactor);

                // Calculate total harmonic distortion for all phases
                double harmonicDistortion = (reading.getTotal_harmonic_distortion_L1() + reading.getTotal_harmonic_distortion_L2() + reading.getTotal_harmonic_distortion_L3()
                        + reading.getTotal_harmonic_distortion_L4() + reading.getTotal_harmonic_distortion_L5() + reading.getTotal_harmonic_distortion_L6()) / 6;
                totalHarmonicDistortion += harmonicDistortion;

                minHarmonicDistortion = Math.min(minHarmonicDistortion, harmonicDistortion);
                maxHarmonicDistortion = Math.max(maxHarmonicDistortion, harmonicDistortion);

                // Calculate RMS for all phases (current and voltage)
                double rms = (reading.getRms_current_L1() + reading.getRms_current_L2() + reading.getRms_current_L3()
                        + reading.getRms_voltage_L1() + reading.getRms_voltage_L2() + reading.getRms_voltage_L3()) / 6;
                totalRms += rms;

                minRms = Math.min(minRms, rms);
                maxRms = Math.max(maxRms, rms);

                // Calculate reactive power for all phases (if applicable)
                double reactivePower = (reading.getReactive_energy_hour_L1() + reading.getReactive_energy_hour_L2() + reading.getReactive_energy_hour_L3()
                        + reading.getReactive_energy_day_L1() + reading.getReactive_energy_day_L2() + reading.getReactive_energy_day_L3()
                        + reading.getReactive_energy_month_L1() + reading.getReactive_energy_month_L2() + reading.getReactive_energy_month_L3()) / 9;
                totalReactivePower += reactivePower;

                minReactivePower = Math.min(minReactivePower, reactivePower);
                maxReactivePower = Math.max(maxReactivePower, reactivePower);
            }

            int count = meterReadings.size();

            // Set energy quality values
            energyQuality.deviceName = meter.getName(); // The device name for the current meter
            energyQuality.energyReadings = energyReadings;
            energyQuality.avgEnergyPhases = roundToThreeDecimalPlaces(totalEnergy / count);
            energyQuality.avgPowerFactor = roundToThreeDecimalPlaces(totalPowerFactor / count);
            energyQuality.avgTotalHarmonicDistortion = roundToThreeDecimalPlaces(totalHarmonicDistortion / count);
            energyQuality.avgRms = roundToThreeDecimalPlaces(totalRms / count);
            energyQuality.minReactivePower = roundToThreeDecimalPlaces(minReactivePower);
            energyQuality.minEnergyPhases = roundToThreeDecimalPlaces(minEnergy);
            energyQuality.minPowerFactor = roundToThreeDecimalPlaces(minPowerFactor);
            energyQuality.minTotalHarmonicDistortion = roundToThreeDecimalPlaces(minHarmonicDistortion);
            energyQuality.minRms = roundToThreeDecimalPlaces(minRms);
            energyQuality.maxReactivePower = roundToThreeDecimalPlaces(maxReactivePower);
            energyQuality.maxEnergyPhases = roundToThreeDecimalPlaces(maxEnergy);
            energyQuality.maxPowerFactor = roundToThreeDecimalPlaces(maxPowerFactor);
            energyQuality.maxTotalHarmonicDistortion = roundToThreeDecimalPlaces(maxHarmonicDistortion);
            energyQuality.maxRms = roundToThreeDecimalPlaces(maxRms);
            energyQuality.energyStandardDeviation = roundToThreeDecimalPlaces(calculateEnergyStandardDeviation(energyReadings, energyQuality.avgEnergyPhases));

            energyQualities.add(energyQuality);
        }

        return energyQualities;
    }


    private double calculateEnergyStandardDeviation(List<Float> energyReadings, double avgEnergyPhases) {
        double variance = 0;
        for (Float energy : energyReadings) {
            variance += Math.pow(energy - avgEnergyPhases, 2);
        }
        variance /= energyReadings.size();
        return Math.sqrt(variance);
    }


    private Double calculateAverage(List<? extends Number> values) {
        return values.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0.0);
    }

    public List<ThlData> getListOfthlReadings(Long userId){
        List<THLMetaData> readings = thlMetaDataRepository.getTHLMetaDataByUserId(userId);
        List<ThlData> thlDataList = new ArrayList<>();
        for (THLMetaData reading : readings) {
            ThlData thlData = new ThlData();
            thlData.setTemperature(reading.getTemperature());
            thlData.setHumidity(reading.getHumidity());
            thlData.setLuminosity(reading.getLight());
            thlData.setDeviceName(reading.getThl().getName());
            thlData.setDate(reading.getDate());

            thlDataList.add(thlData);
        }

        return thlDataList;
    }
    public List<ThlDataPercentage> calculateThlDataPercentage(Long userId) {
        List<ThlData> thlDataList = getListOfthlReadings(userId);
        List<ThlDataPercentage> thlDataPercentageList = new ArrayList<>();

        double sumTemp = 0.0;
        double sumHum = 0.0;
        double sumLum = 0.0;

        // Calculate total sums for temperature, humidity, and luminosity
        for (ThlData thlData : thlDataList) {
            sumTemp += thlData.getTemperature();
            sumHum += thlData.getHumidity();
            sumLum += thlData.getLuminosity();
        }

        // Calculate percentages and populate ThlDataPercentage objects
        for (ThlData thlData : thlDataList) {
            ThlDataPercentage thlDataPercentage = new ThlDataPercentage();
            thlDataPercentage.setDeviceName(thlData.getDeviceName());
            thlDataPercentage.setDate(thlData.getDate());

            if (sumTemp != 0) {
                thlDataPercentage.setPercentageTemperature((thlData.getTemperature() * 100) / sumTemp);
            } else {
                thlDataPercentage.setPercentageTemperature(0.0);
            }

            if (sumHum != 0) {
                thlDataPercentage.setPercentageHumidity((thlData.getHumidity() * 100) / sumHum);
            } else {
                thlDataPercentage.setPercentageHumidity(0.0);
            }

            if (sumLum != 0) {
                thlDataPercentage.setPercentageLight((thlData.getLuminosity() * 100) / sumLum);
            } else {
                thlDataPercentage.setPercentageLight(0.0);
            }

            thlDataPercentageList.add(thlDataPercentage);
        }

        return thlDataPercentageList;
    }
    public THLavg calculateAverageTHL(Long userId) {
        List<ThlData> thlDataList = getListOfthlReadings(userId);
        int nbthl = thlMetaDataRepository.countTHLByUserId(userId)  ;


        double sumTemp = 0.0;
        double sumHum = 0.0;
        double sumLum = 0.0;
        int count = thlDataList.size();

        // Calculate total sums for temperature, humidity, and luminosity
        for (ThlData thlData : thlDataList) {
            sumTemp += thlData.getTemperature();
            sumHum += thlData.getHumidity();
            sumLum += thlData.getLuminosity();
        }

        // Calculate averages
        double avgTemp = (count != 0) ? (sumTemp / count) : 0.0;
        double avgHum = (count != 0) ? (sumHum / count) : 0.0;
        double avgLum = (count != 0) ? (sumLum / count) : 0.0;

        // Round to 3 decimal places
        avgTemp = roundToThreeDecimalPlaces(avgTemp);
        avgHum = roundToThreeDecimalPlaces(avgHum);
        avgLum = roundToThreeDecimalPlaces(avgLum);

        // Create and return THLavg object
        return new THLavg(avgTemp, avgHum, avgLum, nbthl);
    }


}
