package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnergyQuality {
    public String deviceName;
    public List<Float> energyReadings;
    public double avgEnergyPhases;
    public double avgPowerFactor;
    public double avgTotalHarmonicDistortion;
    public double avgRms;
    public double minReactivePower;
    public double minEnergyPhases;
    public double minPowerFactor;
    public double minTotalHarmonicDistortion;
    public double minRms;
    public double maxReactivePower;
    public double maxEnergyPhases;
    public double maxPowerFactor;
    public double maxTotalHarmonicDistortion;
    public double maxRms;
    public double energyStandardDeviation; //Standard Deviation (Energy Balance Across Phases)
}
