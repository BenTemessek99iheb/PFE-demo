package org.acme.repository.charts;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.acme.model.charts.EGraphiqueType;
import org.acme.model.charts.Graphique;

import org.acme.service.GraphiqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GraphiqueRepository implements PanacheRepository<Graphique> {
    private static final Logger logger = LoggerFactory.getLogger(GraphiqueService.class);

    @Inject
    public EntityManager entityManager;

    @Transactional
    public List<Graphique> getGraphiquesByUserId(Long userId) {
        TypedQuery<Graphique> query = entityManager.createQuery(
                "SELECT DISTINCT g FROM Graphique g " +
                        "JOIN g.devices d " +
                        "JOIN ClientDevice cd ON cd.device.id = d.id " +
                        "WHERE cd.client.id = :userId", Graphique.class);
        query.setParameter("userId", userId);
        List<Graphique> graphiques = query.getResultList();

        return graphiques;
    }

    //getGraphique byUserId
    @Transactional
    public List<Graphique> getGraphiqueByClientId(Long userId) {
        TypedQuery<Graphique> query = entityManager.createQuery(
                "SELECT DISTINCT g FROM Graphique g " +
                        "JOIN FETCH g.devices " +
                        "JOIN g.client c " +
                        "WHERE c.id = :userId", Graphique.class);
        query.setParameter("userId", userId);
        List<Graphique> graphiques = query.getResultList();

        if (graphiques.isEmpty()) {
            logger.warn("No graphiques found for clientId: {}", userId);
        }

        return graphiques;
    }


    public List<Graphique> fetchGraphiquesByDeviceId(Long deviceId) {
        TypedQuery<Graphique> query = entityManager.createQuery(
                "SELECT g FROM Graphique g JOIN g.devices d WHERE d.id = :deviceId", Graphique.class);
        query.setParameter("deviceId", deviceId);
        return query.getResultList();
    }

    @Transactional
    public List<Graphique> getGraphiquesByUserId2(Long userId) {
        TypedQuery<Graphique> query = entityManager.createQuery(
                "SELECT DISTINCT g FROM Graphique g " +
                        "JOIN FETCH g.devices " +
                        "JOIN g.client c " +
                        "WHERE c.id = :userId", Graphique.class);
        query.setParameter("userId", userId);
        List<Graphique> graphiques = query.getResultList();

        if (graphiques.isEmpty()) {
            logger.warn("No graphiques found for clientId: {}", userId);
        }

        return graphiques;
    }


    @Transactional
    public Map<EGraphiqueType, List<Graphique>> getGraphiquesAndTypesByUserId(Long userId) {
        TypedQuery<Graphique> query = entityManager.createQuery(
                "SELECT DISTINCT g FROM Graphique g " +
                        "JOIN FETCH g.devices " +
                        "JOIN g.client c " +
                        "WHERE c.id = :userId", Graphique.class);
        query.setParameter("userId", userId);
        List<Graphique> graphiques = query.getResultList();

        if (graphiques.isEmpty()) {
            logger.warn("No graphiques found for clientId: {}", userId);
        }

        // A map to store graphiques by type
        Map<EGraphiqueType, List<Graphique>> graphiquesByType = new HashMap<>();

        // Iterate over graphiques and categorize them by type
        for (Graphique graphique : graphiques) {
            EGraphiqueType type = graphique.getType();
            graphiquesByType.computeIfAbsent(type, k -> new ArrayList<>()).add(graphique);
        }

        return graphiquesByType;
    }


}
