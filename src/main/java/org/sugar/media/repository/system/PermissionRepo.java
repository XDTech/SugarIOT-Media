package org.sugar.media.repository.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.sugar.media.model.system.PermissionModel;

import java.util.List;

/**
 * (Permission)
 *
 * @author Tobin
 * @since 2025-01-20 09:53:44
 */
@Repository
public interface PermissionRepo extends JpaRepository<PermissionModel, Long>, JpaSpecificationExecutor<PermissionModel> {


    PermissionModel findByIdentity(String identity);


    List<PermissionModel> findByIdIn(List<Long> permissionIds);
    List<PermissionModel> findAllByDeleted(boolean flag);


}
