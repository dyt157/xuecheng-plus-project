package com.xuecheng.content.api;

import cn.hutool.core.lang.tree.Tree;
import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program:CourseCategoryController
 * @DATE: 2024/6/14
 */
@RestController
@Api(tags = "课程分类相关接口")
public class CourseCategoryController {


    @Resource
    private CourseCategoryService courseCategoryService;

    /*@GetMapping("/course-category/tree-nodes")
    @ApiOperation("查询课程分类信息")
    public List<CourseCategoryDTO> list(){

        return courseCategoryService.getCategoryTree();
    }*/

    /*@GetMapping("/course-category/tree-nodes")
    @ApiOperation("查询课程分类信息")
    public List<CourseCategoryDTO> list(){

        return courseCategoryService.getCategoryTreeByDiGui();
    }*/


    @GetMapping("/course-category/tree-nodes")
    @ApiOperation("查询课程分类信息")
    public List<Tree<String>> list(){

        return courseCategoryService.getCategoryTreeByHutool();
    }


}
