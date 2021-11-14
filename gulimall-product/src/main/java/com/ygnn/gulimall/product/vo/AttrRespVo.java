package com.ygnn.gulimall.product.vo;

import lombok.Data;

/**
 * @author FangKun
 */
@Data
public class AttrRespVo extends AttrVo{

    /**
     * catelogName: 所属分类名字
     * groupName: 所属分组名字
     */
    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

}
