package com.xuecheng.media.api;

import com.alibaba.nacos.client.utils.AppNameUtils;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    private MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams,
                                       @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);

    }

    @PostMapping("/upload/coursefile")
    @ApiOperation("上传媒体(图片)文件接口")
    public UploadFileResultDto uploadFile(@RequestParam("filedata") MultipartFile file) throws Exception {
        return mediaFileService.uploadFile(file);
    }

    @PostMapping("/upload/checkfile")
    @ApiOperation("检查视频文件是否存在")
    public RestResponse checkFile(String fileMd5){
        return mediaFileService.checkFileExist(fileMd5);
    }

    @ApiOperation("检查分块是否存在")
    @PostMapping("upload/checkchunk")
    public RestResponse checkChunk(String fileMd5,Integer chunk){
        return mediaFileService.checkChunk(fileMd5,chunk);
    }

    @PostMapping("upload/uploadchunk")
    @ApiOperation("上传分块文件")
    public RestResponse uploadChunk(MultipartFile file,String fileMd5,Integer chunk){
        return mediaFileService.uploadChunk(file,fileMd5,chunk);

    }

    @PostMapping("upload/mergechunks")
    @ApiOperation("合并分块文件")
    public RestResponse mergeChunk(String fileMd5,String fileName,Integer chunkTotal){
        return mediaFileService.mergeChunk(fileMd5,fileName,chunkTotal);
    }


}
