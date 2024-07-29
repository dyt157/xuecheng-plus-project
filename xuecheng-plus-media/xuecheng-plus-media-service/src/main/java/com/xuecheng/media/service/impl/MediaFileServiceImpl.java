package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.constant.XcPlusConstant;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
//@PropertySource("classpath:bootstrap.yml")
public class MediaFileServiceImpl
        extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFileService  {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Resource
    private  MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String files;//普通文件桶

    @Value("${minio.bucket.videofiles}")
    private String videoFiles;//视频文件桶


    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId,
                                                  PageParams pageParams,
                                                  QueryMediaParamsDto queryMediaParamsDto) {

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
        //1、上传文件到minio
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


        //2、上传成功更新数据库的数据,media_file
        MediaFiles mediaFiles = getById(md5Hex);
        if (mediaFiles==null){//不需要重复添加
            mediaFiles = new MediaFiles();

            mediaFiles.setId(md5Hex);
            mediaFiles.setCompanyId(12222111L);//先写死
            mediaFiles.setCompanyName("java教育机构");
            mediaFiles.setFilename(originalFilename);
            mediaFiles.setFileType(XcPlusConstant.FILE_TYPE_IMAGE);//图片
            mediaFiles.setTags(XcPlusConstant.COURSE_TAG_IMAGES);
            mediaFiles.setBucket(files);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setFileId(md5Hex);
            mediaFiles.setUrl("/"+files+"/"+objectName);
            mediaFiles.setUsername("张先生");//先写死
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus(XcPlusConstant.OBJECT_AUDIT_STATUS_UNAUDITED);//默认为未审核??
            mediaFiles.setFileSize(multipartFile.getSize());

            //保存media_file表
            save(mediaFiles);
        }


        //返回UploadFileResultDto对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);

        return uploadFileResultDto;
    }

    @Override
    public RestResponse checkFileExist(String fileMd5) {
        //前端把即将上传的视频的md5值发送过来进行验证
        //1、检查media_file中是否存在记录
        MediaFiles mediaFiles = getById(fileMd5);
        if (mediaFiles!=null){//数据库中存在记录

            //2、检查minio中有没有对应的文件
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(videoFiles)
                    .object(mediaFiles.getFilePath()).build();
            GetObjectResponse getObjectResponse = null;
            try {
                getObjectResponse = minioClient.getObject(getObjectArgs);
                //文件已存在就返回true，否则返回false
                log.info("getObjectResponse对象：{}",getObjectResponse);
                return getObjectResponse!=null?RestResponse.success(true):RestResponse.success(false);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    if (getObjectResponse!=null)  getObjectResponse.close();  //流一定不要忘记关闭，否则会出错！！！
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //程序能到这里也说明文件不存在，返回false
        return RestResponse.success(false);


    }

    @Override
    public RestResponse checkChunk(String fileMd5, Integer chunk) {
        //检查分块是否存在
        //参数chunk:指的是分块的序号
        //通过前端传过来的md5值可以获取到这个视频文件在minio中的存放路径
        String object  = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/"+chunk;

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(videoFiles).object(object)
                .build();
        GetObjectResponse getObjectResponse = null;
        try {
            getObjectResponse = minioClient.getObject(getObjectArgs);
            log.info("chunk:{},getObjectResponse对象：{}",chunk,getObjectResponse);
            //程序能到这里，说明没有抛出异常，说明分块存在，返回true
            return RestResponse.success(true);
        } catch (Exception e) {//这里有异常，可能是文件不存在
            System.out.println(e.getMessage());
        }finally {
            try {
                if (getObjectResponse!=null)  getObjectResponse.close();  //流一定不要忘记关闭，否则会出错！！！
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //程序能到这里，说明文件不存在，返回false
        return RestResponse.success(false);

    }

    @Override
    public RestResponse uploadChunk(MultipartFile file, String fileMd5, Integer chunk) {
        //上传分块文件
        String objectName  = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/"+chunk;
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(videoFiles)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getInputStream().available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.success(false);
        }

        //上传成功返回true
        return RestResponse.success(true);
    }

    /**
     * 合并分块
     * @param fileMd5 文件md5值
     * @param fileName 文件名
     * @param chunkTotal 分块总数，用于合并以及删除
     * @return
     */
    @Override
    @Transactional
    public RestResponse mergeChunk(String fileMd5, String fileName, Integer chunkTotal) {
        //合并分块文件
        ArrayList<ComposeSource> composeSourceList = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            String chunkName  = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/"+i;
            ComposeSource composeSource = ComposeSource.builder().bucket(videoFiles).object(chunkName).build();
            composeSourceList.add(composeSource);
        }

        String objectName = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileName;

        //合并分块（指定合并后的文件名以及分块的来源）
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder().bucket(videoFiles)
                .object(objectName)
                .sources(composeSourceList)//分块的来源
                .build();
        try {
            //1、合并分块
            minioClient.composeObject(composeObjectArgs);
            //2、往media_file中添加数据
            MediaFiles mediaFiles = getById(fileMd5);
            if (mediaFiles==null){//不需要重复添加
                mediaFiles = new MediaFiles();

                mediaFiles.setId(fileMd5);
                mediaFiles.setCompanyId(12222111L);//机构id，先写死
                mediaFiles.setCompanyName("java教育机构");
                mediaFiles.setFilename(fileName);
                mediaFiles.setFileType(XcPlusConstant.FILE_TYPE_VIDEO);//图片
                mediaFiles.setTags(XcPlusConstant.COURSE_TAG_VIDEO);
                mediaFiles.setBucket(videoFiles);
                mediaFiles.setFilePath(objectName);
                mediaFiles.setFileId(fileMd5);
                mediaFiles.setUrl("/"+videoFiles+"/"+objectName);
                mediaFiles.setUsername("张先生");//先写死
                mediaFiles.setCreateDate(LocalDateTime.now());
                mediaFiles.setStatus("1");
                mediaFiles.setAuditStatus(XcPlusConstant.OBJECT_AUDIT_STATUS_UNAUDITED);//默认为未审核??

                //获取文件的大小
                StatObjectArgs statObjectArgs = StatObjectArgs.builder().bucket(videoFiles)
                        .object(objectName).build();
                StatObjectResponse statObjectResponse = minioClient.statObject(statObjectArgs);
                mediaFiles.setFileSize(statObjectResponse.size());

                //保存media_file表
                save(mediaFiles);
            }


            //3、清除分块数据（你可以逐个删除，当然最好的就是批量删除，更高效）
            List<DeleteObject> objects = new ArrayList<>();
            for (int i = 0; i < chunkTotal; i++) {
                String chunkName  = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/"+i;
                //参数为具体的文件全路径名称
                DeleteObject deleteObject = new DeleteObject(chunkName);
                objects.add(deleteObject);
            }
            //批量删除分块
            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(videoFiles)
                    .objects(objects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            //这遍历操作一定要做，虽然看起来删除操作在上一步已经做完了
            //但是minio中好像规定了在完成删除操作后，一定要对删除结果做一次遍历，不然删除操作会失败
            for (Result<DeleteError> result : results) {//这一步一定不能省！！！
                DeleteError error = result.get();
                log.info("Error in deleting object " + error.objectName() + "; " + error.message());
            }

            //记录待处理的任务，判断是不是avi格式的视频，如果是，则要记录到media_process表中
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if ("avi".equals(suffix)){
                //记录任务
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtils.copyProperties(mediaFiles,mediaProcess);

                //视频处理状态
                mediaProcess.setStatus("1");
                //失败次数
                mediaProcess.setFailCount(0);
                mediaProcessMapper.insert(mediaProcess);

            }

            return RestResponse.success(true);
        } catch (Exception e) {
            e.printStackTrace();
        }




        return RestResponse.success(false);
    }



    public void getList(){

    }


}
