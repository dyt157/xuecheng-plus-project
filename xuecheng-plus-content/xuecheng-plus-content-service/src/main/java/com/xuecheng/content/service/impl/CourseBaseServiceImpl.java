package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParam;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseBaseDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    @Override
    public PageResult<CourseBase> queryCourseBasePage(PageParam pageParam, QueryCourseBaseDTO queryCourseBaseDTO) {

        Page<CourseBase> page = new Page<>(pageParam.getPageNo(), pageParam.getPageSize());

        //使用MP分页查询
        String auditStatus = queryCourseBaseDTO.getAuditStatus();
        String courseName = queryCourseBaseDTO.getCourseName();
        String publishStatus = queryCourseBaseDTO.getPublishStatus();
        lambdaQuery().like(!StringUtils.isEmpty(courseName),CourseBase::getName,courseName)
                .eq(!StringUtils.isEmpty(auditStatus),CourseBase::getAuditStatus,auditStatus)
                .eq(!StringUtils.isEmpty(publishStatus),CourseBase::getStatus,publishStatus)
                .page(page);

        //创建响应对象
        PageResult<CourseBase> pageResult = new PageResult<>();
        pageResult.setPage(pageParam.getPageNo());
        pageResult.setItems(page.getRecords());
        pageResult.setPageSize(pageParam.getPageSize());
        pageResult.setCounts(page.getTotal());

        return pageResult;
    }
}
