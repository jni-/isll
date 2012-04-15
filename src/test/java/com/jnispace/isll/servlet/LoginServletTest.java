package com.jnispace.isll.servlet;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.facade.ISLLLoginFacade;
import com.jnispace.isll.facade.dto.ResponseDTO;

public class LoginServletTest {
    private final static String RESPONSE_DTO_FAKE_JSON = "some fake json";
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";

    private Logger logger;
    private ISLLConfig config;
    private ISLLLoginFacade facade;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PrintWriter writer;
    private RequestDispatcher dispatcher;

    private LoginServlet servlet;

    @Before
    public void setUp() throws Exception {
        createMocks();
        createServlet();
    }

    @Test
    public void getIsSimplyDispatched() throws Exception {
        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    public void postUsesUsernameAndPasswordFromRequest() throws Exception {
        servlet.doPost(request, response);

        verify(request, atLeastOnce()).getParameter(LoginServlet.USERNAME_FIELD);
        verify(request, atLeastOnce()).getParameter(LoginServlet.PASSWORD_FIELD);
    }

    @Test
    public void servletDispatchesTheRequestToTheFacade() throws Exception {
        servlet.doPost(request, response);

        verify(facade).authenticate(USERNAME, PASSWORD);
    }

    @Test
    public void ifRequestIsAJAXThenJsonIsOutputedToTheWriter() throws Exception {
        makeRequestAjax();

        servlet.doPost(request, response);

        verifyZeroInteractions(dispatcher);
        verify(servlet).dtoToJson(any(ResponseDTO.class));
        verify(writer).append(anyString());
    }

    @Test
    public void ifRequestIsNotAJAXThenTheRequestIsDispatched() throws Exception {
        servlet.doPost(request, response);

        verifyZeroInteractions(writer);
        verify(dispatcher).forward(request, response);
    }

    private void makeRequestAjax() {
        when(request.getHeader(LoginServlet.X_REQUESTED_WITH_HEADER)).thenReturn(LoginServlet.XML_HTTP_REQUEST_HEADER);
    }

    private void createMocks() throws IOException {
        logger = mock(Logger.class);
        facade = mock(ISLLLoginFacade.class);
        config = new ISLLConfig();
        writer = mock(PrintWriter.class);

        request = mock(HttpServletRequest.class);
        when(request.getParameter(LoginServlet.USERNAME_FIELD)).thenReturn(USERNAME);
        when(request.getParameter(LoginServlet.PASSWORD_FIELD)).thenReturn(PASSWORD);

        response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(writer);
    }

    private void createServlet() {
        servlet = spy(new LoginServlet(logger));
        doReturn(facade).when(servlet).getFacade();
        doReturn(config).when(servlet).getConfig();
        doReturn(RESPONSE_DTO_FAKE_JSON).when(servlet).dtoToJson(any(ResponseDTO.class));

        ServletConfig servletConfig = mock(ServletConfig.class);
        ServletContext servletContext = mock(ServletContext.class);
        dispatcher = mock(RequestDispatcher.class);

        doReturn(servletConfig).when(servlet).getServletConfig();
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }
}
