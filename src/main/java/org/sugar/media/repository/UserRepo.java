package org.sugar.media.repository;

import org.sugar.media.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * (MUser)
 *
 * @author Tobin
 * @since 2024-11-10 10:05:25
 */
@Repository
public interface UserRepo extends JpaRepository<UserModel, Long>, JpaSpecificationExecutor<UserModel> {

}
