package com.example.springbootproject.controller;

import cn.hutool.core.io.FileUtil;
import com.aliyun.oss.OSS;
import com.example.springbootproject.pojo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @PostMapping("/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        // 1. 校验非空
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (!List.of("image/png", "image/jpeg", "image/gif", "image/webp").contains(contentType)) {
            return Result.fail("不支持的文件类型");
        }
        // 3. 校验大小
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("文件大小不能超过5MB");
        }
        // 4. 生成 OSS 存储路径
        String ext = FileUtil.extName(file.getOriginalFilename());
        String key = "uploads/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"))
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        // 5. 上传到 OSS
        try {
            ossClient.putObject(bucketName, key, file.getInputStream());
        } catch (Exception e) {
            log.error("OSS上传失败", e);
            return Result.fail("上传失败，请稍后重试");
        }
        // 6. 返回公网访问 URL
        String url = "https://" + bucketName + "." + endpoint + "/" + key;
        return Result.ok(url);
    }
}
