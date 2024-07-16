package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PowerFactorDto {
    private Long userId;
    private Date startDate;
    private Date endDate;
    private double powerFactor;
}
