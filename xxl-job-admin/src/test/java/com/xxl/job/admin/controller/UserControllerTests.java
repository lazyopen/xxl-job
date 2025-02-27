package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.service.LoginService;
import com.xxl.job.core.biz.model.ReturnT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private XxlJobUserDao xxlJobUserDao;

    @Mock
    private XxlJobGroupDao xxlJobGroupDao;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIndex() {
        List<XxlJobGroup> groupList = new ArrayList<>();
        when(xxlJobGroupDao.findAll()).thenReturn(groupList);

        String result = userController.index(model);

        assertEquals("user/user.index", result);
        verify(model).addAttribute("groupList", groupList);
    }

    @Test
    void testPageList() {
        List<XxlJobUser> userList = new ArrayList<>();
        XxlJobUser user = new XxlJobUser();
        user.setPassword("testpass");
        userList.add(user);

        when(xxlJobUserDao.pageList(anyInt(), anyInt(), anyString(), anyInt())).thenReturn(userList);
        when(xxlJobUserDao.pageListCount(anyInt(), anyInt(), anyString(), anyInt())).thenReturn(1);

        Map<String, Object> result = userController.pageList(0, 10, "test", 1);

        assertEquals(1, result.get("recordsTotal"));
        assertEquals(1, result.get("recordsFiltered"));
        List<XxlJobUser> resultList = (List<XxlJobUser>) result.get("data");
        assertNull(resultList.get(0).getPassword());
    }

    @Test
    void testAdd_ValidUser() {
        XxlJobUser user = new XxlJobUser();
        user.setUsername("testuser");
        user.setPassword("testpass");

        when(xxlJobUserDao.loadByUserName(anyString())).thenReturn(null);

        ReturnT<String> result = userController.add(user);

        assertEquals(ReturnT.SUCCESS.getCode(), result.getCode());
        verify(xxlJobUserDao).save(any(XxlJobUser.class));
    }

    @Test
    void testAdd_InvalidUsername() {
        XxlJobUser user = new XxlJobUser();
        user.setUsername("t");
        user.setPassword("testpass");

        ReturnT<String> result = userController.add(user);

        assertEquals(ReturnT.FAIL_CODE, result.getCode());
    }

    @Test
    void testUpdate() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        XxlJobUser loginUser = new XxlJobUser();
        loginUser.setUsername("admin");
        request.setAttribute(LoginService.LOGIN_IDENTITY_KEY, loginUser);

        XxlJobUser user = new XxlJobUser();
        user.setUsername("testuser");
        user.setPassword("newpass");

        ReturnT<String> result = userController.update(request, user);

        assertEquals(ReturnT.SUCCESS.getCode(), result.getCode());
        verify(xxlJobUserDao).update(any(XxlJobUser.class));
    }

    @Test
    void testRemove() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        XxlJobUser loginUser = new XxlJobUser();
        loginUser.setId(2);
        request.setAttribute(LoginService.LOGIN_IDENTITY_KEY, loginUser);

        ReturnT<String> result = userController.remove(request, 1);

        assertEquals(ReturnT.SUCCESS.getCode(), result.getCode());
        verify(xxlJobUserDao).delete(1);
    }

    @Test
    void testUpdatePwd() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        XxlJobUser loginUser = new XxlJobUser();
        loginUser.setUsername("testuser");
        request.setAttribute(LoginService.LOGIN_IDENTITY_KEY, loginUser);

        XxlJobUser existingUser = new XxlJobUser();
        when(xxlJobUserDao.loadByUserName("testuser")).thenReturn(existingUser);

        ReturnT<String> result = userController.updatePwd(request, "newpassword");

        assertEquals(ReturnT.SUCCESS.getCode(), result.getCode());
        verify(xxlJobUserDao).update(any(XxlJobUser.class));
    }

    @Test
    void testUpdatePwd_InvalidPassword() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ReturnT<String> result = userController.updatePwd(request, "");

        assertEquals(ReturnT.FAIL.getCode(), result.getCode());
    }
}
