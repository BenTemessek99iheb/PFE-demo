package org.acme.dto.kpis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HarmonicDistortionPhases {
    private Long deviceId;
    private Double total_harmonic_distortion_L1;
    private Double total_harmonic_distortion_L2;
    private Double total_harmonic_distortion_L3;
    private Double total_harmonic_distortion_L4;
    private Double total_harmonic_distortion_L5;
    private Double total_harmonic_distortion_L6;
    private Date date;
}
