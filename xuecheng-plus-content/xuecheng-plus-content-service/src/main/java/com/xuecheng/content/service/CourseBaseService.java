package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParam;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseBaseDTO;
import com.xuecheng.content.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author itcast
 * @since 2022-10-07
 */
public interface CourseBaseService extends IService<CourseBase> {

    PageResult<CourseBase> queryCourseBasePage(PageParam pageParam, QueryCourseBaseDTO queryCourseBaseDTO);

    CourseBaseInfoDto saveCourse(AddCourseDto addCourseDto);

    CourseBaseInfoDto getCourseBaseInfoDto(Long courseId);

    CourseBaseInfoDto updateCourse(EditCourseDto editCourseDto);
}
