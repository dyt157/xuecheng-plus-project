package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanMediaService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        teachplanList.forEach(teachplan->{
            TeachplanDto teachplanDto = new TeachplanDto();
            BeanUtil.copyProperties(teachplan,teachplanDto);
            TeachplanMedia teachplanMedia = teachplanMediaService.lambdaQuery()
                    .eq(TeachplanMedia::getTeachplanId, teachplan.getId()).one();
            if (teachplanMedia!=null){
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
        if (saveTeachplanDto.getId()==null){
            //新增
            //这个是关键！！
            //先判断层级是多少
            //grade=1代表新增章节
            //grade=2代表新增某一章的小节
            Integer count;
            if (teachplan.getGrade()==1){

                //根据课程id以及parentid=0这个条件可以查询出
                count= lambdaQuery().eq(Teachplan::getCourseId, teachplan.getCourseId()).eq(Teachplan::getParentid, 0)
                        .count();

            }else {
                //先找到对应的章节
                Long parentid = teachplan.getParentid();
                count = lambdaQuery().eq(Teachplan::getParentid, parentid).count();

            }
            teachplan.setOrderby(count+1);

            save(teachplan);
        }else {
            //修改
            teachplan.setChangeDate(LocalDateTime.now());
            updateById(teachplan);

        }



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
        List<Tree<String>> treeNodes = TreeUtil.build(teachplanDtoList,"0", treeNodeConfig,
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
            if (CollectionUtils.isEmpty(tree.getChildren())){
                tree.setChildren(null);
            }
        });


        return treeNodes;
    }




}
