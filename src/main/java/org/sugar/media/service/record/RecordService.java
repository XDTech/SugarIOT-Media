package org.sugar.media.service.record;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.sugar.media.model.record.RecordModel;
import org.sugar.media.repository.record.RecordRepo;

/**
 * Date:2024/12/27 22:41:18
 * Author：Tobin
 * Description:
 */

@Service
public class RecordService {

    @Resource
    private RecordRepo recordRepo;


    public void createRecord(RecordModel recordModel) {

        this.recordRepo.save(recordModel);

    }

}
