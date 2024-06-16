package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParam;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Program:CourseBaseController
 * @DATE: 2024/6/13
 */
@RestController
@Api(tags = "课程信息接口")
public class CourseBaseController {

    @Resource
    private CourseBaseService courseBaseService;

    @PostMapping("/course/list")
    @ApiOperation("条件分页查询课程基本信息")
    public PageResult<CourseBase> list(PageParam pageParam,
                                       @RequestBody QueryCourseBaseDTO queryCourseBaseDTO){

        return courseBaseService.queryCourseBasePage(pageParam,queryCourseBaseDTO);
    }

    @PostMapping("/course")
    @ApiOperation("添加课程基本信息")
    public CourseBaseInfoDto saveCourseBase(@RequestBody AddCourseDto addCourseDto){

        return courseBaseService.saveCourse(addCourseDto);

    }


    @GetMapping("/course/{courseId}")
    @ApiOperation("根据id查询课程信息")
    public CourseBaseInfoDto getCourseBaseInfoDto(@PathVariable Long courseId){

        return courseBaseService.getCourseBaseInfoDto(courseId);
    }

    @PutMapping("/course")
    @ApiOperation("修改课程信息")
    public CourseBaseInfoDto updateCourse(@RequestBody EditCourseDto editCourseDto){

        return courseBaseService.updateCourse(editCourseDto);
    }

}
