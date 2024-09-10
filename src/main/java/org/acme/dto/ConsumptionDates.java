package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumptionDates {
    private Double consumption;
    private Date date;
    private Double carbonFootprint;
    private Double cost;
}
