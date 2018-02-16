package com.dryadandnaiad.sethlans.repositories;

import com.dryadandnaiad.sethlans.domains.database.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created Mario Estrella on 2/16/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
