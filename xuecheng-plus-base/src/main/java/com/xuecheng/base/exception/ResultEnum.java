package com.xuecheng.base.exception;

/**
 * 结果信息枚举类
 */
public enum ResultEnum {


    //常见的错误信息
    UNKNOWN_ERROR("执行过程异常，请重试。"),
    PARAMS_ERROR("非法参数"),
    OBJECT_NULL("对象为空"),
    QUERY_NULL("查询结果为空"),
    REQUEST_NULL("请求参数为空"),

    //课程内容/计划业务相关的信息
    COURSE_NAME_EMPTY("课程名称不能为空"),
    COURSE_USER_EMPTY("适用人群不能为空"),
    COURSE_USER_CONTENT_LENGTH("适用人群描述内容过少"),
    COURSE_CATEGORY_EMPTY("课程分类不能为空"),
    COURSE_GRADE_EMPTY("课程等级不能为空"),
    COURSE_CHARGE_EMPTY("收费规则不能为空"),
    COURSE_CHARGE_ERROR("费用不能小于0"),
    COURSE_PLAN_ERROR("不能直接删除章节信息"),
    PLAN_MOVEUP_ERROR("已到达顶端，无法再上移"),
    PLAN_MOVEDOWN_ERROR("已到达底端，无法再下移");


    private String message;

    public String getMessage() {
        return message;
    }

    private ResultEnum( String message) {
        this.message = message;
    }

}
