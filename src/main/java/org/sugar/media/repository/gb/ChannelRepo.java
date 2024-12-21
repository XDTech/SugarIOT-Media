package org.sugar.media.repository.gb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.model.gb.DeviceChannelModel;

import java.util.Date;
import java.util.List;

/**
 * Date:2024/12/21 11:40:51
 * Author：Tobin
 * Description:
 */


@Repository
public interface ChannelRepo extends JpaRepository<DeviceChannelModel, Long>, JpaSpecificationExecutor<DeviceChannelModel> {

    List<DeviceChannelModel> findByDeviceId(Long deviceId);



    void deleteAllByDeviceId(Long deviceId);

}
