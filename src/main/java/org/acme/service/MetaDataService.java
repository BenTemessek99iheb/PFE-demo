package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.DeviceConsumption_cout_carbone;
import org.acme.dto.GreenTotalConsumptionWmEmCo2Sp;
import org.acme.dto.kpis.ActivePowerPhases;
import org.acme.dto.kpis.EnergyPhases;
import org.acme.dto.kpis.HarmonicDistortionPhases;
import org.acme.dto.kpis.PowerFactorPhases;
import org.acme.model.Alert;
import org.acme.model.Client;
import org.acme.model.MetaData.EnergyMeterMetaData;
import org.acme.model.MetaData.THLMetaData;
import org.acme.model.MetaData.WaterMeterMetaData;
import org.acme.model.devices.*;
import org.acme.repository.AlertRepo;
import org.acme.repository.MetaData.EnergyMeterMetaDataRepository;
import org.acme.repository.MetaData.ThlMetaDataRepository;
import org.acme.repository.MetaData.WaterMeterMetaDataRepository;
import org.acme.repository.devices.DeviceRepository;
import org.acme.util.tools.Exceptions.DeviceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
    DeviceRepository deviceRepo;
    private static final double ELECTRICITY_EMISSION_FACTOR = 0.0115;
    private static final double ENERGY_COST_PER_KWH = 0.352;
    private static final double WATER_COST_PER_M3 = 0.5; // Example value, update accordingly
    private static final double WATER_EMISSION_FACTOR = 0.0003; // Example value, update accordingly

    @Transactional
    public List<EnergyMeterMetaData> findByDeviceId(Long deviceId) {
        return energyMeterMetaDataRepository.findByDeviceId(deviceId);
    }

    @Transactional
    public List<EnergyMeterMetaData> getEmReadings(Long deviceId) {
        return energyMeterMetaDataRepository.findByDeviceId(deviceId);
    }

    @Transactional
    public List<WaterMeterMetaData> getWmReadings(Long deviceId) {
        return waterMeterMetaDataRepository.findByDeviceId(deviceId);
    }

    @Transactional
    public void addReading(Long deviceId, EnergyMeterMetaData reading) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof ElectricityMeter)) {
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }
        ElectricityMeter electricityMeter = (ElectricityMeter) device;
        reading.setElectricityMeter(electricityMeter);
        energyMeterMetaDataRepository.persist(reading);
    }

    @Transactional
    public void addWmReading(Long deviceId, WaterMeterMetaData reading) {
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof WaterMeter)) {
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }
        WaterMeter waterMeter = (WaterMeter) device;
        reading.setWaterMeter(waterMeter);
        waterMeterMetaDataRepository.persist(reading);
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
        Device device = deviceRepo.findById(deviceId);
        if (device == null || !(device instanceof ElectricityMeter)) {
            throw new DeviceNotFoundException("Electricity meter not found with ID: " + deviceId);
        }
        ElectricityMeter electricityMeter = (ElectricityMeter) device;
        List<EnergyMeterMetaData> readings = energyMeterMetaDataRepository.findByDeviceId(deviceId);

        double totalConsumption = calculateTotalConsumption(readings);
        double energyCost = totalConsumption * ENERGY_COST_PER_KWH;
        double carbonFootprint = totalConsumption * ELECTRICITY_EMISSION_FACTOR;

        BigDecimal formattedTotalConsumption = BigDecimal.valueOf(totalConsumption).setScale(3, RoundingMode.HALF_UP);
        BigDecimal formattedEnergyCost = BigDecimal.valueOf(energyCost).setScale(3, RoundingMode.HALF_UP);
        BigDecimal formattedCarbonFootprint = BigDecimal.valueOf(carbonFootprint).setScale(3, RoundingMode.HALF_UP);

        return new DeviceConsumption_cout_carbone(
                electricityMeter.getName(),
                electricityMeter.getDeviceType(),
                electricityMeter.getZone(),
                electricityMeter.getEspace(),
                formattedTotalConsumption.doubleValue(),
                formattedEnergyCost.doubleValue(),
                formattedCarbonFootprint.doubleValue() // exprimée en kg CO2e
        );
    }

    private double calculateTotalConsumption(List<EnergyMeterMetaData> readings) {
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

    //save Alert by UserId


    //saveAlert by UserId and Device Id  on total Consumption Starting from date

/*
    public Alert scheduleAlertOnEm (Long userId, Alert alert){

        List<Device> devices = deviceRepo.getDevicesByUserId(userId);
        for (Device device : devices) {
            if (device instanceof ElectricityMeter) {
                if (alert.getAttribute()=="totalConsumption"){
                    checkTotalConsumptionAlert(alert,device.getId())==true{
                        return alert;
                    }

                }
                if (alert.getAttribute()=="activePower"){
                    checkActivePowerAlert(alert,device.getId())==true;
                    return alert;
                }
                if (alert.getAttribute()=="powerFactor"){
                    checkPowerFactorAlert(alert,device.getId())==true;
                    return alert;
                }
                else
                    throw new DeviceNotFoundException("Attribute not found with ID: " + alert.getAttribute());

        }else if (device instanceof WaterMeter) {
            if (alert.getAttribute()=="totalConsumption"){
                checkTotalWaterConsumptionAlert(alert,device.getId())==true;

            }
            if (alert.getAttribute()=="forwardFlow"){
                checkForwardFlowAlert(alert,device.getId())==true;
            }
            else
                throw new DeviceNotFoundException("Attribute not found with ID: " + alert.getAttribute());

    }else if (device instanceof THL) {
        if (alert.getAttribute()=="temperature"){
            checkTemperatureAlert(alert,device.getId())==true;
        }
        if (alert.getAttribute()=="humidity"){
            checkHumidityAlert(alert,device.getId())==true;
        }
        if (alert.getAttribute()=="luminosity"){
            checkLuminosityAlert(alert,device.getId())==true;
        }
        else
            throw new DeviceNotFoundException("Attribute not found with ID: " + alert.getAttribute());
        }
            }
        return alert;

    }*/
// Method to calculate total energy consumption from Alert.datedebut
@Transactional
    public double calculateTotalEnergyConsumptionFromStartDate(Long deviceId, String startDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
     /*   Date start;
        try {
            start = dateFormat.parse(startDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd.", e);
        }*/

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
        Device device = deviceRepo.findById(deviceId);
        if (device == null) {
            throw new DeviceNotFoundException("Device not found with ID: " + deviceId);
        }
        alert.setDevice(device);

        alertRepo.persist(alert);
        return alert;
    }

    public Alert updateAlert(Alert alert) {
    Alert alert1 = alertRepo.findById(alert.getId());
    alert1.setName(alert.getName());
    alert1.setDescription(alert.getDescription());
    alert1.setTag(alert.getTag());
    alert1.setAttribute(alert.getAttribute());
    alert1.setDatedebut(alert.getDatedebut());
    alert1.setValue(alert.getValue());
    alertRepo.persist(alert1);

    return alert1;
    }

    public List<Alert> getAlerts() {
    AlertRepo alertRepo = new AlertRepo();
    return alertRepo.listAll();

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



}
