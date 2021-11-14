package com.ygnn.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author FangKun
 */
@Data
public class SkuReductionTo {

    private Long skuId;

    private int fullCount;

    private BigDecimal discount;

    private int countStatus;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private List<MemberPrice> memberPrice;

}
