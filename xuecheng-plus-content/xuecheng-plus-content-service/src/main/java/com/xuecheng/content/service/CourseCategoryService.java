package com.xuecheng.content.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author itcast
 * @since 2022-10-08
 */
public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryDTO> getCategoryTree();

    List<Tree<String>> getCategoryTreeByHutool();

    List<CourseCategoryDTO> getCategoryTreeByDiGui();


}
