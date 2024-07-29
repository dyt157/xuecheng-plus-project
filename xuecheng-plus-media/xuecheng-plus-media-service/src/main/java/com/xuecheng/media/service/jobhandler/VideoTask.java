package com.xuecheng.media.service.jobhandler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.minio.Digest;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Program:VedioTask
 * @DATE: 2024/7/28
 */
@Component
@Slf4j
public class VideoTask {

    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Value("${minio.bucket.videofiles}")
    private String videoFiles;//视频文件桶

    @Resource
    private MinioClient minioClient;

    @Resource
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Resource
    private MediaFilesMapper mediaFilesMapper;


    @XxlJob("videoProcess")
    @Transactional
    public void videoProcess() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        log.info("开始执行第"+shardIndex+"批任务");

        //1、先查询需要处理的视频任务
        //条件：1、当前执行器的序号 = 数据库中任务的id和当前执行器的数量的余数
        //     2、任务状态为"未处理"，并且失败次数少于3次，
        // select * from media_process where id % shardTotal = shardIndex and (status = 1 or status = 3) and fail_count <=3
        log.info("mediaProcessMapper:{}",mediaProcessMapper);
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListForExecutor(shardIndex, shardTotal);
        //2、拿到任务后，开始进行处理，即进行转码操作
        if (!CollectionUtils.isEmpty(mediaProcesses)){

            mediaProcesses.forEach(mediaProcess -> {

                try {
                    //ffmpeg的路径（这个值到时应该写在配置文件中）
                    String ffmpeg_path = "D:\\java\\java-project\\xuecheng\\ffmpeg\\ffmpeg.exe";//ffmpeg的安装位置
                    //源avi视频的路径，这里需要从Minio下载到服务器本地
                    //String video_path = "D:\\java\\java-project\\xuecheng\\test.avi";
                    String video_path = downloadVideo(mediaProcess.getFilePath(),mediaProcess.getFilename());
                    //转换后mp4文件的名称
                    String mp4_name = mediaProcess.getFilename().substring(0,mediaProcess.getFilename().lastIndexOf("."))+".mp4";
                    //转换后mp4文件的路径
                    String mp4_path = video_path+mp4_name;
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
                    //开始视频转换，成功将返回success
                    String s = videoUtil.generateMp4();
                    System.out.println(s);

                    //3、处理完后，上传到Minio
                    //计算转码后mp4文件的md5值
                    String fileMd5 = DigestUtils.md5DigestAsHex(new FileInputStream(mp4_path));
                    log.info("新文件的md5值:{}",fileMd5);
                    String objectName  = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+mp4_name;
                    //上传文件
                    addFileToMinio(objectName,mp4_path);
                    //4、数据库的数据更新（如果上传成功，没有抛出异常）
                    //更新media_process、media_process_history、media_file
                    //media_file中与文件相关的字段要全部修改为新文件的信息
                    MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaProcess.getFileId());
                    mediaFiles.setUrl(videoFiles+"/"+objectName);//新文件的url
                    mediaFiles.setFileId(fileMd5);//新文件的md5
                    mediaFiles.setFilePath(objectName);
                    mediaFiles.setFilename(mp4_name);
                    mediaFiles.setFileSize(new File(mp4_path).length());
                    mediaFilesMapper.updateById(mediaFiles);
                    //修改id值
                    mediaFilesMapper.updateIdByFileId(mediaFiles.getFileId());

                    //删除media_process表中的数据
                    mediaProcessMapper.deleteById(mediaProcess.getId());
                    //转移到history表中
                    MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
                    BeanUtils.copyProperties(mediaFiles,mediaProcessHistory);
                    mediaProcessHistory.setId(mediaProcess.getId());//mediaFiles对象的id和MediaProcessHistory的id不一样，得重新设置
                    mediaProcessHistory.setStatus("2");
                    mediaProcessHistory.setFinishDate(LocalDateTime.now());
                    mediaProcessHistoryMapper.insert(mediaProcessHistory);

                    //5、删除本地服务器的文件(或者在搞个定时任务，定期进行清理)
                    //使用递归删除temp目录下所有的文件即可，这里就不做了，可以直接百度超代码即可

                } catch (Exception e) {
                    e.printStackTrace();
                    //转码失败，更新数据库表的失败次数
                    mediaProcess.setStatus("3");//表示处理失败
                    mediaProcess.setFailCount(mediaProcess.getFailCount()+1);
                    mediaProcessMapper.updateById(mediaProcess);
                    throw new RuntimeException();
                }



            });
        }

    }


    /**
     * 上传mp4文件到minio
     */
    private void addFileToMinio(String objectName, String mp4_path) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket(videoFiles)//桶的名称
                .object(objectName)//添加带路径的文件名（容器中）
                .filename(mp4_path)//本地的文件所在路径
                .build();
        minioClient.uploadObject(uploadObjectArgs);
        log.info("上传成功");


    }

    private String downloadVideo(String filePath,String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        //video_path:临时文件的路径，这里是写死的，正确做法应该是写在配置文件中
        String video_path = "D:\\java\\java-project\\xuecheng\\temp\\" + fileName;
        DownloadObjectArgs downloadObjectArgs = DownloadObjectArgs.builder()
                .bucket(videoFiles)
                .object(filePath)//指定带路径的文件名
                .filename(video_path)//本地的文件名
                .build();
        minioClient.downloadObject(downloadObjectArgs);
        System.out.println("下载成功");
        return video_path;


    }


}
