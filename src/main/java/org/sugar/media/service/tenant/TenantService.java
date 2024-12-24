package org.sugar.media.service.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.model.TenantModel;
import org.sugar.media.repository.TenantRepo;

import java.util.Optional;

/**
 * Date:2024/12/18 18:15:15
 * Author：Tobin
 * Description:
 */

@Service
public class TenantService {

    @Autowired
    private TenantRepo tenantRepo;



    public Optional<TenantModel> getTenant(Long id){
        return this.tenantRepo.findById(id);
    }

}
