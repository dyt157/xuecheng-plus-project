package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Program:CourseTeacherContrller
 * @DATE: 2024/6/29
 */
@RestController
@Api(tags = "教师相关接口")
public class CourseTeacherController {

    @Resource
    private CourseTeacherService courseTeacherService;

    @GetMapping("courseTeacher/list/{courseId}")
    @ApiOperation("根据课程id查询出对应的教师信息")
    public List<CourseTeacher> getCourseTeachers(@PathVariable Long courseId){
        return courseTeacherService.getCourseTeachers(courseId);
    }

    @PostMapping("/courseTeacher")
    @ApiOperation("保存/修改教师信息")
    public void saveCourseTeachers(@RequestBody CourseTeacher courseTeacher){
        Long id = courseTeacher.getId();
        if (id==null){
            courseTeacher.setCreateDate(LocalDateTime.now());
            courseTeacherService.save(courseTeacher);
        }else {
            courseTeacherService.updateById(courseTeacher);
        }

    }


    @DeleteMapping("courseTeacher/course/{courseId}/{teacherId}")
    @ApiOperation("删除教师信息")
    public void delete(@PathVariable Long teacherId,@PathVariable Long courseId){
        courseTeacherService.removeById(teacherId);
    }


}
