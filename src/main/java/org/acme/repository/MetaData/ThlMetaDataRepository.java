package org.acme.repository.MetaData;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.model.MetaData.EnergyMeterMetaData;
import org.acme.model.MetaData.THLMetaData;

import java.util.List;

@ApplicationScoped

public class ThlMetaDataRepository implements PanacheRepositoryBase<THLMetaData, Long> {
    @Transactional
    public List<THLMetaData> findByDeviceId(Long deviceId) {
        return find("thl.id", deviceId).list();
    }
}
