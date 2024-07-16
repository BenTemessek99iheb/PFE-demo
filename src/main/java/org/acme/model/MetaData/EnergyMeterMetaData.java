package org.acme.model.MetaData;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.model.devices.ElectricityMeter;

import java.io.Serializable;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "energymetermetadata")

public class EnergyMeterMetaData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private ElectricityMeter electricityMeter;
    private Double currentReading;
    private Double previousReading;
    private Date date;
    private Double energy_L1;
    private Double energy_L2;
    private Double energy_L3;
    private Double energy_L4;
    private Double energy_L5;
    private Double energy_L6;
    private Double power_factor_L1;
    private Double power_factor_L2;
    private Double power_factor_L3;
    private Double power_factor_L4;
    private Double power_factor_L5;
    private Double power_factor_L6;
    private Double total_harmonic_distortion_L1;
    private Double total_harmonic_distortion_L2;
    private Double total_harmonic_distortion_L3;
    private Double total_harmonic_distortion_L4;
    private Double total_harmonic_distortion_L5;
    private Double total_harmonic_distortion_L6;
    private Double rms_current_L1;
    private Double rms_current_L2;
    private Double rms_current_L3;
    private Double rms_voltage_L1;
    private Double rms_voltage_L2;
    private Double rms_voltage_L3;
    private Double active_power_L1;
    private Double active_power_L2;
    private Double active_power_L3;
    private Double rssi;
    private Double snr;
    private Double ctr;
    private Double battery_level;
    private Double estimated_energy_L1;
    private Double estimated_energy_L2;
    private Double estimated_energy_L3;
    private Double estimated_energy_L4;
    private Double estimated_energy_L5;
    private Double estimated_energy_L6;
    private Double nsh;
    private Double tts;
    private Double di;
    private Double accumulated_energy_hour_L1;
    private Double accumulated_energy_hour_L2;
    private Double accumulated_energy_hour_L3;
    private Double accumulated_energy_day_L1;
    private Double accumulated_energy_day_L2;
    private Double accumulated_energy_day_L3;
    private Double accumulated_energy_month_L1;
    private Double accumulated_energy_month_L2;
    private Double accumulated_energy_month_L3;
    private Double reactive_energy_hour_L1;
    private Double reactive_energy_hour_L2;
    private Double reactive_energy_hour_L3;
    private Double reactive_energy_day_L1;
    private Double reactive_energy_day_L2;
    private Double reactive_energy_day_L3;
    private Double reactive_energy_month_L1;
    private Double reactive_energy_month_L2;
    private Double reactive_energy_month_L3;
    private Double degrees;
}
