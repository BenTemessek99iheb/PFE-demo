package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class TotalEnergyConsumptionDto {
    private Double energy_L1;
    private Double energy_L2;
    private Double energy_L3;
    private Double energy_L4;
    private Double energy_L5;
    private Double energy_L6;
    private Double energyConsumption;

}
