package com.xuecheng.media;

import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program:mediaTest
 * @DATE: 2024/7/28
 */
//@SpringBootTest
public class MediaTest {


    @Resource
    private MediaProcessMapper mediaProcessMapper;

//    @Test
    public void test(){
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListForExecutor(0, 1);
        System.out.println(mediaProcesses);
    }
}
