package com.xuecheng.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.system.model.po.Dictionary;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface DictionaryMapper extends BaseMapper<Dictionary> {

    List<Dictionary> selectAll();

}
