package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaFiles;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 媒资信息 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaFilesMapper extends BaseMapper<MediaFiles> {

    @Update("update media_files set id = #{fileId} where file_id = #{fileId}")
    int updateIdByFileId(String fileId);
}
