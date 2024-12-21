package org.sugar.media.repository.gb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;

/**
 * Date:2024/12/13 10:51:04
 * Author：Tobin
 * Description:
 */

@Repository
public interface DeviceRepo extends JpaRepository<DeviceModel, Long>, JpaSpecificationExecutor<DeviceModel> {

    DeviceModel findAllByDeviceId(String deviceId);


}
