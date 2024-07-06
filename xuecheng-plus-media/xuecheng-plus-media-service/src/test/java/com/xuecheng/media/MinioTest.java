package com.xuecheng.media;

import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Program:MinioTest
 * @DATE: 2024/7/5
 */
//@SpringBootTest(classes = MinioTest.class)
public class MinioTest {
    //创建对应的MinioClient对象，这些对应的参数通常写在配置文件中
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.190.128:9000")//minio接口(有两个接口，不要搞错)
                    .credentials("minioadmin", "minioadmin")//用户名 密码
                    .build();


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
    public void getFile(){//查询文件/下载文件

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
}
