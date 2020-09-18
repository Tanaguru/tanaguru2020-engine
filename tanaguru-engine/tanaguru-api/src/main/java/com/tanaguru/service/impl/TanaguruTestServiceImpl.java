package com.tanaguru.service.impl;

import com.tanaguru.service.TanaguruTestService;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

@Service
@Transactional
public class TanaguruTestServiceImpl implements TanaguruTestService {


    public TanaguruTestServiceImpl() {
    }
}
