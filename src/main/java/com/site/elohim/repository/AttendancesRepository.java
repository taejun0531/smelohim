package com.site.elohim.repository;

import com.site.elohim.model.Attendances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendancesRepository extends JpaRepository<Attendances, Long> {


}
