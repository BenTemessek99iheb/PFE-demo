package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.acme.model.devices.SolarPanel;

public class SolarPanelRepository implements PanacheRepositoryBase<SolarPanel, Long> {
}
