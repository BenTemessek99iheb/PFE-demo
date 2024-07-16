package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GreenTotalConsumptionWmEmCo2Sp {
    private double totalenergyConsumtion;
    private double totalwaterConsumtion;
    private double totalco2Emission;
    private double totalCO2Avoided; // Ajout de cette ligne pour stocker le total de CO2 évité
    private double solarEnergyPercentage;
    private double totalSolarEnergyProduced;
    @Override
    public String toString() {
        return "GreenTotalConsumptionWmEmCo2Sp{" +
                "totalEnergyConsumption=" + totalenergyConsumtion + " kWh" +
                ", totalWaterConsumption=" + totalwaterConsumtion + " m3" +
                ", totalCo2Emission=" + totalco2Emission + " kg/CO2" +
                ", totalCO2Avoided=" + totalCO2Avoided + " kg/CO2" +
                ", totalSolarEnergyProduced=" + totalSolarEnergyProduced + " kWh" +
                ", solarEnergyPercentage=" + solarEnergyPercentage + " %" +
                '}';
    }
}
