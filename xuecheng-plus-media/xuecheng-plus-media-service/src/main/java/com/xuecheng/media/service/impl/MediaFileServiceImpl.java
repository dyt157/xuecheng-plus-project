package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.constant.XcPlusConstant;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
//@PropertySource("classpath:bootstrap.yml")
public class MediaFileServiceImpl
        extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFileService  {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Resource
    private MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String files;//普通文件桶

    @Value("${minio.bucket.videofiles}")
    private String videoFiles;//视频文件桶

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    /**
     * 上传图片方法
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @Override
    public UploadFileResultDto uploadFile(MultipartFile multipartFile) throws Exception{
        //1、上传文件到minio，要不要判断是普通文件还是视频文件？？？
        //存储到minio的文件，格式统一是 yyyy/MM/dd/md5值.后缀名
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        //获取文件的md5值
        String md5Hex = DigestUtils.md5Hex(multipartFile.getBytes());
        //获取文件的后缀名
        String originalFilename = multipartFile.getOriginalFilename();
        int lastIndexOf = originalFilename.lastIndexOf(".");
        String suffix = originalFilename.substring(lastIndexOf);

        String objectName = date + "/" + md5Hex + suffix;//最好用StringBuffer做

        //上传文件如果使用uploadObject方法需要创建服务器本地临时文件才行
        //也就是需要把multipartFile复制一份到临时文件中去
        /*UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket(files)
                .object(date+"/"+md5Hex+suffix)
                .filename()
                .contentType().build();
        minioClient.uploadObject(uploadObjectArgs);*/

        //这里不通过本地文件缓存之后的方式上传，直接通过流上传
        PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(files)
                .object(objectName)
                .stream(multipartFile.getInputStream(), multipartFile.getInputStream().available(), -1)
                .build();
        minioClient.putObject(putObjectArgs);


        //2、上传成功更新数据库的数据,包括media_file teachplan_media
        MediaFiles mediaFiles = new MediaFiles();

        mediaFiles.setId(md5Hex);
        mediaFiles.setCompanyId(12222111L);//先写死
        mediaFiles.setCompanyName("java教育机构");
        mediaFiles.setFilename(originalFilename);
        mediaFiles.setFileType(XcPlusConstant.FILE_TYPE_IMAGE);//图片
        mediaFiles.setTags(XcPlusConstant.COURSE_TAG_IMAGES);
        mediaFiles.setBucket(files);
        mediaFiles.setFilePath(objectName);
        mediaFiles.setFileId(md5Hex);
        mediaFiles.setUrl(files+"/"+objectName);
        mediaFiles.setUsername("张先生");//先写死
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setStatus("1");
        mediaFiles.setAuditStatus(XcPlusConstant.OBJECT_AUDIT_STATUS_UNAUDITED);//默认为未审核??
        mediaFiles.setFileSize(multipartFile.getSize());

        //保存media_file表
        save(mediaFiles);

        //返回UploadFileResultDto对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);

        return uploadFileResultDto;
    }
}
