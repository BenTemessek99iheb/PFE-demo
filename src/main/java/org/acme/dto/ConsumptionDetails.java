package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumptionDetails {
    private List<Double> electricityConsumption;
    private List<Double> carbonFootprint;
    private List<Double> waterConsumption;
    private List<Double> electirictyCost;
    private List<Double> waterCost;
    private List<Double> solarEnergyProduction;
    private Date date ;
}
