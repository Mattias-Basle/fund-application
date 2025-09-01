package com.example.fund_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public abstract class AbstractCT {

    @Autowired
    protected MockMvc mockMvc;
}
