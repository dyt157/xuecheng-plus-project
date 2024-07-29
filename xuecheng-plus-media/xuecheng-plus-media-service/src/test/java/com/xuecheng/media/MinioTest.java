package com.xuecheng.media;

import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.impl.MediaFileServiceImpl;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Program:MinioTest
 * @DATE: 2024/7/5
 */
@SpringBootTest(classes = MinioTest.class)
public class MinioTest {
    //创建对应的MinioClient对象，这些对应的参数通常写在配置文件中
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.190.128:9000")//minio接口(有两个接口，不要搞错)
                    .credentials("minioadmin", "minioadmin")//用户名 密码
                    .build();


    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Resource
    private MediaFileServiceImpl mediaFileService;

    @Test
    public void uploadFile(){

        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("mediafiles")//桶的名称
                    .object("test/1.jpg")//添加带路径的文件名（容器中）
                    .filename("C:\\Users\\77321\\Desktop\\2023_02_16_15_29_IMG_0179.JPG")//本地的文件所在路径
//                    .contentType("image/jpeg")//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    @Test
    public void removeFile(){//删除文件

        //操作和上传文件差不多，无非就是minioClient调用不用的操作文件的方法
        //以及传入不同的参数（removeObjectArgs）
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket("mediafiles")
                    .object("test/1.jpg")//指定带路径的文件名
                    .build();
            minioClient.removeObject(removeObjectArgs);
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    @Test
    public void downLoadFile(){//下载文件

        try {
            DownloadObjectArgs downloadObjectArgs = DownloadObjectArgs.builder()
                    .bucket("mediafiles")
                    .object("test/1.jpg")//指定带路径的文件名
                    .filename("C:\\Users\\77321\\Desktop\\new.JPG")
                    .build();
            minioClient.downloadObject(downloadObjectArgs);
            System.out.println("下载成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("下载失败");
        }
    }

    @Test
    public void getFile(){//获取文件
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket("mediafiles")
                    .object("test/1.jpg")//指定带路径的文件名
                    .build();
            GetObjectResponse getObjectResponse = minioClient.getObject(getObjectArgs);

            System.out.println("文件存在");

        } catch (Exception e) {//抛出异常，说明文件不存在
            e.printStackTrace();
            System.out.println("文件不存在");
        }
    }


    @Test
    public void deleteFile() throws Exception {
        String fileMd5 = "255879536f30a93a43ddce3062f05958";
        List<DeleteObject> objects = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String chunkName  = fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/"+i;
            DeleteObject deleteObject = new DeleteObject(chunkName);
            objects.add(deleteObject);
        }
        //批量删除分块
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video")
                .objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        //遍历操作一定不能省，不然会删除失败！！！
        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            System.out.println("Error in deleting object " + error.objectName() + "; " + error.message());
        }

    }

    @Test
    public void test() throws IOException {
        System.out.println(mediaProcessMapper);
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListForExecutor(0, 1);
        System.out.println(mediaProcesses);

    }



}
