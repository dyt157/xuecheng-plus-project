package com.xuecheng.content.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2022-10-07
 */
public interface TeachplanService extends IService<Teachplan> {

//    List<TeachplanDto> getTeachPlanById(Long courseId);
    List<Tree<String>>getTeachPlanById(Long courseId);

    void saveTeachPlan(SaveTeachplanDto saveTeachplanDto);

    void deletePlan(Long teachplanId);

    void moveup(Long teachplanId);

    void movedown(Long teachplanId);
}
