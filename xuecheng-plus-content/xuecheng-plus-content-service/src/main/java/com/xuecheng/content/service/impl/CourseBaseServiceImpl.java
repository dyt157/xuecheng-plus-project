package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.constant.XcPlusConstant;
import com.xuecheng.base.exception.ResultEnum;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParam;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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


    @Resource
    private CourseMarketService courseMarketService;

    @Resource
    private CourseCategoryService courseCategoryService;

    @Resource
    private TeachplanService teachplanService;

    @Resource
    private TeachplanMediaService teachplanMediaService;

    @Resource
    private  CourseTeacherService courseTeacherService;
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

    @Override
    @Transactional
    //TODO 这里不返回值，看看行不行
    public CourseBaseInfoDto saveCourse(AddCourseDto addCourseDto) {
        //保存基本信息和营销信息，涉及多条sql语句的执行，需要添加事务管理
        //1、进行参数的校验（虽然前端也会做校验，但为了防止老六程序员，后端最好也校验一下）
        paramCheck(addCourseDto);


        //2、属性拷贝
        CourseBase courseBase = BeanUtil.copyProperties(addCourseDto, CourseBase.class);
        CourseMarket courseMarket = BeanUtil.copyProperties(addCourseDto, CourseMarket.class);

        //新增课程基本信息
        //3、完善课程信息对象的其余的属性
        courseBase.setAuditStatus(XcPlusConstant.COURSE_AUDIT_STATUS_NOT_SUBMITTED);
        courseBase.setStatus(XcPlusConstant.COURSE_STATUS_UNPUBLISHED);
        //机构id和机构名称可以先暂时写死
        courseBase.setCompanyId(123121L);
        courseBase.setCompanyName("杭州星火教育有限公司");
        //创建人（先写死）和创建时间
        courseBase.setCreatePeople("小A");
        courseBase.setCreateDate(LocalDateTime.now());
        save(courseBase);

        //新增课程营销信息
        //一样先完善信息
        courseMarket.setId(courseBase.getId());//这个id来自于上面新插入记录的id值
        //最好再做个判断，如果收费，价格要大于0...
        //其实可以单独写一个方法，用于保存/更新营销信息，可以提高程序健壮性，因为可能在之后的业务中
        //存在一种情况，比如说机构提交了课程后，想要修改价格之类的，可以重复调用这个方法
        // 不过根本想不到这样做，思考的方向有点太远了....所以这里就不单独写方法了
        courseMarketService.save(courseMarket);


        //一般保存信息不同返回什么东西给前端，但这里好像业务要求需要返回新增的课程信息对象（DTO）

        return getCourseBaseInfoDto(courseBase.getId());
    }

    /**
     * 做参数校验的方法
     * @param addCourseDto
     */
    private void paramCheck(AddCourseDto addCourseDto) {

        if (StringUtils.isEmpty(addCourseDto.getName())){
            throw new XueChengPlusException(ResultEnum.COURSE_NAME_EMPTY);
        }

        if (StringUtils.isEmpty(addCourseDto.getGrade())){
            throw new XueChengPlusException(ResultEnum.COURSE_GRADE_EMPTY);
        }

        if (StringUtils.isEmpty(addCourseDto.getMt())){
            throw new XueChengPlusException(ResultEnum.COURSE_CATEGORY_EMPTY);
        }

        if (StringUtils.isEmpty(addCourseDto.getSt())){
            throw new XueChengPlusException(ResultEnum.COURSE_CATEGORY_EMPTY);
        }
        if (StringUtils.isEmpty(addCourseDto.getCharge())){
            throw new XueChengPlusException(ResultEnum.COURSE_CHARGE_EMPTY);
        }

        if (StringUtils.isEmpty(addCourseDto.getUsers())){
            throw new XueChengPlusException(ResultEnum.COURSE_USER_EMPTY);
        }
        if (addCourseDto.getUsers().length()<10){
            throw new XueChengPlusException(ResultEnum.COURSE_USER_CONTENT_LENGTH);
        }

        if (addCourseDto.getCharge().equals(XcPlusConstant.COURSE_COST_CHARGE)){
            if (addCourseDto.getPrice()<=0){
                throw new XueChengPlusException(ResultEnum.COURSE_CHARGE_ERROR);
            }
        }

    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfoDto(Long courseId) {
        //查询基本信息和营销信息
        CourseBase courseBase = getById(courseId);
        CourseMarket courseMarket = courseMarketService.getById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();

        BeanUtil.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtil.copyProperties(courseMarket,courseBaseInfoDto);

        //此外有两个属性需要特别设置，大分类名称和小分类名称
        //从分类表中进行查询
        String mtName = courseCategoryService.getById(courseBaseInfoDto.getMt()).getName();
        String stName = courseCategoryService.getById(courseBaseInfoDto.getSt()).getName();

        courseBaseInfoDto.setMtName(mtName);
        courseBaseInfoDto.setStName(stName);


        return courseBaseInfoDto;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourse(EditCourseDto editCourseDto) {
        //1、参数校验
        paramCheck(editCourseDto);

        //2、完善信息
        //更新人，更新时间
        CourseBase courseBase = new CourseBase();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtil.copyProperties(editCourseDto,courseBase);
        BeanUtil.copyProperties(editCourseDto,courseMarket);

        courseBase.setChangeDate(LocalDateTime.now());
        courseBase.setChangePeople("小A");

        //3、更新数据库(两张表)

        updateById(courseBase);
        courseMarketService.updateById(courseMarket);

        //查询修改后的记录，封装成dto返回

        return getCourseBaseInfoDto(editCourseDto.getId());
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        //课程只有在未提交时才能删除（在前端已经做了处理，但为了保险，还是需要在后端做验证）
        CourseBase courseBase = getById(courseId);
        if (!courseBase.getAuditStatus().equals(XcPlusConstant.COURSE_AUDIT_STATUS_NOT_SUBMITTED)) {
            //不是未提交的状态
            throw new RuntimeException();

        }

        //删除课程信息，营销信息，课程计划，媒资信息，教师信息
        removeById(courseId);
        courseMarketService.removeById(courseId);
        teachplanService.remove(new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getCourseId,courseId));
        teachplanMediaService.remove(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getCourseId,courseId));
        courseTeacherService.remove(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId,courseId));

    }
}
