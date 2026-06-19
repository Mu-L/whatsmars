package org.hongxi.whatsmars.sentinel.webmvc.configure;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.hongxi.whatsmars.common.result.Result;
import org.hongxi.whatsmars.common.result.ResultHelper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by shenhongxi on 2020/9/15.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public Result<Void> handleBlockException(BlockException e) {
        log.warn("Blocked by Sentinel, resource: {}", e.getRule().getResource());
        return ResultHelper.newErrorResult(444, "Blocked by Sentinel");
    }
}
