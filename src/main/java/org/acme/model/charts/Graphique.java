package org.acme.model.charts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.acme.model.Client;
import org.acme.model.devices.Device;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@RequiredArgsConstructor

public class Graphique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String subtitle;
    @JsonIgnore
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(
            name = "graphique_device",
            joinColumns = @JoinColumn(name = "graphique_id"),
            inverseJoinColumns = @JoinColumn(name = "device_id")
    )
    private Set<Device> devices = new HashSet<>();

    private Boolean status = true;
    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnore
    private Client client;
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "deviceId")
    @Column(name = "attributes")
    @CollectionTable(name = "graphique_details", joinColumns = @JoinColumn(name = "graphique_id"))
    private Map<Long, String> yaxisitems;
    private String xAxis;
    @CreationTimestamp
    private LocalDateTime creationDate;
    private EGraphiqueType type;

    public void setDevices(Set<Device> devices) {

        this.devices = devices;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Use only id for hashCode
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Graphique that = (Graphique) o;
        return Objects.equals(id, that.id); // Use only id for equals
    }
}
