package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.ResultEnum;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanMediaService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.HasSuperClassMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Resource
    private TeachplanMediaService teachplanMediaService;

    @Override
//    public List<TeachplanDto> getTeachPlanById(Long courseId) {
    public List<Tree<String>> getTeachPlanById(Long courseId) {

        List<TeachplanDto> teachplanDtoList = new ArrayList<>();

        //查询基本信息
        List<Teachplan> teachplanList = lambdaQuery().eq(Teachplan::getCourseId, courseId).list();
        //根据课程计划id查询对应的media记录
        //List<Long> planIdList = teachplanList.stream().map(Teachplan::getId).collect(Collectors.toList());

        teachplanList.forEach(teachplan -> {
            TeachplanDto teachplanDto = new TeachplanDto();
            BeanUtil.copyProperties(teachplan, teachplanDto);
            TeachplanMedia teachplanMedia = teachplanMediaService.lambdaQuery()
                    .eq(TeachplanMedia::getTeachplanId, teachplan.getId()).one();
            if (teachplanMedia != null) {
                teachplanDto.setTeachplanMedia(teachplanMedia);
            }
            teachplanDtoList.add(teachplanDto);
        });
        //至此，封装好了原始数据


        //封装为树形结构数据
        return getTeachPlanTree(teachplanDtoList);

    }

    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        //保存课程计划
        //属性拷贝、完善信息
        Teachplan teachplan = BeanUtil.copyProperties(saveTeachplanDto, Teachplan.class);
        teachplan.setCreateDate(LocalDateTime.now());

        //判断是新增还是修改
        if (saveTeachplanDto.getId() == null) {
            //新增
            //这个是关键！！
            //先判断层级是多少
            //grade=1代表新增章节
            //grade=2代表新增某一章的小节
            Integer count;
            if (teachplan.getGrade() == 1) {

                //根据课程id以及parentid=0这个条件可以查询出
                count = lambdaQuery().eq(Teachplan::getCourseId, teachplan.getCourseId()).eq(Teachplan::getParentid, 0)
                        .count();

            } else {
                //先找到对应的章节
                Long parentid = teachplan.getParentid();
                count = lambdaQuery().eq(Teachplan::getParentid, parentid).count();

            }
            teachplan.setOrderby(count + 1);

            save(teachplan);
        } else {
            //修改
            teachplan.setChangeDate(LocalDateTime.now());
            updateById(teachplan);

        }


    }

    @Override
    @Transactional
    public void deletePlan(Long teachplanId) {
        //删除课程计划
        //删除第一级别的大章节时要求大章节下边没有小章节时方可删除。
        //删除第二级别的小章节的同时需要将teachplan_media表关联的信息也删除。
        //首先判断是不是章节
        if (getById(teachplanId).getParentid()==0) {
            //是章节，需要找出所有小节
            Integer count = lambdaQuery().eq(Teachplan::getParentid, teachplanId).count();
            if (count!=0){
                //章节下面有小节，不能删除
                throw new XueChengPlusException(ResultEnum.COURSE_PLAN_ERROR);

            }

        }

        //程序能到这里说明，说明符合条件
        removeById(teachplanId);
        teachplanMediaService
                .remove(new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getTeachplanId,teachplanId ));//如果是章节，执行这步也没关系，因为肯定删除不了任何记录的

    }

    @Override
    @Transactional
    public void moveup(Long teachplanId) {

        //上移计划，修改orderBy字段的值，-1
        Teachplan teachplan = getById(teachplanId);
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        Long courseId = teachplan.getCourseId();
        if (orderby==1){
            throw new XueChengPlusException(ResultEnum.PLAN_MOVEUP_ERROR);
        }
        //如果是章节上移，则是整个章节一起上移
        //查询出前一个小节/章节对象
        Teachplan previous = lambdaQuery().eq(Teachplan::getParentid, parentid)
                .eq(Teachplan::getCourseId,courseId)
                .eq(Teachplan::getOrderby, orderby - 1).one();
        previous.setOrderby(orderby);
        teachplan.setOrderby(orderby-1);
        updateById(previous);
        updateById(teachplan);

    }

    @Override
    public void movedown(Long teachplanId) {

        //下移计划，修改orderBy字段的值，+1
        Teachplan teachplan = getById(teachplanId);
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        Long courseId = teachplan.getCourseId();
        //找出当前章节排在最后的小节的orderBy值
        List<Teachplan> teachplanList = lambdaQuery().eq(Teachplan::getParentid, parentid)
                .eq(Teachplan::getCourseId,courseId)
                .list();
        List<Integer> orderByList = teachplanList.stream().map(Teachplan::getOrderby).collect(Collectors.toList());
        Integer maxOrderBy = Collections.max(orderByList);

        if (Objects.equals(orderby, maxOrderBy)){
            throw new XueChengPlusException(ResultEnum.PLAN_MOVEDOWN_ERROR);
        }

        //查询出下一个小节/章节对象
        Teachplan next = lambdaQuery().eq(Teachplan::getParentid, parentid)
                .eq(Teachplan::getCourseId,courseId)
                .eq(Teachplan::getOrderby, orderby+1).one();
        teachplan.setOrderby(next.getOrderby());
        next.setOrderby(orderby);
        updateById(next);
        updateById(teachplan);



    }

    private List<Tree<String>> getTeachPlanTree(List<TeachplanDto> teachplanDtoList) {


        //第一种方法：使用hutool工具
        //配置
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 自定义属性名 都要默认值的
        treeNodeConfig.setWeightKey("orderBy");
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setParentIdKey("parentid");
        treeNodeConfig.setChildrenKey("teachPlanTreeNodes");

        //转换器,0表示最顶层的id是0
        List<Tree<String>> treeNodes = TreeUtil.build(teachplanDtoList, "0", treeNodeConfig,
                (treeNode, tree) -> {
                    tree.setId(treeNode.getId().toString());
                    tree.setParentId(treeNode.getParentid().toString());
                    tree.setWeight(treeNode.getOrderby());
                    // 扩展属性 ...
                    tree.putExtra("pname", treeNode.getPname());
                    tree.putExtra("grade", treeNode.getGrade());
                    tree.putExtra("mediaType", treeNode.getMediaType());
                    tree.putExtra("startTime", treeNode.getStartTime());
                    tree.putExtra("endTime", treeNode.getEndTime());
                    tree.putExtra("description", treeNode.getDescription());
                    tree.putExtra("timelength", treeNode.getTimelength());
                    tree.putExtra("courseId", treeNode.getCourseId());
                    tree.putExtra("coursePubId", treeNode.getCoursePubId());
                    tree.putExtra("status", treeNode.getStatus());
                    tree.putExtra("isPreview", treeNode.getIsPreview());
                    tree.putExtra("createDate", treeNode.getCreateDate());
                    tree.putExtra("changeDate", treeNode.getChangeDate());
                    tree.putExtra("teachplanMedia", treeNode.getTeachplanMedia());

                });

        treeNodes.forEach(tree -> {
            if (CollectionUtils.isEmpty(tree.getChildren())) {
                tree.setChildren(null);
            }
        });


        return treeNodes;
    }

}
