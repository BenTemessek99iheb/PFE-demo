package org.acme.repository.MetaData;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.model.MetaData.EnergyMeterMetaData;
import org.acme.model.MetaData.WaterMeterMetaData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
@ApplicationScoped

public class WaterMeterMetaDataRepository implements PanacheRepositoryBase<WaterMeterMetaData,Long> {
    @Transactional
    public List<WaterMeterMetaData> findByDeviceIdAndDateAfter(Long deviceId, String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }

        return find("waterMeter.id = ?1 and date >= ?2", deviceId, date).list();
    }
    @Transactional
    public List<WaterMeterMetaData> findByDeviceId(Long deviceId) {
        return find("waterMeter.id", deviceId).list();
    }
}
