package repositories;

import model.User;
import model.enums.EntityStatus;
import model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // auth lookups
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    //role-based queries
    List<User> findByRole(UserRole role);

    // status filters
    List<User> findByEntityStatus(EntityStatus entityStatus);
    List<User> findByIsActiveTrue();

    // suspension review
    List<User> findBySuspensionReasonIsNotNull();

    // email verification
    List<User> findByEmailVerifiedFalse();

    // trust score/bot protection trust ladder
    List<User> findByTrustScoreLessThan(Integer threshold);

    // neighborhood-based browsing
    List<User> findByNeighborhood(String neighborhood);
}
