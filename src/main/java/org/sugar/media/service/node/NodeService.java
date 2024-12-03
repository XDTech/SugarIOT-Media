package org.sugar.media.service.node;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.repository.node.NodeRepo;

import java.util.Optional;

/**
 * Date:2024/12/03 10:50:48
 * Author：Tobin
 * Description: node表基础操作service
 */

@Service
public class NodeService {

    @Autowired
    private NodeRepo nodeRepo;


    public Optional<NodeModel> getNode(Long id) {

        return this.nodeRepo.findById(id);
    }



    public void dibbleSowing(){

    }

}
