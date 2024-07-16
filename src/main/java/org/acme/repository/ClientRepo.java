package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.Client;

@ApplicationScoped

public class ClientRepo implements PanacheRepositoryBase<Client, Long> {
//findById
    public Client findByClientId(Long clientId) {
        return find("id", clientId).firstResult();
    }
}
