package com.ygnn.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {

    /**
     * 库存工作单ID
     */
    private Long id;

    /**
     * 工作详情额所有ID
     */
    private StockDetailTo detailId;

}
