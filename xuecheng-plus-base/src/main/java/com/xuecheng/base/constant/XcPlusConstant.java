package com.xuecheng.base.constant;

/**
 * @Program:StatusConstant
 * @DATE: 2024/6/15
 */

/**
 * 一些状态属性的常量
 */
public class XcPlusConstant {

    //课程审核状态
    public static final String COURSE_AUDIT_STATUS_FAIL = "202001";
    public static final String COURSE_AUDIT_STATUS_SUCCESS = "202004";
    public static final String COURSE_AUDIT_STATUS_NOT_SUBMITTED = "202002";
    public static final String COURSE_AUDIT_STATUS_SUBMITTED = "202003";
    //课程发布状态
    public static final String COURSE_STATUS_UNPUBLISHED = "203001";//未发布
    public static final String COURSE_STATUS_PUBLISHED = "203002";//已发布
    public static final String COURSE_STATUS_OFFLINE = "203003";//下线

    //收费状态
    public static final String COURSE_COST_CHARGE = "201001";//收费
    public static final String COURSE_COST_FREE = "201000";//免费

    //对象审核状态（用于媒体文件的审核）
    public static final String OBJECT_AUDIT_STATUS_FAIL = "002001";
    public static final String OBJECT_AUDIT_STATUS_SUCCESS = "002003";
    public static final String OBJECT_AUDIT_STATUS_UNAUDITED = "002002";

    //文件资源类型
    public static final String FILE_TYPE_IMAGE = "001001";
    public static final String FILE_TYPE_VIDEO = "001002";
    public static final String FILE_TYPE_OTHER = "001003";

    //课程资源标签
    public static final String COURSE_TAG_IMAGES = "课程图片";
    public static final String COURSE_TAG_VIDEO = "课程视频";



}
