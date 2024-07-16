package org.acme.repository.MetaData;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.kpis.ActivePowerPhases;
import org.acme.dto.kpis.EnergyPhases;
import org.acme.dto.kpis.HarmonicDistortionPhases;
import org.acme.dto.kpis.PowerFactorPhases;
import org.acme.model.MetaData.EnergyMeterMetaData;
import org.acme.model.devices.Device;
import org.acme.model.devices.ElectricityMeter;
import org.acme.repository.ClientDeviceRepo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class EnergyMeterMetaDataRepository implements PanacheRepositoryBase<EnergyMeterMetaData, Long> {

    @Inject
    ClientDeviceRepo clientDeviceRepo;

    @Transactional
    public List<EnergyMeterMetaData> findByDeviceId(Long deviceId) {
        return find("electricityMeter.id", deviceId).list();
    }


    @Transactional
    public List<EnergyPhases> getEnergyPhasesByDate(Long deviceId, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start;
        Date end;
        try {
            Date parsedStartDate = dateFormat.parse(startDate);
            Date parsedEndDate = dateFormat.parse(endDate);

            // Compare the dates and set the newer date as the start date
            if (parsedStartDate.after(parsedEndDate)) {
                start = parsedEndDate;
                end = parsedStartDate;
            } else {
                start = parsedStartDate;
                end = parsedEndDate;
            }
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }

        List<EnergyMeterMetaData> energyMeterMetaData = find("electricityMeter.id = ?1 and date >= ?2 and date <= ?3", deviceId, start, end).list();
        List<EnergyPhases> energyPhases = new ArrayList<>();
        for (EnergyMeterMetaData energyMeter : energyMeterMetaData) {
            energyPhases.add(new EnergyPhases(
                    energyMeter.getElectricityMeter().getId(),

                    energyMeter.getEnergy_L1(),

                    energyMeter.getEnergy_L2(),
                    energyMeter.getEnergy_L3(),
                    energyMeter.getEnergy_L4(),
                    energyMeter.getEnergy_L5(),
                    energyMeter.getEnergy_L6(),
                    energyMeter.getDate())

            );
        }
        return energyPhases;
    }

    //get aCTIVEpOWERpHASES by Date filter
    @Transactional
    public List<ActivePowerPhases> getActivePowerPhasesByDate(Long deviceId, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start;
        Date end;
        try {
            Date parsedStartDate = dateFormat.parse(startDate);
            Date parsedEndDate = dateFormat.parse(endDate);

            // Compare the dates and set the newer date as the start date
            if (parsedStartDate.after(parsedEndDate)) {
                start = parsedEndDate;
                end = parsedStartDate;
            } else {
                start = parsedStartDate;
                end = parsedEndDate;
            }
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }
        List<EnergyMeterMetaData> energyMeterMetaData = find("electricityMeter.id = ?1 and date >= ?2 and date <= ?3", deviceId, start, end).list();
        List<ActivePowerPhases> energyPhases = new ArrayList<>();
        for (EnergyMeterMetaData energyMeter : energyMeterMetaData) {
            energyPhases.add(new ActivePowerPhases(
                    energyMeter.getElectricityMeter().getId(),
                    energyMeter.getActive_power_L1(),
                    energyMeter.getActive_power_L2(),
                    energyMeter.getActive_power_L3(),
                    energyMeter.getDate())

            );
        }
        return energyPhases;
    }

    // get Harmonic dispatorion by Date filter
    @Transactional
    public List<HarmonicDistortionPhases> getHarmonicDistortionPhasesByDate(Long deviceId, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start;
        Date end;
        try {
            Date parsedStartDate = dateFormat.parse(startDate);
            Date parsedEndDate = dateFormat.parse(endDate);

            // Compare the dates and set the newer date as the start date
            if (parsedStartDate.after(parsedEndDate)) {
                start = parsedEndDate;
                end = parsedStartDate;
            } else {
                start = parsedStartDate;
                end = parsedEndDate;
            }
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }
        List<EnergyMeterMetaData> energyMeterMetaData = find("electricityMeter.id = ?1 and date >= ?2 and date <= ?3", deviceId, start, end).list();
        List<HarmonicDistortionPhases> energyPhases = new ArrayList<>();
        for (EnergyMeterMetaData energyMeter : energyMeterMetaData) {
            energyPhases.add(new HarmonicDistortionPhases(
                    energyMeter.getElectricityMeter().getId(),

                    energyMeter.getTotal_harmonic_distortion_L1(),
                    energyMeter.getTotal_harmonic_distortion_L2(),
                    energyMeter.getTotal_harmonic_distortion_L3(),
                    energyMeter.getTotal_harmonic_distortion_L4(),
                    energyMeter.getTotal_harmonic_distortion_L5(),
                    energyMeter.getTotal_harmonic_distortion_L6(),
                    energyMeter.getDate())

            );
        }
        return energyPhases;
    }

    //PowerFactor
    @Transactional
    public List<PowerFactorPhases> getPowerFactorPhasesByDate(Long deviceId, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start;
        Date end;
        try {
            Date parsedStartDate = dateFormat.parse(startDate);
            Date parsedEndDate = dateFormat.parse(endDate);

            // Compare the dates and set the newer date as the start date
            if (parsedStartDate.after(parsedEndDate)) {
                start = parsedEndDate;
                end = parsedStartDate;
            } else {
                start = parsedStartDate;
                end = parsedEndDate;
            }
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }
        List<EnergyMeterMetaData> energyMeterMetaData = find("electricityMeter.id = ?1 and date >= ?2 and date <= ?3", deviceId, start, end).list();
        List<PowerFactorPhases> energyPhases = new ArrayList<>();
        for (EnergyMeterMetaData energyMeter : energyMeterMetaData) {
            energyPhases.add(new PowerFactorPhases(
                    energyMeter.getElectricityMeter().getId(),
                    energyMeter.getPower_factor_L1(),
                    energyMeter.getPower_factor_L2(),
                    energyMeter.getPower_factor_L3(),
                    energyMeter.getPower_factor_L4(),
                    energyMeter.getPower_factor_L5(),
                    energyMeter.getPower_factor_L6(),
                    energyMeter.getDate())
            );
        }
        return energyPhases;
    }


    /****************************WaterMeter********************************/













    /*********************************Data By USER ID********************************/

    public List<PowerFactorPhases> getPowerFactorPhasesForUser(Long userId, String startDate, String endDate) {
        List<PowerFactorPhases> allPhases = new ArrayList<>();
        List<Device> devices = clientDeviceRepo.devicesByUserId(userId);

        for (Device device : devices) {
            if (device instanceof ElectricityMeter) {
                List<PowerFactorPhases> devicePhases = getPowerFactorPhasesByDate(device.getId(), startDate, endDate);
                allPhases.addAll(devicePhases);
            }
        }
        return allPhases;
    }

    public List<HarmonicDistortionPhases> getHarmonicDistortionPhasesForUser(Long userId, String startDate, String endDate) {
        List<HarmonicDistortionPhases> allPhases = new ArrayList<>();
        List<Device> devices = clientDeviceRepo.devicesByUserId(userId);

        for (Device device : devices) {
            if (device instanceof ElectricityMeter) {
                List<HarmonicDistortionPhases> devicePhases = getHarmonicDistortionPhasesByDate(device.getId(), startDate, endDate);
                allPhases.addAll(devicePhases);
            }
        }
        return allPhases;
    }

    public List<ActivePowerPhases> getActivePowerPhasesPhasesForUser(Long userId, String startDate, String endDate) {
        List<ActivePowerPhases> allPhases = new ArrayList<>();
        List<Device> devices = clientDeviceRepo.devicesByUserId(userId);

        for (Device device : devices) {
            if (device instanceof ElectricityMeter) {
                List<ActivePowerPhases> devicePhases = getActivePowerPhasesByDate(device.getId(), startDate, endDate);
                allPhases.addAll(devicePhases);
            }
        }
        return allPhases;
    }
    public List<EnergyPhases> getEnergyPhasesForUser(Long userId, String startDate, String endDate) {
        List<EnergyPhases> allPhases = new ArrayList<>();
        List<Device> devices = clientDeviceRepo.devicesByUserId(userId);

        for (Device device : devices) {
            if (device instanceof ElectricityMeter) {
                List<EnergyPhases> devicePhases = getEnergyPhasesByDate(device.getId(), startDate, endDate);
                allPhases.addAll(devicePhases);
            }
        }
        return allPhases;
    }

    //get EnergyMeter MetaData by User Id
    public List<EnergyMeterMetaData> getEnergyMeterMetaDataByUserId(Long userId) {
        List<EnergyMeterMetaData> allMetaData = new ArrayList<>();
        List<Device> devices = clientDeviceRepo.devicesByUserId(userId);

        for (Device device : devices) {
            if (device instanceof ElectricityMeter) {
                List<EnergyMeterMetaData> deviceMetaData = findByDeviceId(device.getId());
                allMetaData.addAll(deviceMetaData);
            }
        }
        return allMetaData;
    }
    //countEnergyMetersByUserId

    public int countEnergyMetersByUserId(Long userId) {
        List<Device> devices = clientDeviceRepo.devicesByUserId(userId);
        int count = 0;
        for (Device device : devices) {
            if (device instanceof ElectricityMeter) {
                count++;
            }
        }
        return count;
    }
    @Transactional
    public List<EnergyMeterMetaData> findByDeviceIdAndDateAfter(Long deviceId, String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }

        return find("electricityMeter.id = ?1 and date >= ?2", deviceId, date).list();
    }
}
