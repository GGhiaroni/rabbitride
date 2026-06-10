package com.gghiaroni.rabbitride.rentalservice.rental;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface RentalRepository extends JpaRepository<Rental, UUID> {
    Page<Rental> findByUserId(UUID userId, Pageable pageable);
    boolean existsByUserIdAndStatusIn(UUID userId, Collection<StatusRental> statuses);
}
