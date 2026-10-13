package com.aicloud.module.infra.api.file;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.infra.api.file.dto.FileCreateReqDTO;
import com.aicloud.module.infra.service.file.FileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.aicloud.framework.common.pojo.CommonResult.success;

@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class FileApiImpl implements FileApi {

    @Resource
    private FileService fileService;

    @Override
    public CommonResult<String> createFile(FileCreateReqDTO createReqDTO) {
        return success(fileService.createFile(createReqDTO.getContent(), createReqDTO.getName(),
                createReqDTO.getDirectory(), createReqDTO.getType()));
    }

    @Override
    public CommonResult<String> presignGetUrl(String url, Integer expirationSeconds) {
        return success(fileService.presignGetUrl(url, expirationSeconds));
    }

    @Override
    public CommonResult<Boolean> registerPrivateFile(String url, Long userId, Long size) {
        fileService.registerPrivateFile(url, userId, size);
        return success(true);
    }

}
