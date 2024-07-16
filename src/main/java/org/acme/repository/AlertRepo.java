package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.Alert;
@ApplicationScoped
public class AlertRepo implements PanacheRepositoryBase<Alert, Long> {
//findById
    public Alert findByAlertId(Long alertId) {
        return find("id", alertId).firstResult();
    }


}
