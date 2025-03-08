package org.sugar.media.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.sugar.media.beans.ResponseBean;

import java.io.File;
import java.io.IOException;

/**
 * Date:2025/01/23 12:10:47
 * Author：Tobin
 * Description:
 */


@Service
public class FileService {


    @Value("${spring.upload.path}")
    private String uploadPath;


    public String getFilePath(){
        return this.uploadPath;
    }

    public ResponseBean upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();// 原文件名
        // 如果没有后缀名 统一按照txt文件处理
        String suffixName = "";
        try {
            suffixName = fileName.substring(fileName.lastIndexOf("."));// 文件后缀
        } catch (Exception e) {
            suffixName = ".txt";
        }

        // 带日期目录的文件地址

        String dateFilePath = StrUtil.format("{}/{}{}", DateUtil.format(DateUtil.date(), "yyyy/MM/dd"), new Snowflake().nextIdStr(), suffixName);

        File dir = new File(uploadPath + dateFilePath);

        if (!FileUtil.exist(dir)) {
            dir.mkdirs();
        }
        try {
            file.transferTo(dir);
            // return ResponseEntity.ok("\"" + dateFilePath + "\"");//
            // 返回带引号的字符串，前台接收json可以自动转义
            return ResponseBean.success(dateFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseBean.fail("上传失败");
        }

    }
}
