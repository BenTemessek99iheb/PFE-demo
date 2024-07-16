package org.acme.model.devices;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.model.Alert;
import org.acme.model.ClientDevice;
import org.acme.model.charts.Graphique;

import java.io.Serializable;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class Device implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private EdeviceType deviceType;
    private String description;
    private String status;
    private String espace;
    private String zone;
    private Integer gateway;
    private String owner;
    private Date installationDate;
    private Date lastMaintenanceDate;
    @OneToMany(mappedBy = "device")
    @JsonIgnore
    private List<ClientDevice> clientDevices;
    @JsonIgnore
    @OneToMany
    private List<Alert> alerts = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "devices", fetch = FetchType.LAZY)
    private Set<Graphique> graphiques = new HashSet<>();

    @Override
    public int hashCode() {
        return Objects.hash(id); // Use only id for hashCode
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(id, device.id); // Use only id for equals
    }

}
