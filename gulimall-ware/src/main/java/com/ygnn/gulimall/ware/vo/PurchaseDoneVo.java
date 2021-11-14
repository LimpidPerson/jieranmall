package com.ygnn.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author FangKun
 */
@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;    // 采购单ID

    private List<PurchaseItemDoneVo> items;

}
