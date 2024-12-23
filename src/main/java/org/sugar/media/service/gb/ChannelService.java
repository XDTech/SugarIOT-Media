package org.sugar.media.service.gb;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.repository.gb.ChannelRepo;

import java.util.List;
import java.util.Optional;

/**
 * Date:2024/12/21 11:41:54
 * Author：Tobin
 * Description:
 */

@Service
public class ChannelService {

    @Resource
    private ChannelRepo channelRepo;


    public List<DeviceChannelModel> getDeviceChannelList(Long deviceId) {

        return this.channelRepo.findByDeviceId(deviceId);


    }

    public Optional<DeviceChannelModel> getChannel(Long channelId) {

        return this.channelRepo.findById(channelId);


    }

    @Transactional
    public void createChannel(List<DeviceChannelModel> channelModels) {
        this.channelRepo.saveAll(channelModels);

    }




    @Transactional
    public void deleteAll(Long deviceId){
        this.channelRepo.deleteAllByDeviceId(deviceId);
    }

}
