package com.ygnn.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author FangKun
 */
@Data
public class SpuBoundTo {

    private Long spuId;

    private BigDecimal buyBounds;

    private BigDecimal growBounds;

}
