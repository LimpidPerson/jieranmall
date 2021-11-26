package com.ygnn.gulimall.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class GulimallGatewayApplicationTests {

    @Test
    void contextLoads() {
        String integers = BubblingSorting(new Integer[]{34, 12, 56, 78});
        System.out.println(integers);
    }

    @Test
    public String BubblingSorting(Integer[] arr){

        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1; j++) {
                if (arr[i] > arr[i + 1]) {
                    arr[i] = arr[i + 1];
                }
            }
        }
        System.out.println(Arrays.toString(arr));
        return arr.toString();
    }

}
