package org.hongxi.whatsmars.sentinel.webmvc.configure;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.hongxi.whatsmars.common.result.Result;
import org.hongxi.whatsmars.common.result.ResultHelper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2020/9/15.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public Result<Void> handleBlockException(BlockException e) {
        log.warn("Blocked by Sentinel, resource: {}", e.getRule().getResource());
        return ResultHelper.newErrorResult(444, "Blocked by Sentinel");
    }
}
