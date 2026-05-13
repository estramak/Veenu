package com.veenu.veenu_backend.repository;

import com.veenu.veenu_backend.model.MagicLinkToken;
import com.veenu.veenu_backend.model.TokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, Long> {

    Optional<MagicLinkToken> findByTokenAndPurpose(String token, TokenPurpose purpose);

    void deleteByEmail(String email);
}
