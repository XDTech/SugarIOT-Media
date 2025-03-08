package org.sugar.media.repository.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.sugar.media.model.system.RoleModel;

import java.util.List;

/**
 * (Role)
 *
 * @author Tobin
 * @since 2025-01-19 11:30:15
 */
@Repository
public interface RoleRepo extends JpaRepository<RoleModel, Long>, JpaSpecificationExecutor<RoleModel> {


    RoleModel findByIdentity(String identity);


    List<RoleModel> findByDeleted(boolean flag);


    List<RoleModel> findByIdIn(Long[] roleIds);

}
