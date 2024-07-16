package org.acme.model.devices;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)

public class SolarPanel extends Device {
    private String reference; // Référence du panneau solaire
    private double efficiency; // Efficacité du panneau solaire en pourcentage (par exemple 20%)
    private double surfaceArea; // Surface du panneau solaire en mètres carrés
    private double co2SavedPerKWh; // CO2 évité par kWh produit en kg CO2/kWh
    private double solarIntensity; // Intensité de la lumière solaire (en kWh/m² par jour)
}
