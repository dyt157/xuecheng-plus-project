package com.xuecheng.base.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Program:PageParam
 * @DATE: 2024/6/13
 */
@ApiModel(value = "分页相关参数类")
@Data
public class PageParam {

    @ApiModelProperty("当前页码")
    private Long pageNo = 1L;

    @ApiModelProperty("页数")
    private Long pageSize = 10L;

}
