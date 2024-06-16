package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Override
    public List<CourseCategoryDTO> getCategoryTree() {
        //查询课程分类的节点结构
        List<CourseCategoryDTO> courseCategoryDTOList = new ArrayList<>();
        //1、把所有数据先查询出来
        List<CourseCategory> courseCategoryList = lambdaQuery().ne(CourseCategory::getParentid, "0")
                .list();

        courseCategoryList.forEach(courseCategory -> {
            CourseCategoryDTO courseCategoryDTO = new CourseCategoryDTO();
            BeanUtils.copyProperties(courseCategory, courseCategoryDTO);
            //进行遍历，如果该节点是parentid是1，则是父节点
            //如果不是1，则是子节点或者孙节点
            if (courseCategory.getParentid().equals("1")) {
                ArrayList<CourseCategoryDTO> childrenTreeNodes = new ArrayList<>();
                courseCategoryDTO.setChildrenTreeNodes(childrenTreeNodes);//避免之后第一次添加子节点数据出现空指针
                courseCategoryDTOList.add(courseCategoryDTO);
            } else {
                //子节点
                //需要先找到父节点
                courseCategoryDTOList.forEach(courseCategoryDTOParent -> {

                    if (courseCategory.getParentid().equals(courseCategoryDTOParent.getId())) {
                        //找到了这个子节点对应的父节点
                        //获取父节点中的子节点集合（不能创建新的）
                        List<CourseCategoryDTO> childrenTreeNodes =
                                courseCategoryDTOParent.getChildrenTreeNodes();
                        //往子节点集合中添加元素
                        childrenTreeNodes.add(courseCategoryDTO);
                        //每次添加完按照orderBy属性进行排序
                        childrenTreeNodes = childrenTreeNodes.stream()
                                .sorted(Comparator.comparing(CourseCategoryDTO::getOrderby)).collect(Collectors.toList());

                        courseCategoryDTOParent.setChildrenTreeNodes(childrenTreeNodes);

                    }

                });


            }

            //问题：
            // 1、如果遍历到子节点时，父节点还没有遍历，这样会导致子节点添加失败，怎么办？
            // 2、这个业务只有两级节点，如果是三级、四级，还能这样做吗？？

        });
        return courseCategoryDTOList;

    }


    /**
     * 使用hutool工具包，最方便
     * @return
     */
    @Override
    public List<Tree<String>> getCategoryTreeByHutool() {

        //1、把所有数据先查询出来
        List<CourseCategory> courseCategoryList = lambdaQuery().list();

        //配置
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 自定义属性名 都要默认值的
        treeNodeConfig.setWeightKey("orderBy");
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setParentIdKey("parentid");
        treeNodeConfig.setChildrenKey("childrenTreeNodes");

        //转换器,0表示最顶层的id是0
        List<Tree<String>> treeNodes = TreeUtil.build(courseCategoryList, "1", treeNodeConfig,
                (treeNode, tree) -> {
                    tree.setId(treeNode.getId());
                    tree.setParentId(treeNode.getParentid());
                    tree.setWeight(treeNode.getOrderby());
                    tree.setName(treeNode.getName());
                    // 扩展属性 ...
                    tree.putExtra("isLeaf", treeNode.getIsLeaf());
                    tree.putExtra("isShow", treeNode.getIsShow());
                    tree.putExtra("label", treeNode.getLabel());
                });

        return treeNodes;
    }


    /**
     * 使用传统的递归查询，也可以，稍微复杂点，以后可以以此作为模版解决类似问题
     */
    @Override
    public  List<CourseCategoryDTO> getCategoryTreeByDiGui() {
        List<CourseCategory> courseCategoryList = list();
        List<CourseCategoryDTO> courseCategoryDTOList = BeanUtil.copyToList(courseCategoryList, CourseCategoryDTO.class);

        //获取父节点
        List<CourseCategoryDTO> collect = courseCategoryDTOList.stream().filter(m -> m.getParentid().equals("1")).map(
                (m) -> {
                    m.setChildrenTreeNodes(getChildren(m, courseCategoryDTOList));
                    return m;
                }
        ).collect(Collectors.toList());
        return collect;
    }

    /**
     * 递归查询子节点
     * @param root  根节点
     * @param all   所有节点
     * @return 根节点信息
     */
    private List<CourseCategoryDTO> getChildren(CourseCategoryDTO root, List<CourseCategoryDTO> all) {
        List<CourseCategoryDTO> children = all.stream().filter(m -> {
            return Objects.equals(m.getParentid(), root.getId());
        }).map(
                (m) -> {
                    if (!CollectionUtils.isEmpty(getChildren(m, all))){
                        m.setChildrenTreeNodes(getChildren(m, all));
                    }
                    return m;
                }
        ).collect(Collectors.toList());
        return children;
    }
}


