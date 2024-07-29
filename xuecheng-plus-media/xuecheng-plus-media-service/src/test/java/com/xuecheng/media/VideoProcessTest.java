package com.xuecheng.media;

import com.xuecheng.base.utils.Mp4VideoUtil;
import org.junit.jupiter.api.Test;

/**
 * @Program:VideoProcessTest
 * @DATE: 2024/7/15
 */
public class VideoProcessTest {

    @Test
    public void process(){
        //ffmpeg的路径
        String ffmpeg_path = "D:\\java\\java-project\\xuecheng\\ffmpeg\\ffmpeg.exe";//ffmpeg的安装位置
        //源avi视频的路径
        String video_path = "D:\\java\\java-project\\xuecheng\\test.avi";
        //转换后mp4文件的名称
        String mp4_name = "test.mp4";
        //转换后mp4文件的路径
        String mp4_path = "D:\\java\\java-project\\xuecheng\\test.mp4";
        //创建工具类对象
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
        //开始视频转换，成功将返回success
        String s = videoUtil.generateMp4();
        System.out.println(s);
    }
}
