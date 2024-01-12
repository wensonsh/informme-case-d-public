package de.dh.informme.hl7Mock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface Hl7MockRepository extends JpaRepository<Hl7Mock, String> {

    /**
     * find a HL7 message by its MSH-Segment identifier
     *
     * @param mshId MSH-Segment identifier
     * @return found HL7Mock object
     */
    Optional<Hl7Mock> findByMshId(String mshId);

    /**
     * find a random HL7Mock object
     *
     * @return random HL7Mock object
     */
    @Query(value = "SELECT * FROM hl7_mocks ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Hl7Mock findRandom();

}
