package com.xuecheng.content.api;

import cn.hutool.core.lang.tree.Tree;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.LifecycleState;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program:TeachPlanController
 * @DATE: 2024/6/16
 */
@RestController
@Api(tags = "课程计划相关接口")
public class TeachPlanController {


    @Resource
    private TeachplanService teachplanService;

    @GetMapping("teachplan/{courseId}/tree-nodes")
    @ApiOperation("根据id查询课程计划")
//    public List<TeachplanDto> getTeachPlan(@PathVariable Long courseId){
    public List<Tree<String>> getTeachPlan(@PathVariable Long courseId){

        return teachplanService.getTeachPlanById(courseId);

    }


    @PostMapping("teachplan")
    @ApiOperation("新增课程计划")
    public void saveTeachPlan(@RequestBody SaveTeachplanDto saveTeachplanDto){

        teachplanService.saveTeachPlan(saveTeachplanDto);

    }


}
