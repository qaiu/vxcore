package cn.qaiu.demo.aop.service;

import cn.qaiu.vx.core.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CalculatorService {
    private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);

    public int add(int a, int b) {
        log.info("CalculatorService.add({}, {})", a, b);
        return a + b;
    }

    public int divide(int a, int b) {
        log.info("CalculatorService.divide({}, {})", a, b);
        if (b == 0) throw new ArithmeticException("Division by zero");
        return a / b;
    }
}
