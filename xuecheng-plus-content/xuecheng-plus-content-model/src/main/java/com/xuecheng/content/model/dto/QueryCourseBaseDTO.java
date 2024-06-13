package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Program:QueryCourseBaseDTO
 * @DATE: 2024/6/13
 */
@ApiModel("查询课程信息的分页条件")
@Data
public class QueryCourseBaseDTO {

    @ApiModelProperty("审核状态")
    private String auditStatus;
    @ApiModelProperty("课程名称")
    private String courseName;
    @ApiModelProperty("发布状态")
    private String publishStatus;
}
