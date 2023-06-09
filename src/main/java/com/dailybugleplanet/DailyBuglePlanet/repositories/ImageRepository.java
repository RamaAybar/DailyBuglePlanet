/*
// Curso Egg FullStack
 */
package com.dailybugleplanet.DailyBuglePlanet.repositories;


import com.dailybugleplanet.DailyBuglePlanet.entities.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Ramiro Aybar
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, String> {

    @Query("SELECT i FROM Image i WHERE i.id = :id")
    Image searchById(@Param("id") String id);
}
