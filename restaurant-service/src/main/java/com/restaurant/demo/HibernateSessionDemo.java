package com.restaurant.demo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.restaurant.model.Beverage;
import com.restaurant.model.MenuItem;

import jakarta.persistence.EntityManagerFactory;

/**
 * Demonstrates the core NATIVE Hibernate API, which Spring Data JPA normally hides from us.
 *
 *  SessionFactory - created ONCE when the application starts. Heavy, thread-safe object that holds
 *                   the database configuration and all entity mappings. It is a factory for Sessions.
 *  Session        - a short-lived, NOT thread-safe unit of work with the database (wraps a JDBC
 *                   connection). Used to save, load, update, delete and query entities.
 *                   (The JPA name for Session is EntityManager.)
 *  Transaction    - groups several operations so they either ALL succeed (commit) or ALL fail (rollback).
 *
 * In Spring Boot the SessionFactory already exists inside the JPA EntityManagerFactory,
 * so we just "unwrap" it.
 */
@Component
public class HibernateSessionDemo {

    private final SessionFactory sessionFactory;

    public HibernateSessionDemo(EntityManagerFactory entityManagerFactory) {
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    }

    public Map<String, Object> run() {
        List<String> steps = new ArrayList<>();
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. open a Session from the SessionFactory (try-with-resources closes it automatically)
        try (Session session = sessionFactory.openSession()) {
            steps.add("1. Opened a Session from the SessionFactory");

            // 2. begin a Transaction
            Transaction tx = session.beginTransaction();
            steps.add("2. Began a Transaction");
            try {
                // 3. save a new entity -> INSERT
                Beverage demo = new Beverage("Session Demo Lemonade", "temporary row", 49.0, "Drinks", 250, true);
                session.persist(demo);
                session.flush();
                steps.add("3. session.persist() -> INSERT, generated id = " + demo.getId());

                // 4. HQL with a named parameter
                List<MenuItem> cheapItems = session
                        .createQuery("FROM MenuItem m WHERE m.price < :limit ORDER BY m.price", MenuItem.class)
                        .setParameter("limit", 100.0)
                        .getResultList();
                steps.add("4. HQL 'FROM MenuItem m WHERE m.price < :limit' returned " + cheapItems.size() + " item(s)");
                result.put("itemsUnder100", cheapItems.stream().map(MenuItem::getName).toList());

                // 5. HQL aggregate on a subclass
                Long foodCount = session.createQuery("SELECT COUNT(f) FROM FoodItem f", Long.class)
                        .getSingleResult();
                steps.add("5. HQL 'SELECT COUNT(f) FROM FoodItem f' = " + foodCount);
                result.put("foodItemCount", foodCount);

                // 6. delete the demo row again -> DELETE (so the demo leaves the database unchanged)
                session.remove(demo);
                steps.add("6. session.remove() -> DELETE of the demo row");

                tx.commit();
                steps.add("7. Transaction committed");
            } catch (RuntimeException ex) {
                tx.rollback();
                steps.add("Transaction rolled back because: " + ex.getMessage());
                throw ex;
            }
        }
        steps.add("8. Session closed");
        result.put("steps", steps);
        return result;
    }
}
