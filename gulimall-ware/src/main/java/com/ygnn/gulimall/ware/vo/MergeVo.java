package com.ygnn.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author FangKun
 */
@Data
public class MergeVo {

    private Long purchaseId;

    private List<Long> items;

}
