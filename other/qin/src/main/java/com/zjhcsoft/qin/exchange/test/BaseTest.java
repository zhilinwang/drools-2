package com.zjhcsoft.qin.exchange.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:*/qin-context.xml", "classpath:*/qin-cache.xml", "classpath:*/qin-ds.xml", "classpath:*/qin-mvc.xml", "classpath:spring-*.xml"})
public abstract class BaseTest extends AbstractTransactionalJUnit4SpringContextTests {

}
