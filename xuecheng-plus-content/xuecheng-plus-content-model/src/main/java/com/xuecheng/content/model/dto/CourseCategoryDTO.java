package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * @Program:CourseCategoryDTO
 * @DATE: 2024/6/14
 */
@Data
public class CourseCategoryDTO extends CourseCategory {

    //子节点的数据集合
    private List<CourseCategoryDTO> childrenTreeNodes;

}
